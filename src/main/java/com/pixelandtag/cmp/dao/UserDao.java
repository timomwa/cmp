package com.pixelandtag.cmp.dao;

import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.model.OLDGenericDao;


public interface UserDao extends OLDGenericDao<User,Long> {
    public User findByUsername(String username);
}