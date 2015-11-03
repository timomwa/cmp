package com.pixelandtag.cmp.ejb;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.json.JSONException;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.ERROR;
import com.pixelandtag.cmp.entities.HttpToSend;
import com.pixelandtag.cmp.entities.SMSMenuLevels;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.SMSServiceMetaData;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.serviceprocessors.dto.ServiceSubscription;
import com.pixelandtag.serviceprocessors.dto.SubscriptionDTO;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.CleanupDTO;
import com.pixelandtag.sms.producerthreads.SuccessfullyBillingRequests;
import com.pixelandtag.sms.producerthreads.USSDSession;
import com.pixelandtag.smsmenu.MenuItem;
import com.pixelandtag.smsmenu.Session;
import com.pixelandtag.subscription.SubscriptionSource;
import com.pixelandtag.subscription.dto.SMSServiceDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.beans.RequestObject;

public interface CMPResourceBeanRemote extends BaseEntityI {
	
	public boolean updateMessageInQueue(String cp_tx_id, BillingStatus billstatus) throws Exception;
	
	public <T> Collection<T> find(Class<T> entityClass,Map<String, Object> criteria, int start, int end)  throws Exception ;
	
	public boolean testEJB(int k) throws Exception;
	
	public String getUnique(String database_name,String table,String field,String idfield,String msisdn,int serviceid,int size,int processor_id) throws Exception;
	
	public List<SubscriptionDTO> getSubscriptionServices()  throws Exception ;

	public List<ServiceSubscription> getServiceSubscription()  throws Exception ;

	public boolean updateServiceSubscription(int subscription_service_id)  throws Exception ;

	public void setServerTz(String server_tz)  throws Exception ;

	public void setClientTz(String client_tz)  throws Exception ;
	
	public String getServerTz()  throws Exception ;

	public String getClientTz()  throws Exception ;
	
	public Map<String, String> getAdditionalServiceInfo(int serviceid)  throws Exception;

	public com.pixelandtag.subscription.dto.SubscriptionDTO getSubscriptionDTO(
			String mSISDN, int serviceid)  throws Exception;

	public boolean subscribe(String mSISDN, SMSService smsService, int smsmenu_levels_id_fk, AlterationMethod method) throws Exception ;

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
			String msisdn,int serviceid,int size,Long processor_id) throws Exception;

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

	public String getMessage(String key, int language_id, Long opcoid) throws Exception;
	
	public boolean updateProfile(String msisdn, int language_id) throws Exception;
	
	public String getMessage(MessageType messageType,
			int language, Long opcoid) throws Exception;

	//public MOSms getContentFromServiceId(int service_id, String msisdn, boolean isSubscription)  throws Exception;
	
	public SMSServiceDTO getSMSservice(int service_id) throws Exception;

	public boolean subscribe(String mSISDN, SMSService smsService, int id,
			SubscriptionStatus confirmed, SubscriptionSource sms, AlterationMethod method) throws Exception;

	public com.pixelandtag.subscription.dto.SubscriptionDTO checkAnyPending(
			String msisdn) throws Exception ;

	public LinkedHashMap<Integer, SMSServiceDTO> getAllSubscribedServices(
			String msisdn) throws Exception;

	public boolean unsubscribeAll(String msisdn, SubscriptionStatus status, AlterationMethod method) throws Exception;


	public SMSServiceDTO getSMSservice(String keyword) throws Exception;
	
	public boolean updateSMSStatLog(String transaction_id, ERROR errorcode) throws Exception;

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
	public void clearUssdSesion(USSDSession sess);
	
	/**
	 * Marks a http to send record
	 * to be in the queue
	 * @param http_to_send_id
	 * @return
	 * @throws Exception
	 */
	public boolean markInQueue(Long http_to_send_id) throws Exception;
	
	/**
	 * 
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public List<Billable> getBillableSForCleanup(Date date) throws Exception;

	public int invalidateSimilarBillables(Billable bill) throws Exception;

	public List<CleanupDTO> getCleanupDtos(Date date) throws Exception;
	
	public List<Billable> getBillableSForTransfer(Date date) throws Exception;

	public List<ServiceProcessorDTO> getServiceProcessors();

	public List<SMSServiceMetaData> getSMSServiceMetaData(Long serviceid);

	public BigInteger count(String db_name, String table,
			String static_category_value);

	public List<Object[]> listContent(String db_name, String table,
			String static_category_value, int start, int limit);

	public Date listContent(String db_name, String table,
			String static_category_value, String sms);

	public List<SMSMenuLevels> getSMSMenuLevels();

	public String getBillingStats(String fromTz, String toTz) throws JSONException;

	public String getCurrentSubDistribution() throws  Exception;

	
	
	

}
