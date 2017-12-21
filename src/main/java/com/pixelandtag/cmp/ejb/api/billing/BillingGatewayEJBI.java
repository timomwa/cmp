package com.pixelandtag.cmp.ejb.api.billing;

import com.pixelandtag.cmp.entities.BillingType;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.SuccessfullyBillingRequests;
import com.pixelandtag.smssenders.SenderResp;

public interface BillingGatewayEJBI {

	public SenderResp bill(Billable billable) throws BillingGatewayException;
	
	public void createSuccesBillRec(Billable billable);
	
	public SuccessfullyBillingRequests createSuccessBillingRec(OutgoingSMS outgoingsms, BillingType billingType);

	public SuccessfullyBillingRequests createSuccessBillingRec(IncomingSMS incomingsms, BillingType moBilling);

}
