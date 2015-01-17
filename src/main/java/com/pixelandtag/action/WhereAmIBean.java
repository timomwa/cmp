package com.pixelandtag.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
/**
 * To be able to quickly see 
 * where this deployment resides
 * in the file system
 * @author Timothy
 *
 */
//@UrlBinding("/where")
public class WhereAmIBean extends BaseActionBean {

	@DefaultHandler
	public Resolution showPath(){
		return sendResponse(getContext().getServletContext().getRealPath("/"));
	}
}