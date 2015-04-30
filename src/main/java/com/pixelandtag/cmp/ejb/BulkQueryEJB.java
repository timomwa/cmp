package com.pixelandtag.cmp.ejb;

import java.math.BigInteger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.api.MTStatus;
import com.pixelandtag.bulksms.BulkSMSAccount;
import com.pixelandtag.bulksms.BulkSMSPlan;
import com.pixelandtag.cmp.ejb.APIAuthenticationException;
import com.pixelandtag.cmp.ejb.BulkSMSI;
import com.pixelandtag.cmp.ejb.ParameterException;
import com.pixelandtag.cmp.ejb.PlanBalanceException;
import com.pixelandtag.cmp.ejb.PlanException;

@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class BulkQueryEJB implements BulkQueryI {
	
	private Logger logger = Logger.getLogger(getClass());

	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	

	@Resource
	private UserTransaction utx;
	
	@EJB
	private BulkSMSUtilBeanI util_ejb;
	
	@Override
	public String query(String sourceIp, String apiKey, String username,
			String password, String jsonString) throws APIAuthenticationException,ParameterException, JSONException, PlanException, PlanBalanceException{
		
		JSONObject inJso  =  new JSONObject(jsonString);
		String planid = null;
		String telcoid = null;
		String senderid = null;
		String price = null;
		
		try{
			planid = inJso.getString("planid");
		}catch(JSONException jse){
			logger.warn("planid not provided");
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
				
		StringBuffer sb = new StringBuffer();
		sb.append("planid").append(" : ").append(planid).append("\n");
		sb.append("telcoid").append(" : ").append(telcoid).append("\n");
		sb.append("senderid").append(" : ").append(senderid).append("\n");
		sb.append("price").append(" : ").append(price).append("\n");
				
		if(planid==null){
			throw new ParameterException("Missing parameters. Parameters: ["+sb.toString()+"]");
		}
				
		logger.info("\n\n incoming batch: "+sb.toString());
				
		BulkSMSAccount account = util_ejb.getAccout(apiKey,username,password); 
		boolean hostAllowed = util_ejb.hostAllowed(account, sourceIp);
		if(!hostAllowed){
			throw new APIAuthenticationException("Host not allowed.");
		}
		

		BulkSMSPlan plan = util_ejb.getPlan(account,planid);
		BigInteger planBalance = util_ejb.getPlanBalance(plan);
		String planstatus_str = util_ejb.getPlanQueueStatus(plan,telcoid,senderid,price); 
		JSONObject planstatus = new JSONObject(planstatus_str);
		BigInteger currentoutgoingsize = util_ejb.getCurrentOutgoingQueue(plan,MTStatus.RECEIVED);
			
		planstatus.put("activationDate", plan.getDatePurchased());
		planstatus.put("bundle_size", plan.getNumberOfSMS().intValue());
		planstatus.put("bundle_balance", planBalance.intValue());
		planstatus.put("bundle_outgoing_queue", currentoutgoingsize.intValue());
		planstatus.put("bundle_usage", plan.getNumberOfSMS().subtract(planBalance).intValue());
		planstatus.put("telcoid", plan.getTelcoid());
		planstatus.put("validity", (plan.getValidity()+" "+plan.getTimeunit() + (plan.getValidity()>1 ? "s":"")).toLowerCase());
		planstatus.put("active", plan.getActive());
		planstatus.put("planid", plan.getPlanid());
		JSONObject jsob = new JSONObject();
		jsob.put("success", true);
		jsob.put("message", planstatus);
		return jsob.toString();
	}

}
