package com.pixelandtag.dao;

import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.model.Folder;
import com.pixelandtag.model.FolderDao;

public class FolderDaoImpl extends BaseDaoImpl<Folder, Integer> implements
		FolderDao {
	public Folder findByName(String name, User user) {
		return findBy("name", name, user);
	}
}