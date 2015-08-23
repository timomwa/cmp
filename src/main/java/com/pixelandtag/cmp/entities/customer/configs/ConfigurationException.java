package com.pixelandtag.cmp.entities.customer.configs;

public class ConfigurationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2010059243061850053L;
	
	private Throwable throwable;
	private String message;

	public Throwable getThrowable() {
		return throwable;
	}


	public String getMessage() {
		return message;
	}


	public ConfigurationException(String message) {
		this.message = message;
	}
	
	
	public ConfigurationException(String message, Throwable e) {
		this.message = message;
		this.throwable = e;
	}

}
