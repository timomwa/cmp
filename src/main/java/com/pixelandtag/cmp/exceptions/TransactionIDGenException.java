package com.pixelandtag.cmp.exceptions;

public class TransactionIDGenException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4412580089089025776L;
	private String message;
	private Throwable throwable;
	public TransactionIDGenException(String message){
		this.message = message;
	}
	public TransactionIDGenException(String message,Throwable throwable){
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
