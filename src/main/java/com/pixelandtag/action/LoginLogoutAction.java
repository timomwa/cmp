package com.pixelandtag.action;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.Query;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

import org.apache.commons.collections.SetUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.cmp.entities.Role;
import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.cmp.handlers.AppProperties;
@RolesAllowed("tester")
public class LoginLogoutAction extends BaseActionBean  {
	
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
	
	@PermitAll
	public Resolution logout() throws JSONException{
		Subject currentUser = SecurityUtils.getSubject();
		currentUser.logout();
		return loginPage ;
	}
	
	@PermitAll
	@DefaultHandler
	public Resolution login() throws JSONException {
		
		JSONObject resp = new JSONObject();
		 
		Subject currentUser = SecurityUtils.getSubject();
		
		logger.info("B4: currentUser.isAuthenticated(): "+currentUser.isAuthenticated());
		
		if ( !currentUser.isAuthenticated() ) {
		    
			UsernamePasswordToken token = new UsernamePasswordToken(loginUsername, loginPassword);
		    
		    token.setRememberMe(true);
		    
		    try {
		        currentUser.login( token );
		        resp.put("success", true);
				resp.put("message", "Successful Login");
				
		    } catch ( UnknownAccountException uae ) {
		    	resp.put("success", false);
				resp.put("message", "No user with that name");
			} catch ( IncorrectCredentialsException ice ) {
		    	resp.put("success", false);
				resp.put("message", "Incorrect password");
			} catch ( LockedAccountException lae ) {
		    	resp.put("success", false);
				resp.put("message", "Your account is locked");
			} catch ( AuthenticationException ae ) {
		    	resp.put("success", false);
				resp.put("message", "Wrong username and password");
			}catch(Exception e){
				resp.put("success", false);
				resp.put("message", "Can't log in right now.");
				logger.error(e.getMessage(),e);
			}
		}
		
		logger.info("AFTA: currentUser.isAuthenticated(): "+currentUser.isAuthenticated());
		
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
