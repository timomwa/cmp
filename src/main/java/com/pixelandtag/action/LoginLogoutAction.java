package com.pixelandtag.action;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.Query;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;

import org.apache.commons.collections.SetUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.cmp.entities.Role;
import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.cmp.handlers.AppProperties;
@RolesAllowed("tester")
public class LoginLogoutAction extends BaseActionBean  {
	
	public static final String VIEW = "/WEB-INF/jsp/login.jsp";
	private Logger logger = Logger.getLogger(LoginLogoutAction.class);

	private String loginUsername;
	private String loginPassword;
	
    
	@DenyAll
	@RolesAllowed("tester")
	public Resolution testRoles() throws JSONException{
		JSONObject resp = new JSONObject();
		resp.put("success", true);
		resp.put("message", "Succeeded");
		return  sendResponse(resp.toString());
	}
	
	@DenyAll
	public Resolution logout() throws JSONException{
		JSONObject resp = new JSONObject();
		resp.put("success", true);
		resp.put("message", "Sucessfully logged out!");
		return  sendResponse(resp.toString());
	}
	
	@PermitAll
	@SuppressWarnings("unchecked")
	@DefaultHandler
	public Resolution login() throws JSONException {
		
		logger.info("loginUsername: "+loginUsername);
		logger.info("loginPassword: "+loginPassword);
		Query qry = cmp_dao.resource_bean.getEM().createQuery("from User where username= :username AND password= :password");
		qry.setParameter("username", loginUsername);
		qry.setParameter("password", loginPassword);
		User user = (User) qry.getSingleResult();
		//String resp = "";
		JSONObject resp = new JSONObject();
		if(user!=null){
			for(Role r : user.getRoles()){
				logger.info(">>>>>>> role "+r.getName());
			}
			
			getContext().getRequest().getSession().setAttribute(AppProperties.CURR_USER_OBJ_NAME, user);
			//getContext().setUser(user);
			resp.put("success", true);
			resp.put("message", "Successful Login");
		}else{
			resp.put("success", false);
			resp.put("message", "Wrong username and password");
		}
		logger.info("user: "+user);
		
		return sendResponse(resp.toString());
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
