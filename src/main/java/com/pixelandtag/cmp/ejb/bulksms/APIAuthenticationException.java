package com.pixelandtag.cmp.ejb.bulksms;


public class APIAuthenticationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 34523523523520411L;
	
	private Throwable throwable;
	private String message;

	public Throwable getThrowable() {
		return throwable;
	}

	public String getMessage() {
		return message;
	}


	public APIAuthenticationException(String message) {
		this.message = message;
	}
	
	
	public APIAuthenticationException(String message, Exception e) {
		this.message = message;
		this.throwable = e;
	}
}
