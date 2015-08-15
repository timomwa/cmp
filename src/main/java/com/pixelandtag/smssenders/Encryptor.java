package com.pixelandtag.smssenders;

public interface Encryptor {
	
	public static final String BASIC_MD5 = "basicmd5";
	public static final String BASIC_BASE64 = "basicbase64";
	
	public String encrypt(String username, String password, String method) throws Exception;

}
