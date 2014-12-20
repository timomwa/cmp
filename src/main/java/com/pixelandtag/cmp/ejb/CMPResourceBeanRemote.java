package com.pixelandtag.cmp.ejb;

import javax.persistence.EntityManager;

import org.hibernate.HibernateException;

import com.pixelandtag.api.BillingStatus;

public interface CMPResourceBeanRemote {
	
	public EntityManager getEM();
	
	public <T> T saveOrUpdate(T t);
	
	public void updateMessageInQueue(long cp_tx_id, BillingStatus billstatus) throws HibernateException;
	
	public <T> T find(Class<T> entityClass, Long id) throws Exception;
	
	public <T> T find(Class<T> entityClass, String param_name, Object value) throws Exception;

}
