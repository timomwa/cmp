package com.pixelandtag.cmp.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.hibernate.HibernateException;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.serviceprocessors.dto.ServiceSubscription;
import com.pixelandtag.serviceprocessors.dto.SubscriptionDTO;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.Subscription;
import com.pixelandtag.smsmenu.MenuItem;
import com.pixelandtag.smsmenu.Session;
import com.pixelandtag.subscription.SubscriptionSource;
import com.pixelandtag.subscription.dto.SMSServiceDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.web.beans.MessageType;

public interface CMPResourceBeanRemote {
	
	public EntityManager getEM();
	
	public <T> T saveOrUpdate(T t) throws Exception ;
	
	public boolean updateMessageInQueue(long cp_tx_id, BillingStatus billstatus) throws Exception;
	
	public <T> T find(Class<T> entityClass, Long id) throws Exception;
	
	public <T> T find(Class<T> entityClass, String param_name, Object value) throws Exception;
	
	public <T> Collection<T> find(Class<T> entityClass,Map<String, Object> criteria, int start, int end)  throws Exception ;
	
	public boolean testEJB(int k) throws Exception;

	public boolean toStatsLog(MOSms mo, String toStatsLog)  throws Exception ;
	
	public boolean  acknowledge(long message_log_id) throws Exception;

	public boolean sendMT(MOSms mo, String sql) throws Exception;
	
	public String getUnique(String database_name,String table,String field,String idfield,String msisdn,int serviceid,int size,int processor_id) throws Exception;
	
	
	public List<SubscriptionDTO> getSubscriptionServices()  throws Exception ;

	public List<ServiceSubscription> getServiceSubscription()  throws Exception ;

	public List<Subscription> listServiceMSISDN(String sub_status, int serviceid)  throws Exception ;

	public boolean updateServiceSubscription(int subscription_service_id)  throws Exception ;

	public void setServerTz(String server_tz)  throws Exception ;

	public void setClientTz(String client_tz)  throws Exception ;
	
	public String getServerTz()  throws Exception ;

	public String getClientTz()  throws Exception ;
	
	public Map<String, String> getAdditionalServiceInfo(int serviceid)  throws Exception;

	public com.pixelandtag.subscription.dto.SubscriptionDTO getSubscriptionDTO(
			String mSISDN, int serviceid)  throws Exception;

	public boolean subscribe(String mSISDN, int serviceid, int smsmenu_levels_id_fk) throws Exception ;

	/**
	 * 
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
	 * @return
	 */
	public String getUniqueFromCategory(String database_name,String table,
			String field,String idfield,String categoryfield,String categoryvalue,
			String msisdn,int serviceid,int size,int processor_id) throws Exception;

	/**
	 * 
	 * @param serviceid
	 * @param meta_field
	 * @return
	 * @throws Exception
	 */
	public String getServiceMetaData(int serviceid,
			String meta_field) throws Exception;
	
	public List<Billable> getBillable(int limit) throws Exception;

	public boolean deleteMT(long id) throws Exception ;

	public boolean logMT(MTsms mt) throws Exception ;

	public boolean logResponse(String msisdn, String sms) throws Exception;

	public boolean postponeMT(long id) throws Exception;

	public int countSubscribers(int service_id) throws Exception;

	public int countPushesToday(int service_id) throws Exception;

	public boolean shouldPushNow(int service_id) throws Exception;

	public int getHourNow() throws Exception;

	public boolean deleteOldLogs() throws Exception;

	public MenuItem getMenuByParentLevelId(int language_id, int parent_level_id)  throws Exception;

	public MenuItem getTopMenu(int menu_id, int language_id)  throws Exception;

	public MenuItem getMenuById(int menu_id) throws Exception;

	public LinkedHashMap<Integer, MenuItem> getSubMenus(int parent_level_id_fk) throws Exception;

	public Session getSession(String msisdn) throws Exception;

	public boolean updateSession(int language_id, String msisdn,
			int smsmenu_levels_id_fk) throws Exception;

	public void printMenu(MenuItem menuItem, int level, int position)  throws Exception;

	public int getSubscriberLanguage(String msisdn) throws Exception;

	public String getMessage(String mainMenuAdvice, int language_id) throws Exception;
	
	public boolean updateProfile(String msisdn, int language_id) throws Exception;
	
	public String getMessage(MessageType messageType,
			int language) throws Exception;

	//public MOSms getContentFromServiceId(int service_id, String msisdn, boolean isSubscription)  throws Exception;
	
	public SMSServiceDTO getSMSservice(int service_id) throws Exception;
	
	public ServiceProcessorDTO getServiceProcessor(int processor_id_fk) throws Exception;

	public boolean subscribe(String mSISDN, int service_id, int id,
			SubscriptionStatus confirmed, SubscriptionSource sms) throws Exception;

	public com.pixelandtag.subscription.dto.SubscriptionDTO checkAnyPending(
			String msisdn) throws Exception ;

	public boolean updateSubscription(int subscription_id, SubscriptionStatus status) throws Exception;

	public LinkedHashMap<Integer, SMSServiceDTO> getAllSubscribedServices(
			String msisdn) throws Exception;

	public boolean unsubscribeAll(String msisdn, SubscriptionStatus status) throws Exception;

	public boolean updateSubscription(int id, String mSISDN,
			SubscriptionStatus unsubscribed) throws Exception;

	public SMSServiceDTO getSMSservice(String keyword) throws Exception;

}
