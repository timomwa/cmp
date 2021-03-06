package com.pixelandtag.sms.producerthreads;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import com.inmobia.util.StopWatch;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.subscription.FreeLoaderEJBI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.sms.core.OutgoingQueueRouter;
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
	private volatile static ConcurrentLinkedQueue<Subscription> subscriptions = new ConcurrentLinkedQueue<Subscription>();
	private static SubscriptionRenewal instance;
	public volatile  BlockingQueue<SubscriptionBillingWorker> billingsubscriptionWorkers = new LinkedBlockingDeque<SubscriptionBillingWorker>();
	private int mandatory_throttle = 60112;
	private int workers = 1;
	private int tps = 5;
	private int billables_per_batch = 1000;
	private Properties mtsenderprop;
	private int idleWorkers;
	public static int max_throttle_billing = 60000;
	private static boolean enable_biller_random_throttling=false;
	public static int min_throttle_billing = 1000;
	public static int throttle;
	private static boolean adaptive_throttling  = false;
	private static boolean we_ve_been_capped  = false;
	private FreeLoaderEJBI freeloaderEJB;
	
	private static Context context = null;
	
	
	
	public SubscriptionRenewal() throws Exception{
		init();
		initEJB();
		initWorkers();
		
		subscriptions = new ConcurrentLinkedQueue<Subscription>();
		
		instance = this;
	}
	
	

	
	private void initEJB() throws Exception{
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
		 subscriptio_nejb =  (SubscriptionBeanI) 
		       		context.lookup("cmp/SubscriptionEJB!com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI");
		 freeloaderEJB =  (FreeLoaderEJBI) this.context.lookup("cmp/FreeLoaderEJBImpl!com.pixelandtag.cmp.ejb.subscription.FreeLoaderEJBI");
			
	}
	
	
	private void init() {
		
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
    	
	
		try{
			tps =  Integer.valueOf(mtsenderprop.getProperty("tps"));
		}catch(NumberFormatException nfe){
			logger.warn(nfe.getMessage(), nfe);
		}
		
		try{
			enable_biller_random_throttling = mtsenderprop.getProperty("enable_biller_random_throttling").trim().equalsIgnoreCase("true");
		}catch(Exception e){
			logger.warn(e.getMessage(),e);
		}
		
		try{
			mandatory_throttle = Integer.valueOf(mtsenderprop.getProperty("mandatory_throttle"));
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
			adaptive_throttling = mtsenderprop.getProperty("adaptive_throttling").trim().equalsIgnoreCase("true");
		}catch(Exception e){
			logger.warn(e.getMessage(),e);
		}
		
		try {
			billables_per_batch = Integer.valueOf(mtsenderprop
					.getProperty("billables_per_batch"));
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

		try {
			workers = Integer.valueOf(mtsenderprop
					.getProperty("billing_workers"));
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

		semaphore = new Semaphore(1, true);
		serialsemaphore = new Semaphore(1, true);
		
		throttle = BigDecimal.valueOf(1000).divide(BigDecimal.valueOf(tps), 3, RoundingMode.HALF_EVEN).intValue();
		
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
		SubscriptionRenewal.adaptive_throttling = adaptive_throttling;
	}


	public static Subscription getBillable() {
		
		try{
			
			
			logger.info("throttle::: "+throttle);
			logger.debug(">>throttle::: "+throttle+", Threads waiting to retrieve message before : " + semaphore.getQueueLength() );
			
			semaphore.acquire();//now lock out everybody else!
			
			Thread.sleep(throttle);
			
			logger.debug(">>Threads waiting to retrieve message after: " + semaphore.getQueueLength() );
			if(subscriptions.size()>0)
			logger.info("SIZE OF QUEUE ? "+subscriptions.size());
			
			 final Subscription subscription = subscriptions.poll();//.takeFirst();//performance issues versus reliability? I choose reliability in this case :)
			 
			 try {
				 
				if(subscription!=null)
					logger.debug("subscription_id:  "+subscription);
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
					if(subscriptions.size()<=0){
						populateQueue();
					}else{
						Thread.sleep(5000);//sleep 5 seconds
					}
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
					"NEEDS RESTART: MEM_USAGE: " + OutgoingQueueRouter.getMemoryUsage()
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

		List<Subscription> subsl = subscriptio_nejb.getExpiredSubscriptions(Long.valueOf(billables_per_batch));

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
			
			if(!freeloaderEJB.isInFreeloaderList(sub.getMsisdn()))//Don't bill them if they're in the freeloader list
				subscriptions.offer(sub);

			
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
				SubscriptionRenewal.instance.waitForQueueToBecomeEmpty();
				logger.info("...");
				SubscriptionRenewal.instance.waitForAllWorkersToFinish();
				logger.info("...");
				SubscriptionRenewal.instance.myfinalize();
				logger.info("...");
			
			}else{
				
				logger.info("App not yet initialized or started.");
			
			}
		}catch(Exception e){
			SubscriptionRenewal.logger.error(e.getMessage(),e);
		}catch(Error e){
			SubscriptionRenewal.logger.error(e.getMessage(),e);
		}
		
	}
	
	
	
	private void waitForAllWorkersToFinish() {
		
		boolean finished = false;
		
		
		//First and foremost, let all threads die if they finish to process what they're processing currently.
		//We don't interrupt them still..
		for(SubscriptionBillingWorker tw : billingsubscriptionWorkers){
			if(tw.isRunning()){
				tw.setRun(false);
				tw.finalizeMe();
			}
		}
		
		//all unprocessed messages in queue are put back to the db.
		
		while(!finished){//Until all workers are idle or dead...
			
			idleWorkers = 0;
			
			for(SubscriptionBillingWorker tw : billingsubscriptionWorkers){
				
				if(tw.isRunning()){
					tw.setRun(false);
					tw.finalizeMe();
				}
				
				
				//might not be necessary because we already set run to false for each thread.
				//but in case we have an empty queue, then we add a poison pill that has id = -1 which forces the thread to run, then terminate
				//because we already set run to false.
				Subscription sub = new Subscription();
				sub.setId(-1L);
				subscriptions.offer(sub);//poison pill...the threads will swallow it and surely die.. bwahahahaha!
				
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
		for(Subscription sms : subscriptions){
			//sms.setQueue_status(0L);//and its now not in the queue
			logger.info("Returned to db?: "+ sms.toString());
			try {
				//sms = cmpbean.saveOrUpdate(sms);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		//now all messages should be put back in quque
		
		
		
		subscriptions.clear();//Nothing is useful in the queue now. Necessary? we will find out using test.. 
		logger.info("workers: "+workers);
		logger.info("idleWorkers: "+idleWorkers);
		
		
		
	}
	

	private void waitForQueueToBecomeEmpty() {
		
		while(subscriptions.size()>0){
			logger.info("BillableQueu.size() : "+subscriptions.size());
			logger.info("BillableQueu.size() : "+subscriptions.size());
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




	public static String generateNextId() {
		
	try{
			
			
			logger.debug(">>Threads waiting to retrieve message before : " + semaphore.getQueueLength() );
			
			serialsemaphore.acquire();//now lock out everybody else!
			
			logger.debug(">>Threads waiting to retrieve message after: " + semaphore.getQueueLength() );
			if(subscriptions.size()>0)
			logger.info("SIZE OF QUEUE ? "+subscriptions.size());
			
			 final String serial = instance.cmpbean.generateNextTxId();//.takeFirst();//performance issues versus reliability? I choose reliability in this case :)
				 
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




	public static void putPackToQueue(Subscription billable) {
		try{
			if(billable!=null)
				subscriptions.add(billable);
		}catch(Exception exp){
			
		}
		
	}

}
