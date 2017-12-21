package com.pixelandtag.cmp.ejb.api.billing;

import java.util.Map;

import com.pixelandtag.billing.BillerProfileConfig;
import com.pixelandtag.billing.OpcoBillingProfile;
import com.pixelandtag.billing.entities.BillerProfileTemplate;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;

public interface BillerConfigsI {

	public Map<String, BillerProfileConfig> getAllConfigs(OpcoBillingProfile profile);

	public Map<String, BillerProfileTemplate> getAllTemplates(OpcoBillingProfile profile, TemplateType payload);

	public OpcoBillingProfile getActiveBillerProfile(OperatorCountry opco);

}
