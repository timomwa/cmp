package com.pixelandtag.action;

import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.LifecycleStage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.cmp.entities.SMSMenuLevels;
import com.pixelandtag.cmp.entities.SMSServiceMetaData;

public class SMSMenuManagementAction extends BaseActionBean {
	private Logger log = Logger.getLogger(this.getClass());
	@org.stripesstuff.plugin.session.Session(key = "smsmenu")
	private SMSMenuLevels smsmenu;
	private String sms;
	
	
	@Before(on = "saveOrUpdate", stages = LifecycleStage.BindingAndValidation)
	public void rehydrate() {
		if (smsmenu == null) {
			log.info("SMSMenuLevels is null,trying to get the params...");
			String id = getContext().getRequest().getParameter("smsmenu.id");
			log.info("Keyword id = "+id);
			smsmenu = (SMSMenuLevels) getContext().getRequest().getSession()
					.getAttribute("smsmenu");
			if(!StringUtils.isEmpty(id)){
				smsmenu = cmp_dao.find(SMSMenuLevels.class, new Long(id));
			}
		} else {
			log.info("Got an member from the session :");
		}
	}
	
	@SuppressWarnings("unchecked")
	public Resolution saveOrUpdate() throws JSONException{
		JSONObject jsob = new JSONObject();
		
		try{
			
			Query qry = cmp_dao.resource_bean.getEM().createQuery("from SMSServiceMetaData smd WHERE sms_service_id_fk=:sms_service_id_fk");
			qry.setParameter("sms_service_id_fk", smsmenu.getServiceid());
			List<SMSServiceMetaData> metaData = qry.getResultList();
			
			String db_name = "pixeland_content360";
			String table = "static_content";
			String static_category_value = "";
			for(SMSServiceMetaData mtda : metaData){
				if(mtda.getMeta_field().equals("static_categoryvalue"))
					static_category_value = mtda.getMeta_value();
				if(mtda.getMeta_field().equals("table"))
					table = mtda.getMeta_value();
				if(mtda.getMeta_field().equals("db_name")){
					db_name = mtda.getMeta_value();
				}
			}
			
			log.info("db_name : "+db_name);
			log.info("table : "+table);
			log.info("static_category_value : "+static_category_value);
			log.info("smsmenu.getServiceid() : "+smsmenu.getServiceid());
			log.info("sms : "+sms);
			
			
			Query qry2 = cmp_dao.resource_bean.getEM().createNativeQuery("SELECT timeStamp FROM `"+db_name+"`.`"+table+"` WHERE Text=:text AND Category=:category");
			qry2.setParameter("text", sms);
			qry2.setParameter("category", static_category_value);
			
			if(qry2.getResultList().size()>0){
				Date timeStamp = (Date) qry2.getResultList().get(0);
				String date = sdf.format(timeStamp);
				jsob.put("success", false);
				jsob.put("msg", "This content piece was uploaded on "+date+". Category: "+static_category_value);
			}else{
				boolean success =  cmp_dao.resource_bean.saveStaticSMS(db_name,table,static_category_value,sms);
				jsob.put("success", success);
				jsob.put("msg", "Successfully uploaded content!");
			}
			
			
		}catch(Exception exp){
			log.error(exp.getMessage(),exp);
			jsob.put("success", false);
			jsob.put("msg", "Problem saving content :( Contact admin.");
		}
		
		return sendResponse(jsob.toString());
		
	}
	
	@SuppressWarnings("unchecked")
	@DefaultHandler
	public Resolution listMenu() throws JSONException{
		List<SMSMenuLevels> parentsl ;
		Query qry = cmp_dao.resource_bean.getEM().createQuery("from SMSMenuLevels sm WHERE sm.serviceid=-1");
		parentsl  = qry.getResultList();
		JSONObject resp = new JSONObject();
		resp.put("size", parentsl.size());
		for(SMSMenuLevels parent :parentsl){
			List<SMSMenuLevels> children;
			try {
				children = cmp_dao.resource_bean.listChildren(parent.getId());
				if(children!=null && children.size()>0)
					parent.setChildren(children);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			resp.append("menuitem", parent.toJson());
			
		}
		
		
		
		
		//Get children
		
		
		/*List<SMSMenuLevels> test;
		qry = cmp_dao.resource_bean.getEM().createQuery("from SMSMenuLevels sm WHERE sm.parent_level_id=-1");
		test = qry.getResultList();
		Iterator<SMSMenuLevels> kws = test.iterator();
		
		while(kws.hasNext()){
			SMSMenuLevels kw = kws.next();
			try {
				List<SMSMenuLevels> children  = cmp_dao.resource_bean.listChildren(kw.getId());
				if(children!=null && children.size()>0)
					kw.setChildren(children);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
			
			log.info("kw : "+kw.toJson());
			resp.append("menuitem", kw.toJson());
		}*/
		return sendResponse(resp.toString());
	}

	public SMSMenuLevels getSmsmenu() {
		return smsmenu;
	}

	public void setSmsmenu(SMSMenuLevels smsmenu) {
		this.smsmenu = smsmenu;
	}

	public String getSms() {
		return sms;
	}

	public void setSms(String sms) {
		this.sms = sms;
	}

	
	
}
