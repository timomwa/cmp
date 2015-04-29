package com.pixelandtag.cmp.ejb;

import java.math.BigInteger;

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

import com.pixelandtag.cmp.entities.CMPSequence;
import com.pixelandtag.cmp.exceptions.CMPSequenceException;

@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class SequenceGenEJB implements SequenceGenI{
	
	private Logger logger = Logger.getLogger(getClass());

	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	

	@Resource
	private UserTransaction utx;
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.SequenceGenI#getSequenceCreateIfNotExists(java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	//@Override
	private CMPSequence getSequenceCreateIfNotExists(String name) throws CMPSequenceException{
		CMPSequence cmp_sequence = findSequenceByName(name);
		
		if(cmp_sequence!=null)
			return cmp_sequence;
		
		try {
			cmp_sequence = new CMPSequence();
			cmp_sequence.setName(name);
			cmp_sequence.setNextval(BigInteger.ZERO);
			cmp_sequence.setPrefix(name);
			cmp_sequence.setSuffix(String.valueOf(name.hashCode()));
			utx.begin();
			cmp_sequence = em.merge(cmp_sequence);
			utx.commit();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			try {
				utx.rollback();
			} catch (Exception e1) {
			}
		}
		
		return cmp_sequence;
		
		
	}
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.SequenceGenI#findSequenceByName(java.lang.String)
	 */
	//@Override
	private CMPSequence findSequenceByName(String name){
		CMPSequence sequence = null;
		try{
			Query query = em.createQuery("from CMPSequence s WHERE s.name=:name");
			query.setParameter("name", name);
			sequence = (CMPSequence) query.getSingleResult();
		}catch(javax.persistence.NoResultException nre){
			logger.warn("Sequence with the name "+name+" not found. We should create a new one");
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		return sequence;
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public CMPSequence getOrCreateNextSequence(String name) throws CMPSequenceException{
		
		try{
			CMPSequence sequence = getSequenceCreateIfNotExists(name);
			BigInteger nextVal = sequence.getNextval().add(BigInteger.ONE);
			utx.begin();
			Query qry = em.createQuery("update CMPSequence cs SET cs.nextval=:nextval_ where cs.name=:name_");
			qry.setParameter("nextval_", nextVal);
			qry.setParameter("name_", name);
			qry.executeUpdate();
			utx.commit();
			return sequence;
		}catch(Exception e) {
			logger.error(e.getMessage(),e);
			
			try {
				utx.rollback();
			} catch (Exception e1) {
			}
			throw new CMPSequenceException("Could not get new sequence",e);
		}
		
		
	}
	
	

}
