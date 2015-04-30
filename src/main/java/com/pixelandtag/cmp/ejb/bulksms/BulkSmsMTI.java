package com.pixelandtag.cmp.ejb.bulksms;

import org.json.JSONException;

import com.pixelandtag.cmp.exceptions.CMPSequenceException;

public interface BulkSmsMTI {
	
	/**
	 *<p> enqueues an MT</p>
	 *
	 * @param sourceIp - 
	 * @param apiKey
	 * @param username
	 * @param password
	 * @param jsonString
	 * @throws APIAuthenticationException
	 * @throws ParameterException
	 * @throws PlanException
	 * @throws PersistenceException
	 * @throws JSONException
	 * @throws QueueFullException
	 * @throws PlanBalanceException
	 * returns a String - transaction id
	 */
	public String enqueue(String sourceIp, String apiKey,String username, String password,String jsonString) throws APIAuthenticationException,ParameterException,PlanException, PersistenceException,JSONException,QueueFullException,PlanBalanceException,CMPSequenceException;

}
