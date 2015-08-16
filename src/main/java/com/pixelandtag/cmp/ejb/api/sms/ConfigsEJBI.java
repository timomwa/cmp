package com.pixelandtag.cmp.ejb.api.sms;

import java.util.List;
import java.util.Map;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.cmp.entities.customer.configs.ProfileTemplate;
import com.pixelandtag.cmp.entities.customer.configs.SenderProfile;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;

public interface ConfigsEJBI {
	
	/**
	 * 
	 * @param profileid
	 * @param configname
	 * @return
	 */
	public ProfileConfigs getConfig(Long profileid, String configname);
	
	/**
	 * 
	 * @param profile
	 * @param configname
	 * @return
	 */
	public ProfileConfigs getConfig(SenderProfile profile, String configname);
	
	
	/**
	 * 
	 * @param profile
	 * @return
	 */
	public Map<String,ProfileConfigs> getAllConfigs(SenderProfile profile);
	
	/**
	 * 
	 * @param profileid
	 * @return
	 */
	public Map<String,ProfileConfigs> getAllConfigs(Long profileid);
	
	/**
	 * 
	 * @param profile
	 * @return
	 */
	public Map<String,ProfileTemplate> getAllTemplates(SenderProfile profile, TemplateType templateType);
	
	/**
	 * 
	 * @param profileid
	 * @return
	 */
	public Map<String,ProfileTemplate> getAllTemplates(Long profileid, TemplateType templateType);
	
	/**
	 * 
	 * @param opco
	 * @return
	 */
	public List<OpcoSenderProfile> getOpcoSenderProfiles(OperatorCountry opco,Boolean active);
	
	/**
	 * 
	 * @param opco
	 * @return
	 */
	public OpcoSenderProfile getActiveOpcoSenderProfile(OperatorCountry opco);

}
