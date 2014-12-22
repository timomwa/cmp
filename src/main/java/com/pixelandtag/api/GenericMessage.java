package com.pixelandtag.api;

import java.math.BigDecimal;

import com.pixelandtag.sms.producerthreads.EventType;

public abstract class GenericMessage {
	
	public final static String SUBSCRIPTION_BASED_CONTENT = "1";
	public final static String IOD_BASED_CONTENT = "2";
	public final static String PROMO_BASED_CONTENT = "3";
	public final static String DEDICATION_CONTENT = "4";
	public final static String RENEWAL_MT_TRIGGER = "5";
	public final static String NON_SMS_REGISTRATION = "6";
	public final static String FRIEND_FINDER = "7";
	public final static String NON_ASCII_SMS_ENCODING_ID = "8";
	public final static String ASCII_SMS_ENCODING_ID = "0";
	
	
	//**mm7
	public static final String STATUS_CODE_NODE_NAME = "StatusCode";
	public static final String STATUS_TEXT_NODE_NAME = "StatusText";
	public static final String SUCCESS_STATUS_CODE = "1000";
	public static final String STATUS_NODE_NAME = "Status";
	public static final String SOAP_MSG_ID_NODE_NAME = "MessageID";
	public static final String SUCCESS_NODE_NAME = "Success";
	public static final String MM7_VERSION = "5.3.0";
	//**mm7
	
	
	private int serviceid;
	private BigDecimal price;
	private long CMP_Txid;
	private int priority = 1;
	private int number_of_sms = 1;
	private boolean split_msg;
	private int processor_id;
	private String pricePointKeyword;

	
	private EventType eventType;
	
	
	private BillingStatus billingStatus;
	
	private boolean charged;
	
	public boolean isCharged() {
		return charged;
	}


	public EventType getEventType() {
		return eventType;
	}


	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}


	public String getPricePointKeyword() {
		return pricePointKeyword;
	}

	public void setPricePointKeyword(String pricePointKeyword) {
		this.pricePointKeyword = pricePointKeyword;
	}
	
	
	public void setCharged(boolean charged) {
		this.charged = charged;
	}
	public BillingStatus getBillingStatus() {
		if(billingStatus==null)
			return BillingStatus.NO_BILLING_REQUIRED;
		return billingStatus;
	}


	public void setBillingStatus(BillingStatus billingStatus) {
		this.billingStatus = billingStatus;
	}


	public BigDecimal getPrice() {
		return price;
	}

	public int getProcessor_id() {
		return processor_id;
	}

	public void setProcessor_id(int processor_id) {
		this.processor_id = processor_id;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public int getServiceid() {
		return serviceid;
	}

	public void setServiceid(int serviceid) {
		this.serviceid = serviceid;
	}

	public boolean isSplit_msg() {
		return split_msg;
	}

	public void setSplit_msg(boolean split_msg) {
		this.split_msg = split_msg;
	}
	
	public int getNumber_of_sms() {
		return number_of_sms;
	}

	public void setNumber_of_sms(int number_of_sms) {
		this.number_of_sms = number_of_sms;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setCMP_Txid(long CMP_Txid_) {
		this.CMP_Txid = CMP_Txid_;
	}
	
	public long getCMP_Txid() {
		return CMP_Txid;
	}

	@Override
	public String toString() {
		return "GenericMessage [CMP_Txid=" + CMP_Txid + ", priority="
				+ priority + ", number_of_sms=" + number_of_sms
				+ ", split_msg=" + split_msg + "]";
	}
	
	
	

}
