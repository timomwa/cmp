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
import com.pixelandtag.cmp.entities.MOProcessorE;
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
import com.pixelandtag.mms.api.TarrifCode;
import com.pixelandtag.serviceprocessors.sms.DatingMessages;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.sms.producerthreads.Operation;
import com.pixelandtag.sms.producerthreads.Subscription;
import com.pixelandtag.smsmenu.MenuItem;
import com.pixelandtag.smsmenu.Session;
import com.pixelandtag.subscription.dto.MediumType;
import com.pixelandtag.web.beans.RequestObject;

@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class DatingServiceBean  extends BaseEntityBean implements DatingServiceI {
	
		

	@EJB
	private CMPResourceBeanRemote cmp_ejb;
	
	@EJB
	private LocationBeanI location_ejb;
	
	public DatingServiceBean() throws KeyManagementException,
			UnrecoverableKeyException, NoSuchAlgorithmException,
			KeyStoreException {
		super();
	}
	
	public String processDating(RequestObject ro) throws Exception{
		
		String resp = "Request received.";
		
		final String MSISDN = ro.getMsisdn();
		
		Person person = getPerson(MSISDN);
		
		if(person==null)
			person = register(MSISDN);
		
		PersonDatingProfile profile = getProfile(person);
		
		if( (profile!=null && profile.getProfileComplete()) //If profile is already created and valid, 
				&& //and no keyword or message passed along, then we find a match for them
			 (ro.getKeyword()==null || ro.getMsg()==null || ro.getMsg().isEmpty() || ro.getKeyword().isEmpty() 
			 || (ro.getCode()!=null && ro.getMsg().equals(ro.getCode()))  )){
			
			String msg = findMatch(ro,person,profile);
			
			
			SMSService smsserv = getSMSService("DATE");
			Long processor_fk = smsserv.getMo_processorFK();
			MOProcessorE proc = find(MOProcessorE.class, processor_fk);
			
			if(smsserv!=null && processor_fk!=null && proc!=null){
				MOSms mo = new MOSms();
				mo.setMsisdn(MSISDN);
				mo.setPrice(BigDecimal.ZERO);
				mo.setBillingStatus(BillingStatus.NO_BILLING_REQUIRED); 
				mo.setMt_Sent(msg);
				mo.setServiceid(smsserv.getId().intValue());
				mo.setProcessor_id(processor_fk);
				mo.setSMS_SourceAddr(proc.getShortcode());
				mo.setPriority(0);
				mo.setCMP_AKeyword(smsserv.getCmd());
				mo.setCMP_SKeyword(smsserv.getCmd());
				
				if(ro.getTransactionID().compareTo(BigInteger.ONE)>0)
					mo.setCMP_Txid(ro.getTransactionID());
				else
					mo.setCMP_Txid(BigInteger.valueOf(generateNextTxId()));
				
				mo.setSplit_msg(false);
				mo.setBillingStatus(BillingStatus.NO_BILLING_REQUIRED);
				mo.setSubscription(false);
				sendMT(mo);
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
			
			PersonDatingProfile match = findMatch(pref_gender,pref_age, location,person.getId());
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
				sb.append("Location : ").append(match.getLocation()).append("\n");
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
						resp = getMessage(DatingMessages.MUST_AGREE_TO_TNC, language_id) + GenericServiceProcessor.SPACE +previousQuestion.getQuestion() ;
						return resp;
					}
						
				}
				
				
				if(attr.equals(ProfileAttribute.CHAT_USERNAME)){
					boolean isunique = isUsernameUnique(KEYWORD);
					if(isunique || person.getMsisdn().equals(KEYWORD)){
						profile.setUsername(KEYWORD);
					}else{
						resp = getMessage(DatingMessages.USERNAME_NOT_UNIQUE_TRY_AGAIN, language_id);
						resp = resp.replaceAll(GenericServiceProcessor.USERNAME_TAG, KEYWORD);
						return resp;
					}
				}
				
				if(attr.equals(ProfileAttribute.GENDER)){
					if(MESSAGE.equalsIgnoreCase("M") ||  MESSAGE.equalsIgnoreCase("MALE") ||  MESSAGE.equalsIgnoreCase("MAN") ||  MESSAGE.equalsIgnoreCase("BOY") ||  MESSAGE.equalsIgnoreCase("MUME") ||  MESSAGE.equalsIgnoreCase("MWANAMME")  ||  MESSAGE.equalsIgnoreCase("MWANAUME")){ 
						profile.setGender(Gender.MALE);
					}else if(MESSAGE.equalsIgnoreCase("F") ||  MESSAGE.equalsIgnoreCase("FEMALE") ||  MESSAGE.equalsIgnoreCase("LADY") ||  MESSAGE.equalsIgnoreCase("GIRL") ||  MESSAGE.equalsIgnoreCase("MKE") ||  MESSAGE.equalsIgnoreCase("MWANAMKE")  ||  MESSAGE.equalsIgnoreCase("MWANAMUKE")){ 
						profile.setGender(Gender.FEMALE);
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
					
					if(age.compareTo(new BigDecimal(18l))<0){
						resp = getMessage(DatingMessages.SERVICE_FOR_18_AND_ABOVE, language_id);
						resp = resp.replaceAll(GenericServiceProcessor.USERNAME_TAG,  profile.getUsername());
						return resp;
					}
					
					dob = calculateDobFromAge(age);
					profile.setDob( dob );
				}
				
				if(attr.equals(ProfileAttribute.LOCATION)){
					profile.setLocation(MESSAGE);
					
					if((req.getCellid()!=null && !req.getCellid().isEmpty()) && (req.getLac()!=null && !req.getLac().isEmpty())){
						
						try{
							location_ejb.findOrCreateLocation(Long.valueOf(req.getCellid()), Long.valueOf(req.getLac()), MESSAGE, profile);
						}catch(Exception exp){
							logger.error(exp.getMessage(), exp);
						}
						
						
					}
					
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
					if(MESSAGE.equalsIgnoreCase("M") ||  MESSAGE.equalsIgnoreCase("MALE") ||  MESSAGE.equalsIgnoreCase("MAN") ||  MESSAGE.equalsIgnoreCase("BOY") ||  MESSAGE.equalsIgnoreCase("MUME") ||  MESSAGE.equalsIgnoreCase("MWANAMME")  ||  MESSAGE.equalsIgnoreCase("MWANAUME")) {
						profile.setPreferred_gender(Gender.MALE);
					}else if(MESSAGE.equalsIgnoreCase("F") ||  MESSAGE.equalsIgnoreCase("FEMALE") ||  MESSAGE.equalsIgnoreCase("LADY") ||  MESSAGE.equalsIgnoreCase("GIRL") ||  MESSAGE.equalsIgnoreCase("MKE") ||  MESSAGE.equalsIgnoreCase("MWANAMKE")  ||  MESSAGE.equalsIgnoreCase("MWANAMUKE")){ 
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
					profile.setProfileComplete(true);
					person.setActive(true);
					profile.setPerson(person);
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
					
					PersonDatingProfile match = findMatch(pref_gender,pref_age, location,person.getId());
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
						Long processor_fk = smsserv.getMo_processorFK();
						MOProcessorE proc = find(MOProcessorE.class, processor_fk);
						
						if(smsserv!=null && processor_fk!=null && proc!=null){
							MOSms mo = new MOSms();
							mo.setMsisdn(MSISDN);
							mo.setPrice(BigDecimal.ZERO);
							mo.setBillingStatus(BillingStatus.NO_BILLING_REQUIRED);
							mo.setMt_Sent(msg);
							mo.setServiceid(smsserv.getId().intValue());
							mo.setProcessor_id(processor_fk);
							mo.setSMS_SourceAddr(proc.getShortcode());
							mo.setPriority(0);
							mo.setCMP_AKeyword(smsserv.getCmd());
							mo.setCMP_SKeyword(smsserv.getCmd());
							
							if(req.getTransactionID().compareTo(BigInteger.ONE)>0)
								mo.setCMP_Txid(req.getTransactionID());
							else
								mo.setCMP_Txid(BigInteger.valueOf(generateNextTxId()));
							
							mo.setSplit_msg(false);
							mo.setBillingStatus(BillingStatus.NO_BILLING_REQUIRED);
							mo.setSubscription(false);
							mo.setProcessor_id(processor_fk);
							sendMT(mo);
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
			resp = "Request received, but something went wrong in that we couldn't process your request. Kindly try again later";
			
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
	

	public Logger logger = Logger.getLogger(DatingServiceBean.class);
	
	@Resource
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	

	@Resource
	private UserTransaction utx;

	
	@Override
	public String getMessage(String key, int language_id) throws DatingServiceException{
		if(language_id<=0)
			language_id = 1;
		String message = "Error 130 :  Translation text not found. language_id = "+language_id+" key = "+key;
		
		try {
			String sql = "SELECT message FROM "+CelcomImpl.database+".message WHERE language_id = ? AND `key` = ? LIMIT 1";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, language_id);
			qry.setParameter(2, key);

			Object obj = qry.getSingleResult();
			if (obj!=null) {
				message = (String) obj;
			}


			logger.debug("looking for :[" + key + "], found [" + message + "]");
			
			return message;

		}catch(javax.persistence.NoResultException ex){
			logger.warn(ex.getMessage() + " no profile for subscriber "+message);
			return null;
		}catch (Exception e) {

			logger.error(e.getMessage(), e);

			throw new DatingServiceException(e.getMessage(),e);

		}finally{
		}
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
	public Person register(String msisdn) throws DatingServiceException {
		Person person = new Person();
		person.setMsisdn(msisdn);
		person.setActive(false);		
		return saveOrUpdate(person);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T> T saveOrUpdate(T t) throws DatingServiceException{
		try{
			utx.begin();
			t = em.merge(t);
			utx.commit();
		}catch(Exception e){
			try {
				utx.rollback();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw new DatingServiceException(e.getMessage(),e);
		}
		return t;
	}
	

	@SuppressWarnings("unchecked")
	public PersonDatingProfile findMatch(Gender pref_gender, Long curPersonId) throws DatingServiceException{
		PersonDatingProfile person = null;
		try{
			List<Long> alreadyMatched = getAlreadyMatched(curPersonId);
			alreadyMatched.add(curPersonId);
			Query qry = em.createQuery("from PersonDatingProfile p WHERE p.username <> p.person.msisdn AND p.person.active=:active AND p.gender=:gender AND p.person.id NOT IN  (SELECT DISTINCT person_b_id from SystemMatchLog sml WHERE sml.person_a_id = :person_a_id) order by p.creationDate asc");//:alreadyMatched) ");//AND p.dob>=:dob
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
			Query qry = em.createQuery("from PersonDatingProfile p WHERE p.username <> p.person.msisdn AND p.person.active=:active AND p.gender=:gender AND p.dob<=:dob AND p.person.id NOT IN (SELECT DISTINCT person_b_id from SystemMatchLog sml WHERE sml.person_a_id = :person_a_id) order by p.creationDate asc");//:alreadyMatched) ");//AND p.dob>=:dob
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
			
				Date dob = calculateDobFromAge(profile.getPreferred_age());
				
				Query qry = em.createQuery("from ProfileLocation pl WHERE (pl.profile.username <> pl.profile.person.msisdn) AND  pl.profile.person.active=:active "
						+ "AND pl.profile.dob<=:dob AND pl.profile.gender=:prefGender AND (  pl.location.cellid=:cellid OR pl.location.location_id=:location_id OR  pl.location.locationName=:locationName) order by pl.timeStamp asc");
				qry.setFirstResult(0);
				qry.setMaxResults(1);
				
				qry.setParameter("cellid", profileLocation.getLocation().getCellid());
				qry.setParameter("location_id", profileLocation.getLocation().getLocation_id());
				qry.setParameter("locationName", profileLocation.getLocation().getLocationName());
				qry.setParameter("prefGender", profile.getPreferred_gender());
				qry.setParameter("dob", dob);
				qry.setParameter("active", new Boolean(true));
				
				ProfileLocation pl = (ProfileLocation) qry.getSingleResult();
				
				persondatingProfile = pl.getProfile();
			
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
			
			Query qry = em.createQuery("from PersonDatingProfile p WHERE p.username <> p.person.msisdn AND  p.person.active=:active AND p.gender=:gender AND p.location like :location AND p.dob<=:dob AND p.person.id NOT IN (SELECT DISTINCT person_b_id from SystemMatchLog sml WHERE sml.person_a_id = :person_a_id) order by p.creationDate asc");//:alreadyMatched)");//AND p.dob>=:dob
			qry.setParameter("active", new Boolean(true));
			qry.setParameter("gender", pref_gender);
			qry.setParameter("location", "%"+location+"%");
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
	public Person getPerson(String msisdn) throws DatingServiceException {
		Person person = null;
		try{
			Query qry = em.createQuery("from Person p where p.msisdn=:msisdn");
			qry.setParameter("msisdn", msisdn);
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
	
	
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
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
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean toStatsLog(MOSms mo, String presql)  throws Exception {
		boolean success = false;
		try{
		 
			utx.begin();
			Query qry = em.createNativeQuery(presql);
			qry.setParameter(1, mo.getServiceid());
			qry.setParameter(2, mo.getMsisdn());
			qry.setParameter(3, mo.getCMP_Txid());
			qry.setParameter(4, mo.getCMP_AKeyword());
			qry.setParameter(5, mo.getCMP_SKeyword());
			if(mo.getCMP_SKeyword().equals(TarrifCode.RM1.getCode()))
				qry.setParameter(6, 1d);
			else
				qry.setParameter(6, mo.getPrice().doubleValue());
			qry.setParameter(7, mo.isSubscriptionPush());
			
			int num =  qry.executeUpdate();
			utx.commit();
			success = num>0;
		
		}catch(Exception e){
			try {
				utx.rollback();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw e;
		}
		 
		return success;
		
	}

	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean  acknowledge(long message_log_id) throws Exception{
		boolean success = false;
		try{
		 
			utx.begin();
			Query qry = em.createNativeQuery("UPDATE `"+CelcomImpl.database+"`.`messagelog` SET mo_ack=1 WHERE id=?");
			qry.setParameter(1, message_log_id);
			int num =  qry.executeUpdate();
			utx.commit();
			success = num>0;
		
		}catch(Exception e){
			try {
				utx.rollback();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
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
			Query qry = em.createQuery("from ProfileQuestion pq WHERE pq.id NOT IN   (SELECT ql.question_id_fk FROM QuestionLog ql WHERE ql.profile_id_fk=:profile_id_fk ) ORDER BY pq.serial asc");
			qry.setParameter("profile_id_fk", profile_id);
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
			
			try{
				BigDecimal bd = new BigDecimal(username);
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
			Query qry = em.createNativeQuery("select DATE_SUB(now(),INTERVAL :age YEAR) ");
			qry.setParameter("age", age.longValue());
			Object o = qry.getSingleResult();
			date = (Date) o;
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
			throw new DatingServiceException(exp.getMessage(), exp);
		}
		return date;
	}
	
	
	public MOSms renewSubscription(MOSms mo, Long serviceid) throws DatingServiceException{
		
		
		try{
			
			
			final RequestObject req = new RequestObject(mo);
			final String KEYWORD = req.getKeyword().trim();
			final String MESSAGE = req.getMsg().trim();
			//final int serviceid = 	mo.getServiceid();
			final String MSISDN = req.getMsisdn();
		
		
			int language_id = 1;
	
			final Person person = getPerson(mo.getMsisdn());
			
		
			Billable billable = createBillable(mo);
			
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
				
				mo.setMt_Sent(message);
				mo.setPrice(BigDecimal.ZERO);//set price to subscription price
				mo.setPriority(0);
				return mo;
				
			}else{
				
				billable = saveOrUpdate(billable);
				
				SMSService smsserv = find(SMSService.class, serviceid);
			
				Subscription sub = renewSubscription(MSISDN, smsserv);
				
				msg = getMessage(DatingMessages.SUBSCRIPTION_RENEWED, language_id);
				msg = msg.replaceAll(EXPIRY_DATE_TAG, sdf.format( sub.getExpiryDate() ));
				msg = msg.replaceAll(SERVICE_NAME_TAG, smsserv.getService_name());
				mo.setMt_Sent(msg);
				mo.setPrice(mo.getPrice());//set price to subscription price
				mo.setPriority(0);
			}
		
		}catch(Exception exp){
			throw new DatingServiceException("Sorry, something went wrong. Try again later.", exp);
		}
		
		return mo;
	}
	
	

	private Billable createBillable(MOSms mo) {
		Billable billable =  new Billable();
			
		billable.setCp_id("CONTENT360_KE");
		billable.setCp_tx_id(mo.getCMP_Txid());
		billable.setDiscount_applied("0");
		billable.setEvent_type(mo.getEventType());
		billable.setIn_outgoing_queue(0l);
		billable.setKeyword(mo.getSMS_Message_String().split("\\s")[0].toUpperCase());
		billable.setMaxRetriesAllowed(1L);
		billable.setMessage_id(mo.getId());
		billable.setMsisdn(mo.getMsisdn());
		billable.setOperation(mo.getPrice().compareTo(BigDecimal.ZERO)>0 ? Operation.debit.toString() : Operation.credit.toString());
		billable.setPrice(mo.getPrice());
		billable.setPriority(0l);
		billable.setProcessed(0L);
		billable.setRetry_count(0L);
		if(mo.getServiceid()>0)
			billable.setService_id(mo.getServiceid()+"");
		else
			billable.setService_id(mo.getSMS_Message_String().split("\\s")[0].toUpperCase());
		billable.setShortcode(mo.getSMS_SourceAddr());		
		billable.setTx_id(mo.getCMP_Txid());
		billable.setEvent_type((mo.getEventType()!=null ? mo.getEventType() :  EventType.SUBSCRIPTION_PURCHASE));
		billable.setPricePointKeyword(mo.getPricePointKeyword());
			
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
			
			logger.info("\n\n\n\t\t:::question_id_fk == "+question_id_fk );
			
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
		PersonDatingProfile datingperson_profile = null;
		try{
			Date date = getPastTime(period,timeUnit);
			logger.info("\n\n\n "+date+",person.getId()="+person.getId()+" \n\n\n ");
			Query qry = em.createQuery("from ChatLog cl WHERE cl.source_person_id=:source_person_id  order by cl.timeStamp desc");///*AND cl.timeStamp>=:timeStamp*/
			qry.setParameter("source_person_id", person.getId());
			qry.setFirstResult(1);
			qry.setMaxResults(10);
			List<ChatLog> ps = qry.getResultList();
			logger.info("\n\n\nps  "+ps+"\n\n\n");
			logger.info("\n\n\nps.size():: "+ps.size()+"\n\n\n");
			if(ps.size()>0){
				ChatLog chatlog = ps.get(0);
				Long latPersonIsentMsg = chatlog.getDest_person_id();
				logger.info("latPersonIsentMsg:: "+latPersonIsentMsg);
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
	
	

	public long generateNextTxId(){
		try {
			Thread.sleep(6);
		} catch (InterruptedException e) {
			logger.warn("\n\t\t::"+e.getMessage());
		}
		return System.currentTimeMillis();
	}
}
