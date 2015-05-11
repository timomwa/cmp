package com.pixelandtag.sms.mt.workerthreads;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
import com.pixelandtag.cmp.entities.MOProcessorE;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.BillableI;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.sms.producerthreads.MTProducer;
import com.pixelandtag.sms.producerthreads.Operation;
import com.pixelandtag.sms.producerthreads.SubscriptionRenewal;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.util.StopWatch;

public class SubscriptionBillingWorker implements Runnable {
	
	private static Logger logger = Logger.getLogger(SubscriptionBillingWorker.class);
	
	private  Context context;
	private StopWatch watch;
	private boolean run = true;
	private boolean finished = false;
	private String name;
	private int mandatory_throttle;
	private boolean busy = false;
	//private volatile boolean success = true;
	private volatile String message = "";
	private Alarm alarm = new Alarm();
	private GenericHTTPClient genericHttpClient;
	private SubscriptionBeanI subscriptionejb;
	private static Random r = new Random();
	private Map<Long, SMSService> sms_serviceCache = new HashMap<Long, SMSService>();
	private Map<Long, MOProcessorE> mo_processorCache = new HashMap<Long, MOProcessorE>();
	
	
	private int getRandom(){
		return (r.nextInt(1000-0) + 0) ;
	}
	
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
			logger.info(getName()+" I've been told to wait. I will not run.");
			this.wait();
		
		} catch (InterruptedException e) {
			
			logger.debug(getName()+" we now run!");
		
		}
	}

	public SubscriptionBillingWorker(String name_, CMPResourceBeanRemote cmpbean_, SubscriptionBeanI subscriptionejb_, int mandatory_throttle_) throws Exception{
		 
		if(cmpbean_==null)
			throw new Exception("CMP EJB is nulll");
		if(subscriptionejb_==null)
			throw new Exception("CMP EJB is nulll");
		this.cmp_ejb = cmpbean_;
		this.subscriptionejb = subscriptionejb_;
		
		this.watch = new StopWatch();
		
		this.name = name_;
		
		this.mandatory_throttle = mandatory_throttle_;
		
		watch.start();
		
		genericHttpClient = new GenericHTTPClient("https");
		
  
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

			Long negative_one = new Long(-1);
			
			while(run){
				
				try {
					
					Long sub_id = SubscriptionRenewal.getBillable();
					Subscription sub = sub_id!=null ? cmp_ejb.find(Subscription.class, sub_id) : null;
					Billable billable = null;
					
					if(sub_id.compareTo(negative_one)==0){//poison pill
						setRun(false);
						setFinished(true);
						setBusy(false);
					}
					
					
					if(sub!=null && sub_id.compareTo(negative_one)>0){
						
						
						if(sub.getSubscription_status()==SubscriptionStatus.confirmed){
							sub.setQueue_status(1L);
							sub = cmp_ejb.saveOrUpdate(sub);
							billable = createBillableFromSubscription(sub);
						}
					}
					
					//if such a billable exists
					if(billable!=null){
						try{
							
								if(billable.getMsisdn()!=null && !billable.getMsisdn().isEmpty() && billable.getPrice()!=null && billable.getPrice().compareTo(BigDecimal.ZERO)>0){
									setBusy(true);
									logger.debug(getName()+":the service id in worker!::::: mtsms.getServiceID():: "+billable.toString());
									String xml = billable.getChargeXML(BillableI.plainchargeXML);
									logger.info("BILLABLE: "+billable.toString());
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
										boolean capped= resp.toUpperCase().contains("SLAClusterEnforcementMediation".toUpperCase());
										String debug = "capped\t\t ::"+capped;
										debug = debug +"SubscriptionRenewal.isAdaptive_throttling():\t\t "+SubscriptionRenewal.isAdaptive_throttling();
										
										logger.debug("THROTTLING PARAMS :::::: "+debug);
										if(resp!=null)
										if(capped){
											if(SubscriptionRenewal.isAdaptive_throttling()){
												//We've been throttled. Let's slow down a little bit.
												logger.debug("Throttling! We've been capped.");
												SubscriptionRenewal.setEnable_biller_random_throttling(true);
												SubscriptionRenewal.setWe_ve_been_capped(true);
												long wait_time = SubscriptionRenewal.getRandomWaitTime();
												logger.info(getName()+" ::: CHILAXING::::::: Trying to chillax for "+wait_time+" milliseconds");
												if(wait_time>-1){
													Thread.sleep(wait_time);
												}
											}
											
											billable.setSuccess(Boolean.FALSE);
											
										}else if(resp.toUpperCase().contains("Insufficient".toUpperCase())){
											
											//Resume back to normal. No throttling
											if(SubscriptionRenewal.isAdaptive_throttling()){
												SubscriptionRenewal.setEnable_biller_random_throttling(false);
												SubscriptionRenewal.setWe_ve_been_capped(false);
											}
											billable.setSuccess(Boolean.FALSE);
										}
										
										billable.setRetry_count(billable.getRetry_count()+1);
										final Boolean success  = Boolean.valueOf(resp.toUpperCase().split("<STATUS>")[1].startsWith("SUCCESS"));
										
										if(success.booleanValue()==false){
											String err = getErrorCode(resp);
											String errMsg = getErrorMessage(resp);
											logger.debug("resp: :::::::::::::::::::::::::::::ERROR_CODE["+err+"]:::::::::::::::::::::: resp:");
											logger.debug("resp: :::::::::::::::::::::::::::::ERROR_MESSAGE["+errMsg+"]:::::::::::::::::::::: resp:");
											logger.info("FAILED TO BILL ERROR="+err+", ERROR_MESSAGE="+errMsg+" msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
											//billable.setSuccess(false);
											try{
												String transactionId = getTransactionId(resp);
												billable.setTransactionId(transactionId);
											}catch(Exception exp){
												logger.warn("No transaction id found");
											}
											
											billable.setResp_status_code(errMsg);
											billable.setSuccess(Boolean.FALSE);
											
											if(resp.toUpperCase().contains("Insufficient".toUpperCase())){
												subscriptionejb.updateCredibilityIndex(billable.getMsisdn(),Long.valueOf(billable.getService_id()),-1);
												//we'll try again. 1 means that we let it sit there, but the cron will set it to 2 so that it's picked
												subscriptionejb.updateQueueStatus(1L, billable.getMsisdn(), Long.valueOf(billable.getService_id()));
											}
											
										}else{
											
											String transactionId = getTransactionId(resp);
											billable.setTransactionId(transactionId);
											billable.setResp_status_code("Success");
											logger.info("SUCCESS BILLING msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
											billable.setSuccess(Boolean.TRUE);
											sub = subscriptionejb.renewSubscription(billable.getMsisdn(), Long.valueOf(billable.getService_id())); 
											subscriptionejb.updateCredibilityIndex(billable.getMsisdn(),Long.valueOf(billable.getService_id()),1);
											if(SubscriptionRenewal.isAdaptive_throttling()){
												//Resume back to normal. No throttling
												SubscriptionRenewal.setEnable_biller_random_throttling(false);
												SubscriptionRenewal.setWe_ve_been_capped(false);
											}
											cmp_ejb.createSuccesBillRec(billable);
										}
										
										
									}else if(RESP_CODE==0){
										logger.info(" HTTP FAILED. WE TRY AGAIN LATER");
										subscriptionejb.updateQueueStatus(2L, billable.getMsisdn(), Long.valueOf(billable.getService_id()));
									
									}
									
									logger.debug(getName()+" ::::::: finished attempt to bill via HTTP");
									
									billable.setProcessed(1L);
									billable.setIn_outgoing_queue(0L);
									
									logger.debug("DONE! ");
									
									billable = cmp_ejb.saveOrUpdate(billable);
									
									setBusy(false);
									
								}else{
									if(billable.getMsisdn()!=null && !billable.getMsisdn().isEmpty() && billable.getPrice().compareTo(BigDecimal.ZERO)<=0){
										sub = subscriptionejb.renewSubscription(billable.getMsisdn(), Long.valueOf(billable.getService_id())); 
										logger.info("No billing requred :::: SUBSCRIPTION RENEWED: "+sub.toString());
									}else{
										setRun(false);//Poison pill
										setFinished(true);
									}
								}
								
								
								
						}catch(Exception exp){
							
							logger.error(exp.getMessage(),exp);
							logger.info("SUBSCRIPTION_RENEWAL:::::::::SOMETHING WENT WRONG, WE TRY AGAIN ");
							subscriptionejb.updateQueueStatus(0L, billable.getMsisdn(), Long.valueOf(billable.getService_id()));
							
						}
					}else{
						try{
							Thread.sleep(getRandom());
						}catch(InterruptedException ie){
							logger.warn(ie);
						}
					}
				
				}catch (Exception e){
					log(e);
				}finally{
				}
				
				
				try{
					logger.debug(">>>>:::Mandatory throttling: sleeping "+mandatory_throttle+"ms ");
					Thread.sleep(mandatory_throttle);
				}catch(InterruptedException e){
					logger.error(e.getMessage(),e);
					setRun(false);
					setFinished(true);
					setBusy(false);
				}catch(Exception e){
					logger.error(e.getMessage(),e);
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
		    
		    finalizeMe();
		} 
		
	}
	
	public void finalizeMe() {
		genericHttpClient.finalizeMe();
	}

	private Billable createBillableFromSubscription(Subscription sub) {
		
		Billable billable = null;
	
		logger.info(" sub "+sub);
		Long sms_service_id = sub.getSms_service_id_fk();
		
		SMSService service = sms_serviceCache.get(sms_service_id);
		
		if (service == null) {
			try {
				service = cmp_ejb.find(SMSService.class, sms_service_id);
			} catch (Exception e) {
				logger.warn("Couldn't find service with id "
						+ sms_service_id);
			}
		}

		logger.info(">>service :: "+service);
		if (service != null) {
			MOProcessorE processor = mo_processorCache.get(service.getMo_processorFK());
			if (processor == null) {
				try {
					processor = cmp_ejb.find(MOProcessorE.class,
							service.getMo_processorFK());
				} catch (Exception exp) {
					logger.warn("Could not find the processor with id : "
							+service.getMo_processorFK());
				}
			}

			//logger.info("\t\t\n\n\n:::::::TXID::::::transaction_id:"+transaction_id+"\n\n\n");

			billable = new Billable();
			billable.setCp_id("CONTENT360_KE");
			billable.setCp_tx_id(BigInteger.valueOf(SubscriptionRenewal.generateNextId()));
			billable.setDiscount_applied("0");
			billable.setKeyword(service.getCmd());
			billable.setService_id(service.getId().toString());
			billable.setMaxRetriesAllowed(0L);
			billable.setMsisdn(sub.getMsisdn());
			billable.setOperation(BigDecimal.valueOf(service.getPrice())
					.compareTo(BigDecimal.ZERO) > 0 ? Operation.debit
					.toString() : Operation.credit.toString());
			billable.setPrice(BigDecimal.valueOf(service.getPrice()));
			billable.setPriority(0l);
			billable.setProcessed(1L);
			billable.setRetry_count(0L);
			billable.setShortcode(processor.getShortcode());
			billable.setEvent_type((EventType.get(service.getEvent_type()) != null ? EventType
					.get(service.getEvent_type())
					: EventType.SUBSCRIPTION_PURCHASE));
			billable.setPricePointKeyword(service.getPrice_point_keyword());
			billable.setSuccess(Boolean.FALSE);
			logger.debug(" before queue transaction_id" + billable.getCp_tx_id());

			
			logger.info("putting in queue....");
		}
		
		
		return billable;
	}

	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}
		
	private String getTransactionId(String resp) {
		int start = resp.indexOf("<transactionId>")+"<transactionId>".length();
		int end  = resp.indexOf("</transactionId>");
		return resp.substring(start, end);
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
