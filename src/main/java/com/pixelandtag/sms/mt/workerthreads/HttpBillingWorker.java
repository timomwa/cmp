package com.pixelandtag.sms.mt.workerthreads;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import com.inmobia.util.StopWatch;
import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.autodraw.Alarm;
import com.pixelandtag.entities.URLParams;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.BillableI;
import com.pixelandtag.sms.producerthreads.BillingService;
import com.pixelandtag.sms.producerthreads.MTProducer;

public class HttpBillingWorker implements Runnable {
	
	private static Logger logger = Logger.getLogger(HttpBillingWorker.class);
	
	private HttpClient httpsclient;
	private int retry_per_msg = 1;
	private int pollWait;
	//private DBPoolDataSource dbpds = null;
	private StopWatch watch;
	private boolean run = true;
	private boolean finished = false;
	private String name;
	private boolean busy = false;
	private List<NameValuePair> qparams = null;
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
	private static final ThreadLocal<Session> session = new ThreadLocal<Session>();
	private static String cannonicalPath = "";
	private static File ft = new File(".");
	static{
		try {
			cannonicalPath = ft.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static File f = new File(cannonicalPath+ System.getProperty("file.separator") +"hibernate.cfg.xml");
	private static  SessionFactory sessionFactory = new AnnotationConfiguration().configure(f).buildSessionFactory();
	
	public static Session getSession() {
		Session session = (Session) HttpBillingWorker.session.get();
		if (session == null) {
			session = sessionFactory.openSession();
			HttpBillingWorker.session.set(session);
			getSession().setFlushMode(FlushMode.AUTO);
		}
		return session;
	}



	/**
	 * saves and commits
	 * @param t
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T find(Class<T> entityClass, Long id) {
		Query query = getSession().createQuery("from " + entityClass.getSimpleName() + " WHERE id =:id ").setParameter("id", id);
		if(query.list().size()>0)
			return (T) query.list().get(0);
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> T find(Class<T> entityClass, String param_name, Object value) {
		Query query = getSession().createQuery("from " + entityClass.getSimpleName() + " WHERE "+param_name+" =:"+param_name+" ").setParameter(param_name, value);
		if(query.list().size()>0)
			return (T) query.list().get(0);
		return null;
	}
	
	/**
	 * saves and commits
	 * @param t
	 * @return
	 */
	public static  <T> T saveOrUpdate(T t) {
		try {
			
			getSession().saveOrUpdate(t);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			try{
				getSession().getTransaction().rollback();
			}catch(Exception e1){}
		}finally{
		}
		
		return t;
	}
	
	protected void begin() {
		getSession().beginTransaction();
	}

	
	public void commitMe(){
		this.commit();
	}
	protected void commit() {
		getSession().getTransaction().commit();
	}

	protected void rollback() {

		try {
			getSession().getTransaction().rollback();
		} catch (HibernateException e) {
			logger.warn("Cannot rollback", e);
		}
		try {
			getSession().close();
		} catch (HibernateException e) {
			logger.warn("Cannot close", e);
		}
		HttpBillingWorker.session.set(null);
	}

	public static void close() {
		getSession().close();
		HttpBillingWorker.session.set(null);
	}
	
	
	
	private String mtUrl = "https://41.223.58.133:8443/ChargingServiceFlowWeb/sca/ChargingExport1";



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

	public HttpBillingWorker( String name_,HttpClient httpclient_) throws Exception{
		
		this.watch = new StopWatch();
		
		this.name = name_;
		
		watch.start();
		
		this.httpsclient = httpclient_;
		
		qparams = new LinkedList<NameValuePair>();
		
		/*int vendor = DriverUtilities.MYSQL;
	    String driver = DriverUtilities.getDriver(vendor);
	    String host = HTTPMTSenderApp.props.getProperty("db_host");
	    String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
	    String url = DriverUtilities.makeURL(host, dbName, vendor);
	    String username = HTTPMTSenderApp.props.getProperty("db_username");
	    String password = HTTPMTSenderApp.props.getProperty("db_password");*/
	    
	    
	    try {
			
		    
			/*dbpds = new DBPoolDataSource();
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
			dbpds.setValidationQuery("SELECT 'test'");*/
			
			//logger.info("Initialized db pool ok!");
			
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
		
		}finally{}
  
		
	
	}
	
	
	
	/*public Connection getConn() {
		
		try {
			
			return dbpds.getConnection();
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
			return null;
		
		}finally{
		
		}
	}*/
	

	public void run() {
		
		try{
			
			pauze();//wait while producer gets ready
			
			
			watch.stop();
			
			logger.info(getName()+" STARTED AFTER :::::RELEASED_BY_PRODUCER after "+(Double.parseDouble(watch.elapsedTime(TimeUnit.MILLISECONDS)+"")) + " mili-seconds");
			
			watch.reset();
			
			StringBuffer sb = new StringBuffer();
			
			while(run){
				
				
				
				try {
					
					final Billable billable = BillingService.getBillable();
					
					begin();
					
					logger.debug(":the service id in worker!::::: mtsms.getServiceID():: "+billable.toString());
					
					charge(billable);
					
					close();
					
				}catch (Exception e){
					
					log(e);
					
				}finally{
					
					setSms_idx(0);
				
				}
				
			}
			
			setFinished(true);
			
			setBusy(false);
			
			logger.info(getName()+": worker shut down safely!");
		
		}catch(OutOfMemoryError e){
			
			logger.fatal("NEEDS RESTART: MEM_USAGE: "+MTProducer.getMemoryUsage() +" >> "+e.getMessage(),e);
			//Hasn't happened so far during testing. Not expected to happen during runtime
			//please send alarm
			
			/*Connection conn = null;
			try{
				conn = getConn();
				alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: SEVERE:", "Hi,\n\n We encountered a fatal exception. Please check Malaysia HTTP Sender app.\n\n  Regards");
			}catch(Exception e2){
				log(e2);
			}finally{
				try{
					conn.close();
				}catch(Exception e4){}
			}*/
			
		}finally{
			
			try{
				//this.conn.close();
			}catch(Exception e){}
			
			
			try{
				commit();
			}catch(Exception e){}
			try{
				close();
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
		
		//Connection conn = null;
		this.success  = true;
		
		setBusy(true);
		
		httsppost = new HttpPost(this.mtUrl);

		HttpEntity resEntity = null;;
		
		
		try {
			
			//conn = getConn();
			
			
			
			String usernamePassword = "CONTENT360_KE" + ":" + "4ecf#hjsan7"; // Username and password will be provided by TWSS Admin
			String encoding = null;
			sun.misc.BASE64Encoder encoder = (sun.misc.BASE64Encoder) Class.forName( "sun.misc.BASE64Encoder" ).newInstance(); 
			encoding = encoder.encode( usernamePassword.getBytes() ); 
			httsppost.setHeader("Authorization", "Basic " + encoding);
			httsppost.setHeader("SOAPAction","");
			httsppost.setHeader("Content-Type","text/xml; charset=utf-8");
			
			
			StringEntity se = new StringEntity(billable.getChargeXML(BillableI.plainchargeXML));
			httsppost.setEntity(se);
			
			
			watch.start();
			 
			HttpResponse response = httpsclient.execute(httsppost);
			watch.stop();
			logger.info("billable.getMsisdn()="+billable.getMsisdn()+" :::: Shortcode="+billable.getShortcode()+" :::< . >< . >< . >< . >< . it took "+(Double.valueOf(watch.elapsedTime(TimeUnit.MILLISECONDS)/1000d)) + " seconds to bill via HTTP");
				
			 
			 final int RESP_CODE = response.getStatusLine().getStatusCode();
			 
			 resEntity = response.getEntity();
			 
			 String resp = convertStreamToString(resEntity.getContent());
			
			 
				

			 logger.debug("RESP CODE : "+RESP_CODE);
			 logger.debug("RESP XML : "+resp);
			 
			 billable.setResp_status_code(String.valueOf(RESP_CODE));
			
			
			billable.setProcessed(1L);
			
			if (RESP_CODE == HttpStatus.SC_OK) {
				
				
				billable.setRetry_count(billable.getRetry_count()+1);
				//remove from billing queue
				
				this.success  = resp.toUpperCase().split("<STATUS>")[1].startsWith("SUCCESS");
				billable.setSuccess(this.success );
				
				
				
				if(!this.success){
					
					String err = getErrorCode(resp);
					logger.info("resp: :::::::::::::::::::::::::::::ERROR_CODE["+err+"]:::::::::::::::::::::: resp:");
					logger.info("resp: :::::::::::::::::::::::::::::ERROR_MESSAGE["+getErrorMessage(resp)+"]:::::::::::::::::::::: resp:");
					
				}else{
					
					billable.setResp_status_code("Success");
					logger.info("resp: :::::::::::::::::::::::::::::ERROR_CODE["+billable.isProcessed()+"]:::::::::::::::::::::: resp:");
				}
				
								
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
				
				watch.reset();
				
				setBusy(false);
				
				logger.info(getName()+" ::::::: finished attempt to bill via HTTP");
				
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
					
					billable.setProcessed(1L);
					billable.setIn_outgoing_queue(0L);
					getSession().saveOrUpdate(billable);
					
					if(billable.isSuccess() ||  "Success".equals(billable.getResp_status_code()) )
						updateMessageInQueue(billable.getCp_tx_id(),BillingStatus.SUCCESSFULLY_BILLED);
					if("TWSS_101".equals(billable.getResp_status_code()) || "TWSS_114".equals(billable.getResp_status_code()) || "TWSS_101".equals(billable.getResp_status_code()))
						updateMessageInQueue(billable.getCp_tx_id(),BillingStatus.BILLING_FAILED_PERMANENTLY);
				
					
					if(!this.success){//return back to queue if we did not succeed
						//We only try 3 times recursively if we've not been poisoned and its one part of a multi-part message, we try to re-send, but no requeuing
							
						//on third try, we abort
						httsppost.abort();
						
						
					}else{
						
						recursiveCounter = 0;
						//logger.warn(message+" >>MESSAGE_NOT_SENT> "+mt.toString());
					}
					
					
					
					
				//}catch(HibernateException e){
					//logger.warn("FAILED TO MARK AS BILLED READY FOR SENDING");
					
					/*try{
						
						
						if(billable.isSuccess() ||  "Success".equals(billable.getResp_status_code()) ){
							int max_retries = 10;
							int count = 0;
							boolean success = false;
							
							while(!success && count<max_retries){

								logger.info("************************** retrying ("+count+"/"+max_retries+") ");
								try{
									
									try{
										getSession().beginTransaction();
									}catch(Exception eE){}
									
									getSession().saveOrUpdate(billable);
									
									try{
										getSession().getTransaction().commit();
									}catch(Exception eE){}
									
								}catch(Exception ev){
									try{
										getSession().getTransaction().rollback();
									}catch(Exception eE){}
								}finally{
									try{
										Thread.sleep(1000);
									}catch(Exception exx){}
									
									count++;
									try{
										commit();
									}catch(Exception eE){}
									
								
						
								}
							}
						}*/
					//}catch(Exception ex){
					//	logger.error("BILLED SOMEBODY BUT DIDN'T DELIVER CONTENT!!! "+ex.getMessage(), ex);
					//}
					
				}catch(Exception e){
					logger.error(e.getMessage(),e);
				}
				
				watch.reset();
				try{
					commit();
				}catch(Exception e){}
			}
	
	}
	
	
	
	

	
	private  String getErrorMessage(String resp) {
		int start = resp.indexOf("<errorMessage>")+"<errorMessage>".length();
		int end  = resp.indexOf("</errorMessage>");
		return resp.substring(start, end);
	}
	private String getErrorCode(String resp) {
		int start = resp.indexOf("<errorCode>")+"<errorCode>".length();
		int end  = resp.indexOf("</errorCode>");
		return resp.substring(start, end);
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
	
	
	
	public static void updateMessageInQueue(long cp_tx_id, BillingStatus billstatus) throws HibernateException{
		Query qry = getSession().createSQLQuery("UPDATE httptosend set priority=:priority, charged=:charged, billing_status=:billing_status WHERE CMP_TxID=:CMP_TxID ")
		.setParameter("priority", billstatus.equals(BillingStatus.SUCCESSFULLY_BILLED) ? 0 :  3)
		.setParameter("charged", billstatus.equals(BillingStatus.SUCCESSFULLY_BILLED) ? 1 :  0)
		.setParameter("billing_status", billstatus.toString())
		.setParameter("CMP_TxID", cp_tx_id);
		int success = qry.executeUpdate();
		
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
