package com.pixelandtag.cmp.ejb.bulksms;

public class  ParameterException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2524165644272236465L;
	private Throwable throwable;
	private String message;

	public Throwable getThrowable() {
		return throwable;
	}

	public String getMessage() {
		return message;
	}


	public ParameterException(String message) {
		this.message = message;
	}
	
	
	public ParameterException(String message, Exception e) {
		this.message = message;
		this.throwable = e;
	}

}
