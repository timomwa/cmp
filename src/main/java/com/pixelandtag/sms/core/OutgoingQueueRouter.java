package com.pixelandtag.sms.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.util.FileUtils;

/**
 * An improvement of com.pixelandtag.sms.producerthreads.MTProducer
 * 
 * @author Timothy Mwangi Gikonyo
 * @date 18th August 2015
 *
 */
public class OutgoingQueueRouter extends Thread {

	private Properties mtsenderprop;
	private boolean run = true;
	private Logger logger = Logger.getLogger(getClass());
	private Long maxsizeofqueue = 100000L;//TODO externalize
	private  Context context = null;
	private static QueueProcessorEJBI queueprocEJB;
	private OpcoSenderProfileEJBI opcosenderprofEJB;
	private static List<SenderThreadWorker> senderworkers = new ArrayList<SenderThreadWorker>();
	private static ConcurrentMap<Long,Queue<OutgoingSMS>> opcoqueuemap = new ConcurrentHashMap<Long,Queue<OutgoingSMS>>();
	private static Queue<Billable> billablequeue = new LinkedBlockingQueue<Billable>();
	private static Map<Long,OpcoSenderReceiverProfile> profilemap = new HashMap<Long,OpcoSenderReceiverProfile>();
	private static Semaphore queueSemaphore;
	private static Semaphore queueSemaphorebillable;
	private int mandatoryqueuewaittime = 0;
	static{
		queueSemaphore = new Semaphore(1, true);
		queueSemaphorebillable = new Semaphore(1, true);
	}
	
	public static OutgoingSMS poll(Long profileId) throws InterruptedException{
		
		try{
			
			queueSemaphore.acquire();
			
			Queue<OutgoingSMS> outqueue = opcoqueuemap.get(profileId);
			
			if(outqueue!=null){
				
				final OutgoingSMS myMt = outqueue.poll();
				
				if(myMt!=null && myMt.getId()>-1)
					queueprocEJB.updateQueueStatus(myMt.getId(), Boolean.TRUE);
				
				return myMt;
			}
			
			return null;
		
		}finally{
			queueSemaphore.release(); 
		}
	}
	
	

	public static Billable pollBillable(Long profileId) throws InterruptedException{
		
		try{
			
			queueSemaphorebillable.acquire();
			
			Queue<OutgoingSMS> outqueue = opcoqueuemap.get(profileId);
			
			if(outqueue!=null){
				
				final Billable billable = billablequeue.poll();
				
				if(billable!=null && billable.getId()>-1){
					//queueprocEJB.updateQueueStatus(myMt.getId(), Boolean.TRUE); update the billable, we tell system it's in queue
				}
				
				return billable;
			}
			
			return null;
		
		}finally{
			queueSemaphorebillable.release(); 
		}
	}
	
	public OutgoingQueueRouter() throws Exception{
		initialize();
	}
	
	
	private void initialize() throws Exception {
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		try{
			mandatoryqueuewaittime = Integer.valueOf( mtsenderprop.getProperty("mandatoryqueuewaittime") );
		}catch(NumberFormatException  nfe){
			logger.warn(nfe.getMessage(), nfe);
		}catch(Exception  nfe){
			logger.error(nfe.getMessage(), nfe);
		}
		initEJB();
		startWorkers();
		setDaemon(true);//I don't know what I am doing here 
	}
	
	private void startWorkers() throws Exception {
		
		List<OpcoSenderReceiverProfile> profiles = opcosenderprofEJB.getAllActiveSenderOrTranceiverProfiles();
		
		for(OpcoSenderReceiverProfile opcoprofile : profiles){
			
			profilemap.put(opcoprofile.getId(), opcoprofile);
			
			Integer threads = opcoprofile.getWorkers();
			logger.info("\n\nAbout to initialize "+threads+" sender threads for opco with code "
					+opcoprofile.getOpco().getCode()
					+" Operator name : "+opcoprofile.getOpco().getOperator().getName()+", country="
					+opcoprofile.getOpco().getCountry().getName()+"\n");
			int success = 0;
			
			Queue<OutgoingSMS> outqueue_ = new ConcurrentLinkedQueue<OutgoingSMS>();
			
			for(int i = 0; i<threads.intValue(); i++){
				
				try{
					SenderThreadWorker worker = new SenderThreadWorker(outqueue_, opcoprofile);
					Thread t1 = new Thread(worker);
					t1.start();
					senderworkers.add(worker);
					success++;
				}catch(Exception exp){
					logger.error(exp.getMessage(),exp);
				}
				
			}
			
			opcoqueuemap.put(opcoprofile.getId(), outqueue_);
			
			logger.info(" successful workers started "+success);
		}
		
	}

	public void initEJB() throws NamingException{
    	 String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		 Properties props = new Properties();
		 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		 props.put(Context.PROVIDER_URL, mtsenderprop.getProperty("ejbprotocol")+"://"+mtsenderprop.getProperty("ejbhost")+":"+mtsenderprop.getProperty("ejbhostport"));
		props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
		props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
		props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        
		props.put("jboss.naming.client.ejb.context", true);
		 context = new InitialContext(props);
		 queueprocEJB =  (QueueProcessorEJBI) 
       		context.lookup("cmp/QueueProcessorEJBImpl!com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI");
		 opcosenderprofEJB = (OpcoSenderProfileEJBI) 
    		context.lookup("cmp/OpcoSenderProfileEJBImpl!com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI");
		 	
		 logger.info(getClass().getSimpleName()+": Successfully initialized EJB QueueProcessorEJBImpl !!");
 	}

	@Override
	public void run() {

		try {

			while (run) {
				
				
				int lowestsize = getLowestSizeOfQueue();
				if(lowestsize<1){
					populateQueue();
				}
				
				try {
					Thread.sleep(mandatoryqueuewaittime);
				} catch (InterruptedException e) {
					logger.error(e);
				}
			}
			
		} catch (OutOfMemoryError e) {

			logger.error("MEM_USAGE: "+ getMemoryUsage()
			        + " >> " 
					+ e.getMessage(), e);
			System.out.println("We ran out of memory, so we don't continue running");
			logger.error("We ran out of memory, so we don't continue running");
			setRun(false);

		} finally {

		}

		logger.info("producer shut down!");

		System.exit(0);

	}
	
	private int getLowestSizeOfQueue() {
		
		int lowestsize = 10;
		
		for(Entry<Long, Queue<OutgoingSMS>> entryset  : opcoqueuemap.entrySet())
			if(lowestsize>=entryset.getValue().size())
				lowestsize = entryset.getValue().size();
		
		return lowestsize;
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
	
	
	
	private void populateQueue()  {
		
		for(Entry<Long, Queue<OutgoingSMS>> entryset  : opcoqueuemap.entrySet()){
			
			Long profileid = entryset.getKey();
			Queue<OutgoingSMS> outqueue = entryset.getValue();
			
			if(outqueue.size()==0){
				
				List<OutgoingSMS> outqueuelist_ = queueprocEJB.getUnsent(maxsizeofqueue, profilemap.get(profileid));  
				
				for(OutgoingSMS outgoingsms : outqueuelist_){
					
					try {
						outgoingsms.setIn_outgoing_queue(Boolean.TRUE);
						queueprocEJB.saveOrUpdate(outgoingsms);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
					//if(outqueue.size()<1){
						outqueue.offer(outgoingsms);
						opcoqueuemap.put(profileid, outqueue);
					//}
				}
			}
		}
		
	}
	
	
	public static void stopApp(){
		
		System.out.println("Shutting down...");
		
		//Update on the db and say none of the sms are in queue
		for(Entry<Long, Queue<OutgoingSMS>> entryset  : opcoqueuemap.entrySet()){
			
			for( OutgoingSMS sms : entryset.getValue() ){
				try {
					
					sms.setIn_outgoing_queue(Boolean.FALSE);
					queueprocEJB.saveOrUpdate(sms);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		
		for(Entry<Long, Queue<OutgoingSMS>> entryset  : opcoqueuemap.entrySet()){
			entryset.getValue().clear();//clear queue
		}
		
		for(SenderThreadWorker worker : senderworkers)
			worker.stop();
		
		boolean allstopped = false;
		
		int totalworkers = senderworkers.size();
		
		while(!allstopped){
			
			int stopped = 0;
			
			for(SenderThreadWorker worker : senderworkers){
				if(worker.isStopped())
					stopped++;
			}
			
			if(stopped>=totalworkers)
				allstopped =true;
			
			try {
				Thread.sleep(1000);
				System.out.println("Waiting for all threads to stop...");
			} catch (InterruptedException e) {
			}
			
		}
		
		System.out.println("Successfully stopped all threads.");
		
		
		
	}

	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

}
