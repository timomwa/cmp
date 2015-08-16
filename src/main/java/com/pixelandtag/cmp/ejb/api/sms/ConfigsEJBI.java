package com.pixelandtag.cmp.ejb.api.sms;

import java.util.Map;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.OpcoConfigs;
import com.pixelandtag.cmp.entities.customer.configs.OpcoTemplates;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;

public interface ConfigsEJBI {
	
	/**
	 * 
	 * @param opcoid
	 * @param name
	 * @return
	 */
	public OpcoConfigs getConfig(Long opcoid, String name);
	
	/**
	 * 
	 * @param opco
	 * @param name
	 * @return
	 */
	public OpcoConfigs getConfig(OperatorCountry opco, String name);
	
	
	/**
	 * 
	 * @param opco
	 * @return
	 */
	public Map<String,OpcoConfigs> getAllConfigs(OperatorCountry opco);
	
	/**
	 * 
	 * @param opcoid
	 * @return
	 */
	public Map<String,OpcoConfigs> getAllConfigs(Long opcoid);
	
	/**
	 * 
	 * @param opco
	 * @return
	 */
	public Map<String,OpcoTemplates> getAllTemplates(OperatorCountry opco, TemplateType templateType);
	
	/**
	 * 
	 * @param opcoid
	 * @return
	 */
	public Map<String,OpcoTemplates> getAllTemplates(Long opcoid, TemplateType templateType);

}
