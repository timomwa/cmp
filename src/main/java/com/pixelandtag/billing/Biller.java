package com.pixelandtag.billing;

import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.smssenders.SenderResp;

public interface Biller {
	
	
	public final String HTTP_BILLER_PROTOCOL = "biller_http_protocol";
	public final String HTTP_BILLER_BASE_URL = "biller_http_base_url";
	public final String HTTP_BILLER_IS_RESTFUL = "biller_http_is_restful";
	public final String HTTP_BILLER_TRANSACTION_ID_PARAM_NAME = "biller_http_transaction_id_param_name";
	public final String HTTP_BILLER_SHORTCODE_PARAM_NAME = "biller_http_shortcode_param_name";
	public final String HTTP_BILLER_KEYWORD_PARAM_NAME = "biller_http_keyword_param_name";
	public final String HTTP_BILLER_EVENT_TYPE_PARAM_NAME = "biller_http_eventtype_param_name";
	public final String HTTP_BILLER_MSISDN_PARAM_NAME = "biller_http_msisdn_param_name";
	public final String HTTP_BILLER_PRICE_PARAM_NAME = "biller_http_price_param_name";
	public final String HTTP_BILLER_SMS_MSG_PARAM_NAME = "biller_http_sms_msg_param_name";
	public final String HTTP_BILLER_CP_ID_PARAM_NAME = "biller_http_cp_id_param_name";
	public final String HTTP_BILLER_OPERATION_PARAM_NAME = "biller_http_operation_param_name";
	public final String HTTP_BILLER_SERVICE_ID_PARAM_NAME = "biller_http_serviceid_param_name";
	public final String HTTP_BILLER_PRICE_POINT_PARAM_NAME = "biller_http_pricepoint_param_name";
	public final String HTTP_BILLER_TIMESTAMP_FORMAT = "biller_http_header_timeStampFormat";
	public final String HTTP_BILLER_PRE_ENCODE_TEMPLATE = "biller_http_pre_encode_template";
	
	public final String HTTP_BILLER_USE_HTTP_HEADER = "biller_http_useheader";
	
	
	public final String HTTP_BILLER_HAS_PAYLOAD = "biller_http_haspayload";
	
	public final String HTTP_BILLER_PAYLOAD_TEMPLATE = "biller_http_payload_template_name";
	
	
	public final String HTTP_BILLER_HEADERAUTH_PARAM_NAME = "biller_http_headerauth_param_name";
	public final String HTTP_BILLER_HEADER_AUTH_PASSWORD_ENCRYPTION_MODE = "biller_http_header_auth_password_encryptionmode";
	public final String HTTP_BILLER_HEADER_AUTH_METHOD_PARAM_NAME = "biller_http_header_auth_method_param_name";//e.g Basic
	public final String HTTP_BILLER_REQUEST_METHOD = "biller_http_request_method";
	public final String HTTP_BILLER_EXPECTED_CONTENTTYPE = "biller_http_expectedcontenttype";
	
	public final String HTTP_BILLER_HEADER_AUTH_HAS_USERNAME_AND_PASSWORD = "biller_http_header_auth_has_username_and_password";
	public final String HTTP_BILLER_HEADER_AUTH_USERNAME_PARAM_NAME = "biller_http_header_auth_username_param_name";
	public final String HTTP_BILLER_HEADER_AUTH_PASSWORD_PARAM_NAME = "biller_http_header_auth_password_param_name";
	
	public final String HTTP_BILLER_HEADER_AUTH_HAS_MULTIPLE_KV_PAIRS = "biller_http_header_auth_hasmultiple_kv_pairs";
	public final String HTTP_BILLER_HEADER_AUTH_PARAM_PREFIX = "biller_http_header_auth_param_";
	public final String HTTP_BILLER_HEADER_PREFIX = "biller_http_header_param_";
	public final String HTTP_BILLER_PAYLOAD_PARAM_PREFIX = "biller_http_payload_param_";
	public final String HTTP_BILLER_REST_PATH_PARAM_PREFIX = "biller_http_rest_path_param_";
	
	public final String HTTP_BILLER_RESP_JSON_REF_VALUE_KEY = "biller_http_resp_json_ref_value_key";
	public final String HTTP_BILLER_RESP_JSON_RESP_MSG_KEY = "biller_http_resp_json_resp_msg_key";
	public final String HTTP_BILLER_RESP_JSON_RESPCODE_KEY = "biller_http_resp_json_respcode_key";
	
	public final String HTTP_BILLER_RESP_XML_REF_VALUE_KEY = "biller_http_resp_xml_ref_value_key";
	public final String HTTP_BILLER_RESP_XML_RESP_MSG_KEY_FAILURE = "biller_http_resp_xml_resp_msg_key_failure";
	public final String HTTP_BILLER_RESP_XML_RESP_MSG_KEY_SUCCESS = "biller_http_resp_xml_resp_msg_key_success";
	public final String HTTP_BILLER_RESP_XML_RESPCODE_KEY = "biller_http_resp_xml_respcode_key";
	public final String HTTP_BILLER_RESP_SUCCESS_STRING = "biller_http_resp_success_string";
	public final String HTTP_BILLER_ALLOW_SENDING_BLANK_TEXT = "biller_http_allow_send_blank_text";

	public SenderResp charge(Billable  billable) throws BillerConfigException;
	public void validateMandatory() throws BillerConfigException;
}
