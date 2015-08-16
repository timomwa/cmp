package com.pixelandtag.smssenders;

import java.util.HashMap;
import java.util.Map;

import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.cmp.entities.customer.configs.ProfileTemplate;

public abstract class GenericSender implements Sender{
	
	
	private Map<String,ProfileConfigs> configuration;
	private Map<String,ProfileTemplate> templates = new HashMap<String,ProfileTemplate>();
	
	public GenericSender(SenderConfiguration configs) throws MessageSenderException{
		
		Map<String,ProfileConfigs> configuration_ = configs.getOpcoconfigs();
		setConfiguration(configuration_);
		setTemplates(configs.getOpcotemplates());
		validateMandatory();
		
	}

	public Map<String, ProfileConfigs> getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Map<String, ProfileConfigs> configuration) {
		this.configuration = configuration;
	}

	public Map<String, ProfileTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(Map<String, ProfileTemplate> templates) {
		this.templates = templates;
	}
	
	

}
