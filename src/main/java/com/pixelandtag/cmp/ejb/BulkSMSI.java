package com.pixelandtag.cmp.ejb;

import javax.ejb.Local;
import javax.ws.rs.core.HttpHeaders;

import org.json.JSONException;

import com.pixelandtag.bulksms.BulkSMSAccount;
import com.pixelandtag.bulksms.BulkSMSPlan;

public interface BulkSMSI {
	
	/**
	 * 
	 * @param apiKey
	 * @param username
	 * @param password
	 * @return com.pixelandtag.bulksms.BulkSMSAccount
	 * @throws APIAuthenticationException
	 */
	public BulkSMSAccount getAccout(String apiKey,String username, String password) throws APIAuthenticationException;

	/**
	 * 
	 * @param account
	 * @param planid
	 * @return com.pixelandtag.bulksms.BulkSMSPlan
	 * @throws PlanException
	 */
	public BulkSMSPlan getPlan(BulkSMSAccount account, String planid) throws PlanException;

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
	public void enqueue(String sourceIp, String apiKey, String username, String password, String jsonString) throws APIAuthenticationException,PlanException,PersistenceException,ParameterException,JSONException;

	/**
	 *  Checks if requesting host is allowed
	 * @param ipAddress
	 * @return boolean - true if allowed, false if not
	 * @throws APIAuthenticationException,ParameterException
	 */
	public boolean hostAllowed(String ipAddress) throws APIAuthenticationException,ParameterException;
	
	/**
	 * Checks if requesting host has an account tied to it and 
	 * is is allowed
	 * @param account - com.pixelandtag.bulksms.BulkSMSAccount
	 * @param sourceIp - java.lang.String
	 * @return boolean - true if allowed, false if not
	 * @throws APIAuthenticationException,ParameterException
	 */
	public boolean hostAllowed(BulkSMSAccount account,String sourceIp) throws APIAuthenticationException,ParameterException;

}
