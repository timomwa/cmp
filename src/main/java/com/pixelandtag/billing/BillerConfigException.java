package com.pixelandtag.billing;

public class BillerConfigException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3319501146244457112L;
	
	private Throwable throwable;
	private String message;

	public Throwable getThrowable() {
		return throwable;
	}


	public String getMessage() {
		return message;
	}


	public BillerConfigException(String message) {
		this.message = message;
	}
	
	
	public BillerConfigException(String message, Throwable e) {
		this.message = message;
		this.throwable = e;
	}

}
