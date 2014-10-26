package com.pixelandtag.action;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.pixelandtag.cmp.entities.Keyword;

import net.sourceforge.stripes.action.Before;
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
