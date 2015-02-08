package com.pixelandtag.staticcontent;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.api.MOProcessorFactory;
import com.pixelandtag.api.ServiceProcessorI;
import com.pixelandtag.dynamic.dbutils.DBConnection;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.serviceprocessors.sms.ServiceProcessorLoader;
import com.pixelandtag.subscription.SubscriptionOld;
import com.pixelandtag.subscription.SubscriptionMain;
import com.pixelandtag.subscription.dto.SMSServiceDTO;

public class ContentRetriever {
	
	private static final String DB_NAME = "pixeland_content360";
	private Logger log = Logger.getLogger(ContentRetriever.class);
	private SubscriptionOld sub = new SubscriptionOld();
	
	
	
	/**
	 * For dynamic content
	 * @param contentid
	 * @param conn
	 * @return
	 */
	public String getDynamicContent(int contentid,Connection conn){
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		final String sql = "SELECT c.`ID`, c.`Text` FROM `"+DB_NAME+"`.`dynamiccontent_content` c WHERE c.`contentid`=? ORDER BY c.`timestamp` DESC LIMIT 0,1";
		String content = null;
		try{
		
			pstmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, contentid);
			rs = pstmt.executeQuery();
				
			if(rs.next()){
					
				content = rs.getString("Text").trim();
					
				int newsID = rs.getInt("ID");
			}
			
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}finally{
			try {
				rs.close();
			} catch (Exception e) {
			}
			try {
				pstmt.close();
			} catch (Exception e) {
			}
		}
		
		return content;
			
	}


	/**
	 * For static content
	 * @param database_name
	 * @param table
	 * @param field
	 * @param idfield
	 * @param categoryfield
	 * @param categoryvalue
	 * @param msisdn
	 * @param serviceid
	 * @param size
	 * @param processor_id
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public String getUniqueFromCategory(String database_name,String table,
			String field,String idfield,String categoryfield,String categoryvalue,
			String msisdn,int serviceid,int size,int processor_id, Connection conn)throws SQLException {
		
		Statement stmt=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		String retval=null;
		int contentid=0;
		
		try {
			String categories[] = categoryvalue.split(",");
			String where="";
			
			// Build the where statement for the prepared statement
			for( int i=0; i<categories.length; i++ ) {
				// Only use LIKE if there is a % in the category since "=" will use the Index on the table whereas LIKE will not 
				where += "s."+categoryfield+((categories[i].indexOf("%")>=0)?" LIKE ":"=")+"? " +
					( ( (i+1)<categories.length )?" OR ":"");
			}
			
			pstmt=conn.prepareStatement(
				"SELECT s."+idfield+" AS id, s."+field+" AS txt, COUNT(log.id) AS cnt " +
				"FROM "+database_name+"."+table+" s "+
				"LEFT JOIN "+database_name+".contentlog log ON ( log.processor_id = "+processor_id+" AND log.serviceid = "+serviceid+" AND log.msisdn='"+msisdn+"' AND log.contentid=s.id ) " +
				"WHERE ( "+where+") "+
				"GROUP BY s.id "+
				"ORDER BY cnt ASC "+
				"LIMIT 0,1"
			);
			for( int i=0; i<categories.length; i++ ) 
				pstmt.setString(i+1,categories[i]);
			
			rs = pstmt.executeQuery();
			if ( rs.next() ) {
				retval = rs.getString("txt");
				contentid = rs.getInt("id");
				
				if ( rs.getInt("cnt")>0 ) {
					// We have been through all the content, so we clean out the logs
					stmt=conn.createStatement();
					stmt.execute("DELETE FROM "+database_name+".contentlog WHERE processor_id="+processor_id+" AND serviceid="+serviceid+" AND msisdn='"+msisdn+"'");
					stmt.close();
				}
				
			}
			rs.close();
			pstmt.close();
			
			if ( contentid>0 ) {
				stmt=conn.createStatement();
				String sql="";
				try {
					sql=(
						"INSERT DELAYED INTO "+database_name+".contentlog SET "+
						"processor_id="+processor_id+", serviceid="+serviceid+", msisdn='"+msisdn+"', timestamp=now(), contentid="+contentid
					);
					stmt.execute(sql);
				} catch ( Exception e ) {
					log.error(e+"\n"+sql,e);			
				}
				stmt.close();
			}
			
			//conn.close();
			
		} catch ( Exception e ) {
			log.error(e,e);
		} finally {
			try { rs.close(); } catch ( Exception e ) {}
			try { pstmt.close(); } catch ( Exception e ) {}
			try { stmt.close(); } catch ( Exception e ) {}
			//try { conn.close(); } catch ( Exception e ) {}
		}
		
		return retval;
	}
	
	/**
	 * For Static content
	 * @param database_name
	 * @param table
	 * @param field
	 * @param idfield
	 * @param msisdn
	 * @param serviceid
	 * @param size
	 * @param processor_id
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public String getUnique(String database_name,String table,String field,String idfield,String msisdn,int serviceid,int size,int processor_id, Connection conn) throws SQLException {
		
		Statement stmt=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		String retval=null;
		int contentid=0;
		
		try {
			String[] fields = field.split(",");
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < fields.length; i++) {
				if (i > 0)
					sb.append(", ");
				sb.append("s."+fields[i]+" AS " + fields[i]);
			}
	
			pstmt=conn.prepareStatement(
				"SELECT s."+idfield+" AS id, "+sb.toString()+", COUNT(log.id) AS cnt " +
				"FROM "+database_name+"."+table+" s "+
				"LEFT JOIN "+database_name+".contentlog log ON ( log.processor_id = "+processor_id+" AND log.ServiceId = "+serviceid+" AND log.MSISDN='"+msisdn+"' AND log.contentid=s.id ) " +
				"GROUP BY s.id "+
				"ORDER BY cnt ASC "+
				"LIMIT 0,1"
			);
			
			rs = pstmt.executeQuery();
			if ( rs.next() ) {
				for (int i = 0; i < fields.length; i++)  {
					if (i == 0)
						retval = rs.getString(fields[i]);
					else 
						retval += " " + rs.getString(fields[i]);
				}
				
				contentid = rs.getInt("id");
				
				if ( rs.getInt("cnt")>0 ) {
					// We have been through all the content, so we clean out the logs
					stmt=conn.createStatement();
					stmt.execute("DELETE FROM "+database_name+".contentlog WHERE processor_id="+processor_id+" AND serviceid="+serviceid+" AND msisdn='"+msisdn+"'");
					stmt.close();
				}
				
			}
			rs.close();
			pstmt.close();
			
			if ( contentid>0 ) {
				stmt=conn.createStatement();
				stmt.execute(
					"INSERT DELAYED INTO "+database_name+".contentlog SET "+
					"processor_id="+processor_id+", serviceid="+serviceid+", msisdn='"+msisdn+"', timestamp=CURDATE(), contentid="+contentid
				);
				stmt.close();
			}
			
			//conn.close();
			
		} catch ( Exception e ) {
			log.debug("Language: " + database_name + " Table: " + table);
			log.error(e,e);
		} finally {
			try { rs.close(); } catch ( Exception e ) {}
			try { pstmt.close(); } catch ( Exception e ) {}
			try { stmt.close(); } catch ( Exception e ) {}
			//try { conn.close(); } catch ( Exception e ) {}
		}
		
		return retval;		
	}
	
	
	public static void main(String[] args) {
		Connection conn  = DBConnection.createFromConnString(DBConnection.CONSTR);
		
		try{
		
			ContentRetriever cr = new ContentRetriever();
			String content = cr.getUniqueFromCategory("celcom_static_content", "more", "Text", "ID", "Category", "salam_raya", "254734821158", 102, 1, 2, conn);
				//cr.getUnique("celcom_static_content", "more", "Text", "ID", "254734821158", 100, 1, 1, conn);
			System.out.println(content);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * For subscriptions to get the service processor
	 * @param id
	 * @param conn
	 * @return
	 */
	public ServiceProcessorDTO getServiceProcessor(int id,Connection conn){

		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		ServiceProcessorDTO service = null;
		
		try {
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+CelcomImpl.database+"`.`mo_processors` WHERE `id`=?", Statement.RETURN_GENERATED_KEYS);//"SELECT * FROM `"+DATABASE+"`.`mo_processors` WHERE enabled=1");
			pstmt.setInt(1, id);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				service = new ServiceProcessorDTO();
				
				service.setId(rs.getInt("id"));
				service.setServiceName(rs.getString("ServiceName"));
				service.setProcessorClass(rs.getString("ProcessorClass"));
				//service.setCMP_Keyword(rs.getString("CMP_Keyword"));
				//service.setCMP_SKeyword(rs.getString("CMP_SKeyword"));
				service.setActive(rs.getBoolean("enabled"));
				service.setClass_status(rs.getString("class_status"));
				service.setShortcode(rs.getString("shortcode"));
//				if(rs.getString("keywords")!=null)
//					service.setKeywords(rs.getString("keywords").split("#"));
				service.setServKey(service.getProcessorClassName()+"_"+service.getCMP_AKeyword()+"_"+service.getCMP_SKeyword()+"_"+service.getShortcode());
				
				service.setThreads(rs.getInt("threads"));
				
			}
			
		}catch (Exception e) {
			
			log.error(e.getMessage(),e);
			
		}finally{
			
			
			try {
				
				if(rs!=null)
					rs.close();
			
			} catch (Exception e) {
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (Exception e) {}
			
			
		}
		
		return service;
	}
	
	
	/**
	 * Gets Content from the service_id
	 * @param service_id
	 * @param conn
	 * @return
	 */
	public MOSms getContentFromServiceId(int service_id, String msisdn, Connection conn) {
		
		String s  = "::::::::::::::::::::::::::::::::::::::::::::::::::::";
		log.info(s+" service_id["+service_id+"] msisdn["+msisdn+"]");
		SMSServiceDTO sm = sub.getSMSservice(service_id, conn);
		log.info(s+sm);
		MOSms mo = null;
		
		if(sm!=null){
			
			ServiceProcessorDTO procDTO = getServiceProcessor(sm.getMo_processor_FK(),conn);
			
			try {
				
				
				ServiceProcessorI processor =  MOProcessorFactory.getProcessorClass(procDTO.getProcessorClassName(), GenericServiceProcessor.class);
				mo = new MOSms();
				mo.setCMP_Txid(SubscriptionMain.generateNextTxId());
				mo.setMsisdn(msisdn);
				mo.setCMP_AKeyword(sm.getCmp_keyword());
				mo.setCMP_SKeyword(sm.getCmp_skeyword());
				mo.setPrice(BigDecimal.valueOf(sm.getPrice()));
				mo.setBillingStatus(mo.getPrice().compareTo(BigDecimal.ZERO)>0 ?  BillingStatus.WAITING_BILLING :   BillingStatus.NO_BILLING_REQUIRED);
				mo.setSMS_SourceAddr(procDTO.getShortcode());
				mo.setPriority(1);
				mo.setServiceid(sm.getId());
				mo.setSMS_Message_String(sm.getCmd());
				
				//added 22nd Dec 2014 - new customer requirement
				mo.setPricePointKeyword(sm.getPricePointKeyword());
				
				//added on 10th June 2013 but not tested
				mo.setProcessor_id(sm.getMo_processor_FK());
				
				
				
				// **** Below is a Dirty hack. *****
				//To 
				//cheat the content processor 
				//that this is a subscription push, 
				//so that it does not subscribe 
				//this subscriber to the service. 
				//We handle subscription elsewhere, 
				//this is solely for content fetcnhing 
				//and not subscribing.
				mo.setSubscriptionPush(true);
				
				mo = processor.process(mo);
				
				
			}catch(Exception e) {
				log.error(e.getMessage(),e);
			}
		}else{
			log.info(s+" sm is null!");
		}
		
		
		return mo;
	}

}
