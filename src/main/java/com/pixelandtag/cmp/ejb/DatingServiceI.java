package com.pixelandtag.cmp.ejb;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.pixelandtag.cmp.entities.TimeUnit;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.dating.entities.Gender;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileQuestion;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.serviceprocessors.sms.DatingMessages;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.smsmenu.MenuItem;
import com.pixelandtag.web.beans.RequestObject;


public interface DatingServiceI extends BaseEntityI {
	
	public Person register(String msisdn) throws DatingServiceException, Exception;

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
	public PersonDatingProfile findMatch(PersonDatingProfile profile) throws DatingServiceException;
	public MOSms renewSubscription(MOSms mo, Long serviceId, AlterationMethod method) throws DatingServiceException;
	public PersonDatingProfile getProfileOfLastPersonIsentMessageTo(Person person, Long period, TimeUnit timeUnit) throws DatingServiceException;

	public BigInteger calculateAgeFromDob(Date dob) throws DatingServiceException;
	public String processDating(RequestObject ro) throws Exception;
	public PersonDatingProfile getProfile(String msisdn) throws DatingServiceException;

	/**
	 * Sets the "active" and "logged in" flags
	 *  to false
	 * @param msisdn
	 * @return
	 */
	public boolean deactivate(String msisdn);
	
	public boolean reactivate(String msisdn);

	

	

	

}
