package com.pixelandtag.smssenders;

import java.text.ParseException;
import java.util.Date;

import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.entities.MTsms;

public interface Sender{
	
	public final String HTTP_PROTOCOL = "http_protocol";
	public final String HTTP_BASE_URL = "http_base_url";
	public final String HTTP_IS_RESTFUL = "http_is_restful";
	public final String HTTP_TRANSACTION_ID_PARAM_NAME = "http_transaction_id_param_name";
	public final String HTTP_SHORTCODE_PARAM_NAME = "http_shortcode_param_name";
	public final String HTTP_MSISDN_PARAM_NAME = "http_msisdn_param_name";
	public final String HTTP_SMS_MSG_PARAM_NAME = "http_sms_msg_param_name";
	public final String HTTP_TIMESTAMP_PARAM_NAME = "http_timestamp_param_name";
	public final String HTTP_PARLAYX_SERVICEID_PARAM_NAME = "http_parlayx_serviceid_param_name";
	public final String HTTP_USE_HTTP_HEADER = "http_useheader";
	
	public final String HTTP_HAS_PAYLOAD = "http_haspayload";
	
	public final String HTTP_PAYLOAD_TEMPLATE = "http_payload_template_name";
	
	
	public final String HTTP_HEADERAUTH_PARAM_NAME = "http_headerauth_param_name";
	public final String HTTP_HEADER_AUTH_PASSWORD_ENCRYPTION_MODE = "http_header_auth_password_encryptionmode";
	public final String HTTP_HEADER_AUTH_METHOD_PARAM_NAME = "http_header_auth_method_param_name";//e.g Basic
	public final String HTTP_REQUEST_METHOD = "http_request_method";
	
	public final String HTTP_HEADER_AUTH_HAS_USERNAME_AND_PASSWORD = "http_header_auth_has_username_and_password";
	public final String HTTP_HEADER_AUTH_USERNAME_PARAM_NAME = "http_header_auth_username_param_name";
	public final String HTTP_HEADER_AUTH_PASSWORD_PARAM_NAME = "http_header_auth_password_param_name";
	
	public final String HTTP_HEADER_AUTH_HAS_MULTIPLE_KV_PAIRS = "http_header_auth_hasmultiple_kv_pairs";
	public final String HTTP_HEADER_AUTH_PARAM_PREFIX = "http_header_auth_param_";
	public final String HTTP_HEADER_PREFIX = "http_header_param_";
	public final String HTTP_PAYLOAD_PARAM_PREFIX = "http_payload_param_";
	public final String HTTP_PAYLOAD_PLAIN_PASSWORD = "http_payload_plain_password";
	public final String HTTP_PAYLOAD_ENCRYPTION_MODE = "http_payload_encryption_mode";
	public final String HTTP_PAYLOAD_TIMESTAMP_FORMAT = "yyyyMMddHHmmss";//TODO externalize this
	public final String HTTP_PAYLOAD_SPPASSWORD_PARAM_NAME = "http_payload_sppassword_param_name";//TODO externalize this
	public final String HTTP_REST_PATH_PARAM_PREFIX = "http_rest_path_param_";
	
	public final String HTTP_RESP_JSON_REF_VALUE_KEY = "http_resp_json_ref_value_key";
	public final String HTTP_RESP_JSON_RESP_MSG_KEY = "http_resp_json_resp_msg_key";
	public final String HTTP_RESP_JSON_RESPCODE_KEY = "http_resp_json_respcode_key";
	
	public final String HTTP_RESP_XML_REF_VALUE_KEY = "http_resp_xml_ref_value_key";
	public final String HTTP_RESP_XML_RESP_MSG_KEY = "http_resp_xml_resp_msg_key";
	public final String HTTP_RESP_XML_RESPCODE_KEY = "http_resp_xml_respcode_key";
	public final String HTTP_ALLOW_SENDING_BLANK_TEXT = "http_allow_send_blank_text";
	public final String HTTP_REQUEST_PAYLOAD_CONTENTTYPE = "http_payload_contenttype";
	
	public final String SMPP_ID = "smpp_id";
	public final String ALT_SMPP_ID = "alt_smpp_id";
	public final String SMPP_IP = "smpp_ip";
	public final String SMPP_PORT = "smpp_port";
	public final String SMPP_TYPE = "smpp_type";
	public final String SMPP_USERNAME = "smpp_username";
	public final String SMPP_PASSWORD = "smpp_password";
	public final String SMPP_TON = "smpp_ton";
	public final String SMPP_NPI = "smpp_npi";
	public final String SMPP_DESTON = "smpp_destinationton";
	public final String SMPP_DESNPI = "smpp_destinationnpi";
	public final String SMPP_SHORTCODE = "smpp_shortcode";
	public final String SMPP_VERSION = "smpp_version";
	
	public SenderResp sendSMS(OutgoingSMS outgoingsms) throws MessageSenderException;
	public void validateMandatory()  throws MessageSenderException;
	//public String dateToString(Date datestr, String dateformat) throws ParseException

}
