package com.pixelandtag.sms.core;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.api.MessageStatus;
import com.pixelandtag.cmp.ejb.api.billing.BillingGatewayEJBI;
import com.pixelandtag.cmp.ejb.api.sms.ConfigsEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.cmp.ejb.api.sms.ServiceNotLinkedToOpcoException;
import com.pixelandtag.cmp.ejb.subscription.DNDListEJBI;
import com.pixelandtag.cmp.entities.BillingType;
import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.cmp.entities.customer.configs.ProfileTemplate;
import com.pixelandtag.cmp.entities.customer.configs.SenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;
import com.pixelandtag.smssenders.Sender;
import com.pixelandtag.smssenders.SenderFactory;
import com.pixelandtag.smssenders.SenderResp;
import com.pixelandtag.util.FileUtils;
/**
 * Generic sender.
 * Uses the opco profile to send out messages.
 * @author Timothy Mwangi Gikonyo
 * 18th August 2015
 *
 */
public class SenderThreadWorker implements Runnable{
	
	private Logger logger = Logger.getLogger(getClass());
	
	private Queue<OutgoingSMS> outqueue;
	private Sender sender;
	private OpcoSenderReceiverProfile opcosenderprofile;
	private ConfigsEJBI configsEJB;
	private BillingGatewayEJBI billingGW;
	private DNDListEJBI dndEJB;
	private QueueProcessorEJBI queueprocbean;
	private Context context;
	private boolean run = true;
	private boolean stopped  = false;
	private Properties mtsenderprop;
	public static final Map<String,OpcoSMSService> opco_sms_service_cache = new ConcurrentHashMap<String,OpcoSMSService>();

	private static final String DEFAULT = "DEFAULT";
	private OpcoSMSServiceEJBI opcoSMSServiceEJB;
	
	
	public SenderThreadWorker(Queue<OutgoingSMS> outqueue_, OpcoSenderReceiverProfile opcosenderprofile_) throws Exception{
		this.outqueue = outqueue_;
		this.opcosenderprofile = opcosenderprofile_;
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		initEJBs();
		initsender();
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
	 	configsEJB =  (ConfigsEJBI) context.lookup("cmp/ConfigsEJBImpl!com.pixelandtag.cmp.ejb.api.sms.ConfigsEJBI");
	 	queueprocbean =  (QueueProcessorEJBI) context.lookup("cmp/QueueProcessorEJBImpl!com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI");
	 	dndEJB  =  (DNDListEJBI) context.lookup("cmp/DNDListEJBImpl!com.pixelandtag.cmp.ejb.subscription.DNDListEJBI");
	 	billingGW =  (BillingGatewayEJBI) context.lookup("cmp/BillingGatewayEJBImpl!com.pixelandtag.cmp.ejb.api.billing.BillingGatewayEJBI");
	 	opcoSMSServiceEJB =  (OpcoSMSServiceEJBI) context.lookup("cmp/OpcoSMSServiceEJBImpl!com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI");
	 	logger.info(getClass().getSimpleName()+": Successfully initialized EJB QueueProcessorEJBImpl !!");
	}

	private void initsender() throws Exception{
		SenderReceiverProfile profile = opcosenderprofile.getProfile();
		Map<String,ProfileConfigs> opcoconfigs = configsEJB.getAllConfigs(profile);
		Map<String,ProfileTemplate> opcotemplates = configsEJB.getAllTemplates(profile,TemplateType.PAYLOAD);
		SenderConfiguration senderconfigs = new SenderConfiguration();
		senderconfigs.setOpcoconfigs(opcoconfigs);
		senderconfigs.setOpcotemplates(opcotemplates);
		sender = SenderFactory.getSenderInstance(senderconfigs);
		sender.validateMandatory();//Validates mandatory configs.
	}

	@Override
	public void run() {
		
		while(run){
			
			try{
			
				OutgoingSMS sms = outqueue.poll();/*outqueue!=null ? (outqueue.size()>1000 ? outqueue.poll() :*/ /*OutgoingQueueRouter.poll(opcosenderprofile.getId())*/ /*) : null*/; 
				
				if(sms!=null && sms.getId().compareTo(-1L)>0){
					
					try{
					
						sms.setIn_outgoing_queue(Boolean.TRUE);
						sms = queueprocbean.saveOrUpdate(sms);//Lock out anyone.
						
						SenderResp response = null;
						
						if(dndEJB.isinDNDList(sms.getMsisdn())){
							response = new SenderResp();
							response.setSuccess(Boolean.FALSE);
							sms.setTtl(-3L);
							
						}else{
							
							if(sms.getSms()!=null && !sms.getSms().isEmpty()){
								if(sms.getSms().trim().startsWith(GenericServiceProcessor.DND_TG)){
									dndEJB.putInDNDList(sms.getMsisdn());
									logger.info("\n\n\n\n\n\n\t\t\t :::PUTTING "+sms.getMsisdn()+" INTO DND!! \n\n\n\n\n\n\n");
								}
								sms.setSms(sms.getSms().replaceAll(GenericServiceProcessor.DND_TG, ""));
							}
							response = sender.sendSMS(sms);
						}
						
						
						MessageStatus mtstatus;
						
						if(response.getSuccess()==Boolean.TRUE){
							
							mtstatus = MessageStatus.SENT_SUCCESSFULLY;
							queueprocbean.deleteFromQueue(sms);
							queueprocbean.deleteCorrespondingIncomingSMS(sms);
							
							createSuccessBillingRec(sms);
							
						}else{
							
							sms.setSent(Boolean.FALSE);
							sms.setIn_outgoing_queue(Boolean.FALSE);
							sms.setRe_tries(sms.getRe_tries().longValue()+1L);
							sms.setPriority(sms.getPriority()+1);//keep decreasing the priority if we have a problem sending this message.
							
							sms = queueprocbean.saveOrUpdate(sms);
							
							if(sms.getRe_tries().intValue()<=sms.getTtl().intValue() && sms.getTtl().compareTo(0L)>0)
								mtstatus = MessageStatus.FAILED_TEMPORARILY;
							else
								mtstatus = MessageStatus.FAILED_PERMANENTLY;
						}
						
						queueprocbean.updateMessageLog(sms, mtstatus);
						
						
					}catch(Exception exp){
						
						logger.error(exp.getMessage(),exp);
					
					}finally{
						
					}
					
				}else if(sms!=null && sms.getId().compareTo(-1L)==0){//poison pill
					setRun(false);
				}
				
				int sleeptime = outqueue.size()>0 ? 0 : 1000;
				logger.debug("\n\n \t\t>>>>>> sleeptime :: "+sleeptime);
				Thread.sleep(sleeptime);
				
			}catch(InterruptedException ie){
				
				logger.warn("we've ben interrupted!");
				setRun(false);
				
			}catch(Exception exp){
				logger.error(exp.getMessage());
			}
			
		}
		
	}
	

	/**
	 * for MT billing, we make sure if the sms was 
	 * sent successfully, we create a success billing record.
	 * @param sms
	 */
	public void createSuccessBillingRec(OutgoingSMS sms) {
		
		try{
			
			String key_ = DEFAULT+sms.getShortcode()+opcosenderprofile.getOpco().getId();
			OpcoSMSService opcosmsservice = opco_sms_service_cache.get(key_);
			if(opcosmsservice==null){
				opcosmsservice = opcoSMSServiceEJB.getOpcoSMSService(DEFAULT, sms.getShortcode(), opcosenderprofile.getOpco());
				opco_sms_service_cache.put(key_, opcosmsservice);
			}
			sms.setPrice(opcosmsservice.getPrice());
			
			if(opcosmsservice.getBillingType()==BillingType.MT_BILLING)
				billingGW.createSuccessBillingRec(sms, BillingType.MT_BILLING);
			
		}catch(ServiceNotLinkedToOpcoException snle){
			logger.warn(snle.getMessage());
		}catch(Exception exp){
			logger.warn(exp.getMessage(), exp);
		}
		
	}
	
	

	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

	public void stop() {
		try{
			setRun(false);
			Thread.currentThread().interrupt();
			setStopped(true);
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		
	}

	public boolean isStopped() {
		return stopped;
	}

	private void setStopped(boolean stopped) {
		this.stopped = stopped;
	}
	

}
