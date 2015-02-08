package com.pixelandtag.dating.entities;

public enum SubscriptionEvent {
	
	subscrition(0L),renewal(1L),unsubscrition(2L);
	private Long code;
	
	SubscriptionEvent(Long code){
		this.code = code;
	}
	public Long getCode() {
		return code;
	}
	public void setCode(Long code) {
		this.code = code;
	}

	
}
