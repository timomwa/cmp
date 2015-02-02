package com.pixelandtag.web.beans;

import javax.servlet.http.HttpServletRequest;

import com.pixelandtag.entities.MOSms;

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
public class RequestObject {
	
	private String telcoid, msisdn, msg, keyword,countryCode,testBalance,litmus, tripWire = null, serviceActive="1";
	int serviceid;
	
	public RequestObject(MOSms request) throws Exception {
		
		String telcoid, msisdn, msg = "", keyword,testBalance,tripWire = null, litmus = null;
		int price, serviceid;
		telcoid = null;
		msisdn = null;
		msg = null;
		keyword = null;
		price=0;
		testBalance=null;
		serviceid = request.getServiceid();
		
		
		if (request.getMsisdn() != null)
			msisdn = request.getMsisdn();
		
		if (request.getSMS_Message_String() != null){
			msg = request.getSMS_Message_String().trim();
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
				
				keyword = replaceAllIllegalCharacters(keyword).trim();;
				
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

	public RequestObject(HttpServletRequest request) throws Exception {
		
		String telcoid, msisdn, msg, keyword,testBalance,tripWire = null, litmus = null;
		int price,serviceid=-1;
		telcoid = null;
		msisdn = null;
		msg = null;
		keyword = null;
		price=0;
		testBalance=null;
		
		if (request.getParameter("serviceid") != null){
			try{
				serviceid = Integer.valueOf(request.getParameter("serviceid"));
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
		
		if (request.getParameter("keyword") != null)
			keyword = request.getParameter("keyword").toUpperCase();
		
		if (keyword == null && msg !=null){
			keyword = msg.split("[\\s]")[0];
			//keyword = request.getParameter("keyword");
			/*if(msg!=null)
				msg = msg.split(keyword)[1];*/
		}/*else{
			//System.out.println("msg: "+msg);
			keyword = msg.split("[\\s]")[0];
		}*/
		
		if (request.getParameter("serviceActive") != null)
			serviceActive = request.getParameter("serviceActive");
		
		
		
		setTripWire(tripWire);
		setTestBalance(testBalance);
		setPrice(price);
		
		try{
			
			if(msg!=null){
				msg = msg.trim();
				
				msg = msg.toUpperCase();
			}
			
			if(keyword!=null){
				keyword = keyword.trim();
				
				keyword = keyword.toUpperCase();
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

	public int getServiceid() {
		return serviceid;
	}

	public void setServiceid(int serviceid) {
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
	
}

