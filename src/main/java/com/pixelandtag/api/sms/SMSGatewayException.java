package com.pixelandtag.api.sms;

public class SMSGatewayException extends Exception {
	
	private static final long serialVersionUID = 4983291290067919659L;
	private Throwable throwable;
	private String message;

	public Throwable getThrowable() {
		return throwable;
	}


	public String getMessage() {
		return message;
	}


	public SMSGatewayException(String message) {
		this.message = message;
	}
	
	
	public SMSGatewayException(String message, Throwable e) {
		this.message = message;
		this.throwable = e;
	}

}
