package com.inmobia.axiata.web.beans;

import com.inmobia.celcom.api.ERROR;

public class CelcomMessageLog {
	
	private String id,newCmpTxId,cMP_Txid,mO_received,mT_Sent,mT_Sendtime,sMS_SourceAddr,sUB_Mobtel,sMS_DataCodingId,cMP_Response,cMP_Keyword,CMP_SKeyword,timeStamp,mo_ack,mt_ack,delivery_report_arrive_time;
	private boolean processing,msg_was_split,re_try;
	private int apiType,serviceid,number_of_sms,re_try_count;
	private ERROR mT_Status;
	private double price;
	
	public String getNewCmpTxId() {
		return newCmpTxId;
	}
	public void setNewCmpTxId(String newCmpTxId) {
		this.newCmpTxId = newCmpTxId;
	}
	public String getId() {
		return id;
	}
	public String getcMP_Txid() {
		return cMP_Txid;
	}
	public String getmO_received() {
		return mO_received;
	}
	public String getmT_Sent() {
		return mT_Sent;
	}
	public String getmT_Sendtime() {
		return mT_Sendtime;
	}
	public String getsMS_SourceAddr() {
		return sMS_SourceAddr;
	}
	public String getsUB_Mobtel() {
		return sUB_Mobtel;
	}
	public String getsMS_DataCodingId() {
		return sMS_DataCodingId;
	}
	public String getcMP_Response() {
		return cMP_Response;
	}
	public String getcMP_Keyword() {
		return cMP_Keyword;
	}
	public String getCMP_SKeyword() {
		return CMP_SKeyword;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public String getMo_ack() {
		return mo_ack;
	}
	public String getMt_ack() {
		return mt_ack;
	}
	public String getDelivery_report_arrive_time() {
		return delivery_report_arrive_time;
	}
	public boolean isProcessing() {
		return processing;
	}
	public boolean isMsg_was_split() {
		return msg_was_split;
	}
	public boolean isRe_try() {
		return re_try;
	}
	public int getApiType() {
		return apiType;
	}
	public int getServiceid() {
		return serviceid;
	}
	public int getNumber_of_sms() {
		return number_of_sms;
	}
	public int getRe_try_count() {
		return re_try_count;
	}
	public ERROR getmT_Status() {
		return mT_Status;
	}
	public double getPrice() {
		return price;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setcMP_Txid(String cMP_Txid) {
		this.cMP_Txid = cMP_Txid;
	}
	public void setmO_received(String mO_received) {
		this.mO_received = mO_received;
	}
	public void setmT_Sent(String mT_Sent) {
		this.mT_Sent = mT_Sent;
	}
	public void setmT_Sendtime(String mT_Sendtime) {
		this.mT_Sendtime = mT_Sendtime;
	}
	public void setsMS_SourceAddr(String sMS_SourceAddr) {
		this.sMS_SourceAddr = sMS_SourceAddr;
	}
	public void setsUB_Mobtel(String sUB_Mobtel) {
		this.sUB_Mobtel = sUB_Mobtel;
	}
	public void setsMS_DataCodingId(String sMS_DataCodingId) {
		this.sMS_DataCodingId = sMS_DataCodingId;
	}
	public void setcMP_Response(String cMP_Response) {
		this.cMP_Response = cMP_Response;
	}
	public void setcMP_Keyword(String cMP_Keyword) {
		this.cMP_Keyword = cMP_Keyword;
	}
	public void setCMP_SKeyword(String cMP_SKeyword) {
		CMP_SKeyword = cMP_SKeyword;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public void setMo_ack(String mo_ack) {
		this.mo_ack = mo_ack;
	}
	public void setMt_ack(String mt_ack) {
		this.mt_ack = mt_ack;
	}
	public void setDelivery_report_arrive_time(String delivery_report_arrive_time) {
		this.delivery_report_arrive_time = delivery_report_arrive_time;
	}
	public void setProcessing(boolean processing) {
		this.processing = processing;
	}
	public void setMsg_was_split(boolean msg_was_split) {
		this.msg_was_split = msg_was_split;
	}
	public void setRe_try(boolean re_try) {
		this.re_try = re_try;
	}
	public void setApiType(int apiType) {
		this.apiType = apiType;
	}
	public void setServiceid(int serviceid) {
		this.serviceid = serviceid;
	}
	public void setNumber_of_sms(int number_of_sms) {
		this.number_of_sms = number_of_sms;
	}
	public void setRe_try_count(int re_try_count) {
		this.re_try_count = re_try_count;
	}
	public void setmT_Status(ERROR mT_Status) {
		this.mT_Status = mT_Status;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	@Override
	public String toString() {
		return "CelcomMessageLog [id=" + id + ", cMP_Txid=" + cMP_Txid
				+ ", mO_received=" + mO_received + ", mT_Sent=" + mT_Sent
				+ ", mT_Sendtime=" + mT_Sendtime + ", sMS_SourceAddr="
				+ sMS_SourceAddr + ", sUB_Mobtel=" + sUB_Mobtel
				+ ", sMS_DataCodingId=" + sMS_DataCodingId + ", cMP_Response="
				+ cMP_Response + ", cMP_Keyword=" + cMP_Keyword
				+ ", CMP_SKeyword=" + CMP_SKeyword + ", timeStamp=" + timeStamp
				+ ", mo_ack=" + mo_ack + ", mt_ack=" + mt_ack
				+ ", delivery_report_arrive_time="
				+ delivery_report_arrive_time + ", processing=" + processing
				+ ", msg_was_split=" + msg_was_split + ", re_try=" + re_try
				+ ", apiType=" + apiType + ", serviceid=" + serviceid
				+ ", number_of_sms=" + number_of_sms + ", re_try_count="
				+ re_try_count + ", mT_Status=" + mT_Status + ", price="
				+ price + "]";
	}
	
	

}
