package com.pixelandtag.api;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.persistence.EntityManager;


public interface DAOLocal {
	public <T> T saveOrUpdate(T t) ;
	
	public <T> T find(Class<T> entityClass, Long id);
	
	public EntityManager getEm();
}
