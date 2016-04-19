package com.pixelandtag.cmp.ejb.api.billing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.pixelandtag.billing.BillerProfile;
import com.pixelandtag.billing.BillerProfileConfig;
import com.pixelandtag.billing.entities.BillerProfileTemplate;
import com.pixelandtag.cmp.dao.opco.BillerProfileConfigDAOI;
import com.pixelandtag.cmp.dao.opco.BillerProfileDAOI;
import com.pixelandtag.cmp.dao.opco.BillerProfileTemplateDAOI;
import com.pixelandtag.cmp.dao.opco.ProfileTemplatesDAOI;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileTemplate;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;

@Stateless
@Remote
public class BillerConfigsImpl implements BillerConfigsI {
	
	@Inject
	private BillerProfileConfigDAOI billerprofileConfigsDAO;
	
	@Inject 
	private BillerProfileTemplateDAOI billerProfileTemlatesDAO;
	
	@Inject
	private BillerProfileDAOI billerprofileDAO;
	
	public Map<String, BillerProfileConfig> getAllConfigs(BillerProfile profile){

		Map<String,BillerProfileConfig> configs = new HashMap<String,BillerProfileConfig>();
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("profile", profile);
		List<BillerProfileConfig> configlist =  billerprofileConfigsDAO.findByNamedQuery(BillerProfileConfig.NQ_FIND_BY_PROFILE, params);
		if(configlist!=null && configlist.size()>0){
			for(BillerProfileConfig config :configlist)
				configs.put(config.getName(), config);
			return configs;
		}else{
			return configs;
		}
		
	}
	
	

	public Map<String, BillerProfileTemplate> getAllTemplates(BillerProfile profile, TemplateType type){
		
		Map<String,BillerProfileTemplate> configs = new HashMap<String,BillerProfileTemplate>();
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("profile", profile);
		params.put("type", type);
		List<BillerProfileTemplate> configlist =  billerProfileTemlatesDAO.findByNamedQuery(BillerProfileTemplate.NQ_FIND_BY_PROFILE, params);
		if(configlist!=null && configlist.size()>0){
			for(BillerProfileTemplate config :configlist)
				configs.put(config.getName(), config);
			return configs;
		}else{
			return configs;
		}
	}
	
	
	public BillerProfile getActiveOpcoSenderReceiverProfile(OperatorCountry opco){
	
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("opco", opco);
		params.put("active", Boolean.TRUE);
		List<BillerProfile> senderprofiles =  billerprofileDAO.findByNamedQuery(OpcoSenderReceiverProfile.NQ_FIND_BY_OPCO, params,0,1);
		if(senderprofiles!=null && senderprofiles.size()>0)
			return senderprofiles.get(0);
		else
			return null;
		
	}

}
