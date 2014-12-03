package com.pixelandtag.sms.producerthreads;

public enum EventType {

	CONTENT_PURCHASE("Content Purchase"), SUBSCRIPTION_PURCHASE("Subscription Purchase"), 
	RESUBSCRIPTION("ReSubscription");	
	private String name;
	
	EventType(String name_){
		this.name = name_;
	}

	@Override
	public String toString() {
		return this.name;
	}
	
	
}
