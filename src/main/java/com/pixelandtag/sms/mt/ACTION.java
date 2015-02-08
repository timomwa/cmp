package com.pixelandtag.sms.mt;
/**
 * 
 * @author Timothy Mwangi Gikonyo
 *
 *Non-SMS Registration possible action values under
APIType 6:
IOD - Non-SMS IOD Registration
IODM - Non-SMS IOD Registration in Bahasa Melayu
REG - Non-SMS SubscriptionOld Registration
REGM - Non-SMS SubscriptionOld Registration in Bahasa
 *FriendFinder possible action values under APIType 7:
ADD - Add a new friend to user's FriendFinder list
FIND - Find an existing friend
SEND - Send user's location to a friend
 */
public enum ACTION {
	
	IODM,IOD,REG,REGM,FriendFinder,ADD,FIND,SEND;
	
	public static ACTION get(String val){
		
		ACTION action = null;
		
		if(val==null || val.isEmpty() || val.equals(""))
			return null;
		
		val = val.trim();
		
		if(val.equals("IODM"))
			action = ACTION.IODM;
		if(val.equals("IOD"))
			action = ACTION.IOD;
		if(val.equals("REG"))
			action = ACTION.REG;
		if(val.equals("REGM"))
			action = ACTION.REGM;
		if(val.equals("FriendFinder"))
			action = ACTION.FriendFinder;
		if(val.equals("ADD"))
			action = ACTION.ADD;
		if(val.equals("FIND"))
			action = ACTION.FIND;
		if(val.equals("SEND"))
			action = ACTION.SEND;
		
		return action;
	}

}
