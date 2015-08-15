package com.pixelandtag.smssenders;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtilImpl implements JsonUtilI, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8555709632121324584L;
	// My stored keys and values from the json object
	
	private Map<String,String> myKeyValues = new HashMap<String,String>();
	// Used for constructing the path to the key in the json object
	private Stack<String> key_path = new Stack<String>();

	// Recursive function that goes through a json object and stores 
	// its key and values in the hashmap 
	public void loadJson(JSONObject json) throws JSONException{
		
		Iterator<?> json_keys = json.keys();

	    while( json_keys.hasNext() ){
	        String json_key = (String)json_keys.next();

	        try{
	            key_path.push(json_key);
	            loadJson(json.getJSONObject(json_key));
	       }catch (JSONException e){
	           // Build the path to the key
	           String key = "";
	           for(String sub_key: key_path){
	               key += sub_key+".";
	           }
	           key = key.substring(0,key.length()-1);

	           key_path.pop();
	           myKeyValues.put(key, json.getString(json_key));
	        }
	    }
	    if(key_path.size() > 0){
	        key_path.pop();
	    }
	}
	
	public Object getValue(String key){
		return myKeyValues.get(key);
	}
	
	public void reset(){
		myKeyValues = new HashMap<String,String>();
		key_path = new Stack<String>();
	}

}
