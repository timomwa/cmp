package com.pixelandtag.cmp.ejb.api.ussd;

public class OrangeUSSDException extends Exception {
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -2162442810488245736L;

	public OrangeUSSDException() {
		super();
	}

	public OrangeUSSDException(String message, Throwable cause) {
		super(message, cause);
	}

	public OrangeUSSDException(String message) {
		super(message);
	}

	public OrangeUSSDException(Throwable cause) {
		super(cause);
	}

	@Override
	public String getMessage() {
		return super.getMessage();
	}

	@Override
	public Throwable getCause() {
		return super.getCause();
	}

	
}
