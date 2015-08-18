package com.pixelandtag.cmp.ejb.api.sms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.pixelandtag.cmp.dao.opco.OpcoSenderProfileDAOI;
import com.pixelandtag.cmp.dao.opco.ProfileConfigsDAOI;
import com.pixelandtag.cmp.dao.opco.ProfileTemplatesDAOI;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.cmp.entities.customer.configs.ProfileTemplate;
import com.pixelandtag.cmp.entities.customer.configs.SenderProfile;
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
	public ProfileConfigs getConfig(SenderProfile profile, String name) {
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
	public Map<String,ProfileConfigs> getAllConfigs(SenderProfile profile){
		
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
	public Map<String, ProfileTemplate> getAllTemplates(SenderProfile profile, TemplateType type) {
		
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
	public List<OpcoSenderProfile> getOpcoSenderProfiles(OperatorCountry opco, Boolean active){
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("opco", opco);
		params.put("active", active);
		return  opcoprofilesDAO.findByNamedQuery(OpcoSenderProfile.NQ_FIND_BY_OPCO, params);
		
	}
	
	
	@Override
	public OpcoSenderProfile getActiveOpcoSenderProfile(OperatorCountry opco){
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("opco", opco);
		params.put("active", Boolean.TRUE);
		List<OpcoSenderProfile> senderprofiles =  opcoprofilesDAO.findByNamedQuery(OpcoSenderProfile.NQ_FIND_BY_OPCO, params,0,1);
		if(senderprofiles!=null && senderprofiles.size()>0)
			return senderprofiles.get(0);
		else
			return null;
		
	}

}
