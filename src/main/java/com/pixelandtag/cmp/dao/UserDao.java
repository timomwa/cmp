package com.pixelandtag.cmp.dao;

import com.pixelandtag.model.Dao;
import com.timothy.cmp.entities.User;


public interface UserDao extends Dao<User,Long> {
    public User findByUsername(String username);
}