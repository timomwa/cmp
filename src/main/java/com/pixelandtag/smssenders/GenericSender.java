package com.pixelandtag.smssenders;

import java.util.HashMap;
import java.util.Map;

import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.cmp.entities.customer.configs.OpcoConfigs;
import com.pixelandtag.cmp.entities.customer.configs.OpcoTemplates;

public abstract class GenericSender implements Sender{
	
	
	private Map<String,OpcoConfigs> configuration;
	private Map<String,OpcoTemplates> templates = new HashMap<String,OpcoTemplates>();
	
	public GenericSender(SenderConfiguration configs) throws MessageSenderException{
		
		Map<String,OpcoConfigs> configuration_ = configs.getOpcoconfigs();
		setConfiguration(configuration_);
		setTemplates(configs.getOpcotemplates());
		validateMandatory();
		
	}

	public Map<String, OpcoConfigs> getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Map<String, OpcoConfigs> configuration) {
		this.configuration = configuration;
	}

	public Map<String, OpcoTemplates> getTemplates() {
		return templates;
	}

	public void setTemplates(Map<String, OpcoTemplates> templates) {
		this.templates = templates;
	}
	
	

}
