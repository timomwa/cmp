package com.pixelandtag.cmp.dao.core;

import com.pixelandtag.cmp.entities.subscription.DNDList;
import com.pixelandtag.dao.generic.GenericDAO;

public interface DNDListDAOI extends GenericDAO<DNDList, Long> {
	
	public boolean isInDND(String msisdn);
	
	public DNDList getDNDList(String msisdn);

	public void deleteFromDND(String msisdn)  throws Exception ;

}
