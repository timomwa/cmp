package com.pixelandtag.cmp.ejb.api.sms;

import com.pixelandtag.cmp.entities.OutgoingSMS;
/**
 * 
 * @author Timothy Mwangi
 * 
 * 
 */
public interface SMSGatewayI {

	/**
	 * Sends an mt sms using the 
	 * parameters in the com.pixelandtag.cmp.entities.OutgoingSMS object
	 * @param outgoingsms - com.pixelandtag.cmp.entities.OutgoingSMS
	 * @return true if successfully queued for sending, false if not
	 * @throws SMSGatewayException
	 */
	public boolean sendMT(OutgoingSMS outgoingsms) throws SMSGatewayException;
}
