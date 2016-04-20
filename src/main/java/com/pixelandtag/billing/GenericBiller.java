package com.pixelandtag.billing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.pixelandtag.billing.entities.BillerProfileTemplate;

public abstract class GenericBiller implements Biller {

	public static final String DEFAULT_TIMESTAMP_FORMAT = "yyyyMMdd";
	private DateFormat format = null;
	private Map<String,DateFormat> date_format_cache = new HashMap<String,DateFormat>();
	private Map<String,BillerProfileConfig> configuration;
	private Map<String,BillerProfileTemplate> templates = new HashMap<String,BillerProfileTemplate>();
	
	public GenericBiller(BillingConfigSet billingconfig){
		Map<String,BillerProfileConfig> configuration_ = billingconfig.getOpcoconfigs();
		setConfiguration(configuration_);
		setTemplates(billingconfig.getOpcotemplates());
	}

	public Map<String, BillerProfileConfig> getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Map<String, BillerProfileConfig> configuration) {
		this.configuration = configuration;
	}

	public Map<String, BillerProfileTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(Map<String, BillerProfileTemplate> templates) {
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
