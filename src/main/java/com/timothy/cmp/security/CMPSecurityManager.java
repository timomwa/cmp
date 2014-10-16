package com.timothy.cmp.security;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.stripesstuff.plugin.security.J2EESecurityManager;
import org.stripesstuff.plugin.security.SecurityHandler;

import com.pixelandtag.cmp.action.BaseActionBean;
import com.pixelandtag.cmp.action.LoginLogoutAction;
import com.pixelandtag.cmp.handlers.AppProperties;
import com.timothy.cmp.entities.Role;
import com.timothy.cmp.entities.User;


public class CMPSecurityManager extends J2EESecurityManager implements SecurityHandler {

	private Logger logger = Logger.getLogger(CMPSecurityManager.class);
	
	
	@Override
	public Boolean getAccessAllowed(ActionBean bean, Method handler) {
		logger.info(" in getAccessAllowed. ");
		logger.info("Determining if access is allowed for " + handler.getName() + " on " + bean.toString());
		Boolean allowed = determineAccessOnElement(bean, handler, handler);

		// If the event handler didn't decide, determine if the action bean class allows access.
		// Rinse and repeat for all superclasses.

		Class<?> beanClass = bean.getClass();
		while (allowed == null && beanClass != null)
		{
			logger.info("Determining if access is allowed for " + beanClass.getName() + " on " + bean.toString());
			allowed = determineAccessOnElement(bean, handler, beanClass);
			beanClass = beanClass.getSuperclass();
		}

		// If the event handler nor the action bean class decided, allow access.
		// This default allows access if no security annotations are used.

		if (allowed == null)
		{
			allowed = true;
		}

		// Return the decision.

		return allowed;
	}




	@Override
	protected Boolean determineAccessOnElement(ActionBean bean, Method handler,
			AnnotatedElement element) {
		logger.info(" in determineAccessOnElement. ");
		Boolean allowed = null;

		if (element.isAnnotationPresent(DenyAll.class))
		{
			// The element denies access.

			allowed = false;
		}
		else if (element.isAnnotationPresent(PermitAll.class))
		{
			// The element allows access to all security roles (i.e. any authenticated user).

			allowed = isUserAuthenticated(bean, handler);
		}
		else
		{
			RolesAllowed rolesAllowed = element.getAnnotation(RolesAllowed.class);
			if (rolesAllowed != null)
			{
				// Still need to check if the users is authorized
				allowed = isUserAuthenticated(bean, handler);

				if (allowed == null || allowed.booleanValue()) {
					
					// The element allows access if the user has one of the specified roles.
					allowed = false;
					for (String role : rolesAllowed.value())
					{
						logger.info(" ***CHECKING WHETHER THIS USER HAS ROLE **** "+role);
						Boolean hasRole = hasRole(bean, handler, role);
						if (hasRole != null && hasRole)
						{
							allowed = true;
							break;
						}
					}
				}
			}
		}
		return allowed;
	}




	@Override
	protected Boolean isUserAuthenticated(ActionBean bean, Method handler) {
		logger.info(" in isUserAuthenticated. ");
		logger.info(">>>>>>> in isUserAuthenticated getUser(bean) "
				+ getUser(bean));
		logger.info("bean.getContext().getEventName().contains(\"login\") " + bean.getContext().getEventName().contains("login"));
		
		boolean userAuthenticated = false;
		if(bean.getContext().getEventName().contains("login"))
			userAuthenticated= true;
		else
			userAuthenticated = getUser(bean) != null;
		logger.info(" in isUserAuthenticated ?  "+userAuthenticated);
		return userAuthenticated;
	}




	@Override
	protected Boolean hasRole(ActionBean bean, Method handler, String role) {
		logger.info(" in hasRole. ");
		logger.info(">>> role pased: " + role);
		User user = getUser(bean);
		logger.info(">>> user:  " + user);
		
		
		logger.info(" .... checking user roles ....  for " + user);

		if (user != null) {
			List<Role> roles = user.getRoles();

			for (Role r : roles) {
				logger.info("user role " + r.getName() + " passed role " + role);
				if (r.getName().equals(role))
					return new Boolean(true);
			}
		}
		return new Boolean(false);
	}




	@Override
	public Resolution handleAccessDenied(ActionBean bean, Method handler) {
		logger.info(" in handleAccessDenied. ");
		User user = getUser(bean) ;
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
		return new StreamingResolution("application/json", jsonob.toString());// new ForwardResolution(LoginLogoutAction.VIEW);
	}
	
	
	private User getUser(ActionBean bean) {
		 return (User) bean.getContext().getRequest() .getSession().getAttribute(AppProperties.CURR_USER_OBJ_NAME);
	}

	/*@Override
	protected Boolean isUserAuthenticated(ActionBean bean, Method handler) {
		logger.info(">>>>>>> in isUserAuthenticated getUser(bean) "
				+ getUser(bean));
		logger.info("bean.getContext().getEventName().contains(\"login\") " + bean.getContext().getEventName().contains("login"));
		if(bean.getContext().getEventName().contains("login"))
			return true;
		return getUser(bean) != null;
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
	}*/
	
	
	
}
