package com.pixelandtag.sms.mt;
/**
 * 
 * @author Timothy Mwangi Gikonyo
 * date created 9th Feb 2012.
 * 
 * Ring Tones - RT
Operator Logos - OL
Icons - IC
Text Cards  - TC
Text Messages - TM
TM will also be used when delivering error, info or menu
messages.
VCARD - for sending VCARD or Business card
VCALENDAR -- for sending VCALENDAR
VBOOKMARK -- for sending VBOOKMARK
IMELODY - for sending IMELODY
SERVICEINDICATION -- for sending SERVICEINDICATION
 *
 */
public enum CONTENTTYPE {
	
	RT,OL,IC,TC,TM,VCARD,VCALENDAR,VBOOKMARK,IMELODY,SERVICEINDICATION;
	
	
	public static CONTENTTYPE get(String val){
		
		CONTENTTYPE type = null;
		
		if(val==null || val.isEmpty() || val.equals(""))
			return null;
		
		val = val.trim().toUpperCase();
		
		if(val.equals("SERVICEINDICATION"))
			type = CONTENTTYPE.SERVICEINDICATION;
		if(val.equals("IMELODY"))
			type = CONTENTTYPE.IMELODY;
		if(val.equals("VBOOKMARK"))
			type = CONTENTTYPE.VBOOKMARK;
		if(val.equals("VCALENDAR"))
			type = CONTENTTYPE.VCALENDAR;
		if(val.equals("VCARD"))
			type = CONTENTTYPE.VCARD;
		if(val.equals("TM"))
			type = CONTENTTYPE.TM;
		if(val.equals("RT"))
			type = CONTENTTYPE.RT;
		if(val.equals("OL"))
			type = CONTENTTYPE.OL;
		if(val.equals("TC"))
			type = CONTENTTYPE.TC;
		
		return type;
	}
	
	
	

}
