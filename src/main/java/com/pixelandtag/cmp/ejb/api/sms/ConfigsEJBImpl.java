package com.pixelandtag.cmp.ejb.api.sms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.pixelandtag.cmp.dao.opco.OpcoSenderProfileDAOI;
import com.pixelandtag.cmp.dao.opco.OpcoIPAddressMapDAOI;
import com.pixelandtag.cmp.dao.opco.ProfileConfigsDAOI;
import com.pixelandtag.cmp.dao.opco.ProfileTemplatesDAOI;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.ConfigurationException;
import com.pixelandtag.cmp.entities.customer.configs.OpcoIPAddressMap;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.cmp.entities.customer.configs.ProfileTemplate;
import com.pixelandtag.cmp.entities.customer.configs.ProfileType;
import com.pixelandtag.cmp.entities.customer.configs.SenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;

@Stateless
@Remote
public class ConfigsEJBImpl implements ConfigsEJBI {
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Inject
	private ProfileConfigsDAOI profileconfigsDAO;
	
	@Inject
	private OpcoSenderProfileDAOI opcoprofilesDAO;
	
	
	@Inject
	private OpcoIPAddressMapDAOI opcoipAddressMapDAO;
	
	
	@Inject 
	ProfileTemplatesDAOI profileTemlatesDAO;
	

	@Override
	public ProfileConfigs getConfig(Long opcoid, String name) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("opcoid", opcoid);
		params.put("name", name);
		List<ProfileConfigs> configs =  profileconfigsDAO.findByNamedQuery(ProfileConfigs.NQ_FIND_BY_PROFILEID_AND_NAME, params);
		if(configs!=null && configs.size()>0)
			return configs.get(0);
		else
			return null;
	}

	@Override
	public ProfileConfigs getConfig(SenderReceiverProfile profile, String name) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("profile", profile);
		params.put("name", name);
		List<ProfileConfigs> configs =  profileconfigsDAO.findByNamedQuery(ProfileConfigs.NQ_FIND_BY_PROFILE_AND_NAME, params);
		if(configs!=null && configs.size()>0)
			return configs.get(0);
		else
			return null;
	}
	
	
	@Override
	public Map<String,ProfileConfigs> getAllConfigs(SenderReceiverProfile profile){
		
		Map<String,ProfileConfigs> configs = new HashMap<String,ProfileConfigs>();
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("profile", profile);
		List<ProfileConfigs> configlist =  profileconfigsDAO.findByNamedQuery(ProfileConfigs.NQ_FIND_BY_PROFILE, params);
		if(configlist!=null && configlist.size()>0){
			for(ProfileConfigs config :configlist)
				configs.put(config.getName(), config);
			return configs;
		}else{
			return configs;
		}
	}
	
	
	@Override
	public Map<String,ProfileConfigs> getAllConfigs(Long profileid){
		
		Map<String,ProfileConfigs> configs = new HashMap<String,ProfileConfigs>();
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("profileid", profileid);
		List<ProfileConfigs> configlist =  profileconfigsDAO.findByNamedQuery(ProfileConfigs.NQ_FIND_BY_PROFILEID, params);
		if(configlist!=null && configlist.size()>0){
			for(ProfileConfigs config :configlist)
				configs.put(config.getName(), config);
			return configs;
		}else{
			return configs;
		}
	}

	@Override
	public Map<String, ProfileTemplate> getAllTemplates(SenderReceiverProfile profile, TemplateType type) {
		
		Map<String,ProfileTemplate> configs = new HashMap<String,ProfileTemplate>();
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("profile", profile);
		params.put("type", type);
		List<ProfileTemplate> configlist =  profileTemlatesDAO.findByNamedQuery(ProfileTemplate.NQ_FIND_BY_PROFILE, params);
		if(configlist!=null && configlist.size()>0){
			for(ProfileTemplate config :configlist)
				configs.put(config.getName(), config);
			return configs;
		}else{
			return configs;
		}
	}

	@Override
	public Map<String, ProfileTemplate> getAllTemplates(Long profileid, TemplateType type) {

		Map<String,ProfileTemplate> configs = new HashMap<String,ProfileTemplate>();
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("profileid", profileid);
		params.put("type", type);
		List<ProfileTemplate> configlist =  profileTemlatesDAO.findByNamedQuery(ProfileTemplate.NQ_FIND_BY_PROFILEID, params);
		if(configlist!=null && configlist.size()>0){
			for(ProfileTemplate config :configlist)
				configs.put(config.getName(), config);
			return configs;
		}else{
			return configs;
		}
	}
	
	@Override
	public List<OpcoSenderReceiverProfile> getOpcoSenderProfiles(OperatorCountry opco, Boolean active){
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("opco", opco);
		params.put("active", active);
		return  opcoprofilesDAO.findByNamedQuery(OpcoSenderReceiverProfile.NQ_FIND_BY_OPCO, params);
		
	}
	
	
	@Override
	public OpcoSenderReceiverProfile getActiveOpcoSenderReceiverProfile(OperatorCountry opco){
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("opco", opco);
		params.put("active", Boolean.TRUE);
		List<OpcoSenderReceiverProfile> senderprofiles =  opcoprofilesDAO.findByNamedQuery(OpcoSenderReceiverProfile.NQ_FIND_BY_OPCO, params,0,1);
		if(senderprofiles!=null && senderprofiles.size()>0)
			return senderprofiles.get(0);
		else
			return null;
		
	}
	
	
	@Override
	public OpcoSenderReceiverProfile findActiveProfileByTypeOrTranceiver(OperatorCountry opco, ProfileType type) throws ConfigurationException{
		return opcoprofilesDAO.findActiveReceiverOrTranceiver(opco, type);
	}
	
	
	
	public OperatorCountry getOperatorByIpAddress(String ip_address){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("ipaddress", ip_address);
		List<OpcoIPAddressMap> ipaddressmap = opcoipAddressMapDAO.findByNamedQuery(OpcoIPAddressMap.NQ_FIND_BY_IP_ADDRESS, params);
		if(ipaddressmap!=null && ipaddressmap.size()>0)
			return ipaddressmap.get(0).getOpco();
		else
			return null;
	}

}
