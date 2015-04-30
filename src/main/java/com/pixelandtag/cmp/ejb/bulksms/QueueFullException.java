package com.pixelandtag.cmp.ejb.bulksms;

public class QueueFullException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1318119316060728787L;
	
	private Throwable throwable;
	private String message;

	public Throwable getThrowable() {
		return throwable;
	}

	public String getMessage() {
		return message;
	}


	public QueueFullException(String message) {
		this.message = message;
	}
	
	
	public QueueFullException(String message, Exception e) {
		this.message = message;
		this.throwable = e;
	}
}
