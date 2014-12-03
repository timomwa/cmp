package com.pixelandtag.sms.producerthreads;

public enum Operation {
	
	debit,credit;
	
	public Operation get(String op){
		if(op.toString().toLowerCase().equals("debit"))
			return debit;
		if(op.toString().toLowerCase().equals("credit"))
			return credit;
		return null;
	}

}
