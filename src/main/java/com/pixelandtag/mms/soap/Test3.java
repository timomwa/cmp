package com.pixelandtag.mms.soap;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.smssenders.JsonUtilI;
import com.pixelandtag.smssenders.JsonUtilImpl;

public class Test3 {
	
	private static JsonUtilI jsonutil = new JsonUtilImpl();
	private static String jsonstr = "{ \"amountTransaction\": { \"resourceURL\": \"10.48.0.7\\/1\\/payment\\/tel%3A%2B254776165280\\/transactions\\/amount\\/32557233164188434\", \"endUserId\": \"254776165280\", \"paymentAmount\": { \"chargingInformation\": { \"code\": \"32329\", \"description\": \"Dating service\", \"currency\": \"KES\", \"amount\": 5.000000 }, \"totalAmountCharged\": 5.000000 }, \"referenceCode\": \"32557233164188434\", \"transactionOperationStatus\": \"Charged\", \"serverReferenceCode\": \"32557233164188434\", \"clientCorrelator\": \"32557233164188434\" } }" ;
	
	private static Map<Long, String> sms_serviceCache = new HashMap<Long, String>();
	public static void main(String[] args) throws JSONException {
		
		System.out.println("<sms7:notifySmsDeliveryReceipt></sms7:notifySmsDeliveryReceipt>".contains("<sms7:notifySmsDeliveryReceipt>"));
	}

}
