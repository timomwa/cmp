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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.api.ERROR;
import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.entities.SMSMenuLevels;
import com.pixelandtag.dynamic.dto.NoContentTypeException;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.entities.Notification;
import com.pixelandtag.exceptions.NoSettingException;
import com.pixelandtag.mms.api.TarrifCode;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.serviceprocessors.dto.ServiceSubscription;
import com.pixelandtag.serviceprocessors.dto.SubscriptionDTO;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.Subscription;
import com.pixelandtag.smsmenu.MenuItem;
import com.pixelandtag.smsmenu.Session;
import com.pixelandtag.subscription.SubscriptionSource;
import com.pixelandtag.subscription.dto.MediumType;
import com.pixelandtag.subscription.dto.SMSServiceDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.web.beans.MessageType;

@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class CMPResourceBean implements CMPResourceBeanRemote {
	
	private String server_tz = "-05:00";//TODO externalize

	private String client_tz = "+03:00";//TODO externalize
	
	private static int DEFAULT_LANGUAGE_ID = 1;
	private String MINUS_ONE = "-1";
	private final String RM1 = "RM1";
	
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
	
	
	/**
	 * 
	 * @param conn
	 * @param keyword
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SMSServiceDTO getSMSservice(String keyword) throws Exception {
		
		SMSServiceDTO sm = null;
		try{
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`sms_service` WHERE `cmd`=?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, keyword);
			List<Object[]> obj = qry.getResultList();
			
			if(obj.size()>0){
				
				sm = new SMSServiceDTO();
				
				for(Object[] o : obj){
				
					sm.setId((Integer) o[0] );// rs.getInt("id"));//0
					sm.setMo_processor_FK((Integer) o[1] );//rs.getInt("mo_processorFK"));//1
					sm.setCmd((String) o[2] );//rs.getString("cmd"));//2
					sm.setPush_unique( ((Integer) o[3]).compareTo(1)==0 );//rs.getBoolean("push_unique"));//3
					sm.setService_name(  (String) o[4]  );//rs.getString("service_name"));//4
					sm.setService_description((String) o[5]  );//rs.getString("service_description"));//5
					sm.setPrice(new Double((Double) (o[6])));//6
					sm.setPricePointKeyword((String) o[7] );//rs.getString("price_point_keyword"));//7
					sm.setCmp_keyword( (String) o[8] );//rs.getString("CMP_Keyword"));//8
					sm.setCmp_skeyword((String) o[9] );//rs.getString("CMP_SKeyword"));//9
					sm.setEnabled(((Boolean) o[10]));//rs.getBoolean("enabled"));//10
					sm.setSplit_mt((Boolean) o[11]);//rs.getBoolean("split_mt"));//11
				}
				//service_id = rs.getInt(1);
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
			throw e;
		}finally{}
		
		return sm;
	}
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean unsubscribeAll(String msisdn, SubscriptionStatus status)  throws Exception {
		boolean success = false;
		
		try{
			utx.begin();
			String sql = "UPDATE `"+CelcomImpl.database+"`.`subscription` SET subscription_status=?, subscription_timeStamp=CURRENT_TIMESTAMP WHERE MSISDN=?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, status.getStatus());
			qry.setParameter(2, msisdn);
				
			success = qry.executeUpdate()>0;
			utx.commit();
		}catch(Exception e){
			try{utx.rollback();}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
		}
		
		
		return success;
		
	}
	
	@SuppressWarnings("unchecked")
	public LinkedHashMap<Integer, SMSServiceDTO> getAllSubscribedServices(
			String msisdn) throws Exception{
		String subscribed_services = "";
		LinkedHashMap<Integer,SMSServiceDTO> services = null;
		try{
			String sql = "SELECT group_concat(sms_service_id_fk) as 'sms_services_subscribed' from `"+CelcomImpl.database+"`.`subscription` where  subscription_status='confirmed' AND msisdn=? ";
			Query qry1 = em.createNativeQuery(sql);
			
			qry1.setParameter(1, msisdn);
			Object rs = qry1.getSingleResult();
			
			if(rs!=null)
				subscribed_services =(String) rs;
			
			if(subscribed_services==null || subscribed_services.trim().isEmpty())
				return services;
			sql = String.format("SELECT * FROM `"+CelcomImpl.database+"`.`sms_service` WHERE `id` in(%s)",subscribed_services);
			Query qr2  = em.createNativeQuery(sql);
			List<Object[]> obj = qr2.getResultList();
			
			SMSServiceDTO sm = null;
			
			int i = 0;
			for(Object[] o : obj){
				
				if(i==0){
					services = new LinkedHashMap<Integer,SMSServiceDTO>();
				}
				i++;
				sm = new SMSServiceDTO();
				
				sm.setId((Integer) o[0] );// rs.getInt("id"));//0
				sm.setMo_processor_FK((Integer) o[1] );//rs.getInt("mo_processorFK"));//1
				sm.setCmd((String) o[2] );//rs.getString("cmd"));//2
				sm.setPush_unique( ((Integer) o[3]).compareTo(1)==0);//rs.getBoolean("push_unique"));//3
				sm.setService_name(  (String) o[4]  );//rs.getString("service_name"));//4
				sm.setService_description((String) o[5]  );//rs.getString("service_description"));//5
				sm.setPrice(new Double((Double) (o[6])));//6
				sm.setPricePointKeyword((String) o[7] );//rs.getString("price_point_keyword"));//7
				sm.setCmp_keyword( (String) o[8] );//rs.getString("CMP_Keyword"));//8
				sm.setCmp_skeyword((String) o[9] );//rs.getString("CMP_SKeyword"));//9
				sm.setEnabled(((Boolean) o[10]));//rs.getBoolean("enabled"));//10
				sm.setSplit_mt((Boolean) o[11]);//rs.getBoolean("split_mt"));//11
				
				services.put(i,sm);
				
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
			throw e;
			
		}finally{}
		
		return services;
	}
	
	/**
	 * Updates a subscriber's subscription status
	 * @param conn
	 * @param subscription_id
	 * @param status - com.inmobia.celcom.subscription.dto.SubscriptionStatus
	 * @return
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean updateSubscription(int subscription_id, String msisdn, SubscriptionStatus status) throws Exception {
		
		boolean success = false;
		
		try{
			utx.begin();
			String sql = "UPDATE `"+CelcomImpl.database+"`.`subscription` SET subscription_status=?, subscription_timeStamp=CURRENT_TIMESTAMP WHERE id=? ANd msisdn=?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, status.getStatus());
			qry.setParameter(2, subscription_id);
			qry.setParameter(3, msisdn);
			success = qry.executeUpdate()>0;
			utx.commit();
		}catch(Exception e){
			try{
			utx.rollback();
			}catch(Exception ee){}
			logger.error(e.getMessage(),e);
			
			throw e;
			
		}finally{}
		
		
		return success;
	}
	/**
	 * Updates a subscriber's subscription status
	 * @param conn
	 * @param subscription_id
	 * @param status - com.inmobia.celcom.subscription.dto.SubscriptionStatus
	 * @return
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean updateSubscription(int subscription_id, SubscriptionStatus status) throws Exception {
		
		boolean success = false;
		
		try{
			utx.begin();
			String sql = "UPDATE `"+CelcomImpl.database+"`.`subscription` SET subscription_status=?, subscription_timeStamp=CURRENT_TIMESTAMP WHERE id=?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, status.getStatus());
			qry.setParameter(2, subscription_id);
			success = qry.executeUpdate()>0;
			utx.commit();
		}catch(Exception e){
			try{
			utx.rollback();
			}catch(Exception ee){}
			logger.error(e.getMessage(),e);
			
			throw e;
			
		}finally{}
		
		
		return success;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public com.pixelandtag.subscription.dto.SubscriptionDTO checkAnyPending(
			String msisdn) throws Exception {
		com.pixelandtag.subscription.dto.SubscriptionDTO subscription = null;
		
		try{
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`subscription` WHERE msisdn=? AND subscription_status='waiting_confirmation' ORDER BY subscription_timeStamp asc LIMIT 1";
			Query qry = em.createNativeQuery(sql);
			
			qry.setParameter(1, msisdn);
				
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
				
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
			
		}finally{}
		
		return subscription;
		
	}
	
	
	
	@SuppressWarnings("unchecked")
	public SMSServiceDTO getSMSservice(int service_id) throws Exception{
		
		SMSServiceDTO sm = null;
		try{
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`sms_service` WHERE `id`=?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, service_id);
			List<Object[]> rs = qry.getResultList();
			
			int c = 0;
			if(rs.size()>0){
				for(Object[] o : rs){
					c++;
					if(c==1)
						sm = new SMSServiceDTO();
					
					sm.setId((Integer) o[0] );// rs.getInt("id"));//0
					sm.setMo_processor_FK((Integer) o[1] );//rs.getInt("mo_processorFK"));//1
					sm.setCmd((String) o[2] );//rs.getString("cmd"));//2
					sm.setPush_unique(((Integer) o[3]).compareTo(1)==0);//rs.getBoolean("push_unique"));//3
					sm.setService_name(  (String) o[4]  );//rs.getString("service_name"));//4
					sm.setService_description((String) o[5]  );//rs.getString("service_description"));//5
					sm.setPrice(new Double((Double) (o[6])));//6
					sm.setPricePointKeyword((String) o[7] );//rs.getString("price_point_keyword"));//7
					sm.setCmp_keyword( (String) o[8] );//rs.getString("CMP_Keyword"));//8
					sm.setCmp_skeyword((String) o[9] );//rs.getString("CMP_SKeyword"));//9
					sm.setEnabled(((Boolean) o[10]));//rs.getBoolean("enabled"));//10
					sm.setSplit_mt((Boolean) o[11]);//rs.getBoolean("split_mt"));//11
				}
				
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
			
		}finally{}
		
		return sm;
	}


	
	@SuppressWarnings("unchecked")
	public ServiceProcessorDTO getServiceProcessor(int processor_id_fk) throws Exception{

		ServiceProcessorDTO service = null;
		
		try {
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`mo_processors` WHERE `id`=?";
			Query qry = em.createNativeQuery(sql);
			
			qry.setParameter(1, processor_id_fk);
			
			List<Object[]> rs = qry.getResultList();
			
			for(Object[] o : rs){
				
				service = new ServiceProcessorDTO();
				
				service.setId((Integer) o[0] );//rs.getInt("id"));//0
				service.setServiceName((String) o[1] );//rs.getString("ServiceName"));//1
				service.setShortcode((String) o[2] );//rs.getString("shortcode"));//2
				service.setThreads((Integer) o[3] );//rs.getInt("threads"));//3
				service.setProcessorClass((String) o[4] );//rs.getString("ProcessorClass"));//4
				service.setActive(((Boolean) o[5]));//rs.getBoolean("enabled"));//5
				service.setClass_status((String) o[6] );//rs.getString("class_status"));//6
				service.setServKey(service.getProcessorClassName()+"_"+service.getCMP_AKeyword()+"_"+service.getCMP_SKeyword()+"_"+service.getShortcode());
				
				
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
			throw e;
			
		}finally{}
		
		return service;
	}
	
	
	/*public MOSms getContentFromServiceId(int service_id, String msisdn, boolean isSubscription)  throws Exception{
		String s  = "::::::::::::::::::::::::::::::::::::::::::::::::::::";
		logger.info(s+" service_id["+service_id+"] msisdn["+msisdn+"]");
		SMSServiceDTO sm = getSMSservice(service_id);
		logger.info(s+sm);
		MOSms mo = null;
		
		if(sm!=null){
			
			ServiceProcessorDTO procDTO = getServiceProcessor(sm.getMo_processor_FK());
			
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
				mo.setSubscriptionPush(isSubscription);
				
				mo = processor.process(mo);
				
				
			}catch(Exception e) {
				logger.error(e.getMessage(),e);
			}
		}else{
			logger.info(s+" sm is null!");
		}
		
		
		return mo;
	}*/
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean updateProfile(String msisdn, int language_id) throws Exception{
		boolean success = false;
		try {
			utx.begin();
			String sql = "INSERT INTO `"+CelcomImpl.database+"`.`subscriber_profile`(`msisdn`,`language_id`) VALUES(?,?) ON DUPLICATE KEY UPDATE `language_id`=?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, msisdn);
			qry.setParameter(2, language_id);
			qry.setParameter(3, language_id);
			success  = qry.executeUpdate()>0;
			utx.commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			try{
			utx.rollback();
			}catch(Exception e1){}
			throw e;
			
		}finally{
			}
		return success;
	}
	
	public String getMessage(String key, int language_id) throws Exception{
		String message = "Error 130 :  Translation text not found. language_id = "+language_id+" key = "+key;
		
		try {
			String sql = "SELECT message FROM "+CelcomImpl.database+".message WHERE language_id = ? AND `key` = ? ORDER BY RAND() LIMIT 1";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, language_id);
			qry.setParameter(2, key);

			Object obj = qry.getSingleResult();
			if (obj!=null) {
				message = (String) obj;
			}


			logger.debug("looking for :[" + key + "], found [" + message + "]");
			
			return message;

		}catch(javax.persistence.NoResultException ex){
			logger.warn(ex.getMessage() + " no profile for subscriber "+message);
			return null;
		}catch (Exception e) {

			logger.error(e.getMessage(), e);

			throw e;

		}finally{
		}
	}
	
	public int getSubscriberLanguage(String msisdn) throws Exception {
		
		int language_id = DEFAULT_LANGUAGE_ID;
		
		try {
			String sql = "SELECT language_id FROM `"+CelcomImpl.database+"`.`subscriber_profile` WHERE msisdn=?";
			Query qry = em.createNativeQuery(sql);
			
			qry.setParameter(1, msisdn);
			
			Object o = qry.getSingleResult();
			
			if(o!=null)
				language_id = ( (Integer) o ).intValue();
			else
				language_id =  DEFAULT_LANGUAGE_ID ;//default language
			
		}catch(javax.persistence.NoResultException ex){
			logger.warn(ex.getMessage() + " no profile for subscriber"+msisdn);
		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw e;
			
		}finally{
		}
		
		return language_id;
	}
	
	public void printMenu(MenuItem menuItem, int level, int position)  throws Exception{
		String tab = "";
		for(int i=0;i<level;i++){
			tab +="\t"; //+ ( ( (i+1)==level   ) ? "+" : "") ;
		}
		
		String op = tab + position+ ". " + menuItem.getName();
		
		System.out.println(op);
		LinkedHashMap<Integer,MenuItem> mis = getSubMenus(menuItem.getId());
		if(mis!=null){
			level++;
			position = 1;
			for (Entry<Integer, MenuItem> entry : mis.entrySet()) {
				printMenu(entry.getValue(),level, position);
			 	position++;
			}
			
		}
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean updateSession(int language_id, String msisdn,
			int smsmenu_levels_id_fk) throws Exception{
		boolean success = false;
		
		try{
			utx.begin();
			String UPDATE_SESSION = "INSERT INTO `"+CelcomImpl.database+"`.`smsmenu_session`(`msisdn`,`smsmenu_levels_id_fk`,`timeStamp`,`language_id`) VALUES(?,?,now(),?) ON DUPLICATE KEY UPDATE `smsmenu_levels_id_fk`=?,timeStamp=NOW(),language_id=?";
			Query qry = em.createNativeQuery(UPDATE_SESSION);
			qry.setParameter(1, msisdn);
			qry.setParameter(2, smsmenu_levels_id_fk);
			qry.setParameter(3, language_id);
			qry.setParameter(4, smsmenu_levels_id_fk);
			qry.setParameter(5, language_id);
			
			success = (qry.executeUpdate()>0);
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
	public Session getSession(String msisdn) throws Exception{
		Session sess = null;
		
		try{
			
			String GET_SESSION = "SELECT ses.*, sl.id as 'menu_level_id', sl.name, sl.language_id as 'language_id_',sl.parent_level_id, sl.menu_id, sl.serviceid, sl.visible FROM `"+CelcomImpl.database+"`.`smsmenu_session` ses LEFT JOIN `"+CelcomImpl.database+"`.`smsmenu_levels` sl ON sl.id = ses.smsmenu_levels_id_fk WHERE ses.`msisdn`=? AND ((TIMESTAMPDIFF(HOUR,ses.timeStamp,CONVERT_TZ(CURRENT_TIMESTAMP,'"+getServerTz()+"','"+getClientTz()+"')))<=24 )";
			Query qry = em.createNativeQuery(GET_SESSION);
			qry.setParameter(1, msisdn);
			List<Object[]> obj = qry.getResultList();
			
			MenuItem mi = null;
			
			int menu_level_id = -1;
			
			for(Object[] o : obj){
				sess = new Session();
				mi = new MenuItem();
				sess.setId((Integer) o[0]);//rs.getInt("id"));//0
				sess.setMsisdn((String) o[1]);//rs.getString("msisdn"));//1
				sess.setSmsmenu_level_id_fk((Integer) o[2]);//rs.getInt("smsmenu_levels_id_fk"));//2
				sess.setLanguage_id((Integer) o[3]);//rs.getInt("language_id"));//3
				sess.setTimeStamp( sdf.format(( (java.sql.Timestamp) o[4]) ));//4
				
				if(o[6]!=null)
					menu_level_id = (Integer) o[6];
				
				if(menu_level_id>0){//6
					mi.setId(menu_level_id);
					mi.setName((String) o[7]);//rs.getString("name"));//7
					mi.setLanguage_id((Integer) o[8]);//rs.getInt("language_id"));//8
					mi.setParent_level_id((Integer) o[9]);//rs.getInt("parent_level_id"));//9
					mi.setMenu_id((Integer) o[5]);//rs.getInt("menu_id"));//5
					mi.setService_id((Integer) o[11]);//rs.getInt("serviceid"));//11
					mi.setVisible(((Boolean) o[12]) );//rs.getBoolean("visible"));//12
					
					mi.setSub_menus(getSubMenus(menu_level_id));
					
					sess.setMenu_item(mi);
				}
				
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
			
		}finally{}
		
		return sess;
	}
	
	
	@SuppressWarnings("unchecked")
	public MenuItem getMenuById(int menu_id) throws Exception{
		
		MenuItem mi = null;
		
		try{
		
			String GET_MENU_BY_ID = "SELECT * FROM `"+CelcomImpl.database+"`.`smsmenu_levels` WHERE id=? AND visible=1";
			Query qry = em.createNativeQuery(GET_MENU_BY_ID);
			qry.setParameter(1, menu_id);
			
			List<Object[]> obj = qry.getResultList();
			
			LinkedHashMap<Integer, MenuItem> topMenus = getSubMenus(menu_id);
			
			//int x = 0;
			
			for(Object[] o : obj){
				//x++;
				
				mi = new MenuItem();
				mi.setId((Integer) o[0]);
				mi.setName((String) o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Boolean) o[6]) );
				
				
			}
			
			if(mi!=null)
				mi.setSub_menus(topMenus);
		
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		
		}
		
		return mi;
	}
	
	
	@SuppressWarnings("unchecked")
	public LinkedHashMap<Integer, MenuItem> getSubMenus(int parent_level_id_fk) throws Exception{
		
		LinkedHashMap<Integer,MenuItem> items = null;
		
		try{
			
			String GET_MENU_ITEMS = "SELECT * FROM `"+CelcomImpl.database+"`.`smsmenu_levels` WHERE parent_level_id = ? AND visible=1";
			Query qry = em.createNativeQuery(GET_MENU_ITEMS);
			qry.setParameter(1, parent_level_id_fk);
			
			List<Object[]> obj = qry.getResultList();
			
			int post = 0;
			
			MenuItem mi = null;
			
			for(Object[] o :obj ){
				post++;
				
				if(post==1)
					items = new LinkedHashMap<Integer,MenuItem>();
				
				mi = new MenuItem();
				
				mi.setId((Integer) o[0]);
				mi.setName((String) o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Boolean) o[6]) );
				items.put(post,mi);
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
		}
		
		return items;
	}
	@SuppressWarnings("unchecked")
	public MenuItem getTopMenu(int menu_id, int language_id)  throws Exception{

		
		MenuItem mi = null;
		
		try{
		
			String GET_TOP_MENU_BY_MENU_ID_AND_LANGUAGE_ID = "SELECT * FROM `"+CelcomImpl.database+"`.`smsmenu_levels` WHERE menu_id=? AND language_id=? AND  parent_level_id=-1 AND visible=1";
			Query qry = em.createNativeQuery(GET_TOP_MENU_BY_MENU_ID_AND_LANGUAGE_ID);
			
			qry.setParameter(1, menu_id);
			qry.setParameter(2, language_id);
			
			List<Object[]> obj = qry.getResultList();
			
			LinkedHashMap<Integer, MenuItem> topMenus = null;
			
			int x = 0;
			
			for(Object[] o : obj){
				x++;
				
				if(x==1)
					topMenus = new LinkedHashMap<Integer, MenuItem>();
				
				mi = new MenuItem();
				
				mi.setId((Integer) o[0]);
				mi.setName((String) o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Boolean) o[6]) );
				
				topMenus.put(x, mi);
				
			}
			
			if(mi!=null)
				mi.setSub_menus(topMenus);
		
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{}
		
		return mi;
	}
	
	@SuppressWarnings("unchecked")
	public MenuItem getMenuByParentLevelId(int language_id, int parent_level_id) throws Exception{
		
		MenuItem menuItem = null;
		try{
			String GET_TOP_MENU = "SELECT * FROM `"+CelcomImpl.database+"`.`smsmenu_levels` WHERE parent_level_id=? AND language_id=? and visible=1";
			Query qry = em.createNativeQuery(GET_TOP_MENU);
			qry.setParameter(1, parent_level_id);
			qry.setParameter(2, language_id);
			
			List<Object[]> rs = qry.getResultList();
			
			LinkedHashMap<Integer, MenuItem> topMenus = null;
			
			int x = 0;
			MenuItem mi = null;
			for(Object[] o : rs){
				x++;
				if(x==1){
					
					topMenus = new LinkedHashMap<Integer, MenuItem>();
				}
				mi = new MenuItem();
				mi.setId((Integer) o[0]);
				mi.setName((String) o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Boolean) o[6]) );
				topMenus.put(x, mi);
				
				
			}
			
			
				menuItem = getMenuById(parent_level_id);
				if(menuItem==null){
					menuItem = new MenuItem();
					menuItem.setId(parent_level_id);
					menuItem.setParent_level_id(parent_level_id);
					menuItem.setLanguage_id(language_id);
				}
				menuItem.setSub_menus(topMenus);
			
		
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		}
		
		return menuItem;
	}
	
	/*@SuppressWarnings("unchecked")
	public MenuItem getMenuById(int parent_level_id) {
		MenuItem mi = null;
		
		
		try{
		
			String GET_MENU_BY_ID = "SELECT * FROM `"+CelcomImpl.database+"`.`smsmenu_levels` WHERE id=? AND visible=1";
			Query qry = em.createNativeQuery(GET_MENU_BY_ID);
			qry.setParameter(1, parent_level_id);
			
			List<Object[]> obj = qry.getResultList();
			
			LinkedHashMap<Integer, MenuItem> topMenus = getSubMenus(parent_level_id);
			
			//int x = 0;
			
			for(Object[] o : obj){
				//x++;
				
				mi = new MenuItem();
				mi.setId((Integer) o[0]);
				mi.setName((String) o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Integer) o[6]).compareTo(1)==0 );
				
				
			}
			
			if(mi!=null)
				mi.setSub_menus(topMenus);
		
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
		
			
		
		}
		
		return mi;
	}
*/
	

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean deleteOldLogs() throws Exception{
		boolean success = false;
		
		try {
			utx.begin();
			String sql = "delete from `"+CelcomImpl.database+"`.`subscriptionlog` where date(CONVERT_TZ(timeStamp,'"+getServerTz()+"','"+getClientTz()+"'))<date(CONVERT_TZ(now(),'"+getServerTz()+"','"+getClientTz()+"'))";
			Query qry = em.createNativeQuery(sql);
			success = qry.executeUpdate()>0;
			utx.commit();
		} catch (Exception e) {
			try{
			utx.rollback();
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		}
		
		return success;
	}
	
	public int getHourNow() throws Exception{
		int hour_now = -1;
		
		try {
			String sql = "SELECT hour(CONVERT_TZ(now(),'"+getServerTz()+"','"+getClientTz()+"')) as 'hour'";
			Query qry = em.createNativeQuery(sql);
			Object o  = qry.getSingleResult();
			hour_now = ( (BigInteger) o ).intValue();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		}
		
		return hour_now;
	}
	
	public int countSubscribers(int service_id) throws Exception{
		
		int count = -1;
		
		try {
			String sql = "SELECT count(*) FROM `"+CelcomImpl.database+"`.`subscription`  WHERE subscription_status='confirmed' AND sms_service_id_fk = ?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, service_id);
			Object o  = qry.getSingleResult();
			count = ( (BigInteger) o ).intValue();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (Exception e) {
			try{
			utx.rollback();
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		}
		
		return count;
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean logResponse(String msisdn, String responseText) throws Exception{
		boolean success = false;
		try {
			utx.begin();
			String sql = "INSERT DELAYED INTO `"+CelcomImpl.database+"`.`raw_out_log`(msisdn,response) VALUES(?,?)";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, msisdn);
			qry.setParameter(2, responseText);
			success = qry.executeUpdate()>0;
			utx.commit();
		} catch (Exception e) {
			try{
			utx.rollback();
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		}
		
		return success;
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean postponeMT(long http_to_send_id) throws Exception{
		
		boolean success = false;
		
		try {
			utx.begin();
			String sql = "UPDATE `"+CelcomImpl.database+"`.`httptosend` SET `sent` = 0, in_outgoing_queue=0 WHERE id = ?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, String.valueOf(http_to_send_id));
			success = qry.executeUpdate()>0;
			utx.commit();
		} catch (Exception e) {
			try{
			utx.rollback();
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		}
		return success;
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean logMT(MTsms mt) throws Exception{
		
		boolean success = false;
		
		try {
				
			utx.begin();
			 
			String sql = "INSERT INTO `"+CelcomImpl.database+"`.`messagelog`(CMP_Txid,MT_Sent,SMS_SourceAddr,SUB_Mobtel,SMS_DataCodingId,CMPResponse,APIType,CMP_Keyword,CMP_SKeyword,MT_STATUS,number_of_sms,msg_was_split,MT_SendTime,mo_ack,serviceid,price,newCMP_Txid,mo_processor_id_fk,price_point_keyword,subscription) " +
							"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+getServerTz()+"','"+getClientTz()+"'),1,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE MT_Sent = ?, mo_ack=1, MT_SendTime=CONVERT_TZ(CURRENT_TIMESTAMP,'"+getServerTz()+"','"+getClientTz()+"'), MT_STATUS = ?, number_of_sms = ?, msg_was_split=?, serviceid=? , price=?, SMS_DataCodingId=?, CMPResponse=?, APIType=?, newCMP_Txid=?, CMP_SKeyword=?, mo_processor_id_fk=?, price_point_keyword=?,subscription=?";

			Query qry = em.createNativeQuery(sql);
			
			String txid = mt.getIdStr();
			if(mt.getCMP_Txid()>0){
				
				if(!(mt.getCMP_Txid()==-1)){
					txid = String.valueOf(mt.getCMP_Txid());
					qry.setParameter(1, txid);//Since we're starting the transaction, what is in the httptosend as id now becomes the value of CMP_Txid.
				}else{
					txid = mt.getIdStr();
					qry.setParameter(1, txid);//Since we're starting the transaction, what is in the httptosend as id now becomes the value of CMP_Txid.
				}
				
			}else{
				
				qry.setParameter(1, mt.getIdStr());
					txid = mt.getIdStr();
			}
			
			boolean isRetry = !mt.getNewCMP_Txid().equals(MINUS_ONE);
			
			qry.setParameter(2, mt.getSms());
			qry.setParameter(3, mt.getFromAddr());//.getSMS_SourceAddr());//Shortcode sent with.
			qry.setParameter(4, mt.getMsisdn());//.getSUB_Mobtel());
			qry.setParameter(5, mt.getSMS_DataCodingId());//SMS_DataCodingId
			qry.setParameter(6, mt.getCMPResponse());//CMPResponse
			qry.setParameter(7, mt.getAPIType());//APIType,
			qry.setParameter(8, mt.getCMP_AKeyword());//CMP_Keyword
			qry.setParameter(9, mt.getCMP_SKeyword());//CMP_SKeyword
			qry.setParameter(10, mt.getMT_STATUS());//MT_STATUS
			qry.setParameter(11, mt.getNumber_of_sms());//number_of_sms
			qry.setParameter(12, (mt.isSplit_msg() ? 1 : 0));//number_of_sms
			qry.setParameter(13, mt.getServiceid());//serviceid
			if(mt.getCMP_SKeyword()!=null && mt.getCMP_SKeyword().equals(TarrifCode.RM1.getCode()))
				qry.setParameter(14, 1.0d);//price
			else
				qry.setParameter(14, mt.getPrice().doubleValue());//price
			qry.setParameter(15, mt.getNewCMP_Txid());//new CMPTxid
			qry.setParameter(16, mt.getProcessor_id());//processor id
			qry.setParameter(17, mt.getPricePointKeyword());//processor id
			qry.setParameter(18, (mt.isSubscription()  ? 1 : 0 ));// is subscription ?
			qry.setParameter(19, mt.getSms());//SMS
			
			if(isRetry)
				qry.setParameter(20, ERROR.PSAInsufficientBalance.toString());//MT_STATUS
			else
				qry.setParameter(20, mt.getMT_STATUS());//MT_STATUS
			
			qry.setParameter(21, mt.getNumber_of_sms());//number_of_sms
			qry.setParameter(22, (mt.isSplit_msg() ? 1 : 0));//number_of_sms
			qry.setParameter(23, mt.getServiceid());//serviceid
			
			if(mt.getCMP_SKeyword().equals(TarrifCode.RM1.getCode()))
				qry.setParameter(24, 1.0d);//price
			else
				qry.setParameter(24, mt.getPrice().doubleValue());//price
			
			qry.setParameter(25, mt.getSMS_DataCodingId());//SMS_DataCodingId
			qry.setParameter(26, mt.getCMPResponse());//CMPResponse
			qry.setParameter(27, mt.getAPIType());//APIType,
			qry.setParameter(28, mt.getNewCMP_Txid());//new CMPTxid
			
			if(mt.getSms().startsWith(RM1))
				qry.setParameter(29, TarrifCode.RM1.getCode());//CMP_SKeyword
			else
				qry.setParameter(29, mt.getCMP_SKeyword());//CMP_SKeyword
			
			qry.setParameter(30, mt.getProcessor_id());//CMP_SKeyword
			
			qry.setParameter(31, mt.getPricePointKeyword());//CMP_SKeyword
			qry.setParameter(32, (mt.isSubscription()  ? 1 : 0 ));// is subscription ?
			success = qry.executeUpdate()>0;
			utx.commit();
		} catch (Exception e) {
			try{
				utx.rollback();
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
			
		}
		
		return success;
		
		
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
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
			throw e;
		
		}finally{
			
			
		}
		
		return value;
	}
	
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean deleteMT(long id) throws Exception {
		
		boolean success = false;
		
		try {
			utx.begin();
			String sql = "DELETE FROM `"+CelcomImpl.database+"`.`httptosend` WHERE id = ?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, String.valueOf(id));
			success = qry.executeUpdate()>0;
			utx.commit();
		} catch (Exception e) {
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
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
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
	public boolean subscribe(String msisdn, int service_id, int smsmenu_levels_id_fk, SubscriptionStatus status,SubscriptionSource source) throws Exception{
		
		boolean success = false;
		
		try{
			
			if(service_id>-1){
				utx.begin();
				String sql = "INSERT INTO `"+CelcomImpl.database+"`.`subscription`(sms_service_id_fk,msisdn,smsmenu_levels_id_fk, request_medium,subscription_status) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE subscription_status = ?";
				Query qry = em.createNativeQuery(sql);
				
				qry.setParameter(1, service_id);
				qry.setParameter(2, msisdn);
				qry.setParameter(3, smsmenu_levels_id_fk);
				qry.setParameter(4, source.toString());
				qry.setParameter(5, status.toString());
				qry.setParameter(6, status.toString());
				
				if(qry.executeUpdate()>0)
					success = true;
				utx.commit();
			}else{
				success = false;
			}
				
			
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			try{
				utx.rollback();
				}catch(Exception e1){}
			
			throw e;
		}finally{}
		
		
		return success;
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
				
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
			
			
		}
		
		
		return subscription;
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getAdditionalServiceInfo(int serviceid) throws Exception{
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
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
			
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
	
	
	
	
	public boolean shouldPushNow(int service_id) throws Exception{
		boolean resp = false;
		try {
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`ServiceSubscription` WHERE `serviceid`=? AND hour(`schedule`)=hour(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')) AND `lastUpdated`<=convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"') AND ExpiryDate>convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')";//first get services to be pushed now.
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, service_id);
			if(qry.getResultList()!=null)
				resp = (qry.getResultList().size()>0 );//if a record comes, do push
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}finally{
		}
		return resp;
	}
	
	@SuppressWarnings("unused")
	public int countPushesToday(int service_id) throws Exception{
		
		int count = -1;
		try {
			
			String sql = "SELECT "
					+ "count(*) as 'cnt'"
					+ "FROM `"+CelcomImpl.database+"`.`subscription` WHERE "
							+ "subscription_status=:subscription_status "
							+ "AND sms_service_id_fk =:sms_service_id_fk "
							+ "AND id in "
							+ "(SELECT subscription_id FROM `"+CelcomImpl.database+"`.`subscriptionlog` "
									+ "WHERE "
									+ "date(convert_tz(`timeStamp`,'"+getServerTz()+"','"+getClientTz()+"'))=date(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')))";
			
			String sqld = "SELECT "
					+ "count(*) as 'cnt'"
					+ "FROM `"+CelcomImpl.database+"`.`subscription` WHERE "
							+ "subscription_status='confirmed'"
							+ "AND sms_service_id_fk ='"+service_id+"'"
							+ "AND id in "
							+ "(SELECT subscription_id FROM `"+CelcomImpl.database+"`.`subscriptionlog` "
									+ "WHERE "
									+ "date(convert_tz(`timeStamp`,'"+getServerTz()+"','"+getClientTz()+"'))=date(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')))";
			
			
			Query qry = em.createNativeQuery(sql);
			qry.setParameter("subscription_status", "confirmed");
			qry.setParameter("sms_service_id_fk", service_id);
			Object res = qry.getSingleResult();
			count =  ( (BigInteger) res ).intValue();
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (Exception e) {
			
			logger.error(e.getMessage());
			throw e;
		}finally{
		}
		return count;
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@SuppressWarnings({ "unchecked", "unused" })
	public List<Subscription> listServiceMSISDN(String sub_status, int serviceid) throws Exception {
		List<Subscription> msisdnL = new ArrayList<Subscription>();
		try {
			
			utx.begin();
			String sql = "SELECT "
					+ "id,"//0
					+ "subscription_status,"//1
					+ "sms_service_id_fk,"//2
					+ "msisdn,"//3
					+ "subscription_timeStamp,"//4
					+ "smsmenu_levels_id_fk,"//5
					+ "request_medium "//6
					+ "FROM `"+CelcomImpl.database+"`.`subscription` WHERE "
							+ "subscription_status=:subscription_status "
							+ "AND sms_service_id_fk =:sms_service_id_fk "
							+ "AND id not in "
							+ "(SELECT subscription_id FROM `"+CelcomImpl.database+"`.`subscriptionlog` "
									+ "WHERE "
									+ "date(convert_tz(`timeStamp`,'"+getServerTz()+"','"+getClientTz()+"'))=date(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')))";
			
			String sqld = "SELECT "
					+ "id,"//0
					+ "subscription_status,"//1
					+ "sms_service_id_fk,"//2
					+ "msisdn,"//3
					+ "subscription_timeStamp,"//4
					+ "smsmenu_levels_id_fk,"//5
					+ "request_medium "//6
					+ "FROM `"+CelcomImpl.database+"`.`subscription` WHERE "
							+ "subscription_status='"+sub_status+"'"
							+ "AND sms_service_id_fk ='"+serviceid+"'"
							+ "AND id not in "
							+ "(SELECT subscription_id FROM `"+CelcomImpl.database+"`.`subscriptionlog` "
									+ "WHERE "
									+ "date(convert_tz(`timeStamp`,'"+getServerTz()+"','"+getClientTz()+"'))=date(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')))";
			
			
			
			//System.out.println("\n\n"+sqld+"\n\n ");
			
			Query qry = em.createNativeQuery(sql);
			qry.setParameter("subscription_status", sub_status);
			qry.setParameter("sms_service_id_fk", serviceid);
			List<Object[]> res = qry.getResultList();
			for(Object[] o : res){
				Subscription sub = new Subscription();
				sub.setId(((Integer)o[0] ).longValue());
				sub.setSubscription_status(SubscriptionStatus.get((String)o[1]));
				sub.setSms_service_id_fk(((Integer)o[2]).longValue());
				sub.setMsisdn((String)o[3]);
				sub.setSubscription_timeStamp(( (java.util.Date) o[4]) );
				sub.setSmsmenu_levels_id_fk((Integer)o[5]);
				sub.setRequest_medium(MediumType.get((String)o[6]));
				msisdnL.add(sub);
			}
			
			
			
			utx.commit();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
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
	
	@SuppressWarnings("unchecked")
	public List<ServiceSubscription> getServiceSubscription()  throws Exception {
		
		List<ServiceSubscription> list = new ArrayList<ServiceSubscription>();

		try {
			
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`ServiceSubscription` WHERE hour(`schedule`)=hour(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')) AND `lastUpdated`<=convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"') AND ExpiryDate>convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')";//first get services to be pushed now.
			//System.out.println(sql);
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
			
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (Exception e) {
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
	public String getUnique(String database_name,String table,String field,String idfield,String msisdn,int serviceid,int size,int processor_id) throws Exception {
		
		
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
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
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
	public <T> T find(Class<T> entityClass, String param_name, Object value) throws Exception  {
		T t = null;
		try{
			Query query = em.createQuery("from " + entityClass.getSimpleName() + " WHERE "+param_name+" =:"+param_name+" ").setParameter(param_name, value);
			if(query.getResultList().size()>0)
				t = (T) query.getResultList().get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception  e){
			throw e;
		}
		return t;
	}
	
	
	public String getMessage(MessageType messageType,
			int language) throws Exception {
		return getMessage(messageType.toString(), language);
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
	
	/**
	 * Logs in httptosend
	 */
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
			qry.setParameter(16, (mo.isSubscription() ? 1 : 0));
			qry.setParameter(17, ( mo.isSubscription() ? 1 : 0 ));
			
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
				+"ON pro.id = sm.mo_processorFK WHERE pro.enabled=1 AND hour(`ss`.`schedule`)=hour(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')) AND convert_tz(`ss`.`lastUpdated`,'"+getServerTz()+"','"+getClientTz()+"')<convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"') AND `ss`.`ExpiryDate`>convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')";
	    logger.debug("\n\t"+sub+"\n");
	  //  System.out.println("\n\t"+sub+"\n");
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
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
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
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
			return null;
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
			//close();
		}
		
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean saveStaticSMS(String db_name, String table,
			String static_category_value, String sms) throws Exception{
		boolean success = false;
		try{
			utx.begin();
			Query qry2 = em.createNativeQuery("INSERT INTO `"+db_name+"`.`"+table+"`(Category,Text,timeStamp) VALUES(:category, :text, CONVERT_TZ(CURRENT_TIMESTAMP,'"+getServerTz()+"','"+getClientTz()+"'))");
			qry2.setParameter("category", static_category_value);
			qry2.setParameter("text", sms);
			
			success =  qry2.executeUpdate()>0;
			utx.commit();
		}catch(Exception exp){
			try{
				utx.rollback();
			}catch(Exception ex){}
			logger.error(exp.getMessage(),exp);
			throw exp;
		}
		return success;
	}
	@SuppressWarnings("unchecked")
	public List<SMSMenuLevels> listChildren(Long id)  throws Exception {
		List<SMSMenuLevels> list = null;
		try{
			Query qry = em.createQuery("from SMSMenuLevels sm where parent_level_id=:parent_level_id");
			qry.setParameter("parent_level_id", id);
			list =  qry.getResultList();
		}catch(javax.persistence.NoResultException  nre){
			logger.error(nre.getMessage(),nre);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean updateSMSStatLog(BigInteger transaction_id, ERROR errorcode) throws Exception {
		
		boolean success = false;
		
		
		try {
			
			long cmpTxid = transaction_id.longValue();
			
			String sql;
			Query qry;
			Object rs;
			try{
				sql = "SELECT CMP_Txid FROM `"+CelcomImpl.database+"`.`messagelog` WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)";
				qry = em.createNativeQuery(sql);
				qry.setParameter(1, cmpTxid);
				qry.setParameter(2, cmpTxid);
				rs = qry.getSingleResult();
					if(rs!=null){
						cmpTxid = (Long) rs;
					}
			}catch(javax.persistence.NoResultException ex){
				logger.warn(ex.getMessage() + " COULD NOT FIND messagelog record with (CMP_Txid = "+cmpTxid+") OR (newCMP_Txid = "+cmpTxid+")");
			}
				
			
			rs = null;
			double priceTbc = 0.0d;
			boolean wasInLog = false;
			List<Object[]> rs2 =null;
			try{
			
				sql = "SELECT price,CMP_SKeyword FROM `"+CelcomImpl.database+"`.`SMSStatLog` WHERE transactionID = ?";
			  
				Query qry2 = em.createNativeQuery(sql);
				qry2.setParameter(1, cmpTxid);
				
				rs2 = qry2.getResultList();
			
			}catch(javax.persistence.NoResultException ex){
				logger.warn(ex.getMessage() + " COULD NOT FIND SMSStatLog record with transactionID = "+cmpTxid+")");
				
			}
			
			if(rs2!=null){
				
				for(Object[] o : rs2){
					priceTbc = new Double((Double) (o[0]));
					wasInLog  = true;
				}
				
				logger.debug("Statlog rec found transactionID = "+cmpTxid);
			
			}else{
				
				logger.warn("Statlog rec with transactionID = "+cmpTxid+ " NOT found! Try search celcom.messagelog");
				
				
				try {
					
					sql = "SELECT "
							+ "SUB_Mobtel,"//0
							+ "CMP_Keyword,"//1
							+ "CMP_SKeyword,"//2
							+ "serviceid,"//3
							+ "price "//4
							+ "from `"+CelcomImpl.database+"`.`messagelog` WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)";
					Query qry3 = em.createNativeQuery(sql);
					
					qry3.setParameter(1, cmpTxid);
					qry3.setParameter(2, cmpTxid);
					
					List<Object[]> rezults = qry3.getResultList();
					
					String msisdn = "UNKNOWN",CMP_Keyword= "UNKNOWN",CMP_SKeyword= "UNKNOWN";
					
					int serviceid = -1;
					
					if(rezults.size()>0){
						for(Object[] o :rezults){
							msisdn = (String) o[0];// rs.getString("SUB_Mobtel");
							CMP_Keyword = (String) o[1];//rs.getString("CMP_Keyword");
							CMP_SKeyword = (String) o[2];//rs.getString("CMP_SKeyword");
							serviceid =(Integer) o[3];// rs.getInt("serviceid");
							priceTbc = new Double((Double) o[4]);//TarrifCode.get(CMP_SKeyword.trim()).getPrice();
						}
						
							
					}
					
					
					utx.begin();
					String sql4 = "INSERT INTO `"+CelcomImpl.database+"`.`SMSStatLog`(SMSServiceID,msisdn,transactionID, CMP_Keyword, CMP_SKeyword,price,statusCode,charged) " +
							"VALUES(?,?,?,?,?,?,?,?)";
					Query qry4 = em.createNativeQuery(sql4);
					
					qry4.setParameter(1, serviceid);
					qry4.setParameter(2, msisdn);
					qry4.setParameter(3, cmpTxid);
					qry4.setParameter(4, CMP_Keyword);
					qry4.setParameter(5, CMP_SKeyword);
					qry4.setParameter(6, priceTbc);
					qry4.setParameter(7, errorcode.toString());
					qry4.setParameter(8, (((priceTbc>0.0d) && errorcode.equals(ERROR.Success)) ? 1: 0 )   );
					
					success = qry4.executeUpdate()>0;
					
					logger.debug("______CMP_SKeyword="+CMP_SKeyword+"________cmpTxid = "+cmpTxid+"________priceTbc = "+priceTbc+"_________________did not find in smsStatLog but no problem, we found in messagelog__________________________________________SUCCESSFULLY_INSERTED_INTO_SMSStatLog___________________________________________________________________________________");
					
					utx.commit();
				}catch(javax.persistence.NoResultException ex){
					try{
						utx.rollback();
					}catch(Exception ex1){}
					logger.error(ex.getMessage(),ex);
				
				}catch (Exception e) {
					try{
						utx.rollback();
					}catch(Exception ex1){}
					logger.error(e.getMessage(),e);
					throw e;
					
				}finally{}
				
			}
			
			
			
			//Only if the message exists in SMSStatLog
			if(wasInLog){
				
				utx.begin();
				String sql5 = "UPDATE `"+CelcomImpl.database+"`.`SMSStatLog` SET statusCode=?,  charged=? WHERE  transactionID = ?";
				
				Query qry5 = em.createNativeQuery(sql5);
				
				qry5.setParameter(1, errorcode.toString());
				qry5.setParameter(2, (((priceTbc>0.0) && errorcode.equals(ERROR.Success)) ? 1: 0 )   );
				qry5.setParameter(3, cmpTxid);
				
				success = qry5.executeUpdate()>0;
				
				logger.debug("MT (transactionID = "+cmpTxid+ ") " + (success ? "successfully logged into SMSStatLog" : "failed to log into SMSStatLog"));
				utx.commit();
			}
			
			
		}catch(javax.persistence.NoResultException ex){
			try{
				utx.rollback();
			}catch(Exception e1){}
			logger.error(ex.getMessage(),ex);
		
		}  catch (Exception e) {
			
			try{
				utx.rollback();
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			
			throw e;
			
		}finally{
		
		}
		
		return success;
		
		
	}
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@SuppressWarnings("unchecked")
	@Override
	public <T> Collection<T> find(Class<T> entityClass,
			Map<String, Object> criteria, int start, int end)  throws Exception {
		try{
			Query query = em.createQuery("from " + entityClass.getSimpleName()
		
				+ buildWhere(criteria));
			int counter1 = 0;
			for (String key : criteria.keySet()){
				counter1++;
				query.setParameter("param"+String.valueOf(counter1), criteria.get(key));			
			}			
			return query.getResultList();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
			return null;
		}catch(Exception e){
			throw e;
		}
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
