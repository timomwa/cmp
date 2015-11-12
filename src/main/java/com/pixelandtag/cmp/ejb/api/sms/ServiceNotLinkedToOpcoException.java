package com.pixelandtag.cmp.ejb.api.sms;

public class ServiceNotLinkedToOpcoException extends Exception {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4365235176724767955L;

	private String message;
	
	private Throwable throwable;
	
	public ServiceNotLinkedToOpcoException(String message, Throwable t){
		super(message, t);
		setMessage(message);
		setThrowable(t);
	}
	public ServiceNotLinkedToOpcoException(){
	}
	
	public ServiceNotLinkedToOpcoException(String msg){
		this.message = msg;
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
