package com.pixelandtag.smssenders;

import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.entities.MTsms;


public class SMPPSender extends GenericSender {
	
	SMPPSender(SenderConfiguration configs) throws MessageSenderException {
		super(configs);
		// TODO Auto-generated constructor stub
	}

	private Logger logger = Logger.getLogger(getClass());

	@Override
	public SenderResp sendSMS(MTsms mtsms) throws MessageSenderException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void validateMandatory() throws MessageSenderException {
		// TODO Auto-generated method stub
		
	}

}
