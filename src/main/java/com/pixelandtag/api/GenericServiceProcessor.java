package com.pixelandtag.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import com.pixelandtag.api.Settings;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.mms.api.TarrifCode;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.BillingService;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.sms.producerthreads.Operation;
import com.pixelandtag.util.FileUtils;

/**
 * @author Timothy Mwangi Gikonyo.
 *
 */
public abstract class GenericServiceProcessor implements ServiceProcessorI {
	
	protected Properties mtsenderprop;
	protected String host;
	protected String dbName;
	protected String username;
	protected String password;
	public static final String DB = "pixeland_content360";
	public static final String SPACE = " ";
	public static final String NUM_SEPERATOR = "."+SPACE;
	public static final String NEW_LINE = "\n";
	public static final String SERVICENAME_TAG = "<SERVICE_NAME>";
	public static final String PRICE_TAG = "<SMS_SUBSCRIPTION_PRICE>";
	public static final String USERNAME_TAG = "<USERNAME>";
	public static final String AGE_TAG = "<AGE>";
	public static final String GENDER_PRONOUN_TAG = "<GENDER_PRONOUN>";
	public static final String GENDER_PRONOUN_TAG2  = "<GENDER_PRONOUN2>";
	public static final String GENDER_PRONOUN_N = "GENDER_PRONOUN_N";
	public static final String DEST_USERNAME_TAG = "<DEST_USERNAME>";
	public static final String PROFILE_TAG = "<PROFILE>";
	public static final String GENDER_PRONOUN_M = "GENDER_PRONOUN_M";
	public static final String GENDER_PRONOUN_F = "GENDER_PRONOUN_F";
	public static final String GENDER_PRONOUN_INCHAT_F = "GENDER_PRONOUN_INCHAT_F";
	public static final String GENDER_PRONOUN_INCHAT_M = "GENDER_PRONOUN_INCHAT_M";
	public static final String CHAT_USERNAME_SEPERATOR = " Says: ";
	public static final String CHAT_USERNAME_SEPERATOR_DIRECT = " : ";
	public static final String KEYWORD_TAG = "<KEYWORD>";
	public static final String CONFIRMED_SUBSCRIPTION_ADVICE = "CONFIRMED_SUBSCRIPTION_ADVICE";
	public static final String SUBSCRIPTION_CONFIRMATION_ADVICE = "SUBSCRIPTION_CONFIRMATION_ADVICE";
	public static final String MAIN_MENU_ADVICE = "MAIN_MENU_ADVICE";
	public static final String NO_PENDING_SUBSCRIPTION_ADVICE = "NO_PENDING_SUBSCRIPTION_ADVICE";
	public static final String UNSUBSCRIBED_ALL_ADVICE = "UNSUBSCRIBED_ALL_ADVICE";
	public static final String UNKNOWN_KEYWORD_ADVICE = "UNKNOWN_KEYWORD_ADVICE";
	public static final String UNSUBSCRIBED_SINGLE_SERVICE_ADVICE = "UNSUBSCRIBED_SINGLE_SERVICE_ADVICE";
	public static final String RM = "";//"RM<PRICE>\n";
	public static final String PRICE_TG = "<PRICE>";
	public static final String CHOSEN = "<CHOSEN>";
	//private static final String COLON = ":";
	public static final String SUBSCRIPTION_CONFIRMATION = "ON";
	public static final String SUBSCRIPTION_ADVICE = "SUBSCRIPTION_ADVICE";

	private static final String ACK_SQL = "UPDATE `"+CelcomImpl.database+"`.`messagelog` SET mo_ack=1 WHERE id=?";

	private static final String SEND_MT_1 = "insert into `"+CelcomImpl.database+"`.`httptosend`" +
				"(SMS,MSISDN,SendFrom,fromAddr,CMP_AKeyword,CMP_SKeyword,Priority,CMP_TxID,split,serviceid,price,SMS_DataCodingId,mo_processorFK,billing_status,price_point_keyword,subscription) " +
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE billing_status=?, re_tries=re_tries+1";

	private static final String SEND_MT_2 = "insert into `"+CelcomImpl.database+"`.`httptosend`" +
				"(SMS,MSISDN,SendFrom,fromAddr,CMP_AKeyword,CMP_SKeyword,Priority,split,serviceid,price,SMS_DataCodingId,mo_processorFK,billing_status,price_point_keyword,subscription) " +
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE billing_status=?,  re_tries=re_tries+1";

	private static final String TO_STATS_LOG = "INSERT INTO `"+CelcomImpl.database+"`.`SMSStatLog`(SMSServiceID,msisdn,transactionID, CMP_Keyword, CMP_SKeyword, price, subscription) " +
						"VALUES(?,?,?,?,?,?,?)";


	protected String subscriptionText;
	protected String unsubscriptionText;
	protected String tailTextSubscribed;
	protected String tailTextNotSubecribed;
	private Map<Long, ServiceProcessorDTO> serviceProcessorCache = new HashMap<Long,ServiceProcessorDTO>();
	
	
	public String getSubscriptionText() {
		return subscriptionText;
	}


	public void setSubscriptionText(String subscriptionText) {
		this.subscriptionText = subscriptionText;
	}


	public String getUnsubscriptionText() {
		return unsubscriptionText;
	}


	public void setUnsubscriptionText(String unsubscriptionText) {
		this.unsubscriptionText = unsubscriptionText;
	}


	public String getTailTextSubscribed() {
		return tailTextSubscribed;
	}


	public void setTailTextSubscribed(String tailTextSubscribed) {
		this.tailTextSubscribed = tailTextSubscribed;
	}


	public String getTailTextNotSubecribed() {
		return tailTextNotSubecribed;
	}


	public void setTailTextNotSubecribed(String tailTextNotSubecribed) {
		this.tailTextNotSubecribed = tailTextNotSubecribed;
	}
	
	public void initQueue(int size){
		this.max_queue_size = size;
		
		if(this.max_queue_size>0)
			moMsgs = new LinkedBlockingDeque<MOSms>(this.max_queue_size);
		else
			moMsgs = new LinkedBlockingDeque<MOSms>();
		
		
	}
	
	protected Logger logger = Logger.getLogger(getClass());//check this
	
	/**
	 * internal message queue
	 */
	private volatile BlockingDeque<MOSms> moMsgs = null;
	
	
	protected boolean run = true;
	protected boolean finished = false;
	public String name;
	protected boolean busy = false;
	protected int max_queue_size = -1;
	
	
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.api.ServiceProcessorI#enqueue(com.pixelandtag.celcom.entities.MOSms)
	 */
	public boolean submit(MOSms mo_){
		boolean success = false;
		try{
			success =  this.moMsgs.offerLast(mo_);
		}catch(Exception e){
			this.logger.error(e.getMessage(),e);
		}
		return success;
	}
	
	
	private MOSms bill(MOSms mo_) {
		
		logger.debug(" in com.pixelandtag.api.GenericServiceProcessor.bill(MOSms) ");
		logger.debug("mo_.getPrice().doubleValue() "+mo_.getPrice().doubleValue());
		logger.debug(" mo_.getPrice().compareTo(BigDecimal.ZERO) "+mo_.getPrice().compareTo(BigDecimal.ZERO));
		if(mo_.getPrice().compareTo(BigDecimal.ZERO)<=0){//if price is zero
			mo_.setCharged(true);
			mo_.setBillingStatus(BillingStatus.NO_BILLING_REQUIRED);
			logger.debug(" returning.... price is zero ");
			return mo_;
		}
		
		Billable billable = null;
		
		try{
			
			billable = getEJB().find(Billable.class, "cp_tx_id",mo_.getCMP_Txid());
			
			
		}catch(Exception e){
			logger.debug(" something went terribly wrong! ");
			logger.error(e.getMessage(),e);
		}finally{
			
		}
		
		if(billable==null)
			billable  = new Billable();
		else
			return mo_;
		
		

		billable.setCp_id("CONTENT360_KE");
		billable.setCp_tx_id(mo_.getCMP_Txid());
		billable.setDiscount_applied("0");
		billable.setEvent_type(mo_.getEventType());
		billable.setIn_outgoing_queue(0l);
		billable.setKeyword(mo_.getSMS_Message_String().split("\\s")[0].toUpperCase());
		billable.setMaxRetriesAllowed(1L);
		billable.setMessage_id(mo_.getId());
		billable.setMsisdn(mo_.getMsisdn());
		billable.setOperation(mo_.getPrice().compareTo(BigDecimal.ZERO)>0 ? Operation.debit.toString() : Operation.credit.toString());
		billable.setPrice(mo_.getPrice());
		billable.setPriority(0l);
		billable.setProcessed(0L);
		billable.setRetry_count(0L);
		billable.setService_id(mo_.getSMS_Message_String().split("\\s")[0].toUpperCase());
		billable.setShortcode(mo_.getSMS_SourceAddr());		
		billable.setTx_id(mo_.getCMP_Txid());
		billable.setEvent_type(mo_.getEventType()!=null ? mo_.getEventType() : EventType.SUBSCRIPTION_PURCHASE);
		billable.setPricePointKeyword(mo_.getPricePointKeyword());
		logger.debug(" before save "+billable.getId());
		
		try{
			billable = getEJB().saveOrUpdate(billable);
		}catch(Exception e){
			e.printStackTrace();
			logger.debug(" something went terribly wrong! ");
			logger.error(e.getMessage(),e);
		}finally{
		}
		
		logger.debug(" after save "+billable.getId());
		mo_.setBillingStatus(BillingStatus.WAITING_BILLING);
		mo_.setPriority(0);
		logger.debug(" leaving  com.pixelandtag.api.GenericServiceProcessor.bill(MOSms) ");
		return mo_;
		
		
	}


	/**
	 * Gets the internal queue size.
	 */
	public int getQueueSize(){
		return this.moMsgs.size();
	}
	
	
	public boolean isBusy() {
		return busy;
	}

	private synchronized void setBusy(boolean busy) {
		this.busy = busy;
		notify();
	}
	

	public boolean isFinished() {
		return finished;
	}

	private void setFinished(boolean finished) {
		this.finished = finished;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRunning() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}
	
	public synchronized void rezume(){
		this.notify();
	}
	
	public synchronized void pauze(){
		try {
			
			this.wait();
		
		} catch (InterruptedException e) {
			
			logger.debug(getName()+" we now run!");
		
		}
	}
	
	
	
	
	
	
	
	/**
	 * Determines whether the queue is full.
	 * @return true if the unprocessed queue size is equal to
	 * the size of the queue
	 */
	public boolean queueFull(){
		
		logger.info("max_queue_size:: "+max_queue_size);
		if(max_queue_size==-1)
			return false;
		if(max_queue_size==moMsgs.size())
			return true;
		else
			return false;
	}
	
	public GenericServiceProcessor(){
		
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		
		host = mtsenderprop.getProperty("db_host");
		dbName = mtsenderprop.getProperty("DATABASE");
		username = mtsenderprop.getProperty("db_username");
		password = mtsenderprop.getProperty("db_password");
		
	}
	
	public static Vector<String> splitText(String input){
  		int maxSize = 156;
		Vector<String> ret=new Vector<String>();
  		
  		while(true){
  			input=input.trim();
  			if (input.length()<=maxSize){
  				ret.add(input);
  				break;
  			}
  			int pos=maxSize;
  			
            while(input.charAt(pos)!=' ' && input.charAt(pos)!='\n')
  				pos--;
  			String tmp=input.substring(0,pos);
  			ret.add(tmp);
  			input=input.substring(pos);
  			
  		}
  		return ret;
  }
	
	/**
	 * TODO turn all connection objects to CMPResourceBean
	 * @param mo
	 * @param conn
	 */
	
	protected void toStatsLog(MOSms mo, Connection conn) {
		
		
		
		try {
			
			getEJB().toStatsLog(mo,TO_STATS_LOG);	
			/*pstmt = conn.prepareStatement(TO_STATS_LOG,Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setInt(1, mo.getServiceid());
			pstmt.setString(2, mo.getMsisdn());
			pstmt.setLong(3, mo.getCMP_Txid());
			pstmt.setString(4, mo.getCMP_AKeyword());
			pstmt.setString(5, mo.getCMP_SKeyword());
			if(mo.getCMP_SKeyword().equals(TarrifCode.RM1.getCode()))
				pstmt.setDouble(6, 1d);
			else
				pstmt.setDouble(6, mo.getPrice().doubleValue());
			
			pstmt.setBoolean(7, mo.isSubscriptionPush());
			pstmt.executeUpdate();*/
			
		} catch (Exception e) {
			
			log(e);
		
		}finally{
			
			try {
				
			//	if(pstmt!=null)
				//	pstmt.close();
				
			} catch (Exception e) {
				
				log(e);
				
			}
		}
		
	}
	
	private void log(Exception e) {
		logger.error(e.getMessage(),e);
	}

	public long generateNextTxId(){
		
		/*String timestamp = String.valueOf(System.currentTimeMillis());
		
		return Settings.INMOBIA.substring(0, (19-timestamp.length())) + timestamp;//(String.valueOf(Long.MAX_VALUE).length()-timestamp.length())) + timestamp;
*/		
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			logger.warn(e.getMessage());
		}
		return System.currentTimeMillis();
	}
	
	
	
	
	
	@Override
	public void run() {

		try{
			
			initQueue(this.max_queue_size);
				
			while(run){
				
				try{
					
					final MOSms mo = this.moMsgs.takeFirst();//will block here while the queue is empty
					
					acknowledge(mo.getId());
					
					if(!(mo.getCMP_Txid().compareTo(BigInteger.valueOf(-1))==0)){
						
						setBusy(true);//set to busy so that it's not picked from the pool.
					
						final MOSms mo_tmp = process(mo);//this will actually process the stuff.. uses the subclass to process..
						
						logger.debug("\n\n\n\n\n\n>>>>>>>>>>>>>>>> OUR MO >>>>>>>>>>>>>>>>> "+mo_tmp.toString()+"\n\n\n\n");
						
						sendMT(mo_tmp);
					
						setBusy(false);
					
					}else{
						logger.debug("got poison pill!");
						setRun(false);
					
					}
					
				}catch(Exception e){
					
					logger.error(e.getMessage(),e);
				
				}
			}
			
			setFinished(true);
			
			setBusy(false);
			
		}catch(OutOfMemoryError e){
			
			logger.error(e.getMessage(),e);
			
			//send alarm
		}finally{
			
			setFinished(true);
			
			setBusy(false);
			
			finalizeMe();
		
		}
		
		logger.debug(":::::::::::::::::: "+getName()+": terminated:");
		
		
	}
	
	/**
	 * Set the internal sms queue
	 */
	public void setInternalQueue(int i){
		this.max_queue_size = i;
	}
	
	
	
	/**
	 * Acknowledge that we've received this MO message and are going to process it.
	 * TODO - have this method in the generic supper class .. no need for all processors
	 * to implement it because the function is just the same no matter the kind
	 * of processor
	 */
	public boolean acknowledge(long message_log_id) {
		
		boolean success = false;
		
		try {
			
			success = getEJB().acknowledge(message_log_id);
			
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			
		}
		
		return success;
	}
	
	
	
	
	/**
	 * Insert into http to send
	 * @param mo
	 */
	public void  sendMT(MOSms mo) {
		
		mo  = bill(mo);
		
		BaseEntityI cmpBean = getEJB();
		
		
		
		try {
			
			
			int max_retry = 5;
			int count = 0;
			
			if(!(mo.getCMP_Txid().compareTo(BigInteger.valueOf(-1))==0)){
			
					boolean success = cmpBean.sendMT(mo,SEND_MT_1);
					while(!success && count<=max_retry){
						success  = cmpBean.sendMT(mo,SEND_MT_1);
						count++;
					}
			
			}else{
				
					boolean success = cmpBean.sendMT(mo,SEND_MT_2);
					while(!success && count<=max_retry){
						success  = cmpBean.sendMT(mo,SEND_MT_1);
						count++;
					}
				
					
			}
			
		
		} catch (SQLException e) {
			
			logger.error(e.getMessage(),e);
			
		}catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			
		}
		
	}
	
	
	
}
