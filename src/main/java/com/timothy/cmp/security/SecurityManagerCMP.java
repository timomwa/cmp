package com.timothy.cmp.security;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.util.Log;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.stripesstuff.plugin.security.InstanceBasedSecurityManager;
import org.stripesstuff.plugin.security.J2EESecurityManager;
import org.stripesstuff.plugin.security.SecurityHandler;
import org.stripesstuff.plugin.security.SecurityManager;

import com.pixelandtag.cmp.action.BaseActionBean;
import com.pixelandtag.cmp.action.LoginLogoutAction;
import com.timothy.cmp.entities.Role;
import com.timothy.cmp.entities.User;
import com.pixelandtag.cmp.handlers.AppProperties;
import com.timothy.cmp.entities.User;


public class SecurityManagerCMP extends InstanceBasedSecurityManager implements
		SecurityHandler {

	private Logger logger = Logger.getLogger(SecurityManagerCMP.class);

	@Override
	protected Boolean isUserAuthenticated(ActionBean bean, Method handler) {
		logger.info(">>>>>>> in isUserAuthenticated getUser(bean) "
				+ getUser(bean));
		if(bean.getContext().getEventName().contains("login"))
			return true;
		return getUser(bean) != null;
	}
	
	
	

	@Override
	protected Boolean hasRoleName(ActionBean actionBean, Method handler,
			String role) {
		logger.info(">>> role: " + role);
		User user = getUser(actionBean);
		logger.info(">>> user: " + user);

		if (user != null) {
			List<Role> roles = user.getRoles();

			for (Role r : roles) {
				logger.info("user role " + r.getName() + " passed role " + role);
				if (r.getName().equals(role))
					return true;
			}
		}
		return false;
	}




	@Override
	protected Boolean hasRole(ActionBean actionBean, Method handler, String role) {
		logger.info(">>> role: " + role);
		User user = getUser(actionBean);
		logger.info(">>> user: " + user);

		if (user != null) {
			List<Role> roles = user.getRoles();

			for (Role r : roles) {
				logger.info("user role " + r.getName() + " passed role " + role);
				if (r.getName().equals(role))
					return true;
			}
		}
		return false;
	}

	private User getUser(ActionBean bean) {
		return (User) bean.getContext().getRequest().getSession().getAttribute(AppProperties.CURR_USER_OBJ_NAME);
	}

	@Override
	public Resolution handleAccessDenied(ActionBean bean, Method handler) {
		User user = (User) bean.getContext().getRequest().getSession()
				.getAttribute("user");
		logger.info("\n\n>>> user: " + user +" ==== ");
		logger.info("bean.getClass() " + bean.getClass());
		logger.info("handler.getName() " + handler.getName());
		JSONObject jsonob = new JSONObject();
		try {
			jsonob.put("success", false);
			jsonob.put("message", "Access denied");
		} catch (JSONException e) {
			logger.error(e.getMessage(),e);
		}
		//LoginLogoutAction.VIEW
		return new ForwardResolution(LoginLogoutAction.VIEW);// StreamingResolution("application/json", jsonob.toString());
	}
	
	
	
}
