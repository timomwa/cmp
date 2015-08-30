package com.pixelandtag.ext;


import javax.servlet.http.HttpSession;

import com.pixelandtag.cmp.dao.UserDaoImpl;
import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.dao.FolderDaoImpl;
import com.pixelandtag.model.Folder;
import com.pixelandtag.model.FolderDao;
import com.pixelandtag.model.MessageEmail;

import net.sourceforge.stripes.action.ActionBeanContext;
/*import stripesbook.dao.FolderDao;
import stripesbook.dao.impl.stripersist.FolderDaoImpl;
import stripesbook.dao.impl.stripersist.UserDaoImpl;
import stripesbook.model.Folder;
import stripesbook.model.Message;
import stripesbook.model.User;*/
public class MyActionBeanContext  extends ActionBeanContext{

	 private static final String FOLDER  = "folder";
	    private static final String MESSAGE = "message";
	    private static final String USER = "user";

	    public void setCurrentFolder(Folder folder) {
	        setCurrent(FOLDER, folder.getId());
	    }
	    public Folder getCurrentFolder() {
	        FolderDao folderDao = new FolderDaoImpl();
	        Folder folder = null;
	        Integer folderId = getCurrent(FOLDER, null);
	        if (folderId != null) {
	            folder = folderDao.read(folderId);
	        }
	        else {
	            folder = folderDao.findByName(Folder.INBOX, getUser());
	        }
	        return folder;
	    }
	    public void setMessageCompose(MessageEmail message) {
	        setCurrent(MESSAGE, message);
	    }
	    public MessageEmail getMessageCompose() {
	        return getCurrent(MESSAGE, new MessageEmail());
	    }
	    public void setUser(User user) {
	        if (user != null) {
	            setCurrent(USER, user.getId());
	        }
	        else {
	            setCurrent(USER, null);
	        }
	    }
	    public User getUser() {
	    	Long userId = getCurrent(USER, null);
	        if (userId == null) { return null ; }
	        return new UserDaoImpl().read(userId);
	    }
	    
	    public void logout() {
	        setUser(null);

	        HttpSession session = getRequest().getSession();
	        if (session != null) {
	            session.invalidate();
	        }
	    }
	    
	    protected void setCurrent(String key, Object value) {
	        getRequest().getSession().setAttribute(key, value);
	    }
	    @SuppressWarnings("unchecked")
	    protected <T> T getCurrent(String key, T defaultValue) {
	        T value = (T) getRequest().getSession().getAttribute(key);
	        if (value == null) {
	            value = defaultValue;
	            setCurrent(key, value);
	        }
	        return value;
	    }
	    
}
