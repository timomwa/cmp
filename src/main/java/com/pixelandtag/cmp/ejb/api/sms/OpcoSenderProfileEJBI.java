package com.pixelandtag.cmp.ejb.api.sms;

import java.util.List;

import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderProfile;

public interface OpcoSenderProfileEJBI {

	public List<OpcoSenderProfile> getAllActiveProfiles();

}
