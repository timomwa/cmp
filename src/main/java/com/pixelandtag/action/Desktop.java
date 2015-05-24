package com.pixelandtag.action;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

public class Desktop extends BaseActionBean {
	
	@DefaultHandler
	public Resolution showDesktop(){
		
		Subject currentUser = SecurityUtils.getSubject();
		
		if(currentUser.isAuthenticated())
			return desktop;
		else
			return loginPage;
	}

}
