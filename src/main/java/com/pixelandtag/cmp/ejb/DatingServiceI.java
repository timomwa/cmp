package com.pixelandtag.cmp.ejb;

import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.serviceprocessors.sms.DatingMessages;


public interface DatingServiceI extends BaseEntityI {
	
	public Person register(String msisdn) throws DatingServiceException;

	public Person getPerson(String msisdn)  throws DatingServiceException;
	
	public String getMessage(String key, int language_id) throws DatingServiceException;

	public String getMessage(DatingMessages successSubscribing, int language_id) throws DatingServiceException;

	public PersonDatingProfile getProfile(Person person) throws DatingServiceException;


}
