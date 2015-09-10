package com.pixelandtag.sms.producerthreads;

public class PricePointException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 903776321114472496L;
	private String message;
	private Throwable throwable;

	public PricePointException(Throwable throwable){
		super(throwable);
		this.throwable = throwable;
	}
	public PricePointException(String message, Throwable throwable){
		super(message,throwable);
		this.message = message;
		this.throwable = throwable;
	}
	public PricePointException(String message){
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}
	
	
	

}
