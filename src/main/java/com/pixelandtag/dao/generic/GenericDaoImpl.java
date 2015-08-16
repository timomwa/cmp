package com.pixelandtag.dao.generic;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.stripesstuff.stripersist.Stripersist;

import com.pixelandtag.cmp.entities.User;

/**
 * @param <T>
 *            The persistent type
 * @param <ID>
 *            The primary key type
 */
@ManagedBean
public class GenericDaoImpl<T, ID extends Serializable> implements GenericDAO<T, ID> {

	private final Class<T> persistentClass;
	
	private Logger log = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	//protected EntityManager em;
	
	public void setEm(EntityManager em){
		this.em = em;
	}
	
	public EntityManager getEm(){
		return this.em;
	}
	
	@Override
	public T findBy(String fieldName, Object value) {
		Query query = Stripersist.getEntityManager()
				.createQuery(getQuery(fieldName, null))
				.setParameter(fieldName, value);
		return getSingleResult(query);
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
	
	
	private String getQuery(String fieldName, User user) {
		String query = "from " + persistentClass.getName() + " t " + "where t."
				+ fieldName + " = :" + fieldName;
		if (user == null) {
			return query;
		}
		return query;
	}
	
	@SuppressWarnings("unchecked")
	public GenericDaoImpl() {
		this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public GenericDaoImpl(final Class<T> persistentClass) {
		super();
		this.persistentClass = persistentClass;
	}
	
	/**
	 * @see GenericDAO#getEntityClass()
	 */
	public Class<T> getEntityClass() {
		return persistentClass;
	}
	
	/**
	 * @see GenericDAO#save(Object)
         * #save(java.lang.Object)
	 */
	public T save(T entity) throws Exception{
		//Method method = entity.getClass().getSuperclass().getDeclaredMethod("getId", new Class[] {});
		//Long id = (Long) method.invoke(entity, new Object[] {});
		//log.info("in generic dao =====================" + id);
		entity = em.merge(entity);
		return entity;
	}
	
	/**
	 * @see GenericDAO#delete(java.lang.Object)
	 */
	public void delete(T entity) throws Exception{
		em.remove(entity);
	}
	
	/**
	 * @see GenericDAO#deleteById(java.lang.Object)
	 */
	public void deleteById(final ID id) throws Exception{
		T entity = em.find(persistentClass, id);
		if(entity != null) em.remove(entity);
	}
	
	public void delete(ID ids []) throws Exception{
		int size = ids.length;
		
		for(int idx = 0; idx < size; idx++){
			T entity = em.find(persistentClass, ids[idx]);
			if(entity != null) em.remove(entity);
		}
		
	}
	
	/**
	 * @see GenericDAO#deleteBatchById(java.lang.Object)
	 */
	public void deleteBatchById(ID ids []) throws Exception{
		int size = ids.length;
		
		for(int idx = 0; idx < size; idx++){
			T entity = em.find(persistentClass, ids[idx]);
			if(entity != null) em.remove(entity);
		}
		
	}
	
	/**
	 * @see GenericDAO#findById(java.io.Serializable)
	 */
	public T findById(final ID id) {
		final T result = em.find(persistentClass, id);
		return result;
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.systech.fm.dao.generic.GenericDAO#getReference(java.io.Serializable)
	 */
	public T getReference(final ID id){
		return em.getReference(persistentClass, id);
	}
	
	/**
	 * @see GenericDAO#findByNamedQuery(String, Object...)
         * #findByNamedQuery(java.lang.String, java.lang.Object[])
	 */
	@SuppressWarnings("unchecked")
	public List<T> findByNamedQuery(final String name, Object... params) {
		javax.persistence.Query query = em.createNamedQuery(name);

		for (int i = 0; i < params.length; i++) {
			query.setParameter(i + 1, params[i]);
		}
		
		final List<T> result = (List<T>) query.getResultList();
		return result;
	}

	/**
	 * @see GenericDAO#findByNamedQuery(String, Map)
	 */
	@SuppressWarnings("unchecked")
	public List<T> findByNamedQuery(final String name,
			final Map<String, ? extends Object> params) {
		javax.persistence.Query query = em.createNamedQuery(name);

		for (final Map.Entry<String, ? extends Object> param : params.entrySet()) {
			query.setParameter(param.getKey(), param.getValue());
		}

		return query.getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<T> findByNamedQuery(final String queryName, final Map<String, ?extends Object> params, int start, int limit){
		
		javax.persistence.Query query = em.createNamedQuery(queryName);

		for (final Map.Entry<String, ? extends Object> param : params.entrySet()) {
			query.setParameter(param.getKey(), param.getValue());
		}
		query.setFirstResult(start);
		query.setMaxResults(limit);

		return query.getResultList();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<T> list(final int firstResult,final int maxResults){
		try{
			Query qry = em.createQuery("from "+getEntityClass().getName());
			qry.setFirstResult(firstResult);
			qry.setMaxResults(maxResults);
			return qry.getResultList();
		}catch(javax.persistence.NoResultException ex){
			log.warn("Could not find any results with the query\"from "+getEntityClass().getName());
		}
		
		return new ArrayList<T>();
		
	}
	

	@Override
	@SuppressWarnings("unchecked")
	public List<T> findByNamedQueryAndNamedParams(final String name,
			final Map<String, ? extends Object> params) {
		javax.persistence.Query query = em.createNamedQuery(name);

		for (final Map.Entry<String, ? extends Object> param : params.entrySet()) {
			query.setParameter(param.getKey(), param.getValue());
		}

		final List<T> result = (List<T>) query.getResultList();
		return result;
	}
	
	
}