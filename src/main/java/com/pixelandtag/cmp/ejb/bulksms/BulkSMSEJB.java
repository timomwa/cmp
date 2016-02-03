package com.pixelandtag.cmp.ejb.bulksms;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.HttpHeaders;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.api.MessageStatus;
import com.pixelandtag.bulksms.BulkSMSAccount;
import com.pixelandtag.bulksms.BulkSMSPlan;
import com.pixelandtag.bulksms.BulkSMSQueue;
import com.pixelandtag.bulksms.BulkSMSText;
import com.pixelandtag.bulksms.IPAddressWhitelist;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;

@Stateless
@Remote
public class BulkSMSEJB implements BulkSMSI {
	
	
	private Logger logger = Logger.getLogger(getClass());

	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
		
	@EJB
	BulkSMSUtilBeanI util_ejb;
	
	@EJB
	TimezoneConverterI timezoneEJB;
	
	
	@Override
	public void enqueue(String sourceIp, String apiKey,String username, String password,String jsonString) throws  Exception{//PlanException, APIAuthenticationException,ParameterException,PlanException, PersistenceException,JSONException,QueueFullException,PlanBalanceException{
	
		//boolean success = true;
		JSONObject inJso  =  new JSONObject(jsonString);
		String planid = null;
		String text = null;
		String schedule = null;
		String telcoid = null;
		String senderid = null;
		String price = null;
		String timezone = null;
		int priority = 1;
		try{
			planid = inJso.getString("planid");
		}catch(JSONException jse){
			logger.warn("planid not provided");
		}
		try{
			text = inJso.getString("text");
		}catch(JSONException jse){
			logger.warn("text not provided");
		}
		try{
			schedule = inJso.getString("schedule");
		}catch(JSONException jse){
			logger.warn("schedule not provided");
		}
		try{
			telcoid = inJso.getString("telcoid");
		}catch(JSONException jse){
			logger.warn("telcoid not provided");
		}
		try{
			senderid = inJso.getString("senderid");
		}catch(JSONException jse){
			logger.warn("senderid not provided");
		}
		try{
			price = inJso.getString("price");
		}catch(JSONException jse){
			logger.warn("price not provided");
		}
		
		try{
			priority = inJso.getInt("priority");
		}catch(JSONException jse){
			logger.warn("priority not provided");
		}
		
		JSONArray msisdnlist = null;
		try{
			msisdnlist = inJso.getJSONArray("msisdnlist");
		}catch(JSONException jse){
			logger.warn("msisdnlist not provided");
		}
		
		
		try{
			timezone = inJso.getString("timezone");
		}catch(JSONException jse){
			logger.warn("priority not provided");
		}
		StringBuffer sb = new StringBuffer();
		sb.append("planid").append(" : ").append(planid).append("\n");
		sb.append("text").append(" : ").append(text).append("\n");
		sb.append("telcoid").append(" : ").append(telcoid).append("\n");
		sb.append("senderid").append(" : ").append(senderid).append("\n");
		sb.append("price").append(" : ").append(price).append("\n");
		sb.append("msisdnlist").append(" : ").append(msisdnlist).append("\n");
		sb.append("priority").append(" : ").append(priority).append("\n");
		sb.append("timezone").append(" : ").append(timezone).append("\n");
		sb.append("schedule").append(" : ").append(schedule).append("\n");
		
		if((schedule!=null && timezone==null || timezone.isEmpty()) || (planid==null|| planid.isEmpty())||(text==null|| text.isEmpty())||(telcoid==null|| telcoid.isEmpty())||(senderid==null|| senderid.isEmpty())||(price==null || price.isEmpty())||(msisdnlist==null || msisdnlist.length()==0)||priority<0){
			throw new ParameterException("Missing parameters. Parameters: ["+sb.toString()+"]");
		}
		boolean tz_valid = timezoneEJB.validateTimezone(timezone);
		if(!tz_valid){
			throw new ParameterException("Timezone format wrong. Examples of timezone. \"America/New_York\", \"Africa/Nairobi\"");
		}
		Date sheduledate_server_time = null;
		try {
			if(schedule==null || schedule.isEmpty()){
				sheduledate_server_time = timezoneEJB.convertFromOneTimeZoneToAnother(timezoneEJB.stringToDate(schedule), timezone,TimeZone.getDefault().getID());
			}else{
				sheduledate_server_time = DateTime.now().toDate();
			}
			boolean isinthepast = timezoneEJB.isDateInThePast(sheduledate_server_time);
			if(isinthepast)
				throw new ParameterException("The schedule date is in the past.");
		} catch (ParseException e) {
			throw new ParameterException("Could not parse the scheduledate. Check if your date format and timezone is correct. Timezone example : Africa/Nairobi . Date format should be yyyy-MM-dd HH:mm:ss where"
									+"\n yyyy – The year, e.g 2015 "
									+"\n MM – The date, e,g 01 for January "
									+"\n dd – The day of the month e.g 31 "
									+"\n HH – the hour of the day between 0 and 23 "
									+"\n mm – the minute, e,g 03. between 0 and 59 ");
		}
		
		sb.append("sheduledate").append(" : ").append(sheduledate_server_time).append("\n");
		
		logger.info("\n\n incoming batch: "+sb.toString());
		
		BulkSMSAccount account = util_ejb.getAccout(apiKey,username,password);
		if(!account.getActive())
			throw new APIAuthenticationException("This plan is not active. Contact support.");
		boolean hostAllowed = util_ejb.hostAllowed(account, sourceIp);
		if(!hostAllowed)
			throw new APIAuthenticationException("Host not allowed.");
		
		
		BulkSMSPlan plan = util_ejb.getPlan(account,planid);
		BigInteger planBalance = util_ejb.getPlanBalance(plan);
		BigInteger thisBatch = BigInteger.valueOf(msisdnlist.length());
		
		logger.info(" PLAN BALANCE BEFORE ::::::: "+planBalance.intValue());
		logger.info(" THIS BATCH BEFORE ::::::: "+thisBatch.intValue());
		logger.info(" NOT ENOUGH BALANCE ?::::::: "+(planBalance.compareTo(thisBatch)<0));
		
		if(planBalance.compareTo(thisBatch)<0){//if we don't have enough balance.
			throw new PlanBalanceException("This plan doesn't have enough credits. Current plan balance is "+planBalance.intValue()+", this batch: "+thisBatch.intValue());
		}
		
		BigInteger currentoutgoingsize = util_ejb.getCurrentOutgoingQueue(plan,MessageStatus.RECEIVED);
		
		if(plan.getMaxoutqueue().compareTo(currentoutgoingsize)<=0)//queue full
			throw new QueueFullException("Queue is full, please try again.");
		
		
		BulkSMSText textb = new BulkSMSText();
		textb.setContent(text);
		textb.setPlan(plan);
		textb.setSenderid(senderid);
		textb.setQueueSize(BigInteger.valueOf(msisdnlist.length()));
		textb.setSheduledate(sheduledate_server_time);
		textb.setTimezone(timezone);
		textb.setPrice(new BigDecimal(price));
		try{
			
			textb = em.merge(textb);
			
			for(int x = 0; x<msisdnlist.length(); x++){
				BulkSMSQueue queue = new BulkSMSQueue();
				queue.setMsisdn((String) msisdnlist.get(x));
				queue.setPriority(priority);
				queue.setStatus(MessageStatus.RECEIVED);
				queue.setText(textb);
				queue = em.merge(queue);
			}
			
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw new PersistenceException("Could not enqueue this batch. Please try again.",exp);
		}finally{
			
		}
	}

}
