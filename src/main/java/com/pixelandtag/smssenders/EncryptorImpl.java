package com.pixelandtag.smssenders;

import java.nio.charset.Charset;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;

@SuppressWarnings("restriction")
public class EncryptorImpl implements Encryptor {
	
	private sun.misc.BASE64Encoder encoder = null;
	private MessageDigest md5digestor = MessageDigest.getInstance("MD5");
	
	public EncryptorImpl() throws Exception{
		encoder =  (sun.misc.BASE64Encoder) Class.forName( "sun.misc.BASE64Encoder" ).newInstance(); 
	}
	

	@Override
	public String encode(String username, String password, String method) throws Exception {
		
		if(method.equalsIgnoreCase(Encryptor.BASIC_BASE64)){
			
			return encoder.encode( (username+":"+password).getBytes() ); 
			
		}else if(method.equalsIgnoreCase(Encryptor.BASIC_MD5)){
			md5digestor.reset();
			md5digestor.update(password.getBytes(Charset.forName("UTF8")));
			return new String(Hex.encodeHex(md5digestor.digest()));
		}
		return null;
	}
	
	@Override
	public String encode(String encodable, String method) throws Exception {
		
		if(method.equalsIgnoreCase(Encryptor.BASIC_BASE64)){
			
			return encoder.encode( encodable.getBytes() ); 
			
		}else if(method.equalsIgnoreCase(Encryptor.BASIC_MD5)){
			md5digestor.reset();
			md5digestor.update(encodable.getBytes(Charset.forName("UTF8")));
			return new String(Hex.encodeHex(md5digestor.digest()));
		}
		return null;
	}

}
