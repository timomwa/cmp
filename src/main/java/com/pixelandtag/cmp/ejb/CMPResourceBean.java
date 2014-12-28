package com.pixelandtag.cmp.ejb;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.dynamic.dto.NoContentTypeException;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.exceptions.NoSettingException;
import com.pixelandtag.mms.api.TarrifCode;
import com.pixelandtag.serviceprocessors.dto.ServiceSubscription;
import com.pixelandtag.serviceprocessors.dto.SubscriptionDTO;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.subscription.dto.SubscriptionStatus;

@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class CMPResourceBean implements CMPResourceBeanRemote {
	
	
	private String server_tz = "-05:00";//TODO externalize

	private String client_tz = "+03:00";//TODO externalize
	
	
	private SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public void setServerTz(String server_tz)  throws Exception {
		this.server_tz = server_tz;
	}

	public void setClientTz(String client_tz)  throws Exception {
		this.client_tz = client_tz;
	}
	
	
	public String getServerTz()  throws Exception {
		return this.server_tz;
	}

	public String getClientTz()  throws Exception {
		return this.client_tz;
	}
	
	
	@Override
	public String getServiceMetaData(int serviceid, String meta_field)
			throws Exception {
		String value = "-1";
		
		try{
		
			String sql = "select meta_value from `"+CelcomImpl.database+"`.`sms_service_metadata` WHERE sms_service_id_fk=? AND meta_field=?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, serviceid);
			qry.setParameter(2, meta_field);
			Object rs = qry.getSingleResult();
		
			if(rs!=null){
				
				value = (String) rs;
				
			}else{
				throw new NoSettingException("No meta data with field name "+meta_field+", for serviceid "+serviceid);
			}
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
			throw e;
		
		}finally{
			
			
		}
		
		return value;
	}

	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String getUniqueFromCategory(String database_name,String table,
			String field,String idfield,String categoryfield,String categoryvalue,
			String msisdn,int serviceid,int size,int processor_id) throws Exception{
		String retval=null;
		int contentid=0;
		
		try {
			utx.begin();
			String categories[] = categoryvalue.split(",");
			String where="";
			
			// Build the where statement for the prepared statement
			for( int i=0; i<categories.length; i++ ) {
				// Only use LIKE if there is a % in the category since "=" will use the Index on the table whereas LIKE will not 
				where += "s."+categoryfield+((categories[i].indexOf("%")>=0)?" LIKE ":"=")+"? " +
					( ( (i+1)<categories.length )?" OR ":"");
			}
			String sql = "SELECT s."+idfield+" AS id, s."+field+" AS txt, COUNT(log.id) AS cnt " +
					"FROM "+database_name+"."+table+" s "+
					"LEFT JOIN "+database_name+".contentlog log ON ( log.processor_id = "+processor_id+" AND log.serviceid = "+serviceid+" AND log.msisdn='"+msisdn+"' AND log.contentid=s.id ) " +
					"WHERE ( "+where+") "+
					"GROUP BY s.id "+
					"ORDER BY cnt ASC "+
					"LIMIT 0,1";
			Query qry = em.createNativeQuery(sql);
			for( int i=0; i<categories.length; i++ ) 
				qry.setParameter(i+1,categories[i]);
			
			List<Object[]> rs = qry.getResultList();
			if (rs!=null ) {
				for(Object[] o : rs){
					retval = (String) o[1];
					contentid = (Integer) o[0];
					int count = ( (BigInteger) o[2] ).intValue();
					if ( count>0 ) {
						String sql2 = "DELETE FROM "+database_name+".contentlog WHERE processor_id="+processor_id+" AND serviceid="+serviceid+" AND msisdn='"+msisdn+"'";
						Query qry2 = em.createNativeQuery(sql2);
						qry2.executeUpdate();
					}
				}
				
			}
			
			if ( contentid>0 ) {
				String sql3="";
				try {
					sql3=(
						"INSERT DELAYED INTO "+database_name+".contentlog SET "+
						"processor_id="+processor_id+", serviceid="+serviceid+", msisdn='"+msisdn+"', timestamp=now(), contentid="+contentid
					);
					Query qry3 = em.createNativeQuery(sql3);
					qry3.executeUpdate();
				} catch ( Exception e ) {
					logger.error(e+"\n"+sql+"\n",e);			
				}
			}
			
			utx.commit();
			
		} catch ( Exception e ) {
			try{
			utx.rollback();
			}catch(Exception e1){}
			logger.error(e,e);
			throw e;
		} finally {
			
		}
		
		return retval;
	}
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean subscribe(String msisdn, int service_id, int smsmenu_levels_id_fk) throws Exception {
		boolean success = false;
		
		try{
			utx.begin();
			if(service_id>-1){
				String sql = "INSERT INTO `"+CelcomImpl.database+"`.`subscription`(sms_service_id_fk,msisdn,smsmenu_levels_id_fk) VALUES(?,?,?) ON DUPLICATE KEY UPDATE subscription_status=?";
				Query qry = em.createNativeQuery(sql);
				
				qry.setParameter(1, service_id);
				qry.setParameter(2, msisdn);
				qry.setParameter(3, smsmenu_levels_id_fk);
				qry.setParameter(4, SubscriptionStatus.waiting_confirmation.getStatus());
				
				if(qry.executeUpdate()>0)
					success = true;
				
			}else{
				success = false;
			}
				
			utx.commit();
			
		}catch(Exception e){
			try{
			utx.rollback();
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			throw e;
			
		}finally{
			
		}
		
		
		return success;
	}
	
	@SuppressWarnings("unchecked")
	public com.pixelandtag.subscription.dto.SubscriptionDTO getSubscriptionDTO(
			String msisdn, int sms_service_id_fk) throws Exception{
		
		com.pixelandtag.subscription.dto.SubscriptionDTO subscription = null;
		
		try{
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`subscription` WHERE msisdn=? AND sms_service_id_fk=? ORDER BY subscription_timeStamp asc LIMIT 1";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, msisdn);
			qry.setParameter(2, sms_service_id_fk);
				
			List<Object[]> rs = qry.getResultList();
			
			for(Object[] o: rs){
				
				subscription = new com.pixelandtag.subscription.dto.SubscriptionDTO();
				subscription.setId( (Integer) o[0] );// rs.getInt("id"));
				subscription.setSubscription_status( (String) o[1] );// rs.getString("subscription_status"));
				subscription.setSms_service_id_fk( (Integer) o[2] );//rs.getInt("sms_service_id_fk"));
				subscription.setMsisdn( (String) o[3] );//rs.getString("msisdn"));
				subscription.setSubscription_timeStamp(  sdf.format(( (java.sql.Timestamp) o[4]) ));//rs.getString("subscription_timeStamp"));
				subscription.setSmsmenu_levels_id_fk( (Integer) o[5] );// rs.getInt("smsmenu_levels_id_fk"));
					
			}
				
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
			
			
		}
		
		
		return subscription;
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getAdditionalServiceInfo(int serviceid) {
		Map<String, String> additionalInfo = null;
		
		
		try{
			String query = "SELECT "
					+ "service_name,"//0
					+ "subscriptionText,"//1
					+ "unsubscriptionText,"//2
					+ "tailText_subscribed,"//3
					+ "tailText_notsubscribed "//4
					+ "FROM `"+CelcomImpl.database+"`.`sms_service` WHERE id=?";
			Query qry = em.createNativeQuery(query);
			qry.setParameter(1, serviceid);
			
			List<Object[]> rs = qry.getResultList();
			int c = 0;
			for(Object[] o : rs){
			
				if(c==0)
					additionalInfo = new HashMap<String,String>();
				
				additionalInfo.put("subscriptionText", (String)o[1]);//rs.getString("subscriptionText"));
				additionalInfo.put("unsubscriptionText", (String)o[2]);//rs.getString("unsubscriptionText"));
				additionalInfo.put("tailText_subscribed", (String)o[3]);//rs.getString("tailText_subscribed"));
				additionalInfo.put("tailText_notsubscribed",(String)o[4]);//rs.getString("tailText_notsubscribed"));
				additionalInfo.put("service_name", (String)o[0]);//rs.getString("service_name"));
				c++;
			}
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
		
		}
		
		return additionalInfo;
	}
		
	public boolean testEJB(int k) throws Exception{
		if(k>0)
			return true;
		else
			throw new NoContentTypeException("No content Exception!! ");
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean updateServiceSubscription(int subscription_service_id)  throws Exception {
		boolean success = false;
		try{
			 utx.begin();
			 String sql = "UPDATE `"+GenericServiceProcessor.DB+"`.`ServiceSubscription` SET lastUpdated=convert_tz(now(),'"+server_tz+"','"+client_tz+"') WHERE id =:id";
			Query qry = em.createNativeQuery(sql);	
			qry.setParameter("id", subscription_service_id);
			int num =  qry.executeUpdate();
			 utx.commit();
			 success = num>0;
		}catch(Exception e){
			try {
				utx.rollback();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw e;
		}
		return success;
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@SuppressWarnings("unchecked")
	public List<String> listServiceMSISDN(String sub_status, int serviceid) throws Exception {
		List<String> msisdnL = new ArrayList<String>();
		
		try {
			
			utx.begin();
			String sql = "SELECT msisdn FROM `"+CelcomImpl.database+"`.`subscription` WHERE subscription_status=:subscription_status AND sms_service_id_fk =:sms_service_id_fk";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter("subscription_status", sub_status);
			qry.setParameter("sms_service_id_fk", serviceid);
			List<String> res = qry.getResultList();
			for(String o : res)
				msisdnL.add(o);
			
			
			
			utx.commit();
		} catch (Exception e) {
			try{
				utx.rollback();
			}catch(Exception e1){}
			logger.error(e.getMessage());
			throw e;
		}finally{
		}


		return msisdnL;
		
	}
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@SuppressWarnings("unchecked")
	public List<ServiceSubscription> getServiceSubscription()  throws Exception {
		
		List<ServiceSubscription> list = new ArrayList<ServiceSubscription>();

		try {
			
			utx.begin();
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`ServiceSubscription` WHERE hour(`schedule`)<=hour(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')) AND `lastUpdated`<convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"') AND ExpiryDate>convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')";//first get services to be pushed now.
			System.out.println(sql);
			Query qry = em.createNativeQuery(sql);
			List<Object[]> res = qry.getResultList();
			ServiceSubscription subdto;
			
			for(Object[] o : res){
				subdto = new ServiceSubscription();
				subdto.setId( ( (Integer) o[0]).intValue() );// rs.getInt("id"));
				subdto.setServiceid( ( (Integer) o[1]).intValue() );// rs.getInt("serviceid"));
				subdto.setSchedule( sdf.format(( (java.sql.Timestamp) o[2]) ));//rs.getString("schedule"));
				subdto.setLastUpdated( ( sdf.format((java.sql.Timestamp) o[3])) ) ;//rs.getString("lastUpdated"));
				subdto.setExpiryDate( sdf.format(( (java.sql.Timestamp) o[4]) ) );//rs.getString("ExpiryDate"));
				
				list.add(subdto);
			}
			
			
			utx.commit();
		} catch (Exception e) {
			try{
				utx.rollback();
			}catch(Exception e1){}
			logger.error(e.getMessage());
			throw e;
		}finally{
		}
		return list;
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
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@SuppressWarnings("unchecked")
	public String getUnique(String database_name,String table,String field,String idfield,String msisdn,int serviceid,int size,int processor_id, Connection conn) throws Exception {
		
		
		String retval=null;
		int contentid=0;
		
		try {
			
			utx.begin();
			
			String[] fields = field.split(",");
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < fields.length; i++) {
				if (i > 0)
					sb.append(", ");
				sb.append("s."+fields[i]+" AS " + fields[i]);
			}
			
			
			String sql = "SELECT s."+idfield+" AS id, "+sb.toString()+", COUNT(log.id) AS cnt " +
					"FROM "+database_name+"."+table+" s "+
					"LEFT JOIN "+database_name+".contentlog log ON ( log.processor_id = "+processor_id+" AND log.ServiceId = "+serviceid+" AND log.MSISDN='"+msisdn+"' AND log.contentid=s.id ) " +
					"GROUP BY s.id "+
					"ORDER BY cnt ASC "+
					"LIMIT 0,1";
	
			Query qry = em.createNativeQuery(sql);
			
			
			List<Object[]> objc = qry.getResultList();
			
			for(Object[] o : objc){
				for(int k = 0; k<o.length; k++){
					for (int i = 0; i < fields.length; i++)  {
						if (i == 0){
							retval = (String)o[k];
						}else{ 
							retval += " " + (String)o[k];
						}
					}
				}
				
				contentid = (Integer)o[0];
				int count =  (Integer)o[(o.length-1)];
				if ( count>0 ) {
					
					String sql2 = "DELETE FROM "+database_name+".contentlog WHERE processor_id="+processor_id+" AND serviceid="+serviceid+" AND msisdn='"+msisdn+"'";
					Query qry2 = em.createNativeQuery(sql2);
					qry2.executeUpdate();
				}
			}
			
			if ( contentid>0 ) {
				
				String sql3 = "INSERT DELAYED INTO "+database_name+".contentlog SET "+"processor_id="+processor_id+", serviceid="+serviceid+", msisdn='"+msisdn+"', timestamp=CURDATE(), contentid="+contentid;
				Query qry3 = em.createNativeQuery(sql3);
				qry3.executeUpdate();
			}
			
			utx.commit();
			
		} catch ( Exception e ) {
			try{
				utx.rollback();
			}catch(Exception e1){
				//logger.error(e1,e1);
			}
			logger.debug("Language: " + database_name + " Table: " + table);
			logger.error(e,e);
			throw e;
		} finally {
			
		}
		
		return retval;		
	}

	private Logger logger = Logger.getLogger(CMPResourceBean.class);
	
	@Resource
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	

	@Resource
	private UserTransaction utx;
	
	
	@Override
	public EntityManager getEM() {
		return em;
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T> T saveOrUpdate(T t) throws Exception{
		try{
			utx.begin();
			t = em.merge(t);
			utx.commit();
		}catch(Exception e){
			try {
				utx.rollback();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw e;
		}
		return t;
	}
	
	
	/**
	 * saves and commits
	 * @param t
	 * @return
	 * @throws Exception 
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T> T find(Class<T> entityClass, Long id) throws Exception {
		try{
			utx.begin();
			T t = em.find( entityClass,id);
			utx.commit();
			return t;
		}catch(Exception  e){
			try{
				utx.rollback();
			}catch(Exception ex){}
			throw e;
			
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T> T find(Class<T> entityClass, String param_name, Object value) throws Exception  {
		T t = null;
		try{
			utx.begin();
			Query query = em.createQuery("from " + entityClass.getSimpleName() + " WHERE "+param_name+" =:"+param_name+" ").setParameter(param_name, value);
			if(query.getResultList().size()>0)
				t = (T) query.getResultList().get(0);
			utx.commit();
		}catch(Exception  e){
			try{
				utx.rollback();
			}catch(Exception ex){}
			throw e;
		}
		return t;
	}
	
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean updateMessageInQueue(long cp_tx_id, BillingStatus billstatus) throws Exception{
		boolean success = false;
		try{
			 utx.begin();
			 Query qry = em.createNativeQuery("UPDATE httptosend set priority=:priority, charged=:charged, billing_status=:billing_status WHERE CMP_TxID=:CMP_TxID ")
			.setParameter("priority", billstatus.equals(BillingStatus.SUCCESSFULLY_BILLED) ? 0 :  3)
			.setParameter("charged", billstatus.equals(BillingStatus.SUCCESSFULLY_BILLED) ? 1 :  0)
			.setParameter("billing_status", billstatus.toString())
			.setParameter("CMP_TxID", cp_tx_id);
			int num =  qry.executeUpdate();
			 utx.commit();
			 success = num>0;
		}catch(Exception e){
			try {
				utx.rollback();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw e;
		}
		return success;
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean sendMT(MOSms mo, String sql) throws Exception{
		boolean success = false;
		try{
		 
			utx.begin();
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, mo.getMt_Sent());
			qry.setParameter(2, mo.getMsisdn());
			qry.setParameter(3, mo.getSMS_SourceAddr());
			qry.setParameter(4, mo.getSMS_SourceAddr());
			
			qry.setParameter(5, mo.getCMP_AKeyword());
			qry.setParameter(6, mo.getCMP_SKeyword());
			qry.setParameter(7, mo.getPriority());
			
			if(!(mo.getCMP_Txid()==-1)){
				qry.setParameter(8, String.valueOf(mo.getCMP_Txid()));
			}
			qry.setParameter(9, (mo.isSplit_msg() ? 1 : 0));
			qry.setParameter(10, mo.getServiceid());
			qry.setParameter(11, String.valueOf(mo.getPrice()));
			qry.setParameter(12, mo.getSMS_DataCodingId());
			qry.setParameter(13, mo.getProcessor_id());
			qry.setParameter(14, mo.getBillingStatus().toString());
			qry.setParameter(15, mo.getPricePointKeyword()==null ? "NONE" :  mo.getPricePointKeyword());
			qry.setParameter(16, mo.getBillingStatus().toString());
			
			int num =  qry.executeUpdate();
			utx.commit();
			success = num>0;
		
		}catch(Exception e){
			try {
				utx.rollback();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw e;
		}
		 
		return success;
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean  acknowledge(long message_log_id) throws Exception{
		boolean success = false;
		try{
		 
			utx.begin();
			Query qry = em.createNativeQuery("UPDATE `"+CelcomImpl.database+"`.`messagelog` SET mo_ack=1 WHERE id=?");
			qry.setParameter(1, message_log_id);
			int num =  qry.executeUpdate();
			utx.commit();
			success = num>0;
		
		}catch(Exception e){
			try {
				utx.rollback();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw e;
		}
		 
		return success;
	}
	
	/**
	 * To statslog
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean toStatsLog(MOSms mo, String presql)  throws Exception {
		boolean success = false;
		try{
		 
			utx.begin();
			Query qry = em.createNativeQuery(presql);
			qry.setParameter(1, mo.getServiceid());
			qry.setParameter(2, mo.getMsisdn());
			qry.setParameter(3, mo.getCMP_Txid());
			qry.setParameter(4, mo.getCMP_AKeyword());
			qry.setParameter(5, mo.getCMP_SKeyword());
			if(mo.getCMP_SKeyword().equals(TarrifCode.RM1.getCode()))
				qry.setParameter(6, 1d);
			else
				qry.setParameter(6, mo.getPrice().doubleValue());
			qry.setParameter(7, mo.isSubscriptionPush());
			
			int num =  qry.executeUpdate();
			utx.commit();
			success = num>0;
		
		}catch(Exception e){
			try {
				utx.rollback();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw e;
		}
		 
		return success;
		
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@SuppressWarnings("unchecked")
	public List<SubscriptionDTO> getSubscriptionServices()  throws Exception {
	
		String sub = "SELECT "
				+ "ss.id as 'service_subscription_id', "//0
				+ "pro.id as 'mo_processor_id_fk', "//1
				+ "pro.shortcode,"//2
				+ "pro.ServiceName,"//3
				+ "sm.cmd,"//4
				+ "sm.CMP_Keyword,"//5
				+ "sm.CMP_SKeyword,"//6
				+ "sm.price as 'price', "//7
				+ "sm.push_unique,"//8
				+ "ss.serviceid as 'sms_serviceid',"//9
				+ "pro.threads,"//10
				+ "pro.ProcessorClass as 'ProcessorClass',"//11
				+ "sm.price_point_keyword"//12
				+" FROM `"+CelcomImpl.database+"`.`ServiceSubscription` ss "
				+"LEFT JOIN `"+CelcomImpl.database+"`.`sms_service` sm "
				+"ON sm.id = ss.serviceid "
				+"LEFT JOIN `"+CelcomImpl.database+"`.`mo_processors` pro "
				+"ON pro.id = sm.mo_processorFK WHERE pro.enabled=1 AND hour(`ss`.`schedule`)=hour(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')) AND `ss`.`lastUpdated`<convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"') AND `ss`.`ExpiryDate`>convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')";
	    logger.info("\n\t"+sub+"\n");
	    System.out.println("\n\t"+sub+"\n");
		List<SubscriptionDTO> sub_services  = new ArrayList<SubscriptionDTO>();
	
		try {
			utx.begin();
			Query qry = em.createNativeQuery(sub);
			List<Object[]> list = qry.getResultList();
			SubscriptionDTO subdto = null;
			
			for(Object[] o : list){
				
				final int threads = ( (Integer) (o[10]) ).intValue() ;
				final int service_id = ( (Integer) (o[9]) ).intValue() ;
				final String service_processor_class_name = ( (String) (o[11]) );//rs.getString("ProcessorClass");
				
				for(int i = 0; i<threads; i++){
					
					
					logger.debug(" >>>>>>>>>>>>>>>>>>>>>>>>>>> service name >>>> "+(String) (o[3]));
				
					subdto = new SubscriptionDTO();
					subdto.setProcessor_id( ( (Integer) (o[1]) ).intValue());//rs.getInt("mo_processor_id_fk"));
					subdto.setShortcode(   (String) (o[2]) );//rs.getString("shortcode"));
					subdto.setServiceName(  (String) (o[3]) );//rs.getString("ServiceName"));
					subdto.setCmd( (String) (o[4]) );//rs.getString("cmd"));
					subdto.setCMP_AKeyword(  (String) (o[5]) );//rs.getString("CMP_Keyword"));
					subdto.setCMP_SKeyword( (String) (o[6]) );//rs.getString("CMP_SKeyword"));
					subdto.setPrice(new Double((Double) (o[7])));
					subdto.setPush_unique(( (Integer) (o[8]) ).intValue());//rs.getInt("push_unique"));
					subdto.setServiceid(service_id);
					subdto.setThreads(threads);
					subdto.setProcessorClass(service_processor_class_name);
					subdto.setId( ( (Integer) (o[0]) ).intValue());//rs.getInt("service_subscription_id"));
					subdto.setPricePointKeyword((String) (o[12]) );//rs.getString("price_point_keyword"));
					
					
					sub_services.add(subdto);
					
				}
				
			
			}
			
			utx.commit();
			
		} catch (Exception e) {
			try{
				utx.rollback();
			}catch(Exception ex){}
			logger.error(e);
			throw e;
		}finally{
			
		}
		
		return sub_services;
	}


	@SuppressWarnings("unchecked")
	public List<Billable> getBillable(int limit) throws Exception{
		try{
			Query qry =  em.createQuery("from Billable where in_outgoing_queue=0 AND (retry_count<=maxRetriesAllowed) AND resp_status_code is null AND price>0 AND  processed=0 order by priority asc");
			qry.setFirstResult(0);
			qry.setMaxResults(limit);
			return qry.getResultList();
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
			//close();
		}
		
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@SuppressWarnings("unchecked")
	@Override
	public <T> Collection<T> find(Class<T> entityClass,
			Map<String, Object> criteria, int start, int end)  throws Exception {
		Query query = em.createQuery("from " + entityClass.getSimpleName()
				+ buildWhere(criteria));
		int counter1 = 0;
		for (String key : criteria.keySet()){
			counter1++;
			query.setParameter("param"+String.valueOf(counter1), criteria.get(key));			
		}			
		return query.getResultList();
	}

	private String buildWhere(Map<String, Object> criteria)  throws Exception {
		StringBuffer sb = new StringBuffer();
		if (criteria.size() > 0)
			sb.append(" WHERE ");
		int counter2 = 0;
		for (String key : criteria.keySet()) {
			counter2++;
			sb.append(key).append("=:").append("param").append(String.valueOf(counter2))
					.append(criteria.size() == counter2 ? "" : " AND ");
		}
		return sb.toString();
	}
	
	


}
