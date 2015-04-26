package com.pixelandtag.cmp.ejb;

import javax.ws.rs.Path;

import org.json.JSONException;

import com.pixelandtag.cmp.ejb.APIAuthenticationException;
import com.pixelandtag.cmp.ejb.ParameterException;
import com.pixelandtag.cmp.ejb.PlanBalanceException;
import com.pixelandtag.cmp.ejb.PlanException;


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
