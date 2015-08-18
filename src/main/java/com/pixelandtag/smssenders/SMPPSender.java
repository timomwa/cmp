package com.pixelandtag.smssenders;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.cmp.entities.OutgoingSMS;


public class SMPPSender extends GenericSender {
	
	SMPPSender(SenderConfiguration configs) throws MessageSenderException {
		super(configs);
	}

	private Logger logger = Logger.getLogger(getClass());

	@Override
	public void validateMandatory() throws MessageSenderException {
	}



	@Override
	public SenderResp sendSMS(OutgoingSMS outgoingsms)
			throws MessageSenderException {
		return null;
	}

}
