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

import com.pixelandtag.cmp.dao.opco.OpcoSenderProfileDAOI;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderProfile;


@Stateless
@Remote
public class OpcoSenderProfileEJBImpl implements OpcoSenderProfileEJBI {

	private Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Inject
	private OpcoSenderProfileDAOI opcosenderprofDAO;
	
	public List<OpcoSenderProfile> getAllActiveProfiles(){
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("active", Boolean.TRUE);
		return opcosenderprofDAO.findByNamedQuery(OpcoSenderProfile.NQ_LIST_ACTIVE, params);
	}
	
	
}
