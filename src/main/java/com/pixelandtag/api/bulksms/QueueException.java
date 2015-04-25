package com.pixelandtag.api.bulksms;

public class QueueException extends Exception {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -7333279503539874227L;
	private String message;
	private Throwable throwable;
	public QueueException(String message){
		this.message = message;
	}
	public QueueException(String message,Throwable t){
		this.message = message;
		this.throwable = t;
	}
	public String getMessage() {
		return message;
	}
	public Throwable getThrowable() {
		return throwable;
	}
	
	

}
