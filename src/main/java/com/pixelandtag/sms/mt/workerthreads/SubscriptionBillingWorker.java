package com.pixelandtag.sms.mt.workerthreads;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import com.pixelandtag.util.StopWatch;
import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.ERROR;
import com.pixelandtag.autodraw.Alarm;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.entities.URLParams;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.BillableI;
import com.pixelandtag.sms.producerthreads.BillingService;
import com.pixelandtag.sms.producerthreads.MTProducer;

public class SubscriptionBillingWorker implements Runnable {
	
	private static Logger logger = Logger.getLogger(SubscriptionBillingWorker.class);
	
	private  Context context;
	private StopWatch watch;
	private boolean run = true;
	private boolean finished = false;
	private String name;
	private boolean busy = false;
	private volatile boolean success = true;
	private volatile String message = "";
	private volatile int sms_idx = 0;
	private volatile HttpResponse response;
	private volatile int recursiveCounter = 0;
	private Alarm alarm = new Alarm();
	private GenericHTTPClient genericHttpClient;
	
	
	
	public boolean isBusy() {
		return busy;
	}

	private synchronized void setBusy(boolean busy) {
		this.busy = busy;
		notify();
	}

	public boolean isFinished() {
		return finished;
	}

	private void setFinished(boolean finished) {
		this.finished = finished;
	}

	private URLParams urlp;
	
	private String mtUrl = "https://41.223.58.133:8443/ChargingServiceFlowWeb/sca/ChargingExport1";

	private CMPResourceBeanRemote cmp_ejb;

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

	public SubscriptionBillingWorker(String name_, HttpClient httpclient_, CMPResourceBeanRemote cmpbean) throws Exception{
		 
		this.watch = new StopWatch();
		
		this.name = name_;
		
		watch.start();
		
		genericHttpClient = new GenericHTTPClient(httpclient_);
		
  
	}


	@SuppressWarnings("restriction")
	public void run() {
		
		try{
			
			pauze();//wait while producer gets ready
			
			
			watch.stop();
			
			logger.info(getName()+" STARTED AFTER :::::RELEASED_BY_PRODUCER after "+(Double.parseDouble(watch.elapsedTime(TimeUnit.MILLISECONDS)+"")) + " mili-seconds");
			
			watch.reset();
			
			GenericHTTPParam param = new GenericHTTPParam();
			param.setUrl(mtUrl);
			
			Map<String,String> headerattrs = new HashMap<String,String>();
			
			String usernamePassword = "CONTENT360_KE" + ":" + "4ecf#hjsan7"; // Username and password will be provided by TWSS Admin
			String encoding = null;
			sun.misc.BASE64Encoder encoder = (sun.misc.BASE64Encoder) Class.forName( "sun.misc.BASE64Encoder" ).newInstance(); 
			encoding = encoder.encode( usernamePassword.getBytes() ); 
			headerattrs.put("Authorization", "Basic " + encoding);
			headerattrs.put("SOAPAction","");
			headerattrs.put("Content-Type","text/xml; charset=utf-8");

			while(run){
				
				try {
					final Billable billable = BillingService.getBillable();
					
					logger.debug(":the service id in worker!::::: mtsms.getServiceID():: "+billable.toString());
					
					String xml = billable.getChargeXML(BillableI.plainchargeXML);
					logger.info("BILLABLE: "+billable.toString());
					logger.debug("XML SENT \n : "+xml + "\n");
					param.setStringentity(xml);
					param.setHeaderParams(headerattrs);

					watch.start();
					final int RESP_CODE = genericHttpClient.call(param);
					watch.stop();
					logger.info(getName()+" PROXY_LATENCY_ON  ("+param.getUrl()+")::::::::::  "+(Double.parseDouble(watch.elapsedTime(TimeUnit.MILLISECONDS)+"")) + " mili-seconds");
					watch.reset();
					final String resp = genericHttpClient.getRespose_msg();
					logger.info("\n\n\t\t::::::SMPP:::::::::PROXY_RESP_CODE: "+RESP_CODE);
					logger.info("\n\n\t\t::::::SMPP:::::::::PROXY_RESPONSE: "+message);
					
					
					billable.setResp_status_code(String.valueOf(RESP_CODE));
					billable.setProcessed(1L);
					
					if (RESP_CODE == HttpStatus.SC_OK) {
						
						billable.setRetry_count(billable.getRetry_count()+1);
						
						this.success  = resp.toUpperCase().split("<STATUS>")[1].startsWith("SUCCESS");
						billable.setSuccess(this.success );
						
						if(!this.success){
							
							String err = getErrorCode(resp);
							String errMsg = getErrorMessage(resp);
							logger.debug("resp: :::::::::::::::::::::::::::::ERROR_CODE["+err+"]:::::::::::::::::::::: resp:");
							logger.debug("resp: :::::::::::::::::::::::::::::ERROR_MESSAGE["+errMsg+"]:::::::::::::::::::::: resp:");
							logger.info("FAILED TO BILL ERROR="+err+", ERROR_MESSAGE="+errMsg+" msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
							
						}else{
							billable.setResp_status_code("Success");
							logger.debug("resp: :::::::::::::::::::::::::::::SUCCESS["+billable.isSuccess()+"]:::::::::::::::::::::: resp:");
							logger.info("SUCCESS BILLING msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
						}
						
										
					}else if(RESP_CODE == 400){
						
						this.success  = false;
						
					}else if(RESP_CODE == 401){
						this.success  = false;
						
						logger.error("\nUnauthorized!");
						
					}else if(RESP_CODE == 404 || RESP_CODE == 403){
						
						this.success  = false;
						
					}else if(RESP_CODE == 503){

						this.success  = false;
						
					}else{
						
						this.success = false;
						
					}

					
					logger.debug(getName()+" ::::::: finished attempt to bill via HTTP");
					
					try{
						
						billable.setProcessed(1L);
						billable.setIn_outgoing_queue(0L);
						
						if(billable.isSuccess() ||  "Success".equals(billable.getResp_status_code()) ){
							cmp_ejb.updateMessageInQueue(billable.getCp_tx_id(),BillingStatus.SUCCESSFULLY_BILLED);
							cmp_ejb.updateSMSStatLog(billable.getCp_tx_id(),ERROR.Success);
							billable.setResp_status_code(BillingStatus.SUCCESSFULLY_BILLED.toString());
						}
						if("TWSS_101".equals(billable.getResp_status_code()) || "TWSS_114".equals(billable.getResp_status_code()) || "TWSS_101".equals(billable.getResp_status_code())){
							cmp_ejb.updateMessageInQueue(billable.getCp_tx_id(),BillingStatus.BILLING_FAILED_PERMANENTLY);
							cmp_ejb.updateSMSStatLog(billable.getCp_tx_id(),ERROR.InvalidSubscriber);
							billable.setResp_status_code(BillingStatus.BILLING_FAILED_PERMANENTLY.toString());
						}
						if("OL402".equals(billable.getResp_status_code()) || "OL404".equals(billable.getResp_status_code()) || "OL405".equals(billable.getResp_status_code())  || "OL406".equals(billable.getResp_status_code())){
							cmp_ejb.updateMessageInQueue(billable.getCp_tx_id(),BillingStatus.INSUFFICIENT_FUNDS);
							cmp_ejb.updateSMSStatLog(billable.getCp_tx_id(),ERROR.PSAInsufficientBalance);
							billable.setResp_status_code(BillingStatus.INSUFFICIENT_FUNDS.toString());
						}
						
						if("TWSS_109".equals(billable.getResp_status_code())){
							cmp_ejb.updateSMSStatLog(billable.getCp_tx_id(),ERROR.PSAChargeFailure);
							billable.setIn_outgoing_queue(0L);
							billable.setProcessed(0L);
							billable.setRetry_count( (billable.getRetry_count()+1 ) );
							billable.setMaxRetriesAllowed(5L);
							billable.setResp_status_code(BillingStatus.BILLING_FAILED.toString());
						}
				
						cmp_ejb.saveOrUpdate(billable);
					
					}catch(Exception e){
						logger.error(e.getMessage(),e);
					}
					
					watch.reset();
					logger.debug("DONE! ");
					
					setBusy(false);
					
				}catch (Exception e){
					
					log(e);
					
				}finally{
				
				}
				
			}
			
			setFinished(true);
			
			setBusy(false);
			
			logger.info(getName()+": worker shut down safely!");
		
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}catch(OutOfMemoryError e){
			logger.fatal("NEEDS RESTART: MEM_USAGE: "+MTProducer.getMemoryUsage() +" >> "+e.getMessage(),e);
		}finally{
			
		    if(context!=null) 
		    	try { 
		    		context.close(); 
		    	}catch(Exception ex) { ex.printStackTrace(); }
		} 
		
	}
	
	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}
		
	private  String getErrorMessage(String resp) {
		int start = resp.indexOf("<errorMessage>")+"<errorMessage>".length();
		int end  = resp.indexOf("</errorMessage>");
		return resp.substring(start, end);
	}
	private String getErrorCode(String resp) {
		int start = resp.indexOf("<errorCode>")+"<errorCode>".length();
		int end  = resp.indexOf("</errorCode>");
		return resp.substring(start, end);
	}


}
