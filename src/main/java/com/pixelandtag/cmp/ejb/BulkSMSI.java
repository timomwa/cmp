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
	public void enqueue(String sourceIp, String apiKey, String username, String password, String jsonString) throws APIAuthenticationException,PlanException,PersistenceException,ParameterException,JSONException,PlanBalanceException,QueueFullException;

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
	
	
	/**
	 * 
	 * @param plan
	 * @return
	 * @throws PlanBalanceException
	 */
	public BigInteger getPlanBalance(BulkSMSPlan plan) throws PlanBalanceException;
	
	/**
	 * Accepts a null account and searches
	 * only the ip Address
	 * @param ipAddress - java.lang.String
	 * @param account - com.pixelandtag.bulksms.BulkSMSAccount
	 * @return com.pixelandtag.bulksms.IPAddressWhitelist
	 * @throws APIAuthenticationException
	 * @throws ParameterException
	 */
	public IPAddressWhitelist getWhitelist(String ipAddress,BulkSMSAccount account) throws APIAuthenticationException,ParameterException;

	/**
	 * 
	 * @param plan - com.pixelandtag.bulksms.BulkSMSPlan
	 * @param status - com.pixelandtag.api.MTStatus
	 * @return - java.math.BigInteger
	 */
	public BigInteger getCurrentOutgoingQueue(BulkSMSPlan plan,MTStatus status);

	/**
	 * 
	 * @param plan
	 * @return
	 */
	public String getPlanQueueStatus(BulkSMSPlan plan);

	
}
