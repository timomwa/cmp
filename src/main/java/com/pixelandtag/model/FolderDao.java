package com.pixelandtag.model;

import com.pixelandtag.cmp.entities.User;

public interface FolderDao extends OLDGenericDao<Folder,Integer> {
    public Folder findByName(String name, User user);
}