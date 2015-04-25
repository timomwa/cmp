package com.pixelandtag.cmp.ejb;

public class PersistenceException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1012096149966221239L;
	private Throwable throwable;
	private String message;

	public Throwable getThrowable() {
		return throwable;
	}

	public String getMessage() {
		return message;
	}


	public PersistenceException(String message) {
		this.message = message;
	}
	
	
	public PersistenceException(String message, Exception e) {
		this.message = message;
		this.throwable = e;
	}
}
