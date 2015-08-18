package com.pixelandtag.cmp.ejb.api.sms;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.cmp.entities.customer.configs.ProfileTemplate;
import com.pixelandtag.cmp.entities.customer.configs.SenderProfile;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;
import com.pixelandtag.smssenders.SMSSenderFactory;
import com.pixelandtag.smssenders.Sender;

@Stateless
@Remote
public class SMSGatewayImpl implements SMSGatewayI {
	
	public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
		
	@EJB
	private ConfigsEJBI configsEJB;
	
	
	@Override
	public boolean sendMT(OutgoingSMS outgoingsms) throws SMSGatewayException {
		
		if(outgoingsms==null)
			throw new SMSGatewayException("OutgoingSMS object passed is null!");
		
		OpcoSenderProfile opcosenderProfile =  outgoingsms.getOpcosenderprofile();
		
		if(opcosenderProfile==null)
			throw new SMSGatewayException("No sender profile set. Please finish this configuration and try agian.!");
		
		SenderProfile profile = opcosenderProfile.getProfile();
		
		Map<String,ProfileConfigs> opcoconfigs = configsEJB.getAllConfigs(profile);
		Map<String,ProfileTemplate> opcotemplates = configsEJB.getAllTemplates(profile,TemplateType.PAYLOAD);
		
		SenderConfiguration senderconfigs = new SenderConfiguration();
		senderconfigs.setOpcoconfigs(opcoconfigs);
		senderconfigs.setOpcotemplates(opcotemplates);
		
		try {
			Sender sender = SMSSenderFactory.getSenderInstance(senderconfigs);
			sender.sendSMS(outgoingsms);
			return true;
		} catch (Exception exp) {
			logger.error(exp.getMessage(),exp);
			throw new SMSGatewayException("Problem occurred instantiating sender. Error: "+exp.getMessage());
		}
	}

}
