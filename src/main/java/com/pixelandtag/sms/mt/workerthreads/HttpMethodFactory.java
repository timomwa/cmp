package com.pixelandtag.sms.mt.workerthreads;

import javax.ws.rs.HttpMethod;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;

public class HttpMethodFactory {

	public static HttpRequestBase getMethod(
			GenericHTTPParam genericparams) {
		if(genericparams.getHttpmethod().toUpperCase().equals(HttpMethod.POST))
			return  new HttpPost(genericparams.getUrl());
		if(genericparams.getHttpmethod().toUpperCase().equals(HttpMethod.PUT))
			return  new HttpPut(genericparams.getUrl());
		if(genericparams.getHttpmethod().toUpperCase().equals(HttpMethod.GET))
			return  new HttpGet(genericparams.getUrl());
		if(genericparams.getHttpmethod().toUpperCase().equals(HttpMethod.DELETE))
			return new HttpDelete(genericparams.getUrl());
		if(genericparams.getHttpmethod()==null)
			return new HttpPost(genericparams.getUrl());
		
		return null;
	}

}
