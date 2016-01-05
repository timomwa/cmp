package com.pixelandtag.cmp.ejb.subscription;

import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.subscription.DoubleConfirmationQueue;

public interface DoubleConfirmationQueueEJBI {

	public DoubleConfirmationQueue getQueue(String mSISDN, OpcoSMSService smsservice);

	public boolean enqueue(String mSISDN, OpcoSMSService smsservice)  throws Exception;

	public void dequeue(DoubleConfirmationQueue doubleConfirmationQueue)  throws Exception;

}
