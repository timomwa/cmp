package com.pixelandtag.utilities;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.CleanupDTO;
import com.pixelandtag.sms.producerthreads.SuccessfullyBillingRequests;
import com.pixelandtag.util.FileUtils;

public class StatsCleaner {
	
	private static InitialContext context;
	private static CMPResourceBeanRemote cmpresourcebean;
	private static TimezoneConverterI tzconvert;
	private static Properties mtsenderprop;
	
	
	private static void initEJB() throws NamingException{
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		 Properties props = new Properties();
		 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		 props.put(Context.PROVIDER_URL, "remote://"+mtsenderprop.getProperty("ejbhost")+":"+mtsenderprop.getProperty("ejbhostport"));
		 props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
		 props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
		 props.put("jboss.naming.client.ejb.context", true);
		 context = new InitialContext(props);
		 cmpresourcebean =  (CMPResourceBeanRemote) 
		      		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		 tzconvert =  (TimezoneConverterI) 
		      		context.lookup("cmp/TimezoneConverterEJB!com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI");
	}
	
	public static void main(String[] args) throws NamingException {
		try{
			if(args!=null && args.length >0){
				initEJB();
				for(int i = 0; i<args.length; i++)
					cleanStats(args[i]);
			}else{
				System.out.println("Please supply the date you want to clean the stats for. date format e.g 2015-05-05");
			}
		}catch(Exception esp){
			esp.printStackTrace();
		}finally{
			cleanup();
		}
	}

	private static void cleanStats(String dateStr) throws Exception{
		Date date = tzconvert.stringToDate(dateStr+" 00:00:00");
		List<Billable> cleanupDtos = cmpresourcebean.getBillableSForTransfer(date);
		
		int c = 0;
		int billable_size = cleanupDtos.size();
		
		for(Billable bill : cleanupDtos){
			
			int cleaned_Recs = 0;
			
			bill.setTransferIn(Boolean.TRUE);
			
			cmpresourcebean.createSuccesBillRec(bill);
			
				
			c++;
			System.out.println("Date = "+dateStr+", cleaned="+cleaned_Recs+", progress : ("+c+"/"+ billable_size +")");
		}
	}
	
	
	private static void cleanup()  {

		try {
			context.close();
		} catch (NamingException e) {
			e.printStackTrace();
		}
		
	}

	

}
