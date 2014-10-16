package com.pixelandtag.cmp.action;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;

public class ShowLoginPageAction extends BaseActionBean  {
	
	
	@DefaultHandler
	//@PermitAll
	@DenyAll
	@HandlesEvent("showLoginPage")
	public Resolution showLoginPage(){
		return new ForwardResolution(LoginLogoutAction.VIEW);
	}

}
