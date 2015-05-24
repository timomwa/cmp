package com.pixelandtag.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;

import com.pixelandtag.action.BaseActionBean;

public class PermissionAction extends BaseActionBean {
	
	private String permissions = "administrator,usermanagement,contentmanagement,customercare,subscriptionmanagement,statistics";
	
	@DefaultHandler
	public Resolution haspermissions() throws JSONException{
		
		Subject currentUser = SecurityUtils.getSubject();
		
		List<String> perms = new ArrayList<String>();
		
		String[] permissionList = permissions.split(",");
		
		for(int i = 0; i<permissionList.length; i++)
			perms.add(permissionList[i]);
		
		boolean[] permsstatus  = currentUser.hasRoles(perms);
		
		JSONArray mainObject = new JSONArray(); 
		
		for(int i =0;i<permsstatus.length; i++){
		
			if(permsstatus[i]){
				JSONObject permission = new JSONObject();
				permission.put("permission",permissionList[i]);
				mainObject.put(permission);
			}
		}
		
		return sendResponse(mainObject.toString());
		
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	
	
	

}
