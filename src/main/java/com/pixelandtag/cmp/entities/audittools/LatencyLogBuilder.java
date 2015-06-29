package com.pixelandtag.cmp.entities.audittools;

import java.util.Date;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;

public class LatencyLogBuilder {
	
	private static LatencyLog instance = new LatencyLog();
	
	public LatencyLogBuilder timeStamp(Date timeStamp){
		instance.setTimeStamp(timeStamp);
		return this;
	}
	public  LatencyLogBuilder opco(OperatorCountry opco){
		instance.setOpco(opco);
		return this;
	}
	
	public  LatencyLogBuilder link(String link_){
		instance.setLink(link_);
		return this;
	}
	
	public  LatencyLogBuilder latency(Long lantency_){
		instance.setLatency(lantency_);
		return this;
	}
	public LatencyLog build(){
		assert instance.getLink() != null;
		assert instance.getLatency() != null;
		return instance;
	}
	public static LatencyLogBuilder create() {
		return new LatencyLogBuilder();
	}


}
