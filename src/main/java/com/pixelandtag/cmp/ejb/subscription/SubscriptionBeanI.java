package com.pixelandtag.cmp.ejb.subscription;

import java.util.List;

import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.subscription.Subscription;

public interface SubscriptionBeanI {
	
	
	/**
	 * Gets a list of expired subscriptions.
	 * @param size - the list size you want returned
	 * @return java.util.List<Subscription>
	 */
	public List<Subscription> getExpiredSubscriptions(Long size);
	
	
	/**
	 * Renews the subscription to this services for this msisdn
	 * @param msisdn
	 * @param smsService - Long serviceid
	 * @return
	 * @throws Exception
	 */
	public Subscription renewSubscription(String msisdn, Long serviceId) throws Exception;
	
	
	/**
	 * Renews the subscription to this services for this msisdn
	 * @param msisdn
	 * @param smsService
	 * @return
	 * @throws Exception
	 */
	public Subscription renewSubscription(String msisdn, SMSService smsService) throws Exception;
	
	
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
	 * 
	 * @param string - status "RENEWAL"
	 * @param id - subscription id
	 * @throws Exception
	 */
	public void updateQueueStatus(Long status, Long id) throws Exception;
	
	/**
	 * 
	 * @param status
	 * @param msisdn
	 * @param sms_service_id
	 * @throws Exception
	 */
	public void updateQueueStatus(Long status, String msisdn, Long sms_service_id) throws Exception;

}
