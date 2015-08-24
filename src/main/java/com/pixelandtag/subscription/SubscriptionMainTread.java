package com.pixelandtag.subscription;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.api.MOProcessorFactory;
import com.pixelandtag.api.ServiceProcessorI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.serviceprocessors.dto.ServiceSubscription;
import com.pixelandtag.serviceprocessors.dto.SubscriptionDTO;

public class SubscriptionMainTread extends Thread{
	
	private Logger logger  = Logger.getLogger(SubscriptionMainTread.class);
	//private static DBPoolDataSource ds;
	private ArrayBlockingQueue<ServiceSubscription> to_be_pushed = null;
	private List<SubscriptionWorker> workers = new ArrayList<SubscriptionWorker>();
	private static Semaphore uniq;
	
	
	
	static{
		uniq = new Semaphore(1, true);
	}
	
	public static String DB = "pixeland_content360";
	private static SubscriptionMainTread instance;

	
	private String sub;
	
	private Map<Integer,ArrayBlockingQueue<SubscriptionDTO>> processor_map = new HashMap<Integer,ArrayBlockingQueue<SubscriptionDTO>>();
	private String constr_;
	private boolean run = false;
	private int idleWorkers;
	private int workers_cnt = 0;;
	
	private SubscriptionBeanI subscriptinoEJB;
	private CMPResourceBeanRemote cmpbean;
	
	public SubscriptionMainTread(CMPResourceBeanRemote cmpbean_,SubscriptionBeanI subscriptinoEJB_) throws Exception{
		this.cmpbean = cmpbean_;
		this.subscriptinoEJB = subscriptinoEJB_;
		init();
		this.instance = this;
	}
	
	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

	private void init() throws Exception {
		
		
		sub = "SELECT "
				+ "ss.id as 'service_subscription_id', "//0
				+ "pro.id as 'mo_processor_id_fk', "//1
				+ "pro.shortcode,"//2
				+ "pro.ServiceName,"//3
				+ "sm.cmd,"//4
				+ "sm.CMP_Keyword,"//5
				+ "sm.CMP_SKeyword,"//6
				+ "sm.price as 'price', "//7
				+ "sm.push_unique,"//8
				+ "ss.serviceid as 'sms_serviceid',"//9
				+ "pro.threads,"//10
				+ "pro.ProcessorClass as 'ProcessorClass',"//11
				+ "sm.price_point_keyword"//12
				+" FROM `"+CelcomImpl.database+"`.`ServiceSubscription` ss "
				+"LEFT JOIN `"+CelcomImpl.database+"`.`sms_service` sm "
				+"ON sm.id = ss.serviceid "
				+"LEFT JOIN `"+CelcomImpl.database+"`.`mo_processors` pro "
				+"ON pro.id = sm.mo_processorFK WHERE pro.enabled=1 AND hour(`ss`.`schedule`)=hour(now()) AND `ss`.`lastUpdated`<now() AND `ss`.`ExpiryDate`>now()";

		
		
		logger.debug("\ninitializing...........");
		
	}

	

	private void init_processor_map() {
		
		try {
			
			ArrayBlockingQueue<SubscriptionDTO> subscr = null;
			
			cmpbean.deleteOldLogs();
			
			List<SubscriptionDTO> subscrList =  cmpbean.getSubscriptionServices();
			
			for(SubscriptionDTO subdto: subscrList){
				
				
				subscr = processor_map.get(subdto.getServiceid());
				int threads = subdto.getThreads();
				int threadBalance = threads;//we need to 
				
				//check if that processor's thread has already been initialized and started
				if(subscr!=null){
					int sizeLiveprocessors = processor_map.get(subdto.getServiceid()).size();
					int requiredProcessorthreads = subdto.getThreads();
					threadBalance = requiredProcessorthreads - sizeLiveprocessors;
				}
				
				
				if(subscr==null)
					subscr = new ArrayBlockingQueue<SubscriptionDTO>(1000,true);
				
				
				
				for(int i = 0; i<threadBalance; i++){//add the balance of threads
					
					String service_processor_class_name = subdto.getProcessorClass();
					ServiceProcessorI processor = MOProcessorFactory.getProcessorClass(service_processor_class_name,ServiceProcessorI.class);
					
					processor.setName(i+"_"+service_processor_class_name);
					processor.setName(i+"_"+subdto.getServiceName());
					processor.setInternalQueue(50);
					logger.info("started : "+service_processor_class_name);
					Thread t = new Thread(processor);
					t.start();
					
					subdto.setProcessor(processor);
					
					subscr.add(subdto);
					
				}
				
				processor_map.put(subdto.getServiceid(), subscr);
			
			}
			
		} catch (Exception e) {
			
			log(e);
		
		}finally{
			
		}
		
	}
	
	
	
	public static long generateNextTxId() throws InterruptedException{
		
		try{
			
			uniq.acquire();
			try{
				Thread.sleep(1);
			}catch(Exception e){}
			
			return System.currentTimeMillis();
		}finally{
			
			uniq.release();
		
		}
		
	}		

	private void log(Exception e) {
		logger.error(e.getMessage(),e);
	}


	public void populateServicesToBePushed(){
		
		if(to_be_pushed==null)
			to_be_pushed = new ArrayBlockingQueue<ServiceSubscription>(1000,true);
		
		try {
			
			List<ServiceSubscription> servSub = cmpbean.getServiceSubscription();
			
			logger.info("servSub.size(): "+servSub.size());
			
			for(ServiceSubscription  subdto : servSub)
				to_be_pushed.put(subdto);
			
		}catch(java.lang.IllegalStateException ise){
			setRun(false);
		} catch (Exception e) {
			log(e);
		}finally{
			
		}
		
	}
	
	public void pushSubscriptions() throws Exception{
		
		StringBuffer sb = new StringBuffer();
		
		for(ServiceSubscription s : to_be_pushed){
			
			
			final int service_id = s.getServiceid();
			final int subscription_service_id = s.getId();
			int x = cmpbean.countSubscribers(service_id);
			int y = cmpbean.countPushesToday(service_id); 
			
			boolean pushnow = cmpbean.shouldPushNow(service_id);
			try{
				Thread.sleep(1000);
			}catch(Exception e){
				logger.error(e.getMessage(),e);
			}
			sb.append("\n\t\tSERVICE ID            : ").append(service_id);
			sb.append("\n\t\tTOTAL SUBSRIBERS      : ").append(x);
			sb.append("\n\t\t# SUBSCRIPTION PUSHES : ").append(y);
			sb.append("\n\t\tPUSH NOW : ").append(pushnow);
			logger.debug(sb.toString());
			
			sb.setLength(0);
			if((x>y) && pushnow)//if subscribers are more than the number of pushed count and it's the hour to push
				if(processor_map.size()>0){
					
					final String service_name = "SubscriptionOld thread:  " + processor_map.get(service_id).peek().getServiceName();
					
					ArrayBlockingQueue<SubscriptionDTO> processors = processor_map.get(service_id);
					
					SubscriptionWorker sw = new SubscriptionWorker(cmpbean, subscriptinoEJB,constr_,service_name,service_id,subscription_service_id,processors);
					Thread t = new Thread(sw);
					t.start();
					workers.add(sw);
					try{
						Thread.sleep(500);//sleep 1/2 second. Save CPU
					}catch(Exception e){
						logger.error(e.getMessage(),e);
					}
				}
			
			workers_cnt = workers.size();
				
		}
		
		
	}
	
	
	public static void stopApp() {
		System.out.println("SubscriptionMain about to shut down...");
		
		try{
			if(instance!=null){
				System.out.println("Shutting down...");
				instance.setRun(false);
				System.out.println("...");
				instance.instance.waitForQueueToBecomeEmpty();
				System.out.println("...");
				instance.instance.waitForAllWorkersToFinish();
				System.out.println("...");
				instance.instance.myfinalize();
				System.out.println("...");
			
			}else{
				
				System.out.println("App not yet initialized or started.");
			
			}
		}catch(Exception e){
			instance.logger.error(e.getMessage(),e);
		}catch(Error e){
			instance.logger.error(e.getMessage(),e);
		}
		
	}
	
	

	public void myfinalize() {

	}

	private void waitForAllWorkersToFinish() {
		
		workers_cnt = workers.size();
		
		boolean finished = false;
		
		//First and foremost, let all threads die if they finish to process what they're processing currently.
		//We don't interrupt them still..
		for(SubscriptionWorker tw : workers){
			try{
				if(tw.isBusy())
					tw.cancelBatch();
			}catch(Exception ex){
				logger.error(ex);
			}
		}
		
		//all unprocessed messages in queue are put back to the db.
		
		while(!finished){//Until all workers are idle or dead...
			
			idleWorkers = 0;
			
			for(SubscriptionWorker tw : workers){
				
				try{
					if(tw.isBusy())
						tw.cancelBatch();
				}catch(Exception ex){
					logger.error(ex);
				}
				
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
			
			finished = (workers_cnt == idleWorkers);
			
		
		}
			
		
	}
	

	private void waitForQueueToBecomeEmpty() {
		
		while(workers.size()>0){
			logger.info("workers.size() : "+workers.size());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(),e);
			}
		}
		logger.info("Queue is now empty, and all threads have been asked not to wait for elements in the queue!");
		notify();
	}

	public void finalizeMe(){
		
		setRun(false);
		
		
		try {
			
		} catch (Exception e1) {
			logger.error(e1.getMessage(),e1);
		}
			
		
		//Finalize all threads.
		for(Map.Entry<Integer, ArrayBlockingQueue<SubscriptionDTO>> entry : processor_map.entrySet()){
			
			Iterator<SubscriptionDTO> it = entry.getValue().iterator();
			
			SubscriptionDTO dto;
			
			while(it.hasNext()){
				
				dto = it.next();
				
				try{
					IncomingSMS incomingsms = new IncomingSMS();
					incomingsms.setCmp_tx_id("-1"); 
					dto.getProcessor().submit(incomingsms);
				}catch(Exception e){
					log(e);
				}
				
			}
			
		}
		
		
		
		boolean somebusy = true;
		
		int x = 0;
		
		while(somebusy){
			
			
			logger.debug("size: " +processor_map.size());
			
			
			//
			somebusy = false;
			//Finalize all threads.
			for(Map.Entry<Integer, ArrayBlockingQueue<SubscriptionDTO>> entry : processor_map.entrySet()){
				
				Iterator<SubscriptionDTO> it2 = entry.getValue().iterator();
				
				SubscriptionDTO dto;
				
				while(it2.hasNext()){
					
					x++;
					if(x==1)
						logger.debug("waiting");
					else
						logger.debug("...");
					dto = it2.next();
					
					
					logger.debug(" DTO:::: "+dto);
					try{
						if(dto.getProcessor().isRunning()){
							somebusy = true;
						}else{
							dto.getProcessor().setRun(false);
							dto.getProcessor().finalizeMe();
							
						}
					}catch(Exception e){
						log(e);
					}
					
				}
			}
			
			
			
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				log(e);
			}
		}
		
		
		//ds.releaseConnectionPool();
		
		logger.debug(">>>>>>>>>>>>>>>> SubscriptionOld program finished");
		//System.exit(0);
	
	}
	
	
	
	
	
	/**
	 * This gets the property file
	 * @param filename String the file name for the given property file
	 * @return java.util.Properties instance of the created property file
	 */
	private Properties getPropertyFile(String filename) {

		Properties prop = new Properties();
		InputStream inputStream = null;
		;
		String path;
		try {
			path = System.getProperty("user.dir")
					+ System.getProperty("file.separator") + filename;
			inputStream = new FileInputStream(path);
		} catch (Exception e) {
			URL urlpath = new String().getClass().getResource(filename);
			try {
				inputStream = new FileInputStream(urlpath.getPath());
			} catch (Exception exb) {
				logger.error(filename + " not found!");
			}
		}
		try {
			if (inputStream != null) {
				prop.load(inputStream);
				inputStream.close();

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return prop;
	}

	@Override
	public void run() {

		

		try{
			
			setRun(true);
			while(run){
				init_processor_map();
				populateServicesToBePushed();
				pushSubscriptions();
				try{
					Thread.sleep(1000);
				}catch(Exception e){
				}
			}
			
		}catch(Exception e){

			setRun(false);
			logger.error(e.getMessage(),e);
		}finally{
			finalizeMe();
		}
		
	}
	
	
}
