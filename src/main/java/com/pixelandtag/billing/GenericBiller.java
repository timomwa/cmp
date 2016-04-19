package com.pixelandtag.billing;

import java.util.HashMap;
import java.util.Map;

import com.pixelandtag.billing.entities.BillerProfileTemplate;

public abstract class GenericBiller implements Biller {

	private Map<String,BillerProfileConfig> configuration;
	private Map<String,BillerProfileTemplate> templates = new HashMap<String,BillerProfileTemplate>();
	
	public GenericBiller(BillingConfigSet billingconfig){
		Map<String,BillerProfileConfig> configuration_ = billingconfig.getOpcoconfigs();
		setConfiguration(configuration_);
		setTemplates(billingconfig.getOpcotemplates());
	}

	public Map<String, BillerProfileConfig> getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Map<String, BillerProfileConfig> configuration) {
		this.configuration = configuration;
	}

	public Map<String, BillerProfileTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(Map<String, BillerProfileTemplate> templates) {
		this.templates = templates;
	}
	
}
