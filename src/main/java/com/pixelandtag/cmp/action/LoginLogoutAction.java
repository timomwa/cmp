package com.pixelandtag.cmp.action;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.cmp.handlers.AppProperties;
import com.timothy.cmp.entities.User;
import com.timothy.cmp.persistence.CMPDao;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

public class LoginLogoutAction extends BaseActionBean {
	
	private static final String VIEW = "/WEB-INF/jsp/login.jsp";
	private Logger logger = Logger.getLogger(LoginLogoutAction.class);

	private String loginUsername;
	private String loginPassword;
	
    
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
			getContext().getRequest().getSession().setAttribute(AppProperties.CURR_USER_OBJ_NAME, user);
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
