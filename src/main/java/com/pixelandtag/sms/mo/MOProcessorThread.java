package com.pixelandtag.sms.mo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.MOProcessorFactory;
import com.pixelandtag.api.ServiceProcessorI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.sms.producerthreads.NoServiceProcessorException;
import com.pixelandtag.util.FileUtils;
import com.pixelandtag.util.StopWatch;

/**
 * Processes all MO Messagess
 * 
 * @author Timothy Mwangi
 * 
 */

public class MOProcessorThread extends Thread {
	
	private volatile boolean run = true;
	private volatile StopWatch watch;
	private boolean finished = false;
	private boolean busy = false;
	private String connStr;
	private  CMPResourceBeanRemote cmpejb;
	public static volatile List<ServiceProcessorDTO> serviceProcessors;
	private final Logger logger = Logger.getLogger(MOProcessorThread.class);
	private volatile int size;
	public static volatile Map<Integer,ArrayList<ServiceProcessorI>> processor_pool = new ConcurrentHashMap<Integer,ArrayList<ServiceProcessorI>>();
	//private volatile Map<Integer, ServiceProcessorDTO> processorDtos;
	private DBPoolDataSource ds;
	private Context context;
	private QueueProcessorEJBI queueprocbean;
	private Properties mtsenderprop;
	private int internalqueue = 5;
	private int mopollwait = 1000;
	
	private void initEJBs() throws NamingException {
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		try{
			internalqueue = Integer.valueOf( mtsenderprop.getProperty("internalqueue") );
		}catch(NumberFormatException exp){
			logger.error(exp.getMessage(), exp);
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
		
		try{
			mopollwait = Integer.valueOf( mtsenderprop.getProperty("mopollwait") );
		}catch(NumberFormatException exp){
			logger.error(exp.getMessage(), exp);
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
		
		
		String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
	 	Properties props = new Properties();
	 	props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
	 	props.put(Context.PROVIDER_URL, "remote://"+mtsenderprop.getProperty("ejbhost")+":"+mtsenderprop.getProperty("ejbhostport"));
	 	props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
	 	props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
	 	props.put("jboss.naming.client.ejb.context", true);
	 	context = new InitialContext(props);
	 	queueprocbean =  (QueueProcessorEJBI) context.lookup("cmp/QueueProcessorEJBImpl!com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI");
	 	cmpejb =  (CMPResourceBeanRemote) context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
	 	logger.info(getClass().getSimpleName()+": Successfully initialized EJB QueueProcessorEJBImpl !!");
	}
	
	

	private void initProcessorCache() throws Exception{
		
		serviceProcessors = cmpejb.getServiceProcessors();
		
		System.out.println("\n\n\n\n\t\t serviceProcessors.size() :: "+serviceProcessors);
		
		ServiceProcessorI p = null;
		
		int processorThreadsStarted = 0;
		if(serviceProcessors!=null){
			for(ServiceProcessorDTO servicep : serviceProcessors){
				
				final int processor_threads = servicep.getThreads();
				
				ArrayList<ServiceProcessorI> processor_array = new ArrayList<ServiceProcessorI>();
				
				for(int x = 0;x<processor_threads; x++){
					p = MOProcessorFactory.getProcessorClass(servicep.getProcessorClassName(),ServiceProcessorI.class);//Create new instances of each proccessor pool item
					p.setName(x+"_"+servicep.getId()+"_"+servicep.getShortcode()+"_"+"_"+servicep.getServiceName());
					p.setInternalQueue(internalqueue);
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
		
		logger.debug(processorThreadsStarted+" processor threads started");
	
	}

	public boolean isRunning() {
		return run;
	}

	public boolean isFinished() {
		return finished;
	}

	public boolean isBusy() {
		return busy;
	}

	public void setRuning(boolean run) {
		this.run = run;
	}

	private void setFinished(boolean finished) {
		this.finished = finished;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}

	
	public void myfinalize(){
		ds.releaseConnectionPool();
		ds.release();
	}
	
	
	public MOProcessorThread() throws Exception {

		watch = new StopWatch();

		//processorDtos = new HashMap<Integer, ServiceProcessorDTO>();

		initEJBs();
		initProcessorCache();
		
		/*List<ServiceProcessorDTO> proc =  cmpejb.getServiceProcessors();
		
		Iterator<ServiceProcessorDTO> it = proc.iterator();
		
		while(it.hasNext()) {
			ServiceProcessorDTO dto = it.next();
			processorDtos.put(dto.getId(), dto);
		}*/
		
	}

	
	public void run() {

		while (run) {

			try {
				
				final List<IncomingSMS> incomingsmses =  queueprocbean.getLatestMO(1000);
				
				if (incomingsmses != null) {

					size = incomingsmses.size();

					if (size > 0) {

						setBusy(true);

						watch.start();
						
						for (IncomingSMS incomingsms : incomingsmses) {
							
							logger.debug(">>>>>>>>>>> moSms.getMoprocessor().getId() : "+incomingsms.getMoprocessor().getId());
							
							try{
								
								final ServiceProcessorI servp = getFreeProcessor(incomingsms.getMoprocessor().getId());
								
								logger.debug(processor_pool+" gugamuga_processor : \n\n"+servp);
								
								if(servp!=null){

									boolean success = servp.submit(incomingsms);  
									
									if(success){
										try{
											incomingsms.setMo_ack(Boolean.TRUE); 
											incomingsms.setProcessed(Boolean.TRUE);
											incomingsms = queueprocbean.saveOrUpdate(incomingsms);
										}catch(Exception exp){
											logger.error(exp.getMessage(),exp);
										}
									}
										
								}else{
									logger.warn(":::::: COULD not get a free processor with processor id: "+incomingsms.getMoprocessor().getId()+" at the moment");
								}
								
							}catch(NoServiceProcessorException spe){
								
								logger.error(spe.getMessage());
							
							}finally{
							}
						
						}

						logger.debug(getClass().getSimpleName()+": it took "+(watch.elapsedTime(TimeUnit.MILLISECONDS)/1000d)+" to process "+size+" MO messages");

						watch.reset();

						setBusy(false);

					}

					size = 0;

				}

				// TO-DO Get all new MO Messages
				// TO-DO Find the appropriate application to process the MO
				// TO-DO Process the MO and get the response text.
				// TO-DO Determine whether to bill or not, if billing required
				// do the necessary.
				// TO-DO Put in outgoing queue.
				// TO-DO Outgoing queue processors should mark the message as
				// sent if its the case...

				try {

					Thread.sleep(mopollwait);

				} catch (InterruptedException e) {

					logger.error(e.getMessage(), e);

				}catch (Exception e) {

					logger.error(e.getMessage(), e);

				}

			} catch (Exception e) {

				logger.error(e.getMessage(), e);

			} finally {
				finalizeMe();
			}

		}
		
		setFinished(true);
		
		logger.info("MO Processor was shut down safely");

	}
	
	
	
	private void finalizeMe() {
		try {
			context.close();
		} catch (Exception e) {
		}
		
	}



	/**
	 * Gets a free processor Thread for the processor class passed.
	 * @param mo_processor_id int
	 * @return
	 * @throws NoServiceProcessorException 
	 */
	public ServiceProcessorI getFreeProcessor(Long mo_processor_id) throws NoServiceProcessorException{
		
		logger.debug("gugamuga_processor_pool: : "+processor_pool);
		
		ArrayList<ServiceProcessorI> processorPool = processor_pool.get(mo_processor_id.intValue());
		
		//TODO add a semaphore so only one thread accesses this at a time.
		//TODO check for processorPool object bing null then, re-direct that message to an existing processor pool
	    //TODO or, edit the mo receiver so that it does not queue for disabled mo processors
		if(processorPool==null)
			throw new NoServiceProcessorException("No service processors pooled! for mo processor id"+mo_processor_id);
		Iterator<ServiceProcessorI> it = processorPool.iterator();
		
		ServiceProcessorI proc;
		int poolsize = processorPool.size();
		int busy = 0;
		while(it.hasNext()){
			proc = it.next();
			if(!proc.queueFull()){
				logger.info("We have "+poolsize+" processors for the processor with id "+mo_processor_id+", busy: "+busy+", availabe: "+ (poolsize-busy));
				return proc;
			}
			busy++;
		}
		logger.info("We have "+poolsize+" processors for the processor with id "+mo_processor_id+", busy: "+busy+", availabe: "+ (poolsize-busy));
		
		
		return null;
	}
	
}
