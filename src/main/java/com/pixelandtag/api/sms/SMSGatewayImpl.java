package com.pixelandtag.api.sms;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.dao.opco.OperatorCountryDAOI;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.entities.MTsms;

@Stateless
@Remote
public class SMSGatewayImpl implements SMSGatewayI {
	
	public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Inject
	private OperatorCountryDAOI opcoDAO;
	
	@PostConstruct
	public void init(){
		opcoDAO.setEm(em);
	}
	

	@Override
	public boolean sendMT(MTsms mtsms) throws SMSGatewayException {
		
		if(mtsms==null)
			throw new SMSGatewayException("MT SMS object passed is null!");
		
		Long opcoid = mtsms.getOpcoid();
		
		if(opcoid==null || opcoid.compareTo(-1L)<=0)
			throw new SMSGatewayException("Opco id not provided in the MTSMS object. Specify this.");
		
		OperatorCountry opco = opcoDAO.findById(opcoid);
		
		if(opco==null)
			opco = opcoDAO.findbyOpcoCode(mtsms.getOpcocode());
		
		if(opco==null)
			throw new SMSGatewayException("The operator with the opcoid = "+opcoid+" was not found!");
		
		
		// Determine the protocol (smpp, http rest SOAP/json)
		
		return false;
	}

}
