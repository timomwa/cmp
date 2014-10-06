package com.inmobia.celcom.entities;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

import com.inmobia.celcom.sms.mt.ACTION;
import com.inmobia.celcom.sms.mt.CONTENTTYPE;

public class MTsms extends GenericMT {
	
	private CONTENTTYPE type;
	private String sendFrom;
	private String timeStamp;
	private String fromAddr  = "";
	private String charged;
	
	//private String CMP_A_Keyword  = "";
	//private String CMP_S_Keyword  = "";
	private String SMS_Msgdata  = "";
	private String SMS_SourceAddr;
	private String SUB_DeviceType;
	private String SUB_R_Mobtel;
	private String SUB_C_Mobtel;
	private String APIType = "1";
	private String CMP_AKeyword;
	private String CMP_SKeyword;
	private ACTION action;
	private String CMPResponse;
	private String SMS_DataCodingId;
	private String MT_STATUS;
	private String msg_part;
	private boolean split;
	private String newCMP_Txid;
	

	public String getNewCMP_Txid() {
		return newCMP_Txid;
	}

	public void setNewCMP_Txid(String newCMP_Txid) {
		this.newCMP_Txid = newCMP_Txid;
	}

	//@Override
	protected MTsms clone() throws CloneNotSupportedException {
		return (MTsms) super.clone();
	}

	public String getMsg_part() {
		return msg_part;
	}

	public void setMsg_part(String msg_part) {
		this.msg_part = msg_part;
	}

	public String getMT_STATUS() {
		return MT_STATUS;
	}

	public void setMT_STATUS(String mT_STATUS) {
		MT_STATUS = mT_STATUS;
	}

	public String getSMS_DataCodingId() {
		return SMS_DataCodingId;
	}
	
	public void setSMS_DataCodingId(String sMS_DataCodingId) {
		SMS_DataCodingId = sMS_DataCodingId;
	}
	public String getCMPResponse() {
		return CMPResponse;
	}
	public void setCMPResponse(String cMPResponse) {
		CMPResponse = cMPResponse;
	}
	public String getCMP_AKeyword() {
		return CMP_AKeyword;
	}
	public void setCMP_AKeyword(String cMP_AKeyword) {
		CMP_AKeyword = cMP_AKeyword;
	}
	public String getCMP_SKeyword() {
		return CMP_SKeyword;
	}
	public void setCMP_SKeyword(String cMP_SKeyword) {
		CMP_SKeyword = cMP_SKeyword;
	}
	public ACTION getAction() {
		return action;
	}
	public void setAction(ACTION action) {
		this.action = action;
	}
	public String getAPIType() {
		return APIType;
	}
	public void setAPIType(String aPIType) {
		APIType = aPIType;
	}
	/*public String getCMP_TxID() {
		return CMP_TxID;
	}
	public void setCMP_TxID(String cMP_TxID) {
		CMP_TxID = cMP_TxID;
	}*/
	public String getSMS_SourceAddr() {
		return SMS_SourceAddr;
	}
	public void setSMS_SourceAddr(String sMS_SourceAddr) {
		SMS_SourceAddr = sMS_SourceAddr;
	}
	public String getSUB_DeviceType() {
		return SUB_DeviceType;
	}
	public void setSUB_DeviceType(String sUB_DeviceType) {
		SUB_DeviceType = sUB_DeviceType;
	}
	public String getSUB_R_Mobtel() {
		return SUB_R_Mobtel;
	}
	public void setSUB_R_Mobtel(String sUB_R_Mobtel) {
		SUB_R_Mobtel = sUB_R_Mobtel;
		super.msisdn = sUB_R_Mobtel;
		SUB_C_Mobtel = sUB_R_Mobtel;
	}
	public String getSUB_C_Mobtel() {
		return SUB_C_Mobtel;
	}
	public void setSUB_C_Mobtel(String sUB_C_Mobtel) {
		SUB_C_Mobtel = sUB_C_Mobtel;
	}

	private boolean sent=false;
	
	
	
	/*public String getCMP_A_Keyword() {
		return CMP_A_Keyword;
	}
	public void setCMP_A_Keyword(String cMP_A_Keyword) {
		CMP_A_Keyword = cMP_A_Keyword;
	}
	public String getCMP_S_Keyword() {
		return CMP_S_Keyword;
	}
	public void setCMP_S_Keyword(String cMP_S_Keyword) {
		CMP_S_Keyword = cMP_S_Keyword;
	}*/
	public String getSMS_Msgdata() {
		return SMS_Msgdata;
	}
	public void setSMS_Msgdata(String sMS_Msgdata) {
		SMS_Msgdata = sMS_Msgdata;
	}
	public boolean isSent() {
		return sent;
	}
	public void setSent(boolean sent) {
		this.sent = sent;
	}
	public CONTENTTYPE getType() {
		return type;
	}
	public void setType(CONTENTTYPE type) {
		this.type = type;
	}
	public String getSendFrom() {
		return sendFrom;
	}
	public void setSendFrom(String sendFrom) {
		this.sendFrom = sendFrom;
	}
	
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getFromAddr() {
		return fromAddr;
	}
	public void setFromAddr(String fromAddr) {
		this.fromAddr = fromAddr;
	}
	public String getCharged() {
		return charged;
	}
	public void setCharged(String charged) {
		this.charged = charged;
	}

	@Override
	public String toString() {
		return "MTsms [type=" + type + ", sendFrom=" + sendFrom
				+ ", timeStamp=" + timeStamp + ", fromAddr=" + fromAddr
				+ ", charged=" + charged + ", SMS_Msgdata=" + SMS_Msgdata
				+ ", SMS_SourceAddr=" + SMS_SourceAddr + ", SUB_DeviceType="
				+ SUB_DeviceType + ", SUB_R_Mobtel=" + SUB_R_Mobtel
				+ ", SUB_C_Mobtel=" + SUB_C_Mobtel + ", APIType=" + APIType
				+ ", CMP_Keyword=" + CMP_AKeyword + ", CMP_SKeyword="
				+ CMP_SKeyword + ", action=" + action + ", CMPResponse="
				+ CMPResponse + ", SMS_DataCodingId=" + SMS_DataCodingId
				+ ", MT_STATUS=" + MT_STATUS + ", msg_part=" + msg_part
				+ ", split=" + split + ", newCMP_Txid=" + newCMP_Txid
				+ ", sent=" + sent + "] "+super.toString();
	}
	
	
	
	
	
	
	

}
