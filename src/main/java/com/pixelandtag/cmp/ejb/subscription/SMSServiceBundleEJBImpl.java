package com.pixelandtag.cmp.ejb.subscription;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

@Stateless
@Remote
public class SMSServiceBundleEJBImpl implements SMSServiceBundleEJBI{

	public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	public <T> T saveOrUpdate(T t) throws Exception{
		try{
			return em.merge(t);
		}catch(Exception e){
			throw e;
		}
	}
	
	
}
