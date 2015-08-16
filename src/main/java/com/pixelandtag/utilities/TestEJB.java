package com.pixelandtag.utilities;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.BasicConfigurator;
import org.jboss.naming.remote.client.InitialContextFactory;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.DatingServiceException;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.api.sms.SMSGatewayI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterEJB;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.TimeUnit;
import com.pixelandtag.dating.entities.Gender;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.SystemMatchLog;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.smsmenu.MenuItem;
import com.pixelandtag.util.FileUtils;

public class TestEJB {
	
	
	private static DatingServiceI datingBean;
	private static InitialContext context;
	private static Properties mtsenderprop;
	private static CMPResourceBeanRemote cmpresourcebean;
	private static SubscriptionBeanI subscriptionBean;
	private static TimezoneConverterI tzconvert;
	private static SMSGatewayI smsgw;
	
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
			 
			 
			 
			 MTsms mtsms = new MTsms();
			 mtsms.setMsisdn("254770178979");
			 mtsms.setSms("Test sms from new platform");
			 mtsms.setShortcode("32329");
			 mtsms.setNewCMP_Txid(String.valueOf(4654534354L));
			 mtsms.setOpcoid(79497164L);
			 
			 
			 smsgw.sendMT(mtsms);
			 
			 Thread.sleep(1000);
	}catch(Exception exp){
			exp.printStackTrace();
		}finally{
		 try{
			 context.close();
		 }catch(Exception exp){
			 
		 }
		}
		 
	}

}
