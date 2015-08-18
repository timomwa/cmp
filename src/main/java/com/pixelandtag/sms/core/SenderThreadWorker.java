package com.pixelandtag.sms.core;

import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pixelandtag.api.MTStatus;
import com.pixelandtag.cmp.ejb.api.sms.ConfigsEJBI;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.cmp.entities.customer.configs.ProfileTemplate;
import com.pixelandtag.cmp.entities.customer.configs.SenderProfile;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;
import com.pixelandtag.smssenders.SMSSenderFactory;
import com.pixelandtag.smssenders.Sender;
import com.pixelandtag.smssenders.SenderResp;
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
	private OpcoSenderProfile opcosenderprofile;
	private ConfigsEJBI configsEJB;
	private QueueProcessorEJBI queueprocbean;
	private Context context;
	private boolean run = true;
	private boolean stopped  = false;
	
	public SenderThreadWorker(Queue<OutgoingSMS> outqueue_, OpcoSenderProfile opcosenderprofile_) throws Exception{
		this.outqueue = outqueue_;
		this.opcosenderprofile = opcosenderprofile_;
		initEJBs();
		initsender();
	}
	
	private void initEJBs() throws NamingException {
		String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
	 	Properties props = new Properties();
	 	props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
	 	props.put(Context.PROVIDER_URL, "remote://localhost:4447");
	 	props.put(Context.SECURITY_PRINCIPAL, "testuser");
	 	props.put(Context.SECURITY_CREDENTIALS, "testpassword123!");
	 	props.put("jboss.naming.client.ejb.context", true);
	 	context = new InitialContext(props);
	 	configsEJB =  (ConfigsEJBI) context.lookup("cmp/ConfigsEJBImpl!com.pixelandtag.cmp.ejb.api.sms.ConfigsEJBI");
	 	queueprocbean =  (QueueProcessorEJBI) context.lookup("cmp/QueueProcessorEJBImpl!com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI");
	 	logger.info(getClass().getSimpleName()+": Successfully initialized EJB QueueProcessorEJBImpl !!");
	}

	private void initsender() throws Exception{
		SenderProfile profile = opcosenderprofile.getProfile();
		Map<String,ProfileConfigs> opcoconfigs = configsEJB.getAllConfigs(profile);
		Map<String,ProfileTemplate> opcotemplates = configsEJB.getAllTemplates(profile,TemplateType.PAYLOAD);
		SenderConfiguration senderconfigs = new SenderConfiguration();
		senderconfigs.setOpcoconfigs(opcoconfigs);
		senderconfigs.setOpcotemplates(opcotemplates);
		sender = SMSSenderFactory.getSenderInstance(senderconfigs);
		sender.validateMandatory();//Validates mandatory configs.
	}

	@Override
	public void run() {
		
		while(run){
			
			try{
			
				OutgoingSMS sms = outqueue!=null ? (outqueue.size()>1000 ? outqueue.poll() : OutgoingQueueRouter.poll() ) : null;
				
				if(sms!=null && sms.getId().compareTo(-1L)>0){
					
					try{
					
						sms.setIn_outgoing_queue(Boolean.TRUE);
						sms = queueprocbean.saveOrUpdate(sms);//Lock out anyone.
						
						SenderResp response = sender.sendSMS(sms);
						
						MTStatus mtstatus;
						
						if(response.getSuccess()==Boolean.TRUE){
							
							mtstatus = MTStatus.SENT_SUCCESSFULLY;
							queueprocbean.deleteFromQueue(sms);
						
						}else{
							
							sms.setSent(Boolean.FALSE);
							sms.setIn_outgoing_queue(Boolean.FALSE);
							sms.setRe_tries(sms.getRe_tries().longValue()+1L);
							sms.setPriority(sms.getPriority()+1);
							sms = queueprocbean.saveOrUpdate(sms);
							
							if(sms.getRe_tries().intValue()<=sms.getTtl())
								mtstatus = MTStatus.FAILED_TEMPORARILY;
							else
								mtstatus = MTStatus.FAILED_PERMANENTLY;
						}
						
						queueprocbean.updateMessageLog(sms.getCmp_tx_id(), mtstatus);
						
						
					}catch(Exception exp){
						
						logger.error(exp.getMessage(),exp);
					
					}finally{
						
					}
					
				}else if(sms!=null && sms.getId().compareTo(-1L)==0){//poison pill
					setRun(false);
				}
				
				Thread.sleep(1000);
				
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
