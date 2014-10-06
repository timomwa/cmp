package com.inmobia.axiata.web.beans;

public enum Language {
	
	MALAY,ENGLISH,INDONESIA,DEFAULT;
	
	
	public static Language get(String langst){
		
		Language lang = null;//Language.ENGLISH;
		
		if(langst!=null)
			if(langst.isEmpty())
				return lang;
		
		langst = langst.trim().toUpperCase();
		
		if(langst.equals("MAL") || langst.equals("MALAY") || langst.equals("MALY") || langst.equals("2") || langst.equals("BM"))
			lang = Language.MALAY;
		if(langst.equals("ENG") || langst.equals("ENGLISH") || langst.equals("EN")|| langst.equals("1"))
			lang = Language.ENGLISH;
		
		if(langst.equals("IND") || langst.equals("INDONESIA") || langst.equals("INDO") || langst.equals("3"))
			lang = Language.INDONESIA;
		
		return lang;
		
		
	}
}
