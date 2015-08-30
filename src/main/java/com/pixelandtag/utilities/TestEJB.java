package com.pixelandtag.utilities;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.BasicConfigurator;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI;
import com.pixelandtag.cmp.ejb.api.sms.SMSGatewayI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.entities.MTsms;

public class TestEJB {
	
	
	private static DatingServiceI datingBean;
	private static InitialContext context;
	private static Properties mtsenderprop;
	private static CMPResourceBeanRemote cmpresourcebean;
	private static SubscriptionBeanI subscriptionBean;
	private static TimezoneConverterI tzconvert;
	private static SMSGatewayI smsgw;
	private static OpcoSenderProfileEJBI opcosenderprofileEJB;
	
	public static void main(String[] args) throws Exception {
		try{
			
			BasicConfigurator.configure();
				
			String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
			 Properties props = new Properties();
			 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
			 props.put(Context.PROVIDER_URL, "remote://localhost:4447");
			 props.put(Context.SECURITY_PRINCIPAL, "testuser");
			 props.put(Context.SECURITY_CREDENTIALS, "testpassword123!");
			 props.put("jboss.naming.client.ejb.context", true);
			 context = new InitialContext(props);
			 datingBean =  (DatingServiceI) 
	      		context.lookup("cmp/DatingServiceBean!com.pixelandtag.cmp.ejb.DatingServiceI");
			 cmpresourcebean =  (CMPResourceBeanRemote) 
			      		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
			 tzconvert =  (TimezoneConverterI) 
			      		context.lookup("cmp/TimezoneConverterEJB!com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI");
	
			 subscriptionBean =  (SubscriptionBeanI) 
			      		context.lookup("cmp/SubscriptionEJB!com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI");
			  
			 smsgw = (SMSGatewayI) context.lookup("cmp/SMSGatewayImpl!com.pixelandtag.cmp.ejb.api.sms.SMSGatewayI");
			 
			 
			 opcosenderprofileEJB = (OpcoSenderProfileEJBI) context.lookup("cmp/OpcoSenderProfileEJBImpl!com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI");
			 
			 
			 
			/* MTsms mtsms = new MTsms();
			 mtsms.setMsisdn("254770178979");//0770178979
			 mtsms.setSms("Test sms from new platform reloaded");
			 mtsms.setShortcode("32329");
			 mtsms.setNewCMP_Txid(String.valueOf(4654534354L));
			 mtsms.setOpcoid(79497164L);
			 */
			 
			 OutgoingSMS mtsms = new OutgoingSMS();
			 
			 mtsms.setSms("Hello Tim, welcome to the pixelAndTag cross-platform SMS Gateway!");
			 mtsms.setShortcode("32329");
			 mtsms.setCmp_tx_id(String.valueOf(4654534354L));//Random val
			 
			 
			 mtsms.setMsisdn("254735594326");//0770178979////254734606096//0772079509
			 OpcoSenderReceiverProfile opcosenderprofile = opcosenderprofileEJB.getActiveProfileForOpco("KEN-639-3");
			 
			 
			 
			 mtsms.setOpcosenderprofile(opcosenderprofile);
			
			 
			 smsgw.sendMT(mtsms);
			 
			 //TODO - Have different configurations - Done!
			 //TODO - Have Default configurations Airtel HTTP, parlayx, oneapi - introduce a profile field or something like that - Done!
			//TODO - Create threads that will proccess these incoming messages - in progress
			 //TODO - Have a way to determine a successful MT (maybe http response code, or parsing response)
			 
			 
			 
	}catch(Exception exp){
			exp.printStackTrace();
		}finally{
		 try{
			 //context.close();
		 }catch(Exception exp){
			 
		 }
		}
		 
	}

}
