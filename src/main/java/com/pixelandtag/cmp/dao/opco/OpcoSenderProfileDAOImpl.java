package com.pixelandtag.cmp.dao.opco;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.ConfigurationException;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.dao.generic.GenericDaoImpl;

public class OpcoSenderProfileDAOImpl extends  GenericDaoImpl<OpcoSenderReceiverProfile, Long> implements OpcoSenderProfileDAOI {
	
	public OpcoSenderReceiverProfile findActiveProfile(OperatorCountry opco) throws ConfigurationException{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("opco", opco);
		params.put("active", Boolean.TRUE);
		List<OpcoSenderReceiverProfile> profiles = findByNamedQuery(OpcoSenderReceiverProfile.NQ_FIND_BY_OPCO, params);
		if(profiles!=null && profiles.size()>1)
			throw new ConfigurationException("The opco with id "+opco.getId()+", and operator name "
		+opco.getOperator().getName()+", has more than one active sender receiver profile in the table opco_senderprofiles"
				+ " . There can only be one active sender profile config at a time. Please disable all except one!");
		return profiles!=null ? profiles.get(0) : null;
	}
}
