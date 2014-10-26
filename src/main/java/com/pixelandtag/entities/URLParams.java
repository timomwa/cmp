package com.pixelandtag.entities;

import java.util.Properties;

public class URLParams {
	
	
	public URLParams(Properties props){
		
		this.CP_Id=props.getProperty("CP_Id");
		this.CP_UserId=props.getProperty("CP_UserId");
		this.CP_Password=props.getProperty("CP_Password");
		this.CMP_ContentType=props.getProperty("CMP_ContentType");
		this.CMP_A_Keyword=props.getProperty("CMP_A_Keyword");
		this.CMP_S_Keyword=props.getProperty("CMP_S_Keyword");
		this.SMS_Msgdata=props.getProperty("SMS_Msgdata");
		this.SMS_SourceAddr=props.getProperty("SMS_SourceAddr");
		this.SUB_DeviceType=props.getProperty("SUB_DeviceType");
		this.SERVER_TZ = props.getProperty("SERVER_TZ");
		this.CLIENT_TZ = props.getProperty("CLIENT_TZ");
		
		
		
		this.mturl=props.getProperty("mturl");
		this.login=props.getProperty("login");
		this.pass=props.getProperty("pass");
		this.type=props.getProperty("type");
		this.src=props.getProperty("src");
		
		
		
		if(props.getProperty("http_timeout")==null){
			
			this.http_timeout=6000;
		
		}else{
			
			this.http_timeout=Integer.parseInt(props.getProperty("http_timeout"));
		
		}
		
		
		if(props.getProperty("retry_per_msg")==null){
			
			this.retry_per_msg=1;
		
		}else{
			
			this.retry_per_msg=Integer.parseInt(props.getProperty("retry_per_msg"));
		
		}
		
		
		if(props.getProperty("msg_part_wait")==null){
			
			this.msg_part_wait=100;
		
		}else{
			
			this.msg_part_wait=Integer.parseInt(props.getProperty("msg_part_wait")); 
		
		}
		
		
		this.mturl=props.getProperty("mturl");
		
	}
	
	public String getSERVER_TZ() {
		return SERVER_TZ;
	}
	public String getCLIENT_TZ() {
		return CLIENT_TZ;
	}
	public void setSERVER_TZ(String sERVER_TZ) {
		SERVER_TZ = sERVER_TZ;
	}
	public void setCLIENT_TZ(String cLIENT_TZ) {
		CLIENT_TZ = cLIENT_TZ;
	}

	private String CMP_Txid;
	private String CP_Id;
	private String CP_UserId;
	private String CP_Password;
	private String CMP_ContentType;
	private String CMP_A_Keyword;
	private String CMP_S_Keyword;
	private String SMS_Msgdata;
	private String SMS_SourceAddr;
	private String SUB_DeviceType="NOKIA2100";
	private String SMS_MsgTxt;//the SMS
	private String SUB_R_Mobtel;
	private String SUB_C_Mobtel;
	
	private int http_timeout;
	private int retry_per_msg;
	private int msg_part_wait;
	private String SERVER_TZ;
	private String CLIENT_TZ;
	
	/*post inmobia*/
	private String mturl;
	private String login;
	private String pass;
	private String type;
	private String src;/*The shortcode*/
	
	
	

	public int getMsg_part_wait() {
		return msg_part_wait;
	}
	public void setMsg_part_wait(int msg_part_wait) {
		this.msg_part_wait = msg_part_wait;
	}
	public int getRetry_per_msg() {
		return retry_per_msg;
	}
	public void setRetry_per_msg(int retry_per_msg) {
		this.retry_per_msg = retry_per_msg;
	}
	public int getHttp_timeout() {
		return http_timeout;
	}
	public void setHttp_timeout(int http_timeout) {
		this.http_timeout = http_timeout;
	}
	public String getMturl() {
		return mturl;
	}
	public void setMturl(String mturl) {
		this.mturl = mturl;
	}
	public String getCMP_Txid() {
		return CMP_Txid;
	}
	public void setCMP_Txid(String cMP_Txid) {
		CMP_Txid = cMP_Txid;
	}
	public String getCP_Id() {
		return CP_Id;
	}
	public void setCP_Id(String cP_Id) {
		CP_Id = cP_Id;
	}
	public String getCP_UserId() {
		return CP_UserId;
	}
	public void setCP_UserId(String cP_UserId) {
		CP_UserId = cP_UserId;
	}
	public String getCP_Password() {
		return CP_Password;
	}
	public void setCP_Password(String cP_Password) {
		CP_Password = cP_Password;
	}
	public String getCMP_ContentType() {
		return CMP_ContentType;
	}
	public void setCMP_ContentType(String cMP_ContentType) {
		CMP_ContentType = cMP_ContentType;
	}
	public String getCMP_A_Keyword() {
		return CMP_A_Keyword;
	}
	public void setCMP_A_Keyword(String cMP_A_Keyword) {
		CMP_A_Keyword = cMP_A_Keyword;
	}
	public String getCMP_S_Keyword() {
		return CMP_S_Keyword;
	}
	public void setCMP_S_Keyword(String cMP_S_Keyword) {
		CMP_S_Keyword = cMP_S_Keyword;
	}
	public String getSMS_Msgdata() {
		return SMS_Msgdata;
	}
	public void setSMS_Msgdata(String sMS_Msgdata) {
		SMS_Msgdata = sMS_Msgdata;
	}
	public String getSMS_SourceAddr() {
		return SMS_SourceAddr;
	}
	public void setSMS_SourceAddr(String sMS_SourceAddr) {
		SMS_SourceAddr = sMS_SourceAddr;
	}
	public String getSUB_DeviceType() {
		return SUB_DeviceType;
	}
	public void setSUB_DeviceType(String sUB_DeviceType) {
		SUB_DeviceType = sUB_DeviceType;
	}
	/**
	 * The SMS message to be sent
	 * @return
	 */
	public String getSMS_MsgTxt() {
		return SMS_MsgTxt;
	}
	public void setSMS_MsgTxt(String sMS_MsgTxt) {
		SMS_MsgTxt = sMS_MsgTxt;
	}
	/**
	 * The msisdn to be sent
	 * @return
	 */
	public String getSUB_R_Mobtel() {
		return SUB_R_Mobtel;
	}
	/**
	 * The msisdn to be sent
	 * @return
	 */
	public void setSUB_R_Mobtel(String sUB_R_Mobtel) {
		SUB_R_Mobtel = sUB_R_Mobtel;
	}
	public String getSUB_C_Mobtel() {
		return SUB_C_Mobtel;
	}
	public void setSUB_C_Mobtel(String sUB_C_Mobtel) {
		SUB_C_Mobtel = sUB_C_Mobtel;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	@Override
	public String toString() {
		return "URLParams [mturl=" + mturl + ", login=" + login + ", pass="
				+ pass + ", type=" + type + ", src=" + src + "]";
	}
	
	
	
	
	
	



}
