package com.pixelandtag.billing;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.ws.rs.HttpMethod;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPClient;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPParam;
import com.pixelandtag.sms.mt.workerthreads.GenericHttpResp;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.PricePointException;
import com.pixelandtag.smssenders.Encryptor;
import com.pixelandtag.smssenders.EncryptorImpl;
import com.pixelandtag.smssenders.JsonUtilI;
import com.pixelandtag.smssenders.JsonUtilImpl;
import com.pixelandtag.smssenders.MessageSenderException;
import com.pixelandtag.smssenders.SenderResp;

public class HttpBiller extends GenericBiller {
	


	private GenericHTTPClient httpclient;
	
	
	@Inject
	private Encryptor encryptor = new EncryptorImpl();
	
	@Inject
	private static JsonUtilI jsonutil = new JsonUtilImpl();
	
	
	private Map<String,BillerProfileConfig> configuration;
	
	public HttpBiller(BillingConfigSet billingconfig) throws Exception {
		super(billingconfig);
		httpclient = new GenericHTTPClient(billingconfig.getOpcoconfigs().get(HTTP_BILLER_PROTOCOL).getValue());
	}

	private Logger logger = Logger.getLogger(getClass());

	@Override
	public SenderResp charge(Billable billable) throws BillerConfigException {
		
		SenderResp response = new SenderResp();
		
		String auth_header_value = "";
		String payload_template = "";
		
		GenericHTTPParam generic_HTTP_BILLER_parameters = new GenericHTTPParam();
		generic_HTTP_BILLER_parameters.setId(billable.getId());
		
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		if(this.configuration.get(HTTP_BILLER_TRANSACTION_ID_PARAM_NAME)!=null)
			qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_BILLER_TRANSACTION_ID_PARAM_NAME).getValue(), billable.getCp_tx_id()));//"cptxid"
		
		if(this.configuration.get(HTTP_BILLER_MSISDN_PARAM_NAME)!=null)
			qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_BILLER_MSISDN_PARAM_NAME).getValue(),billable.getMsisdn()));//"msisdn"
		
		if(this.configuration.get(HTTP_BILLER_PRICE_PARAM_NAME)!=null)
			qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_BILLER_PRICE_PARAM_NAME).getValue(),billable.getPrice().toString()));//"price"
		
		if(this.configuration.get(HTTP_BILLER_SHORTCODE_PARAM_NAME)!=null)
			qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_BILLER_SHORTCODE_PARAM_NAME).getValue(),billable.getShortcode()));//"shortcode"
		
		if(this.configuration.get(HTTP_BILLER_KEYWORD_PARAM_NAME)!=null)
			qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_BILLER_KEYWORD_PARAM_NAME).getValue(),billable.getKeyword()));//"keyword" 
		
		if(this.configuration.get(HTTP_BILLER_EVENT_TYPE_PARAM_NAME)!=null)
			qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_BILLER_EVENT_TYPE_PARAM_NAME).getValue(),billable.getEvent_type().getName()));//"event_type" 
		
		if(this.configuration.get(HTTP_BILLER_CP_ID_PARAM_NAME)!=null)
			qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_BILLER_CP_ID_PARAM_NAME).getValue(),billable.getCp_id()));//"cp_id" 
		
		if(this.configuration.get(HTTP_BILLER_OPERATION_PARAM_NAME)!=null)
			qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_BILLER_OPERATION_PARAM_NAME).getValue(),billable.getOperation()));//"operation" 
		
		if(this.configuration.get(HTTP_BILLER_SERVICE_ID_PARAM_NAME)!=null)
			qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_BILLER_SERVICE_ID_PARAM_NAME).getValue(),billable.getService_id()));//"serviceid" 
		
		if(this.configuration.get(HTTP_BILLER_PRICE_POINT_PARAM_NAME)!=null)
			try {
				qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_BILLER_PRICE_POINT_PARAM_NAME).getValue(),billable.getPricePointKeyword()));
			} catch (PricePointException e1) {
				logger.error(e1.getMessage(), e1);
				throw new BillerConfigException(e1.getMessage(), e1);
			}
		
		
		
		if(this.configuration.get(HTTP_BILLER_USE_HTTP_HEADER).getValue().equalsIgnoreCase("yes")){
			
			Map<String,String> headerParams = new HashMap<String,String>();
			
			BillerProfileConfig headerhasunameandpwd = this.configuration.get(HTTP_BILLER_HEADER_AUTH_HAS_USERNAME_AND_PASSWORD);
			
			if(headerhasunameandpwd==null || headerhasunameandpwd.getValue()==null || headerhasunameandpwd.getValue().isEmpty()){
				throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_HEADER_AUTH_HAS_USERNAME_AND_PASSWORD+"\" for this opco");
			}
			
			if(headerhasunameandpwd.getValue().equalsIgnoreCase("yes")){
				
				BillerProfileConfig headerauthusernameparam = this.configuration.get(HTTP_BILLER_HEADER_AUTH_USERNAME_PARAM_NAME);
				if(headerauthusernameparam==null || headerauthusernameparam.getValue()==null || headerauthusernameparam.getValue().isEmpty()){
					throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_HEADER_AUTH_USERNAME_PARAM_NAME+"\" for this opco");
				}
				BillerProfileConfig headerauthpasswordparam = this.configuration.get(HTTP_BILLER_HEADER_AUTH_PASSWORD_PARAM_NAME);
				if(headerauthpasswordparam==null || headerauthpasswordparam.getValue()==null || headerauthpasswordparam.getValue().isEmpty()){
					throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_HEADER_AUTH_PASSWORD_PARAM_NAME+"\" for this opco");
				}
				BillerProfileConfig encryptionmode = this.configuration.get(HTTP_BILLER_HEADER_AUTH_PASSWORD_ENCRYPTION_MODE);
				if(encryptionmode==null || encryptionmode.getValue()==null || encryptionmode.getValue().isEmpty()){
					throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_HEADER_AUTH_PASSWORD_ENCRYPTION_MODE+"\" for this opco");
				}
				
				String username_param_name = headerauthusernameparam.getValue();
				String password_param_name = headerauthpasswordparam.getValue();
				String encryptionmethod = encryptionmode.getValue();
				
				BillerProfileConfig usernamevalue = this.configuration.get(username_param_name);
				if(usernamevalue==null)
					throw new BillerConfigException("You''ve not set the value for\""+username_param_name+"\" as the username param");
				
				BillerProfileConfig passwordvalue = this.configuration.get(password_param_name);
				if(passwordvalue==null)
					throw new BillerConfigException("You''ve not set value for \""+password_param_name+"\" as the password param");
				
				
				String username = usernamevalue.getValue();
				String password = passwordvalue.getValue();
				
				BillerProfileConfig timestampFormat = this.configuration.get(HTTP_BILLER_TIMESTAMP_FORMAT);
				
				String httpBillertimeformat = timestampFormat!=null ? timestampFormat.getValue() : DEFAULT_TIMESTAMP_FORMAT;
				
				
				BillerProfileConfig pre_encode_tpl = this.configuration.get(HTTP_BILLER_PRE_ENCODE_TEMPLATE);
				
				String digest = "";
				try {
					if(pre_encode_tpl==null){
						digest = encryptor.encode(username,password, encryptionmethod);
					}else{
						String template = pre_encode_tpl.getValue();
						logger.info("  \n\n >>>>>>>>>>>>>>>>>> B41 :  template = "+template);
						template = template.replaceAll("\\$\\{PASSWORD\\}", Matcher.quoteReplacement(password));
						template = template.replaceAll("\\$\\{USERNAME\\}", Matcher.quoteReplacement(username));
						template = template.replaceAll("\\$\\{TIMESTAMP\\}", Matcher.quoteReplacement(  dateToString(billable.getTimeStamp(),httpBillertimeformat) ));
						logger.info("  \n\n >>>>>>>>>>>>>>>>>> B42 :  template = "+template);
						
						digest = encryptor.encode(template, encryptionmethod);
						logger.info("  \n\n >>>>>>>>>>>>>>>>>> AFTER :  digest = "+digest);
						
						
					}
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
					throw new BillerConfigException("Could not encrypt header params",e);
				}
				
				BillerProfileConfig authmethodparamname = this.configuration.get(HTTP_BILLER_HEADER_AUTH_METHOD_PARAM_NAME);
				if(authmethodparamname==null || authmethodparamname.getValue()==null ){
					throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_HEADER_AUTH_METHOD_PARAM_NAME+"\" for this opco");
				}
				
				String authmethod =  authmethodparamname.getValue();//e.g Basic				
				
				BillerProfileConfig httpheaderauthparam = this.configuration.get(HTTP_BILLER_HEADERAUTH_PARAM_NAME);
				if(httpheaderauthparam==null || httpheaderauthparam.getValue()==null || httpheaderauthparam.getValue().isEmpty()){
					throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_HEADERAUTH_PARAM_NAME+"\" for this opco");
				}
				
				
				
				if(this.configuration.get(HTTP_BILLER_HEADER_AUTH_HAS_MULTIPLE_KV_PAIRS).getValue().equalsIgnoreCase("yes")){
					
					for(Map.Entry<String,BillerProfileConfig> config : this.configuration.entrySet()){
						
						String key = config.getKey();
						
						if(key.trim().toLowerCase().startsWith(HTTP_BILLER_HEADER_AUTH_PARAM_PREFIX)){//All other authentication header params MUST have this prefix
							String param_name = key.split(HTTP_BILLER_HEADER_AUTH_PARAM_PREFIX)[1];
							
							if(config.getValue().getValue().contains("${TIMESTAMP}")){
								try {
									auth_header_value += " "+param_name+": \""+config.getValue().getValue().replace("${TIMESTAMP}", dateToString(billable.getTimeStamp(),httpBillertimeformat))+"\"";
								} catch (ParseException e) {
									logger.error(e.getMessage(),e);
									throw new BillerConfigException("Could format timestamp",e);
								}
							}else{
								auth_header_value += " "+param_name+": \""+config.getValue().getValue()+"\"";
							}
							
							
							
							headerParams.put(param_name, config.getValue().getValue());
						}
						
					}
					
					auth_header_value += " "+password_param_name+"=\""+digest+"\"";
					
				}else{
					
					auth_header_value =  ""+(authmethod+" "+digest).trim()+"";
					
				}
				
				
				for(Map.Entry<String,BillerProfileConfig> config : this.configuration.entrySet()){//Any other header param must start with the value HTTP_BILLER_HEADER_PREFIX
					
					String key = config.getKey();
					if(key.trim().toLowerCase().startsWith(HTTP_BILLER_HEADER_PREFIX)){
						String param_name = key.split(HTTP_BILLER_HEADER_PREFIX)[1];
						headerParams.put(param_name, config.getValue().getValue());
					}
				}
				
				headerParams.put(httpheaderauthparam.getValue(), auth_header_value.trim());
				
				
				
			}
			
			generic_HTTP_BILLER_parameters.setHeaderParams(headerParams);
			
		}
		
		
			
		
		
		if(this.configuration.get(HTTP_BILLER_HAS_PAYLOAD).getValue().equalsIgnoreCase("yes")){
			
			BillerProfileConfig httppayloadtemplate = this.configuration.get(HTTP_BILLER_PAYLOAD_TEMPLATE);
			if(httppayloadtemplate==null || httppayloadtemplate.getValue()==null || httppayloadtemplate.getValue().isEmpty()){
				throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_PAYLOAD_TEMPLATE+"\" for this opco");
			}
			String HTTP_BILLER_payload_template_name = httppayloadtemplate.getValue();
			
			payload_template = getTemplates().get(HTTP_BILLER_payload_template_name).getValue();
			
			if(payload_template==null || payload_template.isEmpty())
				throw new BillerConfigException("Template with name \""
						+HTTP_BILLER_payload_template_name+"\" for this profile ("+httppayloadtemplate.getProfile().toString()+") "
						+ "hasn't been found. First check if the name is "
						+ "correct in the opco_configs table and matches in the opco_templates table");
			
			
			
			for(Map.Entry<String,BillerProfileConfig> config : this.configuration.entrySet()){//Any other header param
				
				String key = config.getKey();
				if(key.trim().toLowerCase().startsWith(HTTP_BILLER_PAYLOAD_PARAM_PREFIX)){
					String param_name = key.split(HTTP_BILLER_PAYLOAD_PARAM_PREFIX)[1];
					payload_template = payload_template.replaceAll("\\$\\{"+param_name+"\\}", config.getValue().getValue());
				}
			}
			
			for(NameValuePair valuep : qparams){
				payload_template = payload_template.replaceAll("\\$\\{"+valuep.getName()+"\\}", valuep.getValue());
			}
			
			generic_HTTP_BILLER_parameters.setStringentity(payload_template);
			
		}else{
			
			generic_HTTP_BILLER_parameters.setHttpParams(qparams);
		}
		
		String url = this.configuration.get(HTTP_BILLER_BASE_URL).getValue();
		
		if(this.configuration.get(HTTP_BILLER_IS_RESTFUL).getValue().equalsIgnoreCase("yes")){
			for(Map.Entry<String,BillerProfileConfig> config : this.configuration.entrySet()){
				
				String key = config.getKey();
				if(key.trim().toLowerCase().startsWith(HTTP_BILLER_REST_PATH_PARAM_PREFIX)){
					String param_name = key.split(HTTP_BILLER_REST_PATH_PARAM_PREFIX)[1];
					try {
						String basicparam = getValueFromqparams(qparams,param_name);
						if(basicparam!=null)
							url = url.replaceAll("\\$\\{"+param_name+"\\}", URLEncoder.encode(basicparam,"UTF-8"));
						url = url.replaceAll("\\$\\{"+param_name+"\\}", config.getValue().getValue());
					} catch (UnsupportedEncodingException e) {
						logger.error(e.getMessage(),e);
						throw new BillerConfigException("Could not encode path param",e);
					}
				}
			}
			
			for(NameValuePair valuep : qparams){
				url = url.replaceAll("\\$\\{"+valuep.getName()+"\\}", valuep.getValue());
			}
		}
		
		generic_HTTP_BILLER_parameters.setUrl(url);
		
		BillerProfileConfig httpmethod = this.configuration.get(HTTP_BILLER_REQUEST_METHOD);
		
		if(httpmethod!=null)
			generic_HTTP_BILLER_parameters.setHttpmethod(httpmethod.getValue().trim().toUpperCase());
		else
			logger.warn("No configuration for \""+HTTP_BILLER_REQUEST_METHOD
					+"\" has been provided, so we will use "
					+ "HTTP POST as default. If you wish to override this, "
					+ "provide this config in db");
		logger.debug(generic_HTTP_BILLER_parameters.toString() );
		
		GenericHttpResp resp = httpclient.call(generic_HTTP_BILLER_parameters);
		
		logger.debug("\n\n\t\t>>>url>>> : "+url
				+"\n\t\tauth_header_value = "+auth_header_value+
				"\n\t\t>>>payload>>> : "+payload_template+
				"\n\n\t\t>>>response>>> : "+resp.getBody()+"\n"
			  + "\n\t\t>>>response code>>> : "+resp.getResp_code()
			    + "\n\t\t>>>response contenttype>>> : "+resp.getContenttype()
						+ "\n\n");
		
		response.setRespcode(String.valueOf(resp.getResp_code()));
		if(resp.getResp_code()>=200 && resp.getResp_code()<=299 )//All http 200 series are treated as success
			response.setSuccess(Boolean.TRUE);
		
		
		BillerProfileConfig expected_contenttype = this.configuration.get(HTTP_BILLER_EXPECTED_CONTENTTYPE);
		if((expected_contenttype==null || expected_contenttype.getValue()==null || expected_contenttype.getValue().trim().isEmpty()) && (resp.getContenttype()==null || resp.getContenttype().trim().isEmpty() )){
			throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_EXPECTED_CONTENTTYPE+"\" for this opco. The call didn't return any contenttype either");
		}
		
		//How do we parse the responses and get meaningful data that will match com.pixelandtag.smssenders.PlainHttpSender.sendSMS(MTsms).response?
		if((resp.getContenttype()!=null && resp.getContenttype().toLowerCase().trim().contains("json")) || expected_contenttype.getValue().trim().equalsIgnoreCase("json")){
			try {
				JSONObject jsonobject = new JSONObject(resp.getBody());
				
				BillerProfileConfig pathtorefval = this.configuration.get(HTTP_BILLER_RESP_REF_VALUE_KEY);
				if(pathtorefval==null || pathtorefval.getValue()==null || pathtorefval.getValue().isEmpty()){
					throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_RESP_REF_VALUE_KEY+"\" for this opco");
				}
				
				BillerProfileConfig respmsgcnf_failure = this.configuration.get(HTTP_BILLER_RESP_RESP_MSG_KEY_FAILURE);
				if(respmsgcnf_failure==null || respmsgcnf_failure.getValue()==null || respmsgcnf_failure.getValue().isEmpty()){
					throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_RESP_RESP_MSG_KEY_FAILURE+"\" for this opco");
				}
				
				BillerProfileConfig respmsgcnf_success = this.configuration.get(HTTP_BILLER_RESP_RESP_MSG_KEY_SUCCESS);
				if(respmsgcnf_success==null || respmsgcnf_success.getValue()==null || respmsgcnf_success.getValue().isEmpty()){
					throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_RESP_RESP_MSG_KEY_SUCCESS+"\" for this opco");
				}
				
				BillerProfileConfig success_string = this.configuration.get(HTTP_BILLER_RESP_SUCCESS_STRING);
				if(success_string==null || success_string.getValue()==null || success_string.getValue().isEmpty()){
					throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_RESP_SUCCESS_STRING+"\" for this opco");
				}
				
				jsonutil.loadJson(jsonobject);
				response.setRefvalue((String)jsonutil.getValue(pathtorefval.getValue()));
				String respmsg = (String)jsonutil.getValue(respmsgcnf_failure.getValue());
				if(respmsg==null || respmsg.trim().isEmpty())
					respmsg =   (String)jsonutil.getValue(respmsgcnf_success.getValue());
				
				response.setResponseMsg(respmsg);
				response.setSuccess(  respmsg.contains(  success_string.getValue().trim()  )    );
				jsonutil.reset();
				
			} catch (JSONException e) {
				logger.error(e.getMessage(),e);
				throw new BillerConfigException("Could not parse the json response json -> "+resp.getBody(),e);
			}
			
		}else if((resp.getContenttype()!=null &&  resp.getContenttype().toLowerCase().trim().contains("xml")) || expected_contenttype.getValue().trim().equalsIgnoreCase("xml")){
			
			
			BillerProfileConfig pathtorefval = this.configuration.get(HTTP_BILLER_RESP_REF_VALUE_KEY);
			if(pathtorefval==null || pathtorefval.getValue()==null || pathtorefval.getValue().isEmpty()){
				throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_RESP_REF_VALUE_KEY+"\" for this opco");
			}
			
			BillerProfileConfig respmsgcnf_failure = this.configuration.get(HTTP_BILLER_RESP_RESP_MSG_KEY_FAILURE);
			if(respmsgcnf_failure==null || respmsgcnf_failure.getValue()==null || respmsgcnf_failure.getValue().isEmpty()){
				throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_RESP_RESP_MSG_KEY_FAILURE+"\" for this opco");
			}
			
			BillerProfileConfig respmsgcnf_success = this.configuration.get(HTTP_BILLER_RESP_RESP_MSG_KEY_SUCCESS);
			if(respmsgcnf_success==null || respmsgcnf_success.getValue()==null || respmsgcnf_success.getValue().isEmpty()){
				throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_RESP_RESP_MSG_KEY_SUCCESS+"\" for this opco");
			}
			
			BillerProfileConfig success_string = this.configuration.get(HTTP_BILLER_RESP_SUCCESS_STRING);
			if(success_string==null || success_string.getValue()==null || success_string.getValue().isEmpty()){
				throw new BillerConfigException("No configuration set for \""+HTTP_BILLER_RESP_SUCCESS_STRING+"\" for this opco");
			}
			
			response.setRefvalue(getValue(resp.getBody(),pathtorefval.getValue()));
			String respmsg = getValue(resp.getBody(),respmsgcnf_failure.getValue());
			if(respmsg==null || respmsg.trim().isEmpty())
				respmsg =  getValue(resp.getBody(),respmsgcnf_success.getValue());
			response.setResponseMsg(respmsg);
			response.setSuccess(  respmsg.contains(  success_string.getValue().trim()  )    );
			
		}else{
			
			response.setRefvalue(String.valueOf(billable.getCp_id()));
			response.setResponseMsg(resp.getBody());
		}
		
		
		return response;

	}
	
	
	
	/**
	 * 
	 * @param xml
	 * @param tagname
	 * @return
	 */
	private String getValue(String xml,String tagname) {
		String startTag = "<"+tagname+">";
		String endTag = "</"+tagname+">";
		int start = xml.indexOf(startTag)+startTag.length();
		int end  = xml.indexOf(endTag);
		if(start<0 || end<0)
			return "";
		return xml.substring(start, end);
	}
	
	private String getValueFromqparams(List<NameValuePair> qparams,String key) {
		for(NameValuePair valuep : qparams)
			if(valuep.getName().equals(key))
				return valuep.getValue();
		return null;
	}

	@Override
	public void validateMandatory() throws BillerConfigException {
		
		String[] mandatory = {HTTP_BILLER_PROTOCOL,
							  HTTP_BILLER_BASE_URL,
							  HTTP_BILLER_SHORTCODE_PARAM_NAME,
							  HTTP_BILLER_MSISDN_PARAM_NAME,
							  HTTP_BILLER_SMS_MSG_PARAM_NAME,
							  HTTP_BILLER_USE_HTTP_HEADER,
							  HTTP_BILLER_HAS_PAYLOAD,
							  HTTP_BILLER_IS_RESTFUL,
							  HTTP_BILLER_HEADER_AUTH_HAS_MULTIPLE_KV_PAIRS};
		for(String param : mandatory)
			if(this.configuration.get(param)==null)
				throw new BillerConfigException("No billing configuration set for \""+param+"\" for this opco");
	}

	public Map<String, BillerProfileConfig> getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Map<String, BillerProfileConfig> configuration) {
		this.configuration = configuration;
	}

	
	
}
