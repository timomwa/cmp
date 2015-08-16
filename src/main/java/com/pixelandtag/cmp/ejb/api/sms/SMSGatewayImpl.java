package com.pixelandtag.cmp.ejb.api.sms;

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

import com.pixelandtag.cmp.dao.opco.ProfileTemplatesDAOI;
import com.pixelandtag.cmp.dao.opco.OperatorCountryDAOI;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.cmp.entities.customer.configs.ProfileTemplate;
import com.pixelandtag.cmp.entities.customer.configs.SenderProfile;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.smssenders.SMSSenderFactory;
import com.pixelandtag.smssenders.Sender;

@Stateless
@Remote
public class SMSGatewayImpl implements SMSGatewayI {
	
	public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Inject
	private OperatorCountryDAOI opcoDAO;
		
	@EJB
	private ConfigsEJBI configsEJB;
	
	@PostConstruct
	public void init(){
		//opcoDAO.setEm(em);
		//opcotemplateDAO.setEm(em);
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
		
		OpcoSenderProfile senderProfiles = configsEJB.getActiveOpcoSenderProfile(opco);
		
		if(senderProfiles==null)
			throw new SMSGatewayException("The operator with the opcoid = "+opcoid+" has no active or valid sender profiles set!");
		
		SenderProfile profile = senderProfiles.getProfile();
		
		Map<String,ProfileConfigs> opcoconfigs = configsEJB.getAllConfigs(profile);
		Map<String,ProfileTemplate> opcotemplates = configsEJB.getAllTemplates(profile,TemplateType.PAYLOAD);
		
		SenderConfiguration senderconfigs = new SenderConfiguration();
		senderconfigs.setOpcoconfigs(opcoconfigs);
		senderconfigs.setOpcotemplates(opcotemplates);
		
		try {
			Sender sender = SMSSenderFactory.getSenderInstance(senderconfigs);
			sender.sendSMS(mtsms);
			return true;
		} catch (Exception exp) {
			logger.error(exp.getMessage(),exp);
			throw new SMSGatewayException("Problem occurred instantiating sender. Error: "+exp.getMessage());
		}
	}

}
