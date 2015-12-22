package com.pixelandtag.cmp.ejb.subscription;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.dao.core.FreeLoaderDAOI;

@Stateless
@Remote
public class FreeLoaderEJBImpl implements FreeLoaderEJBI {

	public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Inject
	private FreeLoaderDAOI freeloaderDAO;

	public void removeFromFreeloaderList(String msisdn) throws Exception{
		try {
			freeloaderDAO.deleteByMsisdn(msisdn);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
	
	public boolean isInFreeloaderList(String msisdn) throws Exception{
		return freeloaderDAO.findBy("msisdn", msisdn)!=null;
	}
}
