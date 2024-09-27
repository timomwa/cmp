package com.pixelandtag.cmp.ejb.subscription;

import java.util.List;

import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.SubscriptionLog;
import com.pixelandtag.sms.producerthreads.SuccessfullyBillingRequests;
import com.pixelandtag.subscription.dto.MediumType;
import com.pixelandtag.subscription.dto.SubscriptionStatus;

public interface SubscriptionBeanI {
	
	
	/**
	 * Gets a list of expired subscriptions.
	 * @param size - the list size you want returned
	 * @return java.util.List<Subscription>
	 */
	public List<Subscription> getExpiredSubscriptions(Long size);
	
	
	/**
	 * Renews the subscription to this services for this msisdn
	 * @param operatorCountry
	 * @param msisdn
	 * @param smsService - Long serviceid
	 * @return
	 * @throws Exception
	 */
	public Subscription renewSubscription(OperatorCountry operatorCountry, String msisdn, Long serviceId, AlterationMethod method) throws Exception;
	
	
	/**
	 * Renews the subscription to this services for this msisdn
	 * @param operatorCountry 
	 * @param msisdn
	 * @param smsService
	 * @param substatus - com.pixelandtag.subscription.dto.SubscriptionStatus
	 * @return
	 * @throws Exception
	 */
	public Subscription renewSubscription(OperatorCountry operatorCountry, String msisdn, SMSService smsService,SubscriptionStatus substatus, AlterationMethod method) throws Exception;
	
	
	/**
	 * Lists msisdns that are in a given service
	 * @param sub_status
	 * @param serviceid
	 * @return
	 * @throws Exception
	 */
	public List<Subscription> listServiceMSISDN(String sub_status, int serviceid)  throws Exception;
	
	/**
	 * 
	 * @param msisdn
	 * @param serviceid
	 * @return
	 * @throws Exception
	 */
	public Subscription getSubscription(String msisdn, Long serviceid) throws Exception;
	
	/**
	 * Gets a list of subscriptions
	 * @param msisdn
	 * @return
	 * @throws Exception
	 */
	public List<Subscription> searchSubscriptions(String msisdn) throws Exception;
	
	
	/**
	 * Gets a list of subscriptions
	 * @param msisdn
	 * @return
	 * @throws Exception
	 */
	public List<Subscription> listSubscriptions(String msisdn) throws Exception;



	/**
	 * 
	 * @param string - status "RENEWAL"
	 * @param id - subscription id
	 * @throws Exception
	 */
	public void updateQueueStatus(Long status, Long id, AlterationMethod method) throws Exception;
	
	/**
	 * 
	 * @param status
	 * @param msisdn
	 * @param sms_service_id
	 * @throws Exception
	 */
	public void updateQueueStatus(Long status, String msisdn, Long sms_service_id, AlterationMethod method, OperatorCountry opco) throws Exception;

	/**
	 * 
	 * @param msisdn
	 * @param valueOf
	 * @param change
	 */
	public void updateCredibilityIndex(String msisdn, Long valueOf, int change, OperatorCountry opco);
	
	/**
	 * 
	 * @param msisdn
	 * @param service_id
	 * @return
	 */
	public Subscription subscribe(String msisdn, Long service_id, MediumType medium, AlterationMethod method, OperatorCountry opco) ;
	
	/**
	 * 
	 * @param id
	 * @param msisdn
	 * @param status
	 * @return
	 * @throws Exception
	 */
	public boolean updateSubscription(int id, String msisdn,SubscriptionStatus status, AlterationMethod method) throws Exception;
	
	/**
	 * 
	 * @param subscription_id
	 * @param status
	 * @return
	 * @throws Exception
	 */
	public boolean updateSubscription(int subscription_id, SubscriptionStatus status, AlterationMethod method) throws Exception;
	
	/**
	 * 
	 * @param msisdn
	 * @return
	 * @throws Exception
	 */
	public List<Subscription> searchSubscription(String msisdn) throws Exception;
	
	/**
	 * 
	 * @param msisdn
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public List<Subscription> searchSubscription(String msisdn, int start, int limit) throws Exception;
	
	
	/**
	 * 
	 * @param msisdn
	 * @return
	 * @throws Exception
	 */
	public Long countsearchSubscription(String msisdn) throws Exception;
	
	/**
	 * 
	 * @param msisdn
	 * @param cmd
	 * @return
	 * @throws Exception
	 */
	public Subscription getSubscription(String msisdn, String cmd) throws Exception;
	
	/**
	 * 
	 * @param msisdn
	 * @param serviceid
	 * @return
	 * @throws Exception
	 */
	public boolean subscriptionValid(String msisdn, Long serviceid,OperatorCountry opco) throws Exception;
	
	/**
	 * 
	 * @param msisdn
	 * @param keywords
	 * @return
	 * @throws Exception
	 */
	public boolean hasSubscribedToAnyOfTheseServices(String msisdn, List<String> keywords) throws Exception;
	
	
	/**
	 * 
	 * @param msisdn
	 * @param service_id
	 * @param medium
	 * @param method
	 * @param status
	 * @param opco
	 * @return
	 */
	public Subscription subscribe(String msisdn, Long service_id,MediumType medium, AlterationMethod method,  SubscriptionStatus status, OperatorCountry opco);


	/**
	 * 
	 * @param msisdn
	 * @param opcosmsservice
	 */
	public void unsubscribe(String msisdn, OpcoSMSService opcosmsservice);
	
	
	/**
	 * 
	 * @param msisdn
	 * @param serviceid
	 */
	public void unsubscribe(String msisdn, Long serviceid);
	
	/**
	 * 
	 * @param subscription
	 */
	public void unsubscribe(Subscription subscription)  throws Exception;
	
	/**
	 * 
	 * @param msisdn
	 * @param services
	 * @param opco
	 * @return
	 * @throws Exception
	 */
	public boolean unsubscribe(String msisdn, List<String> services, OperatorCountry opco) throws Exception;


	/**
	 * 
	 * @param billable
	 * @return
	 * @throws Exception
	 */
	public SuccessfullyBillingRequests createSuccesBillRec(Billable billable) throws Exception;


	/**
	 * 
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public <T> T saveOrUpdate(T t) throws Exception;

}
