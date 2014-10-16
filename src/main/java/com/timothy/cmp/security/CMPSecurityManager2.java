package com.timothy.cmp.security;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.stripesstuff.plugin.security.InstanceBasedSecurityManager;
import org.stripesstuff.plugin.security.SecurityHandler;

import com.pixelandtag.cmp.action.BaseActionBean;
import com.pixelandtag.cmp.action.LoginLogoutAction;
import com.pixelandtag.cmp.handlers.AppProperties;
import com.timothy.cmp.entities.Role;
import com.timothy.cmp.entities.User;

public class CMPSecurityManager2 extends InstanceBasedSecurityManager implements SecurityHandler{
	private Logger logger = Logger.getLogger(CMPSecurityManager2.class);
	
	@Override
	protected Boolean isUserAuthenticated(ActionBean bean, Method handler) {

		logger.info(" in isUserAuthenticated. ");
		return true;//getUser(bean) != null;
	}
	
	@Override
	protected Boolean hasRoleName(ActionBean bean, Method handler,String role){

		/*logger.info(" in hasRoleName. ");
			User user = getUser(bean);
			if (user != null) {
			Collection<Role> roles = user.getRoles();
			return roles != null && roles.contains(new Role(role));
			}
			return false;*/
		
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
	
	private User getUser(ActionBean bean) {
		logger.info(" in getUser. ");
		return (User) bean.getContext().getRequest() .getSession().getAttribute(AppProperties.CURR_USER_OBJ_NAME);//((BaseActionBean) bean).getContext().getUser();
	}
	
	@Override
	public Resolution handleAccessDenied(ActionBean bean, Method handler) {
	/*	if (!isUserAuthenticated(bean, handler)) {
			RedirectResolution resolution = new RedirectResolution(LoginLogoutAction.class);
			if (bean.getContext().getRequest().getMethod()
			.equalsIgnoreCase("GET" ))
			{
			String loginUrl = ((BaseActionBean) bean).getLastUrl();
			resolution.addParameter("loginUrl" , loginUrl);
			}
			return resolution;
			}
			return new ErrorResolution(HttpServletResponse.SC_UNAUTHORIZED);*/
		logger.info(" in handleAccessDenied. ");
		User user = getUser(bean);
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
}


