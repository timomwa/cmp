package com.pixelandtag.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

public interface GenericDao<T,ID extends Serializable> {
	public List<T> read();
    public T read(ID id);
    public void save(T t);
    public void delete(T t);
    public void commit();
}