package com.whmlclient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.BasicConfigurator;

import com.pixelandtag.sms.mt.workerthreads.GenericHTTPClient;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPParam;
import com.pixelandtag.sms.mt.workerthreads.GenericHttpResp;

public class WhmlClient {
	
	public static void main(String[] args) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, InstantiationException, IllegalAccessException, ClassNotFoundException, UnsupportedEncodingException {
		
		BasicConfigurator.configure();
		String whmusername = "36";
		String whmpassword = "111111";
		String url = "https://185.27.132.34:2087/json-api/cpanel?cpanel_jsonapi_user=pixelan1&cpanel_jsonapi_apiversion=2&cpanel_jsonapi_module=Branding&cpanel_jsonapi_func=showpkgs&onlyshowyours=1&skipglobal=0&showroot=0&skiphidden=0";// "https://185.27.132.34:2087/json-api/accountsummary";//?api.version=2";
		
		//url = URLEncoder.encode(url,"UTF-8");
		System.out.println(url);
		GenericHTTPClient httpClient = new GenericHTTPClient("https");
		
		
		GenericHTTPParam httpParams = new GenericHTTPParam();
		httpParams.setUrl(url);
		Map<String,String> headerParams = new HashMap<String,String>();
		String encoding = null;
		sun.misc.BASE64Encoder encoder = (sun.misc.BASE64Encoder) Class.forName( "sun.misc.BASE64Encoder" ).newInstance(); 
		encoding = encoder.encode( (whmusername+":"+whmpassword).getBytes() ); 
		
		System.out.println("Encoding: "+encoding);
		headerParams.put("Authorization", "Basic "+encoding);
		httpParams.setHeaderParams(headerParams);
		List<NameValuePair> httpParams_ = new ArrayList<NameValuePair>();
		httpParams_.add(new BasicNameValuePair("cpanel_jsonapi_apiversion", "2"));
		httpParams.setHttpParams(httpParams_);
		GenericHttpResp resp = httpClient.call(httpParams);
		
		System.out.println(resp.getBody());
		
		httpClient.finalizeMe();
		
		
		
	}

}
