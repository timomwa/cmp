package com.pixelandtag.sms.producerthreads;

import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.inmobia.util.StopWatch;
import com.pixelandtag.api.CelcomHTTPAPI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.sms.core.OutgoingQueueRouter;
import com.pixelandtag.sms.mt.workerthreads.HttpBillingWorker;
import com.pixelandtag.util.FileUtils;

public class BillingService extends Thread{
	
	public static Logger logger = Logger.getLogger(BillingService.class);
	private static Semaphore semaphore = new Semaphore(1, true);;
	private static Semaphore save_Sem = new Semaphore(1, true);
	private static Semaphore uniq;
	private boolean run = true;
	public static CelcomHTTPAPI celcomAPI;
	private int idleWorkers;
	private String server_tz;
	private String client_tz;
	//private static DBPoolDataSource ds;
	//private Connection conn;
	private StopWatch watch;
	private int x = 0;
	private static int sentMT = 0;
	private int queueSize = 0;
	public volatile  BlockingQueue<HttpBillingWorker> httpSenderWorkers = new LinkedBlockingDeque<HttpBillingWorker>();
	private volatile static ConcurrentLinkedQueue<Billable> billableQ = null;
	private static int max_throttle_billing = 60000;
	private static boolean enable_biller_random_throttling=false;
	private static int min_throttle_billing = 1000;
	private static Random r = new Random();
	

	
	public static List<Long> refugees = new ArrayList<Long>();
	
	private static BillingService instance;
	private Properties log4J;
	private Properties mtsenderprop;
	
	
	static{
		uniq = new Semaphore(1, true);
	}
	
	private CMPResourceBeanRemote cmpbean;
	private  Context context = null;
	public void initEJB() throws NamingException{
			
		
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		
		String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		props.put(Context.PROVIDER_URL, "remote://"+mtsenderprop.getProperty("ejbhost")+":"+mtsenderprop.getProperty("ejbhostport"));
		props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
		props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
		props.put("jboss.naming.client.ejb.context", true);
		context = new InitialContext(props);
		cmpbean =  (CMPResourceBeanRemote) 
	   		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		logger.info("Successfully initialized EJB CMPResourceBeanRemote !!");
	 
	}
	 
	
	private int workers = 1;
	private int billables_per_batch = 100;
    
    
    public BillingService() throws Exception{
    	
    	watch = new StopWatch();
    	
    	if(queueSize<=0)
    		billableQ =  new ConcurrentLinkedQueue<Billable>();
    	else	
    		billableQ =  new ConcurrentLinkedQueue<Billable>();
    	
    	initEJB();
    	initWorkers();
    	instance = this;
    }
    
   
    public static int getRandomWaitTime(){
    	return enable_biller_random_throttling
    			? 
    			(r.nextInt(max_throttle_billing-min_throttle_billing) + min_throttle_billing) : -1;
    }
    
	private void initWorkers() throws Exception{
		log4J = FileUtils.getPropertyFile("log4j.billing.properties");
		PropertyConfigurator.configure(log4J);
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		
		server_tz = mtsenderprop.getProperty("SERVER_TZ");
		client_tz = mtsenderprop.getProperty("CLIENT_TZ");
		
		try{
			enable_biller_random_throttling = mtsenderprop.getProperty("enable_biller_random_throttling").trim().equalsIgnoreCase("true");
		}catch(Exception e){
			logger.warn(e.getMessage(),e);
		}
		try{
			min_throttle_billing = Integer.valueOf(mtsenderprop.getProperty("min_throttle_billing"));
		}catch(Exception e){
			logger.warn(e.getMessage(),e);
		}
		try{
			max_throttle_billing = Integer.valueOf(mtsenderprop.getProperty("max_throttle_billing"));
		}catch(Exception e){
			logger.warn(e.getMessage(),e);
		}
		
		try{
			billables_per_batch = Integer.valueOf(mtsenderprop.getProperty("billables_per_batch"));
		}catch(Exception e){
			logger.warn(e.getMessage(),e);
		}
		
		try{
			workers = Integer.valueOf(mtsenderprop.getProperty("billing_workers"));
		}catch(Exception e){
			logger.warn(e.getMessage(),e);
		}
		
		
					
		Thread t1;
		for(int i = 0; i<this.workers ; i++){
			HttpBillingWorker worker;
			worker = new HttpBillingWorker(server_tz, client_tz, "THREAD_WORKER_#_"+i, cmpbean);
			t1 = new Thread(worker);
			t1.start();
			httpSenderWorkers.add(worker);
		}
		//wake all thread's because we're done initializing.
		for(HttpBillingWorker worker: httpSenderWorkers)
			worker.rezume();
	}
	

	public static Billable getBillable() {
		
		try{
			
			logger.debug(">>Threads waiting to retrieve message before : " + semaphore.getQueueLength() );
			
			semaphore.acquire();//now lock out everybody else!
			
			logger.debug(">>Threads waiting to retrieve message after: " + semaphore.getQueueLength() );
			logger.debug("SIZE OF QUEUE ? "+billableQ.size());
			
			 final Billable billable = billableQ.poll();//performance issues versus reliability? I choose reliability in this case :)
			 
			 try {
				 
				 if(billable!=null)
					 logger.debug("billable.getId():  "+billable.getId());
			
			 } catch (Exception e) {
				
				 logger.error("\nRootException: " + e.getMessage()+ ": " +
				 		"\n Something happenned. We were not able to mark " +
				 		"\n the message as being in queue. " +
				 		"\n To prevent another thread re-taking the object, " +
				 		"\n we've returned null to the thread/method requesting for " +
				 		"\na n MT.",e);
				 
				 return null;
			}
			 
			 return billable;
		
		} catch (Exception e1) {
			logger.error(e1.getMessage(),e1);			
			return null;
		}finally{
			
			semaphore.release(); // then give way to the next thread trying to access this method..
			
		
		}
	}
	
	
	
	public void run() {
		
		try{
			
			while(run){
				
				
				try{
					
					if(billableQ.size()<=0){
						populateQueue();
					}else{
						try{
							Thread.sleep(10000);//Sleep for 10 seconds
						}catch(InterruptedException esp){
							logger.error(esp.getMessage(),esp);
						}
					}
					
				}catch(Exception e){
					
					logger.error(e.getMessage(),e);
				}
				
				try {
					
					Thread.sleep(1000);
				
				} catch (InterruptedException e) {
					
					log(e);
				
				}finally{
					
					watch.start();
				
				}
				
			}
			
			
		}catch(OutOfMemoryError e){
			
			logger.error("NEEDS RESTART: MEM_USAGE: "+OutgoingQueueRouter.getMemoryUsage() +" >> "+e.getMessage(),e);
			
		
		}finally{
			
			myfinalize();
			
		}
		
		logger.info("producer shut down!");
		
		System.exit(0);
		
		
	}
	
	
	
	private void populateQueue() {
		
		try {
			
			x = 0;
			
			
			List<Billable> billables = cmpbean.getBillable(billables_per_batch);
			
			
			
			for(Billable billable : billables){
				
				logger.info("\n\n\t\tbillable.toString():"+billable.toString()+"\n");
				
			
				x++;
				
				
				if(queueSize==0){//if we can add as many objects to the queue, then we just keep adding
					
					
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
						billableQ.offer(billable);
						
						billable.setIn_outgoing_queue(1L);
						cmpbean.saveOrUpdate(billable);
						//celcomAPI.markInQueue(mtsms.getId());//change at 11th March 2012 - I later realzed we still sending SMS twice!!!!!!
						
					}catch(Exception e){
						
						log(e);
					
					}
					
				}else{//putLast waits for queue to have empty space before adding new elements.	
					
					//We have a restriction so we have to wait until 
					//The queue has space in it to add an element.
					try {
						
						billableQ.offer(billable);//if we've got a limit to the queue
						
						billable.setIn_outgoing_queue(1L);
						cmpbean.saveOrUpdate(billable);
						//celcomAPI.markInQueue(mtsms.getId());//change at 11th March 2012 - I fucking later realzed we still sending SMS twice!!!!!!
						
						//addedToqueue = true;
						
					} catch (InterruptedException e) {
			
						log(e);
					
					}catch(Exception e){
						
						log(e);
					
					}
				}
				
				increaseMT();
				
				
			}
			
			
			
			if(getSentMT()>0){
				
				watch.stop();
				
				logger.info("END!>>>>>>>>>>>>>>>>>>>>< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()/1000d+"")) + " seconds to log a queue of "+getSentMT()+" messages");
			   
				watch.reset();
				
				resetSentMT();
			}
			
			
			Thread.sleep(10);//Sleep 100 ms
		
		} catch (NullPointerException e) {
			
			log(e);
		} catch (Exception e) {
			
			log(e);
		
		}finally{
			
						
		}
		
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


	/**
	 * Logs the exception
	 * @param e - java.lang.Exception
	 */
	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}

	public static void main(String[] args) {
		
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() { 
		    	
		    	System.out.println("SHUTTING DOWN!");
		    	BillingService.stopApp();
		    	
		    }
		});
		
		try {
			BillingService billingserv = new BillingService();
			billingserv.start();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	protected static void stopApp() {
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

	private void waitForAllWorkersToFinish() {
			
		boolean finished = false;
		
		
		//First and foremost, let all threads die if they finish to process what they're processing currently.
		//We don't interrupt them still..
		for(HttpBillingWorker tw : httpSenderWorkers){
			if(tw.isRunning()){
				tw.setRun(false);
				tw.finalizeMe();
			}
		}
		
		//all unprocessed messages in queue are put back to the db.
		
		while(!finished){//Until all workers are idle or dead...
			
			idleWorkers = 0;
			
			for(HttpBillingWorker tw : httpSenderWorkers){
				
				if(tw.isRunning()){
					tw.setRun(false);
					tw.finalizeMe();
				}
				
				
				//might not be necessary because we already set run to false for each thread.
				//but in case we have an empty queue, then we add a poison pill that has id = -1 which forces the thread to run, then terminate
				//because we already set run to false.
				billableQ.offer(new Billable());//poison pill...the threads will swallow it and surely die.. bwahahahaha!
				
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
		for(Billable sms : billableQ){
			sms.setIn_outgoing_queue(0L);//and its now not in the queue
			sms.setProcessed(0L);//nope, we're not processing it
			logger.info("Returned to db: "+ sms.toString());
			try {
				sms = cmpbean.saveOrUpdate(sms);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		//now all messages should be put back in quque
		
		
		
		billableQ.clear();//Nothing is useful in the queue now. Necessary? we will find out using test.. 
		logger.info("workers: "+workers);
		logger.info("idleWorkers: "+idleWorkers);
		
		
		
	}

	private void setRun(boolean b) {
		this.run = b;
		
	}

	private void waitForQueueToBecomeEmpty() {
		
		while(billableQ.size()>0){
			
			logger.info("MTProducer.mtMsgs.size() : "+billableQ.size());
			
			try {
				
				Thread.sleep(500);
				
			} catch (InterruptedException e) {
				
				log(e);
			
			}
			
		}
		
		logger.info("Queue is now empty, and all threads have been asked not to wait for elements in the queue!");
		
		notify();
		
	}

	

/*
	*//**
	 * Gets connection object from a pool
	 * @return java.sql.Connection
	 * @throws InterruptedException
	 * @throws SQLException
	 *//*
	private Connection getConnFromDbPool() throws InterruptedException, SQLException{
		
		return ds.getConnection();
	}
	
	*//**
	 * Gets the connection.
	 * If it is not closed or null, return the existing connection object,
	 * else create one and return it
	 * @return java.sql.Connection object
	 * @throws InterruptedException 
	 * @throws SQLException 
	 *//*
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
		
	}*/
	
	
	
	public void myfinalize(){
		
		try{
			
			if(context!=null)
				context.close();
		
		}catch(NamingException e){
			
			log(e);
		
		}catch (RejectedExecutionException e1) {
			//https://issues.jboss.org/browse/EJBCLIENT-98
		}catch (Exception e1) {
			logger.error(e1.getMessage(),e1);
		}
		
	}
}
