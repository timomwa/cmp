package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.staticcontent.ContentRetriever;
import com.pixelandtag.subscription.SubscriptionOld;
import com.pixelandtag.subscription.dto.SubscriptionDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.web.beans.RequestObject;

public class DynamicContentProcessor extends GenericServiceProcessor{

	private final Logger dynamic_content_processorLogger = Logger.getLogger(DynamicContentProcessor.class);
	private DBPoolDataSource ds;
	private SubscriptionOld subscription;
	
	private ContentRetriever cr = null;
	private String SPACE = " ";
    
    public void initEJB() throws NamingException{
    	cr = new ContentRetriever(baseEntityEJB);
		logger.info("Successfully initialized EJB CMPResourceBeanRemote !!");
    }
    
	public DynamicContentProcessor() throws Exception{
		init_datasource();
		initEJB();
		subscription = new SubscriptionOld();
	}
	
	private void init_datasource(){
		
	}
	

	@Override
	public OutgoingSMS process(IncomingSMS incomingsms){
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		Connection conn = null;
		
		try {
			
			conn = null;//getCon();
			
			final RequestObject req = new RequestObject(incomingsms);
			final String KEYWORD = req.getKeyword().trim();
			final String MSISDN = req.getMsisdn();
			final Long serviceid = incomingsms.getServiceid();
			final Map<String,String> additionalInfo = baseEntityEJB.getAdditionalServiceInfo(serviceid.intValue());
			final int content_id = Integer.valueOf(UtilCelcom.getServiceMetaData(conn,serviceid.intValue(),"dynamic_contentid"));
			
			dynamic_content_processorLogger.info("KEYWORD ::::::::::::::::::::::::: ["+KEYWORD+"]");
			dynamic_content_processorLogger.info("SERVICEID ::::::::::::::::::::::::: ["+serviceid+"]");
			
			String tailMsg = "";
			
			if(!incomingsms.getIsSubscription()){//If this is a subscription push, then don't check if sub is subscribed.
				
				SubscriptionDTO sub = baseEntityEJB.getSubscriptionDTO(MSISDN, serviceid.intValue());
				
				tailMsg = (sub==null ? additionalInfo.get("tailText_notsubscribed") : (SubscriptionStatus.confirmed==SubscriptionStatus.get(sub.getSubscription_status()) ? additionalInfo.get("tailText_subscribed") : additionalInfo.get("tailText_notsubscribed")));
						 
				if(tailMsg==null || tailMsg.equals(additionalInfo.get("tailText_notsubscribed"))){
					SMSService smsService = baseEntityEJB.find(SMSService.class, new Long(serviceid));
					baseEntityEJB.subscribe(MSISDN, smsService, -1,AlterationMethod.self_via_sms);
				}
				
			}else{
				
				tailMsg = additionalInfo.get("tailText_subscribed");
			
			}
			
			final String content = cr.getDynamicContent(content_id, conn);
					
			if(content!=null)
				outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+content+SPACE+tailMsg);
			else
				outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+"Dummy subscription content for "+additionalInfo.get("service_name")+SPACE+".");//No content! Send blank msg.
					
			dynamic_content_processorLogger.info("CONTENT FOR MSISDN["+MSISDN+"] ::::::::::::::::::::::::: ["+incomingsms.toString()+"]");
			
		}catch(Exception e){
			
			dynamic_content_processorLogger.error(e.getMessage(),e);
		
		}finally{
			
			try{
				if(conn!=null)
					conn.close();
			}catch(Exception e){}
		
		}
		
		return outgoingsms;
	}

	@Override
	public void finalizeMe() {
		try{
			if(context!=null)
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

	

}
