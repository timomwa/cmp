package com.pixelandtag.dao;

import com.pixelandtag.model.Folder;
import com.pixelandtag.model.FolderDao;
import com.timothy.cmp.entities.User;

public class FolderDaoImpl extends BaseDaoImpl<Folder, Integer> implements
		FolderDao {
	public Folder findByName(String name, User user) {
		return findBy("name", name, user);
	}
}