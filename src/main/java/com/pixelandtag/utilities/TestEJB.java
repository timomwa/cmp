package com.pixelandtag.utilities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.BasicConfigurator;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.api.billing.BillingGatewayEJBI;
import com.pixelandtag.cmp.ejb.api.sms.ConfigsEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI;
import com.pixelandtag.cmp.ejb.api.sms.SMSGatewayI;
import com.pixelandtag.cmp.ejb.api.ussd.USSDMenuEJBI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.smssenders.SenderResp;
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
	private static USSDMenuEJBI ussdmenuEJB;
	private static BillingGatewayEJBI billinggatewayEJB;
	private static ConfigsEJBI configsEJB;
	private static OpcoEJBI opcoEJB;
	
	
	
	public static String stripStrippables(String originalStr, String strippable_string){
		if((originalStr==null || originalStr.isEmpty()))
			return  originalStr;
		if(strippable_string!=null && strippable_string!=null && !strippable_string.isEmpty()){
			String[] strippables = strippable_string.split(",");
			for(String strippable : strippables){
				originalStr = originalStr.replaceAll( Matcher.quoteReplacement(strippable), Matcher.quoteReplacement("") );   
			}
		}
		return originalStr;
	}
	
	
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
			 
			 ussdmenuEJB = (USSDMenuEJBI) context.lookup("cmp/USSDMenuEJBImpl!com.pixelandtag.cmp.ejb.api.ussd.USSDMenuEJBI");
			 
			 opcoEJB = (OpcoEJBI) context.lookup("cmp/OpcoEJBImpl!com.pixelandtag.cmp.ejb.api.sms.OpcoEJBI");
			 
			 billinggatewayEJB  = (BillingGatewayEJBI) context.lookup("cmp/BillingGatewayEJBImpl!com.pixelandtag.cmp.ejb.api.billing.BillingGatewayEJBI");
			 
			 System.out.println(ussdmenuEJB.getMenu("test","254202407004",1, -1, 1,-1, opcoEJB.findOpcoByCode("KEN-639-7"))); 
			 
			 /*OperatorCountry opco = configsEJB.getOperatorByIpAddress("127.0.0.1");
			 
			 System.out.println("\n"+opco+"\n");
			 
			 OpcoSenderReceiverProfile profile = configsEJB.getActiveOpcoSenderReceiverProfile(opco);
			 
			 System.out.println("\n"+profile.getProfile()+"\n");
			 
			 Map<String, ProfileConfigs> profileconfigs = configsEJB.getAllConfigs(profile.getProfile());
			 
			 
			 for(String ke : profileconfigs.keySet())
				 System.out.println(ke+" : "+profileconfigs.get(ke).getValue());*/
			 
			 
			/* if(true)
				 return;*/
			 
			/* MTsms mtsms = new MTsms();
			 mtsms.setMsisdn("254770178979");//0770178979
			 mtsms.setSms("Test sms from new platform reloaded");
			 mtsms.setShortcode("32329");
			 mtsms.setNewCMP_Txid(String.valueOf(4654534354L));
			 mtsms.setOpcoid(79497164L);
			 */
			 
			 /*OutgoingSMS mtsms = new OutgoingSMS();
			 
			 mtsms.setSms("Sent via Safaricom gateway - parlay x");
			 mtsms.setShortcode("20419");
			 mtsms.setCmp_tx_id(String.valueOf(4654534358L));//Random val
			 
			 
			 mtsms.setMsisdn("254720988636");//0770178979////254734606096//0772079509//254733446767//254202407004
			 OpcoSenderReceiverProfile opcosenderprofile = opcosenderprofileEJB.getActiveProfileForOpco("KEN-639-02");
			 
			 mtsms.setTimestamp(new Date());
			 mtsms.setParlayx_serviceid("6017272000116315");
			 mtsms.setOpcosenderprofile(opcosenderprofile);
			
			 
			 smsgw.sendMT(mtsms);*/
			 
			 Billable billable = new Billable();
			 billable.setOpco(opcoEJB.findOpcoByCode("KEN-639-3"));
			 billable.setMsisdn("254734252504");
			 billable.setCp_id("CONTENT360_KE");
			 billable.setCp_tx_id("082920kddg");
			 billable.setEvent_type(EventType.SUBSCRIPTION_PURCHASE);
			 billable.setIn_outgoing_queue(0L);
			 billable.setKeyword("BILLSERV5");
			 billable.setOperation("debit");
			 billable.setPrice(BigDecimal.valueOf(5));
			 billable.setPricePointKeyword("32329LOVECHAT");
			 billable.setPriority(0L);
			 billable.setProcessed(0L);
			 billable.setRetry_count(0L);
			 billable.setService_id("415");
			 billable.setShortcode("32329");
			 billable.setTimeStamp(new Date());
			 billable.setTransferIn(false);
			 billable.setValid(true);
			 SenderResp success = billinggatewayEJB.bill(billable);  
			 
			 System.out.println("\n\n >>>> "+success+"\n\n");
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
	
	
	public static final String plainchargeXML =  "" +
		      "<soapenv:Envelope\r\n" +
		      "xmlns:soapenv=" + 
		      "\"http://schemas.xmlsoap.org/soap/envelope/\"\r\n" + 
		      "xmlns:char=" + 
		      "\"http://ChargingProcess/com/ibm/sdp/services/charging/abstraction/Charging\">\r\n" +  
		      "<soapenv:Header />\r\n" + 
		      "<soapenv:Body>\r\n" + 
		      "<char:charge>\r\n" + 
		      "<inputMsg>\r\n" + 
		      "<operation>${OPERATION}</operation>\r\n" + 
		      "<userId>${MSISDN}</userId>\r\n" + 
		     // "<contentId>{SHORTCODE}_{KEYWORD}</contentId>\r\n" + 
		      //"<itemName>{SHORTCODE}_{KEYWORD}</itemName>\r\n" +
		      "<contentId>${KEYWORD}</contentId>\r\n" + 
		      "<itemName>${KEYWORD}</itemName>\r\n" + 
		      "<contentDescription>${KEYWORD}</contentDescription>\r\n" +
		     "<circleId></circleId>\r\n" +
		      "<lineOfBusiness></lineOfBusiness>\r\n" + 
		     "<customerSegment></customerSegment>\r\n" +
		      "<contentMediaType>${KEYWORD}</contentMediaType>\r\n" + 
		     "<serviceId>${SERVICE_ID}</serviceId>\r\n" + 
		    "<parentId></parentId>\r\n" +
		      "<actualPrice>${PRICE}</actualPrice>\r\n" + 
		      "<basePrice>${PRICE}</basePrice>\r\n" +
		      "<discountApplied>0</discountApplied>\r\n" +
		     "<paymentMethod></paymentMethod>\r\n" +
		    "<revenuePercent></revenuePercent>\r\n" +
		   "<netShare>0</netShare>\r\n" +
		      "<cpId>${CP_ID}</cpId>\r\n" +
		     "<customerClass></customerClass>\r\n" +
		      "<eventType>${EVENT_TYPE}</eventType>\r\n" +//very important
		     "<localTimeStamp></localTimeStamp>\r\n" +
		    "<transactionId>${TX_ID}</transactionId>\r\n" +
		   "<subscriptionTypeCode>abcd</subscriptionTypeCode>\r\n" +
		  "<subscriptionName>0</subscriptionName>\r\n" +
		 "<parentType></parentType>\r\n" +
		      "<deliveryChannel>SMS</deliveryChannel>\r\n" +
		     "<subscriptionExternalId>0</subscriptionExternalId>\r\n" +
		     "<contentSize></contentSize>\r\n" +
		      "<currency>Kshs</currency>\r\n" + 
		      "<copyrightId>mauj</copyrightId>\r\n" + 
		     "<cpTransactionId>${CP_TX_ID}</cpTransactionId>\r\n" + 
		    "<copyrightDescription>copyright</copyrightDescription>\r\n" + 
		      "<sMSkeyword>${KEYWORD}</sMSkeyword>\r\n" + 
		      "<srcCode>${SHORTCODE}</srcCode>\r\n" + 
		     "<contentUrl>www.content360.co.ke</contentUrl>\r\n" + 
		    "<subscriptiondays>2</subscriptiondays>\r\n" +
		      "</inputMsg>\r\n" + 	      
		      "</char:charge>\r\n" + 		      
		      "</soapenv:Body>\r\n" +  
		      "</soapenv:Envelope>\r\n";

}
