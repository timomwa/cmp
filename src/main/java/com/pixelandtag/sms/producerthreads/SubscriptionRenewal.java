package com.pixelandtag.sms.producerthreads;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.log4j.Logger;

import com.inmobia.util.StopWatch;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.entities.MOProcessorE;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.sms.mt.workerthreads.SubscriptionBillingWorker;
import com.pixelandtag.util.FileUtils;

public class SubscriptionRenewal extends  Thread {
	public static Logger logger = Logger.getLogger(SubscriptionRenewal.class);
	private static Semaphore semaphore = null;
	private static Semaphore serialsemaphore = null;
	private StopWatch watch = new StopWatch();;
	private boolean run = true;
	private CMPResourceBeanRemote cmpbean;
	private SubscriptionBeanI subscriptio_nejb;
	private volatile static ConcurrentLinkedQueue<Subscription> renewables = new ConcurrentLinkedQueue<Subscription>();
	private static SubscriptionRenewal instance;
	public volatile  BlockingQueue<SubscriptionBillingWorker> billingsubscriptionWorkers = new LinkedBlockingDeque<SubscriptionBillingWorker>();
	private int mandatory_throttle = 60112;
	private int queueSize = 1000;
	private int workers = 1;
	private int billables_per_batch = 1000;
	private Properties mtsenderprops;
	private int idleWorkers;
	public static int max_throttle_billing = 60000;
	private static boolean enable_biller_random_throttling=false;
	public static int min_throttle_billing = 1000;
	private static boolean adaptive_throttling  = false;
	private static boolean we_ve_been_capped  = false;
	
	private static Context context = null;
	
	
	
	public SubscriptionRenewal() throws Exception{
		init();
		initEJB();
		initWorkers();
		
		if (this.workers <= 0)
			renewables = new ConcurrentLinkedQueue<Subscription>();
		else
			renewables = new ConcurrentLinkedQueue<Subscription>();
		instance = this;
	}
	
	

	
	private void initEJB() throws Exception{
    	String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		 Properties props = new Properties();
		 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		 props.put(Context.PROVIDER_URL, "remote://localhost:4447");
		 props.put(Context.SECURITY_PRINCIPAL, "testuser");
		 props.put(Context.SECURITY_CREDENTIALS, "testpassword123!");
		 props.put("jboss.naming.client.ejb.context", true);
		 context = new InitialContext(props);
		 cmpbean =  (CMPResourceBeanRemote) 
       		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		 subscriptio_nejb =  (SubscriptionBeanI) 
		       		context.lookup("cmp/SubscriptionEJB!com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI");
	}
	
	
	private void init() {
		
		mtsenderprops = FileUtils.getPropertyFile("mtsender.properties");
		
		try{
			enable_biller_random_throttling = mtsenderprops.getProperty("enable_biller_random_throttling").trim().equalsIgnoreCase("true");
		}catch(Exception e){
			logger.warn(e.getMessage(),e);
		}
		
		try{
			mandatory_throttle = Integer.valueOf(mtsenderprops.getProperty("mandatory_throttle"));
		}catch(Exception e){
			logger.warn(e.getMessage(),e);
		}
		try{
			min_throttle_billing = Integer.valueOf(mtsenderprops.getProperty("min_throttle_billing"));
		}catch(Exception e){
			logger.warn(e.getMessage(),e);
		}
		try{
			max_throttle_billing = Integer.valueOf(mtsenderprops.getProperty("max_throttle_billing"));
		}catch(Exception e){
			logger.warn(e.getMessage(),e);
		}
		try{
			adaptive_throttling = mtsenderprops.getProperty("adaptive_throttling").trim().equalsIgnoreCase("true");
		}catch(Exception e){
			logger.warn(e.getMessage(),e);
		}
		
		try {
			billables_per_batch = Integer.valueOf(mtsenderprops
					.getProperty("billables_per_batch"));
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

		try {
			workers = Integer.valueOf(mtsenderprops
					.getProperty("billing_workers"));
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

		semaphore = new Semaphore(1, true);
		serialsemaphore = new Semaphore(1, true);
		
	}


	public static boolean isWe_ve_been_capped() {
		return we_ve_been_capped;
	}


	public static void setWe_ve_been_capped(boolean we_ve_been_capped) {
		SubscriptionRenewal.we_ve_been_capped = we_ve_been_capped;
	}


	public static int getRandomWaitTime(){
	    	return isEnable_biller_random_throttling()
	    			? 
	    			(new Random().nextInt(max_throttle_billing-min_throttle_billing) + min_throttle_billing) : -1;
	}
	 
	private void initWorkers() throws Exception {
		
		
		
		Thread t1;
		for (int i = 0; i < this.workers; i++) {
			SubscriptionBillingWorker worker;
			worker = new SubscriptionBillingWorker("THREAD_WORKER_#_" + i, cmpbean,subscriptio_nejb, mandatory_throttle); 
			t1 = new Thread(worker);
			t1.start();
			billingsubscriptionWorkers.add(worker);
		}
		
		for (SubscriptionBillingWorker worker : billingsubscriptionWorkers)
			worker.rezume();
	}


	
	public static boolean isAdaptive_throttling() {
		return adaptive_throttling;
	}


	public void setAdaptive_throttling(boolean adaptive_throttling) {
		this.adaptive_throttling = adaptive_throttling;
	}


	public static Subscription getBillable() {
		
		try{
			
			
			logger.debug(">>Threads waiting to retrieve message before : " + semaphore.getQueueLength() );
			
			semaphore.acquire();//now lock out everybody else!
			
			logger.debug(">>Threads waiting to retrieve message after: " + semaphore.getQueueLength() );
			if(renewables.size()>0)
			logger.info("SIZE OF QUEUE ? "+renewables.size());
			
			 final Subscription subscription = renewables.poll();//.takeFirst();//performance issues versus reliability? I choose reliability in this case :)
			 
			 try {
				 
				if(subscription!=null)
					logger.debug("subscription.getId():  "+subscription.getId());
				//billables.remove(billable);//try double remove from this queue
			
			 } catch (Exception e) {
				
				 logger.error("\nRootException: " + e.getMessage()+ ": " +
				 		"\n Something happenned. We were not able to mark " +
				 		"\n the message as being in queue. " +
				 		"\n To prevent another thread re-taking the object, " +
				 		"\n we've returned null to the thread/method requesting for " +
				 		"\na n MT.",e);
				 
				 return null;
			}
			 
			 return subscription;
		
		} catch (Exception e1) {
			logger.error(e1.getMessage(),e1);			
			return null;
		}finally{
			
			semaphore.release(); // then give way to the next thread trying to access this method..
		}
	}
	
	

	public void run() {

		try {
			while (run) {
				watch.start();
				try {
					populateQueue();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e.getMessage(), e);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				} finally {
					watch.stop();
				}
			}
		} catch (OutOfMemoryError e) {
			logger.error(
					"NEEDS RESTART: MEM_USAGE: " + MTProducer.getMemoryUsage()
							+ " >> " + e.getMessage(), e);
		} finally {
			myfinalize();
		}
		logger.info("subscription renewal stopped!!");
		logger.info("producer shut down!");
		//System.exit(0);
	}
	
	

	public static boolean isEnable_biller_random_throttling() {
		return enable_biller_random_throttling;
	}


	public static void setEnable_biller_random_throttling(
			boolean enable_biller_random_throttling) {
		SubscriptionRenewal.enable_biller_random_throttling = enable_biller_random_throttling;
	}


	private void populateQueue() throws Exception {

		List<Subscription> subsl = subscriptio_nejb.getExpiredSubscriptions(Long.valueOf(this.workers));

		logger.debug("EXPIRED LIST SIZE? subscriptio_nejb : subsl.size():::: "+subsl.size()+" this.workers:: "+this.workers);
		
		for (Subscription sub : subsl) {
			
			
			if(isWe_ve_been_capped()){
				
				logger.info(">>>>> Asking all the worker threads to wait....");
				for(SubscriptionBillingWorker tw : billingsubscriptionWorkers){
					try{
						tw.pauze();
					}catch(Exception exp){
						logger.error(exp);
						
					}
				}
				
				long wait_time = SubscriptionRenewal.getRandomWaitTime();
				logger.info(" ::: PRODUCER_CHILAXING::::::: Trying to chillax for "+wait_time+" milliseconds");
				if(wait_time>-1){
					try{
						Thread.sleep(wait_time);
					}catch(InterruptedException ie){}
				}
				
				
				logger.info(">>>>> Asking all the worker threads to resume running ....");
				
				for(SubscriptionBillingWorker tw : billingsubscriptionWorkers){
					try{
						tw.rezume();
					}catch(Exception exp){
						logger.error(exp);
						
					}
				}
			}
			
			renewables.offer(sub);

			
		}

	}

	private void setRun(boolean b) {
		this.run = b;
	}

	
	public static void stopApp() {
		logger.info("SubscriptionMain about to shut down...");
		
		try{
			if(instance!=null){
				logger.info("Shutting down...");
				instance.setRun(false);
				logger.info("...");
				instance.instance.waitForQueueToBecomeEmpty();
				logger.info("...");
				instance.instance.waitForAllWorkersToFinish();
				logger.info("...");
				instance.instance.myfinalize();
				logger.info("...");
			
			}else{
				
				logger.info("App not yet initialized or started.");
			
			}
		}catch(Exception e){
			instance.logger.error(e.getMessage(),e);
		}catch(Error e){
			instance.logger.error(e.getMessage(),e);
		}
		
	}
	
	
	
	private void waitForAllWorkersToFinish() {
		
		boolean finished = false;
		
		
		//First and foremost, let all threads die if they finish to process what they're processing currently.
		//We don't interrupt them still..
		for(SubscriptionBillingWorker tw : billingsubscriptionWorkers){
			if(tw.isRunning())
				tw.setRun(false);
		}
		
		//all unprocessed messages in queue are put back to the db.
		
		while(!finished){//Until all workers are idle or dead...
			
			idleWorkers = 0;
			
			for(SubscriptionBillingWorker tw : billingsubscriptionWorkers){
				
				if(tw.isRunning())
					tw.setRun(false);
				
				
				
				//might not be necessary because we already set run to false for each thread.
				//but in case we have an empty queue, then we add a poison pill that has id = -1 which forces the thread to run, then terminate
				//because we already set run to false.
				renewables.offer(new Subscription());//poison pill...the threads will swallow it and surely die.. bwahahahaha!
				
				if(!tw.isBusy()){
					idleWorkers++;
				}
				
			}
			
			try {
				
				logger.info("workers: "+workers);
				logger.info("idleWorkers: "+idleWorkers);
				
				Thread.sleep(500);
			
			} catch (InterruptedException e) {
				
				logger.error(e.getMessage(),e);
			
			}
			
			finished = workers==idleWorkers;
			
		}
		
		
		
		logger.info("We're shutting down, we put back any unprocessed message to the db queue so that they're picked next time we run..");
		//Now, if we have a big queue of unprocessed messages, we return them back to the db (or rather set the necessary flags
		for(Subscription sms : renewables){
			sms.setQueue_status(0L);//and its now not in the queue
			logger.info("Returned to db: "+ sms.toString());
			try {
				sms = cmpbean.saveOrUpdate(sms);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		//now all messages should be put back in quque
		
		
		
		renewables.clear();//Nothing is useful in the queue now. Necessary? we will find out using test.. 
		logger.info("workers: "+workers);
		logger.info("idleWorkers: "+idleWorkers);
		
		
		
	}
	

	private void waitForQueueToBecomeEmpty() {
		
		while(renewables.size()>0){
			logger.info("BillableQueu.size() : "+renewables.size());
			logger.info("BillableQueu.size() : "+renewables.size());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(),e);
			}
		}
		logger.info("Queue is now empty, and all threads have been asked not to wait for elements in the queue!");
		notify();
	}
	
	public void myfinalize() {

		try {

			if(context!=null)
				context.close();
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}
	}




	public static Long generateNextId() {
		
	try{
			
			
			logger.debug(">>Threads waiting to retrieve message before : " + semaphore.getQueueLength() );
			
			serialsemaphore.acquire();//now lock out everybody else!
			
			logger.debug(">>Threads waiting to retrieve message after: " + semaphore.getQueueLength() );
			if(renewables.size()>0)
			logger.info("SIZE OF QUEUE ? "+renewables.size());
			
			 final Long serial = instance.cmpbean.generateNextTxId();//.takeFirst();//performance issues versus reliability? I choose reliability in this case :)
			 
			 try {
				 
				if(serial!=null)
					logger.debug("serial:  "+serial);
				//billables.remove(billable);//try double remove from this queue
			
			 } catch (Exception e) {
				
				 logger.error("\nRootException: " + e.getMessage()+ ": " +
				 		"\n Something happenned. We were not able to mark " +
				 		"\n the message as being in queue. " +
				 		"\n To prevent another thread re-taking the object, " +
				 		"\n we've returned null to the thread/method requesting for " +
				 		"\na n MT.",e);
				 
				 return null;
			}
			 
			 return serial;
		
		} catch (Exception e1) {
			logger.error(e1.getMessage(),e1);			
			return null;
		}finally{
			
			serialsemaphore.release(); // then give way to the next thread trying to access this method..
		}
		
	}

}
