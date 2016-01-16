package com.pixelandtag.subscription;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.MessageEJBI;
import com.pixelandtag.cmp.ejb.subscription.FreeLoaderEJBI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.sms.producerthreads.SubscriptionRenewal;
import com.pixelandtag.util.FileUtils;

public class SubscriptionMain extends Thread{
	
	private Logger logger  = Logger.getLogger(SubscriptionMain.class);
	private static Properties log4Jprops = null;
	private static Properties subscription_props = null;
	private String server_tz;
	private String client_tz;
	private String constr_;
	private SubscriptionBeanI subscriptinoEJB;
	private static Context context = null;
	private CMPResourceBeanRemote cmpbean;
	
	private void initEJB() throws Exception{
	    	String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
			 Properties props = new Properties();
			 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
			 props.put(Context.PROVIDER_URL, "remote://"+subscription_props.getProperty("ejbhost")+":"+subscription_props.getProperty("ejbhostport"));
			 props.put(Context.SECURITY_PRINCIPAL, subscription_props.getProperty("SECURITY_PRINCIPAL"));
			 props.put(Context.SECURITY_CREDENTIALS, subscription_props.getProperty("SECURITY_CREDENTIALS"));
			 props.put("jboss.naming.client.ejb.context", true);
			 context = new InitialContext(props);
			 cmpbean =  (CMPResourceBeanRemote) 
	       		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
			 subscriptinoEJB =  (SubscriptionBeanI) 
			       		context.lookup("cmp/SubscriptionEJB!com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI");
	}
	
	

	private void initialize() throws Exception {
		
		
		
		log4Jprops = FileUtils.getPropertyFile("log4jsub.properties");
		subscription_props = FileUtils.getPropertyFile("mtsender.properties");
		
		if(log4Jprops==null)
			BasicConfigurator.configure();
		else
			PropertyConfigurator.configure(log4Jprops);
		
		constr_= subscription_props.getProperty("constr");
		
		initEJB();
		
		setDaemon(true);//we've all other threads waiting for this	

	}
	

	
	public static void main(String[] args) throws Exception{
	
	
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() { 
		    	
		    	System.out.println("SHUTTING DOWN Subscription main!");
		    	SubscriptionMainTread.stopApp();
		    	
		    	System.out.println("SHUTTING DOWN SubscriptionRenewal!");
		    	SubscriptionRenewal.stopApp();
		    	
		    	try{
		    		if(SubscriptionMain.context!=null)
		    			SubscriptionMain.context.close();
		    		System.out.println("closed context safely");
		    	}catch(Exception exp){
		    		exp.printStackTrace();
		    	}
		    	
		    }
		});
		
		SubscriptionMain submain = new SubscriptionMain();
		submain.initialize();
		
		SubscriptionRenewal subscriptionRenewal = new SubscriptionRenewal();
		Thread ts = new Thread(subscriptionRenewal);
		ts.start();
		
		SubscriptionMainTread subApp = new SubscriptionMainTread(submain.cmpbean,submain.subscriptinoEJB);
		Thread t = new Thread(subApp);
		t.start();

		
		
		
		
		
	}
}