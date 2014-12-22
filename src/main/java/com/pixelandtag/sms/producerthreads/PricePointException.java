package com.pixelandtag.sms.producerthreads;

public class PricePointException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 903776321114472496L;
	private String message;

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
	
	
	

}
