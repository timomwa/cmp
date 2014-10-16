package com.pixelandtag.cmp.action;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import com.timothy.cmp.persistence.CMPDao;

public class BaseActionBean implements ActionBean {

	private ActionBeanContext ctx;

	protected CMPDao cmp_dao = CMPDao.getInstance(); 

	public ActionBeanContext getContext() {
		return ctx;
	}

	public void setContext(ActionBeanContext ctx) {
		this.ctx = ctx;
	}
	
	public Resolution sendResponse(final String res){
		//final String r = res.replaceAll(",\n]}", "]}").replaceAll(",}", "}");
		return new StreamingResolution("application/json", res);
	}
	
	

}