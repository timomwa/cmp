package com.pixelandtag.smssenders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.pixelandtag.cmp.entities.customer.configs.OpcoConfigs;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPClient;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPParam;

public class PlainHttpSender implements Sender {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Inject
	private Encryptor encryptor;
	
	private Map<String,OpcoConfigs> configuration;
	private GenericHTTPClient httpclient;
	private List<String> mandatoryparams = new ArrayList<String>();
	
	
	public PlainHttpSender(Map<String,OpcoConfigs> configuration_) throws Exception{
		setConfiguration(configuration_);
		validateMandatory();
		httpclient = new GenericHTTPClient(configuration_.get(HTTP_PROTOCOL).getValue());
	}

	private void validateMandatory() throws MessageSenderException{
		mandatoryparams.add(HTTP_PROTOCOL);
		mandatoryparams.add(HTTP_BASE_URL);
		mandatoryparams.add(HTTP_TRANSACTION_ID_PARAM_NAME);
		mandatoryparams.add(HTTP_SHORTCODE_PARAM_NAME);
		mandatoryparams.add(HTTP_MSISDN_PARAM_NAME);
		mandatoryparams.add(HTTP_SMS_MSG_PARAM_NAME);
		mandatoryparams.add(HTTP_USE_HTTP_HEADER);
		mandatoryparams.add(HTTP_HAS_PAYLOAD);
		mandatoryparams.add(HTTP_HEADER_AUTH_HAS_MULTIPLE_KV_PAIRS);
		for(String param : mandatoryparams)
			if(this.configuration.get(param)==null)
				throw new MessageSenderException("No configuration set for \""+param+"\" for this opco");
	}

	@Override
	public boolean sendSMS(MTsms mtsms) throws MessageSenderException {
		
		
		GenericHTTPParam generic_http_parameters = new GenericHTTPParam();
		generic_http_parameters.setUrl(this.configuration.get("url").getValue());
		generic_http_parameters.setId(mtsms.getId());
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_TRANSACTION_ID_PARAM_NAME).getValue(), mtsms.getCMP_Txid().toString()));//"cptxid"
		qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_SHORTCODE_PARAM_NAME).getValue(),mtsms.getShortcode()));//"sourceaddress"	
		qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_MSISDN_PARAM_NAME).getValue(),mtsms.getMsisdn()));//"msisdn"
		qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_SMS_MSG_PARAM_NAME).getValue(),mtsms.getSms()));//"sms"
		
		
		if(this.configuration.get(HTTP_USE_HTTP_HEADER).getValue().equalsIgnoreCase("true")){
			
			Map<String,String> headerParams = new HashMap<String,String>();
			
			if(this.configuration.get(HTTP_HEADER_AUTH_HAS_USERNAME_AND_PASSWORD).getValue().equalsIgnoreCase("true")){
				
				String username_param_name = this.configuration.get(HTTP_HEADER_AUTH_USERNAME_PARAM_NAME).getValue();
				String password_param_name = this.configuration.get(HTTP_HEADER_AUTH_PASSWORD_PARAM_NAME).getValue();
				String encryptionmethod = this.configuration.get(HTTP_HEADER_AUTH_PASSWORD_ENCRYPTION_MODE).getValue();
				
				String username = this.configuration.get(username_param_name).getValue();
				String password = this.configuration.get(password_param_name).getValue();
				
				String digest = "";
				try {
					digest = encryptor.encrypt(username,password, encryptionmethod);
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
					throw new MessageSenderException("Could not encrypt header params",e);
				}
				
				String authmethod = this.configuration.get(HTTP_HEADER_AUTH_METHOD_PARAM_NAME).getValue();//e.g Basic
				
				String auth_header_value = "";
				
				if(this.configuration.get(HTTP_HEADER_AUTH_HAS_MULTIPLE_KV_PAIRS).getValue().equalsIgnoreCase("yes")){
					
					for(Map.Entry<String,OpcoConfigs> config : this.configuration.entrySet()){
						
						String key = config.getKey();
						
						if(key.trim().toLowerCase().startsWith(HTTP_HEADER_AUTH_PARAM_PREFIX)){//All other authentication header params MUST have this prefix
							String param_name = key.split(HTTP_HEADER_AUTH_PARAM_PREFIX)[1];
							auth_header_value += " "+param_name+"=\""+config.getValue().getValue()+"\"";
						}
						
					}
					
					auth_header_value += " "+password_param_name+"=\""+digest+"\"";
					
				}else{
					
					auth_header_value =  authmethod+" "+digest;
					
				}
				
				for(Map.Entry<String,OpcoConfigs> config : this.configuration.entrySet()){//Any other header param must start with the value HTTP_HEADER_PREFIX
					
					String key = config.getKey();
					if(key.trim().toLowerCase().startsWith(HTTP_HEADER_PREFIX)){
						String param_name = key.split(HTTP_HEADER_PREFIX)[1];
						headerParams.put(param_name, config.getValue().getValue());
					}
				}
				
				headerParams.put(this.configuration.get(HTTP_HEADER_AUTH_PARAM_NAME).getValue(),auth_header_value.trim());
				
			}
			
			
			if(this.configuration.get(HTTP_HAS_PAYLOAD).getValue().equalsIgnoreCase("yes")){
				
				String payload_template = this.configuration.get(HTTP_PAYLOAD_TEMPLATE).getValue();
				
				for(Map.Entry<String,OpcoConfigs> config : this.configuration.entrySet()){//Any other header param
					
					String key = config.getKey();
					if(key.trim().toLowerCase().startsWith(HTTP_PAYLOAD_PARAM_PREFIX)){
						String param_name = key.split(HTTP_PAYLOAD_PARAM_PREFIX)[1];
						payload_template = payload_template.replaceAll("\\$\\{"+param_name+"\\}", config.getValue().getValue());
					}
				}
				
				generic_http_parameters.setStringentity(payload_template);
				
			}
			
			String url = this.configuration.get(HTTP_BASE_URL).getValue();
			
			if(this.configuration.get(HTTP_IS_RESTFUL).getValue().equalsIgnoreCase("yes")){
				//How do we append params to the REST URL ?
			}
			
			generic_http_parameters.setUrl(url);
			generic_http_parameters.setHttpParams(qparams);
			
		}
		
		
		
		
		return false;
		
	}

	public void setConfiguration(Map<String, OpcoConfigs> configs) {
		this.configuration = configs;
	}

}
