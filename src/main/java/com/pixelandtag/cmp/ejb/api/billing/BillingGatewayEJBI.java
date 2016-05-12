package com.pixelandtag.cmp.ejb.api.billing;

import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.smssenders.SenderResp;

public interface BillingGatewayEJBI {

	public SenderResp bill(Billable billable) throws BillingGatewayException;
	
	public void createSuccesBillRec(Billable billable);

}
