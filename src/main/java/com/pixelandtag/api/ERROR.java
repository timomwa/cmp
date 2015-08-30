package com.pixelandtag.api;

import com.pixelandtag.sms.mt.ACTION;

public enum ERROR {

	LimitSubscriberFailure, 
	InvalidSubscriber, 
	KeywordsNotFound, 
	TariffCodeNotFound, 
	Invalidshortcode, 
	SetCMPConnectionFailure, 
	ServicePriceNotFound, 
	PSAInsufficientBalance, 
	PSAInvalidNumber, 
	PSANumberBarred, 
	PSABusy, 
	PSACreditExceeded, 
	PSAChargeFailure, 
	SCRNotInAllowList, 
	SubscriberBlackListed, 
	PCMSendFail, 
	NegativeDN,
	Success,
	PCM200,//MessageEmail sent to queue successfully
	PCM301,//Invalid Registration Request
	PCM302,//Invalid Stop Request
	PCM303,//Invalid Keyword Request
	PCM304,//Service Suspended/Deleted
	PCM305,//Registration Failed (Reg ACK exceed 5min)
	PCM306,//MOID mismatch
	PCM307,//Duplication on REG ACK
	PCM400,//Missing param or invalid type field.
	PCM402,//Invalid pricecode
	PCM403,//Invalid Service
	PCM404,//Exceeded IOD Push Frequency
	PCM405,//Exceeded SubscriptionOld Push Frequency
	PCM406,//Invalid IOD services (Msgid mismatch)
	ContentType_Is_Null, 
	SERVER_INTERNAL_QUEUE_FULL,
	FailedToSend,//If the MT wasn't sent for whichever reason
	WaitingForDLR;//Inobia specific. We want to know if we're
	

	
	
	
	public static ERROR get(String val){
		
		ERROR error = null;
		
		if(val==null || val.isEmpty() || val.equals(""))
			return null;
		
		val = val.trim();
		
		
		if(val.equals("SERVER_INTERNAL_QUEUE_FULL"))
			error = ERROR.SERVER_INTERNAL_QUEUE_FULL;
		
		if(val.equals("ContentType_Is_Null"))
			error = ERROR.ContentType_Is_Null;
		
		if(val.equals("InvalidSubscriber"))
			error = ERROR.InvalidSubscriber;
		
		if(val.equals("KeywordsNotFound"))
			error = ERROR.KeywordsNotFound;
		
		if(val.equals("TariffCodeNotFound"))
			error = ERROR.TariffCodeNotFound;
		
		if(val.equals("Invalidshortcode"))
			error = ERROR.Invalidshortcode;
		
		if(val.equals("SetCMPConnectionFailure"))
			error = ERROR.SetCMPConnectionFailure;
		
		if(val.equals("ServicePriceNotFound"))
			error = ERROR.ServicePriceNotFound;
		
		
		if(val.equals("PSAInsufficientBalance"))
			error = ERROR.PSAInsufficientBalance;
		
		
		if(val.equals("PSAInvalidNumber"))
			error = ERROR.PSAInvalidNumber;
		
		
		if(val.equals("PSANumberBarred"))
			error = ERROR.PSANumberBarred;
		
		if(val.equals("PSABusy"))
			error = ERROR.PSABusy;
		
		if(val.equals("PSACreditExceeded"))
			error = ERROR.PSACreditExceeded;
		
		if(val.equals("PSAChargeFailure"))
			error = ERROR.PSAChargeFailure;
		
		if(val.equals("SCRNotInAllowList"))
			error = ERROR.SCRNotInAllowList;
		
		if(val.equals("SubscriberBlackListed"))
			error = ERROR.SubscriberBlackListed;
		
		if(val.equals("PCMSendFail"))
			error = ERROR.PCMSendFail;
		
		if(val.equals("NegativeDN"))
			error = ERROR.NegativeDN;
		
		if(val.equals("Success"))
			error = ERROR.Success;
		if(val.equals("PCM200"))
			error = ERROR.PCM200;
		if(val.equals("PCM301"))
			error = ERROR.PCM301;
		if(val.equals("PCM302"))
			error = ERROR.PCM302;
		if(val.equals("PCM303"))
			error = ERROR.PCM303;
		if(val.equals("PCM304"))
			error = ERROR.PCM304;
		if(val.equals("PCM305"))
			error = ERROR.PCM305;
		if(val.equals("PCM306"))
			error = ERROR.PCM306;
		if(val.equals("PCM307"))
			error = ERROR.PCM307;
		if(val.equals("PCM400"))
			error = ERROR.PCM400;
		if(val.equals("PCM402"))
			error = ERROR.PCM402;
		if(val.equals("PCM403"))
			error = ERROR.PCM403;
		if(val.equals("PCM404"))
			error = ERROR.PCM404;
		if(val.equals("PCM405"))
			error = ERROR.PCM405;
		if(val.equals("PCM406"))
			error = ERROR.PCM406;
		
		
		return error;
	}

}
