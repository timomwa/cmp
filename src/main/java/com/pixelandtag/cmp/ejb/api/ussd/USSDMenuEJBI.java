package com.pixelandtag.cmp.ejb.api.ussd;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;

public interface USSDMenuEJBI {

	/**
	 * 
	 * @param language_id
	 * @param parent_level_id
	 * @param menuid
	 * @return
	 */
	public String getMenu(String contextpath, int language_id, int parent_level_id, int menuid, int menuitemid, OperatorCountry opco);
	
	

}
