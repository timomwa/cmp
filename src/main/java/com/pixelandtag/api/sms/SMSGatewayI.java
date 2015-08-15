package com.pixelandtag.api.sms;

import com.pixelandtag.entities.MTsms;
/**
 * 
 * @author Timothy Mwangi
 * 
 * 
 */
public interface SMSGatewayI {

	/**
	 * Sends an mt sms using the 
	 * parameters in the com.pixelandtag.entities.MTsms object
	 * @param mtsms - com.pixelandtag.entities.MTsms
	 * @return true if successfully queued for sending, false if not
	 * @throws SMSGatewayException
	 */
	public boolean sendMT(MTsms mtsms) throws SMSGatewayException;
}
