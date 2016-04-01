package com.pixelandtag.cmp.dao.opco;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.ConfigurationException;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileType;
import com.pixelandtag.dao.generic.GenericDaoImpl;

public class OpcoSenderProfileDAOImpl extends  GenericDaoImpl<OpcoSenderReceiverProfile, Long> implements OpcoSenderProfileDAOI {
	
	@Override
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
	
	
	@SuppressWarnings("unchecked")
	@Override
	public OpcoSenderReceiverProfile findActiveReceiverOrTranceiver(OperatorCountry opco, ProfileType type) throws ConfigurationException{
		Query qry = em.createQuery("select osp from OpcoSenderReceiverProfile osp where osp.opco=:opco AND osp.active=:active AND (osp.profile.profiletype=:profiletype OR osp.profile.profiletype=:profiletypealt)  order by osp.pickorder desc");
		qry.setParameter("opco", opco);
		qry.setParameter("active", Boolean.TRUE);
		qry.setParameter("profiletype", type);
		qry.setParameter("profiletypealt", ProfileType.TRANCEIVER);
		List<OpcoSenderReceiverProfile> profiles =  qry.getResultList();
		if(profiles!=null && profiles.size()>1)
			throw new ConfigurationException("The opco with id "+opco.getId()+", and operator name "
		+opco.getOperator().getName()+", has more than one active sender receiver profile in the table opco_senderprofiles"
				+ " . There can only be one active sender profile config at a time. Please disable all except one!");
		return profiles!=null ? profiles.get(0) : null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<OpcoSenderReceiverProfile> getAllActiveSenderOrTranceiverProfiles(){
		Query qry = em.createQuery("select osp from OpcoSenderReceiverProfile osp where osp.active=:active AND (osp.profile.profiletype=:profiletype OR osp.profile.profiletype=:profiletypealt)  order by osp.pickorder desc");
		qry.setParameter("active", Boolean.TRUE);
		qry.setParameter("profiletype", ProfileType.SENDER);
		qry.setParameter("profiletypealt", ProfileType.TRANCEIVER);
		return qry.getResultList();
	}
}
