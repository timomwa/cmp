package com.pixelandtag.serviceprocessors.sms;

public class MMSQuerec {

	private int id,mms_id_fk;
	private String timeQueued,msisdn,timeSent,deliveryStatus;
	private boolean charged,delivered;
	
	public int getId() {
		return id;
	}
	public int getMms_id_fk() {
		return mms_id_fk;
	}
	public String getTimeQueued() {
		return timeQueued;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public String getTimeSent() {
		return timeSent;
	}
	public String getDeliveryStatus() {
		return deliveryStatus;
	}
	public boolean isCharged() {
		return charged;
	}
	public boolean isDelivered() {
		return delivered;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setMms_id_fk(int mms_id_fk) {
		this.mms_id_fk = mms_id_fk;
	}
	public void setTimeQueued(String timeQueued) {
		this.timeQueued = timeQueued;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public void setTimeSent(String timeSent) {
		this.timeSent = timeSent;
	}
	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}
	public void setCharged(boolean charged) {
		this.charged = charged;
	}
	public void setDelivered(boolean delivered) {
		this.delivered = delivered;
	}
	@Override
	public String toString() {
		return "MMSQuerec [id=" + id + ", mms_id_fk=" + mms_id_fk
				+ ", timeQueued=" + timeQueued + ", msisdn=" + msisdn
				+ ", timeSent=" + timeSent + ", deliveryStatus="
				+ deliveryStatus + ", charged=" + charged + ", delivered="
				+ delivered + "]";
	}
	
	
	
	
	
}
