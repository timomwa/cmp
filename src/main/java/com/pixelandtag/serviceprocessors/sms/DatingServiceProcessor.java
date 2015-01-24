package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.DatingServiceException;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.util.FileUtils;
import com.pixelandtag.web.beans.RequestObject;

public class DatingServiceProcessor extends GenericServiceProcessor {

	final Logger logger = Logger.getLogger(DatingServiceProcessor.class);
	private DatingServiceI datingBean;
	private InitialContext context;
	private Properties mtsenderprop;
	
	public DatingServiceProcessor() throws NamingException{
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		initEJB();
	}
	
	public void initEJB() throws NamingException{
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
		 
		 System.out.println("Successfully initialized EJB CMPResourceBeanRemote !!");
    }
	
	@Override
	public MOSms process(MOSms mo) {
		
		
		
		try {
			
			final RequestObject req = new RequestObject(mo);
			final String KEYWORD = req.getKeyword().trim();
			final int serviceid = 	mo.getServiceid();
			final String MSISDN = req.getMsisdn();
			
			Person person = null;
			int language_id = -1;
			
			
			
			
			
			
			person = datingBean.getPerson(mo.getMsisdn());
			
			if(person==null)
				person = datingBean.register(mo.getMsisdn());
			
			
			PersonDatingProfile profile = datingBean.getProfile(person);
			
			if(person.getId()>0 && profile==null){//Success registering/registered but no profile
				//Prompt subscriber to create profile
				String msg = null;
				try{
					msg = datingBean.getMessage(DatingMessages.DATING_SUCCESS_REGISTRATION, language_id);
					
				}catch(DatingServiceException dse){
					logger.error(dse.getMessage(), dse);
				}
				
				if(msg==null)
					msg = "You've been successfully registered for the dating service. Next, you need to create a profile";
				
				
				mo.setMt_Sent(msg);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return mo;
	}

	@Override
	public void finalizeMe() {
		// TODO Auto-generated method stub

	}

	@Override
	public Connection getCon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseEntityI getEJB() {
		return this.datingBean;
	}

}
