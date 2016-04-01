package com.pixelandtag.cmp.dao.opco;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.ConfigurationException;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileType;
import com.pixelandtag.dao.generic.GenericDAO;

public interface OpcoSenderProfileDAOI extends GenericDAO<OpcoSenderReceiverProfile, Long> {

	public OpcoSenderReceiverProfile findActiveProfile(OperatorCountry opco) throws ConfigurationException;
	public OpcoSenderReceiverProfile findActiveReceiverOrTranceiver(OperatorCountry opco, ProfileType type) throws ConfigurationException;

}
