package com.pixelandtag.cmp.ejb.api.ussd;

import java.util.Map;

import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.dating.entities.PersonDatingProfile;
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
	 * @param profile
	 * @param incomingsms
	 * @return
	 */
	public ProfileQuestion getNextQuestion(PersonDatingProfile profile, IncomingSMS incomingsms);

	/**
	 * 
	 * @param incomingsms
	 * @return
	 * @throws Exception
	 */
	public String startDatingQuestions(IncomingSMS incomingsms) throws Exception;
	
	/**
	 * 
	 * @param attribz
	 * @param incomingsms
	 * @return
	 */
	public String getNextQuestionOrange(Map<String, String> attribz, IncomingSMS incomingsms)  throws Exception;
	
	

}
