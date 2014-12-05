package com.pixelandtag.sms.producerthreads;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.log4j.Logger;
import org.gjt.mm.mysql.Driver;

import snaq.db.ConnectionPool;
import snaq.db.DBPoolDataSource;

import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.web.triviaI.MechanicsI;
import com.pixelandtag.web.triviaImpl.MechanicsS;
import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.CelcomHTTPAPI;
import com.pixelandtag.api.MOProcessorFactory;
import com.pixelandtag.api.ServiceProcessorI;
import com.pixelandtag.api.Settings;
import com.pixelandtag.api.UnicodeFormatter;
import com.pixelandtag.autodraw.Alarm;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.entities.URLParams;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.sms.mo.MOProcessor;
import com.pixelandtag.sms.mt.ACTION;
import com.pixelandtag.sms.mt.CONTENTTYPE;
import com.inmobia.util.StopWatch;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.sms.mt.workerthreads.MTHttpSender;

/**
 * 
 * @author Timothy Mwangi Gikonyo
 * Date created: Tuesday 7th February 2012.
 * This will retrieve messages from the db and fairly distribute to 
 * http senders to send to the CMP/SMSC
 *
 */


public class MTProducer extends Thread {
	
	private static final boolean FAIR = true;
	private String constr;
	private int workers;
	private int throttle;
	private int initialConnections;
	private int maxConnections;
	private int queueSize;
	private int pollWait;
	private StopWatch watch;
	private boolean run = true;
	private URLParams urlparams;
	public static CelcomHTTPAPI celcomAPI;
	private Statement stmt = null;
	private ResultSet rs = null;
	private int x = 0;
	public static volatile MTProducer instance;
	private static Logger logger = Logger.getLogger(MTProducer.class);
	private String limitStr;
	private MOProcessor moProcessor;
	//private volatile Map<String,ServiceProcessorI> serviceMap = new HashMap<String,ServiceProcessorI>();
	//private volatile Map<String,ServiceProcessorI> runningServiceClasses = new HashMap<String,ServiceProcessorI>();
	//private volatile Map<String,Boolean> split_msg_map = new HashMap<String,Boolean>();
	/**
	 * This will hold a processor pool.
	 */
	public static volatile Map<Integer,ArrayList<ServiceProcessorI>> processor_pool = new HashMap<Integer,ArrayList<ServiceProcessorI>>();
	
	public static volatile Queue<ServiceProcessorDTO> serviceProcessors;
	
	private volatile static BlockingDeque<MTsms> mtMsgs = null;
	public volatile  BlockingQueue<MTHttpSender> httpSenderWorkers = new LinkedBlockingDeque<MTHttpSender>();
	private int idleWorkers;
	private String fr_tz;
	private String to_tz;
	private Connection conn;
	public static final String DEFLT = "DEFAULT_DEFAULT";
	private static int sentMT = 0;
	
	private static Semaphore semaphore;//a semaphore because we might need to recover from a deadlock later.. listen for when we have many recs in the db but no msg is being sent out..
	private static Semaphore uniq;
	static{
		uniq = new Semaphore(1, FAIR);
	}
	private volatile static ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
	private volatile static HttpClient httpclient;
	//private volatile static ConnectionPool pool;
	//private static Semaphore dbSemaphore;
	private static DBPoolDataSource ds;
	private Alarm alarm = new Alarm();
	
	
	
	/**
	 * Gets a free processor Thread for the processor class passed.
	 * @param mo_processor_id int
	 * @return
	 */
	public static ServiceProcessorI getFreeProcessor(int mo_processor_id){
		
		logger.info("gugamuga_processor_pool: : "+processor_pool);
		
		ArrayList<ServiceProcessorI> processorPool = processor_pool.get(mo_processor_id);
		
		//TODO add a semaphore so only one thread accesses this at a time.
		//TODO check for processorPool object bing null then, re-direct that message to an existing processor pool
	    //TODO or, edit the mo receiver so that it does not queue for disabled mo processors
		Iterator<ServiceProcessorI> it = processorPool.iterator();
		
		ServiceProcessorI proc;
		
		while(it.hasNext()){
			proc = it.next();
			if(!proc.queueFull()){
				return proc;
			}
		}
		
		return null;
	}
	
	
	public static String generateNextTxId() throws InterruptedException{
		
		try{
			
			uniq.acquire();
			
			String timestamp = String.valueOf(System.currentTimeMillis());
			
			return Settings.INMOBIA.substring(0, (19-timestamp.length())) + timestamp;//(String.valueOf(Long.MAX_VALUE).length()-timestamp.length())) + timestamp;
		
		}finally{
			
			uniq.release();
		
		}
		
	}	
	
	/**
	 * This method tries - with all might :) not to allow more than one thread to 
	 * access the MT message dequeue object.
	 * During tests, I experienced a situation where two threads got the same message
	 * from one queue.. Its like before the message was removed from the queue, another thread
	 * already took the given message...
	 * @return  com.pixelandtag.entities.MTsms
	 * @throws InterruptedException
	 * @throws NullPointerException
	 */
	public static MTsms getMTsms() throws InterruptedException, NullPointerException{
		
		try{
			
			instance.logger.debug(">>Threads waiting to retrieve message before : " + semaphore.getQueueLength() );
			
			semaphore.acquire();//now lock out everybody else!
			
			
			instance.logger.debug(">>Threads waiting to retrieve message after: " + semaphore.getQueueLength() );
			
			
			 final MTsms myMt = mtMsgs.takeFirst();//performance issues versus reliability? I choose reliability in this case :)
			 
			 //celcomAPI.beingProcessedd(myMt.getId(), true);//mark it as being processed first before returning.
			 try {
				 
				celcomAPI.markInQueue(myMt.getId());
			
			 } catch (Exception e) {
				
				 instance.logger.error("\nRootException: " + e.getMessage()+ ": " +
				 		"\n Something happenned. We were not able to mark " +
				 		"\n the message as being in queue. " +
				 		"\n To prevent another thread re-taking the message, " +
				 		"\n we've returned null to the thread/method requesting for " +
				 		"\na n MT.",e);
				 
				 return null;
			}
			 
			 return myMt;
		
		}finally{
			
			semaphore.release(); // then give way to the next thread trying to access this method..
			
		
		}
		
	}
	
	
	
	
	public String getFr_tz() {
		return fr_tz;
	}




	public String getTo_tz() {
		return to_tz;
	}




	public void setFr_tz(String fr_tz) {
		this.fr_tz = fr_tz;
	}




	public void setTo_tz(String to_tz) {
		this.to_tz = to_tz;
	}




	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

	public int getSentMT() {
		return sentMT;
	}
	
	public static void resetSentMT() {
		sentMT = 0;
	}

	public static void increaseMT() {
		sentMT++;
	}

	
	
	public void myfinalize(){
		
		try{
			
			conn.close();
		
		}catch(Exception e){
			
			log(e);
		
		}
		
		try{
			
			cm.shutdown();
		
		}catch(Exception e){
			
			log(e);
		
		}
		
		if(ds!=null)
			ds.releaseConnectionPool();
		
		
		
	}
	
	
	public static void stopApp(){
		System.out.println("Shutting down...");
		
		try{
			if(instance!=null){
				System.out.println("Shutting down...");
				instance.setRun(false);
				System.out.print("...");
				instance.waitForQueueToBecomeEmpty();
				System.out.print("...");
				instance.waitForAllWorkersToFinish();
				System.out.print("...");
				instance.myfinalize();
				System.out.println("...");
			
			}else{
				
				System.out.println("App not yet initialized or started.");
			
			}
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}catch(Error e){
			logger.error(e.getMessage(),e);
		}
		
	}
	
	
	/**
	 * This means that the workers are done with ALL the usefule elements.
	 * 
	 */
	public synchronized void waitForQueueToBecomeEmpty(){
		
		
		
		while(mtMsgs.size()>0){
			
			logger.info("MTProducer.mtMsgs.size() : "+mtMsgs.size());
			
			try {
				
				Thread.sleep(500);
				
			} catch (InterruptedException e) {
				
				log(e);
			
			}
			
		}
		
		logger.info("Queue is now empty, and all threads have been asked not to wait for elements in the queue!");
		
		notify();
		
	}
	
	
	/**
	 * Actually stops all Workers.
	 */
	public synchronized void waitForAllWorkersToFinish(){
		
		boolean finished = false;
		
		
		//First and foremost, let all threads die if they finish to process what they're processing currently.
		//We don't interrupt them still..
		for(MTHttpSender tw : httpSenderWorkers){
			if(tw.isRunning())
				tw.setRun(false);
		}
		
		//all unprocessed messages in queue are put back to the db.
		
		while(!finished){//Until all workers are idle or dead...
			
			idleWorkers = 0;
			
			for(MTHttpSender tw : httpSenderWorkers){
				
				if(tw.isRunning())
					tw.setRun(false);
				
				
				
				//might not be necessary because we already set run to false for each thread.
				//but in case we have an empty queue, then we add a poison pill that has id = -1 which forces the thread to run, then terminate
				//because we already set run to false.
				mtMsgs.addLast(new MTsms());//poison pill...the threads will swallow it and surely die.. bwahahahaha!
				
				if(!tw.isBusy()){
					idleWorkers++;
				}
				
			}
			
			try {
				
				logger.info("workers: "+workers);
				logger.info("idleWorkers: "+idleWorkers);
				
				Thread.sleep(500);
			
			} catch (InterruptedException e) {
				
				log(e);
			
			}
			
			finished = workers==idleWorkers;
			
		}
		
		
		
		logger.info("We're shutting down, we put back any unprocessed message to the db queue so that they're picked next time we run..");
		//Now, if we have a big queue of unprocessed messages, we return them back to the db (or rather set the necessary flags
		for(MTsms sms : mtMsgs){
			
			logger.info("Returned to db: "+ sms.toString());
			celcomAPI.beingProcessedd(sms.getId(), false);//nope, we're not processing it
			celcomAPI.changeQueueStatus(sms.getIdStr(), false);//and its now not in the queue
		}
		//now all messages should be put back in quque
		
		
		
		mtMsgs.clear();//Nothing is useful in the queue now. Necessary? we will find out using test.. 
		
		//queueUpdater.setRun(false);//Stop the Queue updater worker
		
		//QueueUpdater.inQueue.add(-1L);//This makes sure it runs one more time aka ("Poison-pill shutdown")
		
		logger.info("workers: "+workers);
		logger.info("idleWorkers: "+idleWorkers);
		
		
		moProcessor.setRuning(false);
		
		while(moProcessor.isBusy()){
			
			try {
				
				logger.info("WAITING FOR MO Processor to finish");
				
				Thread.sleep(500);
			
			} catch (InterruptedException e) {
				
				log(e);
			
			}
			
		}
		
		//Finalize all service processors - release resources
		
		for(Map.Entry<Integer, ArrayList<ServiceProcessorI>> entry : processor_pool.entrySet()){
			
			Iterator<ServiceProcessorI> it = entry.getValue().iterator();
			
			ServiceProcessorI proc;
			
			while(it.hasNext()){
				proc = it.next();
				proc.finalizeMe();
			}
		}
		
		
		
		
		
	}

	
	private void initWorkers() throws Exception{
		
		Thread t1;
		
		for(int i = 0; i<this.workers; i++){
			MTHttpSender worker;
			worker = new MTHttpSender(pollWait,"THREAD_WORKER_#_"+i,urlparams, this.constr, httpclient);
			t1 = new Thread(worker);
			t1.start();
			httpSenderWorkers.add(worker);
		}
				
		//First we load all processors from db
		serviceProcessors = celcomAPI.getServiceProcessors();
		
		
		ServiceProcessorI p = null;
		
		int processorThreadsStarted = 0;
		if(serviceProcessors!=null){
			for(ServiceProcessorDTO servicep : serviceProcessors){
				
				
				final int processor_threads = servicep.getThreads();
				
				ArrayList<ServiceProcessorI> processor_array = new ArrayList<ServiceProcessorI>();
				
				for(int x = 0;x<processor_threads; x++){
					p = MOProcessorFactory.getProcessorClass(servicep.getProcessorClassName(),ServiceProcessorI.class);//Create new instances of each proccessor pool item
					p.setName(x+"_"+servicep.getId()+"_"+servicep.getShortcode()+"_"+servicep.getCMP_AKeyword()+"_"+servicep.getCMP_SKeyword()+"_"+servicep.getServiceName());
					p.setInternalQueue(5);
					p.setSubscriptionText(servicep.getSubscriptionText());
					p.setUnsubscriptionText(servicep.getUnsubscriptionText());
					p.setTailTextSubscribed(servicep.getTailTextSubscribed());
					p.setTailTextNotSubecribed(servicep.getTailTextNotSubecribed());
					Thread t = new Thread(p);
					t.start();
					processor_array.add(p);
					processorThreadsStarted++;
					
				}
				
				synchronized(processor_pool){
					processor_pool.put(servicep.getId(), processor_array);
				}
				
				
			
			}
		}else{
			
			logger.error(" ************  THERE ARE NO SERVICE PROCESSORS ************ ");
		}
			
		
		//Then we start the mo processor thread(s)
		try {
			
			moProcessor = new MOProcessor(constr, "MO_PROCESSOR_1");
			
			t1 = new Thread(moProcessor);
			
			t1.start();
			
		} catch (ClassNotFoundException e1) {
			log(e1);
		} catch (InstantiationException e1) {
			log(e1);
		} catch (IllegalAccessException e1) {
			log(e1);
		}catch (Exception e1) {
			log(e1);
		}
		
		
		//wake all thread's because we're done initializing.
		for(MTHttpSender worker: httpSenderWorkers)
			worker.rezume();
		
			
	}
	
	/**
	 * Gets connection object from a pool
	 * @return java.sql.Connection
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	private Connection getConnFromDbPool() throws InterruptedException, SQLException{
		
		return ds.getConnection();
	}
	
	/**
	 * Gets the connection.
	 * If it is not closed or null, return the existing connection object,
	 * else create one and return it
	 * @return java.sql.Connection object
	 * @throws InterruptedException 
	 * @throws SQLException 
	 */
	private Connection getConnection() throws InterruptedException, SQLException {
		
		//return getConnFromDbPool();
		
		
		try{
		
			if(conn!=null || !conn.isClosed()){
				conn.setAutoCommit(true);
				return conn;
			}
			
		}catch(Exception e){
			logger.warn("we'll create a new connection!");
			//conn = ds.getConnection("root", "");
		}
		
		
		while( true ) {
			
			try {
				while ( conn==null || conn.isClosed() ) {
					
					
					
					try {
						conn = getConnFromDbPool();//MTProducer.
						logger.debug(">>>> created connection! ");
						return conn;
					} catch ( Exception e ) {
						logger.warn("Could not create connection. Reason: "+e.getMessage());
						try { Thread.sleep(500); } catch ( Exception ee ) {}
					}
				}
				//return conn;
			} catch ( Exception e ) {
				logger.warn("can't get a connection, re-trying");
				try { Thread.sleep(500); } catch ( Exception ee ) {}
			}
		}
		
	}
	

	
	/**
	 * 
	 * @param connStr_ - java.lang.String
	 * @param workers_ int the number of workers who will deal with MT messages.
	 * @param throttle_ - int the time in milliseconds to sleep before checking for new messages in the db.
	 * @param initialConnections_ - int the initial connections to be created.
	 * @param maxConnections_ int the maximum number of connections that can exist in the connection pool
	 * @param urlparams - com.pixelandtag.entities.URLParams - a set of other runtime variables
	 * @throws Exception 
	 */
	public MTProducer(String connStr_, int workers_, int throttle_, int initialConnections_, int maxConnections_, int queueSize_, int pollWait_, URLParams urlparams_) throws Exception{
		
		semaphore = new Semaphore(1, FAIR);
		
		this.throttle = throttle_;
		
		this.urlparams = urlparams_;
		
		this.constr = connStr_;
		
		this.workers = workers_;
		
		this.initialConnections = initialConnections_;
		
		this.maxConnections = maxConnections_;
		
		this.queueSize = queueSize_;
		
		this.pollWait = pollWait_;
		
		this.fr_tz = urlparams_.getSERVER_TZ(); //timezone
		
		this.to_tz = urlparams_.getCLIENT_TZ(); //timezone
		
		watch = new StopWatch();
		
		initialize();
	}
	
	
	private void initialize() throws Exception {
		
		watch.start();
		int vendor = DriverUtilities.MYSQL;
	    String driver = DriverUtilities.getDriver(vendor);
	    String host = HTTPMTSenderApp.props.getProperty("db_host");
	    String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
	    String url = DriverUtilities.makeURL(host, dbName, vendor);
	    String username = HTTPMTSenderApp.props.getProperty("db_username");
	    String password = HTTPMTSenderApp.props.getProperty("db_password");
	    
	    
	    ds = new DBPoolDataSource();
	    ds.setValidatorClassName("snaq.db.Select1Validator");
	    ds.setName("pool-ds");
	    ds.setDescription("Pooling DataSource");
	    ds.setDriverClassName("com.mysql.jdbc.Driver");
	    ds.setUrl(url);
	    ds.setUser(username);
	    ds.setPassword(password);
	    ds.setMinPool(workers);
	    ds.setMaxPool(workers);
	    ds.setMaxSize(workers);
	    ds.setIdleTimeout(3600);  // Specified in seconds.
	    
	    ds.setValidationQuery("SELECT 'Test'");
	    
	    celcomAPI = new CelcomImpl(ds);
	    
	    logger.info(url);
	    
	    try {
	  
	      if(celcomAPI==null){
	    	  logger.info("API is null");
	    	  try{
					alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Celcom Platform: SEVERE:", "Hi,\n\n API is not initialized properly\n\n  Regards");
				}catch(Exception e2){
					log(e2);
				}
	      }
	      celcomAPI.setFr_tz(fr_tz);
	      
	      celcomAPI.setTo_tz(to_tz);
	      
	      MechanicsS.to_tz  = this.to_tz;
	      
	      MechanicsS.fr_tz = this.fr_tz;
	      
	      watch.stop();
	      
	      logger.info(">< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to create the connection pool");
	      
	      watch.reset();
	      
	    
	    } catch(Exception sqle) {
	     
	    	logger.error("Error making pool: " + sqle.getMessage(),sqle);
	      
	    
	    }
	    
	    
	    if(queueSize>0){
	    	
	    	mtMsgs = new LinkedBlockingDeque<MTsms>(queueSize);
	    	
	    	limitStr = " LIMIT "+queueSize;
	   
	    }else{
	    	
	    	mtMsgs = new LinkedBlockingDeque<MTsms>();
	    
	    	limitStr = "";
	    
	    }
	    
	 
	    instance = this;
	    
	    
	    while(instance==null){
	    	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
	    }
	    
	    
	    
	    SchemeRegistry schemeRegistry = new SchemeRegistry();
    	schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
    	cm = new ThreadSafeClientConnManager(schemeRegistry);
		cm.setDefaultMaxPerRoute(this.workers);
		cm.setMaxTotal(this.workers);//http connections that are equal to the worker threads.
		
		httpclient = new DefaultHttpClient(cm);
		
	    initWorkers();
	    
	}

	
	
	public void run() {
		
		try{
			
			while(run){
				
				populateQueue();
				
				try {
					
					Thread.sleep(1000);
				
				} catch (InterruptedException e) {
					
					log(e);
				
				}finally{
					
					watch.start();
				
				}
				
			}
			
			
		}catch(OutOfMemoryError e){
			
			logger.error("NEEDS RESTART: MEM_USAGE: "+MTProducer.getMemoryUsage() +" >> "+e.getMessage(),e);
			try{
				alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Celcom Platform: SEVERE:", "Hi,\n\n We encountered a fatal exception. Please check Malaysia HTTP Sender app.\n\n  Regards");
			}catch(Exception e2){
				log(e2);
			}
		
		}finally{
			
		
		}
		
		logger.info("producer shut down!");
		
		System.exit(0);
		
		
	}


	

	/**
	 * Populates the queue
	 * @throws InterruptedException 
	 */
	private void populateQueue()  {
		
		try {
			
			x = 0;
			
			//addedToqueue = false;
			conn =  getConnection();
			
			stmt = conn.createStatement();
			
			rs  = stmt.executeQuery("SELECT * FROM `"+CelcomHTTPAPI.database+"`.`httptosend` WHERE in_outgoing_queue = 0 AND sent=0 AND billing_status in ('"+BillingStatus.NO_BILLING_REQUIRED+"' , '"+BillingStatus.INSUFFICIENT_FUNDS+"', '"+BillingStatus.SUCCESSFULLY_BILLED+"') order by `Priority` asc"+limitStr);
			
			while(rs.next()){
			
				x++;
				final MTsms mtsms = new MTsms();//reliability versus performance . reliabiliy++ balance = struck
				//if(rs.getString("CMP_Txid")!=null)// && !rs.getString("CMP_TxID").equalsIgnoreCase("NULL") )
				//	mtsms.setId(Long.parseLong(rs.getString("CMP_Txid")));
				//else
				mtsms.setId(Long.parseLong(rs.getString("id")));
				mtsms.setSms(rs.getString("SMS"));
				mtsms.setMsisdn(rs.getString("MSISDN"));
				mtsms.setType(CONTENTTYPE.get(rs.getString("Type")));
				mtsms.setSendFrom(rs.getString("SendFrom"));
				mtsms.setPrice(BigDecimal.valueOf(rs.getDouble("price")));
				mtsms.setPriority(rs.getInt("Priority"));
				mtsms.setServiceid(rs.getInt("serviceid"));
				mtsms.setTimeStamp(rs.getString("TimeStamp"));
				mtsms.setFromAddr(rs.getString("fromAddr"));
				mtsms.setCharged(rs.getString("charged"));
				mtsms.setCMP_AKeyword(rs.getString("CMP_AKeyword"));
				mtsms.setCMP_SKeyword(rs.getString("CMP_SKeyword"));
				mtsms.setAPIType(rs.getString("apiType"));
				mtsms.setNewCMP_Txid(rs.getString("newCMP_Txid"));
				mtsms.setProcessor_id(rs.getInt("mo_processorFK"));
				mtsms.setShortcode(rs.getString("fromAddr"));
				//mtsms.setMT_STATUS(rs.getString("MT_STATUS"));
				
				//if(mtsms.getNewCMP_Txid().equals(mtsms.getCMP_Txid()))
				
				if(rs.getString("SMS_DataCodingId")!=null)// && !rs.getString("apiType").equalsIgnoreCase("NULL"))
					mtsms.setSMS_DataCodingId(rs.getString("SMS_DataCodingId"));
				
				if(rs.getString("apiType")!=null)// && !rs.getString("apiType").equalsIgnoreCase("NULL"))
					mtsms.setAPIType(rs.getString("apiType"));
				
				
				//If receiver msisdn is null, then we take MSISDN as the msisdn to receive msg
				if(rs.getString("sub_r_mobtel")==null){// || rs.getString("sub_r_mobtel").equalsIgnoreCase("NULL")){
					mtsms.setSUB_R_Mobtel(rs.getString("MSISDN"));
				}else{
					mtsms.setSUB_R_Mobtel(rs.getString("sub_r_mobtel"));
				}
				
				//If c_mobtel (msisdn to bill) is null, then we take MSISDN to be the msisdn to bill 
				if(rs.getString("sub_c_mobtel")==null){// || rs.getString("sub_c_mobtel").equalsIgnoreCase("NULL")){
					mtsms.setSUB_C_Mobtel(rs.getString("MSISDN"));
				}else{
					mtsms.setSUB_C_Mobtel(rs.getString("sub_c_mobtel"));
				}
				
				
				if(rs.getString("CMP_AKeyword")!=null)// && !rs.getString("CMP_AKeyword").equalsIgnoreCase("NULL") )
					mtsms.setCMP_AKeyword(rs.getString("CMP_AKeyword"));
				
				
				if(rs.getString("CMP_SKeyword")!=null)// && !rs.getString("CMP_SKeyword").equalsIgnoreCase("NULL") )
					mtsms.setCMP_SKeyword(rs.getString("CMP_SKeyword"));
				
				
				if(rs.getString("CMP_Txid")!=null){// && !rs.getString("CMP_TxID").equalsIgnoreCase("NULL") )
					mtsms.setCMP_Txid(rs.getLong("CMP_Txid"));
					logger.debug(">>>>>>>>>>>>>>>>>>>>>>: CMP_Txid "+rs.getString("CMP_Txid"));
				}
				
				if(rs.getString("ACTION")!=null)// && !rs.getString("ACTION").equalsIgnoreCase("NULL") )
					mtsms.setAction(ACTION.get(rs.getString("ACTION")));
				
				
				mtsms.setSplit_msg(rs.getBoolean("split"));//whether to split msg or not..
				
				
				if(queueSize==0){//if we can add as many objects to the queue, then we just keep adding
					
					logger.debug("::::::::::::::rs.getBoolean(\"split\"): "+rs.getBoolean("split"));
					
					//adds to the last of the queue. 
					//We don't have limitations of the size of the queue, 
					//so its least likely for there to be no space to add an element.
					try {
						
						//Inserts the specified element at the end of this 
						//deque if it is possible to do so immediately without violating 
						//capacity restrictions, returning true upon success and false if no 
						//space is currently available. When using a capacity-restricted deque, 
						//this method is generally preferable to the addLast method, which can fail 
						//to insert an element only by throwing an exception. 
						mtMsgs.offerLast(mtsms);
						
						celcomAPI.markInQueue(mtsms.getId());//change at 11th March 2012 - I later realzed we still sending SMS twice!!!!!!
						
					}catch(Exception e){
						
						log(e);
					
					}
					
				}else{//putLast waits for queue to have empty space before adding new elements.	
					
					//We have a restriction so we have to wait until 
					//The queue has space in it to add an element.
					try {
						
						mtMsgs.putLast(mtsms);//if we've got a limit to the queue
						
						celcomAPI.markInQueue(mtsms.getId());//change at 11th March 2012 - I fucking later realzed we still sending SMS twice!!!!!!
						
						//addedToqueue = true;
						
					} catch (InterruptedException e) {
			
						log(e);
					
					}catch(Exception e){
						
						log(e);
					
					}
				}
				
				
				/*if(addedToqueue){
					try {
						
						//This method waits here for space to be available... 
						//this queue's size is 1. Untill it's cleared by the thread, the method holds here...
						QueueUpdater.inQueue.putLast(mtsms.getId());
						
					} catch (InterruptedException e) {//If this message was not added to be marked as "in queue", then it will be picked up again and sent again.. so to avoid that, we remove from current queue
						log(e);
						logger.warn("REMOVING MSG FROM QUEUE SINCE THERE IS POSIBILITY OF IT BEING SENT TWICE "+mtsms.toString());
						mtMsgs.remove(mtsms);
					}catch (Exception e){
						log(e);
						logger.warn("REMOVING MSG FROM QUEUE SINCE THERE IS POSIBILITY OF IT BEING SENT TWICE "+mtsms.toString());
						mtMsgs.remove(mtsms);
					}
				}*/
				
				
			}
			
			
			
			if(getSentMT()>0){
				
				watch.stop();
				
				logger.info("END!>>>>>>>>>>>>>>>>>>>>< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()/1000d+"")) + " seconds to clear a queue of "+getSentMT()+" messages");
			   
				watch.reset();
				
				MTProducer.resetSentMT();
			}
			
		
		} catch (NullPointerException e) {
			
			log(e);
		
		} catch (SQLException e) {
			
			log(e);
		
		} catch (InterruptedException e) {
			
			log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
			
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			
			try {
				
				if(stmt!=null)
					stmt.close();
			
			} catch (SQLException e) {
				
				log(e);
			
			}
						
		}
		
		
	}



	/**
	 * Wait
	 */
	public synchronized void pauze() {
		
		try {
			
			wait();
		
		} catch (InterruptedException e) {
			
			logger.error(e.getMessage(),e);
		}
	
	}
	
	
	
	/**
	 * notify
	 */
	public synchronized void rezume() {
		
		notify();
	
	}



	/**
	 * Logs the exception
	 * @param e - java.lang.Exception
	 */
	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}
	
	
	
	
	
	public static String getMemoryUsage() {
		long mem = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
		if ( mem>=(1024*1024*1024) )
			return ((int)mem/1024/1024/1024)+" GB";
		if ( mem>=(1024*1024) )
			return ((int)mem/1024/1024)+" MB";
		if ( mem>=(1024) )
			return ((int)mem/1024)+" KB";
		return mem+"B";
			
	}
	
	/*public static String hexToString(String hex)
	{
	    StringBuilder sb = new StringBuilder();

	    for (int count = 0; count < hex.length() - 1; count += 2)
	    {
	        String output = hex.substring(count, (count + 2));    //grab the hex in pairs

	        int decimal = Integer.parseInt(output, 16);    //convert hex to decimal

	        sb.append((char)decimal);    //convert the decimal to character
	    }

	    return sb.toString();
	}*/
	
	
	public static String hexToString(String txtInHex)
    {
        byte [] txtInByte = new byte [txtInHex.length() / 2];
        int j = 0;
        for (int i = 0; i < txtInHex.length(); i += 2)
        {
                txtInByte[j++] = Byte.parseByte(txtInHex.substring(i, i + 2), 16);
        }
        return new String(txtInByte);
    }
	
	
	public static void main(String[] args) {
		System.out.println("["+hexToString("004f006e00200031".replaceAll("00",""))+"]");
	}
	
	

}
