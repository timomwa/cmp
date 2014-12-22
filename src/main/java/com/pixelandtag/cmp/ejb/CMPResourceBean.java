package com.pixelandtag.cmp.ejb;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import javax.persistence.Query;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.dynamic.dto.NoContentTypeException;

@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class CMPResourceBean implements CMPResourceBeanRemote {
	
	public boolean testEJB(int k) throws Exception{
		if(k>0)
			return true;
		else
			throw new NoContentTypeException("No content Exception!! ");
	}

	private Logger logger = Logger.getLogger(CMPResourceBean.class);
	
	@Resource
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	

	@Resource
	private UserTransaction utx;
	
	
	@Override
	public EntityManager getEM() {
		return em;
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T> T saveOrUpdate(T t){
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
		}
		return t;
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
	
	
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T> T find(Class<T> entityClass, String param_name, Object value) throws Exception  {
		T t = null;
		try{
			utx.begin();
			Query query = em.createQuery("from " + entityClass.getSimpleName() + " WHERE "+param_name+" =:"+param_name+" ").setParameter(param_name, value);
			if(query.getResultList().size()>0)
				t = (T) query.getResultList().get(0);
			utx.commit();
		}catch(Exception  e){
			try{
				utx.rollback();
			}catch(Exception ex){}
			throw e;
		}
		return t;
	}
	
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void updateMessageInQueue(long cp_tx_id, BillingStatus billstatus) throws HibernateException{
		try{
			 utx.begin();
			 Query qry = em.createNativeQuery("UPDATE httptosend set priority=:priority, charged=:charged, billing_status=:billing_status WHERE CMP_TxID=:CMP_TxID ")
			.setParameter("priority", billstatus.equals(BillingStatus.SUCCESSFULLY_BILLED) ? 0 :  3)
			.setParameter("charged", billstatus.equals(BillingStatus.SUCCESSFULLY_BILLED) ? 1 :  0)
			.setParameter("billing_status", billstatus.toString())
			.setParameter("CMP_TxID", cp_tx_id);
			 qry.executeUpdate();
			 utx.commit();
		}catch(Exception e){
			try {
				utx.rollback();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
		}
		
	}

}
