package com.pixelandtag.cmp.dao.core;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.entities.subscription.DNDList;
import com.pixelandtag.dao.generic.GenericDaoImpl;

public class DNDListDAOImpl extends  GenericDaoImpl<DNDList, Long> implements DNDListDAOI{

	private Logger logger = Logger.getLogger(getClass());
	
	public DNDList getDNDList(String msisdn){
		try{
			return findBy("msisdn", msisdn);
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
		return null;
	}
	
	public boolean isInDND(String msisdn){
		return getDNDList(msisdn)!=null;
	}
	
	public void deleteFromDND(String msisdn) throws Exception {
		
		try{
			
			DNDList dnd = getDNDList(msisdn);
			if(dnd!=null)
				delete(dnd);
		
		}catch(Exception exp){
			logger.error(exp.getMessage());
			throw exp;
		}
	}
}
