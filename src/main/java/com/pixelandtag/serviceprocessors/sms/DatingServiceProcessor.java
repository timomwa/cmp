package com.pixelandtag.serviceprocessors.sms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jboss.ejb.client.EJBClientContext;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.DatingServiceException;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.dating.entities.Gender;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileAttribute;
import com.pixelandtag.dating.entities.ProfileQuestion;
import com.pixelandtag.dating.entities.QuestionLog;
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
		 
		 logger.debug("Successfully initialized EJB CMPResourceBeanRemote !!");
    }
	
	@Override
	public MOSms process(MOSms mo) {
		
		
		
		try {
			
			final RequestObject req = new RequestObject(mo);
			final String KEYWORD = req.getKeyword().trim();
			final String MESSAGE = req.getMsg().trim();
			final int serviceid = 	mo.getServiceid();
			final String MSISDN = req.getMsisdn();
			
			Person person = null;
			int language_id = 1;
			
			
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
				
				profile = new PersonDatingProfile();
				profile.setPerson(person);
				profile.setUsername(MSISDN);
				
				profile = datingBean.saveOrUpdate(profile);
				
				ProfileQuestion question = datingBean.getNextProfileQuestion(profile.getId());
				logger.debug("QUESTION::: "+question.getQuestion());
				mo.setMt_Sent(msg+ SPACE +question.getQuestion());
				
				QuestionLog ql = new QuestionLog();
				
				ql.setProfile_id_fk(profile.getId());
				ql.setQuestion_id_fk(question.getId());
				ql = datingBean.saveOrUpdate(ql);
			
			}else{
				
				
				if(!profile.getProfileComplete()){//If profile isn't complete
					
					ProfileQuestion previousQuestion = datingBean.getPreviousQuestion(profile.getId());
					final ProfileAttribute attr = previousQuestion.getAttrib();
					logger.debug("PREVIOUS QUESTION ::: "+previousQuestion.getQuestion() + " SUB ANSWER : "+MESSAGE);
				
					logger.debug("ATRIBUTE ADDRESSING ::: "+attr.toString());
					if(attr.equals(ProfileAttribute.CHAT_USERNAME)){
						boolean isunique = datingBean.isUsernameUnique(KEYWORD);
						if(isunique){
							profile.setUsername(KEYWORD);
						}else{
							String msg = datingBean.getMessage(DatingMessages.USERNAME_NOT_UNIQUE_TRY_AGAIN, language_id);
							mo.setMt_Sent(msg.replaceAll(USERNAME_TAG, KEYWORD));
							return mo;
						}
							
					}
					
					if(attr.equals(ProfileAttribute.GENDER)){
						if(MESSAGE.equalsIgnoreCase("M") ||  MESSAGE.equalsIgnoreCase("MALE") ||  MESSAGE.equalsIgnoreCase("MAN") ||  MESSAGE.equalsIgnoreCase("BOY") ||  MESSAGE.equalsIgnoreCase("MUME") ||  MESSAGE.equalsIgnoreCase("MWANAMME")  ||  MESSAGE.equalsIgnoreCase("MWANAUME")){ 
							profile.setGender(Gender.MALE);
						}else if(MESSAGE.equalsIgnoreCase("F") ||  MESSAGE.equalsIgnoreCase("FEMALE") ||  MESSAGE.equalsIgnoreCase("LADY") ||  MESSAGE.equalsIgnoreCase("GIRL") ||  MESSAGE.equalsIgnoreCase("MKE") ||  MESSAGE.equalsIgnoreCase("MWANAMKE")  ||  MESSAGE.equalsIgnoreCase("MWANAMUKE")){ 
							profile.setGender(Gender.FEMALE);
						}else{
							String msg = null;
							try{
								msg = datingBean.getMessage(DatingMessages.GENDER_NOT_UNDERSTOOD, language_id);
							}catch(DatingServiceException dse){
								logger.error(dse.getMessage(), dse);
							}
							mo.setMt_Sent(msg.replaceAll(USERNAME_TAG, KEYWORD));
							return mo;
						}
					}
					
					if(attr.equals(ProfileAttribute.AGE)){
						Date dob = new Date();
						BigDecimal age = null;
						try{
							age = new BigDecimal(KEYWORD);
						}catch(java.lang.NumberFormatException nfe){
							String msg = datingBean.getMessage(DatingMessages.AGE_NUMBER_INCORRECT, language_id);
							mo.setMt_Sent(msg.replaceAll(USERNAME_TAG, profile.getUsername()));
							return mo;
						}
						
						if(age.compareTo(new BigDecimal(18l))<0){
							String msg = datingBean.getMessage(DatingMessages.SERVICE_FOR_18_AND_ABOVE, language_id);
							mo.setMt_Sent(msg.replaceAll(USERNAME_TAG,  profile.getUsername()));
							return mo;
						}
						
						dob = datingBean.calculateDobFromAge(age);
						profile.setDob( dob );
					}
					
					if(attr.equals(ProfileAttribute.LOCATION)){
						profile.setLocation(MESSAGE);
					}
					if(attr.equals(ProfileAttribute.PREFERRED_AGE)){
						BigDecimal age = null;
						try{
							age = new BigDecimal(KEYWORD);
						}catch(java.lang.NumberFormatException nfe){
							String msg = datingBean.getMessage(DatingMessages.AGE_NUMBER_INCORRECT, language_id);
							mo.setMt_Sent(msg.replaceAll(USERNAME_TAG, profile.getUsername()));
							return mo;
						}
						
						if(age.compareTo(new BigDecimal(18l))<0){
							String msg = datingBean.getMessage(DatingMessages.SERVICE_FOR_18_AND_ABOVE, language_id);
							mo.setMt_Sent(msg.replaceAll(USERNAME_TAG,  profile.getUsername()));
							return mo;
						}
						profile.setPreferred_age(age);
					}
					if(attr.equals(ProfileAttribute.PREFERRED_GENDER)){
						if(MESSAGE.equalsIgnoreCase("M") ||  MESSAGE.equalsIgnoreCase("MALE") ||  MESSAGE.equalsIgnoreCase("MAN") ||  MESSAGE.equalsIgnoreCase("BOY") ||  MESSAGE.equalsIgnoreCase("MUME") ||  MESSAGE.equalsIgnoreCase("MWANAMME")  ||  MESSAGE.equalsIgnoreCase("MWANAUME")) {
							profile.setPreferred_gender(Gender.MALE);
						}else if(MESSAGE.equalsIgnoreCase("F") ||  MESSAGE.equalsIgnoreCase("FEMALE") ||  MESSAGE.equalsIgnoreCase("LADY") ||  MESSAGE.equalsIgnoreCase("GIRL") ||  MESSAGE.equalsIgnoreCase("MKE") ||  MESSAGE.equalsIgnoreCase("MWANAMKE")  ||  MESSAGE.equalsIgnoreCase("MWANAMUKE")){ 
							profile.setPreferred_gender(Gender.FEMALE);
						}else{
							String msg = null;
							try{
								msg = datingBean.getMessage(DatingMessages.GENDER_NOT_UNDERSTOOD, language_id);
							}catch(DatingServiceException dse){
								logger.error(dse.getMessage(), dse);
							}
							mo.setMt_Sent(msg.replaceAll(USERNAME_TAG, KEYWORD));
							return mo;
						}
						profile.setProfileComplete(true);
						person.setActive(true);
						profile.setPerson(person);
					}
					
					profile = datingBean.saveOrUpdate(profile);
					
					
					ProfileQuestion question = datingBean.getNextProfileQuestion(profile.getId());
					
					if(question!=null){
						logger.debug("QUESTION::: "+question.getQuestion());
						mo.setMt_Sent(question.getQuestion().replaceAll(USERNAME_TAG, profile.getUsername()));
						
						QuestionLog ql = new QuestionLog();
						
						ql.setProfile_id_fk(profile.getId());
						ql.setQuestion_id_fk(question.getId());
						ql = datingBean.saveOrUpdate(ql);
					}else{
						String msg = datingBean.getMessage(DatingMessages.PROFILE_COMPLETE, language_id);
						mo.setMt_Sent(msg.replaceAll(USERNAME_TAG, profile.getUsername()));
						
					}
				
				}
				
	
				
				
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
