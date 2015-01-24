package com.pixelandtag.cmp.ejb;

public class DatingServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7801727394254784750L;
	
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public DatingServiceException(String message, Exception e) {
		super(message,e);
	}

}
