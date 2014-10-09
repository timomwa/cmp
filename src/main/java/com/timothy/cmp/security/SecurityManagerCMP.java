package com.timothy.cmp.security;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;
import org.stripesstuff.plugin.security.J2EESecurityManager;
import org.stripesstuff.plugin.security.SecurityHandler;
import org.stripesstuff.plugin.security.SecurityManager;

import com.pixelandtag.cmp.action.BaseActionBean;
import com.timothy.cmp.entities.Role;
import com.timothy.cmp.entities.User;

public class SecurityManagerCMP extends J2EESecurityManager implements SecurityHandler {

	private Logger logger = Logger.getLogger(SecurityManagerCMP.class);

	@Override
	protected Boolean isUserAuthenticated(ActionBean bean, Method handler) {
		logger.info(">>>>>>> in isUserAuthenticated getUser(bean) "+getUser(bean));
		return true;//getUser(bean) != null;
	}

	
	
	@Override
	protected Boolean hasRole(ActionBean actionBean, Method handler, String role) {
		logger.info(">>> role: "+role);
		User user = getUser(actionBean);
		logger.info(">>> user: "+user);
		
		if (user != null) {
			List<Role> roles = user.getRoles();
			
			for(Role r : roles){
				logger.info("user role "+r.getName()+" passed role "+role );
				if(r.getName().equals(role))
					return true;
			}
			//return roles != null && roles.contains(new Role(role)); might need me to override equals method. not so good for hibernate
		}
		return false;
	}

	private User getUser(ActionBean bean) {
		return (User) bean.getContext().getRequest().getSession().getAttribute("user");
	}
	
	private String ACCESS_DENIED_PAGE = "/WEB-INF/jsp/access_denied.jsp";
	
	
	@Override
	public Resolution handleAccessDenied(ActionBean bean, Method handler) {
		User user = (User) bean.getContext().getRequest().getSession().getAttribute("user");
		logger.info(">>> user: "+user);
		logger.info("bean.getClass() "+bean.getClass());
		logger.info("handler.getName() "+handler.getName());
		return new ForwardResolution(ACCESS_DENIED_PAGE);
	}

}
