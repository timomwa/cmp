package com.pixelandtag.serviceprocessors.sms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.LocationBeanI;
import com.pixelandtag.cmp.ejb.MessageEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI;
import com.pixelandtag.cmp.ejb.subscription.FreeLoaderEJBI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.Message;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.web.beans.RequestObject;

public class ReEntryProcessor extends GenericServiceProcessor {
	
	private Logger logger = Logger.getLogger(getClass());
	private SubscriptionBeanI subscriptionBean;
	private InitialContext context;
	private DatingServiceI datingBean;
	private FreeLoaderEJBI freeloaderEJB;
	private MessageEJBI messageEJB;
	
	
	public ReEntryProcessor() throws Exception{
		initEJB();
	}
	
	public void initEJB() throws NamingException{
    	String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		props.put(Context.PROVIDER_URL, "remote://localhost:4447");
		props.put(Context.SECURITY_PRINCIPAL, "testuser");
		props.put(Context.SECURITY_CREDENTIALS, "testpassword123!");
		props.put("jboss.naming.client.ejb.context", true);
		context = new InitialContext(props);
		subscriptionBean = (SubscriptionBeanI) context.lookup("cmp/SubscriptionEJB!com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI");
		datingBean =  (DatingServiceI) context.lookup("cmp/DatingServiceBean!com.pixelandtag.cmp.ejb.DatingServiceI");
		freeloaderEJB =  (FreeLoaderEJBI) context.lookup("cmp/FreeLoaderEJBImpl!com.pixelandtag.cmp.ejb.subscription.FreeLoaderEJBI");
		messageEJB =  (MessageEJBI) context.lookup("cmp/MessageEJBImpl!com.pixelandtag.cmp.ejb.MessageEJBI");
		logger.debug("Successfully initialized EJB CMPResourceBeanRemote !!");
    }

	@Override
	public OutgoingSMS process(IncomingSMS incomingsms) {
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		logger.info("\n\n\n\tIS SUBSCRIPTION?? "+incomingsms.getIsSubscription());
		
		try {
			
			final RequestObject req = new RequestObject(incomingsms);
			final String KEYWORD = req.getKeyword().trim();
			final String MESSAGE = req.getMsg().trim();
			final Long serviceid = 	incomingsms.getServiceid();
			final String MSISDN = req.getMsisdn();
			
			Long language_id = 1L;//We assume English
			
			if(KEYWORD.equalsIgnoreCase("YES") 
					|| KEYWORD.equalsIgnoreCase("OK")
					|| KEYWORD.equalsIgnoreCase("1") 
					|| KEYWORD.equalsIgnoreCase("ACCEPT") 
					|| KEYWORD.equalsIgnoreCase("SAWA") ){
				
				if(freeloaderEJB.isInFreeloaderList(MSISDN))
					freeloaderEJB.removeFromFreeloaderList(MSISDN);
				
				Message message = messageEJB.getMessage(GenericServiceProcessor.ACCEPTED_STANDARD_CHAT_CHARGES, language_id, incomingsms.getOpco().getId());
				
				if(message!=null){
					outgoingsms.setSms(message.getMessage());
					outgoingsms.setBilling_status(BillingStatus.NO_BILLING_REQUIRED);
					outgoingsms.setPrice(BigDecimal.ZERO);
				}else{
					throw new Exception("Could not get the message!");
				}
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		
		return outgoingsms;
	}

	private void isInFreeloaderList(String mSISDN) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finalizeMe() {
		try {
			context.close();
		} catch (NamingException e) {
			logger.error(e.getMessage(),e);
		}
	}

	@Override
	public Connection getCon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseEntityI getEJB() {
		return datingBean;
	}

}
