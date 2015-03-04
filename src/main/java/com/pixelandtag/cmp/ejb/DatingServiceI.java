package com.pixelandtag.cmp.ejb;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.pixelandtag.cmp.entities.TimeUnit;
import com.pixelandtag.dating.entities.Gender;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileQuestion;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.serviceprocessors.sms.DatingMessages;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.web.beans.RequestObject;


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
	public PersonDatingProfile findMatch(Gender pref_gender,BigDecimal pref_age, String location,Long curProfileId) throws DatingServiceException;
	public PersonDatingProfile findMatch(Gender pref_gender,BigDecimal pref_age,Long curProfileId) throws DatingServiceException;
	public PersonDatingProfile findMatch(Gender pref_gender,Long curProfileId) throws DatingServiceException;
	public MOSms renewSubscription(MOSms mo, Long serviceId) throws DatingServiceException;
	public PersonDatingProfile getProfileOfLastPersonIsentMessageTo(Person person, Long period, TimeUnit timeUnit) throws DatingServiceException;

	public BigInteger calculateAgeFromDob(Date dob) throws DatingServiceException;
	public String processDating(RequestObject ro) throws Exception;

	

}
