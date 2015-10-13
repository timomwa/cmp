package com.pixelandtag.cmp.ejb.api.sms;

import java.util.Date;

import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;

public interface OperatorCountryRulesEJBI {

	public Date findEarliestSendtime(OpcoSenderReceiverProfile opcosenderprofile) throws OpcoRuleException;

}
