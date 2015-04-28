package com.pixelandtag.cmp.entities;


public enum ProcessorType {
	CONTENT_PROXY,LOCAL,MT_ROUTER,PHANTOM;
	public static ProcessorType fromString(String val){
		if(val==null || val.equals(""))
			return null;
		val = val.trim();
		if(val.equalsIgnoreCase("MT_ROUTER"))
			return MT_ROUTER;
		if(val.equalsIgnoreCase("LOCAL"))
			return LOCAL;
		if(val.equalsIgnoreCase("CONTENT_PROXY"))
			return CONTENT_PROXY;
		if(val.equalsIgnoreCase("PHANTOM"))
			return PHANTOM;
		return null;
	}

}
