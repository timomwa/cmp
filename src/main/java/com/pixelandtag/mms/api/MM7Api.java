package com.pixelandtag.mms.api;

import java.util.concurrent.BlockingDeque;

import com.pixelandtag.api.Settings;

/**
 * 
 * @author Timothy Mwangi Gikonyo
 *
 */
public interface MM7Api  extends Settings {
	
	public String getFr_tz() ;
	
	public String getTo_tz();

	public void setFr_tz(String fr_tz);

	public void setTo_tz(String to_tz);
	
	
	/**
	 * This method must return an MMS object that is ready to be sent..
	 * @param msisdn
	 * @param uri
	 * @return
	 */
	public MMS createMMS(String msisdn, String imgUrl, String mmstext, String subject);
	
	/**
	 * retrieves from mms_to_send
	 * @param txID
	 * @return
	 */
	public MMS retrieveMMS(String txID);
	
	
	/**
	 * mark mms to be in processing queue, that way, its not retrieved again and reprocessed while its still being processed
	 * @param txID
	 * @return
	 */
	public boolean toggleInProcessingQueue(String txID, boolean inqueue);
	
	
	/**
	 * toggles as sent or not..
	 *  most likely we'll onle need to mark it as sent..
	 * @param txID
	 * @param sent
	 * @return
	 */
	public boolean toggleSent(String txID, boolean sent);

	
	/**
	 * Call this when you want to send mms
	 * @param mms
	 * @return
	 */
	public boolean queueMMSForSending(MMS mms);
	
	/**
	 * When we receive an MM7 dlr, we acknowledge receipt.
	 * @param report
	 * @return
	 */
	public boolean acknowledge(MM7DeliveryReport report);
	
	
	/**
	 * deletes an already sent MTTosend
	 * @param id
	 * @return
	 */
	public boolean deleteMMSMTToSend(String id);
	
	
	public BlockingDeque<MMS> getLatestMTMMS(int limit);

	public boolean logMTMMS(MMS mms);

	public void myfinalize();
	
	public boolean acknowledge(SoapMMSDN soap);

}
