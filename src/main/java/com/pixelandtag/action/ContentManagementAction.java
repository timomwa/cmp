package com.pixelandtag.action;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.cmp.entities.SMSService;

import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.LifecycleStage;

public class ContentManagementAction extends BaseActionBean {
	private Logger log = Logger.getLogger(this.getClass());
	@org.stripesstuff.plugin.session.Session(key = "smsservice")
	private SMSService smsservice;
	
	@Before(on = "saveOrUpdateService", stages = LifecycleStage.BindingAndValidation)
	public void rehydrate() {
		if (smsservice == null) {
			log.info("SMSService is null,trying to get the params...");
			String id = getContext().getRequest().getParameter("smsservice.id");
			log.info("Keyword id = "+id);
			smsservice = (SMSService) getContext().getRequest().getSession()
					.getAttribute("smsservice");
			if(!StringUtils.isEmpty(id)){
				smsservice = cmp_dao.find(SMSService.class, new Long(id));
			}
		} else {
			log.info("Got an member from the session :");
		}
	}
	
	
	@DefaultHandler
	public Resolution listServices() throws JSONException{
		Collection<SMSService> test = cmp_dao.lists(SMSService.class, 0, 100);
		Iterator<SMSService> kws = test.iterator();
		JSONObject resp = new JSONObject();
		resp.put("size", test.size());
		while(kws.hasNext()){
			SMSService kw = kws.next();
			System.out.println(kw);
			log.info("kw : "+kw);
			resp.append("keywords", kw.toJson());
		}
		return sendResponse(resp.toString());
	}
	
	
	public Resolution saveOrUpdateService() throws Exception{
		smsservice = cmp_dao.saveOrUpdate(smsservice);
		JSONObject jsonob = new JSONObject();
		jsonob.put("success", true);
		jsonob.put("msg", "Successfully saved keyword");
		jsonob.put("id", smsservice.getId());
		return  sendResponse(jsonob.toString());
	}


	public SMSService getSmsservice() {
		return smsservice;
	}


	public void setSmsservice(SMSService smsservice) {
		this.smsservice = smsservice;
	}

	
	
	

}
