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
import com.pixelandtag.dating.entities.SystemMatchLog;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.smsmenu.MenuItem;
import com.pixelandtag.util.FileUtils;

public class TestEJB {
	
	
	private static DatingServiceI datingBean;
	private static InitialContext context;
	private static Properties mtsenderprop;
	private static CMPResourceBeanRemote cmpresourcebean;
	
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
		 cmpresourcebean =  (CMPResourceBeanRemote) 
		      		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
	
		 //com.pixelandtag.cmp.ejb.CMPResourceBean.getMenuByParentLevelId(int, int, int)
		 int language_id =1;
		 int parent_level_id = -1;
		 int menuid=2;
		 //int 
		 MenuItem mi =   cmpresourcebean.getMenuByParentLevelId(language_id, parent_level_id, menuid);
		 System.out.println(mi);
		 
		 /*
		 Person person = datingBean.find(Person.class, 1560L);
		 System.out.println(person);
		 PersonDatingProfile thisPerson = datingBean.getProfile(person);
		 
		 
		 PersonDatingProfile profile = datingBean.getProfileOfLastPersonIsentMessageTo(person, 1L, TimeUnit.YEAR); 
		 System.out.println("LAST PERSON I ("+thisPerson.getUsername()+") CHATTED WITH id="+profile.getId()+" username: "+profile.getUsername()); 
		 profile = datingBean.getProfile(person);
		 
			Gender pref_gender = profile.getPreferred_gender();
			BigDecimal pref_age = profile.getPreferred_age();
			String location = profile.getLocation();
			
			System.out.println("pref_gender : "+pref_gender.toString());
			System.out.println("pref_age : "+pref_age);
			System.out.println("location : "+location);
			PersonDatingProfile match = datingBean.findMatch(profile);
					
					
					System.out.println("MATCH FOUND ?? "+match);

			int x = 0;
			
			if(match==null){
				match = datingBean.findMatch(pref_gender,pref_age, location,person.getId());
				x = 1;
			}
			
			if(match==null){
				match = datingBean.findMatch(pref_gender,pref_age, person.getId());
				x = 1;
			}
			if(match==null){
				match = datingBean.findMatch(pref_gender,person.getId());
				x = 2;
			}
			if(match!=null){
				System.out.println(x +". match.getPerson().getId(): "+match.getPerson().getId()+" MAtch: "+match.getId()+" username: "+match.getUsername());
				try{
					System.out.println("PERSON A: "+person.getId());
					System.out.println("PERSON B: "+match.getPerson().getId());
					SystemMatchLog sysmatchlog = new SystemMatchLog();
					sysmatchlog.setPerson_a_id(person.getId());
					sysmatchlog.setPerson_b_id(match.getPerson().getId());
					sysmatchlog.setPerson_a_notified(true);
					sysmatchlog = datingBean.saveOrUpdate(sysmatchlog);
				}catch(Exception exp){
					exp.printStackTrace();
				}
			}else{
				System.out.println("MAtch: "+match);
			}
			
			*/
		 context.close();
		 
		 
	}

}
