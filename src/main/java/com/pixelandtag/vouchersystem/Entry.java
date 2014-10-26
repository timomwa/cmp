package com.pixelandtag.vouchersystem;
public class Entry {

	private int id;
	private String msisdn;
	//used as the voucher number
	private String cmp_txid_fk;
	private String timeStampAwarded;
	public int getId() {
		return id;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public String getCmp_txid_fk() {
		return cmp_txid_fk;
	}
	public String getTimeStampAwarded() {
		return timeStampAwarded;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public void setCmp_txid_fk(String cmp_txid_fk) {
		this.cmp_txid_fk = cmp_txid_fk;
	}
	public void setTimeStampAwarded(String timeStampAwarded) {
		this.timeStampAwarded = timeStampAwarded;
	}
	@Override
	public String toString() {
		return "Entry [id=" + id + ", msisdn=" + msisdn + ", cmp_txid_fk="
				+ cmp_txid_fk + ", timeStampAwarded=" + timeStampAwarded + "]";
	}
	
	
	
	
	
}
