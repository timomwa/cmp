package com.pixelandtag.action;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import antlr.collections.List;

import com.pixelandtag.cmp.entities.Keyword;

import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.LifecycleStage;

public class ContentManagementAction extends BaseActionBean {
	private Logger log = Logger.getLogger(this.getClass());
	private Keyword keyword;
	
	@Before(on = "saveOrUpdateKeyword", stages = LifecycleStage.BindingAndValidation)
	public void rehydrate() {
		if (keyword == null) {
			log.info("Keyword is null,trying to get the params...");
			String id = getContext().getRequest().getParameter("keyword.id");
			log.info("Keyword id = "+id);
			keyword = (Keyword) getContext().getRequest().getSession()
					.getAttribute("keyword");
			if(!StringUtils.isEmpty(id)){
				keyword = cmp_dao.find(Keyword.class, new Long(id));
			}
		} else {
			log.info("Got an member from the session :");
		}
	}
	
	@DefaultHandler
	public Resolution listKeywords() throws JSONException{
		Collection<Keyword> test = cmp_dao.lists(Keyword.class, 0, 100);
		Iterator<Keyword> kws = test.iterator();
		JSONObject resp = new JSONObject();
		resp.put("size", test.size());
		while(kws.hasNext()){
			Keyword kw = kws.next();
			System.out.println(kw);
			log.info("kw : "+kw);
			resp.append("keywords", kw.toJson());
		}
		return sendResponse(resp.toString());
	}
	
	public Resolution saveOrUpdateKeyword(){
		return null;
	}


	public Keyword getKeyword() {
		return keyword;
	}


	public void setKeyword(Keyword keyword) {
		this.keyword = keyword;
	}
	
	
	

}
