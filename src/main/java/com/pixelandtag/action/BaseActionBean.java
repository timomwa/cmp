package com.pixelandtag.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.LocalizableMessage;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import com.pixelandtag.cmp.dao.UserDao;
import com.pixelandtag.cmp.dao.UserDaoImpl;
import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.cmp.persistence.CMPDao;
import com.pixelandtag.dao.FolderDaoImpl;
import com.pixelandtag.ext.MyActionBeanContext;
import com.pixelandtag.ext.MyLocalePicker;
import com.pixelandtag.model.Folder;
import com.pixelandtag.model.FolderDao;


public class BaseActionBean implements ActionBean {
	
	
	protected static final String VIEW_LOGIN_PAGE = "/WEB-INF/jsp/login.jsp";
	protected static final String VIEW_DESKTOP = "/WEB-INF/jsp/desktop.jsp";
	protected static final String STATS_MONITOR = "/WEB-INF/jsp/billingmonitor.jsp";
	protected ForwardResolution desktop = new ForwardResolution(VIEW_DESKTOP);
	protected ForwardResolution loginPage = new ForwardResolution(VIEW_LOGIN_PAGE);
	protected ForwardResolution statsMonitorPage = new ForwardResolution(STATS_MONITOR);
	
	public static final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a E MMM d y");
	public static final SimpleDateFormat sdf2 = new SimpleDateFormat("E MMM d y");
	public static final String PREFERED_DATE_FORMAT_0 = "yyyy-MM-dd";
	
	public static final String PREFERED_DATE_FORMAT_1 = "dd-MMM-yyyy";
	
	public static final SimpleDateFormat preferredDateFormatter_0 = new SimpleDateFormat(PREFERED_DATE_FORMAT_0);
	
	public static final SimpleDateFormat preferred_date_format_1 = new SimpleDateFormat(PREFERED_DATE_FORMAT_1);

	/*
	 * private ActionBeanContext ctx;
	 * 
	 * protected CMPDao cmp_dao = CMPDao.getInstance();
	 * 
	 * public ActionBeanContext getContext() { return ctx; }
	 * 
	 * public void setContext(ActionBeanContext ctx) { this.ctx = ctx; }
	 */
	 public Resolution sendResponse(final String res){ 
		// res.replaceAll(",\n]}", "]}").replaceAll(",}", "}"); 
		 return new StreamingResolution("application/json", res); 
	 }
	 
	 
	 public Resolution sendResponse(final String res, String resptype){ 
			// res.replaceAll(",\n]}", "]}").replaceAll(",}", "}"); 
			 return new StreamingResolution(resptype, res); 
	 }
	 
	 
	 
	 
	protected CMPDao cmp_dao = CMPDao.getInstance();
	protected FolderDao folderDao = new FolderDaoImpl();
	protected UserDao userDao = new	  UserDaoImpl();
	
	private MyActionBeanContext context;

	public MyActionBeanContext getContext() {
		return context;
	}

	public void setContext(ActionBeanContext context) {
		this.context = (MyActionBeanContext) context;
	}

	public void setFolder(Folder folder) {
		if (getUser().equals(folder.getUser())) {
			getContext().setCurrentFolder(folder);
		}
	}

	@SuppressWarnings("unchecked")
	public String getLastUrl() {
		HttpServletRequest req = getContext().getRequest();
		StringBuilder sb = new StringBuilder();

		// Start with the URI and the path
		String uri = (String) req
				.getAttribute("javax.servlet.forward.request_uri");
		String path = (String) req
				.getAttribute("javax.servlet.forward.path_info");
		if (uri == null) {
			uri = req.getRequestURI();
			path = req.getPathInfo();
		}
		sb.append(uri);
		if (path != null) {
			sb.append(path);
		}

		// Now the request parameters
		sb.append('?');
		Map<String, String[]> map = new HashMap<String, String[]>(
				req.getParameterMap());

		// Remove previous locale parameter, if present.
		map.remove(MyLocalePicker.LOCALE);

		// Append the parameters to the URL
		for (String key : map.keySet()) {
			String[] values = map.get(key);
			for (String value : values) {
				sb.append(key).append('=').append(value).append('&');
			}
		}
		// Remove the last '&'
		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	protected LocalizableMessage getLocalizableMessage(String key, Object... parameters) {
		return new LocalizableMessage(getClass().getName() + "." + key,parameters);
	}

	protected User getUser() {
		return getContext().getUser();
	}
	
	protected void setUser(User user) {
		getContext().setUser(user); 
	}
	
	public String getRemoteHost(){
		String host = null;
		
		try{
			host = getContext().getRequest().getRemoteHost();
		}catch(Exception exp){
			
		}
		
		return host==null ? "..." : host;
	}
	  /*protected AttachmentDao attachmentDao = new AttachmentDaoImpl();
	  protected ContactDao contactDao = new ContactDaoImpl(); protected
	  protected MessageDao	 messageDao = new MessageDaoImpl(); */
	  
	 
}