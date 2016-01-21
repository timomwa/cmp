package com.pixelandtag.cmp.ejb.api.sms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.cmp.dao.core.IncomingSMSDAOI;
import com.pixelandtag.cmp.dao.core.MessageLogDAOI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.IncomingSMS;

@Stateless
@Remote 
public class MoProcessorEJBImpl implements MoProcessorEJBI {
	
	
	@Inject
	private IncomingSMSDAOI incomingsmsDAO;
	
	private Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@EJB
	private TimezoneConverterI timeconverterEJB;
	
	private Collection<BillingStatus> billingstatuses = new ArrayList<BillingStatus>();
	
	@PostConstruct
	public void init(){
		billingstatuses.add(BillingStatus.NO_BILLING_REQUIRED);
		billingstatuses.add(BillingStatus.INSUFFICIENT_FUNDS);
		billingstatuses.add(BillingStatus.SUCCESSFULLY_BILLED);
	}
	
	
	@Override
	public IncomingSMS saveOrUpdate(IncomingSMS incomingsms) throws Exception{
		return incomingsmsDAO.save(incomingsms);
	}
	
	
	@Override
	public List<IncomingSMS> getLatestMO(int size){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("processed", Boolean.FALSE);
		params.put("mo_ack", Boolean.FALSE);
		return incomingsmsDAO.findByNamedQuery(IncomingSMS.NQ_LIST_UNPROCESSED, params);
	}

}
