package com.pixelandtag.cmp.ejb.api.sms;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.dao.opco.OpcoSMSServiceDAOI;

@Stateless
@Remote
public class OpcoSMSServiceEJBImpl implements OpcoSMSServiceEJBI {
	
	public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Inject
	private OpcoSMSServiceDAOI opcosmsserviceDAO;

}
