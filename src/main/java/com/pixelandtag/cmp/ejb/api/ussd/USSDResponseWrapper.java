package com.pixelandtag.cmp.ejb.api.ussd;

import java.io.Serializable;

public class USSDResponseWrapper implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4982858458514021233L;

	/**
	 * The loggable message in the db.
	 * This is only for the purposes of
	 * logging traffic.
	 */
	private String loggableMessage;
	
	/**
	 * This is the message to be sent back to 
	 * the requester. Can be XML, JSON etc
	 */
	private String responseMessage;
	
	public String getLoggableMessage() {
		return loggableMessage;
	}
	public void setLoggableMessage(String loggableMessage) {
		this.loggableMessage = loggableMessage;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	

}
