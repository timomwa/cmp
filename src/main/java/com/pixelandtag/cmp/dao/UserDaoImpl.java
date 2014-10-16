package com.pixelandtag.cmp.dao;

import com.pixelandtag.dao.BaseDaoImpl;
import com.timothy.cmp.entities.User;

public class UserDaoImpl extends BaseDaoImpl<User, Long> implements UserDao {
	public User findByUsername(String username) {
		return findBy("username", username); 
	}
}