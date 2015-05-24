package com.pixelandtag.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.inject.Inject;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.json.JSONArray;
import org.json.JSONObject;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.dao.SMSServiceDAO;
import com.pixelandtag.model.GenericDao;

public class CustomerCare extends BaseActionBean {
	
	private String msisdn;
	private String query;
	private String callback;
	private int start = 0;
	private int limit = 10;
	
	@EJB(mappedName =  "java:global/cmp/SubscriptionEJB")
	private SubscriptionBeanI subscriptionBean;
	
	@EJB(mappedName =  "java:global/cmp/CMPResourceBean")
	private CMPResourceBeanRemote cmpBean;
	
	//@Inject
	//private SMSServiceDAO dao;
	
	private Logger logger = Logger.getLogger(getClass());
	
	@DefaultHandler
	public Resolution getSubscriptions() throws Exception {
		
		logger.info("query : "+query);
		logger.info("callback : "+callback);
		Subject currentUser = SecurityUtils.getSubject();
		
		Map<Long,SMSService> servicecache = new HashMap<Long,SMSService>();
		
		if ( currentUser.isAuthenticated() ) {
			
			JSONObject subscription_root = new JSONObject();
			
			JSONArray subscriptions = new  JSONArray();
			
			Long size = subscriptionBean.countsearchSubscription(query);
			
			List<Subscription> subscriptionlist =  subscriptionBean.searchSubscription(query,start,limit) ;
			
			for(Subscription sub : subscriptionlist){
				
				JSONObject subscr = new JSONObject();
				
				Long serviceid  = sub.getSms_service_id_fk();
				SMSService smsservice = servicecache.get(serviceid);
				if(smsservice==null)
					smsservice = cmpBean.find(SMSService.class,serviceid);
				
				subscr.put("id", sub.getId());
				subscr.put("msisdn", sub.getMsisdn());
				subscr.put("servicename", smsservice.getService_name());
				subscr.put("subscriptionDate", sub.getSubscription_timeStamp());
				
				subscriptions.put(subscr);
				
			}
			subscription_root.put("subscriptions",subscriptions);
			subscription_root.put("totalCount",size);
			
			if(callback!= null) {
			    return sendResponse(callback + "("+subscription_root.toString()+")","text/javascript");
			} else {
				return sendResponse(subscription_root.toString(),"application/x-json");
			}
		}else{
			
			return loginPage;
		
		}
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	

}
