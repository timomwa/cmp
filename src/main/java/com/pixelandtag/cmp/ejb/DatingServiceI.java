package com.pixelandtag.cmp.ejb;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.pixelandtag.dating.entities.Gender;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileQuestion;
import com.pixelandtag.serviceprocessors.sms.DatingMessages;


public interface DatingServiceI extends BaseEntityI {
	
	public Person register(String msisdn) throws DatingServiceException;

	public Person getPerson(String msisdn)  throws DatingServiceException;
	
	public String getMessage(String key, int language_id) throws DatingServiceException;

	public String getMessage(DatingMessages successSubscribing, int language_id) throws DatingServiceException;

	public PersonDatingProfile getProfile(Person person) throws DatingServiceException;

	public ProfileQuestion getNextProfileQuestion(Long profile_id) throws DatingServiceException;

	public ProfileQuestion getPreviousQuestion(Long id) throws DatingServiceException;

	public boolean isUsernameUnique(String username) throws DatingServiceException;

	public Date calculateDobFromAge(BigDecimal age) throws DatingServiceException;

	public PersonDatingProfile getperSonUsingChatName(String chat_username) throws DatingServiceException;

	public PersonDatingProfile findMatch(Gender pref_gender,BigDecimal pref_age, String location) throws DatingServiceException;


}
