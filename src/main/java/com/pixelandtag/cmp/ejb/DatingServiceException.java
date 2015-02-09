package com.pixelandtag.cmp.ejb;

public class DatingServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7801727394254784750L;
	private Throwable throwable;
	private String message;

	public Throwable getThrowable() {
		return throwable;
	}


	public String getMessage() {
		return message;
	}


	public DatingServiceException(String message) {
		this.message = message;
	}
	
	
	public DatingServiceException(String message, Exception e) {
		this.message = message;
		this.throwable = e;
	}

}
