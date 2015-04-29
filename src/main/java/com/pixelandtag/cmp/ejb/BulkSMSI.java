package com.pixelandtag.cmp.ejb;

import java.math.BigInteger;

import javax.ejb.Local;
import javax.ws.rs.core.HttpHeaders;

import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.api.MTStatus;
import com.pixelandtag.bulksms.BulkSMSAccount;
import com.pixelandtag.bulksms.BulkSMSPlan;
import com.pixelandtag.bulksms.IPAddressWhitelist;

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
