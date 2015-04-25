package com.pixelandtag.cmp.ejb;

public class PlanException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5021627526990735572L;
	
	private Throwable throwable;
	private String message;

	public Throwable getThrowable() {
		return throwable;
	}

	public String getMessage() {
		return message;
	}


	public PlanException(String message) {
		this.message = message;
	}
	
	
	public PlanException(String message, Exception e) {
		this.message = message;
		this.throwable = e;
	}

}
