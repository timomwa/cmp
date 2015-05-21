package com.pixelandtag.sms.producerthreads;

import java.io.Serializable;

public class CleanupDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2080067227596395489L;
	
	private Long count;
	private String msisdn;
	private Long serviceid;
	
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public Long getServiceid() {
		return serviceid;
	}
	public void setServiceid(Long serviceid) {
		this.serviceid = serviceid;
	}
	
	
	
	

}
