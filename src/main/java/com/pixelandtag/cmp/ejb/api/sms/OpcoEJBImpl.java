package com.pixelandtag.cmp.ejb.api.sms;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.dao.opco.OperatorCountryDAOI;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;

@Stateless
@Remote
public class OpcoEJBImpl implements OpcoEJBI {
	
	
	public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Inject
	private OperatorCountryDAOI opcoDAO;
	
	@Override
	public OperatorCountry findOpcoByCode(String opcocode){
		return opcoDAO.findbyOpcoCode(opcocode);
	}
	
	@Override
	public OperatorCountry findOpcoById(Long opcoid){
		return opcoDAO.findById(opcoid);
	}

}
