package com.pixelandtag.sms.mt.workerthreads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.web.triviaI.MechanicsI;
import com.pixelandtag.web.triviaImpl.MechanicsS;
import com.pixelandtag.api.CelcomHTTPAPI;
import com.pixelandtag.api.ERROR;
import com.pixelandtag.api.GenericMessage;
import com.pixelandtag.autodraw.Alarm;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.entities.URLParams;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.mms.api.TarrifCode;
import com.inmobia.util.StopWatch;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.sms.producerthreads.MTProducer;




	
/**
 * 
 * @author Timothy Mwangi Gikonyo 
 * Date Created 7th Feb 2012.
 *
 */
public class MTHttpSender implements Runnable{
	
	private Logger logger = Logger.getLogger(MTHttpSender.class);
	
	private int http_timeout;
	private HttpClient httpclient;
	private int retry_per_msg;
	private int pollWait;
	//private DataSource ds = null;
	private DBPoolDataSource dbpds = null;
	
	//private Connection conn = null;
	private CelcomHTTPAPI celcomAPI;
	private StopWatch watch;
	private boolean run = true;
	private boolean finished = false;
	private String name;
	private boolean busy = false;
	private List<NameValuePair> qparams = null;
	private String connStr;
	private volatile boolean success = true;
	private volatile String message = "";
	private volatile int sms_idx = 0;
	private HttpPost httppost = null;
	private volatile UrlEncodedFormEntity entity;
	private volatile HttpEntity resEntity;
	private volatile HttpResponse response;
	private volatile int recursiveCounter = 0;
	private Alarm alarm = new Alarm();
	
	
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

	private URLParams urlp;



	private String mtUrl;



	private long msg_part_wait;

	private String MINUS_ONE = "-1";

	private final String FREE_TARRIF_CODE_CMP_SKEYWORD = "free_tarrif_code_cmp_SKeyword";
	private final String FREE_TARRIF_CODE_CMP_AKEYWORD = "free_tarrif_code_cmp_AKeyword";

	
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

	public MTHttpSender(int pollWait_, String name_,URLParams urlp_, String constr, HttpClient httpclient_) throws Exception{
		
		this.watch = new StopWatch();
		
		this.name = name_;
		
		watch.start();
		
		this.connStr = constr;
		
		this.http_timeout = urlp_.getHttp_timeout();
		
		this.retry_per_msg = urlp_.getRetry_per_msg();
		
		this.mtUrl = urlp_.getMturl();
		
		this.urlp = urlp_;
		
		this.pollWait = pollWait_;
		
		this.msg_part_wait = urlp_.getMsg_part_wait();
		
		this.celcomAPI = new CelcomImpl(this.connStr,"THRD_"+name);
		
		this.httpclient = httpclient_;
		
		qparams = new LinkedList<NameValuePair>();
		
		this.celcomAPI.setFr_tz(urlp.getSERVER_TZ());
		this.celcomAPI.setTo_tz(urlp.getCLIENT_TZ());
		
		
		
		
		int vendor = DriverUtilities.MYSQL;
	    String driver = DriverUtilities.getDriver(vendor);
	    String host = HTTPMTSenderApp.props.getProperty("db_host");
	    String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
	    String url = DriverUtilities.makeURL(host, dbName, vendor);
	    String username = HTTPMTSenderApp.props.getProperty("db_username");
	    String password = HTTPMTSenderApp.props.getProperty("db_password");
	    
	    
  try {
			
			
		    
		    
			dbpds = new DBPoolDataSource();
			dbpds.setName(this.name+"-DS");
			dbpds.setValidatorClassName("snaq.db.Select1Validator");
			dbpds.setName("celcom-impl");
			dbpds.setDescription("Impl Pooling DataSource");
			dbpds.setDriverClassName("com.mysql.jdbc.Driver");
			dbpds.setUrl(url);
			dbpds.setUser(username);
			dbpds.setPassword(password);
			dbpds.setMinPool(1);
			dbpds.setMaxPool(2);
			dbpds.setMaxSize(3);
			dbpds.setIdleTimeout(3600);  // Specified in seconds.
			 dbpds.setValidatorClassName("snaq.db.Select1Validator");
			dbpds.setValidationQuery("SELECT 'test'");
			
			logger.info("Initialized db pool ok!");
			
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
		
		}finally{}
  
		logger.info("this.celcomAPI.getFr_tz():::::: "+this.celcomAPI.getFr_tz());
		logger.info("this.celcomAPI.getTo_tz():::::: "+this.celcomAPI.getTo_tz());
		
	
	}
	
	
	
	public Connection getConn() {
		
		try {
			
			return dbpds.getConnection();
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
			return null;
		
		}finally{
		
		}
	}
	

	public void run() {
		
		try{
			
			pauze();//wait while producer gets ready
			
			celcomAPI.setFr_tz(urlp.getSERVER_TZ());//set timezones
			celcomAPI.setTo_tz(urlp.getCLIENT_TZ());//set timezones
			
			watch.stop();
			
			logger.info(getName()+" STARTED AFTER :::::RELEASED_BY_PRODUCER after "+(Double.parseDouble(watch.elapsedTime(TimeUnit.MILLISECONDS)+"")) + " mili-seconds");
			
			watch.reset();
			
			StringBuffer sb = new StringBuffer();
			
			while(run){
				
				
				
				try {
					
					//blocks if queue is empty
					//synchronized(MTProducer.mtMsgs){
					final MTsms mtsms = MTProducer.getMTsms();
					//}
				
					
					logger.debug(":the service id in worker!::::: mtsms.getServiceID():: "+mtsms.getServiceid());
					
					
					if(mtsms!=null)
					if(mtsms.getId()>-1){
						
						setSms_idx(0);//reset the sms index counter each time we get a brand new message to send.
						//String originalTXID = mtsms.getCMP_Txid();
						
						
						if(mtsms.getSms()!=null)
						if(mtsms.getSms().length()>140 && mtsms.isSplit_msg()){
						
							Vector<String> msg = splitText(mtsms.getSms());
							
							mtsms.setNumber_of_sms(msg.size());
							
							
							for(int x=0; x<msg.size();x++){
								
								mtsms.setMsg_part(sb.append((x+1)).append("/").append(msg.size()).append(" ").append(msg.get(x)).toString());
								
								sb.setLength(0);
								
								sendMT(mtsms);//send SMS the way it is
								
							}
							
						}else{
							
							mtsms.setNumber_of_sms(1);
							
							sendMT(mtsms);
						
						}
						
						
					}
					
				
				} catch (InterruptedException e) {
					
					log(e);
				
				}catch (Exception e){
					
					log(e);
					
				}finally{
					
					setSms_idx(0);
				
				}
				
			}
			
			celcomAPI.myfinalize();
			
			setFinished(true);
			
			setBusy(false);
			
			logger.info(getName()+": worker shut down safely!");
		
		}catch(OutOfMemoryError e){
			
			logger.fatal("NEEDS RESTART: MEM_USAGE: "+MTProducer.getMemoryUsage() +" >> "+e.getMessage(),e);
			//Hasn't happened so far during testing. Not expected to happen during runtime
			//please send alarm
			
			Connection conn = null;
			try{
				conn = getConn();
				alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: SEVERE:", "Hi,\n\n We encountered a fatal exception. Please check Malaysia HTTP Sender app.\n\n  Regards");
			}catch(Exception e2){
				log(e2);
			}finally{
				try{
					conn.close();
				}catch(Exception e4){}
			}
			
		}finally{
			
			try{
				//this.conn.close();
			}catch(Exception e){}
		} 
		
	}
	
	
	private synchronized void setSms_idx(int i) {
		this.sms_idx = i;
		notify();
	}

	private synchronized int getSms_idx() {
		return sms_idx;
		
	}

	private Vector<String> splitText(String input){
  		int maxSize = 136;
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
	
	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}

	/**
	 * Sends the MT message
	 * @param mt - com.pixelandtag.MTsms
	 */
	private void sendMT(MTsms mt){
		
		
		Connection conn = null;
		this.success  = true;
		
		setBusy(true);
		
		if(getSms_idx()==0){
			logger.debug(getName()+" got message to send!! "+mt.toString());
		}else{
			logger.debug(getName()+" Sending message "+(getSms_idx()+1)+ " of "+mt.getNumber_of_sms() + " sms: "+mt.getMsg_part());
		}
		
		httppost = new HttpPost(this.mtUrl);
		
		setSms_idx((getSms_idx()+1));
		
		try {
			
			conn = getConn();
			
			
			
			qparams.add(new BasicNameValuePair("login", urlp.getLogin()));
			qparams.add(new BasicNameValuePair("pass",urlp.getPass()));	
			qparams.add(new BasicNameValuePair("type",urlp.getType()));
			qparams.add(new BasicNameValuePair("src",mt.getShortcode()));
			qparams.add(new BasicNameValuePair("msisdn",mt.getMsisdn()));
			
			
			watch.start();
			
			
			if(mt.getSms()!=null || mt.getMsg_part()!=null){
				
				logger.debug("full msg: "+mt.getSms());
				logger.debug("part msg: "+mt.getMsg_part());
				
				
				
				if(mt.getNumber_of_sms()>1){//only on the last SMS do we actually send the whole sms...
					
					if(mt.getSMS_DataCodingId().equalsIgnoreCase(GenericMessage.NON_ASCII_SMS_ENCODING_ID))
						qparams.add(new BasicNameValuePair("sms",celcomAPI.toUnicodeString(mt.getMsg_part())));//URLEncoder.encode(mt.getMsg_part(), "UTF8"));//send part after part if msg is < 140 char.
					else
						qparams.add(new BasicNameValuePair("sms",mt.getMsg_part()));//URLEncoder.encode(mt.getMsg_part(), "UTF8"));//send part after part if msg is < 140 char.
					
				}else{
					if(mt.getSMS_DataCodingId()!=null && mt.getSMS_DataCodingId().equalsIgnoreCase(GenericMessage.NON_ASCII_SMS_ENCODING_ID))
						qparams.add(new BasicNameValuePair("sms",celcomAPI.toUnicodeString(mt.getSms())));//URLEncoder.encode(mt.getSms(), "UTF8"));
					else
						qparams.add(new BasicNameValuePair("sms",mt.getSms()));
				}
				
				
			}
			
			entity = new UrlEncodedFormEntity(qparams, "UTF-8");
			
			httppost.setEntity(entity);
			
			response = httpclient.execute(httppost);
			
			resEntity = response.getEntity();
			
			printHeader();
			
	        final int RESP_CODE = response.getStatusLine().getStatusCode();
			
			logger.debug("resp: :::::::::::::::::::::::::::::RESP_CODE["+RESP_CODE+"]:::::::::::::::::::::: resp:");
			
			mt.setCMPResponse("PCM"+RESP_CODE);
			
			if (RESP_CODE == HttpStatus.SC_OK) {
				
				mt.setCMPResponse(ERROR.Success.toString());
				
				if(getSms_idx()==mt.getNumber_of_sms()){//If we've sent ALL, then we delete the MT from queue, then do other logs.. 
					celcomAPI.deleteMT(mt.getId());//Delete the MT from smpptosend table. TODO uncomment when in production
					mt.setMT_STATUS(ERROR.WaitingForDLR.toString());
					celcomAPI.logMT(mt);//insert into msglog table
				}else{
					logger.debug("PARANOIA: WAITING FOR "+this.msg_part_wait+" milliseconds before sending the next message segment");
					Thread.sleep(this.msg_part_wait);
				
				}
				
				//TODO when we launch, remove to save CPU.
				if(mt.getNumber_of_sms()>1){
					
					celcomAPI.logResponse(mt.getMsisdn(),mt.getMsg_part());//log each segment of an SMS..
					
				}else{
					
					celcomAPI.logResponse(mt.getMsisdn(),mt.getSms());//log each segment of an SMS..
				
				}
					
				//TODO and postponed - introduce postpone functionality
				
				watch.stop();
				
				
				if(mt.getNewCMP_Txid()!=null)
					if(!mt.getNewCMP_Txid().equals(MINUS_ONE)){
						logger.info("mt.getSUB_R_Mobtel()="+mt.getSUB_R_Mobtel()+" ::::: MT_Sent="+mt.getSms()+":::: Shortcode="+mt.getShortcode()+" :::< . >< . >< . >< . >< . it took "+(Double.valueOf(watch.elapsedTime(TimeUnit.MILLISECONDS)/1000d)) + " seconds to clear deliver SMS via HTTP");
					}else{
						logger.debug("RETRY__ mt.getSUB_R_Mobtel()="+mt.getSUB_R_Mobtel()+" ::::: MT_Sent="+mt.getSms()+":::: Shortcode="+mt.getSendFrom()+" :::< . >< . >< . >< . >< . it took "+(Double.valueOf(watch.elapsedTime(TimeUnit.MILLISECONDS)/1000d)) + " seconds to clear deliver SMS via HTTP");
					}
				logAllParams(qparams);
				
				this.success  = true;
				
			}else if(RESP_CODE == 400){
				
				mt.setMT_STATUS(ERROR.FailedToSend.toString());
				mt.setCMPResponse(ERROR.PCM400.toString());
				
				celcomAPI.logMT(mt);//insert into msglog table
				
				logger.error("\nCP_Id is Null or blank"
				+"\nCP_UserId Null or blank"
				+"\nCP_Password Null or blank"
				+"\nCMP_A_Keyword =Null or blank"
				+"\nCMP_Txid = Null or blank"
				+"\nSMS_Msgdata is not Valid.");
				
				logAllParams(qparams);
				
				
				
			}else if(RESP_CODE == 401){
				
				mt.setCMPResponse(ERROR.ContentType_Is_Null.toString());
				
				mt.setMT_STATUS(ERROR.FailedToSend.toString());
				
				celcomAPI.logMT(mt);//insert into msglog table
				
				logger.error("\nCMP_ContentType is Null or not defined as per CMP_ContentType in above table..");
				
			}else if(RESP_CODE == 404 || RESP_CODE == 403){
				
				
				mt.setCMPResponse(ERROR.PCM404.toString()); 
				
				mt.setMT_STATUS(ERROR.FailedToSend.toString());
				
				celcomAPI.logMT(mt);//insert into msglog table
				
				logger.error("SMS_SourceAddr is Null or blank"
							+"\nSUB_R_Mobtel is Null or blank"
							+"\nSUB_C_Mobtel is Null or blank"
							+"\nSMS_Msgdata is Null for CMP_ContentType not equal to TM"
							+"\nSMS_Msgtxt is Null for CMP_ContentType equal to TM");
				
				logAllParams(qparams);
				
			}else if(RESP_CODE == 503){

				mt.setMT_STATUS(ERROR.FailedToSend.toString());
				
				mt.setCMPResponse(ERROR.SERVER_INTERNAL_QUEUE_FULL.toString());
				
				logger.error("CMP Server Internal Queue full for this Service. CP needs to try again later");
				
				this.success  = false;
				
				logAllParams(qparams);
				
			}else{
				
				this.success = false;
				
				logAllParams(qparams);
				
			}
			
					
			}catch(ConnectException ce){
				
				this.success  = false;
				
				message = ce.getMessage();
				
				logger.error(message, ce);
				
				httppost.abort();
				
			}catch(SocketTimeoutException se){
				
				this.success  = false;
				
				message = se.getMessage();
				
				httppost.abort();
				
				logger.error(message, se);
				
			} catch (IOException ioe) {
				
				this.success  = false;
				
				message = ioe.getMessage();
				
				httppost.abort();
				
				logger.error("\n\n==============================================================\n\n"+message+" CONNECTION TO OPERATOR FAILED. WE SHALL TRY AGAIN. Re-tries so far "+recursiveCounter+"\n\n==============================================================\n\n");
				
			} catch (Exception ioe) {
				
				this.success  = false;
				
				message = ioe.getMessage();
				
				httppost.abort();
				
				logger.error(message, ioe);
				
			} finally{
				
				
				
				
				//postMethod.;
				//client.executeMethod(postMethod);
				
				if(!this.success){//return back to queue if we did not succeed
					
					
					if((mt.getNumber_of_sms()>1) && run && (recursiveCounter<=3)){//We only try 3 times recursively if we've not been poisoned and its one part of a multi-part message, we try to re-send, but no requeuing
						
						setSms_idx((getSms_idx()-1));//we did not send successfully, so it does not count!
						sendMT(mt); //try to send re-cursively instead of putting it back in the queue..
						logger.warn("SENDING RE_CURSIVELY!!!!!!!!!!!!! we've been here "+recursiveCounter+ " more time(s) we'll give up on the 3rd try, and queue the message to the database");
					
						recursiveCounter++;
						
					}else{
						
						logger.info(message+" Putting back MT into queue! "+mt.toString());
						
						celcomAPI.postponeMT(mt.getId());//If we timeout, we postpone the MT
					
					}
					httppost.abort();
					
					
				}else{
					
					recursiveCounter = 0;
					//logger.warn(message+" >>MESSAGE_NOT_SENT> "+mt.toString());
				}
				
				watch.reset();
				
				setBusy(false);
				
				logger.debug(getName()+" ::::::: finished attempt to deliver SMS via HTTP");
				
				removeAllParams(qparams);
				
				 // When HttpClient instance is no longer needed,
	            // shut down the connection manager to ensure
	            // immediate deallocation of all system resources
				try {
					
					EntityUtils.consume(resEntity);
				
				} catch (IOException e) {
					
					log(e);
				
				}
				
				
				
				try{
					conn.close();
				}catch(Exception ex){}
	            
	            
			}
	
	}
	
	
	
	

	private void printHeader() {
		logger.debug("\n===================HEADER=========================\n");
	
	try{
			for(org.apache.http.Header h : httppost.getAllHeaders()){
			
			if(h!=null){
				
				logger.debug("name: "+h.getName());
				logger.debug("value: "+h.getValue());
				
				
				for(org.apache.http.HeaderElement hl : h.getElements()){
					if(hl!=null){
						logger.debug("\tname: "+hl.getName());
						logger.debug("\tvalue: "+hl.getValue());
						
						if(hl.getParameters()!=null)
						for(NameValuePair nvp : hl.getParameters()){
							if(nvp!=null){
								logger.debug("\t\tname: "+nvp.getName());
								logger.debug("\t\tvalue: "+nvp.getValue());
							}
						}
					}
					
				}
			}
		}
		
	}catch(Exception e){
		
		logger.warn(e.getMessage(),e);
	}
	
	logger.debug("\n===================HEADER END======================\n");
		
	}

	private void removeAllParams(List<NameValuePair> params) {
		params.clear();
	}
	

	private void logAllParams(List<NameValuePair> params) {
		
		for(NameValuePair np: params){
			
			if(np.getName().equals("SMS_MsgTxt"))
				logger.debug(np.getName()+ "=" + np.getValue()+" Length="+np.getValue().length());
			else
				logger.debug(np.getName() + "=" + np.getValue());
			
		}
		
		logger.debug(">>>>>>>>>>>>>>>>> ||||||||||||| MEM_USAGE: " + MTProducer.getMemoryUsage()+" |||||||||||||||| <<<<<<<<<<<<<<<<<<<<<<<< ");
	}

	/**
	 * Utility method for converting Stream To String
	 * To convert the InputStream to String we use the
	 * BufferedReader.readLine() method. We iterate until the BufferedReader
	 * return null which means there's no more data to read. Each line will
	 * appended to a StringBuilder and returned as String.
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public  String convertStreamToString(InputStream is)
			throws IOException {
		
		StringBuilder sb = null;
		BufferedReader reader = null;
		
		if (is != null) {
			sb = new StringBuilder();
			String line;

			try {
				reader = new BufferedReader(
						new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				is.close();
			}
			return sb.toString();
		} else {
			return "";
		}
	}
	
	
}
