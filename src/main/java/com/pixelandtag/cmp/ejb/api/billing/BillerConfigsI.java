package com.pixelandtag.cmp.ejb.api.billing;

import java.util.Map;

import com.pixelandtag.billing.BillerProfile;
import com.pixelandtag.billing.BillerProfileConfig;
import com.pixelandtag.billing.entities.BillerProfileTemplate;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;

public interface BillerConfigsI {

	public Map<String, BillerProfileConfig> getAllConfigs(BillerProfile profile);

	public Map<String, BillerProfileTemplate> getAllTemplates(BillerProfile profile, TemplateType payload);

	public BillerProfile getActiveOpcoSenderReceiverProfile(OperatorCountry opco);

}
