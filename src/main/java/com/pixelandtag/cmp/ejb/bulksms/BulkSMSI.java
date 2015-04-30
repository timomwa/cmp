package com.pixelandtag.cmp.ejb.bulksms;

import org.json.JSONException;

public interface BulkSMSI {
	
	

	/**
	 * @param sourceIp
	 * @param apiKey
	 * @param username
	 * @param password
	 * @param jsonString
	 * @throws APIAuthenticationException
	 * @throws PlanException
	 * @throws PersistenceException
	 * @throws ParameterException
	 * @throws JSONException
	 */
	public void enqueue(String sourceIp, String apiKey, String username, String password, String jsonString) throws APIAuthenticationException,PlanException,PersistenceException,ParameterException,JSONException,PlanBalanceException,QueueFullException;

	

	

	
}
