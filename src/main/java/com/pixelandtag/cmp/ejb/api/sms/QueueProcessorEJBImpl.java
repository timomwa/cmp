package com.pixelandtag.cmp.ejb.api.sms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.MTStatus;
import com.pixelandtag.cmp.dao.core.SMSOutQueueDAOI;
import com.pixelandtag.cmp.entities.OutgoingSMS;

@Stateless
@Remote
public class QueueProcessorEJBImpl implements QueueProcessorEJBI {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	private Collection<BillingStatus> billingstatuses = new ArrayList<BillingStatus>();
	
	@PostConstruct
	public void init(){
		billingstatuses.add(BillingStatus.NO_BILLING_REQUIRED);
		billingstatuses.add(BillingStatus.INSUFFICIENT_FUNDS);
		billingstatuses.add(BillingStatus.SUCCESSFULLY_BILLED);
	}
	
	@Inject
	private SMSOutQueueDAOI smsoutDAO;
	
	@Override
	public boolean updateMessageLog(String cmp_tx_id, MTStatus status)  throws Exception{
		
		try{
			throw new Exception("com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBImpl.updateMessageLog(Long, MTStatus) "
					+ "has not been implemented, but we're not going to cause for this. Implement this later");
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		return true;
	}
	
	
	@Override
	public boolean deleteFromQueue(OutgoingSMS sms)  throws Exception{
		smsoutDAO.delete(sms);
		return true;
	}
	
	
	@Override
	public OutgoingSMS saveOrUpdate(OutgoingSMS queue) throws Exception{
		return smsoutDAO.save(queue);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<OutgoingSMS> getUnsent(Long size){
		
		try{
			
			Query qry = em.createNamedQuery(OutgoingSMS.NQ_LIST_UNSENT_ORDER_BY_PRIORITY_DESC);
			qry.setParameter("billstatus", billingstatuses);
			qry.setParameter("in_outgoing_queue", Boolean.FALSE);
			qry.setParameter("sent", Boolean.FALSE);
			qry.setFirstResult(0);
			qry.setMaxResults(size.intValue());
			return qry.getResultList();
		
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}
		
		return new ArrayList<OutgoingSMS>();
		
	}


}
