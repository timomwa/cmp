package com.pixelandtag.cmp.ejb.bulksms;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import com.pixelandtag.api.MTStatus;
import com.pixelandtag.bulksms.BulkSMSAccount;
import com.pixelandtag.bulksms.BulkSMSPlan;
import com.pixelandtag.bulksms.IPAddressWhitelist;

public interface BulkSMSUtilBeanI {
	
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
	 * @param price 
	 * @param senderid 
	 * @param telcoid 
	 * @return
	 */
	public String getPlanQueueStatus(BulkSMSPlan plan, String telcoid, String senderid, String price);
	
	
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

}
