package com.pixelandtag.cmp.action;

import org.apache.log4j.Logger;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

public class AuthenticatorAction extends BaseActionBean {

	private static final String VIEW = "/WEB-INF/jsp/control_panel.jsp";
	private String loginUsername;
	private String loginPassword;
	
	private Logger logger = Logger.getLogger(AuthenticatorAction.class);
	
	@DefaultHandler
	public Resolution authentication() {
		logger.info("loginUsername : "+loginUsername);
		logger.info("loginPassword : "+loginPassword);
		return new ForwardResolution(VIEW);
	}
	
	
	public String getLoginUsername() {
		return loginUsername;
	}
	public void setLoginUsername(String loginUsername) {
		this.loginUsername = loginUsername;
	}
	public String getLoginPassword() {
		return loginPassword;
	}
	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}
	
	
	
	
}
