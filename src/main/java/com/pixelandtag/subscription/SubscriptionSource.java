package com.pixelandtag.subscription;


public enum SubscriptionSource {
	
	SMS,WAP;
	
	//private String type;
	
	/*
	@Override
	public String toString(){
		return type;
	}
	*/
	/*public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}*/

	/*private SubscriptionSource(String type){
		if(get(type)!=null)
			this.type = type;
	}*/
	
	public static SubscriptionSource get(String val){
		
		SubscriptionSource type = null;
		
		if(val==null || val.isEmpty() || val.equals(""))
			return null;
		
		val = val.trim().toUpperCase();
		
		if(val.equals("SMS"))
			type = SubscriptionSource.SMS;
		if(val.equals("WAP"))
			type = SubscriptionSource.WAP;
		
		return type;
	}

}
