package com.pixelandtag.vouchersystem;


public enum Status {
	
	queued,processing,processed;
	
	
  public static Status get(String val){
		
	  	Status status = null;
		
		if(val==null || val.equals("") || val.length()<=0)
			return null;
		
		val = val.trim();
		
		if(val.equals("queued"))
			status = Status.queued;
		if(val.equals("processing"))
			status = Status.processing;
		if(val.equals("processed"))
			status = Status.processed;
		
		return status;
	}

}
