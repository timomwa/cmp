package com.pixelandtag.sms.mt.workerthreads;

import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.naming.remote.client.InitialContextFactory;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.sms.producerthreads.Billable;

public class TestEJB {
	
	public static void main(String[] args) throws NamingException {
		
		 String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		 Properties props = new Properties();
		 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		 props.put(Context.PROVIDER_URL, "remote://localhost:4447");
		 props.put(Context.SECURITY_PRINCIPAL, "testuser");
		 props.put(Context.SECURITY_CREDENTIALS, "testpassword123!");
		 props.put("jboss.naming.client.ejb.context", true);
		 Context context = new InitialContext(props);
		 CMPResourceBeanRemote cmpbean =   (CMPResourceBeanRemote) 
        		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		
		Billable billable = new Billable();
		billable.setCp_id("");
		billable.setMsisdn("12312312");
		try {
			boolean resp = cmpbean.testEJB(-1);
			
			System.out.println("RESP: "+resp);
		} catch (Exception e) {
			System.out.println("RESP: "+e.getMessage());
		}finally{
		    if(context!=null)
		    	try { 
		    		context.close(); 
		    		System.out.println("closed!");
		    	} catch (Exception ex) { ex.printStackTrace(); }
		    
		}
        
	}

}
