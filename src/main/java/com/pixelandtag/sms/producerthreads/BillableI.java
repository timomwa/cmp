package com.pixelandtag.sms.producerthreads;

public interface BillableI {
	
	public String getOperation();

	public void setOperation(String operation);

	public String getMsisdn();

	public void setMsisdn(String msisdn);

	public String getShortcode();

	public void setShortcode(String shortcode);

	public String getKeyword();

	public void setKeyword(String keyword);

	public String getPrice();

	public void setPrice(String price);

	public String getCp_id();

	public void setCp_id(String cp_id);

	public EventType getEvent_type();

	public void setEvent_type(EventType event_type);

	public String getService_id();

	public void setService_id(String service_id);

	public String getDiscount_applied();

	public void setDiscount_applied(String discount_applied);

	public long getCp_tx_id();

	public void setCp_tx_id(long cp_tx_id);

	public long getTx_id();

	public void setTx_id(long tx_id);

	public boolean isInqueue();

	public void setInqueue(boolean inqueue);

	public boolean isProcessed();

	public void setProcessed(boolean processed);

	public String getChargeXML();
	
	public String toString();

	public final String plainchargeXML =  "" +
		      "<soapenv:Envelope\r\n" +
		      "xmlns:soapenv=" + 
		      "\"http://schemas.xmlsoap.org/soap/envelope/\"\r\n" + 
		      "xmlns:char=" + 
		      "\"http://ChargingProcess/com/ibm/sdp/services/charging/abstraction/Charging\">\r\n" +  
		      "<soapenv:Header />\r\n" + 
		      "<soapenv:Body>\r\n" + 
		      "<char:charge>\r\n" + 
		      "<inputMsg>\r\n" + 
		      "<operation>{OPERATION}</operation>\r\n" + 
		      "<userId>{MSISDN}</userId>\r\n" + 
		      "<contentId>{SHORTCODE}_{KEYWORD}</contentId>\r\n" + 
		      "<itemName>{SHORTCODE}_{KEYWORD}</itemName>\r\n" + 
		      "<contentDescription>{SHORTCODE}_{KEYWORD}</contentDescription>\r\n" +
		     "<circleId></circleId>\r\n" +
		      "<lineOfBusiness></lineOfBusiness>\r\n" + 
		     "<customerSegment></customerSegment>\r\n" +
		      "<contentMediaType>{SHORTCODE}_{KEYWORD}</contentMediaType>\r\n" + 
		     "<serviceId>{SERVICE_ID}</serviceId>\r\n" + 
		    "<parentId></parentId>\r\n" +
		      "<actualPrice>{PRICE}</actualPrice>\r\n" + 
		      "<basePrice>{PRICE}</basePrice>\r\n" +
		      "<discountApplied>0</discountApplied>\r\n" +
		     "<paymentMethod></paymentMethod>\r\n" +
		    "<revenuePercent></revenuePercent>\r\n" +
		   "<netShare>0</netShare>\r\n" +
		      "<cpId>{CP_ID}</cpId>\r\n" +
		     "<customerClass></customerClass>\r\n" +
		      "<eventType>{EVENT_TYPE}</eventType>\r\n" +//very important
		     "<localTimeStamp></localTimeStamp>\r\n" +
		    "<transactionId>{TX_ID}</transactionId>\r\n" +
		   "<subscriptionTypeCode>abcd</subscriptionTypeCode>\r\n" +
		  "<subscriptionName>0</subscriptionName>\r\n" +
		 "<parentType></parentType>\r\n" +
		      "<deliveryChannel>SMS</deliveryChannel>\r\n" +
		     "<subscriptionExternalId>0</subscriptionExternalId>\r\n" +
		     "<contentSize></contentSize>\r\n" +
		      "<currency>Kshs</currency>\r\n" + 
		      "<copyrightId>mauj</copyrightId>\r\n" + 
		     "<cpTransactionId>{CP_TX_ID}</cpTransactionId>\r\n" + 
		    "<copyrightDescription>copyright</copyrightDescription>\r\n" + 
		      "<sMSkeyword>{KEYWORD}</sMSkeyword>\r\n" + 
		      "<srcCode>{SHORTCODE}</srcCode>\r\n" + 
		     "<contentUrl>www.content360.co.ke</contentUrl>\r\n" + 
		    "<subscriptiondays>2</subscriptiondays>\r\n" +
		      "</inputMsg>\r\n" + 	      
		      "</char:charge>\r\n" + 		      
		      "</soapenv:Body>\r\n" +  
		      "</soapenv:Envelope>\r\n";
}
