package com.pixelandtag.serviceprocessors.sms;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.DatingServiceException;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.LocationBeanI;
import com.pixelandtag.cmp.ejb.MessageEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI;
import com.pixelandtag.cmp.ejb.subscription.FreeLoaderEJBI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.Message;
import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.TimeUnit;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.dating.entities.AlterationMethod;
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
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.util.FileUtils;
import com.pixelandtag.web.beans.RequestObject;

public class DatingServiceProcessor extends GenericServiceProcessor {

	final Logger logger = Logger.getLogger(DatingServiceProcessor.class);
	private DatingServiceI datingBean;
	private LocationBeanI location_ejb; 
	private CMPResourceBeanRemote cmp_bean;
	private OpcoSMSServiceEJBI opcosmsserviceejb;
	private SubscriptionBeanI subscriptionBean;
	private OpcoSenderProfileEJBI opcosenderprofileEJB;
	private FreeLoaderEJBI freeloaderEJB;
	private MessageEJBI messageEJB;
	private InitialContext context;
	//private Properties mtsenderprop;
	private boolean allow_number_sharing  = true;
	private boolean allow_multiple_plans = false;
	
	public DatingServiceProcessor() throws NamingException{
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		initEJB();
	}
	
	
	public void initEJB() throws NamingException{
    	String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		 Properties props = new Properties();
		 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		 props.put(Context.PROVIDER_URL, "remote://"+mtsenderprop.getProperty("ejbhost")+":"+mtsenderprop.getProperty("ejbhostport"));
		 props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
		 props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
		 props.put("jboss.naming.client.ejb.context", true);
		 context = new InitialContext(props);
		 datingBean =  (DatingServiceI) 
       		context.lookup("cmp/DatingServiceBean!com.pixelandtag.cmp.ejb.DatingServiceI");
		location_ejb = (LocationBeanI) context.lookup("cmp/LocationEJB!com.pixelandtag.cmp.ejb.LocationBeanI");
		cmp_bean = (CMPResourceBeanRemote) context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		subscriptionBean = (SubscriptionBeanI) context.lookup("cmp/SubscriptionEJB!com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI");
		opcosenderprofileEJB = (OpcoSenderProfileEJBI) context.lookup("cmp/OpcoSenderProfileEJBImpl!com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI");
		opcosmsserviceejb = (OpcoSMSServiceEJBI) context.lookup("cmp/OpcoSMSServiceEJBImpl!com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI");
		freeloaderEJB =  (FreeLoaderEJBI) context.lookup("cmp/FreeLoaderEJBImpl!com.pixelandtag.cmp.ejb.subscription.FreeLoaderEJBI");
		messageEJB =  (MessageEJBI) context.lookup("cmp/MessageEJBImpl!com.pixelandtag.cmp.ejb.MessageEJBI");
		logger.debug("Successfully initialized EJB CMPResourceBeanRemote !!");
    }
	
	@Override
	public OutgoingSMS process(IncomingSMS incomingsms) {
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		logger.info("\n\n\n\tIS SUBSCRIPTION?? "+incomingsms.getIsSubscription());
		
		try {
			
			final RequestObject req = new RequestObject(incomingsms);
			final String KEYWORD = req.getKeyword().trim();
			final String MESSAGE = req.getMsg().trim();
			final Long serviceid = 	incomingsms.getServiceid();
			final String MSISDN = req.getMsisdn();
			
			int language_id = 1;
		
			
			if(freeloaderEJB.isInFreeloaderList(MSISDN)){
				Message message = messageEJB.getMessage(GenericServiceProcessor.PROMPT_USER_TO_ACCEPT_STANDARD_CHARGE, Long.valueOf(language_id), incomingsms.getOpco().getId());
				
				if(message!=null){
					outgoingsms.setSms(message.getMessage());
					outgoingsms.setBilling_status(BillingStatus.NO_BILLING_REQUIRED);
					outgoingsms.setPrice(BigDecimal.ZERO);
					return outgoingsms;
				}
			}
			
			Person person = datingBean.getPerson(incomingsms.getMsisdn(), incomingsms.getOpco());
			if(person==null)
				person = datingBean.register(incomingsms.getMsisdn(), incomingsms.getOpco());
			
			
			PersonDatingProfile profile = datingBean.getProfile(person);
			
			if(KEYWORD.equalsIgnoreCase("BUNDLES")){
				String submenustring = cmp_bean.getSubMenuString(KEYWORD,language_id);
				outgoingsms.setSms(submenustring+cmp_bean.getMessage(MAIN_MENU_ADVICE,language_id, person.getOpco().getId()));//get all the sub menus there.
				
			}else if(KEYWORD.equalsIgnoreCase("LOGIN")){
				
				String msg = "";
				
				if(person.getLoggedin().booleanValue()==false){
					person.setLoggedin(Boolean.TRUE);
					person = datingBean.saveOrUpdate(person);
					msg = "Welcome back "+profile.getUsername()+"! People missed you while you were away. You'll now be able to receive messages sent by other people. Dial *329# to find a friend to chat with, or reply with FIND";
				}else{
					msg = "You are already logged in. Reply with FIND to find a friend near your area to chat with, or Dial *329#";
				}
				
				List<String> services = new ArrayList<String>();
				services.add("BILLING_SERV5");
				services.add("BILLING_SERV15");
				services.add("BILLING_SERV30");
				
				datingBean.changeStatusIfSubscribed(MSISDN, services, SubscriptionStatus.confirmed);
				
				
				outgoingsms.setPrice(BigDecimal.ZERO);
				outgoingsms.setSms(msg);
				return outgoingsms;
				
			}else if(KEYWORD.equalsIgnoreCase("LOGOUT")){
				String msg = "";
				if(person.getLoggedin().booleanValue()==true){
					person.setLoggedin(Boolean.FALSE);
					person = datingBean.saveOrUpdate(person);
					msg = "You've successfully been logged out '"+profile.getUsername()+"'. You will not be able to receive messages from the chat service. To log back in, reply with LOGIN";
				}else{
					msg = "You are already logged out. You will not be able to receive messages from the chat service. To log back in, reply with LOGIN";
				}
				
				
				List<String> services = new ArrayList<String>();
				services.add("BILLING_SERV5");
				services.add("BILLING_SERV15");
				services.add("BILLING_SERV30");
				
				datingBean.changeStatusIfSubscribed(MSISDN, services, SubscriptionStatus.temporarily_suspended);
				
				
				outgoingsms.setPrice(BigDecimal.ZERO);
				outgoingsms.setSms(msg);
				return outgoingsms;
				
				
			}else if(KEYWORD.equalsIgnoreCase("FIND") || KEYWORD.equalsIgnoreCase("TAFUTA")) {
				
				if(person.getId()>0 && (profile==null || profile.getProfileComplete()==Boolean.FALSE)){//Success registering/registered but no profile
					
					outgoingsms = startProfileQuestions(incomingsms,person);
					
					return outgoingsms;
				}
				
				Gender pref_gender = profile.getPreferred_gender();
				
				PersonDatingProfile match =  datingBean.searchMatch(profile);
				
				
				if(match==null || match.getUsername()==null || match.getUsername().trim().isEmpty()){
					String msg = datingBean.getMessage(DatingMessages.COULD_NOT_FIND_MATCH_AT_THE_MOMENT, language_id, incomingsms.getOpco().getId());
					outgoingsms.setSms(msg.replaceAll(USERNAME_TAG, profile.getUsername()));
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
					String gender_pronoun = pref_gender.equals(Gender.FEMALE)? datingBean.getMessage(GENDER_PRONOUN_F, language_id, incomingsms.getOpco().getId()) : datingBean.getMessage(GENDER_PRONOUN_M, language_id, incomingsms.getOpco().getId());
					String gender_pronoun2 = pref_gender.equals(Gender.FEMALE)? datingBean.getMessage(GENDER_PRONOUN_INCHAT_F, language_id, incomingsms.getOpco().getId()) : datingBean.getMessage(GENDER_PRONOUN_INCHAT_M, language_id, incomingsms.getOpco().getId());
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
					String msg = datingBean.getMessage(DatingMessages.MATCH_FOUND, language_id, incomingsms.getOpco().getId());
					msg = msg.replaceAll(USERNAME_TAG, Matcher.quoteReplacement( profile.getUsername() ));
					msg = msg.replaceAll(GENDER_PRONOUN_TAG, Matcher.quoteReplacement( gender_pronoun ) );
					msg = msg.replaceAll(GENDER_PRONOUN_TAG2, Matcher.quoteReplacement( gender_pronoun2 ) );
					msg = msg.replaceAll(DEST_USERNAME_TAG, Matcher.quoteReplacement( match.getUsername() ) );
					msg = msg.replaceAll(PROFILE_TAG, Matcher.quoteReplacement( sb.toString() ) );
					outgoingsms.setSms(msg);
					
				}
				
				
				
			}else if(KEYWORD.equalsIgnoreCase("RENEW") || KEYWORD.equalsIgnoreCase("WEZESHA") || KEYWORD.equalsIgnoreCase("BILLING_SERV5")
					|| KEYWORD.equalsIgnoreCase("BILLING_SERV15")
					|| KEYWORD.equalsIgnoreCase("BILLING_SERV30")){
				
				SMSService smsservice0 = datingBean.getSMSService(KEYWORD, incomingsms.getOpco());
				List<String> services = new ArrayList<String>();
				services.add("BILLING_SERV5");
				services.add("BILLING_SERV15");
				services.add("BILLING_SERV30");
				
				boolean subvalid = datingBean.hasAnyActiveSubscription(MSISDN, services, incomingsms.getOpco());
				
				
				if(!subvalid ||  allow_multiple_plans ){
					
					try{
						
						try{
							subscriptionBean.unsubscribe(MSISDN, services, incomingsms.getOpco());
						}catch(Exception exp){
							logger.error(exp.getMessage(), exp);
						}
						
						datingBean.hasAnyActiveSubscription(MSISDN, services, incomingsms.getOpco());
						
						outgoingsms = datingBean.renewSubscription(incomingsms,smsservice0.getId(),AlterationMethod.self_via_sms, incomingsms.getOpco()); 
						
					}catch(DatingServiceException dse){
						logger.error(dse.getMessage(),dse);
						outgoingsms.setSms(null);//set nul so that we don't send it out..
						outgoingsms.setPrice(BigDecimal.ZERO);
					}
					
				}else{
					int lang = profile.getLanguage_id();
					if(lang<=0)
						lang = 1;
					outgoingsms.setPrice(BigDecimal.ZERO);
					String msg = datingBean.getMessage(DatingMessages.YOU_ALREADY_HAVE_VALID_SUBSCRIPTION_NOW_FIND_MATCH, lang, incomingsms.getOpco().getId());
					outgoingsms.setSms(msg);
				}
				
			}else if(KEYWORD.equalsIgnoreCase("DATE") || person!=null){
				
				if(outgoingsms.getIsSubscription()){//if it's subscription push, for this service we return no message.
					outgoingsms.setSms(null);
					outgoingsms.setPrice(BigDecimal.ZERO);
					return outgoingsms;
				}
				
				List<String> services = new ArrayList<String>();
				services.add("BILLING_SERV5");
				services.add("BILLING_SERV15");
				services.add("BILLING_SERV30");
				
				boolean subvalid = datingBean.hasAnyActiveSubscription(MSISDN, services, person.getOpco());
				
				//if(!subvalid) //TODO - have a config per opco to decide whether to auto renew or not.
				//	cmp_bean.mimicMO("BILLING_SERV5",MSISDN,incomingsms.getOpco());
				
				if(subvalid && profile!=null && profile.getProfileComplete()){//if subscription is valid && their profile is complete
					
					outgoingsms = processDating(incomingsms,person);
						
				}else if((profile==null || !profile.getProfileComplete() || !subvalid) ){//No profile or incomplete profile
					
					//Please top up to continue chatting with x ..
					int lang = 1;
					if(!subvalid && profile!=null && profile.getProfileComplete() ){//Profile is complete just need to renew subscription
						SMSService dating = datingBean.getSMSService("DATE",incomingsms.getOpco());
						lang = profile.getLanguage_id();
						PersonDatingProfile dest = datingBean.getperSonUsingChatName(MESSAGE);//find destination person
						String msg = "";
						if(dest!=null){//if they were chatting with someone.
							msg = datingBean.getMessage(DatingMessages.RENEW_CHAT_SUBSCRIPTION, lang, incomingsms.getOpco().getId());
							logger.info("\n\n\n\t1. msg::::>>> "+msg);
							msg = msg.replaceAll(DEST_USERNAME_TAG,  Matcher.quoteReplacement( dest.getUsername()) );
							
						}else{//else generic renew message
							msg  = datingBean.getMessage(DatingMessages.RENEW_SUBSCRIPTION, lang, incomingsms.getOpco().getId());
							logger.info("\n\n\n\t2. msg::::>>> "+msg);
						}
						logger.info("\n\n\n\tmsg::::>>> "+msg);
						msg = msg.replaceAll(USERNAME_TAG, Matcher.quoteReplacement( profile.getUsername()));
						msg = msg.replaceAll(SERVICENAME_TAG, Matcher.quoteReplacement( dating.getService_name()));
						outgoingsms.setPrice(BigDecimal.ZERO);//set price to zero so they receive msg
						outgoingsms.setSms(msg);//tell them to renew
						outgoingsms.setPriority(3);
						
					}else{
						outgoingsms = processDating(incomingsms,person);
					}
				}
			}else{
				String msg = datingBean.getMessage(UNKNOWN_KEYWORD_ADVICE, language_id, incomingsms.getOpco().getId());
				outgoingsms.setSms(msg.replaceAll(USERNAME_TAG, Matcher.quoteReplacement(KEYWORD)));
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return outgoingsms;
	}



	private OutgoingSMS processDating(IncomingSMS incomingsms, Person person) throws Exception { 
					  
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		if(person==null)
			person = datingBean.register(incomingsms.getMsisdn(),incomingsms.getOpco());
		
		
		PersonDatingProfile profile = datingBean.getProfile(person);
		
		if(profile!=null && profile.getProfileComplete()){
			outgoingsms = chat(incomingsms,profile,person);
		}
				
		if(person.getId()>0 && profile==null){//Success registering/registered but no profile
			
			outgoingsms = startProfileQuestions(incomingsms,person);
		
		}else{
			
			if(!profile.getProfileComplete()){
				
				outgoingsms = completeProfile(incomingsms,person,profile);
			
			}
		}
		
		return outgoingsms;
	}

	private OutgoingSMS startProfileQuestions(IncomingSMS incomingsms, Person person) throws DatingServiceException {

		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		try{
				final RequestObject req = new RequestObject(incomingsms);
				final String MSISDN = req.getMsisdn();
				int language_id = 1;
				
				String msg = null;
				try{
					msg = datingBean.getMessage(DatingMessages.DATING_SUCCESS_REGISTRATION, language_id, incomingsms.getOpco().getId());
				}catch(DatingServiceException dse){
					logger.error(dse.getMessage(), dse);
				}
				
				PersonDatingProfile profile = datingBean.getProfile(person);
				
				if(profile==null){
					profile = new PersonDatingProfile();
					profile.setPerson(person);
					profile.setUsername(MSISDN);
				}
				
				profile = datingBean.saveOrUpdate(profile);
				
				ProfileQuestion question = datingBean.getNextProfileQuestion(profile.getId());
				logger.debug("QUESTION::: "+question.getQuestion());
				outgoingsms.setSms(msg+ SPACE +question.getQuestion());
				
				QuestionLog ql = new QuestionLog();
				
				ql.setProfile_id_fk(profile.getId());
				ql.setQuestion_id_fk(question.getId());
				ql = datingBean.saveOrUpdate(ql);
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw new DatingServiceException("Sorry, problem occurred, please try again.",exp);
		}
		
		return outgoingsms;
	}

	private OutgoingSMS completeProfile(IncomingSMS incomingSMS, Person person,
			PersonDatingProfile profile) throws DatingServiceException {
		
		OutgoingSMS outgoingsms = incomingSMS.convertToOutgoing();
		
		try{
			
				final RequestObject req = new RequestObject(incomingSMS);
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
					
					if( (keywordIsNumber && agreed==2 ) || (KEYWORD!=null && (KEYWORD.trim().equalsIgnoreCase("B") || KEYWORD.trim().equalsIgnoreCase("Y") || KEYWORD.trim().equalsIgnoreCase("YES") || KEYWORD.trim().equalsIgnoreCase("YEP")
							|| KEYWORD.trim().equalsIgnoreCase("NDIO") || KEYWORD.trim().equalsIgnoreCase("NDIYO")  || KEYWORD.trim().equalsIgnoreCase("SAWA") || KEYWORD.trim().equalsIgnoreCase("OK") )) ){
						person.setAgreed_to_tnc(Boolean.TRUE);
						person = datingBean.saveOrUpdate(person);
					}else if((keywordIsNumber && agreed==1 ) || (KEYWORD!=null && (KEYWORD.trim().equalsIgnoreCase("A") || KEYWORD.trim().equalsIgnoreCase("N") || KEYWORD.trim().equalsIgnoreCase("NO")))){
						outgoingsms.setSms("Ok. Bye");
						return outgoingsms;
					}else{
						String msg = datingBean.getMessage(DatingMessages.MUST_AGREE_TO_TNC, language_id, person.getOpco().getId());
						outgoingsms.setSms(msg+SPACE+previousQuestion.getQuestion());
						return outgoingsms;
					}
						
				}
				
				if(attr.equals(ProfileAttribute.CHAT_USERNAME)){
					boolean isunique = datingBean.isUsernameUnique(KEYWORD);
					
					try{
						if(isunique)
							isunique = !(("0"+person.getMsisdn().substring(3)).equals(Integer.valueOf(KEYWORD).toString()));
					}catch(Exception exp){}
					
					if(isunique){
						profile.setUsername(KEYWORD);
					}else{
						String msg = "";
						if(KEYWORD.equalsIgnoreCase(incomingSMS.getShortcode())){
							msg = datingBean.getMessage(DatingMessages.REPLY_WITH_USERNAME, language_id,person.getOpco().getId());
						}else{
							msg = datingBean.getMessage(DatingMessages.USERNAME_NOT_UNIQUE_TRY_AGAIN, language_id,person.getOpco().getId());
						}
						outgoingsms.setSms(msg.replaceAll(USERNAME_TAG, KEYWORD));
						return outgoingsms;
					
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
							msg = datingBean.getMessage(DatingMessages.GENDER_NOT_UNDERSTOOD, language_id,person.getOpco().getId());
						}catch(DatingServiceException dse){
							logger.error(dse.getMessage(), dse);
						}
						outgoingsms.setSms(msg.replaceAll(USERNAME_TAG, KEYWORD));
						return outgoingsms;
					}
				}
				
				if(attr.equals(ProfileAttribute.AGE)){
					Date dob = new Date();
					BigDecimal age = null;
					try{
						age = new BigDecimal(KEYWORD);
					}catch(java.lang.NumberFormatException nfe){
						String msg = datingBean.getMessage(DatingMessages.AGE_NUMBER_INCORRECT, language_id,person.getOpco().getId());
						msg = msg.replaceAll(USERNAME_TAG, profile.getUsername());
						msg = msg.replaceAll(AGE_TAG, age.intValue()+"");
						outgoingsms.setSms(msg);
						return outgoingsms;
					}
					
					if(age.compareTo(new BigDecimal(100l))>=0){
						String msg = datingBean.getMessage(DatingMessages.UNREALISTIC_AGE, language_id,person.getOpco().getId());
						msg = msg.replaceAll(USERNAME_TAG,  profile.getUsername());
						msg = msg.replaceAll(AGE_TAG, age.intValue()+"");
						outgoingsms.setSms(msg);
						return outgoingsms;
					}
					
					if(age.compareTo(new BigDecimal(18l))<0){
						String msg = datingBean.getMessage(DatingMessages.SERVICE_FOR_18_AND_ABOVE, language_id,person.getOpco().getId());
						outgoingsms.setSms(msg.replaceAll(USERNAME_TAG,  profile.getUsername()));
						return outgoingsms;
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
					if(KEYWORD.contains("*") || KEYWORD.equalsIgnoreCase(incomingSMS.getShortcode()) || MESSAGE.equalsIgnoreCase(incomingSMS.getShortcode()) || location_is_only_number){
						String msg = datingBean.getMessage(DatingMessages.LOCATION_INVALID, language_id,person.getOpco().getId());
						outgoingsms.setSms(msg.replaceAll(USERNAME_TAG,  profile.getUsername()));
						return outgoingsms;
					}else{
						profile.setLocation(MESSAGE);
						
						profile.setProfileComplete(true);
						person.setActive(true);
						profile.setPerson(person);
						
						SMSService smsservice = datingBean.getSMSService("DATE",person.getOpco());
						
						subscriptionBean.renewSubscription(incomingSMS.getOpco(), MSISDN, smsservice, SubscriptionStatus.confirmed,AlterationMethod.self_via_sms);
					}
				}
				
				if(attr.equals(ProfileAttribute.PREFERRED_AGE)){
					BigDecimal age = null;
					try{
						age = new BigDecimal(KEYWORD);
					}catch(java.lang.NumberFormatException nfe){
						String msg = datingBean.getMessage(DatingMessages.AGE_NUMBER_INCORRECT, language_id,person.getOpco().getId());
						outgoingsms.setSms(msg.replaceAll(USERNAME_TAG, profile.getUsername()));
						return outgoingsms;
					}
					
					if(age.compareTo(new BigDecimal(18l))<0){
						String msg = datingBean.getMessage(DatingMessages.SERVICE_FOR_18_AND_ABOVE, language_id,person.getOpco().getId());
						outgoingsms.setSms(msg.replaceAll(USERNAME_TAG,  profile.getUsername()));
						return outgoingsms;
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
							msg = datingBean.getMessage(DatingMessages.GENDER_NOT_UNDERSTOOD, language_id,person.getOpco().getId());
						}catch(DatingServiceException dse){
							logger.error(dse.getMessage(), dse);
						}
						outgoingsms.setSms(msg.replaceAll(USERNAME_TAG, KEYWORD));
						return outgoingsms;
					}
					
				}
				
				profile = datingBean.saveOrUpdate(profile);
				
				
				ProfileQuestion question = datingBean.getNextProfileQuestion(profile.getId());
				
				if(question!=null){
					logger.debug("QUESTION::: "+question.getQuestion());
					outgoingsms.setSms(question.getQuestion().replaceAll(USERNAME_TAG, profile.getUsername()));
					
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
						
						String msg = datingBean.getMessage(DatingMessages.PROFILE_COMPLETE, language_id,person.getOpco().getId());
						outgoingsms.setSms(msg.replaceAll(USERNAME_TAG, profile.getUsername()));
						
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
					
						String gender_pronoun = pref_gender.equals(Gender.FEMALE)? datingBean.getMessage(GENDER_PRONOUN_F, language_id,person.getOpco().getId()) : datingBean.getMessage(GENDER_PRONOUN_M, language_id,person.getOpco().getId());
						String gender_pronoun2 = pref_gender.equals(Gender.FEMALE)? datingBean.getMessage(GENDER_PRONOUN_INCHAT_F, language_id,person.getOpco().getId()) : datingBean.getMessage(GENDER_PRONOUN_INCHAT_M, language_id,person.getOpco().getId());
						String msg = datingBean.getMessage(DatingMessages.MATCH_FOUND, language_id,person.getOpco().getId());
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
						outgoingsms.setSms(msg);
					}
					
				}
		
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw new DatingServiceException("Sorry, we couldn't process your request at this time. Kindly try again.",exp);
			
		}
		
		return outgoingsms;
	}

	private OutgoingSMS chat(IncomingSMS incomingSMS, PersonDatingProfile profile, Person person) throws DatingServiceException {
			
		OutgoingSMS outgoingsms = incomingSMS.convertToOutgoing();
		
		try{
			
			final RequestObject req = new RequestObject(incomingSMS);
			final String KEYWORD = req.getKeyword().trim();
			final String MESSAGE = req.getMsg().trim();
			
			//If profile is complete, we allow chat
			boolean directMsg  = false;
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
				incomingSMS.setPrice(BigDecimal.ZERO);
				
				Gender gender = profile.getGender();
				Gender dest_gender = destination_person.getGender();
				String pronoun = (gender == Gender.FEMALE) ? datingBean.getMessage(GENDER_PRONOUN_INCHAT_F, language_id,person.getOpco().getId()) : datingBean.getMessage(GENDER_PRONOUN_INCHAT_M, language_id,person.getOpco().getId());
				String dest_pronoun = (dest_gender == Gender.FEMALE) ? datingBean.getMessage(GENDER_PRONOUN_INCHAT_F, language_id,person.getOpco().getId()) : datingBean.getMessage(GENDER_PRONOUN_INCHAT_M, language_id,person.getOpco().getId());
				if(destination_person.getPerson().getLoggedin()==null || destination_person.getPerson().getLoggedin()==true){
					log.setOffline_msg(Boolean.FALSE);
					
					OutgoingSMS outgoingchatsms  = incomingSMS.convertToOutgoing();
					outgoingchatsms.setMsisdn(destination_person.getPerson().getMsisdn());
					String msg = "";
					OpcoSMSService opcosmsserv = opcosmsserviceejb.getOpcoSMSService(incomingSMS.getServiceid(), destination_person.getPerson().getOpco());
					
					if(!directMsg){//if it's not a direct message, then put advice
						msg = datingBean.getMessage(CHAT_MESSAGE_TEMPLATE, language_id,destination_person.getPerson().getOpco().getId());
						msg = msg.replaceAll(SOURCE_USERNAME_TAG, source_user);
						msg = msg.replaceAll(CHAT_MESSAGE_TAG, (allow_number_sharing ? MESSAGE.replaceAll(KEYWORD, "") : MESSAGE.replaceAll(KEYWORD, "").trim().replaceAll("\\d{5,10}", "*"))+NEW_LINE);
						msg = msg.replaceAll(PRONOUN_TAG, pronoun);
						msg = msg.replaceAll(MSG_PRICE_TAG, opcosmsserv.getPrice().toString());
						
						//msg = source_user+CHAT_USERNAME_SEPERATOR+(allow_number_sharing ? MESSAGE.replaceAll(KEYWORD, "") : MESSAGE.replaceAll(KEYWORD, "").trim().replaceAll("\\d{5,10}", "*")) ;
						//msg += NEW_LINE+" to continue chatting with "+pronoun+", reply starting with "+source_user+" SMS cost 0.0/-";
					}else{
				        msg = source_user+CHAT_USERNAME_SEPERATOR_DIRECT+(allow_number_sharing ? MESSAGE.replaceAll(KEYWORD, "") : MESSAGE.replaceAll(KEYWORD, "").trim().replaceAll("\\d{5,10}", "*")) ;
					}
					
					
					outgoingchatsms.setSms(msg);
					outgoingchatsms.setCmp_tx_id(generateNextTxId());//Is a totally new message
					outgoingchatsms.setOpco_tx_id(generateNextTxId());//Is a totally new message
					outgoingchatsms.setPriority(0);//highest priority possible
					outgoingchatsms.setPrice(BigDecimal.ZERO);
					OpcoSenderReceiverProfile opcotrxprofile = opcosenderprofileEJB.getActiveProfileForOpco(destination_person.getPerson().getOpco().getId());
					outgoingchatsms.setOpcosenderprofile(opcotrxprofile);
					String shortcode = opcosmsserv.getMoprocessor().getShortcode();//opcosmsserviceejb.getShortcodeByServiceIdAndOpcoId(incomingSMS.getServiceid(), destination_person.getPerson().getOpco());
					outgoingchatsms.setShortcode(shortcode);
					outgoingchatsms.setTimestamp(new Date());
					
					sendMT(outgoingchatsms);
					String tailmsg = "";
					if(!person.getLoggedin()){
						String offlineNotifier = datingBean.getMessage(OFFLINE_NOTIFIER, language_id,person.getOpco().getId());
						offlineNotifier = offlineNotifier.replaceAll(Matcher.quoteReplacement(DEST_USERNAME_TAG), Matcher.quoteReplacement(destination_person.getUsername()));
						tailmsg = ". "+offlineNotifier;
					}
					
					String msgsentto = datingBean.getMessage(MESSAGE_SENT_NOTIFICATION, language_id,person.getOpco().getId());
					msgsentto = msgsentto.replaceAll(Matcher.quoteReplacement(DEST_USERNAME_TAG), Matcher.quoteReplacement(destination_person.getUsername()));
					outgoingsms.setSms(msgsentto+tailmsg);
					outgoingsms.setTimestamp(new Date());
					
				}else{
					log.setOffline_msg(Boolean.TRUE);
					incomingSMS.setPrice(BigDecimal.ZERO);
					String pronoun2 = dest_pronoun.equalsIgnoreCase("her") ? "she" : "he";
					outgoingsms.setSms("Sorry "+profile.getUsername()+", '"+destination_person.getUsername()+"' is currently offline. You can chat with "+dest_pronoun+" when "+pronoun2+" gets back online.");
				}
				
				log = datingBean.saveOrUpdate(log); 
				return outgoingsms;
			}else if(destination_person!=null && !destination_person.getPerson().getActive()){
				Gender gender  = destination_person.getGender();
				String pronoun = gender.equals(Gender.FEMALE) ? datingBean.getMessage(GENDER_PRONOUN_INCHAT_F, language_id,person.getOpco().getId()) : datingBean.getMessage(GENDER_PRONOUN_INCHAT_M, language_id,person.getOpco().getId());
				String msg = "Sorry, \""+destination_person.getUsername()+"\" has unsubcribed from the chat service and you cannot chat with "+pronoun+". To find a different person to chat with, reply with \"FIND\" and the system will find a match for you based on your profile.";
				outgoingsms.setSms(msg);
				return outgoingsms;
			}else{//destination person not found.. Check in their friends list. Ask them whom they want to chat to..
				incomingSMS.setPrice(BigDecimal.ZERO);
				destination_person = datingBean.getProfileOfLastPersonIsentMessageTo(person,0L,TimeUnit.MINUTE);//last 1 year
				String msg = "";
				if(destination_person!=null){
					Gender gender  = destination_person.getGender();
					String pronoun = gender.equals(Gender.FEMALE) ? datingBean.getMessage(GENDER_PRONOUN_INCHAT_F, language_id,person.getOpco().getId()) : datingBean.getMessage(GENDER_PRONOUN_INCHAT_M, language_id,person.getOpco().getId());
					msg = "Sorry, no user with the username \""+KEYWORD+"\". "
								+ "You last had a chat with "+destination_person.getUsername()+", if you want to continue chatting with "+pronoun+", send your message starting"
								+ " with \""+destination_person.getUsername()+"\", or to find another person to chat with, reply with \"FIND\" and we shall hook you up!";
				}else{
					msg = "Sorry, no user with the username \""+KEYWORD+"\". ";
				}
				outgoingsms.setSms(msg);//get their friendslist/match
				return outgoingsms;
				
			}
		
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
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
