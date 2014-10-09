package com.timothy.cmp.security;

import java.lang.reflect.Method;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;
import org.stripesstuff.plugin.security.SecurityHandler;

public class SecurityHandlerCMP {//implements SecurityHandler 
	
	private Logger logger = Logger.getLogger(SecurityHandlerCMP.class);

	private String ACCESS_DENIED_PAGE = "/WEB-INF/jsp/access_denied.jsp";
	//@Override
	public Resolution handleAccessDenied(ActionBean bean, Method handler) {
		logger.info("bean.getClass() "+bean.getClass());
		logger.info("handler.getName() "+handler.getName());
		return new ForwardResolution(ACCESS_DENIED_PAGE);
	}

}
