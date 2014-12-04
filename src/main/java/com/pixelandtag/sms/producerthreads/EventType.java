package com.pixelandtag.sms.producerthreads;

public enum EventType {

	CONTENT_PURCHASE("Content Purchase"), SUBSCRIPTION_PURCHASE("Subscription Purchase"), 
	RESUBSCRIPTION("ReSubscription");	
	private String name;
	
	EventType(String name_){
		this.name = name_;
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public static EventType get(String string) {
		if(string==null)
			return null;
		if(string.trim().equals(""))
			return null;
		
		if(string.equalsIgnoreCase(CONTENT_PURCHASE.toString()))
			return CONTENT_PURCHASE;
		if(string.equalsIgnoreCase(SUBSCRIPTION_PURCHASE.toString()))
			return SUBSCRIPTION_PURCHASE;
		if(string.equalsIgnoreCase(RESUBSCRIPTION.toString()))
			return RESUBSCRIPTION;
		return null;
	}
	
	
}
