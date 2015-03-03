package com.pixelandtag.cmp.ejb;

public class USSDEception extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6902064647586681002L;

	private String message;
	
	private Throwable throwable;
	
	public USSDEception(String message, Throwable t){
		super(message, t);
		setMessage(message);
		setThrowable(t);
	}
	public USSDEception(){
	}
	
	public USSDEception(String msg){
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
