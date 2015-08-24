package com.pixelandtag.sms.mt.workerthreads;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import com.pixelandtag.api.CelcomHTTPAPI;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.sms.core.OutgoingQueueRouter;


/**
 * 
 * @author Timothy Mwangi Gikonyo
 * Every MT message retrieved from the db (celcom.httptosend)
 * MUST be marked as to be in queue after being put in the queue.
 * This thread uses an ID of each msg to do this marking.
 *@deprecated Do not use it. it has no use in MTProducer. 
 *MT producer now updates the Queue itself in a static sycnhronized method 
 *called getMTsms()... When worker threads try to get a message, 
 *they call the getMTsms() method statically. 
 *
 *How?
 *The method has a semaphore/lock.
 *When a thread tries to get an MTsms object, it MUST be set as to be in queue in the db.
 *If it is not set as to be in queue, then there is high likelyhood of the same message being retrieved once again from the
 *httptosend table and put back in the outgoing queue. To avoid this happening, a message is Never sent unless it is 
 *marked as an outgoing message in the httptosend table. If the method setting this flag to true fails, then getMTsms
 *returns null.
 *
 * After this, I had to take a shower for a job well done. :)
 * 
 */
@Deprecated 
public class QueueUpdater implements Runnable {

	public static BlockingDeque<Long> inQueue;
	public CelcomHTTPAPI celcomAPI;
	private volatile boolean run = true;
	private volatile boolean busy = false;
	private String name;
	private Logger logger = Logger.getLogger(QueueUpdater.class);
	private volatile boolean success = false;
	private String connUrl;
	private volatile long  http_to_send_id = -1;
	
	
	
	
	
	public boolean isBusy() {
		return busy;
	}



	private synchronized void setBusy(boolean busy) {
		this.busy = busy;
		this.notify();
	}



	public synchronized String getName() {
		return name;
	}



	public synchronized void setName(String name) {
		this.name = name;
	}



	public synchronized boolean isRun() {
		return run;
	}



	public synchronized void setRun(boolean run) {
		this.run = run;
	}



	public QueueUpdater(String name_, String connUrl_) throws Exception{
		
		if(connUrl_==null || connUrl_.isEmpty())
			throw new NullPointerException("com.inmobia.celcom.mt.MTProducer.connectionPool is null or the jdbc connection string is null also!");
		
		this.name = name_;
		
		this.connUrl = connUrl_;
		
		initialize();
		
	}
	
	
	
	private void initialize() throws Exception{
		
		inQueue = new LinkedBlockingDeque<Long>(1);
		
		this.celcomAPI = new CelcomImpl(this.connUrl,"THRD_"+name); 
		
	}
	
	
	public void run() {
		
		try{
		
			
			while(run){
				
				try {
					
					
					setBusy(false);
					
					http_to_send_id = inQueue.takeFirst();
					
					setBusy(true);
					
					//if its -1, we know that its a 
					//poison pill, just run to find run=false so we can shutdown
					if(http_to_send_id>-1){
						success = celcomAPI.markInQueue(http_to_send_id);
					}else{
						success = false;
					}
					if(success){
						
						logger.debug("message with id: "+http_to_send_id+" was put in queue");
						
					}else if(!success && (http_to_send_id>-1)){
						
						logger.warn("problem occurred putting message with id: "+http_to_send_id+"  in queue!");
					
					}
					
					http_to_send_id = -1;//make sure we dont process unless we have >-1
						
					
				} catch (InterruptedException e) {
					
					logger.error(e.getMessage(),e);
				
				}catch(Exception e){
					
					logger.error(e.getMessage(),e);
				
				}
				
			}
			
			
			
			setBusy(false);
			
		}catch(OutOfMemoryError e){
			
			//I don't expect this to happen, but if it does...
			logger.error("NEEDS RESTART: MEM_USAGE: "+OutgoingQueueRouter.getMemoryUsage() +" >> "+e.getMessage(),e);
		
		}finally{
			
			celcomAPI.myfinalize();
			
			setBusy(false);
		
		}
		
		logger.debug("Queueupdator shut down!");
		
	}




}
