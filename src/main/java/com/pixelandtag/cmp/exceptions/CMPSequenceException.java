package com.pixelandtag.cmp.exceptions;

public class CMPSequenceException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3747097870105445224L;
	private String message;
	private Throwable throwable;
	public CMPSequenceException(String message){
		this.message = message;
	}
	public CMPSequenceException(String message,Throwable throwable){
		this.message = message;
		this.throwable = throwable;
	}
	public String getMessage() {
		return message;
	}
	public Throwable getThrowable() {
		return throwable;
	}

}
