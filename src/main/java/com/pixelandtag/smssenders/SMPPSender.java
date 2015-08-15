package com.pixelandtag.smssenders;

import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.pixelandtag.entities.MTsms;

@Stateless
public class SMPPSender implements Sender {
	
	private Logger logger = Logger.getLogger(getClass());

	@Override
	public SenderResp sendSMS(MTsms mtsms) throws MessageSenderException {
		// TODO Auto-generated method stub
		return null;
	}

}
