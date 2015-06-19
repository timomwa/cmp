package com.pixelandtag.cmp.ejb;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.exceptions.TransactionIDGenException;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.SuccessfullyBillingRequests;
import com.pixelandtag.subscription.dto.SubscriptionStatus;

public interface BaseEntityI {
	public static final String EXPIRY_DATE_TAG = "<EXPIRY_DATE>";
	public static final String SERVICE_NAME_TAG = "<SERVICE_NAME>";
	public static final String BILLING_FAILED = "BILLING_FAILED";
	public static SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public <T> T find(Class<T> entityClass, Long id) throws Exception;
	public <T> Collection<T> find(Class<T> entityClass,	Map<String, Object> criteria, int start, int end)   throws Exception;
	public <T> T saveOrUpdate(T t) throws Exception ;
	public <T> T find(Class<T> entityClass, String param_name, Object value) throws Exception;
	public boolean toStatsLog(MOSms mo, String toStatsLog)  throws Exception ;
	public boolean  acknowledge(long message_log_id) throws Exception;
	public boolean sendMT(MOSms mo, String sql) throws Exception;
	public boolean sendMTSMPP(MOSms mo,Long smppid) throws Exception;
	public EntityManager getEM();
	public Billable charge(Billable billable) throws Exception;
	public SMSService getSMSService(String cmd)  throws Exception;
	public MOSms logMO(MOSms mo) throws TransactionIDGenException;
	public String replaceAllIllegalCharacters(String text);
	public MOSms resolveKeywords(MOSms mo);
	public boolean hasAnyActiveSubscription(String msisdn, List<String> services) throws Exception;
	public MOSms updateMO(MOSms mo) throws TransactionIDGenException;
	public void updateMO(String msg, Long msgId) throws TransactionIDGenException;
	public void mimicMO(String keyword, String msisdn);
	public boolean sendMT(MOSms mo) throws Exception;
	public long generateNextTxId();
	public boolean sendMTSMPP(Long sppid,String msisdn,String shortcode,String sms,String mo_text, Integer priority) throws Exception;
	public void createSuccesBillRec(Billable billable);
	public boolean changeStatusIfSubscribed(String msisdn, List<String> services, SubscriptionStatus status);
	public ServiceProcessorDTO getServiceProcessor(Long processor_id_fk) throws Exception;
	public boolean sendMTSMPP(MTsms mt, Long sppid) throws Exception;
	


}
