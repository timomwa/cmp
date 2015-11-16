package com.pixelandtag.cmp.ejb.api.sms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.dao.opco.OpcoSMSServiceDAOI;
import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;

@Stateless
@Remote
public class OpcoSMSServiceEJBImpl implements OpcoSMSServiceEJBI {
	
	public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Inject
	private OpcoSMSServiceDAOI opcosmsserviceDAO;
	
	
	public String getShortcodeByServiceIdAndOpcoId(Long serviceid, OperatorCountry opco) throws ServiceNotLinkedToOpcoException{
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("service_id", serviceid);
		params.put("opco", opco);
		List<OpcoSMSService> opcosmsservice = opcosmsserviceDAO.findByNamedQuery(OpcoSMSService.NQ_FIND_BY_SERVICE_ID_AND_OPCO, params);
		if(opcosmsservice==null || opcosmsservice.size()<1)
			throw new ServiceNotLinkedToOpcoException("Looks like the service with id '"+serviceid+"' isn't enabled for the opco with id '"+opco.getId()+"'");
		return opcosmsservice.get(0).getMoprocessor().getShortcode();
	}
	
	public OpcoSMSService getOpcoSMSService(Long serviceid, OperatorCountry opco) throws ServiceNotLinkedToOpcoException{
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("service_id", serviceid);
		params.put("opco", opco);
		List<OpcoSMSService> opcosmsservice = opcosmsserviceDAO.findByNamedQuery(OpcoSMSService.NQ_FIND_BY_SERVICE_ID_AND_OPCO, params);
		if(opcosmsservice==null || opcosmsservice.size()<1)
			throw new ServiceNotLinkedToOpcoException("Looks like the service with id '"+serviceid+"' isn't enabled for the opco with id '"+opco.getId()+"'");
		return opcosmsservice.get(0);
	}
	
	public OpcoSMSService getOpcoSMSService(String keyword, OperatorCountry opco) throws ServiceNotLinkedToOpcoException{
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("keyword", keyword);
		params.put("opco", opco);
		List<OpcoSMSService> opcosmsservice = opcosmsserviceDAO.findByNamedQuery(OpcoSMSService.NQ_FIND_BY_KEYWORD_AND_OPCO, params);
		if(opcosmsservice==null || opcosmsservice.size()<1)
			throw new ServiceNotLinkedToOpcoException("Looks like the service with keyword '"+keyword+"' isn't enabled for the opco with id '"+opco.getId()+"'");
		return opcosmsservice.get(0);
		
	}

}
