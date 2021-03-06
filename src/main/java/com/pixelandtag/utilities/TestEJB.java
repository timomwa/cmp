package com.pixelandtag.utilities;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.BasicConfigurator;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.api.sms.ConfigsEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI;
import com.pixelandtag.cmp.ejb.api.sms.SMSGatewayI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.util.FileUtils;

public class TestEJB {
	
	
	private static DatingServiceI datingBean;
	private static InitialContext context;
	private static Properties mtsenderprop;
	private static CMPResourceBeanRemote cmpresourcebean;
	private static SubscriptionBeanI subscriptionBean;
	private static TimezoneConverterI tzconvert;
	private static SMSGatewayI smsgw;
	private static OpcoSenderProfileEJBI opcosenderprofileEJB;
	private static ConfigsEJBI configsEJB;
	
	public static void main(String[] args) throws Exception {
		try{
			
			BasicConfigurator.configure();
			mtsenderprop  = FileUtils.getPropertyFile("mtsender.properties");
			String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
			 Properties props = new Properties();
			 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
			 props.put(Context.PROVIDER_URL, "remote://"+mtsenderprop.getProperty("ejbhost")+":"+mtsenderprop.getProperty("ejbhostport"));
			 props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
			 props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
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
			
			 configsEJB = (ConfigsEJBI) context.lookup("cmp/ConfigsEJBImpl!com.pixelandtag.cmp.ejb.api.sms.ConfigsEJBI");
			 
			 OperatorCountry opco = configsEJB.getOperatorByIpAddress("127.0.0.1");
			 
			 System.out.println("\n"+opco+"\n");
			 
			 OpcoSenderReceiverProfile profile = configsEJB.getActiveOpcoSenderReceiverProfile(opco);
			 
			 System.out.println("\n"+profile.getProfile()+"\n");
			 
			 Map<String, ProfileConfigs> profileconfigs = configsEJB.getAllConfigs(profile.getProfile());
			 
			 
			 for(String ke : profileconfigs.keySet())
				 System.out.println(ke+" : "+profileconfigs.get(ke).getValue());
			 
			 
			 if(true)
				 return;
			 
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
			 
			 
			// context.close();
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
