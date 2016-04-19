package com.pixelandtag.cmp.ejb.api.billing;

import com.pixelandtag.sms.producerthreads.Billable;

public interface BillingGatewayI {

	public boolean bill(Billable billable) throws BillingGatewayException;

}
