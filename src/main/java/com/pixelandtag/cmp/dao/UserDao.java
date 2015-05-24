package com.pixelandtag.cmp.dao;

import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.model.GenericDao;


public interface UserDao extends GenericDao<User,Long> {
    public User findByUsername(String username);
}