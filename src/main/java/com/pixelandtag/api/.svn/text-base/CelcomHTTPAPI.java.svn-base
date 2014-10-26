package com.inmobia.celcom.api;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Queue;

import com.inmobia.celcom.entities.MOSms;
import com.inmobia.celcom.entities.MTsms;
import com.inmobia.celcom.entities.Notification;
import com.inmobia.celcom.serviceprocessors.dto.ServiceProcessorDTO;

/**
 * 
 * @author Timothy Mwangi 
 * @since 2nd Feb 2012.
 * 
 * This interfaces the CELCOM over HTTP(S) API for sending messages.
 * See the document  - "CMP30 - HTTP(S) API Specification v2.04.pdf"
 *
 */
public interface CelcomHTTPAPI extends Settings {
	
	
	/**
	 * Sets the server timezone value
	 * @param server_timezone. Accepted for mia = -05:00, malaysia = +08:00
	 * @return
	 */
	public void setFr_tz(String server_timezone);
	
	/**
	 * gets you the set Server timezone
	 * @return
	 */
	public String getFr_tz();
	
	
	
	/**
	 * the destination timestamp malay = +08:00
	 * @param to_tz
	 */
	public void setTo_tz(String to_tz);
	
	public String getTo_tz();
	
	/**
	 * Logs an incoming MO to the database.
	 * 
	 * @param mo - com.inmobia.celcom.MO
	 */
	public void logMO(MOSms mo);
	
	
	/**
	 * Retrieves an mo message from the database.
	 * @return
	 */
	public MOSms retrieveMO(String cMP_Txid);
	
	
	/**
	 * Logs an MT to the database
	 * @param mt - com.inmobia.celcom.MTsms
	 */
	public void logMT(MTsms mt);
	
	
	/**
	 * When we receive a message, we should acknowledge it's receipt.
	 * We should send to the operator
	 * @param mo - com.inmobia.celcom.MO
	 */
	public void acknowledgeReceipt(MOSms mo);
	
	
	
	/**
	 * When we send an MT, we need to know if it was delivered.
	 * 
	 * @param notif
	 */
	public void acknowledgeDN(Notification notif);
	
	
	
	/**
	 * Update SMSStatLog.
	 * 
	 * @param notif
	 */
	public void updateSMSStatLog(Notification notif);
	
	
	/**
	 * Retrieves an MO using Transaction id - CPM_Txid
	 * @param cpm_txId - java.lang.String
	 * @return com.inmobia.celcom.MO if found, null if none is found.
	 */
	public MOSms findMO(String cpm_txId);
	
	
	/**
	 * Retrieves an MT 
	 * @param cpm_txId - java.lang.String
	 * @return - com.inmobia.celcom.MTsms
	 */
	public MTsms findMT(String cpm_txId);
	
	
	/**
	 * Deletes an mt from the "to-send" log
	 * @param id
	 */
	public boolean deleteMT(long id);
	
	
	
	/**
	 * 
	 * @param http_to_send_id - the Id of the message who'se queue status needs to be modified.
	 * If a message is in queue,then the boolean inQueue is set to true, else if its out of the queue, its set to false
	 * @param inQueue - true if the message is now in queue
	 * @return true if the process was successfull, false if it was not.
	 */
	public boolean changeQueueStatus(String http_to_send_id,boolean inQueue);
	

	/**
	 * Marks a message to be in queue
	 * @param http_to_send_id - Id of the message record who'se queue status needs to be set as inqueue.
	 * @return true if operation was successfull.
	 * @throws java.lang.Exception
	 */
	public boolean markInQueue(long http_to_send_id) throws Exception;
	
	
	/**
	 * Marks a message to be sent
	 * @param http_to_send_id - Id of the message record who'se queue status needs to be set as inqueue.
	 * @return true if operation was successfull.
	 */
	public boolean markSent(long http_to_send_id);
	
	
	public boolean postponeMT(long http_to_send_id);
	
	
	/**
	 * Converts String to hex string
	 * @param input
	 * @return
	 */
	//public String toHex(String input) throws UnsupportedEncodingException;
	
	/**
	 * Checks if an SMS contains one or more unicode characters.
	 */
	public boolean containsUnicode(String sms);
	
	
	public Queue<MOSms> getLatestMO(int limit, String CMP_Keyword, String CMP_SKeyword);
	
	
	public Queue<MOSms> getLatestMO(int limit);
	
	
	public void updateMessageLog(MOSms mo);
	
	public Queue<ServiceProcessorDTO> getServiceProcessors();
	
	public MOSms resolveKeywords(MOSms mo, Connection conn);
	
	public void closeConnectionIfNecessary();
	
	
	public boolean beingProcessedd(long http_to_send_id, boolean inqueue);
	
	public void logResponse(String msisdn, String responseText);
	
	public void myfinalize();
	
	public String generateNextTxId();
	
	public String toUnicodeString(String sms);
	
	public String toHex(String sms) throws UnsupportedEncodingException;

	/**
	 * If we have MMSs waiting for an SMS to be billed, then we flag it as "paidFor"
	 * so that it's now sent.
	 * Sort of a relay mechanism
	 * @param notification
	 */
	public void flagMMSIfAny(Notification notification);

	
	/**
	 * Processes the lucky dip SMS.
	 * @param notification
	 */
	public void processLuckyDip(Notification notification);
	
	
	public Connection getConnection();
	
	
}
