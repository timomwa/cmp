package com.pixelandtag.cmp.ejb.subscription;

import com.pixelandtag.cmp.entities.subscription.DNDList;

public interface DNDListEJBI {

	public DNDList putInDNDList(String msisdn);
	
	public boolean isinDNDList(String msisdn);
	
	public boolean removeFromDNDList(String msisdn);

}
