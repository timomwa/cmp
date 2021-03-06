package com.pixelandtag.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.pixelandtag.sms.producerthreads.EventType;

public abstract class GenericMessage implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2538337546926006071L;
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
	private String cmp_tx_id;
	private String opco_tx_id;
	private int priority = 1;
	private int number_of_sms = 1;
	private boolean split_msg;
	private Long processor_id;
	private String pricePointKeyword;
	private Long subscription_id;
	private Long opcoid;
	private String opcocode;
	private boolean subscription;
	private EventType eventType;
	private BillingStatus billingStatus;
	private boolean charged;
	private Long opcoprofileid;
	private String destSenderAddress;
	
	
	public boolean isCharged() {
		return charged;
	}


	public boolean isSubscription() {
		return subscription;
	}


	public void setSubscription(boolean subscription) {
		this.subscription = subscription;
	}


	public EventType getEventType() {
		return eventType;
	}


	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}


	public String getPricePointKeyword() {
		if(pricePointKeyword==null || pricePointKeyword.isEmpty())
			pricePointKeyword = "JOBS";
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
		if(price==null)
			return BigDecimal.ZERO;
		return price;
	}

	public Long getProcessor_id() {
		return processor_id;
	}

	public void setProcessor_id(Long processor_id) {
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

	public String getCmp_tx_id() {
		return cmp_tx_id;
	}


	public void setCmp_tx_id(String cmp_tx_id) {
		this.cmp_tx_id = cmp_tx_id;
	}


	public String getOpco_tx_id() {
		return opco_tx_id;
	}


	public void setOpco_tx_id(String opco_tx_id) {
		this.opco_tx_id = opco_tx_id;
	}


	public Long getSubscription_id() {
		return subscription_id;
	}


	public void setSubscription_id(Long subscription_id) {
		this.subscription_id = subscription_id;
	}
	
	
	
	public Long getOpcoid() {
		return opcoid;
	}


	public void setOpcoid(Long opcoid) {
		this.opcoid = opcoid;
	}

	

	@Override
	public String toString() {
		return "GenericMessage [serviceid=" + serviceid + ", price=" + price
				+ ", cmp_tx_id=" + cmp_tx_id + ", opco_tx_id=" + opco_tx_id
				+ ", priority=" + priority + ", number_of_sms=" + number_of_sms
				+ ", split_msg=" + split_msg + ", processor_id=" + processor_id
				+ ", pricePointKeyword=" + pricePointKeyword
				+ ", subscription_id=" + subscription_id + ", opcoid=" + opcoid
				+ ", opcocode=" + opcocode + ", subscription=" + subscription
				+ ", eventType=" + eventType + ", billingStatus="
				+ billingStatus + ", charged=" + charged + ", opcoprofile="
				+ opcoprofileid + "]";
	}


	public String getOpcocode() {
		return opcocode;
	}


	public void setOpcocode(String opcocode) {
		this.opcocode = opcocode;
	}


	public Long getOpcoprofileId() {
		return opcoprofileid;
	}


	public void setOpcoprofileId(Long opcoprofileid) {
		this.opcoprofileid = opcoprofileid;
	}


	public String getDestSenderAddress() {
		return destSenderAddress;
	}


	public void setDestSenderAddress(String destSenderAddress) {
		this.destSenderAddress = destSenderAddress;
	}
	
	
	

}
