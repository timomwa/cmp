package com.pixelandtag.cmp.ejb.bulksms;

import java.util.List;

import org.json.JSONException;

import com.pixelandtag.bulksms.BulkSMSQueue;
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

	/**
	 * Gets a list of unprocessed
	 * outgoing messaes
	 * @param size
	 * @return
	 */
	public List<BulkSMSQueue> getUnprocessed(Long size);

	/**
	 * 
	 * @param bulktext
	 * @return
	 */
	public BulkSMSQueue saveOrUpdate(BulkSMSQueue bulktext)  throws Exception;
}
