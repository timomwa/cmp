package com.pixelandtag.serviceprocessors.sms;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.DatingServiceException;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.LocationBeanI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.TimeUnit;
import com.pixelandtag.dating.entities.ChatLog;
import com.pixelandtag.dating.entities.Gender;
import com.pixelandtag.dating.entities.Location;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileAttribute;
import com.pixelandtag.dating.entities.ProfileLocation;
import com.pixelandtag.dating.entities.ProfileQuestion;
import com.pixelandtag.dating.entities.QuestionLog;
import com.pixelandtag.dating.entities.SystemMatchLog;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.util.FileUtils;
import com.pixelandtag.web.beans.RequestObject;

public class DatingServiceProcessor extends GenericServiceProcessor {

	final Logger logger = Logger.getLogger(DatingServiceProcessor.class);
	private DatingServiceI datingBean;
	private LocationBeanI location_ejb;
	private CMPResourceBeanRemote cmp_bean;
	private SubscriptionBeanI subscriptionBean;
	private InitialContext context;
	//private Properties mtsenderprop;
	private boolean allow_number_sharing  = false;
	private boolean allow_multiple_plans = true;
	
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
		location_ejb = (LocationBeanI) context.lookup("cmp/LocationEJB!com.pixelandtag.cmp.ejb.LocationBeanI");
		cmp_bean = (CMPResourceBeanRemote) context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		subscriptionBean = (SubscriptionBeanI) context.lookup("cmp/SubscriptionEJB!com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI");
		 logger.debug("Successfully initialized EJB CMPResourceBeanRemote !!");
    }
	
	@Override
	public MOSms process(MOSms mo) {
		
		
		logger.info("\n\n\tIS SUBSCRIPTION?? "+mo.isSubscription());
		
		try {
			
			final RequestObject req = new RequestObject(mo);
			final String KEYWORD = req.getKeyword().trim();
			final String MESSAGE = req.getMsg().trim();
			final int serviceid = 	mo.getServiceid();
			final String MSISDN = req.getMsisdn();
			
			int language_id = 1;
		
			Person person = datingBean.getPerson(mo.getMsisdn());
			if(person==null)
				person = datingBean.register(mo.getMsisdn());
			
			PersonDatingProfile profile = datingBean.getProfile(person);
			
			if(KEYWORD.equalsIgnoreCase("BUNDLES")){
				String submenustring = cmp_bean.getSubMenuString(KEYWORD,language_id);
				mo.setMt_Sent(submenustring+cmp_bean.getMessage(MAIN_MENU_ADVICE,language_id));//get all the sub menus there.
			
			}else if(KEYWORD.equalsIgnoreCase("LOGIN")){
				
				person.setLoggedin(Boolean.TRUE);
				person = datingBean.saveOrUpdate(person);
				mo.setPrice(BigDecimal.ZERO);
				mo.setMt_Sent("Welcome back "+profile.getUsername()+"! People missed you while you were away. You'll now be able to receive messages sent by other people. Dial *329# to find a friend to chat with, or reply with FIND");
				return mo;
				
			}else if(KEYWORD.equalsIgnoreCase("LOGOUT")){
				
				person.setLoggedin(Boolean.FALSE);
				person = datingBean.saveOrUpdate(person);
				mo.setPrice(BigDecimal.ZERO);
				mo.setMt_Sent("You've successfully been logged out '"+profile.getUsername()+"'. You will not be able to receive messages from the chat service. To log back in, reply with LOGIN");
				return mo;
				
			}else if(KEYWORD.equalsIgnoreCase("FIND") || KEYWORD.equalsIgnoreCase("TAFUTA")) {
				
				if(person.getId()>0 && profile==null){//Success registering/registered but no profile
					
					mo = startProfileQuestions(mo,person);
					
					return mo;
				}
				
				Gender pref_gender = profile.getPreferred_gender();
				BigDecimal pref_age = profile.getPreferred_age();
				String location = profile.getLocation();
				//location_ejb
				PersonDatingProfile match = null;
				if(match==null){
					try{
						match = datingBean.findMatch(profile);//try find by their location
					}catch(DatingServiceException exp){
						logger.error(exp.getMessage(),exp);
					}
				}
				
				if(match==null)
					match = datingBean.findMatch(pref_gender,pref_age, location,person.getId());
				if(match==null)
					 match = datingBean.findMatch(pref_gender,pref_age,person.getId());
				if(match==null)
					 match = datingBean.findMatch(pref_gender,person.getId());
				
				if(match==null || match.getUsername()==null || match.getUsername().trim().isEmpty()){
					String msg = datingBean.getMessage(DatingMessages.COULD_NOT_FIND_MATCH_AT_THE_MOMENT, language_id);
					mo.setMt_Sent(msg.replaceAll(USERNAME_TAG, profile.getUsername()));
				}else{
					try{
						SystemMatchLog sysmatchlog = new SystemMatchLog();
						sysmatchlog.setPerson_a_id(person.getId());
						sysmatchlog.setPerson_b_id(match.getPerson().getId());
						sysmatchlog.setPerson_a_notified(true);
						sysmatchlog = datingBean.saveOrUpdate(sysmatchlog);
					}catch(Exception exp){
						logger.warn("\n\n\n\t\t"+exp.getMessage()+"\n\n",exp);
					}
					String gender_pronoun = pref_gender.equals(Gender.FEMALE)? datingBean.getMessage(GENDER_PRONOUN_F, language_id) : datingBean.getMessage(GENDER_PRONOUN_M, language_id);
					String gender_pronoun2 = pref_gender.equals(Gender.FEMALE)? datingBean.getMessage(GENDER_PRONOUN_INCHAT_F, language_id) : datingBean.getMessage(GENDER_PRONOUN_INCHAT_M, language_id);
					StringBuffer sb = new StringBuffer();
					BigInteger age = datingBean.calculateAgeFromDob(match.getDob());  
					sb.append("\n").append("Age: ").append(age.toString()).append("\n");
					String locationName = match.getLocation();
					if(locationName==null || locationName.trim().isEmpty()){
						ProfileLocation pl = location_ejb.findProfileLocation(match);
						if(pl!=null && pl.getLocation()!=null){
							locationName = pl.getLocation().getLocationName();
							if(locationName==null || locationName.trim().isEmpty()){
								Location loc = location_ejb.getLastKnownLocationWithNameUsingLac(pl.getLocation().getLocation_id());
								if(loc!=null)
									locationName = loc.getLocationName();
							}
						}
					}
					sb.append("Location : ").append(locationName).append("\n");
					sb.append("Gender : ").append(match.getGender()).append("\n");
					String msg = datingBean.getMessage(DatingMessages.MATCH_FOUND, language_id);
					msg = msg.replaceAll(USERNAME_TAG, profile.getUsername());
					msg = msg.replaceAll(GENDER_PRONOUN_TAG, gender_pronoun);
					msg = msg.replaceAll(GENDER_PRONOUN_TAG2, gender_pronoun2);
					msg = msg.replaceAll(DEST_USERNAME_TAG, match.getUsername());
					msg = msg.replaceAll(PROFILE_TAG, sb.toString());
					mo.setMt_Sent(msg);
					
					//notify person b?
				}
				
				
				
			}else if(KEYWORD.equalsIgnoreCase("RENEW") || KEYWORD.equalsIgnoreCase("WEZESHA") || KEYWORD.equalsIgnoreCase("BILLING_SERV5")
					|| KEYWORD.equalsIgnoreCase("BILLING_SERV15")
					|| KEYWORD.equalsIgnoreCase("BILLING_SERV30")){
				
				SMSService smsservice0 = datingBean.getSMSService(KEYWORD);
				List<String> services = new ArrayList<String>();
				services.add("BILLING_SERV5");
				services.add("DATE");
				services.add("BILLING_SERV15");
				services.add("BILLING_SERV30");
				
				boolean subvalid = datingBean.hasAnyActiveSubscription(MSISDN, services);
				
				
				if(!subvalid || allow_multiple_plans  ){
					
					try{
						mo = datingBean.renewSubscription(mo,smsservice0.getId());
						
					}catch(DatingServiceException dse){
						logger.error(dse.getMessage(),dse);
						mo.setMt_Sent(null);//set nul so that we don't send it out..
						mo.setPrice(BigDecimal.ZERO);
					}
					
				}else{
					mo.setPrice(BigDecimal.ZERO);
					mo.setMt_Sent("You already have a valid subscription. Dial *329# to find a friend to chat with, or reply with FIND");
				}
				
			}else if(KEYWORD.equalsIgnoreCase("DATE") || person!=null){
				
				if(mo.isSubscription()){//if it's subscription push, for this service we return no message.
					mo.setMt_Sent(null);
					mo.setPrice(BigDecimal.ZERO);
					return mo;
				}
				
				List<String> services = new ArrayList<String>();
				services.add("BILLING_SERV5");
				services.add("DATE");
				services.add("BILLING_SERV15");
				services.add("BILLING_SERV30");
				
				boolean subvalid = datingBean.hasAnyActiveSubscription(MSISDN, services);
				
				if(!subvalid)
					cmp_bean.mimicMO("BILLING_SERV5",MSISDN);
				
				if(subvalid && profile!=null && profile.getProfileComplete()){//if subscription is valid && their profile is complete
					
					mo = processDating(mo,person);
						
				}else if((profile==null || !profile.getProfileComplete() || !subvalid) ){//No profile or incomplete profile
					
					//Please top up to continue chatting with x ..
					int lang = 1;
					if(!subvalid && profile!=null && profile.getProfileComplete() ){//Profile is complete just need to renew subscription
						SMSService dating = datingBean.getSMSService("DATE");
						lang = profile.getLanguage_id();
						PersonDatingProfile dest = datingBean.getperSonUsingChatName(MESSAGE);//find destination person
						String msg = "";
						if(dest!=null){//if they were chatting with someone.
							msg = datingBean.getMessage(DatingMessages.RENEW_CHAT_SUBSCRIPTION, lang);
							logger.info("\n\n\n\t1. msg::::>>> "+msg);
							msg = msg.replaceAll(DEST_USERNAME_TAG,  dest.getUsername());
							
						}else{//else generic renew message
							msg  = datingBean.getMessage(DatingMessages.RENEW_SUBSCRIPTION, lang);
							logger.info("\n\n\n\t2. msg::::>>> "+msg);
						}
						logger.info("\n\n\n\tmsg::::>>> "+msg);
						msg = msg.replaceAll(USERNAME_TAG,  profile.getUsername());
						msg = msg.replaceAll(SERVICENAME_TAG, dating.getService_name());
						mo.setPrice(BigDecimal.ZERO);//set price to zero so they receive msg
						mo.setMt_Sent(msg);//tell them to renew
						mo.setPriority(3);
					}else{//No profile, so they create
						//(profile!=null &&  !profile.getProfileComplete() ){//if they've got a profile but not a complet
						mo = processDating(mo,person);
					}
				}
			}else{
				String msg = datingBean.getMessage(UNKNOWN_KEYWORD_ADVICE, language_id);
				mo.setMt_Sent(msg.replaceAll(USERNAME_TAG, KEYWORD));
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return mo;
	}



	private MOSms processDating(MOSms mo, Person person) throws Exception { 
					
		if(person==null)
			person = datingBean.register(mo.getMsisdn());
		
		
		PersonDatingProfile profile = datingBean.getProfile(person);
		
		if(profile!=null && profile.getProfileComplete()){
			mo = chat(mo,profile,person);
		}
				
		if(person.getId()>0 && profile==null){//Success registering/registered but no profile
			
			mo = startProfileQuestions(mo,person);
		
		}else{
			
			if(!profile.getProfileComplete()){
				
				mo = completeProfile(mo,person,profile);
			
			}
		}
		
		return mo;
	}

	private MOSms startProfileQuestions(MOSms mo, Person person) throws DatingServiceException {

		try{
				final RequestObject req = new RequestObject(mo);
				final String MSISDN = req.getMsisdn();
				int language_id = 1;
				
				String msg = null;
				try{
					msg = datingBean.getMessage(DatingMessages.DATING_SUCCESS_REGISTRATION, language_id);
				}catch(DatingServiceException dse){
					logger.error(dse.getMessage(), dse);
				}
				
				PersonDatingProfile profile = new PersonDatingProfile();
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
		}catch(Exception exp){
			throw new DatingServiceException("Sorry, problem occurred, please try again.",exp);
		}
		
		return mo;
	}

	private MOSms completeProfile(MOSms mo, Person person,
			PersonDatingProfile profile) throws DatingServiceException {
		
		try{
			
				final RequestObject req = new RequestObject(mo);
				final String KEYWORD = req.getKeyword().trim();
				final String MESSAGE = req.getMsg().trim();
				final String MSISDN = req.getMsisdn();
				
				int language_id = 1;
				
				
				ProfileQuestion previousQuestion = datingBean.getPreviousQuestion(profile.getId());
				final ProfileAttribute attr = previousQuestion.getAttrib();
				logger.debug("PREVIOUS QUESTION ::: "+previousQuestion.getQuestion() + " SUB ANSWER : "+MESSAGE);
			
				logger.debug("ATRIBUTE ADDRESSING ::: "+attr.toString());
				
				if(attr.equals(ProfileAttribute.DISCLAIMER)){
					boolean keywordIsNumber = false;
					int agreed = -1;
					try{
						agreed = Integer.parseInt(KEYWORD);
						keywordIsNumber = true;
					}catch(Exception exp){}
					
					if( (keywordIsNumber && agreed==2 ) || (KEYWORD!=null && (KEYWORD.trim().equalsIgnoreCase("B") || KEYWORD.trim().equalsIgnoreCase("Y") || KEYWORD.trim().equalsIgnoreCase("YES"))) ){
						person.setAgreed_to_tnc(Boolean.TRUE);
						person = datingBean.saveOrUpdate(person);
					}else if((keywordIsNumber && agreed==1 ) || (KEYWORD!=null && (KEYWORD.trim().equalsIgnoreCase("A") || KEYWORD.trim().equalsIgnoreCase("N") || KEYWORD.trim().equalsIgnoreCase("NO")))){
						mo.setMt_Sent("Ok. Bye");
						return mo;
					}else{
						String msg = datingBean.getMessage(DatingMessages.MUST_AGREE_TO_TNC, language_id);
						mo.setMt_Sent(msg+SPACE+previousQuestion.getQuestion());
						return mo;
					}
						
				}
				
				if(attr.equals(ProfileAttribute.CHAT_USERNAME)){
					boolean isunique = datingBean.isUsernameUnique(KEYWORD);
					if(isunique){
						profile.setUsername(KEYWORD);
					}else{
						String msg = "";
						if(KEYWORD.equalsIgnoreCase(mo.getSMS_SourceAddr())){
							msg = datingBean.getMessage(DatingMessages.REPLY_WITH_USERNAME, language_id);
						}else{
							msg = datingBean.getMessage(DatingMessages.USERNAME_NOT_UNIQUE_TRY_AGAIN, language_id);
						}
						mo.setMt_Sent(msg.replaceAll(USERNAME_TAG, KEYWORD));
						return mo;
					
					}
					
						
				}
				
				if(attr.equals(ProfileAttribute.GENDER)){
					if(MESSAGE.equalsIgnoreCase("2") || MESSAGE.equalsIgnoreCase("M") ||  MESSAGE.equalsIgnoreCase("MALE") ||  MESSAGE.equalsIgnoreCase("MAN") ||  MESSAGE.equalsIgnoreCase("BOY") ||  MESSAGE.equalsIgnoreCase("MUME") ||  MESSAGE.equalsIgnoreCase("MWANAMME")  ||  MESSAGE.equalsIgnoreCase("MWANAUME")){ 
						profile.setGender(Gender.MALE);
						profile.setPreferred_gender(Gender.FEMALE);
					}else if(MESSAGE.equalsIgnoreCase("2") || MESSAGE.equalsIgnoreCase("F") ||  MESSAGE.equalsIgnoreCase("FEMALE") ||  MESSAGE.equalsIgnoreCase("LADY") ||  MESSAGE.equalsIgnoreCase("GIRL") ||  MESSAGE.equalsIgnoreCase("MKE") ||  MESSAGE.equalsIgnoreCase("MWANAMKE")  ||  MESSAGE.equalsIgnoreCase("MWANAMUKE")){ 
						profile.setGender(Gender.FEMALE);
						profile.setPreferred_gender(Gender.MALE);
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
						msg = msg.replaceAll(USERNAME_TAG, profile.getUsername());
						msg = msg.replaceAll(AGE_TAG, age.intValue()+"");
						mo.setMt_Sent(msg);
						return mo;
					}
					
					if(age.compareTo(new BigDecimal(100l))>=0){
						String msg = datingBean.getMessage(DatingMessages.UNREALISTIC_AGE, language_id);
						msg = msg.replaceAll(USERNAME_TAG,  profile.getUsername());
						msg = msg.replaceAll(AGE_TAG, age.intValue()+"");
						mo.setMt_Sent(msg);
						return mo;
					}
					
					if(age.compareTo(new BigDecimal(18l))<0){
						String msg = datingBean.getMessage(DatingMessages.SERVICE_FOR_18_AND_ABOVE, language_id);
						mo.setMt_Sent(msg.replaceAll(USERNAME_TAG,  profile.getUsername()));
						return mo;
					}
					
					dob = datingBean.calculateDobFromAge(age);
					profile.setDob( dob );
					profile.setPreferred_age(BigDecimal.valueOf(18L));
				}
				
				if(attr.equals(ProfileAttribute.LOCATION)){
					boolean location_is_only_number = false;
					try{
						new BigDecimal(MESSAGE);
						location_is_only_number = true;
					}catch(java.lang.NumberFormatException nfe){
					}
					if(KEYWORD.contains("*") || KEYWORD.equalsIgnoreCase(mo.getSMS_SourceAddr()) || MESSAGE.equalsIgnoreCase(mo.getSMS_SourceAddr()) || location_is_only_number){
						String msg = datingBean.getMessage(DatingMessages.LOCATION_INVALID, language_id);
						mo.setMt_Sent(msg.replaceAll(USERNAME_TAG,  profile.getUsername()));
						return mo;
					}else{
						profile.setLocation(MESSAGE);
						
						profile.setProfileComplete(true);
						person.setActive(true);
						profile.setPerson(person);
						
						SMSService smsservice = datingBean.getSMSService("DATE");
						
						subscriptionBean.renewSubscription(MSISDN, smsservice, SubscriptionStatus.confirmed);
					}
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
					if(MESSAGE.equalsIgnoreCase("2") || MESSAGE.equalsIgnoreCase("M") ||  MESSAGE.equalsIgnoreCase("MALE") ||  MESSAGE.equalsIgnoreCase("MAN") ||  MESSAGE.equalsIgnoreCase("BOY") ||  MESSAGE.equalsIgnoreCase("MUME") ||  MESSAGE.equalsIgnoreCase("MWANAMME")  ||  MESSAGE.equalsIgnoreCase("MWANAUME")) {
						profile.setPreferred_gender(Gender.MALE);
					}else if(MESSAGE.equalsIgnoreCase("1") || MESSAGE.equalsIgnoreCase("F") ||  MESSAGE.equalsIgnoreCase("FEMALE") ||  MESSAGE.equalsIgnoreCase("LADY") ||  MESSAGE.equalsIgnoreCase("GIRL") ||  MESSAGE.equalsIgnoreCase("MKE") ||  MESSAGE.equalsIgnoreCase("MWANAMKE")  ||  MESSAGE.equalsIgnoreCase("MWANAMUKE")){ 
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
					Gender pref_gender = profile.getPreferred_gender();
					BigDecimal pref_age = profile.getPreferred_age();
					String location = profile.getLocation();
					
					PersonDatingProfile match = null;
					if(match==null){
						try{
							match = datingBean.findMatch(profile);//try find by their location
						}catch(DatingServiceException exp){
							logger.warn(exp.getMessage(),exp);
						}
					}
					if(match==null)
						match = datingBean.findMatch(pref_gender,pref_age, location,person.getId());
					if(match==null)
						 match = datingBean.findMatch(pref_gender,pref_age,person.getId());
					if(match==null)
						 match = datingBean.findMatch(pref_gender,person.getId());
					
					if(match==null){
						
						String msg = datingBean.getMessage(DatingMessages.PROFILE_COMPLETE, language_id);
						mo.setMt_Sent(msg.replaceAll(USERNAME_TAG, profile.getUsername()));
						
					}else{
						try{
							SystemMatchLog sysmatchlog = new SystemMatchLog();
							sysmatchlog.setPerson_a_id(person.getId());
							sysmatchlog.setPerson_b_id(match.getPerson().getId());
							sysmatchlog.setPerson_a_notified(true);
							sysmatchlog = datingBean.saveOrUpdate(sysmatchlog);
						}catch(Exception exp){
							logger.warn("\n\n\n\t\t"+exp.getMessage()+"\n\n");
						}
					
						String gender_pronoun = pref_gender.equals(Gender.FEMALE)? datingBean.getMessage(GENDER_PRONOUN_F, language_id) : datingBean.getMessage(GENDER_PRONOUN_M, language_id);
						String gender_pronoun2 = pref_gender.equals(Gender.FEMALE)? datingBean.getMessage(GENDER_PRONOUN_INCHAT_F, language_id) : datingBean.getMessage(GENDER_PRONOUN_INCHAT_M, language_id);
						String msg = datingBean.getMessage(DatingMessages.MATCH_FOUND, language_id);
						logger.info("\n\n\n\t\t msg:::"+msg);
						logger.info("\n\n\n\t\t profile:::"+profile);
						StringBuffer sb = new StringBuffer();
						BigInteger age = datingBean.calculateAgeFromDob(match.getDob()); 
						sb.append("\n").append("Age: ").append(age).append("\n");
						String locationName = match.getLocation();
						if(locationName==null || locationName.trim().isEmpty()){
							ProfileLocation pl = location_ejb.findProfileLocation(match);
							if(pl!=null && pl.getLocation()!=null){
								locationName = pl.getLocation().getLocationName();
								if(locationName==null || locationName.trim().isEmpty()){
									Location loc = location_ejb.getLastKnownLocationWithNameUsingLac(pl.getLocation().getLocation_id());
									if(loc!=null)
										locationName = loc.getLocationName();
								}
							}
						}
						sb.append("Location : ").append(locationName).append("\n");
						sb.append("Gender : ").append(match.getGender()).append("\n");
						msg = msg.replaceAll(USERNAME_TAG, profile.getUsername());
						msg = msg.replaceAll(GENDER_PRONOUN_TAG, gender_pronoun);
						msg = msg.replaceAll(GENDER_PRONOUN_TAG2, gender_pronoun2);
						msg = msg.replaceAll(DEST_USERNAME_TAG, match.getUsername());
						msg = msg.replaceAll(PROFILE_TAG, sb.toString());
						mo.setMt_Sent(msg);
					}
					
				}
		
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw new DatingServiceException("Sorry, we couldn't process your request at this time. Kindly try again.",exp);
			
		}
		
		return mo;
	}

	private MOSms chat(MOSms mo, PersonDatingProfile profile, Person person) throws DatingServiceException {
				
		try{
			
			final RequestObject req = new RequestObject(mo);
			final String KEYWORD = req.getKeyword().trim();
			final String MESSAGE = req.getMsg().trim();
			
			//If profile is complete, we allow chat
			boolean directMsg  = false;
			//System.out.println("profile complete ? "+profile.getProfileComplete());
			PersonDatingProfile destination_person = datingBean.getperSonUsingChatName(KEYWORD);
			
			if(destination_person==null){//Is a direct message, so we get last person they sent a message to
				destination_person = datingBean.getProfileOfLastPersonIsentMessageTo(person,0L,TimeUnit.SECOND);//last 30 minutes
				if(destination_person!=null)
					directMsg  = true;
			}
			
			int language_id = profile.getLanguage_id()>0 ? profile.getLanguage_id() : 1;
			
			if(destination_person!=null && destination_person.getPerson().getActive()){
				
				
				String source_user = profile.getUsername();
				ChatLog log = new ChatLog();
				log.setSource_person_id(person.getId());
				log.setDest_person_id(destination_person.getPerson().getId());
				String chatLog = (allow_number_sharing ? MESSAGE : MESSAGE.replaceAll("\\d{3,10}", "*"));
				log.setMessage(directMsg ? (destination_person.getUsername() +CHAT_USERNAME_SEPERATOR_DIRECT+ chatLog) : chatLog);
				mo.setPrice(BigDecimal.ZERO);
				
				
				if(destination_person.getPerson().getLoggedin()==null || destination_person.getPerson().getLoggedin()==true){
					log.setOffline_msg(Boolean.FALSE);
					log = datingBean.saveOrUpdate(log);
					
					MOSms chatMT  = mo.clone();
					chatMT.setMsisdn(destination_person.getPerson().getMsisdn());
					Gender gender = profile.getGender();
					String pronoun = gender.equals(Gender.FEMALE) ? datingBean.getMessage(GENDER_PRONOUN_INCHAT_F, language_id) : datingBean.getMessage(GENDER_PRONOUN_INCHAT_M, language_id);
					String msg = "";
					if(!directMsg){//if it's not a direct message, then put advice
						msg = source_user+CHAT_USERNAME_SEPERATOR+(allow_number_sharing ? MESSAGE.replaceAll(KEYWORD, "") : MESSAGE.replaceAll(KEYWORD, "").trim().replaceAll("\\d{5,10}", "*")) ;
						msg += NEW_LINE+" to continue chatting with "+pronoun+", reply starting with "+source_user+" SMS cost 0.0/-";
					}else{
				        msg = source_user+CHAT_USERNAME_SEPERATOR_DIRECT+(allow_number_sharing ? MESSAGE.replaceAll(KEYWORD, "") : MESSAGE.replaceAll(KEYWORD, "").trim().replaceAll("\\d{5,10}", "*")) ;
					}
					chatMT.setMt_Sent(msg);
					chatMT.setCMP_Txid(BigInteger.valueOf(generateNextTxId()));
					chatMT.setPriority(0);//highest priority possible
					chatMT.setPrice(BigDecimal.ZERO);
					chatMT.setCMP_AKeyword(mo.getCMP_AKeyword());
					chatMT.setCMP_SKeyword(mo.getCMP_SKeyword());
					sendMT(chatMT);
					String tailmsg = "";
					if(!person.getLoggedin()){
						tailmsg = ". However, you're offline. This means you'll not be able to receive any messages from anyone or '"+destination_person.getUsername()+"'. Reply with the word LOGIN to log in.";
					}
					
					mo.setMt_Sent("Message sent to '"+destination_person.getUsername()+"'"+tailmsg);
				}else{
					log.setOffline_msg(Boolean.TRUE);
					mo.setPrice(BigDecimal.ZERO);
					mo.setMt_Sent("Sorry "+profile.getUsername()+", '"+destination_person.getUsername()+"' is currently offline. You can chat with them when they get back online.");
				}
				return mo;
			}else if(destination_person!=null && !destination_person.getPerson().getActive()){
				Gender gender  = destination_person.getGender();
				String pronoun = gender.equals(Gender.FEMALE) ? datingBean.getMessage(GENDER_PRONOUN_INCHAT_F, language_id) : datingBean.getMessage(GENDER_PRONOUN_INCHAT_M, language_id);
				String msg = "Sorry, \""+destination_person.getUsername()+"\" has unsubcribed from the chat service and you cannot chat with "+pronoun+". To find a different person to chat with, reply with \"FIND\" and the system will find a match for you based on your profile.";
				mo.setMt_Sent(msg);
				return mo;
			}else{//destination person not found.. Check in their friends list. Ask them whom they want to chat to..
				mo.setPrice(BigDecimal.ZERO);
				destination_person = datingBean.getProfileOfLastPersonIsentMessageTo(person,0L,TimeUnit.MINUTE);//last 1 year
				String msg = "";
				if(destination_person!=null){
					Gender gender  = destination_person.getGender();
					String pronoun = gender.equals(Gender.FEMALE) ? datingBean.getMessage(GENDER_PRONOUN_INCHAT_F, language_id) : datingBean.getMessage(GENDER_PRONOUN_INCHAT_M, language_id);
					msg = "Sorry, no user with the username \""+KEYWORD+"\". "
								+ "You last had a chat with "+destination_person.getUsername()+", if you want to continue chatting with "+pronoun+", send your message starting"
								+ " with \""+destination_person.getUsername()+"\", or to find another person to chat with, reply with \"FIND\" and we shall hook you up!";
				}else{
					msg = "Sorry, no user with the username \""+KEYWORD+"\". ";
				}
				mo.setMt_Sent(msg);//get their friendslist/match
				return mo;
				
			}
		
		}catch(Exception exp){
			throw new DatingServiceException("Sorry, something went wrong. Please try again",exp);
		}
	}

	@Override
	public void finalizeMe() {
		try {
			context.close();
		} catch (NamingException e) {
			logger.error(e.getMessage(),e);
		}
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
