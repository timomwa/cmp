package com.pixelandtag.sms.producerthreads;

public class NoServiceProcessorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4256794042387608705L;
	public String message;
	
	public String getMessage() {
		return message;
	}

	private void setMessage(String message) {
		this.message = message;
	}

	public NoServiceProcessorException(String message){
		setMessage(message);
	}
}
