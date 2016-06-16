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
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.api.MessageStatus;
import com.pixelandtag.bulksms.BulkSMSAccount;
import com.pixelandtag.bulksms.BulkSMSPlan;
import com.pixelandtag.bulksms.IPAddressWhitelist;

@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class BulkSMSUtilEJB implements BulkSMSUtilBeanI {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Resource
	private UserTransaction utx;
	
	

	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#getPlanQueueStatus(com.pixelandtag.bulksms.BulkSMSPlan, java.lang.String, java.lang.String, java.lang.String)
	 */
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
				MessageStatus status = (MessageStatus)o[1];
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
	
	
	
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#getCurrentOutgoingQueue(com.pixelandtag.bulksms.BulkSMSPlan, com.pixelandtag.api.MessageStatus)
	 */
	@Override
	public BigInteger getCurrentOutgoingQueue(BulkSMSPlan plan,MessageStatus status)  {
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
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#getPlanBalance(com.pixelandtag.bulksms.BulkSMSPlan)
	 */
	@Override
	public BigInteger getPlanBalance(BulkSMSPlan plan) throws PlanBalanceException { 
		BigInteger planBalance = null;
		try{
			Query query = em.createQuery("select coalesce((pln.numberOfSMS - count(*)), pln.numberOfSMS), pln.id from BulkSMSQueue q, BulkSMSText txt, BulkSMSPlan pln"
					+ " WHERE q.text=txt and txt.plan=:plan group by pln.id");
			query.setParameter("plan", plan);
			Object[] rest = (Object[]) query.getSingleResult();
			
			planBalance = (BigInteger) rest[0];
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
	
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#hostAllowed(com.pixelandtag.bulksms.BulkSMSAccount, java.lang.String)
	 */
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
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#hostAllowed(java.lang.String)
	 */
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

	
		
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#getAccout(java.lang.String, java.lang.String, java.lang.String)
	 */
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
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#getPlan(com.pixelandtag.bulksms.BulkSMSAccount, java.lang.String)
	 */
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
			throw new PlanException("Couldn't find any plan with the given parameters planid:"+planid);
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw new PlanException("An error occurred while fetching plan. Please try again later");
		}
		return plan;
	}

	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#getWhitelist(java.lang.String, com.pixelandtag.bulksms.BulkSMSAccount)
	 */
	@Override
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

}
