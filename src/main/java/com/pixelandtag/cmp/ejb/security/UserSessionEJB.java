package com.pixelandtag.cmp.ejb.security;

import javax.annotation.Resource;
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

import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.cmp.entities.audit.AuditTrail;


@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class UserSessionEJB implements UserSessionI {
	
	private Logger logger = Logger.getLogger(getClass());

	//@PersistenceContext(unitName = "EjbComponentPU4")
	//private EntityManager em;
	
	@PersistenceContext
	private EntityManager em;
	
	
	//@Resource
	//private UserTransaction utx;
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.security.UserSessionI#getUser(java.lang.String, java.lang.String)
	 */
	@Override
	public User getUser(String username, String password) throws Exception{
		
		User user = null;
		
		try{
			Query qry = em.createQuery("from User u WHERE u.username = :username and u.password = :password");
			qry.setParameter("username", username);
			qry.setParameter("password", password);
			user = (User) qry.getSingleResult();
		}catch(javax.persistence.NoResultException nr){
			logger.warn(nr.getMessage()+" no user with credentials provided");
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		
		return user;
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.security.UserSessionI#createAuditTrail(com.pixelandtag.cmp.entities.audit.UserAction)
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void createAuditTrail(AuditTrail useraction){
		try{
			//utx.begin();
			em.merge(useraction);
			//utx.commit();
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			try{
				//utx.rollback();
			}catch(Exception ex){}
		}
	}

}
