package com.inmobia.celcom.serviceprocessors.sms;
/**
 * @author timothy
 * This is to show you how to create an sms service.
 * All you need to do is extend com.inmobia.celcom.api.GenericServiceProcessor
 * Override the process(MOSms mo) method to do your own processing of incoming messages
 * return the mo message and platform takes care of the rest
 */
import java.sql.Connection;

import com.inmobia.celcom.api.GenericServiceProcessor;
import com.inmobia.celcom.entities.MOSms;

public class SampleSMSService extends GenericServiceProcessor {

	@Override
	public MOSms process(MOSms mo) {
		//the object mo has all there is about a message
		//Do your service specific processing here
		mo.setMt_Sent("SMS TO SEND");//Set the response using setMt_Sent
		return mo;//Return the mo object and the rest is taken care of
	}

	@Override
	public void finalizeMe() {
		// TODO Auto-generated method stub

	}

	@Override
	public Connection getCon() {
		
		// TODO Auto-generated method stub
		return null;
	}

}
