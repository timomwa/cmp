package com.inmobia.celcom.mms.workerthreads;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.mail.MessagingException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.inmobia.axiata.connections.DriverUtilities;
import com.inmobia.axiata.web.triviaI.MechanicsI;
import com.inmobia.axiata.web.triviaImpl.MechanicsS;
import com.inmobia.celcom.autodraw.Alarm;
import com.inmobia.celcom.mms.producerthreads.MMSProducer;
import com.inmobia.celcom.sms.application.HTTPMTSenderApp;
import com.inmobia.mms.api.MM7Api;
import com.inmobia.mms.api.MM7DeliveryReport;
import com.inmobia.mms.api.MM7_Submit_req;
import com.inmobia.mms.api.MMS;
import com.inmobia.mms.apiImpl.MMSApiImpl;
/**
 * 
 * @author Timothy Mwangi Gikonyo
 *
 */
public class MMSSender implements Runnable {
	
	private  final String SUCCESS = "MMS_SENT_SUCCESSFULLY ";
	private Logger logger = Logger.getLogger(MMSSender.class);
	private volatile boolean  busy = false;
	private volatile boolean run = true;
	private volatile boolean finished = false;
	private String name;
	private MM7Api mm7API;
	private DBPoolDataSource ds;
	private MM7_Submit_req mm7_submit_req;
	//private SOAPMessage message;
	//private MM7DeliveryReport instantDLR; 
	//private MMS mms;
	private long throttle;
	private Alarm alarm = new Alarm();
	
	
	public MMSSender(String name, String ws_endpointURL, long throttle) throws Exception{
		
		this.name = name;
		this.throttle = throttle; 
		
		int vendor = DriverUtilities.MYSQL;
		String driver = DriverUtilities.getDriver(vendor);
		String host = "db";
		String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
		String url = DriverUtilities.makeURL(host, dbName, vendor);
		
		    
		ds = new DBPoolDataSource();
	    ds.setName("MMS_S_"+name);
	    ds.setDescription("MM7 Sender thread : "+ds.getName());
	    ds.setDriverClassName(driver);
	    ds.setUrl(url);
	    ds.setUser("root");
	    ds.setPassword("");
	    ds.setMinPool(1);
	    ds.setMaxPool(2);
	    ds.setMaxSize(3);
	    ds.setIdleTimeout(3600);  // Specified in seconds.
	    ds.setValidationQuery("SELECT COUNT(*) FROM `celcom`.`sms_service`");
	    //ds.registerShutdownHook();
		mm7API = new MMSApiImpl(ds);
		mm7API.setFr_tz("+08:00");
		mm7API.setTo_tz("+08:00");
		mm7_submit_req = new MM7_Submit_req(ws_endpointURL);
		
		
		
	}
	
	
	public void myfinalize(){
		
		this.mm7API.myfinalize();
		
		this.ds.releaseConnectionPool();
	
	}
	
	 
	
	private void log(Exception e){
		logger.error(e.getMessage(),e);
	}
	

	public boolean isBusy() {
		return busy;
	}


	public boolean isFinished() {
		return finished;
	}


	private void setBusy(boolean busy) {
		this.busy = busy;
	}


	public void setFinished(boolean finished) {
		this.finished = finished;
	}


	@Override
	public void run() {
		
		pauze();//we wait for producer to initialize completely
		
		logger.info(getName()+" Releazed by producer! Starting");
		
		while(run){
			
			try {
				
				final MMS mms = MMSProducer.getMTMMS();
				
				logger.debug(":::::::::: got mms to send "+mms.toString());
				
				if(mms!=null){
					
					if(!mms.getId().equals("-1"))
						sendMMS(mms);
				
				}
				
				Thread.sleep(this.throttle);
				
			} catch (NullPointerException e) {
				
				log(e);
			
			} catch (InterruptedException e) {
				
				log(e);
			
			}catch (Exception e) {
				
				log(e);
			
			}finally{
				
				setBusy(false);
			
			}
			
		}
		
		setFinished(true);
		setBusy(false);
		
		if(isFinished() && !isBusy())
			logger.info(getName()+": mm7 thread was shut down safely...");
		
		
	}
	
	
	private void sendMMS(MMS mms) {
		
		this.busy = true;
	
		try {
			
			final SOAPMessage message = mm7_submit_req.submit(mms);
			
			final MM7DeliveryReport instantDLR = new MM7DeliveryReport(message);
			
			try{
				
				mms.setSent(instantDLR.isSuccess());
				mm7API.logMTMMS(mms);
				
			
			}catch(Exception e){
				
				log(e);
			
			}
			
			if(instantDLR.isSuccess()){
				logger.info(SUCCESS+instantDLR.isSuccess());
					mm7API.deleteMMSMTToSend(mms.getId());
			}else{
				//re-try later
				logger.info("WAS NOT ABLE TO SEND MMS: "+mms.toString());
				mm7API.toggleInProcessingQueue(mms.getId(), false);
				
			}
		
		}catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
			Connection conn = null;
			
			try{
				conn = ds.getConnection();
				
				alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: ERROR: ", "Hi,\n\n mms:"+mms.getMedia_path()+"\n\n"+e.getMessage()+"\n\n Stacktrace:\n\n"+MechanicsS.stackTraceToString(e)+"\n\n  Regards");
				
			}catch(Exception e3){
				
				log(e3);
				
			}finally{
				
				try {
					if(conn!=null)
						conn.close();
				} catch (Exception e1) {
					log(e1);
				}
				
			}
		
		}catch (java.lang.OutOfMemoryError e){
			
			logger.error(e.getMessage(),e);
			
			Connection conn = null;
			
			try{
				conn = ds.getConnection();
				
				alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: SERVERE: OUT OF MEMORY WHILE SENDING MMS", "Hi,\n\n mms:"+mms.getMedia_path()+"\n\n"+MechanicsS.stackTraceToString(e)+"\n\n  Regards");
				
			}catch(Exception e3){
				
				log(e3);
				
			}finally{
				
				try {
					if(conn!=null)
						conn.close();
				} catch (Exception e1) {
					log(e1);
				}
				
			}
		}
		
		
		this.busy = false;
		
	}


	public synchronized void rezume(){
		this.notify();
	}
	
	public synchronized void pauze(){
		
		try {
			
			this.wait();
		
		} catch (InterruptedException e) {
			
			logger.debug(getName()+" we now run!");
		
		}
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRunning() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}
	

}
