package com.pixelandtag.cmp.ejb;

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
	

	private IPAddressWhitelist getWhitelist(String ipAddress,BulkSMSAccount account) throws APIAuthenticationException,ParameterException{
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
	public void enqueue(String sourceIp, String apiKey,String username, String password,String jsonString) throws APIAuthenticationException,ParameterException,PlanException, PersistenceException,JSONException{
	
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
		BulkSMSText textb = new BulkSMSText();
		textb.setContent(text);
		textb.setPlan(plan);
		textb.setSenderid(senderid);
		
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
		
		//return success;
	}



	
}
