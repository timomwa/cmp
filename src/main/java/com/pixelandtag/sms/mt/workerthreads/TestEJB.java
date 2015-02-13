package com.pixelandtag.sms.mt.workerthreads;

import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.naming.remote.client.InitialContextFactory;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.DatingServiceException;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.entities.TimeUnit;
import com.pixelandtag.dating.entities.Gender;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.util.FileUtils;

public class TestEJB {
	
	
	private static DatingServiceI datingBean;
	private static InitialContext context;
	private static Properties mtsenderprop;
	
	public static void main(String[] args) throws Exception {
		
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
		
	
		 Person person = datingBean.find(Person.class, 4203L);
		 System.out.println(person);
		 
		 PersonDatingProfile profile = datingBean.getProfileOfLastPersonIsentMessageTo(person, 1L, TimeUnit.YEAR); 
		 System.out.println("LAST PERSON I CHATTED WITH id="+profile.getId()+" username: "+profile.getUsername()); 
		 profile = datingBean.getProfile(person);
		 
			Gender pref_gender = profile.getPreferred_gender();
			BigDecimal pref_age = profile.getPreferred_age();
			String location = profile.getLocation();
			
			System.out.println("pref_gender : "+pref_gender.toString());
			System.out.println("pref_age : "+pref_age);
			System.out.println("location : "+location);
			PersonDatingProfile match = datingBean.findMatch(pref_gender,pref_age, location,-1L);

			if(match!=null)
				System.out.println(match.getUsername());
			else
				System.out.println(match);
				 
		 context.close();
		 
		 
	}

}
