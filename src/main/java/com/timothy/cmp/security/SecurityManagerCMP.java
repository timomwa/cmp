package com.timothy.cmp.security;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import net.sourceforge.stripes.action.ActionBean;

import org.apache.log4j.Logger;
import org.stripesstuff.plugin.security.SecurityManager;

import com.pixelandtag.cmp.handlers.AppProperties;
import com.timothy.cmp.entities.User;

public class SecurityManagerCMP implements SecurityManager {

	private Logger logger = Logger.getLogger(SecurityManagerCMP.class);	
	
	@Override
	public Boolean getAccessAllowed(ActionBean bean, Method handler) {
		logger.info(" getAccessAllowed: "+bean.getClass());
		logger.info(" getAccessAllowed: handler "+handler.getName());
		Annotation[] annotations =  handler.getAnnotations();
		for(int i = 0; i< annotations.length; i++){
			
		}
		User user = (User) bean.getContext().getRequest().getSession().getAttribute(AppProperties.CURR_USER_OBJ_NAME);
		
		logger.info(" getAccessAllowed: user "+user);
		return new Boolean(true);
	}
	

}
