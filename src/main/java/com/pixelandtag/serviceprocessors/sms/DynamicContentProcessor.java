package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.staticcontent.ContentRetriever;
import com.pixelandtag.subscription.SubscriptionOld;
import com.pixelandtag.subscription.dto.SubscriptionDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.web.beans.RequestObject;
import com.pixelandtag.web.triviaI.MechanicsI;

public class DynamicContentProcessor extends GenericServiceProcessor{

	private final Logger dynamic_content_processorLogger = Logger.getLogger(DynamicContentProcessor.class);
	private DBPoolDataSource ds;
	private SubscriptionOld subscription;
	
	private ContentRetriever cr = new ContentRetriever();
	private String SPACE = " ";
	private InitialContext context;
	private CMPResourceBeanRemote cmpbean;
    
    public void initEJB() throws NamingException{
    	String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		 Properties props = new Properties();
		 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		 props.put(Context.PROVIDER_URL, "remote://localhost:4447");
		 props.put(Context.SECURITY_PRINCIPAL, "testuser");
		 props.put(Context.SECURITY_CREDENTIALS, "testpassword123!");
		 props.put("jboss.naming.client.ejb.context", true);
		 context = new InitialContext(props);
		 cmpbean =  (CMPResourceBeanRemote) 
       		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		 
		 System.out.println("Successfully initialized EJB CMPResourceBeanRemote !!");
    }
	public DynamicContentProcessor(){
		init_datasource();
		subscription = new SubscriptionOld();
	}
	
	private void init_datasource(){
		
		int vendor = DriverUtilities.MYSQL;
		String driver = DriverUtilities.getDriver(vendor);
		String host = "db";
		String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
		String url = DriverUtilities.makeURL(host, dbName, vendor);
		
		ds = new DBPoolDataSource();
	    ds.setName("DYNAMICCONTENT_PROCESSOR_DS");
	    ds.setDescription("DYNAMIC Content thread datasource: "+ds.getName());
	    ds.setDriverClassName(driver);
	    ds.setUrl(url);
	    //ds.setUser("root");
	   // ds.setPassword("");
	    ds.setMinPool(1);
	    ds.setMaxPool(2);
	    ds.setMaxSize(3);
	    ds.setIdleTimeout(3600);  // Specified in seconds.
	    ds.setValidationQuery("SELECT 'Test'");
		
	}

	@Override
	public MOSms process(MOSms mo){
		
		Connection conn = null;
		
		try {
			
			conn = getCon();
			
			final RequestObject req = new RequestObject(mo);
			final String KEYWORD = req.getKeyword().trim();
			final String MSISDN = req.getMsisdn();
			final int serviceid = mo.getServiceid();
			final Map<String,String> additionalInfo = cmpbean.getAdditionalServiceInfo(serviceid);
			final int content_id = Integer.valueOf(UtilCelcom.getServiceMetaData(conn,serviceid,"dynamic_contentid"));
			
			dynamic_content_processorLogger.info("KEYWORD ::::::::::::::::::::::::: ["+KEYWORD+"]");
			dynamic_content_processorLogger.info("SERVICEID ::::::::::::::::::::::::: ["+serviceid+"]");
			
			String tailMsg = "";
			
			if(!mo.isSubscriptionPush()){//If this is a subscription push, then don't check if sub is subscribed.
				
				SubscriptionDTO sub = cmpbean.getSubscriptionDTO(MSISDN, serviceid);
				
				tailMsg = (sub==null ? additionalInfo.get("tailText_notsubscribed") : (SubscriptionStatus.confirmed==SubscriptionStatus.get(sub.getSubscription_status()) ? additionalInfo.get("tailText_subscribed") : additionalInfo.get("tailText_notsubscribed")));
						 
				if(tailMsg==null || tailMsg.equals(additionalInfo.get("tailText_notsubscribed"))){
					SMSService smsService = cmpbean.find(SMSService.class, new Long(serviceid));
					cmpbean.subscribe(MSISDN, smsService, -1);
				}
				
			}else{
				
				tailMsg = additionalInfo.get("tailText_subscribed");
			
			}
			
			final String content = cr.getDynamicContent(content_id, conn);
					
			if(content!=null)
				mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+content+SPACE+tailMsg);
			else
				mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+"Dummy subscription content for "+additionalInfo.get("service_name")+SPACE+".");//No content! Send blank msg.
					
			dynamic_content_processorLogger.info("CONTENT FOR MSISDN["+MSISDN+"] ::::::::::::::::::::::::: ["+mo.toString()+"]");
			
		}catch(Exception e){
			
			dynamic_content_processorLogger.error(e.getMessage(),e);
		
		}finally{
			
			try{
				conn.close();
			}catch(Exception e){}
		
		}
		
		return mo;
	}

	@Override
	public void finalizeMe() {
		

		try{
			
			context.close();
		
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}

		try {
			
			ds.releaseConnectionPool();
			
		
		} catch (Exception e) {
			
			dynamic_content_processorLogger.error(e.getMessage(),e);
			
		}
		
	}

	@Override
	public Connection getCon() {
		
		try {
			
			return ds.getConnection();
		
		} catch (Exception e) {
			
			dynamic_content_processorLogger.error(e.getMessage(),e);
			
			return null;
		
		}finally{
		
		}
	}
	
	

	
	@Override
	public CMPResourceBeanRemote getEJB() {
		return this.cmpbean;
	}

}
