package com.pixelandtag.model;

import com.pixelandtag.cmp.entities.User;

public interface FolderDao extends Dao<Folder,Integer> {
    public Folder findByName(String name, User user);
}