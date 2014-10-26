package com.pixelandtag.cmp.dao;

import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.model.Dao;


public interface UserDao extends Dao<User,Long> {
    public User findByUsername(String username);
}