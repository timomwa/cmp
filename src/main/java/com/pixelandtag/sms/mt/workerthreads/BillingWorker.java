package com.pixelandtag.sms.mt.workerthreads;

import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pixelandtag.billing.Biller;
import com.pixelandtag.billing.BillerFactory;
import com.pixelandtag.billing.BillerProfile;
import com.pixelandtag.billing.BillerProfileConfig;
import com.pixelandtag.billing.BillingConfigSet;
import com.pixelandtag.billing.OpcoBillingProfile;
import com.pixelandtag.billing.entities.BillerProfileTemplate;
import com.pixelandtag.cmp.ejb.api.billing.BillerConfigsI;
import com.pixelandtag.cmp.ejb.api.sms.ConfigsEJBI;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.cmp.ejb.subscription.DNDListEJBI;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.cmp.entities.customer.configs.ProfileTemplate;
import com.pixelandtag.cmp.entities.customer.configs.SenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;
import com.pixelandtag.sms.core.OutgoingQueueRouter;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.smssenders.SenderFactory;
import com.pixelandtag.smssenders.SenderResp;
import com.pixelandtag.util.FileUtils;

public class BillingWorker implements Runnable {
	
	private Logger logger = Logger.getLogger(getClass());
	private Queue<Billable> billableQueue;
	private OpcoBillingProfile opcoBillingProfile;
	private Biller biller;
	private Properties mtsenderprop;
	private Context context;
	private boolean run = true;
	private boolean stopped  = false;
	private BillerConfigsI billerConfigEJB;
	private DNDListEJBI dndEJB;
	
	public BillingWorker(Queue<Billable> billableQueue, OpcoBillingProfile opcoBillingProfile) throws Exception{
		this.billableQueue = billableQueue;
		this.opcoBillingProfile = opcoBillingProfile;
		this.mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		initEJBs();
		initbiller();
	}

	private void initEJBs() throws NamingException {
		String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
	 	Properties props = new Properties();
	 	props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
	 	props.put(Context.PROVIDER_URL, "remote://"+mtsenderprop.getProperty("ejbhost")+":"+mtsenderprop.getProperty("ejbhostport"));
	 	props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
	 	props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
	 	props.put("jboss.naming.client.ejb.context", true);
	 	context = new InitialContext(props);
	 	billerConfigEJB =  (BillerConfigsI) context.lookup("cmp/BillerConfigsImpl!com.pixelandtag.cmp.ejb.api.billing.BillerConfigsI");
	 	//queueprocbean =  (QueueProcessorEJBI) context.lookup("cmp/QueueProcessorEJBImpl!com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI");
	 	dndEJB  =  (DNDListEJBI) context.lookup("cmp/DNDListEJBImpl!com.pixelandtag.cmp.ejb.subscription.DNDListEJBI");
	 	logger.info(getClass().getSimpleName()+": Successfully initialized EJB QueueProcessorEJBImpl !!");
	}
	
	private void initbiller() throws Exception{
		BillerProfile profile = opcoBillingProfile.getProfile();
		Map<String,BillerProfileConfig> opcoconfigs = billerConfigEJB.getAllConfigs(profile);
		Map<String,BillerProfileTemplate> opcotemplates = billerConfigEJB.getAllTemplates(profile,TemplateType.PAYLOAD);
		BillingConfigSet billerconfigs = new BillingConfigSet();
		billerconfigs.setOpcoconfigs(opcoconfigs);
		billerconfigs.setOpcotemplates(opcotemplates);
		biller = BillerFactory.getInstance(billerconfigs);
		biller.validateMandatory();//Validates mandatory configs.
		
	}

	
	
	@Override
	public void run() {
		
		while(run){
			
			int k = 1/0;
			
			try{
			
				Billable billable = billableQueue!=null ? (billableQueue.size()>1000 ? billableQueue.poll() : OutgoingQueueRouter.pollBillable(opcoBillingProfile.getId()) ) : null;
			
				if(billable!=null && billable.getId().compareTo(-1L)>0){
					
					billable.setIn_outgoing_queue(1L);
					//TODO save
					SenderResp response = null;
					
					if(dndEJB.isinDNDList(billable.getMsisdn())){
						response = new SenderResp();
						response.setSuccess(Boolean.FALSE);
					}
					
					
					response = biller.charge(billable);
					billable.setResp_status_code(response.getResponseMsg());
					billable.setProcessed(1L);
					if(response.getSuccess()==Boolean.TRUE){
						
					}else{
						
					}
					
					
				}
			
			}catch(InterruptedException ie){
				
				logger.warn("we've ben interrupted!");
				setRun(false);
				
			}catch(Exception exp){
				logger.error(exp.getMessage());
			}
		}
		
	}

	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}
	
	
	

}
