package com.pixelandtag.smssenders;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.cmp.entities.customer.configs.ProfileTemplate;

public abstract class GenericSender implements Sender{
	
	
	private DateFormat format = null;
	private Map<String,DateFormat> date_format_cache = new HashMap<String,DateFormat>();
	private Map<String,ProfileConfigs> configuration;
	private Map<String,ProfileTemplate> templates = new HashMap<String,ProfileTemplate>();
	
	public GenericSender(SenderConfiguration configs) throws MessageSenderException{
		
		Map<String,ProfileConfigs> configuration_ = configs.getOpcoconfigs();
		setConfiguration(configuration_);
		setTemplates(configs.getOpcotemplates());
		validateMandatory();
		
	}

	public Map<String, ProfileConfigs> getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Map<String, ProfileConfigs> configuration) {
		this.configuration = configuration;
	}

	public Map<String, ProfileTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(Map<String, ProfileTemplate> templates) {
		this.templates = templates;
	}
	
	/**
	 * Converts a date to the desired format.
	 * Uses cacheing.
	 * 
	 * @param datestr
	 * @param dateformat - "yyyy-MM-dd HH:mm:ss"
	 * @return string - formatted date
	 * @throws ParseException
	 */
	public String dateToString(Date datestr, String dateformat) throws ParseException{
		if(date_format_cache.get(dateformat)==null){
			format = new SimpleDateFormat(dateformat);
			date_format_cache.put(dateformat, format);
		}
		return date_format_cache.get(dateformat).format(datestr);
	}
	

}
