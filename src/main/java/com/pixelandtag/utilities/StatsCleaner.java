package com.pixelandtag.utilities;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.sms.producerthreads.Billable;

public class StatsCleaner {
	
	private static DatingServiceI datingBean;
	private static InitialContext context;
	private static Properties mtsenderprop;
	private static CMPResourceBeanRemote cmpresourcebean;
	private static TimezoneConverterI tzconvert;
	
	
	private static void initEJB() throws NamingException{
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
	}
	
	public static void main(String[] args) throws NamingException {
		try{
			if(args!=null && args.length >0){
				initEJB();
				cleanStats(args[0]);
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
		List<Billable> billables = cmpresourcebean.getBillableSForCleanup(date);
		int c = 0;
		int billable_size = billables.size();
		for(Billable bill : billables){
			int cleaned_Recs = cmpresourcebean.invalidateSimilarBillables(bill); 
			
			
			c++;
			
			System.out.println("cleaned="+cleaned_Recs+", progress : ("+c+"/"+ billable_size +")");
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
