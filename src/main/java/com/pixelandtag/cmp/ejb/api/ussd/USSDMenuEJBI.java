package com.pixelandtag.cmp.ejb.api.ussd;

import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.dating.entities.ProfileQuestion;

public interface USSDMenuEJBI {

	/**
	 * 
	 * @param language_id
	 * @param parent_level_id
	 * @param menuid
	 * @return
	 */
	public String getMenu(String contextpath, String msisdn, int language_id, int parent_level_id, int menuid, int menuitemid, OperatorCountry opco);
	
	/**
	 * 
	 * @param incomingsms
	 * @return
	 */
	public ProfileQuestion getNextQuestion(IncomingSMS incomingsms);

	/**
	 * 
	 * @param incomingsms
	 * @return
	 */
	public String startDatingQuestions(IncomingSMS incomingsms);
	
	/**
	 * 
	 * @param baseurl
	 * @param incomingsms
	 * @return
	 */
	public String getNextQuestionOrange(String baseurl, IncomingSMS incomingsms);
	
	

}
