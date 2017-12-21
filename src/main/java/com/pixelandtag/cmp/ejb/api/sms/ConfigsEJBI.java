package com.pixelandtag.cmp.ejb.api.sms;

import java.util.List;
import java.util.Map;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.ConfigurationException;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.cmp.entities.customer.configs.ProfileTemplate;
import com.pixelandtag.cmp.entities.customer.configs.ProfileType;
import com.pixelandtag.cmp.entities.customer.configs.SenderReceiverProfile;
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
	public ProfileConfigs getConfig(SenderReceiverProfile profile, String configname);
	
	
	/**
	 * 
	 * @param profile
	 * @return
	 */
	public Map<String,ProfileConfigs> getAllConfigs(SenderReceiverProfile profile);
	
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
	public Map<String,ProfileTemplate> getAllTemplates(SenderReceiverProfile profile, TemplateType templateType);
	
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
	public List<OpcoSenderReceiverProfile> getOpcoSenderProfiles(OperatorCountry opco,Boolean active);
	
	/**
	 * 
	 * @param opco
	 * @return
	 */
	public OpcoSenderReceiverProfile getActiveOpcoSenderReceiverProfile(OperatorCountry opco);

	/**
	 * 
	 * @param ip_address
	 * @return
	 */
	public OperatorCountry getOperatorByIpAddress(String ip_address);
	
	/**
	 * 
	 * @param opco
	 * @param type
	 * @return
	 */
	public OpcoSenderReceiverProfile findActiveProfileByTypeOrTranceiver(OperatorCountry opco, ProfileType type) throws ConfigurationException;
	
	
	
	

}
