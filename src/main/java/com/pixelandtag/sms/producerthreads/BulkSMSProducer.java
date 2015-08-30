package com.pixelandtag.sms.producerthreads;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.inmobia.util.StopWatch;
import com.pixelandtag.api.MessageStatus;
import com.pixelandtag.bulksms.BulkSMSPlan;
import com.pixelandtag.bulksms.BulkSMSQueue;
import com.pixelandtag.bulksms.BulkSMSText;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.bulksms.BulkSmsMTI;
import com.pixelandtag.entities.URLParams;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPClientWorker;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPParam;

/**
 * 
 * @author Timothy Mwangi Gikonyo
 * Date created: Sunday 3rd May 2015.
 * This will retrieve messages from the db and fairly distribute to 
 * http senders to send to the CMP/SMSC
 *
 */


public class BulkSMSProducer extends Thread {
	
	private static final boolean FAIR = true;
	private int workers;
	private StopWatch watch;
	private boolean run = true;
	private URLParams urlparams; 
	public static volatile BulkSMSProducer instance;
	private static Logger logger = Logger.getLogger(BulkSMSProducer.class);
	
	private volatile static ConcurrentLinkedQueue<GenericHTTPParam> genericMT = new ConcurrentLinkedQueue<GenericHTTPParam>();
	public volatile  BlockingDeque<GenericHTTPClientWorker> generichttpSenderWorkers = new LinkedBlockingDeque<GenericHTTPClientWorker>();
	private int idleWorkers;
	private String fr_tz;
	private String to_tz;
	public static final String DEFLT = "DEFAULT_DEFAULT";
	private static int sentMT = 0;
	
	private static Semaphore semaphoreg;
	private static Semaphore semaphore;//a semaphore because we might need to recover from a deadlock later.. listen for when we have many recs in the db but no msg is being sent out..
	private CMPResourceBeanRemote cmpbean;
	private BulkSmsMTI bulksmsBean;
	private  Context context = null;
	private int queueSize;
	private int throttle;

	public void initEJB() throws NamingException{
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
			 bulksmsBean = (BulkSmsMTI) 
			       		context.lookup("cmp/BulkSmsMTEJB!com.pixelandtag.cmp.ejb.bulksms.BulkSmsMTI");
			 
			 logger.info(getClass().getSimpleName()+": Successfully initialized EJB BulkSmsMTI !!");
	 }
	
	
	
	/**
	 * This method tries - with all might :) not to allow more than one thread to 
	 * access the MT message dequeue object.
	 * During tests, I experienced a situation where two threads got the same message
	 * from one queue.. Its like before the message was removed from the queue, another thread
	 * already took the given message...
	 * @return  com.pixelandtag.sms.mt.workerthreads.GenericHTTPParam
	 * @throws InterruptedException
	 * @throws NullPointerException
	 */
	public static GenericHTTPParam getGenericHttp() throws InterruptedException, NullPointerException{
		
		try{
			
			
			instance.logger.debug(">>Threads waiting to retrieve message before : " + semaphore.getQueueLength() );
			
			semaphoreg.acquire();//now lock out everybody else!
			
			
			instance.logger.debug(">>Threads waiting to retrieve message after: " + semaphore.getQueueLength() );
			
			
			 final GenericHTTPParam myMt = genericMT.poll();//performance issues versus reliability? I choose reliability in this case :)
			 
			 //celcomAPI.beingProcessedd(myMt.getId(), true);//mark it as being processed first before returning.
			 try {
				 
				//celcomAPI.markInQueue(myMt.getId());
			
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
			semaphoreg.release(); // then give way to the next thread trying to access this method..
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
		
		try {
			if(context!=null)
				context.close();
		} catch (Exception e) {
			log(e);
		}
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
		
		
		
		while(genericMT.size()>0){
			
			logger.info("BulkSMSProducer.genericMT.size() : "+genericMT.size());
			
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
		
		
		for(GenericHTTPClientWorker tw : generichttpSenderWorkers){
			if(tw.isRunning())
				tw.setRun(false);
		}
		
		
		
		//all unprocessed messages in queue are put back to the db.
		
		while(!finished){//Until all workers are idle or dead...
			
			idleWorkers = 0;
			
			for(GenericHTTPClientWorker tw : generichttpSenderWorkers){
				
				if(tw.isRunning())
					tw.setRun(false);
				//might not be necessary because we already set run to false for each thread.
				//but in case we have an empty queue, then we add a poison pill that has id = -1 which forces the thread to run, then terminate
				//because we already set run to false.
				genericMT.offer(new GenericHTTPParam());//poison pill...the threads will swallow it and surely die.. bwahahahaha!
				
				if(!tw.isBusy()){
					idleWorkers++;
				}
				
			}
			
			try {
				
				logger.info("(workers*2): "+(workers*2));
				logger.info("idleWorkers: "+idleWorkers);
				
				logger.info("workers: "+workers);
				logger.info("idleWorkers: "+idleWorkers);
				
				Thread.sleep(500);
			
			} catch (InterruptedException e) {
				
				log(e);
			
			}
			
			finished = (workers*2)>=idleWorkers;
			
		}
		
		
		
		logger.info("We're shutting down, we put back any unprocessed message to the db queue so that they're picked next time we run..");
		//Now, if we have a big queue of unprocessed messages, we return them back to the db (or rather set the necessary flags
		
		for(GenericHTTPParam sms : genericMT){
			try {
				logger.info("Returned to db: "+ sms.toString());
				BulkSMSQueue q = sms.getBulktext();
				q.setStatus(MessageStatus.FAILED_TEMPORARILY);
				cmpbean.saveOrUpdate(q);
			} catch (Exception e) {
				log(e);
			}
		}
		//now all messages should be put back in quque
		genericMT.clear();//Nothing is useful in the queue now. Necessary? we will find out using test.. 
		
		//queueUpdater.setRun(false);//Stop the Queue updater worker
		
		//QueueUpdater.inQueue.add(-1L);//This makes sure it runs one more time aka ("Poison-pill shutdown")
		
		logger.info("workers: "+workers);
		logger.info("idleWorkers: "+idleWorkers);
		
	}

	
	private void initWorkers() throws Exception{
		
		
		for(int i = 0; i<this.workers; i++){
			GenericHTTPClientWorker genWorker;
			genWorker = new GenericHTTPClientWorker(cmpbean) ;
			Thread t2 = new Thread(genWorker);
			t2.start();
			generichttpSenderWorkers.add(genWorker);
		
		}
		
		for(GenericHTTPClientWorker worker: generichttpSenderWorkers)
			worker.rezume();
		
			
	}
	
	
	
	public BulkSMSProducer(int workers_, int throttle_, int queueSize_, URLParams urlparams_) throws Exception{
		
		semaphore = new Semaphore(1, FAIR);
		
		semaphoreg = new Semaphore(1, FAIR);
		
		this.throttle = throttle_;
		
		this.queueSize = queueSize_;
		
		this.urlparams = urlparams_;
		
		this.workers = workers_;
		
		watch = new StopWatch();
		
		initEJB();
		initialize();
	}
	
	
	private void initialize() throws Exception {
		
		watch.start();
		
	    try {
	  
	      watch.stop();
	      
	      logger.info(">< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to create the connection pool");
	      
	      watch.reset();
	      
	    
	    } catch(Exception sqle) {
	     
	    	logger.error("Error making pool: " + sqle.getMessage(),sqle);
	      
	    
	    }
	    
	   
	    
	    instance = this;
	    
	    while(instance==null){
	    	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
	    }
	   
	    initWorkers();
	    
	}

	
	
	public void run() {
		
		try{
			
			while(run){
				
				if(genericMT.size()==0){
					populateQueue();
				}else{
					try{
						Thread.sleep(60000);//1 minute sleep
					}catch(Exception exp){}
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
			
			logger.error("NEEDS RESTART: MEM_USAGE: "+BulkSMSProducer.getMemoryUsage() +" >> "+e.getMessage(),e);
			try{
				//alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Celcom Platform: SEVERE:", "Hi,\n\n We encountered a fatal exception. Please check Malaysia HTTP Sender app.\n\n  Regards");
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
			
			List<BulkSMSQueue> queue = bulksmsBean.getUnprocessed(1000L);
			
			logger.debug(">>> BULK_SMS #%#%#%#%#%#%#%#%#%#% queue.size():: "+queue.size());
			for(BulkSMSQueue bulktext : queue){
				
				try{
					 String cpTxId = cmpbean.generateNextTxId();
					 bulktext.setCptxId(cpTxId.toString());
					 bulktext.setRetrycount(bulktext.getRetrycount().intValue() + 1);
					 bulktext.setStatus(MessageStatus.IN_QUEUE);
					 bulktext =  cmpbean.saveOrUpdate(bulktext);//Mar it as is in queue.
					 
					
					 BulkSMSText text = bulktext.getText();
					 BulkSMSPlan plan =  text.getPlan();
					
					 logger.info(">>::protocol:"+plan.getProtocol());
					 logger.info(">>::processorId:"+plan.getProcessor_id());
					 logger.info(">>::sms ::: "+text.getContent());
					 logger.info(">>::msisdn ::: "+bulktext.getMsisdn());
						
					 if(plan.getProtocol().equalsIgnoreCase("smpp")){
						 boolean success  = cmpbean.sendMTSMPP(plan.getProcessor_id(),bulktext.getMsisdn(),text.getSenderid(),text.getContent(),"",bulktext.getPriority());
						 if(success){
							 bulktext.setStatus(MessageStatus.SENDING);
						 }else{
							 MessageStatus status = (bulktext.getRetrycount().compareTo(bulktext.getMax_retries())<0) ? 
									 MessageStatus.FAILED_TEMPORARILY : MessageStatus.FAILED_PERMANENTLY;
							 bulktext.setStatus(status);
						 }
					 }
					 
					 if(plan.getProtocol().equalsIgnoreCase("http")){
						 	try{
						 		GenericHTTPParam param = new GenericHTTPParam();
								param.setUrl(urlparams.getMturl());
								param.setId(bulktext.getId());
								param.setBulktext(bulktext);
								List<NameValuePair> qparams = new ArrayList<NameValuePair>();
								qparams.add(new BasicNameValuePair("login", urlparams.getLogin()));
								qparams.add(new BasicNameValuePair("pass",urlparams.getPass()));	
								qparams.add(new BasicNameValuePair("type",urlparams.getType()));
								qparams.add(new BasicNameValuePair("src",text.getSenderid()));
								qparams.add(new BasicNameValuePair("msisdn",bulktext.getMsisdn()));
								qparams.add(new BasicNameValuePair("sms",text.getContent()));
								param.setHttpParams(qparams);
								if(queueSize==0){
									genericMT.offer(param);
								}else{
									genericMT.offer(param);
								}
								bulktext.setStatus(MessageStatus.SENDING);
								
						 	}catch(Exception e){
								
								log(e);
								
								MessageStatus status = (bulktext.getRetrycount().compareTo(bulktext.getMax_retries())<0)
										 ? MessageStatus.FAILED_TEMPORARILY : MessageStatus.FAILED_PERMANENTLY;
								 bulktext.setStatus(status);
							}
					 }
					 
				}catch(Exception exp){
					log(exp);
					 MessageStatus status = (bulktext.getRetrycount().compareTo(bulktext.getMax_retries())<0)
							 ? MessageStatus.FAILED_TEMPORARILY : MessageStatus.FAILED_PERMANENTLY;
					 bulktext.setStatus(status);
				}finally{
					bulktext =  cmpbean.saveOrUpdate(bulktext);
				}
				 
				BulkSMSProducer.increaseMT();
				
			}
			
			
			
			if(getSentMT()>0){
				
				watch.stop();
				
				logger.info("END!>>>>>>>>>>>>>>>>>>>>< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()/1000d+"")) + " seconds to clear a queue of "+getSentMT()+" messages");
			   
				watch.reset();
				
				BulkSMSProducer.resetSentMT();
			}
			
		
		} catch (Exception e) {
			
			log(e);
		
		}finally{
			
						
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
