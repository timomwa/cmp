package com.pixelandtag.cmp.dao;

import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.dao.BaseDaoImpl;

public class UserDaoImpl extends BaseDaoImpl<User, Long> implements UserDao {
	public User findByUsername(String username) {
		return findBy("username", username); 
	}
}