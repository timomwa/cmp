package com.pixelandtag.cmp.ejb;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.ERROR;
import com.pixelandtag.cmp.entities.HttpToSend;
import com.pixelandtag.cmp.entities.SMSMenuLevels;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.serviceprocessors.dto.ServiceSubscription;
import com.pixelandtag.serviceprocessors.dto.SubscriptionDTO;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.Subscription;
import com.pixelandtag.sms.producerthreads.USSDSession;
import com.pixelandtag.smsmenu.MenuItem;
import com.pixelandtag.smsmenu.Session;
import com.pixelandtag.subscription.SubscriptionSource;
import com.pixelandtag.subscription.dto.SMSServiceDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.beans.RequestObject;

public interface CMPResourceBeanRemote extends BaseEntityI {
	
	public boolean updateMessageInQueue(BigInteger cp_tx_id, BillingStatus billstatus) throws Exception;
	
	public <T> Collection<T> find(Class<T> entityClass,Map<String, Object> criteria, int start, int end)  throws Exception ;
	
	public boolean testEJB(int k) throws Exception;
	
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

	public boolean subscribe(String mSISDN, SMSService smsService, int smsmenu_levels_id_fk) throws Exception ;

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

	public MenuItem getMenuByParentLevelId(int language_id, int parent_level_id, int menuid)  throws Exception;

	public MenuItem getTopMenu(int menu_id, int language_id)  throws Exception;

	public MenuItem getMenuById(int menu_id) throws Exception;

	public LinkedHashMap<Integer, MenuItem> getSubMenus(int parent_level_id_fk) throws Exception;

	public Session getSession(String msisdn) throws Exception;

	public boolean updateSession(int language_id, String msisdn,
			int smsmenu_levels_id_fk) throws Exception;

	public void printMenu(MenuItem menuItem, int level, int position)  throws Exception;

	public int getSubscriberLanguage(String msisdn) throws Exception;

	public String getMessage(String key, int language_id) throws Exception;
	
	public boolean updateProfile(String msisdn, int language_id) throws Exception;
	
	public String getMessage(MessageType messageType,
			int language) throws Exception;

	//public MOSms getContentFromServiceId(int service_id, String msisdn, boolean isSubscription)  throws Exception;
	
	public SMSServiceDTO getSMSservice(int service_id) throws Exception;
	
	public ServiceProcessorDTO getServiceProcessor(int processor_id_fk) throws Exception;

	public boolean subscribe(String mSISDN, SMSService smsService, int id,
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
	
	public boolean updateSMSStatLog(BigInteger transaction_id, ERROR errorcode) throws Exception;

	public List<SMSMenuLevels> listChildren(Long id)  throws Exception ;

	public boolean saveStaticSMS(String db_name, String table,
			String static_category_value, String sms) throws Exception;

	public String processUSSD(RequestObject ro) throws USSDEception;

	public String topUp(RequestObject ro) throws USSDEception;
	public MenuItem getTopMenu(String string) throws Exception;

	public String getSubMenuString(String keyword, int language_id) throws Exception;

	public void updateSession(int language_id, String msisdn,
			int smsmenu_levels_id_fk, USSDSession sess, int menuId, BigInteger sessionId);

	public MTsms getMTsms(Long id);
	
	public MTsms convertToLegacyMt(HttpToSend httpTosend);
	

}
