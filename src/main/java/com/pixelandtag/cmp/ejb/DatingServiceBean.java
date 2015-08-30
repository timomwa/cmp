package com.pixelandtag.cmp.ejb;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.MOProcessor;
import com.pixelandtag.cmp.entities.Message;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.TimeUnit;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.subscription.Subscription;
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
import com.pixelandtag.model.MessageEmail;
import com.pixelandtag.serviceprocessors.sms.DatingMessages;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.sms.producerthreads.Operation;
import com.pixelandtag.smsmenu.MenuItem;
import com.pixelandtag.smsmenu.Session;
import com.pixelandtag.subscription.dto.MediumType;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.util.StopWatch;
import com.pixelandtag.web.beans.RequestObject;

@Stateless
@Remote
public class DatingServiceBean  extends BaseEntityBean implements DatingServiceI {
	

	public Logger logger = Logger.getLogger(DatingServiceBean.class);
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@EJB
	private CMPResourceBeanRemote cmp_ejb;
	
	@EJB
	private LocationBeanI location_ejb;
	
	@EJB
	private TimezoneConverterI timezone_ejb;
	
	
	@EJB
	private SubscriptionBeanI subscriptionBean;
	
	@EJB
	private MessageEJBI messageEJB;
	
	
	@EJB
	TimezoneConverterI timezoneEJB;
	
	private StopWatch watch = new StopWatch();
	
	public DatingServiceBean() throws KeyManagementException,
			UnrecoverableKeyException, NoSuchAlgorithmException,
			KeyStoreException {
		super();
	}
	
	public String processDating(RequestObject ro) throws Exception{
		
		String resp = "";
		
		final String MSISDN = ro.getMsisdn();
		
		Person person = getPerson(MSISDN,ro.getOpco());
		
		if(person==null)
			person = register(MSISDN,ro.getOpco());
		
		PersonDatingProfile profile = getProfile(person);
		
		if( (profile!=null && profile.getProfileComplete()) //If profile is already created and valid, 
				&& //and no keyword or message passed along, then we find a match for them
			 (ro.getKeyword()==null || ro.getMsg()==null || ro.getMsg().isEmpty() || ro.getKeyword().isEmpty() 
			 || (ro.getCode()!=null && ro.getMsg().equals(ro.getCode()))  )){
			
			String msg = findMatch(ro,person,profile);
			
			
			SMSService smsserv = getSMSService("DATE");
			MOProcessor proc = smsserv.getMoprocessor();
			
			if(smsserv!=null  && proc!=null){
				OutgoingSMS outgoingsms = new OutgoingSMS();
				outgoingsms.setMsisdn(MSISDN);
				outgoingsms.setPrice(BigDecimal.ZERO);
				outgoingsms.setBilling_status(BillingStatus.NO_BILLING_REQUIRED); 
				outgoingsms.setSms(msg);
				outgoingsms.setServiceid(smsserv.getId());
				outgoingsms.setMoprocessor(proc);
				outgoingsms.setShortcode(proc.getShortcode());
				
				if(!ro.getTransactionID().equals("1"))
					outgoingsms.setCmp_tx_id(ro.getTransactionID());
				else
					outgoingsms.setCmp_tx_id(generateNextTxId());
				
				outgoingsms.setSplit(false);
				outgoingsms.setBilling_status(BillingStatus.NO_BILLING_REQUIRED);
				outgoingsms.setIsSubscription(false);
				sendMT(outgoingsms);
			}
			
		
		}else if(person.getId()>0 && profile==null){//Success registering/registered but no profile
			
			resp = startProfileQuestions(ro,person);
		
		}else{
			
			if(!profile.getProfileComplete()){
				
				resp = completeProfile(ro,person,profile);
			
			}else{
				//force process to go to another method
				resp = null;
			}
		}
		
		return resp;
	}
	
	
	
	public String findMatch(RequestObject ro,Person person, PersonDatingProfile profile) {

		String resp = "Request to find match received.";
		
		Gender pref_gender = profile.getPreferred_gender();
		BigDecimal pref_age = profile.getPreferred_age();
		String location = profile.getLocation();
		int language_id = profile.getLanguage_id();
		
		try{
			
			PersonDatingProfile match = findMatch(profile);
			if(match==null)
				match  = findMatch(pref_gender,pref_age, location,person.getId());
			if(match==null)
				 match = findMatch(pref_gender,pref_age,person.getId());
			if(match==null)
				 match = findMatch(pref_gender,person.getId());
			
			if(match==null || match.getUsername()==null || match.getUsername().trim().isEmpty()){
				resp = getMessage(DatingMessages.COULD_NOT_FIND_MATCH_AT_THE_MOMENT, language_id);
				resp = resp.replaceAll(GenericServiceProcessor.USERNAME_TAG, profile.getUsername());
			}else{
				try{
					SystemMatchLog sysmatchlog = new SystemMatchLog();
					sysmatchlog.setPerson_a_id(person.getId());
					sysmatchlog.setPerson_b_id(match.getPerson().getId());
					sysmatchlog.setPerson_a_notified(true);
					sysmatchlog = saveOrUpdate(sysmatchlog);
				}catch(Exception exp){
					logger.warn("\n\n\n\t\t"+exp.getMessage()+"\n\n");
				}
				String gender_pronoun = pref_gender.equals(Gender.FEMALE)? getMessage(GenericServiceProcessor.GENDER_PRONOUN_F, language_id) : getMessage(GenericServiceProcessor.GENDER_PRONOUN_M, language_id);
				String gender_pronoun2 = pref_gender.equals(Gender.FEMALE)? getMessage(GenericServiceProcessor.GENDER_PRONOUN_INCHAT_F, language_id) : getMessage(GenericServiceProcessor.GENDER_PRONOUN_INCHAT_M, language_id);
				StringBuffer sb = new StringBuffer();
				BigInteger age = calculateAgeFromDob(match.getDob());  
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
				String msg = getMessage(DatingMessages.MATCH_FOUND, language_id);
				msg = msg.replaceAll(GenericServiceProcessor.USERNAME_TAG, profile.getUsername());
				msg = msg.replaceAll(GenericServiceProcessor.GENDER_PRONOUN_TAG, gender_pronoun);
				msg = msg.replaceAll(GenericServiceProcessor.GENDER_PRONOUN_TAG2, gender_pronoun2);
				msg = msg.replaceAll(GenericServiceProcessor.DEST_USERNAME_TAG, match.getUsername());
				msg = msg.replaceAll(GenericServiceProcessor.PROFILE_TAG, sb.toString());
				resp = msg;
			}
		
		}catch(Exception exp){
			resp = "Sorry, we couldn't process request at the moment. Please try again.";
			logger.error(exp.getMessage(),exp);
		}
		
		return resp;
		
	}

	public String completeProfile(RequestObject req, Person person,
			PersonDatingProfile profile) throws DatingServiceException {
		
		String resp = "Request Received.";
		
		try{
			
				final String KEYWORD = req.getKeyword().trim();
				final String MESSAGE = req.getMsg().trim();
				final String MSISDN = req.getMsisdn();
				
				int language_id = 1;
				
				
				ProfileQuestion previousQuestion = getPreviousQuestion(profile.getId());
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
						person.setAgreed_to_tnc(true);
						person = saveOrUpdate(person);
					}else if((keywordIsNumber && agreed==1 ) || (KEYWORD!=null && (KEYWORD.trim().equalsIgnoreCase("A") || KEYWORD.trim().equalsIgnoreCase("N") || KEYWORD.trim().equalsIgnoreCase("NO")))){
						resp = "Ok. Bye";
						return resp;
					}else{
						resp = getMessage(DatingMessages.MUST_AGREE_TO_TNC, language_id) + GenericServiceProcessor.SPACE +"Proceed?\n1. No\n2. Yes" ;
						return resp;
					}
						
				}
				
				
				if(attr.equals(ProfileAttribute.CHAT_USERNAME)){
					boolean isunique = isUsernameUnique(KEYWORD);
					
					try{
						if(isunique)
							isunique = !(("0"+person.getMsisdn().substring(3)).equals(Integer.valueOf(KEYWORD).toString()));
					}catch(Exception exp){}
					
					if(isunique){
						profile.setUsername(KEYWORD);
					}else{
						if(KEYWORD.equalsIgnoreCase(req.getCode())){
							resp = getMessage(DatingMessages.REPLY_WITH_USERNAME, language_id);
						}else{
							resp = getMessage(DatingMessages.USERNAME_NOT_UNIQUE_TRY_AGAIN, language_id);
						}
						
						resp = resp.replaceAll(GenericServiceProcessor.USERNAME_TAG, KEYWORD);
						return resp;
					
					}
					
				}
				
				if(attr.equals(ProfileAttribute.GENDER)){
					if(MESSAGE.equalsIgnoreCase("2") || MESSAGE.equalsIgnoreCase("M") ||  MESSAGE.equalsIgnoreCase("MALE") ||  MESSAGE.equalsIgnoreCase("MAN") ||  MESSAGE.equalsIgnoreCase("BOY") ||  MESSAGE.equalsIgnoreCase("MUME") ||  MESSAGE.equalsIgnoreCase("MWANAMME")  ||  MESSAGE.equalsIgnoreCase("MWANAUME")){ 
						profile.setGender(Gender.MALE);
						profile.setPreferred_gender(Gender.FEMALE);
					}else if(MESSAGE.equalsIgnoreCase("1") || MESSAGE.equalsIgnoreCase("F") ||  MESSAGE.equalsIgnoreCase("FEMALE") ||  MESSAGE.equalsIgnoreCase("LADY") ||  MESSAGE.equalsIgnoreCase("GIRL") ||  MESSAGE.equalsIgnoreCase("MKE") ||  MESSAGE.equalsIgnoreCase("MWANAMKE")  ||  MESSAGE.equalsIgnoreCase("MWANAMUKE")){ 
						profile.setGender(Gender.FEMALE);
						profile.setPreferred_gender(Gender.MALE);
					}else{
						
						try{
							resp = getMessage(DatingMessages.GENDER_NOT_UNDERSTOOD, language_id);
						}catch(DatingServiceException dse){
							logger.error(dse.getMessage(), dse);
						}
						resp = resp.replaceAll(GenericServiceProcessor.USERNAME_TAG, KEYWORD);
						return resp;
					}
				}
				
				if(attr.equals(ProfileAttribute.AGE)){
					Date dob = new Date();
					BigDecimal age = null;
					try{
						age = new BigDecimal(KEYWORD);
					}catch(java.lang.NumberFormatException nfe){
						resp = getMessage(DatingMessages.AGE_NUMBER_INCORRECT, language_id);
						resp = resp.replaceAll(GenericServiceProcessor.USERNAME_TAG, profile.getUsername());
						return resp;
					}
					if(age.compareTo(new BigDecimal(100l))>=0){
						resp = getMessage(DatingMessages.UNREALISTIC_AGE, language_id);
						resp = resp.replaceAll(GenericServiceProcessor.USERNAME_TAG,  profile.getUsername());
						resp = resp.replaceAll(GenericServiceProcessor.AGE_TAG,  age.intValue()+"");
						return resp;
					}
					if(age.compareTo(new BigDecimal(18l))<0){
						resp = getMessage(DatingMessages.SERVICE_FOR_18_AND_ABOVE, language_id);
						resp = resp.replaceAll(GenericServiceProcessor.USERNAME_TAG,  age.intValue()+"");
						return resp;
					}
					
					dob = calculateDobFromAge(age);
					profile.setDob( dob );
					profile.setPreferred_age(BigDecimal.valueOf(18L));
				}
				
				if(attr.equals(ProfileAttribute.LOCATION)){//The last one as per new version
					boolean location_is_only_number = false;
					try{
						new BigDecimal(MESSAGE);
						location_is_only_number = true;
					}catch(java.lang.NumberFormatException nfe){
					}
					if(KEYWORD.contains("*") || KEYWORD.equalsIgnoreCase(req.getCode()) || MESSAGE.equalsIgnoreCase(req.getCode()) || location_is_only_number){
						resp = getMessage(DatingMessages.LOCATION_INVALID, language_id);
						resp = resp.replaceAll(GenericServiceProcessor.USERNAME_TAG,  profile.getUsername());
						return resp;
					}
					
					profile.setLocation(MESSAGE);
					if((req.getCellid()!=null && !req.getCellid().isEmpty()) && (req.getLac()!=null && !req.getLac().isEmpty())){
						try{
							location_ejb.findOrCreateLocation(Long.valueOf(req.getCellid()), Long.valueOf(req.getLac()), MESSAGE, profile);
						}catch(Exception exp){
							logger.error(exp.getMessage(), exp);
						}
					}
					profile.setProfileComplete(true);
					person.setActive(true);
					profile.setPerson(person);
				}
				
				if(attr.equals(ProfileAttribute.PREFERRED_AGE)){
					BigDecimal age = null;
					try{
						age = new BigDecimal(KEYWORD);
					}catch(java.lang.NumberFormatException nfe){
						resp = getMessage(DatingMessages.AGE_NUMBER_INCORRECT, language_id);
						resp = resp.replaceAll(GenericServiceProcessor.USERNAME_TAG, profile.getUsername());
						return resp;
					}
					
					if(age.compareTo(new BigDecimal(18l))<0){
						resp = getMessage(DatingMessages.SERVICE_FOR_18_AND_ABOVE, language_id);
						resp = resp.replaceAll(GenericServiceProcessor.USERNAME_TAG,  profile.getUsername());
						return resp;
					}
					profile.setPreferred_age(age);
				}
				
				if(attr.equals(ProfileAttribute.PREFERRED_GENDER)){
					if(MESSAGE.equalsIgnoreCase("2") || MESSAGE.equalsIgnoreCase("M") ||  MESSAGE.equalsIgnoreCase("MALE") ||  MESSAGE.equalsIgnoreCase("MAN") ||  MESSAGE.equalsIgnoreCase("BOY") ||  MESSAGE.equalsIgnoreCase("MUME") ||  MESSAGE.equalsIgnoreCase("MWANAMME")  ||  MESSAGE.equalsIgnoreCase("MWANAUME")) {
						profile.setPreferred_gender(Gender.MALE);
					}else if(MESSAGE.equalsIgnoreCase("1") || MESSAGE.equalsIgnoreCase("F") ||  MESSAGE.equalsIgnoreCase("FEMALE") ||  MESSAGE.equalsIgnoreCase("LADY") ||  MESSAGE.equalsIgnoreCase("GIRL") ||  MESSAGE.equalsIgnoreCase("MKE") ||  MESSAGE.equalsIgnoreCase("MWANAMKE")  ||  MESSAGE.equalsIgnoreCase("MWANAMUKE")){ 
						profile.setPreferred_gender(Gender.FEMALE);
					}else{
						try{
							resp = getMessage(DatingMessages.GENDER_NOT_UNDERSTOOD, language_id);
						}catch(DatingServiceException dse){
							logger.error(dse.getMessage(), dse);
						}
						resp = resp.replaceAll(GenericServiceProcessor.USERNAME_TAG, KEYWORD);
						return resp;
					}
					
				}
				
				profile = saveOrUpdate(profile);
				
				
				ProfileQuestion question = getNextProfileQuestion(profile.getId());
				
				if(question!=null){
					logger.debug("QUESTION::: "+question.getQuestion());
					resp = question.getQuestion().replaceAll(GenericServiceProcessor.USERNAME_TAG, profile.getUsername());
					
					QuestionLog ql = new QuestionLog();
					
					ql.setProfile_id_fk(profile.getId());
					ql.setQuestion_id_fk(question.getId());
					ql = saveOrUpdate(ql);
				}else{
					Gender pref_gender = profile.getPreferred_gender();
					BigDecimal pref_age = profile.getPreferred_age();
					String location = profile.getLocation();
					
					PersonDatingProfile match = null;
					if(match==null){
						try{
							match = findMatch(profile);//try find by their location
						}catch(DatingServiceException exp){
							logger.warn(exp.getMessage(),exp);
						}
					}
					
					if(match==null)
						match = findMatch(pref_gender,pref_age, location,person.getId());
					if(match==null)
						 match = findMatch(pref_gender,pref_age,person.getId());
					if(match==null)
						 match = findMatch(pref_gender,person.getId());
					
					
						try{
							SystemMatchLog sysmatchlog = new SystemMatchLog();
							sysmatchlog.setPerson_a_id(person.getId());
							sysmatchlog.setPerson_b_id(match.getPerson().getId());
							sysmatchlog.setPerson_a_notified(true);
							sysmatchlog = saveOrUpdate(sysmatchlog);
						}catch(Exception exp){
							logger.warn("\n\n\n\t\t"+exp.getMessage()+"\n\n");
						}
					    
						
						String gender_pronoun = pref_gender.equals(Gender.FEMALE)? getMessage(GenericServiceProcessor.GENDER_PRONOUN_F, language_id) : getMessage(GenericServiceProcessor.GENDER_PRONOUN_M, language_id);
						String gender_pronoun2 = pref_gender.equals(Gender.FEMALE)? getMessage(GenericServiceProcessor.GENDER_PRONOUN_INCHAT_F, language_id) : getMessage(GenericServiceProcessor.GENDER_PRONOUN_INCHAT_M, language_id);
						String msg = getMessage(DatingMessages.MATCH_FOUND, language_id);
						logger.info("\n\n\n\t\t msg:::"+resp);
						logger.info("\n\n\n\t\t profile:::"+profile);
						StringBuffer sb = new StringBuffer();
						BigInteger age = calculateAgeFromDob(match.getDob()); 
						sb.append("\n").append("Age: ").append(age).append("\n");
						sb.append("Location : ").append(match.getLocation()).append("\n");
						sb.append("Gender : ").append(match.getGender()).append("\n");
						msg = msg.replaceAll(GenericServiceProcessor.USERNAME_TAG, profile.getUsername());
						msg = msg.replaceAll(GenericServiceProcessor.GENDER_PRONOUN_TAG, gender_pronoun);
						msg = msg.replaceAll(GenericServiceProcessor.GENDER_PRONOUN_TAG2, gender_pronoun2);
						msg = msg.replaceAll(GenericServiceProcessor.DEST_USERNAME_TAG, match.getUsername());
						msg = msg.replaceAll(GenericServiceProcessor.PROFILE_TAG, sb.toString());
						
						SMSService smsserv = getSMSService("DATE");
						MOProcessor proc = smsserv.getMoprocessor();
						
						if(smsserv!=null && proc!=null){
							OutgoingSMS outgoingsms = new OutgoingSMS();
							outgoingsms.setMsisdn(MSISDN);
							outgoingsms.setPrice(BigDecimal.ZERO);
							outgoingsms.setBilling_status(BillingStatus.NO_BILLING_REQUIRED);
							outgoingsms.setSms(msg);
							outgoingsms.setServiceid(smsserv.getId());
							outgoingsms.setMoprocessor(proc);
							outgoingsms.setShortcode(proc.getShortcode());
							outgoingsms.setCmp_tx_id(generateNextTxId());
							
							outgoingsms.setSplit(false);
							outgoingsms.setBilling_status(BillingStatus.NO_BILLING_REQUIRED);
							outgoingsms.setIsSubscription(false);
							sendMT(outgoingsms);
							//resp = getMessage(DatingMessages.PROFILE_COMPLETE, language_id);
							//resp = resp.replaceAll(GenericServiceProcessor.USERNAME_TAG, profile.getUsername());
							
							MenuItem topMenu = cmp_ejb.getTopMenu("BUNDLES");
							MenuItem item = null;
							if(topMenu!=null){
								item = cmp_ejb.getMenuByParentLevelId(language_id,topMenu.getId(),topMenu.getMenu_id());
							}
							resp =  item!=null ? item.enumerate() : null;
							
							if(resp!=null){
								Session sess = cmp_ejb.getSession(MSISDN);
								if(sess==null){
									sess = new Session();
									sess.setLanguage_id(language_id);
									sess.setMenu_item(item);
									sess.setMsisdn(MSISDN);
									sess.setSmsmenu_level_id_fk(item.getId());
								}
								if(req.getMediumType()==MediumType.ussd)
									cmp_ejb.updateSession(language_id,MSISDN, item.getParent_level_id(),null,item.getMenu_id(),req.getSessionid());
								else
									cmp_ejb.updateSession(language_id,MSISDN, item.getParent_level_id());//update session to upper menu.
								resp = "Select your chat bundles.\n"+ resp+getMessage(GenericServiceProcessor.MAIN_MENU_ADVICE,language_id);
							}
						}else{
							resp = "Request received but we couldn't process your request. Do try again later.";
						}
						
										
				}
		
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			//throw new DatingServiceException("Something went Wrong. Kindly try again.",exp);
			resp = "Could not process request. Kindly try again later";
			
		}
		
		return resp;
	}
	
	

	private String startProfileQuestions(RequestObject req, Person person) throws DatingServiceException {

		String resp = "Request received.";
		
		try{
				final String MSISDN = req.getMsisdn();
				int language_id = 1;
				
				
				try{
					resp = getMessage(DatingMessages.DATING_SUCCESS_REGISTRATION, language_id);
				}catch(DatingServiceException dse){
					logger.error(dse.getMessage(), dse);
				}
				
				PersonDatingProfile profile = new PersonDatingProfile();
				profile.setPerson(person);
				profile.setUsername(MSISDN);
				
				profile = saveOrUpdate(profile);
				
				ProfileQuestion question = getNextProfileQuestion(profile.getId());
				logger.debug("QUESTION::: "+question.getQuestion());
				resp =  GenericServiceProcessor.SPACE +question.getQuestion();
				
				QuestionLog ql = new QuestionLog();
				
				ql.setProfile_id_fk(profile.getId());
				ql.setQuestion_id_fk(question.getId());
				ql = saveOrUpdate(ql);
		}catch(Exception exp){
			throw new DatingServiceException("Sorry, problem occurred, please try again.",exp);
		}
		
		return resp;
	}
	

		
	@Override
	public String getMessage(String key, int language_id) throws DatingServiceException{
		
		Message message = messageEJB.getMessage(key, Long.valueOf(language_id));
		
		return message.getMessage();
		
	}
	
	@SuppressWarnings("unchecked")
	public PersonDatingProfile getProfile(String msisdn) throws DatingServiceException{
		
		PersonDatingProfile profile = null;
		
		try{
			Query qry = em.createQuery("from PersonDatingProfile prf WHERE prf.person.msisdn=:msisdn");
			qry.setParameter("msisdn", msisdn);
			List<PersonDatingProfile> lst = qry.getResultList();
			
			if(lst.size()>0)
				profile = lst.get(0);
			
		}catch(javax.persistence.NoResultException ex){
			logger.warn(ex.getMessage() + " no profile for person "+msisdn);
		}catch (Exception e) {

			logger.error(e.getMessage(), e);

			throw new DatingServiceException(e.getMessage(),e);

		}finally{
		}
		
		return profile;
		
	}
	
	@SuppressWarnings("unchecked")
	public PersonDatingProfile getProfile(Person person) throws DatingServiceException{
		
		PersonDatingProfile profile = null;
		
		try{
			Query qry = em.createQuery("from PersonDatingProfile prf WHERE prf.person=:person");
			qry.setParameter("person", person);
			List<PersonDatingProfile> lst = qry.getResultList();
			
			if(lst.size()>0)
				profile = lst.get(0);
			
		}catch(javax.persistence.NoResultException ex){
			logger.warn(ex.getMessage() + " no profile for person "+person);
		}catch (Exception e) {

			logger.error(e.getMessage(), e);

			throw new DatingServiceException(e.getMessage(),e);

		}finally{
		}
		
		return profile;
		
	}
	@Override
	public String getMessage(DatingMessages datingMsg, int language_id) throws DatingServiceException{
		if(language_id<=0)
			language_id = 1;
		return getMessage(datingMsg.toString(),language_id);
	}
	
	@Override
	public Person register(String msisdn, OperatorCountry opco) throws Exception {
		Person person = new Person();
		person.setMsisdn(msisdn);
		person.setActive(false);
		person.setOpco(opco);
		return saveOrUpdate(person);
	}

	

	@SuppressWarnings("unchecked")
	public PersonDatingProfile findMatch(Gender pref_gender, Long curPersonId) throws DatingServiceException{
		PersonDatingProfile person = null;
		try{
			List<Long> alreadyMatched = getAlreadyMatched(curPersonId);
			alreadyMatched.add(curPersonId);
			Query qry = em.createQuery("from PersonDatingProfile p WHERE p.username <> p.person.msisdn AND p.person.active=:active AND p.gender=:gender AND p.person.id NOT IN  (SELECT DISTINCT person_b_id from SystemMatchLog sml WHERE sml.person_a_id = :person_a_id) order by p.creationDate desc");//:alreadyMatched) ");//AND p.dob>=:dob
			qry.setParameter("active", new Boolean(true));
			qry.setParameter("gender", pref_gender);
			qry.setParameter("person_a_id", curPersonId);
			//qry.setParameter("alreadyMatched", alreadyMatched);
			List<PersonDatingProfile> ps = qry.getResultList();
			if(ps.size()>0)
				person = ps.get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}
		return person;
	}

	
	
	@SuppressWarnings("unchecked")
	public PersonDatingProfile findMatch(Gender pref_gender,BigDecimal pref_age,Long curPersonId) throws DatingServiceException{
		PersonDatingProfile person = null;
		try{
			List<Long> alreadyMatched = getAlreadyMatched(curPersonId);
			alreadyMatched.add(curPersonId);
			Date dob = calculateDobFromAge(pref_age);
			Query qry = em.createQuery("from PersonDatingProfile p WHERE p.username <> p.person.msisdn AND p.person.active=:active AND p.gender=:gender AND p.dob<=:dob AND p.person.id NOT IN (SELECT DISTINCT person_b_id from SystemMatchLog sml WHERE sml.person_a_id = :person_a_id) order by p.creationDate desc");//:alreadyMatched) ");//AND p.dob>=:dob
			qry.setParameter("gender", pref_gender);
			qry.setParameter("active", new Boolean(true));
			qry.setParameter("dob", dob);
			qry.setParameter("person_a_id", curPersonId);
			//qry.setParameter("alreadyMatched", alreadyMatched);
			List<PersonDatingProfile> ps = qry.getResultList();
			if(ps.size()>0)
				person = ps.get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}
		return person;
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.DatingServiceI#findMatch(com.pixelandtag.dating.entities.PersonDatingProfile)
	 */
	public PersonDatingProfile findMatch(PersonDatingProfile profile) throws DatingServiceException{
	
		PersonDatingProfile persondatingProfile = null;
		try {
			
			ProfileLocation profileLocation  = location_ejb.findProfileLocation(profile);
			
			if(profileLocation!=null && profileLocation.getProfile()!=null){
				
				CellIdRanges cellidRanges = null;//location_ejb.getCellIdRangesByLocationId(profileLocation.getLocation().getLocation_id());
				
				Long min_cell_id = cellidRanges!=null?cellidRanges.getMin_cell_id() : 0;
				Long max_cell_id = cellidRanges!=null?cellidRanges.getMax_cell_id() : 0;
			
				//Date dob = calculateDobFromAge(profile.getPreferred_age());
				
				Query qry = em.createQuery("   FROM "
											+ "	ProfileLocation pl "
											+ "WHERE "
											+ "   (   (pl.profile.username <> pl.profile.person.msisdn) "
											+ "	   AND  "
											+ "      pl.profile.person.active=:active "
											+ "    AND "
											//+ "      pl.profile.dob<=:dob "
											//+ "    AND "
											+ "      pl.profile.gender=:prefGender "
											+ "    AND "
											+ "        (  pl.location.location_id=:location_id "
										//	+ "				OR "
										//	+ "				   pl.location.location_id between :location_id_lower and :location_id_upper  "
											+ "				OR  "
											+ "					pl.location.cellid=:cellid"
											+(max_cell_id.longValue()>0 ? (  
											 "				OR  "
											+ "					pl.location.cellid between :min_cell_id and :max_cell_id "
													) : "")
											+ "				OR "
											+ "				   lower(pl.location.locationName) like lower(:locationName)  "
											+ "		   ) "
											+ "   )  AND "
											+ "    (   pl.profile.person.id "
											+ "				NOT IN "
											+ "					(SELECT "
											+ "						DISTINCT person_b_id "
											+ "					 FROM "
											+ "						SystemMatchLog sml "
											+ "					 WHERE "
											+ "                     sml.person_a_id = :person_a_id"
											+ "                 )"
											+ "   )"
											+ " order by "
											+ "    pl.timeStamp desc, pl.profile.dob desc");
				qry.setFirstResult(0);
				qry.setMaxResults(1);
				
				qry.setParameter("cellid", profileLocation.getLocation().getCellid());
				qry.setParameter("location_id", profileLocation.getLocation().getLocation_id());
				//qry.setParameter("location_id_lower", (profileLocation.getLocation().getLocation_id()-1));
				//qry.setParameter("location_id_upper", (profileLocation.getLocation().getLocation_id()+1));
				if(max_cell_id.longValue()>0){
					qry.setParameter("min_cell_id", min_cell_id);
					qry.setParameter("max_cell_id", max_cell_id);
				} 
				qry.setParameter("locationName", "%"+profileLocation.getLocation().getLocationName()+"%");
				qry.setParameter("prefGender", profile.getPreferred_gender());
				qry.setParameter("person_a_id",profile.getPerson().getId());
				//qry.setParameter("dob", dob);
				qry.setParameter("active", new Boolean(true));
				ProfileLocation pl = (ProfileLocation) qry.getSingleResult();
				
				persondatingProfile = pl.getProfile();
				logger.info("REAL_LOCATION_SEARCH SUCCESS YAY!! we found tweo people in "+profileLocation.getLocation().getLocationName());
			
			}
		
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new DatingServiceException("Problem finding match.",e);
		}
		
		return persondatingProfile;
	}
	
	@SuppressWarnings("unchecked")
	public PersonDatingProfile findMatch(Gender pref_gender,BigDecimal pref_age, String location, Long curPersonId) throws DatingServiceException{
		PersonDatingProfile person = null;
		try{
			List<Long> alreadyMatched = getAlreadyMatched(curPersonId);
			alreadyMatched.add(curPersonId);
			
			Date dob = calculateDobFromAge(pref_age);
			
			Query qry = em.createQuery("from PersonDatingProfile p WHERE p.username <> p.person.msisdn AND  p.person.active=:active AND p.gender=:gender AND p.location like :location AND p.dob<=:dob AND p.person.id NOT IN (SELECT DISTINCT person_b_id from SystemMatchLog sml WHERE sml.person_a_id = :person_a_id) order by p.creationDate desc");//:alreadyMatched)");//AND p.dob>=:dob
			qry.setParameter("active", new Boolean(true));
			qry.setParameter("gender", pref_gender);
			qry.setParameter("location", "%"+location+"%");
			qry.setParameter("dob", dob);
			qry.setParameter("person_a_id", curPersonId);
			
			try{
				watch.start();
			}catch(Exception exp){
				logger.error(exp);
			}
			
			List<PersonDatingProfile> ps = qry.getResultList();
			
			if(ps.size()>0){
				person = ps.get(0);
				try{
					watch.stop();
					logger.info("::::::::::It took "+(Double.parseDouble(watch.elapsedTime(java.util.concurrent.TimeUnit.MILLISECONDS)+"")) + " mili-seconds to search a member");
					watch.reset();
				}catch(Exception exp){
					logger.error(exp);
				}
			}
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}
		return person;
	}
	
	@SuppressWarnings("unchecked")
	private List<Long> getAlreadyMatched(Long person_id) {
		List<Long> alreadyMatched = new ArrayList<Long>();
		try{
			Query qry   = em.createQuery("SELECT DISTINCT person_b_id from SystemMatchLog sml WHERE sml.person_a_id = :person_a_id");
			qry.setParameter("person_a_id", person_id);
			alreadyMatched = qry.getResultList();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}
		
		if(alreadyMatched.size()<=0)
			alreadyMatched.add(-1L);
		return alreadyMatched;
	}

	@SuppressWarnings("unchecked")
	public PersonDatingProfile getperSonUsingChatName(String chat_username) throws DatingServiceException{
		PersonDatingProfile person = null;
		try{
			Query qry = em.createQuery("from PersonDatingProfile p where p.username=:username ");
			qry.setParameter("username", chat_username);
			List<PersonDatingProfile> ps = qry.getResultList();
			if(ps.size()>0)
				person = ps.get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}
		return person;
	}
	@SuppressWarnings("unchecked")
	@Override
	public Person getPerson(String msisdn, OperatorCountry opco) throws DatingServiceException {
		Person person = null;
		try{
			Query qry = em.createQuery("from Person p where p.msisdn=:msisdn AND p.opco=:opco");
			qry.setParameter("msisdn", msisdn);
			qry.setParameter("opco", opco);
			List<Person> ps = qry.getResultList();
			if(ps.size()>0)
				person = ps.get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}
		return person;
	}

	
	
	
	private String buildWhere(Map<String, Object> criteria)  throws Exception {
		StringBuffer sb = new StringBuffer();
		if (criteria.size() > 0)
			sb.append(" WHERE ");
		int counter2 = 0;
		for (String key : criteria.keySet()) {
			counter2++;
			sb.append(key).append("=:").append("param").append(String.valueOf(counter2))
					.append(criteria.size() == counter2 ? "" : " AND ");
		}
		return sb.toString();
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Collection<T> find(Class<T> entityClass,
			Map<String, Object> criteria, int start, int end)  throws Exception {
		try{
			Query query = em.createQuery("from " + entityClass.getSimpleName()
		
				+ buildWhere(criteria));
			int counter1 = 0;
			for (String key : criteria.keySet()){
				counter1++;
				query.setParameter("param"+String.valueOf(counter1), criteria.get(key));			
			}			
			return query.getResultList();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
			return null;
		}catch(Exception e){
			throw e;
		}
	}
	
    /**
	 * To statslog
	 */
	
	public boolean toStatsLog(IncomingSMS mo, String presql)  throws Exception {
		boolean success = false;
		try{
		 
			Query qry = em.createNativeQuery(presql);
			qry.setParameter(1, mo.getServiceid());
			qry.setParameter(2, mo.getMsisdn());
			qry.setParameter(3, mo.getCmp_tx_id());
			qry.setParameter(4, mo.getPrice().doubleValue());
			qry.setParameter(5, mo.getIsSubscription());
			
			int num =  qry.executeUpdate();
			success = num>0;
		
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
		}
		 
		return success;
		
	}

	
	
	
	public boolean  acknowledge(long message_log_id) throws Exception{
		boolean success = false;
		try{
		 
			Query qry = em.createNativeQuery("UPDATE `"+CelcomImpl.database+"`.`messagelog` SET mo_ack=1 WHERE id=?");
			qry.setParameter(1, message_log_id);
			int num =  qry.executeUpdate();
			success = num>0;
		
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}
		 
		return success;
	}
	

	
/**
	 * Logs in httptosend
	 */
	
	
	
	@SuppressWarnings("unchecked")
	public ProfileQuestion getNextProfileQuestion(Long profile_id) throws DatingServiceException{
		ProfileQuestion nexqQ = null;
		
		try{
			Query qry = em.createQuery("from ProfileQuestion pq WHERE pq.id NOT IN   (SELECT ql.question_id_fk FROM QuestionLog ql WHERE ql.profile_id_fk=:profile_id_fk ) AND pq.active=:active_ ORDER BY pq.serial asc");
			qry.setParameter("profile_id_fk", profile_id);
			qry.setParameter("active_", true);
			List<ProfileQuestion> pq =  qry.getResultList();
			if(pq.size()>0)
				nexqQ = pq.get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.warn(ex.getMessage() + " no more questions for profile  id  "+profile_id);
			return null;
		}catch (Exception e) {

			logger.error(e.getMessage(), e);

			throw new DatingServiceException(e.getMessage(),e);

		}finally{
		}
		
		return nexqQ;
	}
	
	public boolean isUsernameUnique(String username) throws DatingServiceException{
		boolean isunique = true;
		
		try{
			if(username==null)
				return false;
			
			if(username.contains("*"))
				return false;
			try{
				BigDecimal bd = new BigDecimal(username);
				if(username.length()<4)
					return false;
			}catch(Exception exp){}
			
			Query qry = em.createQuery("from PersonDatingProfile p WHERE (p.username=:username OR p.person.msisdn=:username)");
			qry.setParameter("username", username);
			if(qry.getResultList().size()>0)
				isunique = false;
			
			
			qry = em.createQuery("from SMSService sm WHERE sm.cmd=:keyword");
			qry.setParameter("keyword", username);
			if(qry.getResultList().size()>0)
				isunique = false;
			
			qry = em.createQuery("from DisallowedWords WHERE word=:username");
			qry.setParameter("username", username);
			if(qry.getResultList().size()>0)
				isunique = false;
			
		}catch(javax.persistence.NoResultException ex){
			logger.warn(ex.getMessage() + " no PersonDatingProfile found with the username "+username);
			return isunique;
		}catch (Exception e) {

			logger.error(e.getMessage(), e);

			throw new DatingServiceException(e.getMessage(),e);

		}finally{
		}

		return isunique;
	}
	
	
	/**
	 * Subtracts the period from
	 * current's timestamp on the server.
	 * @param period
	 * @param unit
	 * @return
	 * @throws DatingServiceException
	 */
	public Date getPastTime(Long period, TimeUnit unit) throws DatingServiceException{
		Date date = null;
		try{
			Query qry = em.createNativeQuery("select DATE_SUB(now(), INTERVAL :period "+unit.toString()+") ");
			qry.setParameter("period", period);
			Object o = qry.getSingleResult();
			date = (Date) o;
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
			throw new DatingServiceException(exp.getMessage(), exp);
		}
		return date;
	}
	
	public BigInteger calculateAgeFromDob(Date dob) throws DatingServiceException{
		BigInteger age = null;
		try{
			Query qry = em.createNativeQuery("SELECT TIMESTAMPDIFF(YEAR, :dob, CURDATE()) as 'age'");
			qry.setParameter("dob", dob);
			Object o = qry.getSingleResult();
			age = (BigInteger) o;
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
			throw new DatingServiceException(exp.getMessage(), exp);
		}
		return age;
	}
	public Date calculateDobFromAge(BigDecimal age) throws DatingServiceException{
		Date date = null;
		try{
			Date timeInNairobi = timezoneEJB.convertFromOneTimeZoneToAnother(new Date(), "America/New_York", "Africa/Nairobi");
			String dateNbi = timezoneEJB.dateToString(timeInNairobi);
			logger.info("QRY: select DATE_SUB('"+dateNbi+"' ,INTERVAL :age YEAR) ");
			Query qry = em.createNativeQuery("select DATE_SUB(CONVERT_TZ(CURRENT_TIMESTAMP,'-04:00','+03:00') ,INTERVAL :age YEAR) ");
			qry.setParameter("age", age.longValue());
			Object o = qry.getSingleResult();
			date = (Date) o;
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
			throw new DatingServiceException(exp.getMessage(), exp);
		}
		return date;
	}
	
	
	public OutgoingSMS renewSubscription(IncomingSMS incomingsms, Long serviceid, AlterationMethod method) throws DatingServiceException{
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		try{
			
			
			final RequestObject req = new RequestObject(incomingsms);
			final String KEYWORD = req.getKeyword().trim();
			final String MESSAGE = req.getMsg().trim();
			//final int serviceid = 	mo.getServiceid();
			final String MSISDN = req.getMsisdn();
		
		
			int language_id = 1;
	
			final Person person = getPerson(incomingsms.getMsisdn(), incomingsms.getOpco());
			
		
			Billable billable = createBillable(incomingsms);
			
			
			billable = charge(billable);
			
			String msg = "";
			
			PersonDatingProfile profile = person!=null?  getProfile(person) : null;
			if(profile!=null)
				language_id = profile.getLanguage_id();
			
			if(!billable.isSuccess()){
				
				
				String message_key = BILLING_FAILED;
				if(billable.getResp_status_code().equalsIgnoreCase(BillingStatus.INSUFFICIENT_FUNDS.toString())
						|| "OL402".equals(billable.getResp_status_code()) 
						|| "OL404".equals(billable.getResp_status_code()) 
						|| "OL405".equals(billable.getResp_status_code())  
						|| "OL406".equals(billable.getResp_status_code())){
				
					 message_key =  billable.getResp_status_code();
				}
				
				String message = getMessage(message_key, language_id);
				
				outgoingsms.setSms(message);
				outgoingsms.setPrice(BigDecimal.ZERO);//set price to subscription price
				outgoingsms.setPriority(0);
				
				try{
					subscriptionBean.updateQueueStatus(2L, incomingsms.getMsisdn(), serviceid,method);
				}catch(Exception exp){
					logger.error("ERROR DURING SUBSCRIPTION RENEWAL:: "+exp.getMessage(),exp);
				}
				return outgoingsms;
				
			}else{
				
				
				SMSService smsserv = find(SMSService.class, serviceid);
				
				
				Subscription sub = subscriptionBean.renewSubscription(MSISDN, smsserv,SubscriptionStatus.confirmed,method);
				
				msg = getMessage(DatingMessages.SUBSCRIPTION_RENEWED, language_id);
				msg = msg.replaceAll(EXPIRY_DATE_TAG, timezone_ejb.convertToPrettyFormat( sub.getExpiryDate() ));
				msg = msg.replaceAll(SERVICE_NAME_TAG, smsserv.getService_name());
				outgoingsms.setSms(msg);
				outgoingsms.setPrice(incomingsms.getPrice());//set price to subscription price
				outgoingsms.setPriority(0);
			}
			
			billable.setIn_outgoing_queue(0L);
			billable.setProcessed(1L);
			billable = saveOrUpdate(billable);
		
		}catch(Exception exp){
			throw new DatingServiceException("Sorry, something went wrong. Try again later.", exp);
		}
		
		return outgoingsms;
	}
	
	

	private Billable createBillable(IncomingSMS incomingsms) {
		Billable billable =  new Billable();
			
		billable.setCp_id("CONTENT360_KE");
		billable.setCp_tx_id(incomingsms.getCmp_tx_id());
		billable.setDiscount_applied("0");
		billable.setIn_outgoing_queue(0l);
		billable.setKeyword(incomingsms.getSms().split("\\s")[0].toUpperCase());
		billable.setMaxRetriesAllowed(1L);
		billable.setMessage_id(incomingsms.getId());
		billable.setMsisdn(incomingsms.getMsisdn());
		billable.setOperation(incomingsms.getPrice().compareTo(BigDecimal.ZERO)>0 ? Operation.debit.toString() : Operation.credit.toString());
		billable.setPrice(incomingsms.getPrice());
		billable.setPriority(0l);
		billable.setProcessed(0L);
		billable.setRetry_count(0L);
		if(incomingsms.getServiceid()>0)
			billable.setService_id(incomingsms.getServiceid()+"");
		else
			billable.setService_id(incomingsms.getSms().split("\\s")[0].toUpperCase());
		billable.setShortcode(incomingsms.getShortcode());		
		billable.setCp_tx_id(incomingsms.getCmp_tx_id());
		billable.setEvent_type((incomingsms.getEvent_type()!=null ?  EventType.get(incomingsms.getEvent_type()) :  EventType.SUBSCRIPTION_PURCHASE));
		billable.setPricePointKeyword(incomingsms.getPrice_point_keyword());
			
		return billable;
	}

	@SuppressWarnings("unchecked")
	public ProfileQuestion getPreviousQuestion(Long profile_id) throws DatingServiceException{
		
		ProfileQuestion nexqQ = null;
		
		try{
			
			Query qry = em.createQuery("SELECT ql.question_id_fk FROM QuestionLog ql WHERE ql.profile_id_fk=:profile_id_fk order by timeStamp desc,id desc,question_id_fk desc ");
			qry.setParameter("profile_id_fk", profile_id);
			qry.setFirstResult(0);
			qry.setMaxResults(1);
			List<Object> qidfko  = qry.getResultList();
			
			Long question_id_fk = -1l;
			
			if(qidfko.size()>0)
			 question_id_fk = (Long) qidfko.get(0);
			
			qry = em.createQuery("from ProfileQuestion pq WHERE pq.id=:question_id_fk");
			qry.setParameter("question_id_fk", question_id_fk.longValue());
			List<ProfileQuestion> pq =  qry.getResultList();
			if(pq.size()>0)
				nexqQ = pq.get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.warn(ex.getMessage() + " no more questions for profile  id  "+profile_id);
			return null;
		}catch (Exception e) {

			logger.error(e.getMessage(), e);

			throw new DatingServiceException(e.getMessage(),e);

		}finally{
		}
		
		return nexqQ;
	}


	
	
	@SuppressWarnings("unchecked")
	public PersonDatingProfile getProfileOfLastPersonIsentMessageTo(Person person, Long period, TimeUnit timeUnit) throws DatingServiceException{
		
		if(period==0)
			return null;
		
		PersonDatingProfile datingperson_profile = null;
		
		try{
			Date date = getPastTime(period,timeUnit);
			Query qry = em.createQuery("from ChatLog cl WHERE cl.source_person_id=:source_person_id AND cl.timeStamp >= :pastTs  order by cl.timeStamp desc");///*AND cl.timeStamp>=:timeStamp*/
			qry.setParameter("source_person_id", person.getId());
			qry.setParameter("pastTs", date);
			qry.setFirstResult(1);
			qry.setMaxResults(10);
			List<ChatLog> ps = qry.getResultList();
			if(ps.size()>0){
				ChatLog chatlog = ps.get(0);
				Long latPersonIsentMsg = chatlog.getDest_person_id();
				Query qry2 = em.createQuery("from PersonDatingProfile p WHERE p.person.active=:active AND p.person.id=:person_id");
				qry2.setParameter("active", new Boolean(true));
				qry2.setParameter("person_id", latPersonIsentMsg);
				datingperson_profile = (PersonDatingProfile) qry2.getSingleResult();
			}
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
		return datingperson_profile;
	}

	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.DatingServiceI#deactivate(java.lang.String)
	 */
	@Override
	public boolean deactivate(String msisdn) {
		boolean success = false;
		try{
			PersonDatingProfile profile = getProfile(msisdn);
			if(profile!=null){
				Person person = profile.getPerson();
				if(person!=null){
					person.setActive(Boolean.FALSE);
					person.setLoggedin(Boolean.FALSE);
					person = saveOrUpdate(person);
				}
					
			}
			success = true;
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
		return success;
	}
	
	
	@Override
	public boolean reactivate(String msisdn) {
		boolean success = false;
		try{
			PersonDatingProfile profile = getProfile(msisdn);
			if(profile!=null){
				Person person = profile.getPerson();
				if(person!=null){
					person.setActive(Boolean.TRUE);
					person.setLoggedin(Boolean.TRUE);
					person = saveOrUpdate(person);
				}
					
			}
			success = true;
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
		return success;
	}
	
	

	
}
