package com.pixelandtag.api;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@Remote
public class DAO implements com.pixelandtag.api.DAOLocal {
	
	
	@Resource
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Override
	public <T> T saveOrUpdate(T t) {
		t = em.merge(t);
        return t;
	} 
	
	@Override
	public <T> T find(Class<T> entityClass, Long primaryKey) {
		return em.find(entityClass, primaryKey);
	}

	public EntityManager getEm() {
		return em;
	}

}
