package com.pixelandtag.subscription.dto;

import java.util.HashMap;


public enum MediumType {
	
	ussd("ussd"),sms("sms"),wap("wap");
	
	private final String type;
	
	private MediumType(String type){
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	
	private static final HashMap<String, MediumType> lookup = new HashMap<String, MediumType>();
	
	static {
		for (MediumType status : MediumType.values()){
			lookup.put(status.getType(), status);
		}
	}
	
	public static MediumType get(String status) {
		return lookup.get(status);
	}
	
	
}
