package com.pixelandtag.action;

import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;

import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.cmp.persistence.CMPDao;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

public class LoginPage extends BaseActionBean {
	
	private static final String VIEW = "/WEB-INF/jsp/login.jsp";

	
	
    
	
	@DefaultHandler
	public Resolution login() {
		Query qry = cmp_dao.resource_bean.getEM().createQuery("from User");
		List<User> users = qry.getResultList();
		
		for(User u : users){
			System.out.println(u);
		}
		return new ForwardResolution(VIEW);
	}

}
