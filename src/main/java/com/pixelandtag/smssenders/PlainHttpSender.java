package com.pixelandtag.smssenders;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPClient;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPParam;
import com.pixelandtag.sms.mt.workerthreads.GenericHttpResp;

public class PlainHttpSender extends GenericSender {
	
	

	private Logger logger = Logger.getLogger(getClass());
	
	@Inject
	private Encryptor encryptor = new EncryptorImpl();
	
	@Inject
	private static JsonUtilI jsonutil;
	
	private Map<String,ProfileConfigs> configuration;
	private GenericHTTPClient httpclient;
	
	
	
	public PlainHttpSender(SenderConfiguration configs) throws Exception{
		super(configs);
		httpclient = new GenericHTTPClient(configs.getOpcoconfigs().get(HTTP_PROTOCOL).getValue());
	}
	
	
	@Override
	public void validateMandatory() throws MessageSenderException{
		List<String> mandatoryparams = new ArrayList<String>();
		mandatoryparams.add(HTTP_PROTOCOL);
		mandatoryparams.add(HTTP_BASE_URL);
		mandatoryparams.add(HTTP_SHORTCODE_PARAM_NAME);
		mandatoryparams.add(HTTP_MSISDN_PARAM_NAME);
		mandatoryparams.add(HTTP_SMS_MSG_PARAM_NAME);
		mandatoryparams.add(HTTP_USE_HTTP_HEADER);
		mandatoryparams.add(HTTP_HAS_PAYLOAD);
		mandatoryparams.add(HTTP_IS_RESTFUL);
		mandatoryparams.add(HTTP_HEADER_AUTH_HAS_MULTIPLE_KV_PAIRS);
		mandatoryparams.add(HTTP_ALLOW_SENDING_BLANK_TEXT);
		for(String param : mandatoryparams)
			if(this.configuration.get(param)==null)
				throw new MessageSenderException("No configuration set for \""+param+"\" for this opco");
	}

	@Override
	public SenderResp sendSMS(OutgoingSMS outgoingsms) throws MessageSenderException {
		
		if(this.configuration.get(HTTP_SHORTCODE_PARAM_NAME)!=null)
			if(this.configuration.get(HTTP_ALLOW_SENDING_BLANK_TEXT).getValue().equalsIgnoreCase("yes"))
				if(outgoingsms.getSms()==null || outgoingsms.getSms().isEmpty())
					throw new MessageSenderException("SMS to be sent is null. We can't send null or empty messages");
		
		SenderResp response = new SenderResp();
		
		String auth_header_value = "";
		String payload_template = "";
		
		GenericHTTPParam generic_http_parameters = new GenericHTTPParam();
		generic_http_parameters.setId(outgoingsms.getId());
		
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		if(this.configuration.get(HTTP_TRANSACTION_ID_PARAM_NAME)!=null){
			String transaction_id_ = outgoingsms.getOpco_tx_id();
			if(transaction_id_==null || transaction_id_.trim().isEmpty())
				transaction_id_ = outgoingsms.getCmp_tx_id();
			qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_TRANSACTION_ID_PARAM_NAME).getValue(), transaction_id_));//"cptxid"
		}
		
		qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_SHORTCODE_PARAM_NAME).getValue(),outgoingsms.getShortcode()));//"sourceaddress"	
		qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_MSISDN_PARAM_NAME).getValue(),outgoingsms.getMsisdn()));//"msisdn"
		qparams.add(new BasicNameValuePair(this.configuration.get(HTTP_SMS_MSG_PARAM_NAME).getValue(),outgoingsms.getSms()));//"sms"
		
		if(this.configuration.get(HTTP_USE_HTTP_HEADER).getValue().equalsIgnoreCase("yes")){
			
			Map<String,String> headerParams = new HashMap<String,String>();
			
			ProfileConfigs headerhasunameandpwd = this.configuration.get(HTTP_HEADER_AUTH_HAS_USERNAME_AND_PASSWORD);
			
			if(headerhasunameandpwd==null || headerhasunameandpwd.getValue()==null || headerhasunameandpwd.getValue().isEmpty()){
				throw new MessageSenderException("No configuration set for \""+HTTP_HEADER_AUTH_HAS_USERNAME_AND_PASSWORD+"\" for this opco");
			}
			
			if(headerhasunameandpwd.getValue().equalsIgnoreCase("yes")){
				
				ProfileConfigs headerauthusernameparam = this.configuration.get(HTTP_HEADER_AUTH_USERNAME_PARAM_NAME);
				if(headerauthusernameparam==null || headerauthusernameparam.getValue()==null || headerauthusernameparam.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_HEADER_AUTH_USERNAME_PARAM_NAME+"\" for this opco");
				}
				ProfileConfigs headerauthpasswordparam = this.configuration.get(HTTP_HEADER_AUTH_PASSWORD_PARAM_NAME);
				if(headerauthpasswordparam==null || headerauthpasswordparam.getValue()==null || headerauthpasswordparam.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_HEADER_AUTH_PASSWORD_PARAM_NAME+"\" for this opco");
				}
				ProfileConfigs encryptionmode = this.configuration.get(HTTP_HEADER_AUTH_PASSWORD_ENCRYPTION_MODE);
				if(encryptionmode==null || encryptionmode.getValue()==null || encryptionmode.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_HEADER_AUTH_PASSWORD_ENCRYPTION_MODE+"\" for this opco");
				}
				
				String username_param_name = headerauthusernameparam.getValue();
				String password_param_name = headerauthpasswordparam.getValue();
				String encryptionmethod = encryptionmode.getValue();
				
				ProfileConfigs usernamevalue = this.configuration.get(username_param_name);
				if(usernamevalue==null)
					throw new MessageSenderException("You''ve not set the value for\""+username_param_name+"\" as the username param");
				
				ProfileConfigs passwordvalue = this.configuration.get(password_param_name);
				if(passwordvalue==null)
					throw new MessageSenderException("You''ve not set value for \""+password_param_name+"\" as the password param");
				
				
				String username = usernamevalue.getValue();
				String password = passwordvalue.getValue();
				
				String digest = "";
				try {
					digest = encryptor.encrypt(username,password, encryptionmethod);
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
					throw new MessageSenderException("Could not encrypt header params",e);
				}
				
				ProfileConfigs authmethodparamname = this.configuration.get(HTTP_HEADER_AUTH_METHOD_PARAM_NAME);
				if(authmethodparamname==null || authmethodparamname.getValue()==null || authmethodparamname.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_HEADER_AUTH_METHOD_PARAM_NAME+"\" for this opco");
				}
				String authmethod = authmethodparamname.getValue();//e.g Basic
				
				
				
				if(this.configuration.get(HTTP_HEADER_AUTH_HAS_MULTIPLE_KV_PAIRS).getValue().equalsIgnoreCase("yes")){
					
					for(Map.Entry<String,ProfileConfigs> config : this.configuration.entrySet()){
						
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
				
				for(Map.Entry<String,ProfileConfigs> config : this.configuration.entrySet()){//Any other header param must start with the value HTTP_HEADER_PREFIX
					
					String key = config.getKey();
					if(key.trim().toLowerCase().startsWith(HTTP_HEADER_PREFIX)){
						String param_name = key.split(HTTP_HEADER_PREFIX)[1];
						headerParams.put(param_name, config.getValue().getValue());
					}
				}
				
				ProfileConfigs httpheaderauthparam = this.configuration.get(HTTP_HEADERAUTH_PARAM_NAME);
				if(httpheaderauthparam==null || httpheaderauthparam.getValue()==null || httpheaderauthparam.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_HEADERAUTH_PARAM_NAME+"\" for this opco");
				}
				headerParams.put(httpheaderauthparam.getValue(), auth_header_value.trim());
				
			}
			
			generic_http_parameters.setHeaderParams(headerParams);
			
		}
			
		
		
		if(this.configuration.get(HTTP_HAS_PAYLOAD).getValue().equalsIgnoreCase("yes")){
			
			ProfileConfigs httppayloadtemplate = this.configuration.get(HTTP_PAYLOAD_TEMPLATE);
			if(httppayloadtemplate==null || httppayloadtemplate.getValue()==null || httppayloadtemplate.getValue().isEmpty()){
				throw new MessageSenderException("No configuration set for \""+HTTP_PAYLOAD_TEMPLATE+"\" for this opco");
			}
			String http_payload_template_name = httppayloadtemplate.getValue();
			
			payload_template = getTemplates().get(http_payload_template_name).getValue();
			
			if(payload_template==null || payload_template.isEmpty())
				throw new MessageSenderException("Template with name \""
						+http_payload_template_name+"\" for this profile ("+httppayloadtemplate.getProfile().toString()+") "
						+ "hasn't been found. First check if the name is "
						+ "correct in the opco_configs table and matches in the opco_templates table");
			
			for(Map.Entry<String,ProfileConfigs> config : this.configuration.entrySet()){//Any other header param
				
				String key = config.getKey();
				if(key.trim().toLowerCase().startsWith(HTTP_PAYLOAD_PARAM_PREFIX)){
					String param_name = key.split(HTTP_PAYLOAD_PARAM_PREFIX)[1];
					payload_template = payload_template.replaceAll("\\$\\{"+param_name+"\\}", Matcher.quoteReplacement(config.getValue().getValue()) );
				}
			}
			for(NameValuePair valuep : qparams){
				if(valuep.getValue()!=null)
					payload_template = payload_template.replaceAll("\\$\\{"+valuep.getName()+"\\}", Matcher.quoteReplacement( valuep.getValue()) );
				else
					logger.warn("Value for valuep.getName()-> '"+valuep.getName()+"' is valuep.getValue()-> '"+valuep.getValue()+"'");
			}
			
			generic_http_parameters.setStringentity(payload_template);
			
		}else{
			
			generic_http_parameters.setHttpParams(qparams);
		}
		
		String url = this.configuration.get(HTTP_BASE_URL).getValue();
		
		if(this.configuration.get(HTTP_IS_RESTFUL).getValue().equalsIgnoreCase("yes")){
			for(Map.Entry<String,ProfileConfigs> config : this.configuration.entrySet()){
				
				String key = config.getKey();
				if(key.trim().toLowerCase().startsWith(HTTP_REST_PATH_PARAM_PREFIX)){
					String param_name = key.split(HTTP_REST_PATH_PARAM_PREFIX)[1];
					try {
						String basicparam = getValueFromqparams(qparams,param_name);
						if(basicparam!=null)
							url = url.replaceAll("\\$\\{"+param_name+"\\}", URLEncoder.encode( Matcher.quoteReplacement(basicparam ),"UTF-8"  ) );
						url = url.replaceAll("\\$\\{"+param_name+"\\}",  URLEncoder.encode( Matcher.quoteReplacement(config.getValue().getValue())  ,"UTF-8"));
					}catch (IllegalArgumentException e) {
						logger.error(e.getMessage() +" param_name = "+param_name+" config value = "+ config.getValue().getValue(),e);
						throw new MessageSenderException( " param_name = "+param_name+" config value = "+ config.getValue().getValue(),e);
					} catch (Exception e) {
						logger.error(e.getMessage(),e);
					}
				}
			}
			
			for(NameValuePair valuep : qparams){
				try {
					if(valuep.getName()!=null && valuep.getValue()!=null)
						url = url.replaceAll("\\$\\{"+valuep.getName()+"\\}", URLEncoder.encode( Matcher.quoteReplacement(valuep.getValue()) ,"UTF-8"  )   )   ;
				} catch (UnsupportedEncodingException e) {
					logger.error(e.getMessage(),e);
					throw new MessageSenderException("Unsupporte Encoding. Could not encode param ="+valuep.getName()+",  value"+valuep.getValue(),e);
				}catch(Exception e){
					logger.error(e.getMessage(),e);
					throw new MessageSenderException("General Error. Could not encode param ="+valuep.getName()+",  value"+valuep.getValue(),e);
				}
			}
		}
		
		generic_http_parameters.setUrl(url);
		
		ProfileConfigs httpmethod = this.configuration.get(HTTP_REQUEST_METHOD);
		
		if(httpmethod!=null){
			generic_http_parameters.setHttpmethod(httpmethod.getValue());
		}else{
			generic_http_parameters.setHttpmethod(HttpMethod.POST);
			logger.warn("No configuration for \""+HTTP_REQUEST_METHOD
					+"\" has been provided, so we will use "
					+ "HTTP POST as default. If you wish to override this, "
					+ "provide this config in db");
		}
		
		
		GenericHttpResp resp = null;
		try{
			resp = httpclient.call(generic_http_parameters);
		}catch(Exception exp){
			logger.info(outgoingsms);
			logger.error(exp.getMessage(), exp);
		}
		
		logger.info("\n\n\t\t>>>url>>> : ["+url
				+"]\n\t\tauth_header_value = "+auth_header_value+
				"\n\t\t>>>payload>>> : "+payload_template+
				"\n\t\t>>>response>>> : "+resp.getBody()+"\n\n");
		
		response.setRespcode(String.valueOf(resp.getResp_code()));
		if(resp.getResp_code()>=200 && resp.getResp_code()<=299 )//All http 200 series are treated as success
			response.setSuccess(Boolean.TRUE);
		
		
		
		//How do we parse the responses and get meaningful data that will match com.pixelandtag.smssenders.PlainHttpSender.sendSMS(MTsms).response?
		if(resp.getContenttype()!=null && resp.getContenttype().toLowerCase().contains("json")){
			try {
				JSONObject jsonobject = new JSONObject(resp.getBody());
				
				ProfileConfigs pathtorefval = this.configuration.get(HTTP_RESP_JSON_REF_VALUE_KEY);
				if(pathtorefval==null || pathtorefval.getValue()==null || pathtorefval.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_RESP_JSON_REF_VALUE_KEY+"\" for this opco");
				}
				
				ProfileConfigs respmsgcnf = this.configuration.get(HTTP_RESP_JSON_RESP_MSG_KEY);
				if(respmsgcnf==null || respmsgcnf.getValue()==null || respmsgcnf.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_RESP_JSON_RESP_MSG_KEY+"\" for this opco");
				}
				
				ProfileConfigs respcodeconfg = this.configuration.get(HTTP_RESP_JSON_RESPCODE_KEY);
				if(respcodeconfg==null || respcodeconfg.getValue()==null || respcodeconfg.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_RESP_JSON_RESPCODE_KEY+"\" for this opco");
				}
				
				jsonutil.loadJson(jsonobject);
				response.setRefvalue((String)jsonutil.getValue(pathtorefval.getValue()));
				response.setResponseMsg((String)jsonutil.getValue(respmsgcnf.getValue()));
				response.setRespcode((String)jsonutil.getValue(respcodeconfg.getValue()));
				jsonutil.reset();
				
			} catch (JSONException e) {
				logger.error(e.getMessage(),e);
				throw new MessageSenderException("Could not parse the json response json -> "+resp.getBody(),e);
			}
			
		}else if(resp.getContenttype()!=null &&  resp.getContenttype().toLowerCase().contains("xml")){
			
			
			ProfileConfigs pathtorefval = this.configuration.get(HTTP_RESP_XML_REF_VALUE_KEY);
			if(pathtorefval==null || pathtorefval.getValue()==null || pathtorefval.getValue().isEmpty()){
				throw new MessageSenderException("No configuration set for \""+HTTP_RESP_XML_REF_VALUE_KEY+"\" for this opco");
			}
			
			ProfileConfigs respmsgcnf = this.configuration.get(HTTP_RESP_XML_RESP_MSG_KEY);
			if(respmsgcnf==null || respmsgcnf.getValue()==null || respmsgcnf.getValue().isEmpty()){
				throw new MessageSenderException("No configuration set for \""+HTTP_RESP_XML_RESP_MSG_KEY+"\" for this opco");
			}
			
			ProfileConfigs respcodeconfg = this.configuration.get(HTTP_RESP_XML_RESPCODE_KEY);
			if(respcodeconfg==null || respcodeconfg.getValue()==null || respcodeconfg.getValue().isEmpty()){
				throw new MessageSenderException("No configuration set for \""+HTTP_RESP_XML_RESPCODE_KEY+"\" for this opco");
			}
			
			response.setRefvalue(getValue(resp.getBody(),pathtorefval.getValue()));
			response.setResponseMsg(getValue(resp.getBody(),respmsgcnf.getValue()));
			response.setRespcode(getValue(resp.getBody(),respcodeconfg.getValue()));
			
		}else{
			
			response.setRefvalue(String.valueOf(outgoingsms.getCmp_tx_id()));
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
		return xml.substring(start, end);
	}
	
	private String getValueFromqparams(List<NameValuePair> qparams,String key) {
		for(NameValuePair valuep : qparams)
			if(valuep.getName().equals(key))
				return valuep.getValue();
		return null;
	}
	
	

	public void setConfiguration(Map<String, ProfileConfigs> configs) {
		this.configuration = configs;
	}
	

	public static void main(String[] args) throws JSONException {
		String json = "{\"msg\": \"test\",  \"respobj\" : {\"code\" : 0, \"ref\": 1564546}  }";
		JSONObject jObject = new JSONObject(json);
		
		jsonutil = new JsonUtilImpl();
		jsonutil.loadJson(jObject);
		System.out.println("ref = "+jsonutil.getValue("respobj.ref"));
		jsonutil.reset();
	}
	
	
	
}
