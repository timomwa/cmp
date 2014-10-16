package com.inmobia.celcom.entities;

import javax.servlet.http.HttpServletRequest;

import com.inmobia.celcom.api.GenericMessage;

/**
 * 
 * @author Timothy Mwangi Gikonyo
 * Date: 2012-02-02.
 * 
 * When the CMP (Content Management Platform) sends us an MO,
 * the following properties will be there in the request.
 *
 */
public abstract class GenericMO extends GenericMessage {
	
	public GenericMO(){
	}
	
	public GenericMO(HttpServletRequest request) {
		
		
		//setCMP_Txid(request.getParameter("CMP_Txid"));
		setSMS_Message_String(request.getParameter("text"));
		setSMS_SourceAddr("32329");
		setMsisdn(request.getParameter("msisdn"));
		//setSMS_DataCodingId(request.getParameter("SMS_DataCodingId"));
		//setCMPResponse(request.getParameter("PCMResponse"));
		//setAPIType(request.getParameter("APIType"));
		setCMP_AKeyword(getfirstWord(request.getParameter("text")));
		setCMP_SKeyword(getfirstWord(request.getParameter("text")));
	}
	
	
	
	/*
	 * CMP-assigned unique transaction id of length 19
	 * characters. This is the "transaction id" the CP must
	 * respond with while sending back MT.
	 */
	//private String CMP_Txid;
	
	
	private String getfirstWord(String parameter) {
		if(parameter!=null)
			return parameter.split("\\s")[0];
		return null;
	}



	/*
	 * The keyword string entered by the subscriber
	 * modified or not before forwarding to the CP.
     *
     *in celcom.MessageLog, the field is called SMS
	 */
	private String SMS_Message_String;
	
	
	/*
	 * Large account on which the request came in.
	 */
	private String SMS_SourceAddr;
	
	
	
	/*
	 * Shadowed MSISDN or actual MSISDN - depending on
     * whether a masking was chosen or not for the given
     * VAS. This field can contain Celcom prefix
     * 013,019,0148,0145 or any other ported-In number 
	 */
	private String msisdn;
	
	
	/*
	 * SMS_DataCodingId=8 it will be sent only for MO
     * messages containing one or more Unicode characters.
	 */
	private String SMS_DataCodingId;
	
	
	/*
	 * Response code returned by CMP. Will be present only
	 *	if CMP check is enabled for the shortcode. Please see
     * CMP Status message
	 */
	private String CMPResponse;
	
	
	/*
	 * APIType=8 is to indicate that CMP has been sent
	 * Reminder to subscriber.
	 */
	private String APIType;
	
	
	/*
	 * MO service keyword forwarded to CP when Reminder
	 * has been sent to Subscriber.
	 */
	private String CMP_AKeyword;
	
	
	/*
	 * MO secondary service keyword forwarded to CP when
	 * Reminder has been sent to Subscriber
	 */
	private String CMP_SKeyword;
	
	
	


	/*public String getCMP_Txid() {
		return CMP_Txid;
	}


	public void setCMP_Txid(String cMP_Txid) {
		CMP_Txid = cMP_Txid;
	}*/

	
	

	public String getSMS_Message_String() {
		return SMS_Message_String;
	}

	public void setSMS_Message_String(String sMS_Message_String) {
		SMS_Message_String = sMS_Message_String;
	}


	public String getSMS_SourceAddr() {
		return SMS_SourceAddr;
	}


	public void setSMS_SourceAddr(String sMS_SourceAddr) {
		SMS_SourceAddr = sMS_SourceAddr;
	}


	public String getMsisdn() {
		return msisdn;
	}


	public void setMsisdn(String sUB_Mobtel) {
		msisdn = sUB_Mobtel;
	}


	public String getSMS_DataCodingId() {
		if(SMS_DataCodingId==null)
			return GenericMessage.ASCII_SMS_ENCODING_ID;
		return SMS_DataCodingId;
	}


	public void setSMS_DataCodingId(String sMS_DataCodingId) {
		SMS_DataCodingId = sMS_DataCodingId;
	}


	public String getCMPResponse() {
		return CMPResponse;
	}


	public void setCMPResponse(String CMPResponse) {
		this.CMPResponse = CMPResponse;
	}


	public String getAPIType() {
		return APIType;
	}


	public void setAPIType(String aPIType) {
		APIType = aPIType;
	}


	public String getCMP_AKeyword() {
		return CMP_AKeyword;
	}


	public void setCMP_AKeyword(String cMP_AKeyword) {
		CMP_AKeyword = cMP_AKeyword;
	}


	public String getCMP_SKeyword() {
		return CMP_SKeyword;
	}


	public void setCMP_SKeyword(String cMP_SKeyword) {
		CMP_SKeyword = cMP_SKeyword;
	}

	@Override
	public String toString() {
		
		return "GenericMO [SMS_Message_String="
				+ SMS_Message_String + ", SMS_SourceAddr=" + SMS_SourceAddr
				+ ", SUB_Mobtel=" + msisdn + ", SMS_DataCodingId="
				+ SMS_DataCodingId + ", CMPResponse=" + CMPResponse
				+ ", APIType=" + APIType + ", CMP_AKeyword=" + CMP_AKeyword
				+ ", CMP_SKeyword=" + CMP_SKeyword + "] "+super.toString();
		
	}

}
