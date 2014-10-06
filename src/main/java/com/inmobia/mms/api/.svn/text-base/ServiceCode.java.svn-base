package com.inmobia.mms.api;

import java.util.HashMap;

public enum ServiceCode {
	
	RM0("MMSCMPMMS0000"),RM1("MMSCMPMMS0100");
	
	private final String code;
	
	private ServiceCode(String code){
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	
	private static final HashMap<String, ServiceCode> lookup = new HashMap<String, ServiceCode>();

	static {
		for (ServiceCode status : ServiceCode.values())
			lookup.put(status.getCode(), status);
	}
	
	public static ServiceCode get(int code) {
		return lookup.get(code);
	}
	
	

}
