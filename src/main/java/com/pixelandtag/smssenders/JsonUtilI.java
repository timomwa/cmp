package com.pixelandtag.smssenders;

import javax.inject.Named;

import org.json.JSONException;
import org.json.JSONObject;

@Named
public interface JsonUtilI {
	
	public void loadJson(JSONObject json) throws JSONException;
	public Object getValue(String key);
	public void reset();

}
