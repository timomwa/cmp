package com.pixelandtag.sms.mt.workerthreads;

import java.io.Serializable;

import com.pixelandtag.cmp.entities.audittools.LatencyLog;


public class GenericHttpResp implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1543853773676611632L;
	
	private int resp_code = 0;
	
	private String body;
	
	private LatencyLog latencyLog;
	
	private String contenttype;

	public int getResp_code() {
		return resp_code;
	}

	public void setResp_code(int resp_code) {
		this.resp_code = resp_code;
	}

	public LatencyLog getLatencyLog() {
		return latencyLog;
	}

	public void setLatencyLog(LatencyLog latencyLog) {
		this.latencyLog = latencyLog;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getContenttype() {
		return contenttype;
	}

	public void setContenttype(String contenttype) {
		this.contenttype = contenttype;
	}
	
	

}
