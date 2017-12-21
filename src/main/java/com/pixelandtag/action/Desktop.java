package com.pixelandtag.action;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

public class Desktop extends BaseActionBean {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@DefaultHandler
	public Resolution showDesktop(){
		
		Subject currentUser = SecurityUtils.getSubject();
		
		logger.info(" \n\n  currentUser -> "+currentUser+"\n\n");
		logger.info(" \n\n  currentUser.isAuthenticated() -> "+currentUser.isAuthenticated()+"\n\n");
		
		if(currentUser.isAuthenticated()){
			return desktop;
		}else{
			return loginPage;
		}
	}

}
