package com.pixelandtag.cmp.ejb.api.ussd;

public interface USSDMenuEJBI {

	/**
	 * 
	 * @param language_id
	 * @param parent_level_id
	 * @param menuid
	 * @return
	 */
	public String getMenu(int language_id, int parent_level_id, int menuid);
	
	

}
