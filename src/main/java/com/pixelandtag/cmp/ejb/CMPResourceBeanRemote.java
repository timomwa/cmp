package com.pixelandtag.cmp.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.hibernate.HibernateException;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.serviceprocessors.dto.ServiceSubscription;
import com.pixelandtag.serviceprocessors.dto.SubscriptionDTO;
import com.pixelandtag.sms.producerthreads.Billable;

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
	
	public String getUnique(String database_name,String table,String field,String idfield,String msisdn,int serviceid,int size,int processor_id, Connection conn) throws Exception;
	
	
	public List<SubscriptionDTO> getSubscriptionServices()  throws Exception ;

	public List<ServiceSubscription> getServiceSubscription()  throws Exception ;

	public List<String> listServiceMSISDN(String sub_status, int serviceid)  throws Exception ;

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

}
