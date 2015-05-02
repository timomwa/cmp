package com.pixelandtag.sms.mt.workerthreads;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.ERROR;
import com.pixelandtag.autodraw.Alarm;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.BillableI;
import com.pixelandtag.sms.producerthreads.MTProducer;
import com.pixelandtag.sms.producerthreads.SubscriptionRenewal;
import com.pixelandtag.util.StopWatch;

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
	private Alarm alarm = new Alarm();
	private GenericHTTPClient genericHttpClient;
	private SubscriptionBeanI subscriptionejb;
	
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

	public SubscriptionBillingWorker(String name_, HttpClient httpclient_, CMPResourceBeanRemote cmpbean_, SubscriptionBeanI subscriptionejb_) throws Exception{
		 
		if(cmpbean_==null)
			throw new Exception("CMP EJB is nulll");
		if(subscriptionejb_==null)
			throw new Exception("CMP EJB is nulll");
		this.cmp_ejb = cmpbean_;
		this.subscriptionejb = subscriptionejb_;
		
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
					
					final Billable billable = SubscriptionRenewal.getBillable();
					
					try{
						
							logger.debug(getName()+" kujaribu kutafuta billable");
							
							logger.debug(getName()+":::: tumekamata moja hapa.."+billable);
							if(billable.getMsisdn()!=null && !billable.getMsisdn().isEmpty() && billable.getPrice()!=null && billable.getPrice().compareTo(BigDecimal.ZERO)>0){
								setBusy(true);
								logger.debug(getName()+":the service id in worker!::::: mtsms.getServiceID():: "+billable.toString());
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
								logger.info("\n\n\t\t::::::SMPP::::RESP_CODE=["+RESP_CODE+"]:::::PROXY_RESPONSE: "+resp);
								billable.setResp_status_code(String.valueOf(RESP_CODE));
								billable.setProcessed(1L);
								
								if (RESP_CODE == HttpStatus.SC_OK) {
									
									if(resp!=null)
									if(resp.toUpperCase().equalsIgnoreCase("SLAClusterEnforcementMediation".toUpperCase())){
										if(SubscriptionRenewal.isAdaptive_throttling()){
											//We've been throttled. Let's slow down a little bit.
											logger.debug("Throttling! We've been capped.");
											SubscriptionRenewal.setEnable_biller_random_throttling(true);
										}
										
									}else if(resp.toUpperCase().equalsIgnoreCase("Insufficient".toUpperCase())){
										//Resume back to normal. No throttling
										if(SubscriptionRenewal.isAdaptive_throttling()){
											SubscriptionRenewal.setEnable_biller_random_throttling(false);
										}
									}
									
									billable.setRetry_count(billable.getRetry_count()+1);
									this.success  = resp.toUpperCase().split("<STATUS>")[1].startsWith("SUCCESS");
									billable.setSuccess(this.success );
									
									
									if(!this.success){
										String err = getErrorCode(resp);
										String errMsg = getErrorMessage(resp);
										logger.debug("resp: :::::::::::::::::::::::::::::ERROR_CODE["+err+"]:::::::::::::::::::::: resp:");
										logger.debug("resp: :::::::::::::::::::::::::::::ERROR_MESSAGE["+errMsg+"]:::::::::::::::::::::: resp:");
										logger.info("FAILED TO BILL ERROR="+err+", ERROR_MESSAGE="+errMsg+" msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
										billable.setSuccess(false);
										billable.setResp_status_code(errMsg);
										
										
									}else{
										billable.setResp_status_code("Success");
										logger.debug("resp: :::::::::::::::::::::::::::::SUCCESS["+billable.isSuccess()+"]:::::::::::::::::::::: resp:");
										logger.info("SUCCESS BILLING msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
										billable.setSuccess(true);
										Subscription sub = subscriptionejb.renewSubscription(billable.getMsisdn(), Long.valueOf(billable.getService_id())); 
										logger.info(":::: SUBSCRIPTION RENEWED: "+sub.toString());
									
										if(SubscriptionRenewal.isAdaptive_throttling()){
											//Resume back to normal. No throttling
											SubscriptionRenewal.setEnable_biller_random_throttling(false);
										}
										
									}
									cmp_ejb.saveOrUpdate(billable);
									
								}else if(RESP_CODE == 400){
									this.success  = false;
								}else if(RESP_CODE == 401){
									this.success  = false;
									logger.error("\n::::::::::::::::: Unauthorized! :::::::::::::::");
									
								}else if(RESP_CODE == 404 || RESP_CODE == 403){
									
									this.success  = false;
									
								}else if(RESP_CODE == 503){
			
									this.success  = false;
									
								}else if(RESP_CODE==0){
									this.success = false;
									logger.info(" HTTP FAILED. WE TRY AGAIN LATER");
									subscriptionejb.updateQueueStatus(0L, billable.getMsisdn(), Long.valueOf(billable.getService_id()));
								
								}else{
									
									this.success  = false;
								
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
								
								}catch(Exception e){
									logger.error(e.getMessage(),e);
								}
								
								logger.debug("DONE! ");
								
							}else{
								if(billable.getMsisdn()!=null && !billable.getMsisdn().isEmpty()){
									Subscription sub = subscriptionejb.renewSubscription(billable.getMsisdn(), Long.valueOf(billable.getService_id())); 
									logger.info("No billing requred :::: SUBSCRIPTION RENEWED: "+sub.toString());
								}else{
									setRun(false);//Poison pill
									setFinished(true);
								}
							}
							
							setBusy(false);
							
					}catch(Exception exp){
						
						logger.error(exp.getMessage(),exp);
						logger.info("SUBSCRIPTION_RENEWAL:::::::::SOMETHING WENT WRONG, WE TRY AGAIN ");
						subscriptionejb.updateQueueStatus(0L, billable.getMsisdn(), Long.valueOf(billable.getService_id()));
						
					}
				
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
