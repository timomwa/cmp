package com.pixelandtag.cmp.dao.opco;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.dao.generic.GenericDaoImpl;


public class OperatorCountryDAOImpl extends GenericDaoImpl<OperatorCountry, Long> implements OperatorCountryDAOI {
	
	private Logger logger = Logger.getLogger(getClass());
	
	public OperatorCountry findbyOpcoCode(String opcocode){
		
		OperatorCountry opco = null;
		
		try{
			opco = findBy("code", opcocode);
		}catch(Exception exop){
			logger.error(exop.getMessage(),exop);
		}
		
		return opco;
	}
	
}
