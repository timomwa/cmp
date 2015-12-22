package com.pixelandtag.cmp.dao.core;

import com.pixelandtag.cmp.entities.subscription.FreeLoader;
import com.pixelandtag.dao.generic.GenericDAO;

public interface FreeLoaderDAOI  extends GenericDAO<FreeLoader, Long>{

	public void deleteByMsisdn(String msisdn) throws Exception;

}
