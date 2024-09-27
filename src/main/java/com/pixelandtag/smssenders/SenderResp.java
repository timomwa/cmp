package com.pixelandtag.smssenders;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SenderResp implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8260152311604552122L;
	
	private String respcode;
	private String responseMsg;
	private String refvalue;//can be transaction id from operator, or just our internal unique identifier for this message that has been sent out
	private Map<String,String> othervalues = new HashMap<String,String>();
	private Boolean success;//General success determined by the success criteria
	
	
	public String getRespcode() {
		return respcode;
	}
	public void setRespcode(String respcode) {
		this.respcode = respcode;
	}
	public String getResponseMsg() {
		return responseMsg;
	}
	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}
	public String getRefvalue() {
		return refvalue;
	}
	public void setRefvalue(String refvalue) {
		this.refvalue = refvalue;
	}
	public Map<String, String> getOthervalues() {
		return othervalues;
	}
	public void setOthervalues(Map<String, String> othervalues) {
		this.othervalues = othervalues;
	}
	public Boolean getSuccess() {
		return success;
	}
	public void setSuccess(Boolean success) {
		this.success = success;
	}
	@Override
	public String toString() {
		return "SenderResp [respcode=" + respcode + ", responseMsg="
				+ responseMsg + ", refvalue=" + refvalue + ", othervalues="
				+ othervalues + ", success=" + success + "]";
	}
	
	

}
