package com.pixelandtag.cmp.ejb.api.sms;

import java.util.List;

import com.pixelandtag.cmp.entities.MOProcessor;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;

public interface OpcoSenderProfileEJBI {

	public List<OpcoSenderReceiverProfile> getAllActiveProfiles();

	public OpcoSenderReceiverProfile getActiveProfileForOpco(String opcocode);
	
	public OpcoSenderReceiverProfile getActiveProfileForOpco(Long opcoid);
	
	public MOProcessor getMOProcessorByTelcoShortcodeAndKeyword(String keyword, String shortcode, OperatorCountry opco);

}
