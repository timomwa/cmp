package com.pixelandtag.action;

import java.math.BigInteger;
import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.persistence.Query;

import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.LifecycleStage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.cmp.ejb.content.ContentManagementI;
import com.pixelandtag.cmp.entities.SMSMenuLevels;
import com.pixelandtag.cmp.entities.SMSServiceMetaData;

public class SMSMenuManagementAction extends BaseActionBean {
	private Logger log = Logger.getLogger(this.getClass());
	@org.stripesstuff.plugin.session.Session(key = "smsmenu")
	private SMSMenuLevels smsmenu;
	private String sms;
	private int start = 0;
	private int limit = 20;
	private String query;
	private Long contentid;
	private String callback;
	
	@EJB(mappedName =  "java:global/cmp/ContentManagementImpl")
	private ContentManagementI contentManagementBean;
	
	
	@Before(on = {"saveOrUpdate","listContent"}, stages = LifecycleStage.BindingAndValidation)
	public void rehydrate() {
		if (smsmenu == null) {
			log.info("SMSMenuLevels is null,trying to get the params...");
			String id = getContext().getRequest().getParameter("smsmenu.id");
			log.info("Keyword id = "+id);
			//smsmenu = (SMSMenuLevels) getContext().getRequest().getSession()
			//		.getAttribute("smsmenu");
			if(!StringUtils.isEmpty(id)){
				smsmenu = cmp_dao.find(SMSMenuLevels.class, new Long(id));
			}
		} else {
			log.info("Got an member from the session :");
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public Resolution listContent() throws JSONException{
		
		JSONObject resp = new JSONObject();
		
		try{
			
			Query qry = cmp_dao.resource_bean.getEM().createQuery("from SMSServiceMetaData smd WHERE sms_service_id_fk=:sms_service_id_fk");
			qry.setParameter("sms_service_id_fk", smsmenu.getServiceid());
			List<SMSServiceMetaData> metaData = qry.getResultList();
			
			String db_name = null;
			String table = null;
			String static_category_value = null;
			for(SMSServiceMetaData mtda : metaData){
				if(mtda.getMeta_field().equals("static_categoryvalue"))
					static_category_value = mtda.getMeta_value();
				if(mtda.getMeta_field().equals("table"))
					table = mtda.getMeta_value();
				if(mtda.getMeta_field().equals("db_name")){
					db_name = mtda.getMeta_value();
				}
			}
			
			String err_msg = "The following meta data is not set for sms_service_id "+smsmenu.getServiceid() +"\n";
			boolean hasError = false;
			if(db_name==null){
				err_msg += " db_name ";
				hasError = true;
			}
			if(static_category_value==null){
				err_msg += " static_category_value ";
				hasError = true;
			}
			if(table==null){
				err_msg += " table ";
				hasError = true;
			}
			if(hasError)
				throw new MetaDataException(err_msg);
			
			log.info("db_name : "+db_name);
			log.info("table : "+table);
			log.info("static_category_value : "+static_category_value);
			log.info("smsmenu.getServiceid() : "+smsmenu.getServiceid());
			log.info("sms : "+sms);
			
			
			Query qry2 = cmp_dao.resource_bean.getEM().createNativeQuery("SELECT count(*) FROM `"+db_name+"`.`"+table+"` WHERE Category=:category");
			qry2.setParameter("category", static_category_value);
			Object obj = qry2.getSingleResult();
			BigInteger size = (BigInteger) obj;
			
			qry2 = cmp_dao.resource_bean.getEM().createNativeQuery("SELECT id,Text,timeStamp FROM `"+db_name+"`.`"+table+"` WHERE Category=:category");
			qry2.setParameter("category", static_category_value);
			qry2.setFirstResult(getStart());
			qry2.setMaxResults(getLimit());
			List<Object[]> list = qry2.getResultList();
			
			
			JSONArray data = new JSONArray();
			resp.put("total", size.intValue());
			for(Object[] o : list){
				Date timeStamp = (Date) o[2];
				String date = sdf2.format(timeStamp);
				String sms = (String) o[1];
				Integer id = (Integer) o[0];
				
				JSONObject smsrec  = new JSONObject();
				smsrec.put("id", id);
				smsrec.put("sms",sms);
				smsrec.put("date", date);
				smsrec.put("serviceid", smsmenu.getServiceid());
				
				data.put(smsrec);
			}
			
			resp.put("data", data);
			
		}catch(Exception exp){
			log.error(exp.getMessage(),exp);
			resp.put("success", false);
			resp.put("msg", "Problem occurred");
		}	
		
		if(callback!= null) {
			return sendResponse(callback + "("+resp.toString()+")","text/javascript");
		}else{
			return sendResponse(resp.toString(),"application/json");
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public Resolution deleteContent() throws JSONException{
		
		JSONObject jsob = new JSONObject();
		
		try{
			
			contentManagementBean.deleteContent( smsmenu.getServiceid(), contentid);
			
			jsob.put("success", true);
			jsob.put("message", "Content piece deleted successfully");
			
		}catch(Exception exp){
		
			log.error(exp.getMessage(),exp);
			jsob.put("success", false);
			jsob.put("message", "Problem deleting content :( Contact admin.");
		}
		
		return sendResponse(jsob.toString());
	}
	
	@SuppressWarnings("unchecked")
	public Resolution saveOrUpdate() throws JSONException{
		JSONObject jsob = new JSONObject();
		
		try{
			
			Query qry = cmp_dao.resource_bean.getEM().createQuery("from SMSServiceMetaData smd WHERE sms_service_id_fk=:sms_service_id_fk");
			qry.setParameter("sms_service_id_fk", smsmenu.getServiceid());
			List<SMSServiceMetaData> metaData = qry.getResultList();
			
			String db_name = null;
			String table = null;
			String static_category_value = null;
			for(SMSServiceMetaData mtda : metaData){
				if(mtda.getMeta_field().equals("static_categoryvalue"))
					static_category_value = mtda.getMeta_value();
				if(mtda.getMeta_field().equals("table"))
					table = mtda.getMeta_value();
				if(mtda.getMeta_field().equals("db_name")){
					db_name = mtda.getMeta_value();
				}
			}
			
			String err_msg = "The following meta data is not set for sms_service_id "+smsmenu.getServiceid() +"\n";
			boolean hasError = false;
			if(db_name==null){
				err_msg += " db_name ";
				hasError = true;
			}
			if(static_category_value==null){
				err_msg += " static_category_value ";
				hasError = true;
			}
			if(table==null){
				err_msg += " table ";
				hasError = true;
			}
			if(hasError)
				throw new MetaDataException(err_msg);
			
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
			
			
		}catch(MetaDataException exp){
			log.error(exp.getMessage(),exp);
			jsob.put("success", false);
			jsob.put("msg", "Problem saving content. ERROR "+exp.getMessage());
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


	public Logger getLog() {
		return log;
	}


	public void setLog(Logger log) {
		this.log = log;
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


	public Long getContentid() {
		return contentid;
	}


	public void setContentid(Long contentid) {
		this.contentid = contentid;
	}

	
	
}
