package com.pixelandtag.cmp.ejb;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.subscription.dto.SubscriptionStatus;

public interface BaseEntityI {
	public static final String EXPIRY_DATE_TAG = "<EXPIRY_DATE>";
	public static final String SERVICE_NAME_TAG = "<SERVICE_NAME>";
	public static final String BILLING_FAILED = "BILLING_FAILED";
	public static final String PRICE_TAG = "<PRICE>";
	public static final String FREQUENCY = "<FREQUENCY>";
	public static SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public <T> Collection<T> listAll(Class<T> entityClass) throws Exception;
	public <T> T find(Class<T> entityClass, Long id) throws Exception;
	public <T> Collection<T> find(Class<T> entityClass,	Map<String, Object> criteria, int start, int end)   throws Exception;
	public <T> T saveOrUpdate(T t) throws Exception ;
	public <T> T find(Class<T> entityClass, String param_name, Object value) throws Exception;
	public boolean toStatsLog(IncomingSMS incomingsms, String toStatsLog)  throws Exception ;
	public boolean  acknowledge(long message_log_id) throws Exception;
	public boolean sendMTSMPP(OutgoingSMS outgoingsms,Long smppid) throws Exception;
	public Billable charge(Billable billable) throws Exception;
	public SMSService getSMSService(String cmd, OperatorCountry opco)  throws Exception;
	public IncomingSMS logMO(IncomingSMS mo) ;
	public String replaceAllIllegalCharacters(String text);
	public boolean hasAnyActiveSubscription(String msisdn, List<String> services, OperatorCountry operatorCountry) throws Exception;
	public void mimicMO(String keyword, String msisdn, OperatorCountry operatorCountry);
	public OutgoingSMS sendMT(OutgoingSMS mo) throws Exception;
	public String generateNextTxId();
	public boolean sendMTSMPP(Long sppid,String msisdn,String shortcode,String sms,String mo_text, Integer priority) throws Exception;
	public void createSuccesBillRec(Billable billable);
	public boolean changeStatusIfSubscribed(String msisdn, List<String> services, SubscriptionStatus status);
	public ServiceProcessorDTO getServiceProcessor(Long processor_id_fk) throws Exception;
	public OpcoSenderReceiverProfile getopcosenderProfileFromOpcoId(Long opcoid);
	
	


}
