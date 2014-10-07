package com.inmobia.mms.api;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;

import com.inmobia.celcom.api.ERROR;
import com.inmobia.celcom.api.GenericMessage;

public class MMS extends GenericMessage {
	
	
	public static final String JPG = "jpg";
	public static final String PNG = "jpg";
	public static final String GIF = "gif";
	public static final String FILE_SEPERATOR = "file.separator";
	private static final String WINDOWS_FS = "\\";
	public static final String FILE_SEPARATOR_REGEX = System.getProperty(FILE_SEPERATOR).equals(WINDOWS_FS) ? "[\\"+System.getProperty(FILE_SEPERATOR)+"]" : System.getProperty(FILE_SEPERATOR);
	public static final String UNDERSCORE = "_";
	private String id = "-1";
	private String timeStampOfInsertion;
	private String dlrArriveTime;
	private boolean inprocessingQueue;
	private boolean sent;
	private String dlrReportStatus;
	private String transactionID;
	private String MM7_VERSION = GenericMessage.MM7_VERSION;//"5.3.0";
	private String VASPID = "inmobia";
	private String VASID = "inmobia";
	private String shortcode = "23355";//basically the tarrif. - charge indicator
	private String servicecode = ServiceCode.RM0.getCode();//"5674";//No charge
	private String linked_id = "0000";
	private String msisdn;
	private String eariest_delivery_time = "2012-01-01 13:01:04";
	private String expiry_date = "2025-12-31 23:59:59";
	private String delivery_report_requested = "true";
	private String read_reply = "false";
	private String subject;
	private String distribution_indicator = "true";
	private volatile String media_path;
	private String mms_text = "MMS First Text";
	private String password_username = "inmobia:inmobia123";//HTTP authentication - should not be hardcoded!!!!!
	private boolean paidFor = false;
	private ERROR billingStatus;
	/**
	 * The txId to wait for to succeed before sending the SMS.
	 */
	private String wait_for_txId;
	
	
	
	
	public ERROR getBillingStatus() {
		return billingStatus;
	}
	public void setBillingStatus(ERROR billingStatus) {
		this.billingStatus = billingStatus;
	}
	public String getWait_for_txId() {
		return wait_for_txId;
	}
	public void setWait_for_txId(String wait_for_txId) {
		this.wait_for_txId = wait_for_txId;
	}
	public boolean isPaidFor() {
		return paidFor;
	}
	public void setPaidFor(boolean paidFor) {
		this.paidFor = paidFor;
	}
	public boolean isInprocessingQueue() {
		return inprocessingQueue;
	}
	public boolean isSent() {
		return sent;
	}
	public void setInprocessingQueue(boolean inprocessingQueue) {
		this.inprocessingQueue = inprocessingQueue;
	}
	public void setSent(boolean sent) {
		this.sent = sent;
	}
	public String getTimeStampOfInsertion() {
		return timeStampOfInsertion;
	}
	public String getDlrArriveTime() {
		return dlrArriveTime;
	}
	public String getDlrReportStatus() {
		return dlrReportStatus;
	}
	public String getMedia_path() {
		return media_path;
	}
	public void setTimeStampOfInsertion(String timeStampOfInsertion) {
		this.timeStampOfInsertion = timeStampOfInsertion;
	}
	public void setDlrArriveTime(String dlrArriveTime) {
		this.dlrArriveTime = dlrArriveTime;
	}
	public void setDlrReportStatus(String dlrReportStatus) {
		this.dlrReportStatus = dlrReportStatus;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPassword_username() {
		return password_username;
	}
	public void setPassword_username(String password_username) {
		this.password_username = password_username;
	}
	public String getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}
	public void setMM7_VERSION(String mM7_VERSION) {
		MM7_VERSION = mM7_VERSION;
	}
	public void setVASPID(String vASPID) {
		VASPID = vASPID;
	}
	public void setVASID(String vASID) {
		VASID = vASID;
	}
	public String getTxId() {
		return this.transactionID;
	}
	public String getMM7_VERSION() {
		return MM7_VERSION;
	}
	public String getVASPID() {
		return VASPID;
	}
	public String getVASID() {
		return VASID;
	}
	public String getShortcode() {
		return shortcode;
	}
	public String getServicecode() {
		return servicecode;
	}
	public String getLinked_id() {
		return linked_id;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public String getEariest_delivery_time() {
		return eariest_delivery_time;
	}
	public String getExpiry_date() {
		return expiry_date;
	}
	public String getDelivery_report_requested() {
		return delivery_report_requested;
	}
	public String getRead_reply() {
		return read_reply;
	}
	public String getSubject() {
		return subject;
	}
	public String getDistribution_indicator() {
		return distribution_indicator;
	}
	public String getMediaPath() {
		return media_path;
	}
	public String getMms_text() {
		return mms_text;
	}
	public void setTxId(String txId) {
		transactionID = txId;
	}
	public void setShortcode(String shortcode) {
		this.shortcode = shortcode;
	}
	public void setServicecode(String servicecode) {
		this.servicecode = servicecode;
	}
	public void setLinked_id(String linked_id) {
		this.linked_id = linked_id;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public void setEariest_delivery_time(String eariest_delivery_time) {
		this.eariest_delivery_time = eariest_delivery_time;
	}
	public void setExpiry_date(String expiry_date) {
		this.expiry_date = expiry_date;
	}
	public void setDelivery_report_requested(String delivery_report_requested_) {
		this.delivery_report_requested = delivery_report_requested_;
	}
	public void setRead_reply(String read_reply) {
		this.read_reply = read_reply;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public void setDistribution_indicator(String distribution_indicator) {
		this.distribution_indicator = distribution_indicator;
	}
	public void setMedia_path(String meia_path_) {
		this.media_path = meia_path_;
		/*
		try{
			
			
			this.setSubject(meia_path_.split(FILE_SEPARATOR_REGEX)[meia_path_.split(FILE_SEPARATOR_REGEX).length-1].split(UNDERSCORE)[0]);
			this.setMms_text(meia_path_.split(FILE_SEPARATOR_REGEX)[meia_path_.split(FILE_SEPARATOR_REGEX).length-1].split(UNDERSCORE)[1]);
			
			if(this.mms_text.toLowerCase().endsWith(JPG))
				this.mms_text = this.mms_text.split(JPG)[0];
			if(this.mms_text.toLowerCase().endsWith(PNG))
				this.mms_text = this.mms_text.split(PNG)[0];
			if(this.mms_text.toLowerCase().endsWith(GIF))
				this.mms_text = this.mms_text.split(GIF)[0];
			
			
		}catch(Exception e){
			//throw new NonConformanceToInmobiaMMS_NamingConvention(meia_path_ + "does not confirm to inmobia mms naming convention");
		}*/
	}
	
	public void setMediaPath(String meia_path_) {
		this.media_path = meia_path_;
	}
	
	
	public void setMms_text(String mms_text) {
		this.mms_text = mms_text;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public String toString() {
		return "MMS [id=" + id + ", serviceid=" + super.getServiceid()
				+ ", timeStampOfInsertion=" + timeStampOfInsertion
				+ ", dlrArriveTime=" + dlrArriveTime + ", inprocessingQueue="
				+ inprocessingQueue + ", sent=" + sent + ", dlrReportStatus="
				+ dlrReportStatus + ", transactionID=" + transactionID
				+ ", MM7_VERSION=" + MM7_VERSION + ", VASPID=" + VASPID
				+ ", VASID=" + VASID + ", shortcode=" + shortcode
				+ ", servicecode=" + servicecode + ", linked_id=" + linked_id
				+ ", msisdn=" + msisdn + ", eariest_delivery_time="
				+ eariest_delivery_time + ", expiry_date=" + expiry_date
				+ ", delivery_report_requested=" + delivery_report_requested
				+ ", read_reply=" + read_reply + ", subject=" + subject
				+ ", distribution_indicator=" + distribution_indicator
				+ ", media_path=" + media_path + ", mms_text=" + mms_text
				+ ", password_username=" + password_username + "]";
	}
	public static void main(String[] args) throws SOAPException, IOException, MessagingException {
		
		String endpointURL = "http://203.82.66.118:5777/mms/mm7tomms.sh";
		
		MM7_Submit_req request = new MM7_Submit_req(endpointURL){};
		
		MMS mms = new MMS();
		//add properties of the mms
		//mms.setMms_text("A test msisdn");
		//mms.setContent_path("http://m.inmobia.com/web");
		
		SOAPMessage response  = request.submit(mms);
		
		//get response, act accordingly
		
	}

}