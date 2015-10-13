package com.pixelandtag.subscription.dto;

import java.util.HashMap;


public enum SubscriptionStatus {
	
	temporarily_suspended("temporarily_suspended"),
	waiting_confirmation("waiting_confirmation"),
	confirmed("confirmed"),
	unsubscribed("unsubscribed");
	
	private final String status;
	
	private SubscriptionStatus(String status){
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}
	
	
	private static final HashMap<String, SubscriptionStatus> lookup = new HashMap<String, SubscriptionStatus>();
	
	static {
		for (SubscriptionStatus status : SubscriptionStatus.values()){
			lookup.put(status.getStatus(), status);
		}
	}
	
	public static SubscriptionStatus get(String status) {
		return lookup.get(status);
	}
}
