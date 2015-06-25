package com.pixelandtag.cmp.ejb.content;

public interface ContentManagementI {
	/**
	 * 
	 * @param serviceid
	 * @param contentId
	 * @return
	 */
	public boolean deleteContent(Long serviceid, Long contentId) throws Exception;

}
