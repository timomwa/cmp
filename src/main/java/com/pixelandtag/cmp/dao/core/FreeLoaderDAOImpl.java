package com.pixelandtag.cmp.dao.core;

import com.pixelandtag.cmp.entities.subscription.FreeLoader;
import com.pixelandtag.dao.generic.GenericDaoImpl;

public class FreeLoaderDAOImpl extends  GenericDaoImpl<FreeLoader, Long> implements FreeLoaderDAOI {

	public void deleteByMsisdn(String msisdn) throws Exception {
		FreeLoader freeloader = findBy("msisdn", msisdn);
		if(freeloader!=null){
			delete(freeloader);
		}
	}
}
