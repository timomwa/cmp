package com.pixelandtag.cmp.ejb;

import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.serviceprocessors.sms.DatingMessages;


public interface DatingServiceI {
	
	public Person register(String msisdn) throws DatingServiceException;
	
	
	public <T> T saveOrUpdate(T t) throws DatingServiceException ;


	public Person getPerson(String msisdn)  throws DatingServiceException;
	
	public String getMessage(String key, int language_id) throws DatingServiceException;


	public String getMessage(DatingMessages successSubscribing, int language_id) throws DatingServiceException;


}
