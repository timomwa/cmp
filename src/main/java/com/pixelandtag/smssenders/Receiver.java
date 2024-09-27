package com.pixelandtag.smssenders;

import java.util.Map;

public interface Receiver {
	
	public static String IP_ADDRESS = "ip.address";
	public static String MO_MEDIUM_SOURCE = "mo_medium_source";
	public static String STRIPPABLE_STRING = "strippable_string";
	public static String HTTP_HEADER_PREFIX = "http_header_";
	public static String HTTP_RECEIVER_MSISDN_PARAM_NAME = "receiver_msisdn_param_name";
	public static String HTTP_RECEIVER_SHORTCODE_PARAM_NAME = "receiver_shortcode_param_name";
	public static String HTTP_RECEIVER_SMS_PARAM_NAME = "receiver_sms_param_name";
	public static String HTTP_RECEIVER_OPCO_TX_ID_PARAM_NAME = "receiver_opco_txid_param_name";
	public static String HTTP_RECEIVER_EXTRA_PARAMS_PREFIX = "receiver_extra_";
	public static String HTTP_RECEIVER_HAS_PAYLOAD = "receiver_has_payload";
	public static String HTTP_RECEIVER_PAYLOAD = "receiver_payload";
	public static String HTTP_RECEIVER_EXPECTED_CONTENTTYPE = "receiver_expected_contenttype";
	public static String HTTP_RECEIVER_OPCO_CODE = "receiver_opco_code";
	public static String HTTP_RECEIVER_TYPE = "receiver_message_type";
	
	public boolean logMO(Map<String, String> params);

}
