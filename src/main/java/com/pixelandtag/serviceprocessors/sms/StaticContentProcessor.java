package com.pixelandtag.serviceprocessors.sms;

import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.staticcontent.ContentRetriever;
import com.pixelandtag.subscription.SubscriptionOld;
import com.pixelandtag.subscription.dto.SubscriptionDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.web.beans.RequestObject;

public class StaticContentProcessor extends GenericServiceProcessor{

	private final Logger static_content_processor_logger = Logger.getLogger(StaticContentProcessor.class);
	private SubscriptionOld subscription;
	private ContentRetriever cr = null;
	private String SPACE = " ";
	public void initEJB() throws NamingException{
    	cr = new ContentRetriever(baseEntityEJB);
		logger.info(getClass().getSimpleName()+" : Successfully initialized EJB CMPResourceBeanRemote !!");
	}
	
	public StaticContentProcessor() throws NamingException{
		init_datasource();
		initEJB();
		subscription = new SubscriptionOld();
	}
	
	private void init_datasource(){
	}

	@Override
	public OutgoingSMS process(IncomingSMS incomingsms) {
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		
		try {
			
			final RequestObject req = new RequestObject(incomingsms);
			final String KEYWORD = req.getKeyword().trim();
			final Long serviceid = 	incomingsms.getServiceid();
			final String MSISDN = req.getMsisdn();
			final Map<String,String> additionalInfo = baseEntityEJB.getAdditionalServiceInfo(serviceid.intValue());
			
			
			
			final String static_categoryvalue = baseEntityEJB.getServiceMetaData(serviceid.intValue(),"static_categoryvalue");//UtilCelcom.getServiceMetaData(conn,serviceid,"static_categoryvalue");
			final String table =  baseEntityEJB.getServiceMetaData(serviceid.intValue(),"table");
			
			static_content_processor_logger.info(" KEYWORD ::::::::::::::::::::::::: ["+KEYWORD+"]");
			static_content_processor_logger.info(" SERVICEID ::::::::::::::::::::::::: ["+serviceid+"]");
			static_content_processor_logger.info(" static_categoryvalue ::::::::::::::::::::::::: ["+static_categoryvalue+"]");
			
			
			if(KEYWORD.equalsIgnoreCase("MORE")){
				
				String more = "";
				
				outgoingsms.setSms(more);

			}else if(!static_categoryvalue.equals("-1")){
				
				String tailMsg = "";
				
				if(!incomingsms.getIsSubscription()){
					
					SubscriptionDTO sub = baseEntityEJB.getSubscriptionDTO(MSISDN, serviceid.intValue());
					
					tailMsg = (sub==null ? additionalInfo.get("tailText_notsubscribed") : (SubscriptionStatus.confirmed==SubscriptionStatus.get(sub.getSubscription_status()) ? additionalInfo.get("tailText_subscribed") : additionalInfo.get("tailText_notsubscribed")));
							 
					if(tailMsg==null || tailMsg.equals(additionalInfo.get("tailText_notsubscribed"))){
						SMSService smsService = baseEntityEJB.find(SMSService.class, new Long(serviceid));
						@SuppressWarnings("unused")
						boolean success = baseEntityEJB.subscribe(MSISDN, smsService, -1,AlterationMethod.self_via_sms);
					}
					
				}else{
					tailMsg = additionalInfo.get("tailText_subscribed");
				}
				final String content = baseEntityEJB.getUniqueFromCategory("pixeland_content360", table, "Text", "id", "Category", static_categoryvalue, MSISDN, serviceid.intValue(), 1, incomingsms.getMoprocessor().getId());
				
				if(content!=null)
					outgoingsms.setSms(content+SPACE+tailMsg);
				else
					outgoingsms.setSms(SPACE);//No content! Send blank msg.
						
				
				toStatsLog(incomingsms, null);
				static_content_processor_logger.debug("CONTENT FOR MSISDN["+MSISDN+"] ::::::::::::::::::::::::: ["+incomingsms.toString()+"]");
				
			}else{
				String unknown_keyword = baseEntityEJB.getServiceMetaData(-1,"unknown_keyword");
				
				if(unknown_keyword==null)
					unknown_keyword = "Unknown Keyword.";
					outgoingsms.setSms(unknown_keyword);
			
			}
			
			static_content_processor_logger.debug(incomingsms.toString());
			
		}catch(Exception e){
			
			static_content_processor_logger.error(e.getMessage(),e);
		
		}finally{
			
			try{
				//conn.close();
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
			static_content_processor_logger.error(e.getMessage(),e);
		}
		
	}

	

}
