package com.pixelandtag.mms.api;

import java.util.HashMap;

public enum TarrifCode {
	
	RM0("IOD0000", 0d),RM1("IOD0100", 1d),RM_015("IOD0015", 0.15d),RM_030("IOD0030", 0.30d);
	
	private final String code;
	private final double price;
	
	private TarrifCode(String code, double price){
		this.code = code;
		this.price = price;
	}
	
	public String getCode() {
		return code;
	}
	
	public double getPrice() {
		return price;
	}
	
	
	
	private static final HashMap<String, TarrifCode> lookup = new HashMap<String, TarrifCode>();
	
	static {
		for (TarrifCode status : TarrifCode.values()){
			lookup.put(status.getCode(), status);
		}
	}
	
	public static TarrifCode get(String code) {
		return lookup.get(code);
	}
	
	public static void main(String[] args){
		//System.out.println(price_lookup);
		System.out.println(TarrifCode.get("IOD0100").getPrice());
	}

}
