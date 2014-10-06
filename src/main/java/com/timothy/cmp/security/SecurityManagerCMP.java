package com.timothy.cmp.security;

import java.lang.reflect.Method;

import net.sourceforge.stripes.action.ActionBean;

import org.apache.log4j.Logger;
import org.stripesstuff.plugin.security.SecurityManager;

public class SecurityManagerCMP implements SecurityManager {

	private Logger logger = Logger.getLogger(SecurityManagerCMP.class);	
	
	@Override
	public Boolean getAccessAllowed(ActionBean bean, Method handler) {
		logger.info(" getAccessAllowed: "+bean.getClass());
		logger.info(" getAccessAllowed: handler "+handler.getName());
		return new Boolean(true);
	}
	

}
