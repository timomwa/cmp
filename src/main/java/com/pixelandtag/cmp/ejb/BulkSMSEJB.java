package com.pixelandtag.cmp.ejb;

import java.math.BigInteger;
import java.util.List;

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
			logger.warn("priority not provided");
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("planid").append(" : ").append(planid).append("\n");
		sb.append("text").append(" : ").append(text).append("\n");
		sb.append("schedule").append(" : ").append(schedule).append("\n");
		sb.append("telcoid").append(" : ").append(telcoid).append("\n");
		sb.append("senderid").append(" : ").append(senderid).append("\n");
		sb.append("price").append(" : ").append(price).append("\n");
		sb.append("msisdnlist").append(" : ").append(msisdnlist).append("\n");
		sb.append("priority").append(" : ").append(priority).append("\n");
		
		if(planid==null||text==null||schedule==null||telcoid==null||senderid==null||price==null||msisdnlist==null||priority<0){
			throw new ParameterException("Missing parameters. Parameters: ["+sb.toString()+"]");
		}
		
		logger.info("\n\n incoming batch: "+sb.toString());
		
		BulkSMSAccount account = getAccout(apiKey,username,password);
		boolean hostAllowed = hostAllowed(account, sourceIp);
		if(!hostAllowed){
			throw new APIAuthenticationException("Host not allowed.");
			
		}
		
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


	


	@SuppressWarnings("unchecked")
	@Override
	public String getPlanQueueStatus(BulkSMSPlan plan) {
		JSONObject jsob = new JSONObject();
		
		try{
			
			Query query = em.createQuery("select coalesce(count(*),0), q.status from BulkSMSQueue q, BulkSMSText txt, BulkSMSPlan pln"
					+ " WHERE q.text=txt AND txt.plan=:plan group by q.status");
			query.setParameter("plan", plan);
			List<Object[]> obj = query.getResultList();
			
			for(Object[] o : obj){
				Long count = (Long)o[0];
				MTStatus status = (MTStatus)o[1];
				JSONObject stats = new JSONObject();
				stats.put("count", count);
				stats.put("status", status.toString());
				jsob.append("stats", stats);
			}
			
		}catch(javax.persistence.NoResultException nre){
			logger.warn(nre.getMessage()+"This plan has no sms");
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		return jsob.toString();
	}


	
	


	



	
}
