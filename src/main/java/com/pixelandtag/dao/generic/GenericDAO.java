package com.pixelandtag.dao.generic;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;

/**
 * 
 *
 * @param <T> the entity type
 * @param <ID> the primary key type
 */
public interface GenericDAO<T, ID extends Serializable> {
	
	public void setEm(EntityManager em);
	
	public EntityManager getEm();
	
	/**
	 * 
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public T findBy(String fieldName, Object value);
	
    /**
     * Get the Class of the entity.
     *
     * @return the class
     */
    Class<T> getEntityClass();

    /**
     * Find an entity by its primary key
     *
     * @param id the primary key
     * @return the entity
     */
    T findById(final ID id);
    
    
    
    /**
     * Get reference of 
     * the entity whose state may be lazily fetched.
     * @param id
     * @return
     */
    T getReference(final ID id);
    
   

    /**
     * Find using a named query.
     *
     * @param queryName the name of the query
     * @param params the query parameters
     *
     * @return the list of entities
     */
    List<T> findByNamedQuery(final String queryName, Object... params);

    /**
     * Find using a named query.
     *
     * @param queryName the name of the query
     * @param params the query parameters
     *
     * @return the list of entities
     */
    List<T> findByNamedQuery(final String queryName, final Map<String, ?extends Object> params);


   

    
    /**
     * save an entity. This can be either a INSERT or UPDATE in the database.
     * 
     * @param entity the entity to save
     * 
     * @return the saved entity
     */
    T save(final T entity) throws Exception;

    /**
     * delete an entity from the database.
     * 
     * @param entity the entity to delete
     */
    void delete(final T entity) throws Exception;
    
    /**
     * delete an entity by its primary key
     *
     * @param id the primary key of the entity to delete
     */
    void deleteById(final ID id) throws Exception;
    
    /**
     * delete batch entities by their primary keys array
     *
     * @param ids [] the primary key of entities to delete
     */
    void deleteBatchById(ID ids []) throws Exception;
    
    /**
     * delete batch entities by their primary keys array
     *
     * @param ids [] the primary key of entities to delete
     */
    void delete(ID ids []) throws Exception;
    
   
	public List<T> list(final int firstResult,final int maxResults);

	
    /**
     * Find using a named query.
     *
     * @param queryName the name of the query
     * @param params the query parameters
     *
     * @return the list of entities
     */
    List<T> findByNamedQueryAndNamedParams(final String queryName, final Map<String, ?extends Object> params);


}
