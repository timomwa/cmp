package com.pixelandtag.cmp.ejb;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.api.MTStatus;
import com.pixelandtag.bulksms.BulkSMSAccount;
import com.pixelandtag.bulksms.BulkSMSPlan;
import com.pixelandtag.bulksms.BulkSMSQueue;
import com.pixelandtag.bulksms.BulkSMSText;
import com.pixelandtag.bulksms.IPAddressWhitelist;

@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class BulkSMSEJB implements BulkSMSI {
	
	
	private Logger logger = Logger.getLogger(getClass());

	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	

	@Resource
	private UserTransaction utx;
	
	@Override
	public boolean hostAllowed(BulkSMSAccount account,String sourceIp) throws APIAuthenticationException,ParameterException{
		boolean allowed = false;
		IPAddressWhitelist whitelist = null;
		try{
			whitelist = getWhitelist(sourceIp,account);
			if(whitelist!=null)
				allowed = whitelist.getActive();
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw new APIAuthenticationException("An error occurred while authenticating account. Please try again later");
		}
		return allowed;
	}
	
	
	@Override
	public boolean hostAllowed(String sourceIp) throws APIAuthenticationException,ParameterException{
		boolean allowed = false;
		IPAddressWhitelist whitelist = null;
		try{
			whitelist = getWhitelist(sourceIp,null);
			if(whitelist!=null)
				allowed = whitelist.getActive();
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw new APIAuthenticationException("An error occurred while authenticating account. Please try again later");
		}
		return allowed;
	}
	

	public BigInteger getCurrentOutgoingQueue(BulkSMSPlan plan,MTStatus status)  {
		BigInteger planBalance = null;
		try{
			Query query = em.createQuery("select coalesce(count(*),0) from BulkSMSQueue q, BulkSMSText txt, BulkSMSPlan pln"
					+ " WHERE q.text=txt and q.status=:status AND txt.plan=:plan");
			query.setParameter("plan", plan);
			query.setParameter("status", status);
			Long val = ((Long) query.getSingleResult());
			planBalance = BigInteger.valueOf( (val==null? 0L: val) );
			
			if(planBalance==null)
				planBalance = BigInteger.ZERO;
			
		}catch(javax.persistence.NoResultException nre){
			logger.warn(nre.getMessage()+"This plan has no sms");
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		return planBalance;
	}
	
	
	@Override
	public BigInteger getPlanBalance(BulkSMSPlan plan) throws PlanBalanceException { 
		BigInteger planBalance = null;
		try{
			Query query = em.createQuery("select coalesce((pln.numberOfSMS - count(*)), pln.numberOfSMS) from BulkSMSQueue q, BulkSMSText txt, BulkSMSPlan pln"
					+ " WHERE q.text=txt and txt.plan=:plan");
			query.setParameter("plan", plan);
			planBalance = (BigInteger) query.getSingleResult();
			
			if(planBalance==null)
				planBalance = plan.getNumberOfSMS();
			
		}catch(javax.persistence.NoResultException nre){
			logger.warn(nre.getMessage()+"This plan has no sms");
			throw new PlanBalanceException("Could not get the plan balance. Does it exist?");
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw new PlanBalanceException("Could not get the plan balance.");
		}
		return planBalance;
	}
	
	
	public IPAddressWhitelist getWhitelist(String ipAddress,BulkSMSAccount account) throws APIAuthenticationException,ParameterException{
		IPAddressWhitelist whitelist = null;
		try{
			Query query = em.createQuery("from IPAddressWhitelist ip WHERE ip.ipaddress=:ipaddress"+(account!=null ? " AND ip.account=:account" : ""));
			query.setParameter("ipaddress", ipAddress);
			if(account!=null)
				query.setParameter("account", account);
			query.setFirstResult(0);
			query.setMaxResults(1);
			whitelist = (IPAddressWhitelist) query.getSingleResult();
		}catch(javax.persistence.NoResultException nre){
			logger.warn(nre.getMessage()+" Couldn't find the ip address "+ipAddress+" in whitelist.");
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw new APIAuthenticationException("An error occurred while authenticating account. Please try again later");
		}
		return whitelist;
	}
	
	@Override
	public BulkSMSAccount getAccout(String apiKey,String username, String password) throws APIAuthenticationException{
		BulkSMSAccount bulkaccount = null;
		try{
			Query query = em.createQuery("from BulkSMSAccount ba WHERE ba.username=:username AND ba.password=:password");
			query.setParameter("username", username);
			query.setParameter("password", password);
			query.setFirstResult(0);
			query.setMaxResults(1);
			bulkaccount = (BulkSMSAccount) query.getSingleResult();
			if(!bulkaccount.getApiKey().equals(apiKey))
				throw new APIAuthenticationException("The API key provided ("+apiKey+") is incorrect. Please provide correct API key");
			
		}catch(javax.persistence.NoResultException nr){
			logger.warn("couldn't find an account with given parameters");
			throw new APIAuthenticationException("No account found with the provided credentials");
		}catch(APIAuthenticationException authe){
			logger.error(authe.getMessage(),authe);
			throw authe;
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw new APIAuthenticationException("An error occurred while authenticating account. Please try again later");
		}
		return bulkaccount;
	}
	
	
	
	@Override
	public BulkSMSPlan getPlan(BulkSMSAccount account, String planid) throws PlanException{
		
		BulkSMSPlan plan = null;
		
		if(planid==null || planid.isEmpty())
			throw new PlanException("Could not retrieve plan id not provided.");
		
		try{
			Query query = em.createQuery("from BulkSMSPlan p WHERE p.planid=:planid AND p.account=:account");
			query.setParameter("planid", planid);
			query.setParameter("account", account);
			query.setFirstResult(0);
			query.setMaxResults(1);
			plan = (BulkSMSPlan) query.getSingleResult();
		}catch(javax.persistence.NoResultException nr){
			logger.warn("Couldn't find an plan with the given parameters planid:"+planid);
			throw new PlanException("Couldn't find an plan with the given parameters planid:"+planid);
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw new PlanException("An error occurred while fetching plan. Please try again later");
		}
		return plan;
	}

	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public void enqueue(String sourceIp, String apiKey,String username, String password,String jsonString) throws APIAuthenticationException,ParameterException,PlanException, PersistenceException,JSONException,QueueFullException,PlanBalanceException{
	
		//boolean success = true;
		JSONObject inJso  =  new JSONObject(jsonString);
		String planid = null;
		String text = null;
		String schedule = null;
		String telcoid = null;
		String senderid = null;
		String price = null;
		String timezone = null;
		int priority = 3;
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
		boolean tz_valid = validateTimezone(timezone);
		if(!tz_valid){
			throw new ParameterException("Timezone format wrong. Examples of timezone. \"America/New_York\", \"Africa/Nairobi\"");
		}
		Date sheduledate = null;
		try {
			sheduledate = stringToDate(schedule);
			boolean isinthepast = isDateInThePast(sheduledate,timezone);
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
		
		sb.append("sheduledate").append(" : ").append(sheduledate).append("\n");
		com.inmobia.util.StopWatch s;
		logger.info("\n\n incoming batch: "+sb.toString());
		
		BulkSMSAccount account = getAccout(apiKey,username,password);
		if(!account.getActive())
			throw new APIAuthenticationException("This plan is not active. Contact support.");
		boolean hostAllowed = hostAllowed(account, sourceIp);
		if(!hostAllowed)
			throw new APIAuthenticationException("Host not allowed.");
		
		
		BulkSMSPlan plan = getPlan(account,planid);
		BigInteger planBalance = getPlanBalance(plan);
		BigInteger thisBatch = BigInteger.valueOf(msisdnlist.length());
		
		logger.info(" PLAN BALANCE BEFORE ::::::: "+planBalance.intValue());
		logger.info(" THIS BATCH BEFORE ::::::: "+thisBatch.intValue());
		logger.info(" NOT ENOUGH BALANCE ?::::::: "+(planBalance.compareTo(thisBatch)<0));
		
		if(planBalance.compareTo(thisBatch)<0){//if we don't have enough balance.
			throw new PlanBalanceException("This plan doesn't have enough credits. Current plan balance is "+planBalance.intValue()+", this batch: "+thisBatch.intValue());
		}
		
		BigInteger currentoutgoingsize = getCurrentOutgoingQueue(plan,MTStatus.RECEIVED);
		
		if(plan.getMaxoutqueue().compareTo(currentoutgoingsize)<=0)//queue full
			throw new QueueFullException("Queue is full, please try again.");
		
		
		BulkSMSText textb = new BulkSMSText();
		textb.setContent(text);
		textb.setPlan(plan);
		textb.setSenderid(senderid);
		textb.setQueueSize(BigInteger.valueOf(msisdnlist.length()));
		textb.setSheduledate(sheduledate);
		textb.setTimezone(timezone);
		textb.setPrice(new BigDecimal(price));
		try{
			utx.begin();
			
			textb = em.merge(textb);
			
			for(int x = 0; x<msisdnlist.length(); x++){
				BulkSMSQueue queue = new BulkSMSQueue();
				queue.setMsisdn((String) msisdnlist.get(x));
				queue.setPriority(priority);
				queue.setStatus(MTStatus.RECEIVED);
				queue.setText(textb);
				queue = em.merge(queue);
			}
			
			utx.commit();
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			try{
				utx.rollback();
			}catch(Exception esp){}
			throw new PersistenceException("Could not enqueue this batch. Please try again.",exp);
		}finally{
			
		}
	}


	


	/**
	 * 
	 * @param schedule
	 * @param timezone
	 * @return
	 * @throws ParseException
	 */
	private boolean isDateInThePast(Date schedule, String timezone) throws ParseException {
		StringBuffer sb = new StringBuffer();
		DateFormat mdyFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		mdyFormat.setTimeZone(TimeZone.getTimeZone(timezone));
		Date now = new Date();
		String nowInOpcoTZ = mdyFormat.format(now);
		mdyFormat.setTimeZone(TimeZone.getDefault());
		Date now_to_opcotimezone = mdyFormat.parse(nowInOpcoTZ);
		
		boolean isinthepast = schedule.before(now_to_opcotimezone);
		
		sb.append("\n\n\t>>>>current timestamp to opco timezone: "+now_to_opcotimezone+" "+TimeZone.getTimeZone(timezone).getID());
		sb.append("\n\n\t>>>>current timestamp in this timezone: "+now+" "+TimeZone.getDefault().getID());
		sb.append("\n\n\t>>>>Schedule in opco timezone: "+schedule+" "+timezone);
		sb.append("\n\n\t>>>> this date is in the past ?: "+isinthepast);
		
		logger.info(sb.toString());
		
		return isinthepast;
	}


	/**
	 * Validates the timezone.
	 * Makes sure timezone is always word slash word e.g "America/New_York"
	 * @param timezone
	 * @return
	 */
	private boolean validateTimezone(String timezone) {
		String regex = "\\w+\\/\\w+";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(timezone);
		return matcher.matches();
	}


	/**
	 * Converts string to given timezone
	 * @param dateStr
	 * @param dateformat
	 * @return java.util.Date
	 * @throws ParseException
	 */
	private Date stringToDate(String dateStr, String dateformat) throws ParseException{
		DateFormat format = new SimpleDateFormat(dateformat);
		return format.parse(dateStr);
	}
	
	/**
	 * assumes date format to be 
	 * "yyyy-MM-dd HH:m:ss"
	 * 
	 * @param dateStr - java.lang.String
	 * @return date - java.util.Date 
	 * @throws ParseException
	 */
	private Date stringToDate(String dateStr) throws ParseException{
		return stringToDate(dateStr,"yyyy-MM-dd HH:m:ss");
	}
	/**
	 * Converts string to given timezone
	 * @param time
	 * @param dateformat
	 * @param timezone
	 * @return
	 * @throws ParseException
	 */
	private Date convertToThisTimezone(String time, String dateformat, TimeZone timezone) throws ParseException{
		DateFormat format = new SimpleDateFormat(dateformat);
		format.setTimeZone(timezone);
		return format.parse(time);
	}
	/**
	 * <p></p>
	 * @param dateformat, e.g "yyyy-MM-dd HH:m:ss"
	 * @param schedule
	 * @param scheduleutcoffset e.g  "America/New_York", "Africa/Nairobi"
	 * @return
	 * @throws ParseException 
	 */
	private Date convertToThisTimezone(String dateformat,String time,  String timeZone) throws ParseException {
		return convertToThisTimezone(time,dateformat,TimeZone.getTimeZone(timeZone));
	}
	
	private Date convertToThisTimezone(String time, String timeZone) throws ParseException {
		return convertToThisTimezone(time,"yyyy-MM-dd HH:m:ss",TimeZone.getTimeZone(timeZone));
	}
	private Date convertToThisTimezone(String time) throws ParseException {
		return convertToThisTimezone(time,"yyyy-MM-dd HH:m:ss",TimeZone.getDefault());
	}
	


	@SuppressWarnings("unchecked")
	@Override
	public String getPlanQueueStatus(BulkSMSPlan plan, String telcoid, String senderid, String price) {
		JSONObject jsob = new JSONObject();
		
		
		try{
			String lastpartofQuery = "";
			if(telcoid!=null && !telcoid.isEmpty())
				lastpartofQuery += ((lastpartofQuery.length()>0) ? "AND " :"") + "pln.telcoid=:telcoid ";
			if(senderid!=null && !senderid.isEmpty())
				lastpartofQuery += ((lastpartofQuery.length()>0) ? "AND " :"") + "txt.senderid=:senderid ";
			if(price!=null && !price.isEmpty())
				lastpartofQuery += ((lastpartofQuery.length()>0) ? "AND " :"") + "txt.price=:price ";
			
			lastpartofQuery = (lastpartofQuery.isEmpty() ? "" : "AND ")+lastpartofQuery;
			
			logger.info(">>>>>>>>>>>> "+lastpartofQuery);
			
			Query query = em.createQuery("select coalesce(count(*),0), q.status,coalesce(txt.sheduledate,timecreated), txt.timezone, q.text.id, txt.content from BulkSMSQueue q, BulkSMSText txt, BulkSMSPlan pln"
					+ " WHERE q.text=txt AND txt.plan=:plan "+lastpartofQuery+"  group by q.status,q.text.id, txt.sheduledate order by q.text.id desc");
			query.setParameter("plan", plan);
			if(telcoid!=null && !telcoid.isEmpty())
				query.setParameter("telcoid", telcoid);
			if(senderid!=null && !senderid.isEmpty())
				query.setParameter("senderid", senderid);
			if(price!=null && !price.isEmpty())
				query.setParameter("price", new BigDecimal(price));
			
			
			List<Object[]> obj = query.getResultList();
			
			Map<Long, JSONObject> statsar = new HashMap<Long, JSONObject>();
			
			for(Object[] o : obj){
				Long count = (Long)o[0];
				MTStatus status = (MTStatus)o[1];
				Date scheduletime = (Date)o[2];
				String timezone = (String)o[3];
				Long text_id = (Long)o[4];
				String text = (String)o[5];
				JSONObject stat =  statsar.get(text_id);
				
				String sname = "";
				if(stat!=null){
					try{
						sname = stat.getString("sname");
					}catch(JSONException je){
					}
				}
				
				if(!sname.equals(text_id))
					stat = new JSONObject();
				
				stat.put(status.toString(), count);
				stat.put("text_id",text_id);
				stat.put("scheduletime", (scheduletime==null ? "N/A" :scheduletime)  );
				stat.put("timezone", (timezone==null ? "N/A" :timezone));
				stat.put("text",text);
				statsar.put(text_id, stat);
				
			}
			
			
			if(statsar.size()<1)
				jsob.append("stats", new JSONObject());
			for(Long n : statsar.keySet())
				jsob.append("stats", statsar.get(n));
			
			
		}catch(javax.persistence.NoResultException nre){
			logger.warn(nre.getMessage()+"This plan has no sms");
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		return jsob.toString();
	}


	
	


	



	
}
