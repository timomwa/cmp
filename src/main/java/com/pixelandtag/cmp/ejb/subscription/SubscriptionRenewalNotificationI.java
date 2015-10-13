package com.pixelandtag.cmp.ejb.subscription;

import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.subscription.Subscription;

public interface SubscriptionRenewalNotificationI {

	public boolean sendSubscriptionRenewalMessage(OperatorCountry operatorCountry,
			SMSService service, String msisdn, Subscription sub);

}
