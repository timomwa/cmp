package com.pixelandtag.action;

public class MetaDataException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = -4018519690202277687L;
	private String message;
	public MetaDataException(String message) {
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
	

}
