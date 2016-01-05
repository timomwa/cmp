package com.pixelandtag.cmp.ejb.subscription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.dao.smsservice.DoubleConfirmationQueueDAOI;
import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.subscription.DoubleConfirmationQueue;

@Stateless
@Remote
public class DoubleConfirmationQueueEJBImpl implements DoubleConfirmationQueueEJBI {

public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Inject
	private DoubleConfirmationQueueDAOI doubleconfirmationQueueDAO;
	
	@Override
	public DoubleConfirmationQueue getQueue(String msisdn, OpcoSMSService smsservice){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("msisdn", msisdn);
		params.put("opcosmsservice", smsservice);
		List<DoubleConfirmationQueue> dcqlist = doubleconfirmationQueueDAO.findByNamedQuery(DoubleConfirmationQueue.NQ_FIND_BY_MSISDN_AND_SERVICE, params);
		return dcqlist!=null && dcqlist.size()>0 ? dcqlist.get(0) : null;
	}
	
	@Override
	public boolean enqueue(String msisdn, OpcoSMSService smsservice) throws Exception{
		DoubleConfirmationQueue doubleconfirmqueue = new DoubleConfirmationQueue();
		doubleconfirmqueue.setMsisdn(msisdn);
		doubleconfirmqueue.setOpcosmsservice(smsservice);
		doubleconfirmationQueueDAO.save(doubleconfirmqueue);
		return true;
	}
	
	
	@Override
	public void dequeue(DoubleConfirmationQueue doubleConfirmationQueue) throws Exception{
		doubleConfirmationQueue = em.merge(doubleConfirmationQueue);
		doubleconfirmationQueueDAO.delete(doubleConfirmationQueue);
	}
}
