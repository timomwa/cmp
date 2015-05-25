package com.pixelandtag.dating.entities;

import java.util.HashMap;

public enum AlterationMethod {
	
	customer_care_interface("cci"),
	self_via_ussd("self_ussd"),
	self_via_sms("self_sms"),
	self_via_ivr("self_ivr"),
	self_via_phone_app("self_app"),
	self_via_selfcare("self_selfcare"),
	system_autorenewal("system_autorenewal"),
	backend("backend");
	
	private String code;
	private static final HashMap<String, AlterationMethod> lookup = new HashMap<String, AlterationMethod>();
	
	AlterationMethod(String code_){
		this.code = code_;
	}
	
	static {
		for (AlterationMethod status : AlterationMethod.values()){
			lookup.put(status.getCode(), status);
		}
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}
