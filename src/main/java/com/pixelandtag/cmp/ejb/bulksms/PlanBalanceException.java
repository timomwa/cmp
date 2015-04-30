package com.pixelandtag.cmp.ejb.bulksms;

public class PlanBalanceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6128686448002113388L;
	private Throwable throwable;
	private String message;

	public Throwable getThrowable() {
		return throwable;
	}

	public String getMessage() {
		return message;
	}


	public PlanBalanceException(String message) {
		this.message = message;
	}
	
	
	public PlanBalanceException(String message, Exception e) {
		this.message = message;
		this.throwable = e;
	}
}
