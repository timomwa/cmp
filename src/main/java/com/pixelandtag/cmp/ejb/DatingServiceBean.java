package com.pixelandtag.cmp.ejb;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.serviceprocessors.sms.DatingMessages;

public class DatingServiceBean implements DatingServiceI {
	
	
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
	
	
	@Override
	public String getMessage(DatingMessages datingMsg, int language_id) throws DatingServiceException{
		return getMessage(datingMsg.toString(),language_id);
	}
	
	@Override
	public Person register(String msisdn) throws DatingServiceException {
		Person person = new Person();
		person.setMsisdn(msisdn);
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

}
