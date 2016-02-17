package com.pixelandtag.smssenders;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.cmp.entities.OutgoingSMS;

public class SMPPSender extends GenericSender {

	private Logger logger = Logger.getLogger(getClass());

	SMPPSender(SenderConfiguration configs) throws MessageSenderException {
		super(configs);

	}

	@Override
	public void validateMandatory() throws MessageSenderException {

	}

	@Override
	public SenderResp sendSMS(OutgoingSMS outgoingsms) throws MessageSenderException {
		SenderResp response = new SenderResp();

		return response;
	}

}
