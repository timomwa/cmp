package com.pixelandtag.cmp.ejb.security;

import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.cmp.entities.audit.UserAction;

/**
 * 
 * @author Timothy Mwangi Gikony
 * @since 2.0
 *
 */
public interface UserSessionI {
	
	/**
	 * Gets the user using username and password
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public User getUser(String username, String password) throws Exception;
	
	/**
	 * Creates an audit trail
	 * com.pixelandtag.cmp.entities.audit.UserAction
	 * @param useraction
	 */
	public void createAuditTrail(UserAction useraction);

}
