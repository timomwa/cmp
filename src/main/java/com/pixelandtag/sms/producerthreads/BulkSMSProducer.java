package com.pixelandtag.sms.producerthreads;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.pixelandtag.api.MessageStatus;
import com.pixelandtag.bulksms.BulkSMSPlan;
import com.pixelandtag.bulksms.BulkSMSQueue;
import com.pixelandtag.bulksms.BulkSMSText;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.ejb.bulksms.BulkSmsMTI;
import com.pixelandtag.cmp.ejb.sequences.SequenceGenI;
import com.pixelandtag.cmp.entities.MOProcessor;
import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.entities.URLParams;
import com.pixelandtag.util.FileUtils;

/**
 * 
 * @author Timothy Mwangi Gikonyo
 * Date created: Sunday 3rd May 2015.
 * This will retrieve messages from the db and fairly distribute to 
 * http senders to send to the CMP/SMSC
 *
 */


public class BulkSMSProducer extends Thread {
	
	private static final boolean FAIR = true;
	private boolean run = true;
	public static volatile BulkSMSProducer instance;
	private static Logger logger = Logger.getLogger(BulkSMSProducer.class);
	
	private String fr_tz;
	private String to_tz;
	public static final String DEFLT = "DEFAULT_DEFAULT";
	private static int sentMT = 0;
	
	private CMPResourceBeanRemote cmpbean;
	private SequenceGenI sequenceGen;
	private QueueProcessorEJBI queueprocessor;
	private BulkSmsMTI bulksmsBean;
	private OpcoSenderProfileEJBI opcosenderProfileEJB;
	private OpcoSMSServiceEJBI opcoSMSServiceEJB;
	private  Context context = null;
	private Properties mtsenderprop;
	private Properties log4jProps;
	private int bulk_sms_poll_wait_time = 1000;//1 sec in miliseconds by default
	private Map<String,OpcoSenderReceiverProfile> profileSenderCache = new HashMap<String,OpcoSenderReceiverProfile>();

	public void initEJB() throws NamingException{
		
			mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
	    	String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
			 Properties props = new Properties();
			 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
			 props.put(Context.PROVIDER_URL, "remote://"+mtsenderprop.getProperty("ejbhost")+":"+mtsenderprop.getProperty("ejbhostport"));
			 props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
			 props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
			 props.put("jboss.naming.client.ejb.context", true);
			 context = new InitialContext(props);
			 opcoSMSServiceEJB = (OpcoSMSServiceEJBI) 
			       		context.lookup("cmp/OpcoSMSServiceEJBImpl!com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI");
			 opcosenderProfileEJB = (OpcoSenderProfileEJBI) 
			       		context.lookup("cmp/OpcoSenderProfileEJBImpl!com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI");
			 sequenceGen  = (SequenceGenI) 
			       		context.lookup("cmp/SequenceGenEJB!com.pixelandtag.cmp.ejb.sequences.SequenceGenI");
			 queueprocessor =  (QueueProcessorEJBI) 
			       		context.lookup("cmp/QueueProcessorEJBImpl!com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI");
			 cmpbean =  (CMPResourceBeanRemote) 
	       		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
			 bulksmsBean = (BulkSmsMTI) 
			       		context.lookup("cmp/BulkSmsMTEJB!com.pixelandtag.cmp.ejb.bulksms.BulkSmsMTI");
			 
			 logger.info(getClass().getSimpleName()+": Successfully initialized EJB BulkSmsMTI !!");
			 
			 try{
				 bulk_sms_poll_wait_time = Integer.valueOf(mtsenderprop.getProperty("bulk_sms_poll_wait_time"));
			 }catch(Exception e){}
	 }
	
	
	
	
	public String getFr_tz() {
		return fr_tz;
	}




	public String getTo_tz() {
		return to_tz;
	}




	public void setFr_tz(String fr_tz) {
		this.fr_tz = fr_tz;
	}




	public void setTo_tz(String to_tz) {
		this.to_tz = to_tz;
	}


	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

	public int getSentMT() {
		return sentMT;
	}
	
	public static void resetSentMT() {
		sentMT = 0;
	}

	public static void increaseMT() {
		sentMT++;
	}

	
	
	public void myfinalize(){
		
		try {
			if(context!=null)
				context.close();
		} catch (Exception e) {
		}
	}
	
	
	public static void stopApp(){
		
		System.out.println("Shutting down...");
		
		try{
			if(instance!=null){
				System.out.println("Shutting down...");
				instance.myfinalize();
				
			}else{
				System.out.println("App not yet initialized or started.");
			}
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}catch(Error e){
			logger.error(e.getMessage(),e);
		}
		
	}
	
	
	
	public BulkSMSProducer() throws Exception{
		
		initEJB();
		
		instance = this;
	}
	
	
	public void run() {
		try{
			while(run){
				
				populateQueue();
				
				try {
					Thread.sleep(bulk_sms_poll_wait_time);
				} catch (InterruptedException e) {
					log(e);
				}
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}finally{
		}
	}


	

	/**
	 * Populates the queue
	 * @throws InterruptedException 
	 */
	private void populateQueue()  {
		
		try {
			
			List<BulkSMSQueue> queue = bulksmsBean.getUnprocessed(1000L); 
			
			logger.debug(">>> BULK_SMS #%#%#%#%#%#%#%#%#%#% queue.size():: "+queue.size()+"\n");
			for(BulkSMSQueue bulktext : queue){
				
				try{
					 bulktext.setBulktxId( UUID.randomUUID().toString()  );//We link the cmp tx id to the bulk text, so that later we can update the status as sent or something like that
					 bulktext.setStatus(MessageStatus.IN_QUEUE);
					 bulktext.setRetrycount( (bulktext.getRetrycount().intValue() + 1) );
					 bulktext =  bulksmsBean.saveOrUpdate(bulktext);//Save state
					 
					 String telcoid =  bulktext.getText().getPlan().getTelcoid();//could be the ISO opco code.
					 OpcoSenderReceiverProfile opcosenderprofile = profileSenderCache.get(telcoid);
							 
					 
					 
					 if(opcosenderprofile==null){
						 opcosenderprofile = opcosenderProfileEJB.getActiveProfileForOpco(telcoid);
						 
						 if(opcosenderprofile==null)
							 try{
								 opcosenderprofile =  opcosenderProfileEJB.getActiveProfileForOpco( Long.valueOf(telcoid)  );
							 }catch(NumberFormatException nfe){
								 logger.warn(nfe.getMessage()+" "+telcoid+" isn't a digit");
							 }
						 
						 profileSenderCache.put(telcoid, opcosenderprofile);
					 }
					 
					 BulkSMSText text = bulktext.getText();
					 
					// MOProcessor moproc = opcosenderProfileEJB.getMOProcessorByTelcoShortcodeAndKeyword("DEFAULT", text.getSenderid(), opcosenderprofile.getOpco());
					 OpcoSMSService opcosmsservice = opcoSMSServiceEJB.getOpcoSMSService("DEFAULT", text.getSenderid(), opcosenderprofile.getOpco());
					 MOProcessor moproc = opcosmsservice.getMoprocessor();
					 OutgoingSMS outgoingsms = bulktext.convertToOutGoingSMS();
					 outgoingsms.setMoprocessor(moproc);
					 outgoingsms.setOpcosenderprofile(opcosenderprofile);
					 outgoingsms.setParlayx_serviceid(  opcosmsservice.getServiceid()  );
					 outgoingsms.setPrice(opcosmsservice.getPrice());
					
					 BulkSMSPlan plan =  text.getPlan();
					
					 logger.info(">>::protocol:"+plan.getProtocol());
					 logger.info(">>::processorId:"+plan.getProcessor_id());
					 logger.info(">>::sms ::: "+text.getContent());
					 logger.info(">>::msisdn ::: "+bulktext.getMsisdn());
					 logger.info(">>::opcosmsservice.getServiceid()-> "+opcosmsservice.getServiceid());
					
					 outgoingsms = queueprocessor.saveOrUpdate(outgoingsms);
								 
				}catch(Exception exp){
					
					log(exp);
					 
					MessageStatus status = (bulktext.getRetrycount().compareTo(bulktext.getMax_retries())<0)
							 ? MessageStatus.FAILED_TEMPORARILY : MessageStatus.FAILED_PERMANENTLY;
					bulktext.setStatus(status);
					
				}finally{
					
					try{
						bulktext =  bulksmsBean.saveOrUpdate(bulktext);
					}catch(Exception exp){
						log(exp);
					}
					
				}
				 
			}
			
			
		
		} catch (Exception e) {
			
			log(e);
		
		}finally{
			
						
		}
		
		
	}

	
	


	/**
	 * Wait
	 */
	public synchronized void pauze() {
		
		try {
			
			wait();
		
		} catch (InterruptedException e) {
			
			logger.error(e.getMessage(),e);
		}
	
	}
	
	
	
	/**
	 * notify
	 */
	public synchronized void rezume() {
		
		notify();
	
	}



	/**
	 * Logs the exception
	 * @param e - java.lang.Exception
	 */
	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}
	
	
	
	
	
	public static String getMemoryUsage() {
		long mem = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
		if ( mem>=(1024*1024*1024) )
			return ((int)mem/1024/1024/1024)+" GB";
		if ( mem>=(1024*1024) )
			return ((int)mem/1024/1024)+" MB";
		if ( mem>=(1024) )
			return ((int)mem/1024)+" KB";
		return mem+"B";
			
	}
	
	
	
	
	public static String hexToString(String txtInHex)
    {
        byte [] txtInByte = new byte [txtInHex.length() / 2];
        int j = 0;
        for (int i = 0; i < txtInHex.length(); i += 2)
        {
                txtInByte[j++] = Byte.parseByte(txtInHex.substring(i, i + 2), 16);
        }
        return new String(txtInByte);
    }
	
	
	public static void main(String[] args) {
		System.out.println("["+hexToString("004f006e00200031".replaceAll("00",""))+"]");
	}
	

	private static Properties createProps() {
		Properties props = new Properties();
		props.put("log4j.rootCategory", "INFO, dailyApp");
		props.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
		props.put("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
		props.put("log4j.appender.A1.layout.ConversionPattern", "%d %-4r [%t] %-5p %c{2} %x - %m%n");
		props.put("log4j.appender.dailyApp", "org.apache.log4j.ConsoleAppender");
		props.put("log4j.appender.dailyApp.layout", "org.apache.log4j.PatternLayout");
		props.put("log4j.appender.dailyApp.layout.ConversionPattern", "%d %-4r [%t] %-5p %c{2} %x - %m%n");
		return props;
	}

}
