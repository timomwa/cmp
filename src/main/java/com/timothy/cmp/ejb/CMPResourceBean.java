package com.timothy.cmp.ejb;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

@Stateless
@Remote
public class CMPResourceBean implements CMPResourceBeanRemote {

	private Logger logger = Logger.getLogger(CMPResourceBean.class);
	
	@Resource
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Override
	public EntityManager getEM() {
		logger.info(" em : "+em);
		return em;
	}

}
