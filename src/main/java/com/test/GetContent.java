package com.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.BasicConfigurator;

import com.pixelandtag.sms.mt.workerthreads.GenericHTTPClient;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPParam;
import com.pixelandtag.sms.mt.workerthreads.GenericHttpResp;

public class GetContent {


	private static GenericHTTPClient httpclient;
	
	public static void main(String[] args) throws Exception {
		
		BasicConfigurator.configure();
		
		httpclient = new GenericHTTPClient("http");
		
		GenericHTTPParam param = new GenericHTTPParam();
		param.setUrl("http://212.22.169.19:8081/share/page/document-details");
		
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("nodeRef", "workspace://SpacesStore/8c506959-5cd9-49e5-8edc-4d14a26cb0d1"));
		
		param.setHttpParams(qparams);
		param.setHttpmethod(HttpMethod.GET);
		
		Map<String,String> headerParams = new HashMap<String,String>();
		headerParams.put("X-Alfresco-Remote-User", "admin");
		headerParams.put("Authorization", "Basic YWxmcmVzY28tc3lzdGVtOmFsZnJlc2Nv");
		param.setHeaderParams(headerParams);
		
		final GenericHttpResp resp = httpclient.call(param);
		final int RESP_CODE = resp.getResp_code();
		
		String message = resp.getBody();
		
		System.out.println("code-> RESP_CODE-> "+RESP_CODE+",  resp:: "+message); 
		
	}
}
