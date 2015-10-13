package com.pixelandtag.cmp.ejb.api.sms;

public class OpcoRuleException extends Exception {

	private static final long serialVersionUID = -7611688055803134947L;
	
	private String message;
	private Throwable throwable;
	public OpcoRuleException(String message){
		this.message = message;
	}
	public OpcoRuleException(String message,Throwable throwable){
		this.message = message;
		this.throwable = throwable;
	}
	public String getMessage() {
		return message;
	}
	public Throwable getThrowable() {
		return throwable;
	}

}
