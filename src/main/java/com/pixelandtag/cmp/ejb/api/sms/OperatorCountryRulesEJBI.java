package com.pixelandtag.cmp.ejb.api.sms;

import java.util.Date;

import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.OperatorCountryRules;

public interface OperatorCountryRulesEJBI {

	public Date findEarliestSendtime(OpcoSenderReceiverProfile opcosenderprofile) throws OpcoRuleException;

	public OperatorCountryRules getConfig(String string, OperatorCountry opco);


}
