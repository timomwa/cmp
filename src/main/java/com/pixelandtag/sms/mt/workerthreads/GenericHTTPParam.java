package com.pixelandtag.sms.mt.workerthreads;

import java.io.Serializable;
import java.util.List;

import org.apache.http.NameValuePair;

import com.pixelandtag.entities.MTsms;
/**
 * 
 * @author Timothy
 * Holds generic http params.
 * TODO add header.
 *
 */
public class GenericHTTPParam implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8834391423692237679L;
	private String url;
	private Long id;
	private MTsms mtsms;
	
	private List<NameValuePair> httpParams;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public List<NameValuePair> getHttpParams() {
		return httpParams;
	}
	public void setHttpParams(List<NameValuePair> httpParams) {
		this.httpParams = httpParams;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public MTsms getMtsms() {
		return mtsms;
	}
	public void setMtsms(MTsms mtsms) {
		this.mtsms = mtsms;
	}
	
	
	

}
