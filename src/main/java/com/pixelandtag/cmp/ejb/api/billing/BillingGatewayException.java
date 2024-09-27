package com.pixelandtag.cmp.ejb.api.billing;

public class BillingGatewayException extends Exception {
	
	private static final long serialVersionUID = 6258948310339127833L;
	private Throwable throwable;
	private String message;

	public Throwable getThrowable() {
		return throwable;
	}


	public String getMessage() {
		return message;
	}


	public BillingGatewayException(String message) {
		this.message = message;
	}
	
	
	public BillingGatewayException(String message, Throwable e) {
		this.message = message;
		this.throwable = e;
	}

}
