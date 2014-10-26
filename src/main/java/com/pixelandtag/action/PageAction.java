package com.pixelandtag.action;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;

public class PageAction extends BaseActionBean  {
	
	public static final String VIEW = "/WEB-INF/jsp/mainapp.jsp";
	@DefaultHandler
	
	public Resolution showPage(){
		return new ForwardResolution(VIEW);
	}
	
	@RolesAllowed("tester")
	public List<String> getSections(){
		List<String> sections = new ArrayList<String>();
		sections.add("section1");
		sections.add("section2");
		sections.add("section3");
		sections.add("section3");
		return sections;
	}

}
