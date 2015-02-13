package com.pixelandtag.cmp.ejb;

import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
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
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.TimeUnit;
import com.pixelandtag.dating.entities.ChatLog;
import com.pixelandtag.dating.entities.Gender;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileQuestion;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.mms.api.TarrifCode;
import com.pixelandtag.serviceprocessors.sms.DatingMessages;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.sms.producerthreads.Operation;
import com.pixelandtag.sms.producerthreads.Subscription;
import com.pixelandtag.web.beans.RequestObject;

@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class DatingServiceBean  extends BaseEntityBean implements DatingServiceI {
	
		

	@EJB
	private CMPResourceBeanRemote cmp_ejb;
	
	public DatingServiceBean() throws KeyManagementException,
			UnrecoverableKeyException, NoSuchAlgorithmException,
			KeyStoreException {
		super();
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
			String sql = "SELECT message FROM "+CelcomImpl.database+".message WHERE language_id = ? AND `key` = ? ORDER BY RAND() LIMIT 1";
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
	public PersonDatingProfile findMatch(Gender pref_gender, Long curProfileId) throws DatingServiceException{
		PersonDatingProfile person = null;
		try{
			Query qry = em.createQuery("from PersonDatingProfile p where p.gender=:gender AND p.id <> :this_profile order by rand()");//AND p.dob>=:dob
			qry.setParameter("gender", pref_gender);
			qry.setParameter("this_profile", curProfileId);
			List<PersonDatingProfile> ps = qry.getResultList();
			if(ps.size()>0)
				person = ps.get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}
		return person;
	}

	
	
	@SuppressWarnings("unchecked")
	public PersonDatingProfile findMatch(Gender pref_gender,BigDecimal pref_age,Long curProfileId) throws DatingServiceException{
		PersonDatingProfile person = null;
		try{
			Date dob = calculateDobFromAge(pref_age);
			Query qry = em.createQuery("from PersonDatingProfile p where p.gender=:gender AND p.dob<=:dob AND p.id <> :this_profile order by rand()");//AND p.dob>=:dob
			qry.setParameter("gender", pref_gender);
			qry.setParameter("dob", dob);
			qry.setParameter("this_profile", curProfileId);
			List<PersonDatingProfile> ps = qry.getResultList();
			if(ps.size()>0)
				person = ps.get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}
		return person;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public PersonDatingProfile findMatch(Gender pref_gender,BigDecimal pref_age, String location, Long curProfileId) throws DatingServiceException{
		PersonDatingProfile person = null;
		try{
			Date dob = calculateDobFromAge(pref_age);
			Query qry = em.createQuery("from PersonDatingProfile p where p.gender=:gender AND p.location like :location AND p.dob<=:dob AND p.id <> :this_profile order by rand()");//AND p.dob>=:dob
			qry.setParameter("gender", pref_gender);
			qry.setParameter("location", "%"+location+"%");
			qry.setParameter("dob", dob);
			qry.setParameter("this_profile", curProfileId);
			List<PersonDatingProfile> ps = qry.getResultList();
			if(ps.size()>0)
				person = ps.get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}
		return person;
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
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean sendMT(MOSms mo, String sql) throws Exception{
		boolean success = false;
		try{
		 
			utx.begin();
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, mo.getMt_Sent());
			qry.setParameter(2, mo.getMsisdn());
			qry.setParameter(3, mo.getSMS_SourceAddr());
			qry.setParameter(4, mo.getSMS_SourceAddr());
			
			qry.setParameter(5, mo.getCMP_AKeyword());
			qry.setParameter(6, mo.getCMP_SKeyword());
			qry.setParameter(7, mo.getPriority());
			
			if(!(mo.getCMP_Txid()==-1)){
				qry.setParameter(8, String.valueOf(mo.getCMP_Txid()));
			}
			qry.setParameter(9, (mo.isSplit_msg() ? 1 : 0));
			qry.setParameter(10, mo.getServiceid());
			qry.setParameter(11, String.valueOf(mo.getPrice()));
			qry.setParameter(12, mo.getSMS_DataCodingId());
			qry.setParameter(13, mo.getProcessor_id());
			qry.setParameter(14, mo.getBillingStatus().toString());
			qry.setParameter(15, mo.getPricePointKeyword()==null ? "NONE" :  mo.getPricePointKeyword());
			qry.setParameter(16, (mo.isSubscription() ? 1 : 0));
			qry.setParameter(17, ( mo.isSubscription() ? 1 : 0 ));
			
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
			
			Query qry = em.createQuery("from PersonDatingProfile p WHERE p.username=:username");
			qry.setParameter("username", username);
			if(qry.getResultList().size()>0)
				isunique = false;
			
			
			qry = em.createQuery("from SMSService sm WHERE sm.cmd=:keyword");
			qry.setParameter("keyword", username);
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
				if(billable.getResp_status_code().equalsIgnoreCase(BillingStatus.INSUFFICIENT_FUNDS.toString())){
					 message_key =  billable.getResp_status_code();
				}
				
				String message = getMessage(message_key, language_id);
				
				mo.setMt_Sent(message);
				
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
		billable.setCp_tx_id(Long.valueOf(mo.getCMP_Txid()));
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
		billable.setService_id(mo.getSMS_Message_String().split("\\s")[0].toUpperCase());
		billable.setShortcode(mo.getSMS_SourceAddr());		
		billable.setTx_id(Long.valueOf(mo.getCMP_Txid()));
		billable.setEvent_type(EventType.SUBSCRIPTION_PURCHASE);
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
			
			System.out.println("\n\n\n\t\t:::question_id_fk == "+question_id_fk );
			
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
			qry.setMaxResults(1);
			List<ChatLog> ps = qry.getResultList();
			logger.info("\n\n\nps  "+ps+"\n\n\n");
			logger.info("\n\n\nps.size():: "+ps.size()+"\n\n\n");
			if(ps.size()>0){
				ChatLog chatlog = ps.get(0);
				Long latPersonIsentMsg = chatlog.getDest_person_id();
				logger.info("latPersonIsentMsg:: "+latPersonIsentMsg);
				Query qry2 = em.createQuery("from PersonDatingProfile pdp WHERE pdp.person.id=:person_id");
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
}
