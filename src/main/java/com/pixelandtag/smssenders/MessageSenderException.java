package com.pixelandtag.smssenders;

public class  MessageSenderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1197699562785430083L;
	
	private Throwable throwable;
	private String message;

	public Throwable getThrowable() {
		return throwable;
	}


	public String getMessage() {
		return message;
	}


	public MessageSenderException(String message) {
		this.message = message;
	}
	
	
	public MessageSenderException(String message, Throwable e) {
		this.message = message;
		this.throwable = e;
	}

}
