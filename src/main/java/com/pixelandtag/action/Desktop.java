package com.pixelandtag.action;

import javax.annotation.security.RolesAllowed;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

public class Desktop extends BaseActionBean {
	
	private static final String VIEW = "/WEB-INF/jsp/desktop.jsp";
	
	@DefaultHandler
	public Resolution showDesktop(){
		return new ForwardResolution(VIEW);
	}

}
