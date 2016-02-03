package com.pixelandtag.cmp.ejb.api.sms;

import java.util.Map;

import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.MOProcessor;
import com.pixelandtag.cmp.entities.MessageLog;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.ConfigurationException;

public interface ProcessorResolverEJBI {

	public IncomingSMS processMo(Map<String, String> incomingparams) throws ConfigurationException;
	
	public IncomingSMS populateProcessorDetails(IncomingSMS incomingsms);

	public IncomingSMS processMo(IncomingSMS incomingsms);

	public MOProcessor getMOProcessor(String shortcode);

	public MessageLog saveMessageLog(MessageLog messageLog) throws Exception;
	
	
	
	

}
