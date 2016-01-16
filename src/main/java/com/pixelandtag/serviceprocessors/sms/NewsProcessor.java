package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.web.beans.RequestObject;

public class NewsProcessor extends GenericServiceProcessor{

	private final Logger logger = Logger.getLogger(NewsProcessor.class);
	private DBPoolDataSource ds;
	private InitialContext context;
	private CMPResourceBeanRemote cmpbean;
    
    public void initEJB() throws NamingException{
    	String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		 Properties props = new Properties();
		 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		 props.put(Context.PROVIDER_URL, "remote://"+mtsenderprop.getProperty("ejbhost")+":"+mtsenderprop.getProperty("ejbhostport"));
		 props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
		 props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
		 props.put("jboss.naming.client.ejb.context", true);
		 context = new InitialContext(props);
		 cmpbean =  (CMPResourceBeanRemote) 
       		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		 
		 logger.info("Successfully initialized EJB CMPResourceBeanRemote !!");
    }
	public NewsProcessor() throws Exception{
		init_datasource();
		initEJB();
	}
	
	private void init_datasource(){
		
		
	}
	
	public OutgoingSMS process(IncomingSMS incomingsms) {
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		
		try {
			
			final RequestObject req = new RequestObject(incomingsms);
			
			final String KEYWORD = req.getKeyword().trim();
			final Long serviceid = 	incomingsms.getServiceid();
			conn = getCon();
			final int content_id =  Integer.valueOf(UtilCelcom.getServiceMetaData(conn,serviceid.intValue(),"dynamic_contentid"));
			final String sql = "SELECT c.`ID`, c.`Text` FROM `celcom`.`dynamiccontent_content` c WHERE c.`contentid`=? ORDER BY c.`timestamp` DESC LIMIT 0,1";
			
			logger.info(" KEYWORD ::::::::::::::::::::::::: ["+KEYWORD+"]");
			logger.info(" SERVICEID ::::::::::::::::::::::::: ["+serviceid+"]");
			
			
			if(KEYWORD.equalsIgnoreCase("berita")){
				
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, content_id);
				rs = pstmt.executeQuery();
				
				if(rs.next()){
					
					String news = rs.getString("Text").trim();
					outgoingsms.setSms(news);
					//int newsID = rs.getInt("ID");
				}
				
			}else{
				
				String unknown_keyword = UtilCelcom.getServiceMetaData(conn,-1,"unknown_keyword");
				
				if(unknown_keyword==null)
					unknown_keyword = "Unknown Keyword.";
				outgoingsms.setSms(unknown_keyword);
			}
			
			logger.info(incomingsms.toString());
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try{
				rs.close();
			}catch(Exception e){}
			try{
				pstmt.close();
			}catch(Exception e){}
			try{
				conn.close();
			}catch(Exception e){}
		}
		
		return outgoingsms;
	}



	
	
	
	/**
	 * Gets connection from datasource
	 */
	public Connection getCon() {
		
		try {
			
			return ds.getConnection();
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
			return null;
		
		}finally{
		
		}
	}

	@Override
	public void finalizeMe() {
		

		try{
			
			context.close();
		
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}
		
		try {
			
			ds.releaseConnectionPool();
			
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
		}
		
	}

	
	@Override
	public BaseEntityI getEJB() {
		return this.cmpbean;
	}

}
