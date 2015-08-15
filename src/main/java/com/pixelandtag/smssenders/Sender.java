package com.pixelandtag.smssenders;

import com.pixelandtag.entities.MTsms;

public interface Sender{
	
	public final String HTTP_PROTOCOL = "http_protocol";
	public final String HTTP_BASE_URL = "http_base_url";
	public final String HTTP_IS_RESTFUL = "http_is_restful";
	public final String HTTP_TRANSACTION_ID_PARAM_NAME = "http_transaction_id_param_name";
	public final String HTTP_SHORTCODE_PARAM_NAME = "http_shortcode_param_name";
	public final String HTTP_MSISDN_PARAM_NAME = "http_msisdn_param_name";
	public final String HTTP_SMS_MSG_PARAM_NAME = "http_sms_msg_param_name";
	public final String HTTP_USE_HTTP_HEADER = "http_useheader";
	
	public final String HTTP_HAS_PAYLOAD = "http_haspayload";
	
	public final String HTTP_PAYLOAD_TEMPLATE = "http_payload_template";
	
	
	public final String HTTP_HEADER_AUTH_PARAM_NAME = "http_header_auth_param_name";
	public final String HTTP_HEADER_AUTH_PASSWORD_ENCRYPTION_MODE = "http_header_auth_password_encryptionmode";
	public final String HTTP_HEADER_AUTH_METHOD_PARAM_NAME = "http_header_auth_method_param_name";//e.g Basic
	
	public final String HTTP_HEADER_AUTH_HAS_USERNAME_AND_PASSWORD = "http_header_auth_username_param_name";
	public final String HTTP_HEADER_AUTH_USERNAME_PARAM_NAME = "http_header_auth_username_param_name";
	public final String HTTP_HEADER_AUTH_PASSWORD_PARAM_NAME = "http_header_auth_password_param_name";
	
	public final String HTTP_HEADER_AUTH_HAS_MULTIPLE_KV_PAIRS = "http_header_auth_hasmultiple_kv_pairs";
	public final String HTTP_HEADER_AUTH_PARAM_PREFIX = "http_header_auth_param_";
	public final String HTTP_HEADER_PREFIX = "http_header_param_";
	public final String HTTP_PAYLOAD_PARAM_PREFIX = "http_payload_param_";
	public final String HTTP_REST_PATH_PARAM_PREFIX = "http_rest_path_param_";
	
	public final String HTTP_RESP_JSON_REF_VALUE_KEY = "http_resp_json_ref_value_key";
	public final String HTTP_RESP_JSON_RESP_MSG_KEY = "http_resp_json_resp_msg_key";
	public final String HTTP_RESP_JSON_RESPCODE_KEY = "http_resp_json_respcode_key";
	
	public final String HTTP_RESP_XML_REF_VALUE_KEY = "http_resp_xml_ref_value_key";
	public final String HTTP_RESP_XML_RESP_MSG_KEY = "http_resp_xml_resp_msg_key";
	public final String HTTP_RESP_XML_RESPCODE_KEY = "http_resp_xml_respcode_key";
	
	public SenderResp sendSMS(MTsms mtsms) throws MessageSenderException;

}
