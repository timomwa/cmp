package com.pixelandtag.sms.mt.workerthreads;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import com.pixelandtag.billing.Biller;
import com.pixelandtag.billing.BillerFactory;
import com.pixelandtag.billing.BillerProfileConfig;
import com.pixelandtag.billing.BillingConfigSet;
import com.pixelandtag.billing.OpcoBillingProfile;
import com.pixelandtag.billing.entities.BillerProfileTemplate;
import com.pixelandtag.cmp.dao.core.SuccessfullyBillingRequestsDAOI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.api.billing.BillerConfigsI;
import com.pixelandtag.cmp.ejb.api.billing.BillingGatewayEJBI;
import com.pixelandtag.cmp.ejb.api.billing.BillingGatewayException;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI;
import com.pixelandtag.cmp.ejb.api.sms.ServiceNotLinkedToOpcoException;
import com.pixelandtag.cmp.ejb.subscription.FreeLoaderEJBI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.entities.MOProcessor;
import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.sms.core.OutgoingQueueRouter;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.sms.producerthreads.Operation;
import com.pixelandtag.sms.producerthreads.SubscriptionRenewal;
import com.pixelandtag.sms.producerthreads.SuccessfullyBillingRequests;
import com.pixelandtag.smssenders.SenderResp;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.util.FileUtils;
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
	private GenericHTTPClient genericHttpClient;
	private SubscriptionBeanI subscriptionejb;
	private BillingGatewayEJBI billinggatewayEJB;
	private BillerConfigsI billerConfigEJB;
	private static Random r = new Random();
	private Map<Long, OpcoSMSService> sms_serviceCache = new HashMap<Long, OpcoSMSService>();
	private Map<Long, MOProcessor> mo_processorCache = new HashMap<Long, MOProcessor>();
	private Map<Long, Biller> biller_cache = new HashMap<Long, Biller>();
	private Properties mtsenderprop;
	
	
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

	private CMPResourceBeanRemote cmp_ejb;
	
	private OpcoSMSServiceEJBI opcosmsserviceEJB;

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
	

	private void init() {
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
	}
	
	private void initEJB() throws Exception{
		String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		 Properties props = new Properties();
		 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		 props.put(Context.PROVIDER_URL, "remote://"+mtsenderprop.getProperty("ejbhost")+":"+mtsenderprop.getProperty("ejbhostport"));
		 props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
		 props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
		 props.put("jboss.naming.client.ejb.context", true);
		 context = new InitialContext(props);
		 cmp_ejb =  (CMPResourceBeanRemote) 
       		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		 subscriptionejb =  (SubscriptionBeanI) 
		       		context.lookup("cmp/SubscriptionEJB!com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI");
		 billerConfigEJB =  (BillerConfigsI) this.context.lookup("cmp/BillerConfigsImpl!com.pixelandtag.cmp.ejb.api.billing.BillerConfigsI");
		 opcosmsserviceEJB = (OpcoSMSServiceEJBI)  this.context.lookup("cmp/OpcoSMSServiceEJBImpl!com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI");
	}

	public SubscriptionBillingWorker(String name_, int mandatory_throttle_) throws Exception{
	
		init();
		
		initEJB();
		 
		this.watch = new StopWatch();
		
		this.name = name_;
		
		this.mandatory_throttle = mandatory_throttle_;
		
		watch.start();
		
		genericHttpClient = new GenericHTTPClient("https");
	}

	public void run() {
		
		try{
			pauze();//wait while producer gets ready
			watch.stop();
			logger.info(getName()+" STARTED AFTER :::::RELEASED_BY_PRODUCER after "+(Double.parseDouble(watch.elapsedTime(TimeUnit.MILLISECONDS)+"")) + " mili-seconds");
			watch.reset();
			
			Long negative_one = new Long(-1);
			
			while(run){
				
				try {
					
					Subscription sub = SubscriptionRenewal.getBillable();
					//Subscription sub = sub_id!=null ? cmp_ejb.find(Subscription.class, sub_id) : null;
					Billable billable = null;
					Long sub_id = sub!=null ? sub.getId() : null;
					
					if(sub_id!=null && sub_id.compareTo(negative_one)==0){//poison pill
						setRun(false);
						setFinished(true);
						setBusy(false);
					}
					
					
					if(sub!=null && sub_id.compareTo(negative_one)>0){
						
						
						if(sub.getSubscription_status()==SubscriptionStatus.confirmed){
							sub.setQueue_status(1L);
							subscriptionejb.updateQueueStatus(1L, sub.getId(), AlterationMethod.backend);
							billable = createBillableFromSubscription(sub);
						}
					}
					
					//if such a billable exists
					if(billable!=null){
						try{
							logger.debug(getName()+":the service id in worker!::::: mtsms.getServiceID():: "+billable.toString());
							
								if(billable.getMsisdn()!=null && !billable.getMsisdn().isEmpty() 
										&& billable.getPrice()!=null && billable.getPrice().compareTo(BigDecimal.ZERO)>0){
									setBusy(true);
									watch.start();
									
									
									Biller biller = biller_cache.get(billable.getOpco().getId());
									
									if(biller==null){
										logger.info("*******We didn't gave in cache, we've put one biller*****");
										biller = getBiller(billable.getOpco());
										biller_cache.put(billable.getOpco().getId(), biller);
									}
									
									SenderResp senderresp = biller.charge(billable);
									
									watch.stop();
									logger.info(getName()+" BILLING TIME   "+(Double.parseDouble(watch.elapsedTime(TimeUnit.MILLISECONDS)+"")) + " mili-seconds");
									watch.reset();
									final String resp = senderresp.getResponseMsg();
									logger.info("\n\t::::::BILLING::::RESP_CODE=["+senderresp.getRespcode()+"]:::: Success flag-> ["+senderresp.getSuccess()+"] :PROXY_RESPONSE: "+resp);
									billable.setResp_status_code( senderresp.getRespcode() );
									billable.setProcessed(1L);
									
									
									
									
									if (senderresp.getSuccess()) {
										boolean capped = resp.toUpperCase().contains("SLAClusterEnforcementMediation".toUpperCase()) ||
												resp.toUpperCase().contains("Service TPS Control".toUpperCase()) ;
										String debug = "capped\t\t ::"+capped;
										debug = debug +"SubscriptionRenewal.isAdaptive_throttling():\t\t "+SubscriptionRenewal.isAdaptive_throttling();
										
										logger.debug("THROTTLING PARAMS :::::: "+debug);
										if(resp!=null)
										if(capped){
											if(SubscriptionRenewal.isAdaptive_throttling()){
												
												//SubscriptionRenewal.putPackToQueue(sub);
												//We've been throttled. Let's slow down a little bit.
												logger.debug("Throttling! We've been capped.");
												//SubscriptionRenewal.setEnable_biller_random_throttling(true);
												//SubscriptionRenewal.setWe_ve_been_capped(true);
												long wait_time = SubscriptionRenewal.getRandomWaitTime();
												logger.info(getName()+" ::: CHILAXING::::::: Trying to chillax for "+wait_time+" milliseconds");
												if(wait_time>-1){
													genericHttpClient.releaseConnection();
													Thread.sleep(wait_time);
													genericHttpClient.initHttpClient();
													Thread.sleep(15000);//wait for client to be initialized
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
										
										if(!senderresp.getSuccess()){
											logger.info("FAILED TO BILL ERROR_MESSAGE="+senderresp.getResponseMsg()+" msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
											//billable.setSuccess(false);
											try{
												billable.setTransactionId(senderresp.getRefvalue());
											}catch(Exception exp){
												logger.warn("No transaction id found");
											}
											
											billable.setResp_status_code(senderresp.getRespcode());
											billable.setSuccess(Boolean.FALSE);
											
											if(resp.toUpperCase().contains("Insufficient".toUpperCase())){
												subscriptionejb.updateCredibilityIndex(billable.getMsisdn(),Long.valueOf(billable.getService_id()),-1, sub.getOpco());
												//we'll try again. 1 means that we let it sit there, but the cron will set it to 2 so that it's picked
												//subscriptionejb.updateQueueStatus(1L, billable.getMsisdn(), Long.valueOf(billable.getService_id()));
											}
											
										}else{
											
											billable.setTransactionId(senderresp.getRefvalue());
											billable.setResp_status_code("Success");
											logger.info("SUCCESS BILLING msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
											billable.setSuccess(Boolean.TRUE);
											sub = subscriptionejb.renewSubscription(billable.getOpco(), billable.getMsisdn(), Long.valueOf(billable.getService_id()), AlterationMethod.system_autorenewal); 
											subscriptionejb.updateCredibilityIndex(billable.getMsisdn(),Long.valueOf(billable.getService_id()),1, sub.getOpco());
											if(SubscriptionRenewal.isAdaptive_throttling()){
												//Resume back to normal. No throttling
												SubscriptionRenewal.setEnable_biller_random_throttling(false);
												SubscriptionRenewal.setWe_ve_been_capped(false);
											}
											subscriptionejb.createSuccesBillRec(billable);
										}
										
									}else if(senderresp.getRespcode()==null || senderresp.getRespcode().equals("0")){
										logger.info(" HTTP FAILED. WE TRY AGAIN LATER");
										//subscriptionejb.updateQueueStatus(2L, billable.getMsisdn(), Long.valueOf(billable.getService_id()));
									
									}else{
										
										boolean capped = resp.toUpperCase().contains("Service TPS Control".toUpperCase()) ;
										if(capped){
											logger.info("ORANGE CAPPING ...... CHILAXING FOR 15 seconds");
											//For Orange. If we're capped, each thread sleeps for 15 seconds.
											//TODO have a biller policy to match SLA TPS.
											try{
												Thread.sleep(15000);
											}catch(InterruptedException ie){
												ie.printStackTrace();
											}
										}
									}
									
									logger.debug(getName()+" ::::::: finished attempt to bill via HTTP");
									
									billable.setProcessed(1L);
									billable.setIn_outgoing_queue(0L);
									
									logger.debug("DONE! ");
									
									billable = cmp_ejb.saveOrUpdate(billable);
									
									setBusy(false);
									
								}else{
									if(billable.getMsisdn()!=null && !billable.getMsisdn().isEmpty() && billable.getPrice().compareTo(BigDecimal.ZERO)<=0){
										sub = subscriptionejb.renewSubscription(billable.getOpco(), billable.getMsisdn(), Long.valueOf(billable.getService_id()), AlterationMethod.system_autorenewal); 
										logger.info("No billing requred :::: SUBSCRIPTION RENEWED: "+sub.toString());
									}else{
										setRun(false);//Poison pill
										setFinished(true);
									}
								}
								
								
								
						}catch(Exception exp){
							
							logger.error(exp.getMessage(),exp);
							logger.info("SUBSCRIPTION_RENEWAL:::::::::SOMETHING WENT WRONG, WE TRY AGAIN ");
							subscriptionejb.updateQueueStatus(0L, billable.getMsisdn(), Long.valueOf(billable.getService_id()),AlterationMethod.system_autorenewal, sub.getOpco());
							
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
			logger.fatal("NEEDS RESTART: MEM_USAGE: "+OutgoingQueueRouter.getMemoryUsage() +" >> "+e.getMessage(),e);
		}finally{
			
		    if(context!=null) 
		    	try { 
		    		context.close(); 
		    	}catch(Exception ex) { ex.printStackTrace(); }
		    
		    finalizeMe();
		} 
		
	}
	
	private Biller getBiller(OperatorCountry opco) throws Exception{
		Biller biller = null; 
		OpcoBillingProfile billerprofile = billerConfigEJB.getActiveBillerProfile(opco);
		if(billerprofile==null)
			throw new BillingGatewayException("No opco billing profile for opco with id "+opco.getId()
					+". Please insert a record in the table opco_biller_profile");
		
		Map<String,BillerProfileConfig> opcoconfigs = billerConfigEJB.getAllConfigs(billerprofile);
		Map<String,BillerProfileTemplate> opcotemplates = billerConfigEJB.getAllTemplates(billerprofile,TemplateType.PAYLOAD);
		
		BillingConfigSet billerconfigs = new BillingConfigSet();
		billerconfigs.setOpcoconfigs(opcoconfigs);
		billerconfigs.setOpcotemplates(opcotemplates);
		try {
			biller = BillerFactory.getInstance(billerconfigs);
			biller.validateMandatory();//Validates mandatory configs.
		}catch (Exception exp) {
			logger.error(exp.getMessage(),exp);
			throw new BillingGatewayException("Problem occurred instantiating sender. Error: "+exp.getMessage());
		}
		return biller;
	}

	public void finalizeMe() {
		genericHttpClient.finalizeMe();
	}

	private Billable createBillableFromSubscription(Subscription sub) {
		
		Billable billable = null;
	
		
		logger.debug(" sub "+sub);
		Long sms_service_id = sub.getSms_service_id_fk();
		
		OpcoSMSService service = sms_serviceCache.get(sms_service_id);
		
		if (service == null) {
			try {
				try{
					service = opcosmsserviceEJB.getOpcoSMSService(sms_service_id, sub.getOpco());
				}catch(ServiceNotLinkedToOpcoException se){
					logger.error(se.getMessage());
				}
				
				if(service!=null){
					if(service.getPrice()!=null && service.getPrice().compareTo(BigDecimal.ZERO)>0){
						sms_serviceCache.put(sms_service_id, service);
					}else{
						service = new OpcoSMSService();
						service.setId(-1L);//Set id to -1 so that it's cached and we don't have to hit db. Also so that it's not picked in the next if block
						sms_serviceCache.put(sms_service_id, service);
					}
				}
			} catch (Exception e) {
				logger.warn("Couldn't find service with id "
						+ sms_service_id);
			}
		}

		//logger.info(">>service :: "+service+"  passes ? "+(service != null && (service.getId().compareTo(-1L)>0)));
		if (service != null && (service.getId().compareTo(-1L)>0)) {
			MOProcessor processor = service.getMoprocessor();
			
			billable = new Billable();
			billable.setCp_id("CONTENT360_KE");
			billable.setCp_tx_id(SubscriptionRenewal.generateNextId());
			billable.setDiscount_applied("0");
			billable.setKeyword(service.getSmsservice().getCmd());
			billable.setService_id(service.getSmsservice().getId().toString());
			billable.setMaxRetriesAllowed(0L);
			billable.setMsisdn(sub.getMsisdn());
			billable.setOperation(service.getPrice()
					.compareTo(BigDecimal.ZERO) > 0 ? Operation.debit
					.toString() : Operation.credit.toString());
			billable.setPrice(service.getPrice());
			billable.setPriority(0l);
			billable.setProcessed(1L);
			billable.setRetry_count(0L);
			billable.setShortcode(processor.getShortcode());
			billable.setEvent_type((EventType.get(service.getSmsservice().getEvent_type()) != null ? EventType
					.get(service.getSmsservice().getEvent_type())
					: EventType.SUBSCRIPTION_PURCHASE));
			billable.setPricePointKeyword(service.getSmsservice().getPrice_point_keyword());
			billable.setSuccess(Boolean.FALSE);
			billable.setOpco(sub.getOpco());
			logger.debug(" before queue transaction_id" + billable.getCp_tx_id());

			
			logger.info("putting in queue....");
		}
		
		
		return billable;
	}

	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}

}
