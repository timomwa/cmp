package com.pixelandtag.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.json.JSONArray;
import org.json.JSONObject;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.security.UserSessionI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.cmp.entities.audit.AuditTrail;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.subscription.dto.SubscriptionStatus;

public class CustomerCare extends BaseActionBean {
	
	private String msisdn;
	private String query;
	private String status;
	private Long subscription_id;
	private String callback;
	private int start = 0;
	private int limit = 10;
	
	@EJB(mappedName =  "java:global/cmp/SubscriptionEJB")
	private SubscriptionBeanI subscriptionBean;
	
	@EJB(mappedName =  "java:global/cmp/CMPResourceBean")
	private CMPResourceBeanRemote cmpBean;
	
	@EJB(mappedName =  "java:global/cmp/UserSessionEJB")
    private UserSessionI usersessionEJB;
	
	@EJB(mappedName = "java:global/cmp/DatingServiceBean")
	private DatingServiceI datingServiceI;
	
	private Logger logger = Logger.getLogger(getClass());
	
	private List<String> services = new ArrayList<String>();
	
	@PostConstruct
	public void init(){
		services.add("BILLING_SERV5");
		services.add("BILLING_SERV15");
		services.add("BILLING_SERV30");
	}
	
	
	public Resolution changeSubscriptionStatus() throws Exception{
		
		Subject currentUser = SecurityUtils.getSubject();
		
		
		JSONObject jsob = new JSONObject();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("subscription_id : "+subscription_id);
		sb.append(" msisdn : "+msisdn);
		sb.append(" status : "+status);
		sb.append(" SubscriptionStatus.get(status) : "+SubscriptionStatus.get(status));
		
		if ( currentUser.isAuthenticated() ) {
			
			boolean success = subscriptionBean.updateSubscription(subscription_id.intValue(), msisdn, SubscriptionStatus.get(status), AlterationMethod.customer_care_interface); 
			
			try{
				
				Subscription subscr = cmpBean.find(Subscription.class,subscription_id);
				
				SMSService smsserv = subscr!=null ? cmpBean.find(SMSService.class, subscr.getSms_service_id_fk()) : null;
				
				boolean isstilldating =  subscriptionBean.hasSubscribedToAnyOfTheseServices(msisdn, services);
				
				if(smsserv!=null && !(isstilldating)){
					
					if((smsserv.getService_description()!=null && !smsserv.getService_description().isEmpty()
							&& smsserv.getService_description().toLowerCase().contains("dating"))
							||
							(smsserv.getService_name()!=null && !smsserv.getService_name().isEmpty()
							&& smsserv.getService_name().toLowerCase().contains("dating"))
							){
					
						if(SubscriptionStatus.get(status) == SubscriptionStatus.unsubscribed){

							datingServiceI.deactivate(msisdn);
							
						}else{
							
							datingServiceI.reactivate(msisdn);
						}
					}
				}
				
			}catch(Exception exp){
				logger.warn(exp.getMessage(),exp);
			}
			
			
			jsob.put("success", success);
			jsob.put("message", "Successfully "+(status.equalsIgnoreCase("confirmed") ? "renewed subscription for":"unsubscribed")+" "+msisdn);
			
			
			try{
				AuditTrail action = new AuditTrail.UserActionBuilder((User)currentUser.getSession().getAttribute("user"))
										.module("customerCare")
										.objectAffected(Subscription.class.getCanonicalName())
										.process("changeSubscriptionStatus")
										.timeStamp(new Date())
										.timeZone(TimeZone.getDefault().getID())
										.data(sb.toString())
										.remoteHost(getRemoteHost())
										.build();
				usersessionEJB.createAuditTrail(action);
			}catch(Exception exp){
				logger.error(exp.getMessage(),exp);
			}
			
			
		}else{
			jsob.put("success", false);
			jsob.put("message", "You're not logged in. Please log in first.");
			
		}
		
		return sendResponse(jsob.toString());
		
	}
	
	@DefaultHandler
	public Resolution getSubscriptions() throws Exception {
		
		Subject currentUser = SecurityUtils.getSubject();
		
		if(query !=null && !query.isEmpty())
			currentUser.getSession().setAttribute("query",query);
		else
			setQuery((String)currentUser.getSession().getAttribute("query"));
		
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
				subscr.put("pricepointkeyword", smsservice.getPrice_point_keyword());
				subscr.put("status", sub.getSubscription_status().toString());
				subscr.put("price", smsservice.getPrice());
				subscr.put("expiryDate", sub.getExpiryDate());
				subscr.put("renewal_count", sub.getRenewal_count());
				
				subscriptions.put(subscr);
				
			}
			subscription_root.put("subscriptions",subscriptions);
			subscription_root.put("totalCount",size);
			
			
			try{
				AuditTrail action = new AuditTrail.UserActionBuilder((User)currentUser.getSession().getAttribute("user"))
										.module("customerCare")
										.objectAffected(Subscription.class.getCanonicalName())
										.process("viewsubscriptions")
										.timeStamp(new Date())
										.timeZone(TimeZone.getDefault().getID())
										.remoteHost(getRemoteHost())
										.data(subscription_root.toString())
										.build();
				usersessionEJB.createAuditTrail(action);
			}catch(Exception exp){
				logger.error(exp.getMessage(),exp);
			}
			
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

	public Long getSubscription_id() {
		return subscription_id;
	}

	public void setSubscription_id(Long subscription_id) {
		this.subscription_id = subscription_id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	

}
