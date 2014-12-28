package com.pixelandtag.subscription.dto;

import java.util.HashMap;


public enum MediumType {
	
	sms("sms"),wap("wap");
	
	private final String status;
	
	private MediumType(String status){
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}
	
	
	private static final HashMap<String, MediumType> lookup = new HashMap<String, MediumType>();
	
	static {
		for (MediumType status : MediumType.values()){
			lookup.put(status.getStatus(), status);
		}
	}
	
	public static MediumType get(String status) {
		return lookup.get(status);
	}
	
	
}
