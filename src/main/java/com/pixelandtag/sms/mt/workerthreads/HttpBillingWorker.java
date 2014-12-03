package com.pixelandtag.sms.mt.workerthreads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.inmobia.util.StopWatch;
import com.pixelandtag.api.CelcomHTTPAPI;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.api.ERROR;
import com.pixelandtag.api.GenericMessage;
import com.pixelandtag.autodraw.Alarm;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.entities.URLParams;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.BillableI;
import com.pixelandtag.sms.producerthreads.BillingService;
import com.pixelandtag.sms.producerthreads.MTProducer;
import com.pixelandtag.web.triviaImpl.MechanicsS;

public class HttpBillingWorker {
	
private Logger logger = Logger.getLogger(HttpBillingWorker.class);
	
	private int http_timeout;
	private HttpClient httpsclient;
	private int retry_per_msg;
	private int pollWait;
	private DBPoolDataSource dbpds = null;
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
	private HttpPost httsppost = null;
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

	public HttpBillingWorker(int pollWait_, String name_,URLParams urlp_, String constr, HttpClient httpclient_) throws Exception{
		
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
		
		this.httpsclient = httpclient_;
		
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
			dbpds.setName(this.name+"-DS-BBL");
			dbpds.setValidatorClassName("snaq.db.Select1Validator");
			dbpds.setName("celcom-impl");
			dbpds.setDescription("Billing class connection");
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
					
					final Billable billable = BillingService.getBillable();
					
					logger.debug(":the service id in worker!::::: mtsms.getServiceID():: "+billable.toString());
					
					charge(billable);
					
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
	 * @param billable - com.pixelandtag.MTsms
	 */
	@SuppressWarnings("restriction")
	private void charge(Billable  billable){
		
		
		Connection conn = null;
		this.success  = true;
		
		setBusy(true);
		
		httsppost = new HttpPost(this.mtUrl);

		HttpEntity resEntity = null;;
		
		
		try {
			
			conn = getConn();
			
			watch.start();
			
			String usernamePassword = "CONTENT360_KE" + ":" + "4ecf#hjsan7"; // Username and password will be provided by TWSS Admin
			String encoding = null;
			sun.misc.BASE64Encoder encoder = (sun.misc.BASE64Encoder) Class.forName( "sun.misc.BASE64Encoder" ).newInstance(); 
			encoding = encoder.encode( usernamePassword.getBytes() ); 
			httsppost.setHeader("Authorization", "Basic " + encoding);
			httsppost.setHeader("SOAPAction","");
			httsppost.setHeader("Content-Type","text/xml; charset=utf-8");
			
			
			StringEntity se = new StringEntity(billable.getChargeXML(BillableI.plainchargeXML));
			httsppost.setEntity(se);
			
			 HttpResponse response = httpsclient.execute(httsppost);
			 
			 
			 final int RESP_CODE = response.getStatusLine().getStatusCode();
			 
			 resEntity = response.getEntity();
			 
			 String resp = convertStreamToString(resEntity.getContent());
			 

			 System.out.println("RESP CODE : "+RESP_CODE);
			 System.out.println("RESP XML : "+resp);
			
			logger.debug("resp: :::::::::::::::::::::::::::::RESP_CODE["+RESP_CODE+"]:::::::::::::::::::::: resp:");
			
			
			if (RESP_CODE == HttpStatus.SC_OK) {
				
				//mark as billing successful
				//remove from billing queue
				
				this.success  = resp.toUpperCase().split("<STATUS>")[1].startsWith("SUCCESS");
				
			}else if(RESP_CODE == 400){
				
				//log error
				this.success  = false;
				
			}else if(RESP_CODE == 401){
				this.success  = false;
				
				logger.error("\nUnauthorized!");
				
			}else if(RESP_CODE == 404 || RESP_CODE == 403){
				
				this.success  = false;
				
			}else if(RESP_CODE == 503){

				this.success  = false;
				
			}else{
				
				this.success = false;
				
			}
			
					
			}catch(ConnectException ce){
				
				this.success  = false;
				
				message = ce.getMessage();
				
				logger.error(message, ce);
				
				httsppost.abort();
				
			}catch(SocketTimeoutException se){
				
				this.success  = false;
				
				message = se.getMessage();
				
				httsppost.abort();
				
				logger.error(message, se);
				
			} catch (IOException ioe) {
				
				this.success  = false;
				
				message = ioe.getMessage();
				
				httsppost.abort();
				
				logger.error("\n\n==============================================================\n\n"+message+" CONNECTION TO OPERATOR FAILED. WE SHALL TRY AGAIN. Re-tries so far "+recursiveCounter+"\n\n==============================================================\n\n");
				
			} catch (Exception ioe) {
				
				this.success  = false;
				
				message = ioe.getMessage();
				
				httsppost.abort();
				
				logger.error(message, ioe);
				
			} finally{
				
				
				
				
				//postMethod.;
				//client.executeMethod(postMethod);
				
				if(!this.success){//return back to queue if we did not succeed
					
					
					//We only try 3 times recursively if we've not been poisoned and its one part of a multi-part message, we try to re-send, but no requeuing
						
					//on third try, we abort
					httsppost.abort();
					
					
				}else{
					
					recursiveCounter = 0;
					//logger.warn(message+" >>MESSAGE_NOT_SENT> "+mt.toString());
				}
				
				watch.reset();
				
				setBusy(false);
				
				logger.debug(getName()+" ::::::: finished attempt to bill via HTTP");
				
				removeAllParams(qparams);
				
				 // When HttpClient instance is no longer needed,
	            // shut down the connection manager to ensure
	            // immediate deallocation of all system resources
				try {
					
					if(resEntity!=null)
						EntityUtils.consume(resEntity);
				
				} catch (Exception e) {
					
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
			for(org.apache.http.Header h : httsppost.getAllHeaders()){
			
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
