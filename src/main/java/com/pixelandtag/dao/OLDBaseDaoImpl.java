package com.pixelandtag.dao;

import java.io.Serializable;

import org.stripesstuff.stripersist.Stripersist;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.NonUniqueResultException;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.pixelandtag.cmp.entities.User;
import com.pixelandtag.model.OLDGenericDao;

@Deprecated
public abstract class OLDBaseDaoImpl<T, ID extends Serializable> implements
		OLDGenericDao<T, ID> {
	private Class<T> entityClass;

	@SuppressWarnings("unchecked")
	public OLDBaseDaoImpl() {
		entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	@SuppressWarnings("unchecked")
	public List<T> read() {
		return Stripersist.getEntityManager().createQuery("from " + entityClass.getName()).getResultList();
	}

	public T read(ID id) {
		return Stripersist.getEntityManager().find(entityClass, id);
	}

	@SuppressWarnings("unchecked")
	public void save(T object) {
		Stripersist.getEntityManager().persist(object);
	}

	public void delete(T object) {
		Stripersist.getEntityManager().remove(object);
	}

	public void commit() {
		Stripersist.getEntityManager().getTransaction().commit();
	}

	@SuppressWarnings("unchecked")
	public T findBy(String fieldName, Object value) {
		Query query = Stripersist.getEntityManager()
				.createQuery(getQuery(fieldName, null))
				.setParameter(fieldName, value);
		return getSingleResult(query);
	}

	@SuppressWarnings("unchecked")
	public T findBy(String fieldName, Object value, User user) {
		Query query = Stripersist.getEntityManager()
				.createQuery(getQuery(fieldName, user))
				.setParameter(fieldName, value).setParameter("user", user);
		return getSingleResult(query);
	}

	private String getQuery(String fieldName, User user) {
		String query = "from " + entityClass.getName() + " t " + "where t."
				+ fieldName + " = :" + fieldName;
		if (user == null) {
			return query;
		}
		return query + " and t.user = :user";
	}

	@SuppressWarnings("unchecked")
	private T getSingleResult(Query query) {
		try {
			return (T) query.getSingleResult();
		} catch (NonUniqueResultException exc) {
			return (T) query.getResultList().get(0);
		} catch (NoResultException exc) {
			return null;
		}
	}
}
