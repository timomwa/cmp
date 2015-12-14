package com.pixelandtag.cmp.ejb.subscription;


import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.dao.core.DNDListDAOI;
import com.pixelandtag.cmp.entities.subscription.DNDList;

@Stateless
@Remote
public class DNDListEJBImpl implements DNDListEJBI {
	
	public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Inject
	private DNDListDAOI dndDAO;
	
	public boolean isinDNDList(String msisdn){
		return dndDAO.isInDND(msisdn);
	}
	
	public boolean removeFromDNDList(String msisdn){
		try {
			dndDAO.deleteFromDND(msisdn);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}
	
	
	public DNDList putInDNDList(String msisdn){
		
		try{
			
			DNDList dndlist = dndDAO.getDNDList(msisdn);
			if(dndlist==null)
				dndlist = new DNDList();
			dndlist.setMsisdn(msisdn);
			return dndDAO.save(dndlist);
			
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}finally{}
		
		return null;
	}

}
