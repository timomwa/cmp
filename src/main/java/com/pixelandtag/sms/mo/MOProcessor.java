package com.pixelandtag.sms.mo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.CelcomHTTPAPI;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.api.ServiceProcessorI;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.sms.producerthreads.MTProducer;
import com.pixelandtag.sms.producerthreads.NoServiceProcessorException;
import com.pixelandtag.util.StopWatch;
import com.pixelandtag.web.triviaI.MechanicsI;

/**
 * Processes all MO Messagess
 * 
 * @author Timothy Mwangi
 * 
 */

public class MOProcessor implements Runnable {

	private volatile boolean run = true;
	private volatile String name;
	private CelcomHTTPAPI celcomAPI;
	private volatile StopWatch watch;
	private boolean finished = false;
	private boolean busy = false;
	private String connStr;
	// private volatile ServiceProcessorI procesor = null;
	// private volatile Queue<MOSms> moSMSSes;
	private final Logger logger = Logger.getLogger(MOProcessor.class);
	private volatile int size;
	//private Map<String, ServiceProcessorI> serviceMap;
	//private volatile Map<String, Boolean> split_msg_map;
	private volatile Map<Integer, ServiceProcessorDTO> processorDtos;
	private DBPoolDataSource ds;
	//private final String DEFAULT_PROCESSOR = "com.inmobia.celcom.serviceprocessors.sms.DefaultProcessor";

	/*
	 * public ServiceProcessorI getProcesor() { return procesor; }
	 * 
	 * 
	 * private synchronized void setProcesor(ServiceProcessorI procesor) {
	 * this.procesor = procesor; this.notify(); }
	 */

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	public MOProcessor(String connstr, String name) throws Exception {

		//this.split_msg_map = split_msg_map_;

		//this.serviceMap = serviceMap;

		this.name = name;
		

		this.connStr = connstr;
		int vendor = DriverUtilities.MYSQL;
	    String driver = DriverUtilities.getDriver(vendor);
	    String host =  HTTPMTSenderApp.props.getProperty("db_host");
	    String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
	    String url = DriverUtilities.makeURL(host, dbName, vendor);
	    String username = HTTPMTSenderApp.props.getProperty("db_username");
	    String password = HTTPMTSenderApp.props.getProperty("db_password");
	    
		ds = new DBPoolDataSource();
	    ds.setName("mo-processor-pool");
	    ds.setDescription("Pooling DataSource");
	    ds.setDriverClassName("com.mysql.jdbc.Driver");
	    ds.setUrl(url);
	    ds.setUser(username);
	    ds.setPassword(password);
	    ds.setMinPool(1);
	    ds.setMaxPool(2);
	    ds.setMaxSize(3);
	    ds.setIdleTimeout(3600);  // Specified in seconds.
	    ds.setValidationQuery("SELECT 'Test'");
	    

		celcomAPI = new CelcomImpl(ds);

		watch = new StopWatch();

		processorDtos = new HashMap<Integer, ServiceProcessorDTO>();

		Queue<ServiceProcessorDTO> proc =  celcomAPI.getServiceProcessors();
		
		Iterator<ServiceProcessorDTO> it = proc.iterator();
		
		while(it.hasNext()) {

			ServiceProcessorDTO dto = it.next();
			
			processorDtos.put(dto.getId(), dto);
			
		}
		

	}

	public void run() {

		while (run) {

			try {
				final Queue<MOSms> moSMSSes = celcomAPI.getLatestMO(1000);
				// TODO - externalize the number of MO's we can fetch from db.
				// we might need to change this during high traffic.

				if (moSMSSes != null) {

					size = moSMSSes.size();

					if (size > 0) {

						setBusy(true);

						watch.start();
						
						for (MOSms moSms : moSMSSes) {
							
							logger.debug(">>>>>>>>>>> moSms.getProcessor_id() : "+moSms.getProcessor_id());
							
							try{
								
								final ServiceProcessorI servp = MTProducer.getFreeProcessor(moSms.getProcessor_id());
								
								logger.debug(MTProducer.processor_pool+" gugamuga_processor : \n\n"+servp);
								
								if(servp!=null){
									boolean success = servp.submit(moSms);
								}else{
									logger.warn(":::::: COULD not get a free processor with processor id: "+moSms.getProcessor_id()+" at the moment");
									//put sms back in queue?
								}
							}catch(NoServiceProcessorException spe){
								logger.error(spe.getMessage());
							}finally{
							}
						
						}

						

						 logger.debug(getName()+
						 ": it took "+(watch.elapsedTime(TimeUnit.MILLISECONDS)/1000d)+" to process "+size+
						 " MO messages");

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

					Thread.sleep(500);

				} catch (InterruptedException e) {

					logger.error(e.getMessage(), e);

				}

			} catch (Exception e) {

				logger.error(e.getMessage(), e);

			} finally {

			}

		}
		
		setFinished(true);
		
		logger.info("MO Processor was shut down safely");

	}
	
	
}
