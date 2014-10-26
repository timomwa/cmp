package com.pixelandtag.entities;

import com.pixelandtag.api.GenericMessage;

public class GenericMT extends GenericMessage{
	
	protected long id = -1;
	protected String sms = "",id_str =  "";
	protected String msisdn = "";
	
	
	
	public void setPrice(double price) {
		super.setPrice(price);
	}
	public void setServiceid(int serviceid) {
		super.setServiceid(serviceid);
	}
	public double getPrice() {
		return super.getPrice();
	}
	public int getServiceid() {
		return super.getServiceid();
	}
	public long getId() {
		return id;
	}
	public String getIdStr() {
		return id_str;
	}
	public void setId(long id_) {
		this.id = id_;
		this.id_str = String.valueOf(id_);
	}
	public String getSms() {
		return sms;
	}
	public void setSms(String sms) {
		this.sms = sms;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;		
	}
	
	@Override
	public String toString() {
		return "GenericMT [id=" + id + ", sms=" + sms + ", msisdn=" + msisdn
				+ "] "+super.toString();
	}
	
}
