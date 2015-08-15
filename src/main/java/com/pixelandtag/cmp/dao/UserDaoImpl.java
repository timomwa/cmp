package com.pixelandtag.cmp.dao;

import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.dao.OLDBaseDaoImpl;

public class UserDaoImpl extends OLDBaseDaoImpl<User, Long> implements UserDao {
	public User findByUsername(String username) {
		return findBy("username", username); 
	}
}