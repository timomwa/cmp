package com.pixelandtag.web.beans;

import java.io.Serializable;
import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;

import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.subscription.dto.MediumType;

/**
 * This class is instantiated by use of the constructor that
 * accepts a javax.servlet.http.HttpServletRequest object
 * 
 * The necessary values are extracted from the 
 * <code>javax.servlet.http.HttpServletRequest object</code>
 * and stored as variables within this classs' instance.
 * 
 * Therefore, you don't have to pass around the HttpServletRequest object
 * 
 * @author Timothy Mwangi Gikonyo
 * 
 * Date Created: 22nd September 2011
 * 
 *
 */
public class RequestObject implements Serializable{
	
	/**
	 * 
	 */
	private static final  long serialVersionUID = -697978310928640125L;
	private String lac,code, location,cellid,telcoid, msisdn, msg, keyword,countryCode,testBalance,litmus, tripWire = null, serviceActive="1";
	private Long serviceid = -1L;
	private Long messageId;
	private String transactionID = null;
	private BigInteger sessionid = null;
	private MediumType mediumType;
	private OperatorCountry opco;
	
	public RequestObject(IncomingSMS incomingsms) throws Exception {
		
		String telcoid, msisdn, msg = "", keyword,testBalance,tripWire = null, litmus = null;
		int price;
		Long serviceid;
		telcoid = null;
		msisdn = null;
		msg = null;
		keyword = null;
		price=0;
		testBalance=null;
		
		serviceid = incomingsms.getServiceid();
		
		opco = incomingsms.getOpco();
		
		if (incomingsms.getMsisdn() != null)
			msisdn = incomingsms.getMsisdn();
		
		if (incomingsms.getSms() != null){
			msg = incomingsms.getSms().trim();
		}
		
		if (keyword == null && msg !=null){
			keyword = msg.split("[\\s]")[0];
		}
		
		
		
		setTripWire(tripWire);
		setTestBalance(testBalance);
		setPrice(price);
		
		try{
			
			if(msg!=null)
				if(!msg.isEmpty()){
				
					msg = msg.trim();
					
					//msg = msg.toUpperCase();//TIMO revert if there is trouble
				}else{
				
					msg = "Customer";
			
				}
			
			if(keyword!=null){
				
				//keyword = keyword.toUpperCase(); // TIMO revert if there is trouble
				
				keyword = replaceAllIllegalCharacters(keyword).trim();
				
			}
			
		}catch (Exception e){
			
			e.printStackTrace();
			
		}
		
		setMsg(msg);
		setMsisdn(msisdn);
		setServiceid(serviceid);
		setTelcoid(telcoid);
		setLitmus(litmus);
		setKeyword(keyword);
		
		
		//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>["+msg+"]");
		
	}
	
	
	
	public String replaceAllIllegalCharactersLeaveSpace(String text){
		
		if(text==null)
			return null;
		
		text = text.replaceAll("[\\r]", "");
		text = text.replaceAll("[\\n]", "");
		text = text.replaceAll("[\\t]", "");
		text = text.replaceAll("[.]", "");
		text = text.replaceAll("[,]", "");
		text = text.replaceAll("[?]", "");
		text = text.replaceAll("[@]", "");
		text = text.replaceAll("[\"]", "");
		text = text.replaceAll("[\\]]", "");
		text = text.replaceAll("[\\[]", "");
		text = text.replaceAll("[\\{]", "");
		text = text.replaceAll("[\\}]", "");
		text = text.replaceAll("[\\(]", "");
		text = text.replaceAll("[\\)]", "");
		text = text.replaceAll("[<]", "");
		text = text.replaceAll("[>]", "");
		text = text.replaceAll("[%]", "");
		text = text.replaceAll("[|]", "");
		text = text.replaceAll("[!]", "");
		text = text.replaceAll("[;]", "");
		
		//for all the space key happy texters.. I stop you here!!
		while(text.indexOf("  ")>-1)
			text = text.replaceAll("  ", " ");
				
		text = text.trim();
		
		return text;
		
	}

	
	public static String replaceAllIllegalCharacters(String text){
		
		if(text==null)
			return null;
		
		text = text.replaceAll("[\\s]", "");
		text = text.replaceAll("[\\r]", "");
		text = text.replaceAll("[\\n]", "");
		text = text.replaceAll("[\\t]", "");
		text = text.replaceAll("[.]", "");
		text = text.replaceAll("[,]", "");
		text = text.replaceAll("[?]", "");
		text = text.replaceAll("[@]", "");
		text = text.replaceAll("[\"]", "");
		text = text.replaceAll("[\\]]", "");
		text = text.replaceAll("[\\[]", "");
		text = text.replaceAll("[\\{]", "");
		text = text.replaceAll("[\\}]", "");
		text = text.replaceAll("[\\(]", "");
		text = text.replaceAll("[\\)]", "");
		text = text.replaceAll("[<]", "");
		text = text.replaceAll("[>]", "");
		text = text.replaceAll("[%]", "");
		text = text.replaceAll("[|]", "");
		text = text.replaceAll("[!]", "");
		text = text.trim();
		
		//for all the space key happy texters.. I stop you here!!
		while(text.indexOf("  ")>-1)
			text = text.replaceAll("  ", " ");
		
		return text;
		
	}

	public RequestObject(HttpServletRequest request, String nextTransactionid, boolean replace_illegal_xters) throws Exception {
		
		String telcoid, msisdn, msg, keyword,testBalance,tripWire = null, litmus = null;
		int price;
		Long serviceid=-1L;
		telcoid = null;
		msisdn = null;
		msg = "";
		keyword = null;
		price=0;
		testBalance=null;
		
		if (request.getParameter("serviceid") != null){
			try{
				serviceid = Long.valueOf(request.getParameter("serviceid"));
			}catch(Exception e){e.printStackTrace();}
		}
		if (request.getParameter("litmus") != null)
			litmus = request.getParameter("litmus");
		
		if (request.getParameter("telcoid") != null)
			telcoid = request.getParameter("telcoid"); 
		if (request.getParameter("msisdn") != null)
			msisdn = request.getParameter("msisdn");
		if (request.getParameter("msg") != null){
			msg = request.getParameter("msg").trim();
		}if (request.getParameter("price") != null){
			price = (int)Double.parseDouble(request.getParameter("price"));
		}if (request.getParameter("testBalance") != null){
			testBalance = request.getParameter("testBalance").trim();
		}if (request.getParameter("tripWire") != null){
			tripWire = request.getParameter("tripWire").trim();
		}
		if (request.getParameter("tid") != null){
			transactionID = "-1";
			try{
				transactionID  = nextTransactionid;//Too big to handle in db for now.. request.getParameter("tid").trim());
			}catch(Exception exp){}
		}
		if (request.getParameter("sessionid") != null){
			sessionid = new BigInteger("-1");
			try{
				sessionid  = new BigInteger(request.getParameter("sessionid").trim());
			}catch(Exception exp){}
		}
		if (request.getParameter("code") != null){
			code  = request.getParameter("code").trim();
		}
		if (request.getParameter("location") != null){
			location  = request.getParameter("location").trim();
		}
		if (request.getParameter("lac") != null){
			lac  = request.getParameter("lac").trim();
		}
		if (request.getParameter("cellid") != null){
			cellid  = request.getParameter("cellid").trim();
		}
		
		if (request.getParameter("keyword") != null)
			keyword = request.getParameter("keyword").toUpperCase();
		
		if (keyword == null && msg !=null){
			keyword = msg.split("[\\s]")[0];
		}
		
		if (request.getParameter("serviceActive") != null)
			serviceActive = request.getParameter("serviceActive");
		
		
		
		setTripWire(tripWire);
		setTestBalance(testBalance);
		setPrice(price);
		
		try{
			
			
			if(msg!=null){
				msg = msg.trim();
				
				//msg = msg.toUpperCase();
			}
			
			if(replace_illegal_xters)
			if(keyword!=null){
				keyword = keyword.trim();
				
				//keyword = keyword.toUpperCase();
				keyword = replaceAllIllegalCharacters(keyword).trim();
			}
			
		}catch (Exception e){
			
			e.printStackTrace();
			
		}
		setMsg(msg);
		setMsisdn(msisdn);
		setServiceid(serviceid);
		setTelcoid(telcoid);
		setLitmus(litmus);
		setKeyword(keyword);
		
		
		//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>["+msg+"]");
		
	}

	
	public static void main(String[] st){
		//String test = "Kila <k!|>%";
		//test = replaceAllIllegalCharacters(test);
		//System.out.println(test.replaceAll("[^\\p{L}\\p{N}]", ""));
		
		String msg = "on      4";
		
		while(msg.indexOf("  ")>-1)
			msg = msg.replaceAll("  ", " ");
		
		System.out.println(msg);
	}
	
	public String getServiceActive() {
		return serviceActive;
	}

	public void setServiceActive(String serviceActive) {
		this.serviceActive = serviceActive;
	}

	public String getLitmus() {
		return litmus;
	}

	public void setLitmus(String litmus) {
		this.litmus = litmus;
	}

	private int price;

	public Long getServiceid() {
		return serviceid;
	}

	public void setServiceid(Long serviceid) {
		this.serviceid = serviceid;
	}

	public String getTelcoid() {
		return telcoid;
	}

	public void setTelcoid(String telcoid) {
		this.telcoid = telcoid;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getTestBalance() {
		return testBalance;
	}

	public void setTestBalance(String testBalance) {
		this.testBalance = testBalance;
	}

	public String getTripWire() {
		return tripWire;
	}
	public void setTripWire(String tripWire) {
		this.tripWire = tripWire;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public int getPrice() {
		return price;
	}
	public String getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}
	public BigInteger getSessionid() {
		return sessionid;
	}
	public void setSessionid(BigInteger sessionid) {
		this.sessionid = sessionid;
	}
	public String getLac() {
		return lac;
	}
	public void setLac(String lac) {
		this.lac = lac;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getLocation() {
		return (location==null) ? "" : location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getCellid() {
		return cellid;
	}
	public void setCellid(String cellid) {
		this.cellid = cellid;
	}

	public MediumType getMediumType() {
		return mediumType;
	}

	public void setMediumType(MediumType mediumType) {
		this.mediumType = mediumType;
	}
	public Long getMessageId() {
		return messageId;
	}
	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}



	public OperatorCountry getOpco() {
		return opco;
	}



	public void setOpco(OperatorCountry opco) {
		this.opco = opco;
	}

	
	
}

