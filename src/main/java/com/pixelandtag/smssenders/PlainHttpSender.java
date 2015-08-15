package com.pixelandtag.smssenders;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.cmp.entities.customer.configs.OpcoConfigs;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPClient;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPParam;
import com.pixelandtag.sms.mt.workerthreads.GenericHttpResp;

public class PlainHttpSender implements Sender {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Inject
	private Encryptor encryptor;
	
	@Inject
	private static JsonUtilI jsonutil;
	
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
		mandatoryparams.add(HTTP_IS_RESTFUL);
		mandatoryparams.add(HTTP_HEADER_AUTH_HAS_MULTIPLE_KV_PAIRS);
		for(String param : mandatoryparams)
			if(this.configuration.get(param)==null)
				throw new MessageSenderException("No configuration set for \""+param+"\" for this opco");
	}

	@Override
	public SenderResp sendSMS(MTsms mtsms) throws MessageSenderException {
		
		SenderResp response = new SenderResp();
		
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
			
			OpcoConfigs headerhasunameandpwd = this.configuration.get(HTTP_HEADER_AUTH_HAS_USERNAME_AND_PASSWORD);
			
			if(headerhasunameandpwd==null || headerhasunameandpwd.getValue()==null || headerhasunameandpwd.getValue().isEmpty()){
				throw new MessageSenderException("No configuration set for \""+HTTP_HEADER_AUTH_HAS_USERNAME_AND_PASSWORD+"\" for this opco");
			}
			
			if(headerhasunameandpwd.getValue().equalsIgnoreCase("true")){
				
				OpcoConfigs headerauthusernameparam = this.configuration.get(HTTP_HEADER_AUTH_USERNAME_PARAM_NAME);
				if(headerauthusernameparam==null || headerauthusernameparam.getValue()==null || headerauthusernameparam.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_HEADER_AUTH_USERNAME_PARAM_NAME+"\" for this opco");
				}
				OpcoConfigs headerauthpasswordparam = this.configuration.get(HTTP_HEADER_AUTH_PASSWORD_PARAM_NAME);
				if(headerauthpasswordparam==null || headerauthpasswordparam.getValue()==null || headerauthpasswordparam.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_HEADER_AUTH_PASSWORD_PARAM_NAME+"\" for this opco");
				}
				OpcoConfigs encryptionmode = this.configuration.get(HTTP_HEADER_AUTH_PASSWORD_ENCRYPTION_MODE);
				if(encryptionmode==null || encryptionmode.getValue()==null || encryptionmode.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_HEADER_AUTH_PASSWORD_ENCRYPTION_MODE+"\" for this opco");
				}
				
				String username_param_name = headerauthusernameparam.getValue();
				String password_param_name = headerauthpasswordparam.getValue();
				String encryptionmethod = encryptionmode.getValue();
				
				String username = this.configuration.get(username_param_name).getValue();
				String password = this.configuration.get(password_param_name).getValue();
				
				String digest = "";
				try {
					digest = encryptor.encrypt(username,password, encryptionmethod);
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
					throw new MessageSenderException("Could not encrypt header params",e);
				}
				
				OpcoConfigs authmethodparamname = this.configuration.get(HTTP_HEADER_AUTH_METHOD_PARAM_NAME);
				if(authmethodparamname==null || authmethodparamname.getValue()==null || authmethodparamname.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_HEADER_AUTH_METHOD_PARAM_NAME+"\" for this opco");
				}
				String authmethod = authmethodparamname.getValue();//e.g Basic
				
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
				OpcoConfigs httpheaderauthparam = this.configuration.get(HTTP_HEADER_AUTH_PARAM_NAME);
				if(httpheaderauthparam==null || httpheaderauthparam.getValue()==null || httpheaderauthparam.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_HEADER_AUTH_PARAM_NAME+"\" for this opco");
				}
				headerParams.put(httpheaderauthparam.getValue(),auth_header_value.trim());
				
			}
			
		}
			
		
		
		if(this.configuration.get(HTTP_HAS_PAYLOAD).getValue().equalsIgnoreCase("yes")){
			
			OpcoConfigs httppayloadtemplate = this.configuration.get(HTTP_PAYLOAD_TEMPLATE);
			if(httppayloadtemplate==null || httppayloadtemplate.getValue()==null || httppayloadtemplate.getValue().isEmpty()){
				throw new MessageSenderException("No configuration set for \""+HTTP_PAYLOAD_TEMPLATE+"\" for this opco");
			}
			String payload_template = httppayloadtemplate.getValue();
			
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
			for(Map.Entry<String,OpcoConfigs> config : this.configuration.entrySet()){
				
				String key = config.getKey();
				if(key.trim().toLowerCase().startsWith(HTTP_REST_PATH_PARAM_PREFIX)){
					String param_name = key.split(HTTP_PAYLOAD_PARAM_PREFIX)[1];
					try {
						url = url.replaceAll("\\$\\{"+param_name+"\\}", URLEncoder.encode(getValueFromqparams(qparams,param_name),"UTF-8"));
					} catch (UnsupportedEncodingException e) {
						logger.error(e.getMessage(),e);
						throw new MessageSenderException("Could not encode path param",e);
					}
				}
			}
		}
		
		generic_http_parameters.setUrl(url);
		generic_http_parameters.setHttpParams(qparams);
		
		
		GenericHttpResp resp = httpclient.call(generic_http_parameters);
		response.setRespcode(String.valueOf(resp.getResp_code()));
		
		
		
		//How do we parse the responses and get meaningful data that will match com.pixelandtag.smssenders.PlainHttpSender.sendSMS(MTsms).response?
		if(resp.getContenttype()!=null && resp.getContenttype().toLowerCase().contains("json")){
			try {
				JSONObject jsonobject = new JSONObject(resp.getBody());
				
				OpcoConfigs pathtorefval = this.configuration.get(HTTP_RESP_JSON_REF_VALUE_KEY);
				if(pathtorefval==null || pathtorefval.getValue()==null || pathtorefval.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_RESP_JSON_REF_VALUE_KEY+"\" for this opco");
				}
				
				OpcoConfigs respmsgcnf = this.configuration.get(HTTP_RESP_JSON_RESP_MSG_KEY);
				if(respmsgcnf==null || respmsgcnf.getValue()==null || respmsgcnf.getValue().isEmpty()){
					throw new MessageSenderException("No configuration set for \""+HTTP_RESP_JSON_RESP_MSG_KEY+"\" for this opco");
				}
				
				OpcoConfigs respcodeconfg = this.configuration.get(HTTP_RESP_JSON_RESPCODE_KEY);
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
			
			
			OpcoConfigs pathtorefval = this.configuration.get(HTTP_RESP_XML_REF_VALUE_KEY);
			if(pathtorefval==null || pathtorefval.getValue()==null || pathtorefval.getValue().isEmpty()){
				throw new MessageSenderException("No configuration set for \""+HTTP_RESP_XML_REF_VALUE_KEY+"\" for this opco");
			}
			
			OpcoConfigs respmsgcnf = this.configuration.get(HTTP_RESP_XML_RESP_MSG_KEY);
			if(respmsgcnf==null || respmsgcnf.getValue()==null || respmsgcnf.getValue().isEmpty()){
				throw new MessageSenderException("No configuration set for \""+HTTP_RESP_XML_RESP_MSG_KEY+"\" for this opco");
			}
			
			OpcoConfigs respcodeconfg = this.configuration.get(HTTP_RESP_XML_RESPCODE_KEY);
			if(respcodeconfg==null || respcodeconfg.getValue()==null || respcodeconfg.getValue().isEmpty()){
				throw new MessageSenderException("No configuration set for \""+HTTP_RESP_XML_RESPCODE_KEY+"\" for this opco");
			}
			
			response.setRefvalue(getValue(resp.getBody(),pathtorefval.getValue()));
			response.setResponseMsg(getValue(resp.getBody(),respmsgcnf.getValue()));
			response.setRespcode(getValue(resp.getBody(),respcodeconfg.getValue()));
			
		}else{
			
			response.setRefvalue(String.valueOf(mtsms.getCMP_Txid()));
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
	
	

	public void setConfiguration(Map<String, OpcoConfigs> configs) {
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
