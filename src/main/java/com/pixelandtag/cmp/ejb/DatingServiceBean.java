package com.pixelandtag.cmp.ejb;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
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

import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.dating.entities.Gender;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileQuestion;
import com.pixelandtag.dating.entities.QuestionLog;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.mms.api.TarrifCode;
import com.pixelandtag.serviceprocessors.sms.DatingMessages;

@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class DatingServiceBean  extends BaseEntityBean implements DatingServiceI {
	
	
	public Logger logger = Logger.getLogger(DatingServiceBean.class);
	
	@Resource
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	

	@Resource
	private UserTransaction utx;

	
	@Override
	public String getMessage(String key, int language_id) throws DatingServiceException{
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
	public PersonDatingProfile findMatch(Gender pref_gender) throws DatingServiceException{
		PersonDatingProfile person = null;
		try{
			Query qry = em.createQuery("from PersonDatingProfile p where p.gender=:gender order by rand()");//AND p.dob>=:dob
			qry.setParameter("gender", pref_gender);
			List<PersonDatingProfile> ps = qry.getResultList();
			if(ps.size()>0)
				person = ps.get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}
		return person;
	}

	
	
	@SuppressWarnings("unchecked")
	public PersonDatingProfile findMatch(Gender pref_gender,BigDecimal pref_age) throws DatingServiceException{
		PersonDatingProfile person = null;
		try{
			Date dob = calculateDobFromAge(pref_age);
			Query qry = em.createQuery("from PersonDatingProfile p where p.gender=:gender AND p.dob<=:dob order by rand()");//AND p.dob>=:dob
			qry.setParameter("gender", pref_gender);
			qry.setParameter("dob", dob);
			List<PersonDatingProfile> ps = qry.getResultList();
			if(ps.size()>0)
				person = ps.get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}
		return person;
	}
	
	
	@SuppressWarnings("unchecked")
	public PersonDatingProfile findMatch(Gender pref_gender,BigDecimal pref_age, String location) throws DatingServiceException{
		PersonDatingProfile person = null;
		try{
			Date dob = calculateDobFromAge(pref_age);
			Query qry = em.createQuery("from PersonDatingProfile p where p.gender=:gender AND p.location like :location AND p.dob<=:dob order by rand()");//AND p.dob>=:dob
			qry.setParameter("gender", pref_gender);
			qry.setParameter("location", "%"+location+"%");
			qry.setParameter("dob", dob);
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
			Query qry = em.createQuery("from PersonDatingProfile p where p.username=:username");
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
	 * saves and commits
	 * @param t
	 * @return
	 * @throws Exception 
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T> T find(Class<T> entityClass, Long id) throws Exception {
		try{
			utx.begin();
			T t = em.find( entityClass,id);
			utx.commit();
			return t;
		}catch(Exception  e){
			try{
				utx.rollback();
			}catch(Exception ex){}
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

}
