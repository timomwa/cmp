package com.pixelandtag.api;

import java.sql.Connection;

import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;

public interface ServiceProcessorI extends Runnable{
	
	public String name = "ServiceProcessorI";
	
	/**
	 * Process what's to be done to an MO and 
	 * @param mo
	 * @return
	 */
	public OutgoingSMS process(IncomingSMS incomingSMS);
	
	public boolean acknowledge(IncomingSMS incomingsms);
	
	
	public void run();
	
	public void setName(String name);
	
	public String getName();
	
	public boolean isBusy();
	
	public boolean isRunning();
	
	public void setRun(boolean run);
	
	public void finalizeMe();
	
	
	/**
	 * Enqueues an MOSsms object into the internal
	 * queue of the processor. Due to Memory considerations,
	 * the internal queue should not be more than 100 SMS.
	 * @param mo - com.pixelandtag.celcom.entities.MOSms
	 * @return true if the MOSms object was added to the queue successfully, 
	 * else return false
	 */
	public boolean submit(IncomingSMS mo);
	
	
	/**
	 * Insert into http to send/or any other way
	 * the MT should be sent.
	 * @param mo - com.pixelandtag.celcom.entities.MOSms
	 */
	public void sendMT(OutgoingSMS mo);
	
	
	public int getQueueSize();
	
	
	public boolean queueFull();

	public void setInternalQueue(int i);
	
	public String getSubscriptionText();
	public void setSubscriptionText(String subscriptionText);
	public String getUnsubscriptionText();
	public void setUnsubscriptionText(String unsubscriptionText);
	public String getTailTextSubscribed();
	public void setTailTextSubscribed(String tailTextSubscribed) ;
	public String getTailTextNotSubecribed() ;
	public void setTailTextNotSubecribed(String tailTextNotSubecribed);
	
	
}
