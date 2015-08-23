package com.pixelandtag.sms.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPParam;
import com.pixelandtag.sms.producerthreads.MTProducer;

/**
 * An improvement of com.pixelandtag.sms.producerthreads.MTProducer
 * 
 * @author Timothy Mwangi Gikonyo
 * @date 18th August 2015
 *
 */
public class OutgoingQueueRouter extends Thread {

	private boolean run = true;
	private Logger logger = Logger.getLogger(getClass());
	private volatile static Queue<OutgoingSMS> outqueue = null;
	private int maxsizeofqueue = 100000;//TODO externalize
	private  Context context = null;
	private static QueueProcessorEJBI queueprocEJB;
	private OpcoSenderProfileEJBI opcosenderprofEJB;
	private static List<SenderThreadWorker> senderworkers = new ArrayList<SenderThreadWorker>();
	private static Semaphore queueSemaphore;
	static{
		queueSemaphore = new Semaphore(1, true);
	}
	
	public static OutgoingSMS poll() throws InterruptedException{
		
		try{
			
			queueSemaphore.acquire();
			 
			final OutgoingSMS myMt = outqueue.poll();
				 
			if(myMt!=null && myMt.getId()>-1)
				queueprocEJB.updateQueueStatus(myMt.getId(), Boolean.TRUE);
			
			return myMt;
		
		}finally{
			queueSemaphore.release(); 
		}
	}
	
	public OutgoingQueueRouter() throws Exception{
		initialize();
	}
	
	
	private void initialize() throws Exception {
		outqueue = new ConcurrentLinkedQueue<OutgoingSMS>();
		initEJB();
		startWorkers();
		setDaemon(true);//I don't know what I am doing here 
	}
	
	private void startWorkers() {
		
		List<OpcoSenderReceiverProfile> profiles = opcosenderprofEJB.getAllActiveProfiles();
		
		for(OpcoSenderReceiverProfile opcoprofile : profiles){
			Integer threads = opcoprofile.getWorkers();
			logger.info("\n\nAbout to initialize "+threads+" sender threads for opco with code "
					+opcoprofile.getOpco().getCode()
					+" Operator name : "+opcoprofile.getOpco().getOperator().getName()+", country="
					+opcoprofile.getOpco().getCountry().getName()+"\n");
			int success = 0;
			for(int i = 0; i<threads; i++){
				try{
					SenderThreadWorker worker = new SenderThreadWorker(outqueue, opcoprofile);
					Thread t1 = new Thread(worker);
					t1.start();
					senderworkers.add(worker);
					success++;
				}catch(Exception exp){
					logger.error(exp.getMessage(),exp);
				}
			}
			logger.info(" successful workers started "+success);
		}
		
	}

	public void initEJB() throws NamingException{
    	 String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		 Properties props = new Properties();
		 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		 props.put(Context.PROVIDER_URL, "remote://localhost:4447");
		 props.put(Context.SECURITY_PRINCIPAL, "testuser");
		 props.put(Context.SECURITY_CREDENTIALS, "testpassword123!");
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
				
				logger.info("NEW_MT_QUEUE : "+outqueue.size());
				
				if(outqueue.size()<1){
					populateQueue();
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.error(e);
				}
			}
			
		} catch (OutOfMemoryError e) {

			logger.error("MEM_USAGE: "+ MTProducer.getMemoryUsage()
			        + " >> " 
					+ e.getMessage(), e);

		} finally {

		}

		logger.info("producer shut down!");

		System.exit(0);

	}
	
	
	
	private void populateQueue()  {
		
		List<OutgoingSMS> outqueuelist_ = queueprocEJB.getUnsent(1000L); 
	
		if(outqueue.size()<=maxsizeofqueue)//Control the amount of RAM to be used
			for(OutgoingSMS queue : outqueuelist_)
				outqueue.offer(queue);
		
	}
	
	
	public static void stopApp(){
		
		System.out.println("Shutting down...");
		
		outqueue.clear();//clear queue
		
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
