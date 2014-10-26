package com.pixelandtag.mms.producerthreads;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.pixelandtag.api.ERROR;
import com.pixelandtag.api.Settings;
import com.pixelandtag.autoAlarms.HealthCheck;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.mms.api.MM7Api;
import com.pixelandtag.mms.api.MMS;
import com.pixelandtag.mms.apiImpl.MMSApiImpl;
import com.pixelandtag.mms.workerthreads.MMSSender;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.util.StopWatch;
import com.pixelandtag.web.triviaI.MechanicsI;

import snaq.db.DBPoolDataSource;

/**
 * Gets the MMS messages to be sent and provides for the worker threads.
 * @author Timothy Mwangi
 *
 */
public class MMSProducer extends Thread  {
	
	private static volatile MMSProducer instance;
	private final String DATABASE = Settings.database;
	private final boolean FAIR = true;
	private StopWatch watch = null;
	private long throttle = 50;
	private long workers;
	private long fetchInterval = 100;//fetch for mms every 100ms
	private static DBPoolDataSource ds;
	private static Semaphore semaphore;
	private boolean run = true;
	private static MM7Api mm7API;
	private Logger logger = Logger.getLogger(MMSProducer.class);
	private static volatile BlockingDeque<MMS> mtmmsq = null;
	public static volatile BlockingQueue<MMSSender> mm7SenderWorkers = new LinkedBlockingDeque<MMSSender>();
	private String ws_endpoint;;
	private String server_timezone;
	private String client_timezone;
	private int QUEUE_LIMIT = 1000;
	private HealthCheck hc = null;
	
	
	
	
	public static MMS getMTMMS() throws InterruptedException, NullPointerException{
		
		try{
			
			
			semaphore.acquire();//now lock out everybody else!
			
			instance.logger.info(">>Threads waiting to retrieve message before : " + semaphore.getQueueLength() );
			
			//instance.logger.info(">>Threads waiting to retrieve message after: " + semaphore.getQueueLength() );
			
			 instance.logger.info("::::::::::::::::::::::::::::SIZE BEFORE TAKE>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+MMSProducer.mtmmsq.size());
			 final MMS mymms = mtmmsq.takeFirst();//performance issues versus reliability? I choose reliability in this case :)
			 instance.logger.info("::::::::::::::::::::::::::::SIZE AFTER TAKE>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ MMSProducer.mtmmsq.size());
			 //celcomAPI.beingProcessedd(myMt.getId(), true);//mark it as being processed first before returning.
			 try {
				 
				 mm7API.toggleInProcessingQueue(mymms.getId(), true);
			
			 } catch (Exception e) {
				
				 instance.logger.error("\nRootException: " + e.getMessage()+ ": " +
				 		"\nSomething happenned. We were not able to mark " +
				 		"\nthe message as being in queue. " +
				 		"\nTo prevent another thread re-taking the message, " +
				 		"\nwe've returned null to the method requesting for " +
				 		"\nan MT.",e);
				 
				 return null;
			}
			 
			 return mymms;
		
		}finally{
			
			semaphore.release(); // then give way to the next thread trying to access this method..
			
		
		}
		
	}

	
	
	/**
	 * 
	 * @param workers int - the number of mm7 sender workers
	 * @param throttle - int the time in milliseconds that each thread waits before sending the next mm7 request
	 * @param fetchInterval - int the time in milliseconds that the producer waits before fetching any more mms to be distributed to the workers
	 * @param ws_endpoint - String - the web service endpoint url
	 * @param server_timezone - the server timezone - in miami it is -05:00
	 * @param client_timezone - the timezone for where the subscribers use the application for Malaysia it is +08:00
	 * @throws Exception - exception
	 */
	public MMSProducer(long workers, long throttle, long fetchInterval, String ws_endpoint, String server_timezone, String client_timezone) throws Exception{
		
		setDaemon(true);//this the real daemon
		
		watch = new StopWatch();
		
		watch.start();
		
		this.ws_endpoint = ws_endpoint;
		
		this.workers = workers;
		
		semaphore = new Semaphore(1, FAIR);
		
		this.throttle = throttle;
		
		this.fetchInterval = fetchInterval;
		
		this.server_timezone = server_timezone;
		
		this.client_timezone = client_timezone;
		
		int vendor = DriverUtilities.MYSQL;
	    String driver = DriverUtilities.getDriver(vendor);
	    String host = "db";
	    String dbName =HTTPMTSenderApp.props.getProperty("DATABASE");
	    String url = DriverUtilities.makeURL(host, dbName, vendor);
	    
	    
	    ds = new DBPoolDataSource();
	    ds.setName("mm7_conn");
	    ds.setDescription("Datasource for MMS sending app");
	    ds.setDriverClassName(driver);
	    ds.setUrl(url);
	    ds.setUser("root");
	    ds.setPassword("");
	    ds.setMinPool(5);
	    ds.setMaxPool(Integer.parseInt((workers+2)+""));
	    ds.setMaxSize(Integer.parseInt((workers+2)+""));
	    ds.setIdleTimeout(36000);  // Specified in seconds. = 1 hour.
	    ds.setValidationQuery("SELECT 'Test'");
	    
	    mm7API = new MMSApiImpl(ds);
	    
	    mm7API.setFr_tz(this.server_timezone);
	    
	    mm7API.setTo_tz(this.client_timezone);
	    
	    mtmmsq = new LinkedBlockingDeque<MMS>(QUEUE_LIMIT );
	    
	    instance = this;
	    
	    initializeThreads();
	    
	    watch.stop();
	    logger.info("it took "+watch.elapsedMillis() + " milliseconds to initialize.");
	    
	    
	    hc = new HealthCheck(Executors.newSingleThreadScheduledExecutor(), 15, TimeUnit.MINUTES);
		hc.call();
		
	}
	
	
	
	public boolean isRun() {
		return run;
	}



	public void setRun(boolean run) {
		this.run = run;
	}



	public static void  stopApp(){
		instance.logger.info("................Shutting down now....................");
		instance.setRun(false);
		instance.logger.info("...............waiting for workers to finish....................\n\n");
		instance.waitForWorkersToFinish();
		instance.logger.info("................workers finished!...................\n\n");
		instance.logger.info("................Draining queue of "+(MMSProducer.mtmmsq.size())+" message(s) ...................\n");
		instance.drainQueue();
		instance.logger.info("................queue drained!...................\n\n");
		instance.logger.info("................workers finished!...................\n\n");
		System.out.println("||||||||||<<<<<<<<<<<<<<<<<<<   Shutting down complete >>>>>>>>>>>>>>>||||||||||||||");
		
	}
	
	/**
	 * Drain the queue
	 */
	private void drainQueue() {
		for(MMS mms : mtmmsq){
			mm7API.toggleInProcessingQueue(mms.getId(), false);
			mtmmsq.remove(mms);
		}
		mtmmsq.clear();
	}



	public synchronized void waitForWorkersToFinish(){
		
		boolean finished = false;
		
		while(!finished){
			
			int idleWorkers = 0;
			
			for(MMSSender worker : mm7SenderWorkers){
				if(worker.isRunning()){
					worker.setRun(false);
				}
				mtmmsq.addLast(new MMS());//poison pill...the threads will swallow it and surely die.. bwahahahaha!
				if(!worker.isBusy()){
					idleWorkers++;
				}
			}
			
			try {
				logger.debug("workers: "+workers);
				logger.debug("idleWorkers: "+idleWorkers);
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log(e);
			}
			
			finished = workers==idleWorkers;
		
		}
		
		notify();
		
	}
	

	private void initializeThreads() throws Exception {

		
		MMSSender sender;
		Thread thread;
		
		for(int i=0; i<this.workers; i++){
			sender = new MMSSender("mm7Sender_#"+i,this.ws_endpoint, this.throttle);
			thread = new Thread(sender);
			thread.start();
			mm7SenderWorkers.add(sender);
		}
		
		//wake all thread's because we're done initializing.
		for(MMSSender worker : mm7SenderWorkers)
			worker.rezume();
		
		
	}


	public void run() {

		while(run){
			
			
			try{
				
				fetchMMSToSendThenSend(QUEUE_LIMIT);
				
				try {
					
					Thread.sleep(this.fetchInterval);
				
				} catch (InterruptedException e) {
					
					log(e);
				
				}
				
			}catch(Exception e){
				
			}
			
		}

	}
	
	private void fetchMMSToSendThenSend(int limit) {
		
		
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		MMS mtmms = null;
		
		try{
			
			conn = getConnection();
				
			//pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`mms_to_send` WHERE sent = 0 AND inProcessingQueue=0  AND paidFor=1 AND timeStampOfInsertion    between timestamp(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY)) AND ((CURRENT_DATE + INTERVAL 1 DAY) - INTERVAL 1 SECOND)  ORDER BY timeStampOfInsertion asc"+(limit>0 ? (" LIMIT "+limit) : ("")),Statement.RETURN_GENERATED_KEYS);
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`mms_to_send` WHERE sent = 0 AND inProcessingQueue=0  AND paidFor=1 AND (TIMESTAMPDIFF(HOUR,timeStampOfInsertion,CURRENT_TIMESTAMP)<=24) ORDER BY timeStampOfInsertion asc"+(limit>0 ? (" LIMIT "+limit) : ("")),Statement.RETURN_GENERATED_KEYS);
			rs = pstmt.executeQuery();
			
			
			dbloop:
			
			while(rs.next()){
				
				
				if(!run){//if we've been called to shutdown
					logger.info("We've been instructed not to run, exiting from db loop!");
					break dbloop;
				}
				
				mtmms = new MMS();
				mtmms.setId(rs.getString("id"));
				mtmms.setCMP_Txid(rs.getString("id"));
				mtmms.setTransactionID(rs.getString("id"));
				mtmms.setMsisdn(rs.getString("msisdn"));
				mtmms.setSubject(rs.getString("subject"));
				mtmms.setMms_text(rs.getString("mms_text"));
				mtmms.setMediaPath(rs.getString("media_path"));
				mtmms.setServiceid(rs.getInt("serviceid"));
				mtmms.setShortcode(rs.getString("shortcode"));
				mtmms.setServicecode(rs.getString("servicecode"));
				mtmms.setLinked_id(rs.getString("linked_id"));
				try{
					mtmms.setEariest_delivery_time(rs.getString("earliest_delivery_time"));
				}catch(Exception e){
					mtmms.setEariest_delivery_time("0000");
				}
				mtmms.setExpiry_date(rs.getString("expiry_date"));
				mtmms.setDistribution_indicator(String.valueOf(rs.getBoolean("distributable")));
				mtmms.setTimeStampOfInsertion(rs.getString("timeStampOfInsertion"));
				mtmms.setInprocessingQueue(rs.getBoolean("inProcessingQueue"));
				mtmms.setSent(rs.getBoolean("sent"));
				mtmms.setPaidFor(rs.getInt("paidFor")==1);
				mtmms.setBillingStatus(ERROR.get(rs.getString("billingStatus")));
				mtmms.setWait_for_txId(rs.getString("tx_id_waiting_to_succeed_before_sending"));
				mtmms.setDlrArriveTime(rs.getString("dlrReceived"));
				
				if(limit>0){
					
					try{

						mtmmsq.putLast(mtmms);
					
					}catch(Exception e){
						log(e);
					}
				
				}else{
					
					try {
						
						mtmmsq.putLast(mtmms);
						
					} catch (InterruptedException e) {
						log(e);
					}catch(Exception e){
						log(e);
					}
					
				}
				
			}
		
		
		}catch(SQLException e){
			
			log(e);
		
		}finally{
			
			
			try {
				if(rs!=null)
					rs.close();
			} catch (SQLException e) {
				log(e);
			}
			
			
			try {
				if(pstmt!=null)
					pstmt.close();
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			try {
				if(conn!=null)
					conn.close();
			} catch (SQLException e) {
				log(e);
			}
			
		}
		
	}


	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}


	public void myfinalize(){
		
		try{
			if(hc!=null)
				hc.finalizeME();
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}
		
		
		
		try{
			
			if(ds!=null)
				ds.releaseConnectionPool();
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}
		
		
	
	}
	
	
	
	
	/**
	 * Gets the connection.
	 * If it is not closed or null, return the existing connection object,
	 * else create one and return it
	 * @return java.sql.Connection object
	 * @throws InterruptedException 
	 * @throws SQLException 
	 */
	private Connection getConnection() {
		
		Connection conn_ = null;
		
		while( true ) {
			
			try {
				while ( conn_==null || conn_.isClosed() ) {
					try {
						conn_ = getConnFromDbPool();
						logger.debug(">>>> created connection! ");
						return conn_;
					} catch ( Exception e ) {
						logger.warn("Could not create connection. Reason: "+e.getMessage());
						try { Thread.sleep(500); } catch ( Exception ee ) {}
					}
				}
				return conn_;
			} catch ( Exception e ) {
				logger.warn("can't get a connection, re-trying");
				try { Thread.sleep(500); } catch ( Exception ee ) {}
			}
		}
		
	}
	
	
	public static Connection getConnFromDbPool() throws InterruptedException, SQLException{
		
		try{
			
			return ds.getConnection();
						
		}finally{
			
			
		}
		
	}

}
