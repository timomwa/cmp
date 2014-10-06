package com.inmobia.celcom.entities;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class MOSms extends GenericMO{
	
	public static StringBuilder sb = new StringBuilder();

	private static final String U_SCR = "_";

	private String msisdn;
	
	
	
	private String mt_Sent;
	
	private long id;
	
	private String timeStamp;
	
	private String mo_ack;
	
	private String mt_ack;
	
	private String MT_STATUS;
	
	private boolean isSubscriptionPush = false;
	
	
	private Map<String,String> attribz = new HashMap<String,String>();
	
	
	public Map<String, String> getAttribz() {
		return attribz;
	}
	
	public MOSms(HttpServletRequest request) {
		
		super(request);
		
		Enumeration enums = request.getParameterNames();
		
		String paramName = "";
		String value  = "";
		
		while(enums.hasMoreElements()){
			
			paramName = (String) enums.nextElement();
			
			value = request.getParameter(paramName);
			
			System.out.println("celcom : paramName: "+paramName+ " value: "+value);
			
			attribz.put(paramName, request.getParameter(paramName));
			
			
		}
		
		
		
		
		
	}
	
	
	public boolean isSubscriptionPush() {
		return isSubscriptionPush;
	}

	public void setSubscriptionPush(boolean isSubscriptionPush) {
		this.isSubscriptionPush = isSubscriptionPush;
	}

	public String getMT_STATUS() {
		return MT_STATUS;
	}


	public void setMT_STATUS(String mT_STATUS) {
		MT_STATUS = mT_STATUS;
	}


	public String getMo_ack() {
		return mo_ack;
	}


	public void setMo_ack(String mo_ack) {
		this.mo_ack = mo_ack;
	}


	public String getMt_ack() {
		return mt_ack;
	}


	public void setMt_ack(String mt_ack) {
		this.mt_ack = mt_ack;
	}


	public String getTimeStamp() {
		return timeStamp;
	}


	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}


	public String getMt_Sent() {
		return mt_Sent;
	}


	public void setMt_Sent(String mt_Sent) {
		this.mt_Sent = mt_Sent;
	}


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public MOSms(){
		
	}
	

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}


	@Override
	public String toString() {
		return "MOSms [msisdn=" + msisdn + ", mt_Sent=" + mt_Sent + ", id="
				+ id + ", timeStamp=" + timeStamp + ", mo_ack=" + mo_ack
				+ ", mt_ack=" + mt_ack + ", MT_STATUS=" + MT_STATUS
				+ ", attribz=" + attribz + ", getSMS_Message_String()="
				+ getSMS_Message_String() + ", getSMS_SourceAddr()="
				+ getSMS_SourceAddr() + ", getSUB_Mobtel()=" + getSUB_Mobtel()
				+ ", getSMS_DataCodingId()=" + getSMS_DataCodingId()
				+ ", getCMPResponse()=" + getCMPResponse() + ", getAPIType()="
				+ getAPIType() + ", getCMP_AKeyword()=" + getCMP_AKeyword()
				+ ", getCMP_SKeyword()=" + getCMP_SKeyword() + ", toString()="
				+ super.toString() + ", getPrice()=" + getPrice()
				+ ", getProcessor_id()=" + getProcessor_id()
				+ ", getServiceid()=" + getServiceid() + ", isSplit_msg()="
				+ isSplit_msg() + ", getNumber_of_sms()=" + getNumber_of_sms()
				+ ", getPriority()=" + getPriority() + ", getCMP_Txid()="
				+ getCMP_Txid() + ", getClass()=" + getClass()
				+ ", hashCode()=" + hashCode() + "]";
	}
	

	public static MOSms clone(MOSms mo) {
		MOSms moC = new MOSms();
		moC.setAPIType(mo.getAPIType());
		moC.setCMP_AKeyword(mo.getCMP_AKeyword());
		moC.setCMP_Txid(mo.getCMP_Txid());
		moC.setCMPResponse(mo.getCMPResponse());
		moC.setId(mo.getId());
		moC.setMo_ack(mo.getMo_ack());
		moC.setMsisdn(mo.getMsisdn());
		moC.setMt_ack(mo.getMt_ack());
		moC.setMt_Sent(mo.getMt_Sent());
		moC.setMT_STATUS(mo.getMT_STATUS());
		moC.setNumber_of_sms(mo.getNumber_of_sms());
		moC.setPriority(mo.getPriority());
		moC.setSMS_DataCodingId(mo.getSMS_DataCodingId());
		moC.setSMS_Message_String(mo.getSMS_Message_String());
		moC.setSMS_SourceAddr(mo.getSMS_SourceAddr());
		moC.setSplit_msg(mo.isSplit_msg());
		moC.setSUB_Mobtel(mo.getSUB_Mobtel());
		moC.setTimeStamp(mo.getTimeStamp());
		moC.setServiceid(mo.getServiceid());
		
		return moC;
	}

	
	public MOSms clone() {
		MOSms moC = new MOSms();
		moC.setAPIType(getAPIType());
		moC.setCMP_AKeyword(getCMP_AKeyword());
		moC.setCMP_Txid(getCMP_Txid());
		moC.setCMPResponse(getCMPResponse());
		moC.setId(getId());
		moC.setMo_ack(getMo_ack());
		moC.setMsisdn(getMsisdn());
		moC.setMt_ack(getMt_ack());
		moC.setMt_Sent(getMt_Sent());
		moC.setMT_STATUS(getMT_STATUS());
		moC.setNumber_of_sms(getNumber_of_sms());
		moC.setPriority(getPriority());
		moC.setSMS_DataCodingId(getSMS_DataCodingId());
		moC.setSMS_Message_String(getSMS_Message_String());
		moC.setSMS_SourceAddr(getSMS_SourceAddr());
		moC.setSplit_msg(isSplit_msg());
		moC.setSUB_Mobtel(getSUB_Mobtel());
		moC.setTimeStamp(getTimeStamp());
		moC.setServiceid(getServiceid());
		
		return moC;
	}


	public String getServKey() {
		sb.setLength(0);
		return MOSms.sb.append(getCMP_AKeyword()).append(U_SCR).append(getCMP_SKeyword()).append(U_SCR).append(getSMS_SourceAddr()).toString();
	}
	

	
	
	
	
	
	
	
	
	
	
	

}
