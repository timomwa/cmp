package com.pixelandtag.cmp.ejb.bulksms;

import org.json.JSONException;


public interface BulkQueryI {
	
	/**
	 * 
	 * @param ipAddress
	 * @param apiKey
	 * @param username
	 * @param password
	 * @param jsonString
	 * @return
	 * @throws PlanException 
	 * @throws PlanBalanceException 
	 */
	public String query(String ipAddress, String apiKey, String username,String password, String jsonString) throws APIAuthenticationException,ParameterException,JSONException, PlanException, PlanBalanceException;
}
