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

import com.pixelandtag.cmp.dao.opco.OpcoConfigsDAOI;
import com.pixelandtag.cmp.dao.opco.OperatorCountryDAOI;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.OpcoConfigs;

@Stateless
@Remote
public class ConfigsEJBImpl implements ConfigsEJBI {
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Inject
	private OpcoConfigsDAOI opcoconfigsDAO;
	
	@Inject
	private OperatorCountryDAOI opcoDAO;
	
	@PostConstruct
	public void init(){
		opcoconfigsDAO.setEm(em);
		opcoDAO.setEm(em);
	}

	@Override
	public OpcoConfigs getConfig(Long opcoid, String name) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("opcoid", opcoid);
		params.put("name", name);
		List<OpcoConfigs> configs =  opcoconfigsDAO.findByNamedQuery(OpcoConfigs.NQ_FIND_BY_OPCOID_AND_NAME, params);
		if(configs!=null && configs.size()>0)
			return configs.get(0);
		else
			return null;
	}

	@Override
	public OpcoConfigs getConfig(OperatorCountry opco, String name) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("opco", opco);
		params.put("name", name);
		List<OpcoConfigs> configs =  opcoconfigsDAO.findByNamedQuery(OpcoConfigs.NQ_FIND_BY_OPCO_AND_NAME, params);
		if(configs!=null && configs.size()>0)
			return configs.get(0);
		else
			return null;
	}
	
	
	@Override
	public Map<String,OpcoConfigs> getAllConfigs(OperatorCountry opco){
		
		Map<String,OpcoConfigs> configs = new HashMap<String,OpcoConfigs>();
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("opco", opco);
		List<OpcoConfigs> configlist =  opcoconfigsDAO.findByNamedQuery(OpcoConfigs.NQ_FIND_BY_OPCO, params);
		if(configlist!=null && configlist.size()>0){
			for(OpcoConfigs config :configlist)
				configs.put(config.getName(), config);
			return configs;
		}else{
			return configs;
		}
	}
	
	
	@Override
	public Map<String,OpcoConfigs> getAllConfigs(Long opcoid){
		
		Map<String,OpcoConfigs> configs = new HashMap<String,OpcoConfigs>();
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("opcoid", opcoid);
		List<OpcoConfigs> configlist =  opcoconfigsDAO.findByNamedQuery(OpcoConfigs.NQ_FIND_BY_OPCOID, params);
		if(configlist!=null && configlist.size()>0){
			for(OpcoConfigs config :configlist)
				configs.put(config.getName(), config);
			return configs;
		}else{
			return configs;
		}
	}

}
