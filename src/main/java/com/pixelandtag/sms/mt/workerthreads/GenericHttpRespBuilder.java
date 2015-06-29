package com.pixelandtag.sms.mt.workerthreads;

import com.pixelandtag.cmp.entities.audittools.LatencyLog;

public class GenericHttpRespBuilder {
	
	private static GenericHttpResp instance = new GenericHttpResp();
	
	public GenericHttpRespBuilder latencyLog(LatencyLog latencyLog_){
		instance.setLatencyLog(latencyLog_);
		return this;
	}
	public  GenericHttpRespBuilder respCode(int respCode_){
		instance.setResp_code(respCode_);
		return this;
	}
	public  GenericHttpRespBuilder body(String body_){
		instance.setBody(body_);
		return this;
	}
	
	public GenericHttpResp build(){
		assert instance.getResp_code() >-1;
		assert instance.getLatencyLog() != null;
		return instance;
	}
	public static GenericHttpRespBuilder create() {
		return new GenericHttpRespBuilder();
	}

}
