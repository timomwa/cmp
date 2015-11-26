package com.pixelandtag.cmp.ejb;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.TimeUnit;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.dating.entities.Gender;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileQuestion;
import com.pixelandtag.dating.entities.QuestionLog;
import com.pixelandtag.serviceprocessors.sms.DatingMessages;
import com.pixelandtag.web.beans.RequestObject;


public interface DatingServiceI extends BaseEntityI {
	
	public Person register(String msisdn, OperatorCountry opco) throws DatingServiceException, Exception;

	public Person getPerson(String msisdn, OperatorCountry opco)  throws DatingServiceException;
	
	public String getMessage(String key, int language_id, Long opcoid) throws DatingServiceException;

	public String getMessage(DatingMessages successSubscribing, int language_id, Long opcoid) throws DatingServiceException;

	public PersonDatingProfile getProfile(Person person) throws DatingServiceException;

	public ProfileQuestion getNextProfileQuestion(Long profile_id) throws DatingServiceException;

	public ProfileQuestion getPreviousQuestion(Long id) throws DatingServiceException;

	public boolean isUsernameUnique(String username) throws DatingServiceException;

	public Date calculateDobFromAge(BigDecimal age) throws DatingServiceException;

	public PersonDatingProfile getperSonUsingChatName(String chat_username) throws DatingServiceException;
	public PersonDatingProfile findMatch(Gender pref_gender,BigDecimal pref_age, String location,Long curProfileId,OperatorCountry opco) throws DatingServiceException;
	public PersonDatingProfile findMatch(Gender pref_gender,BigDecimal pref_age, String location,Long curProfileId) throws DatingServiceException;
	public PersonDatingProfile findMatch(Gender pref_gender,BigDecimal pref_age,Long curProfileId) throws DatingServiceException;
	public PersonDatingProfile findMatch(Gender pref_gender,Long curProfileId) throws DatingServiceException;
	public PersonDatingProfile findMatch(PersonDatingProfile profile) throws DatingServiceException;
	public PersonDatingProfile findMatch(Gender pref_gender,BigDecimal pref_age,Long curPersonId, OperatorCountry opco) throws DatingServiceException;
	public PersonDatingProfile findMatch(PersonDatingProfile profile, OperatorCountry opco) throws DatingServiceException;
	public PersonDatingProfile findMatch(Gender pref_gender, Long curPersonId, OperatorCountry opco) throws DatingServiceException;
	public OutgoingSMS renewSubscription(IncomingSMS icomingsms, Long serviceId, AlterationMethod method) throws DatingServiceException;
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

	public BigInteger countIncompleteProfiles();
	
	public BigInteger countAllProfiles(Gender gender);

	public List<PersonDatingProfile> listIncompleteProfiles(BigInteger start,BigInteger records_per_run);
	
	public QuestionLog getLastQuestionLog(Long profile_id);
	
	public String startProfileQuestions(String msisdn, Person person);

	public List<PersonDatingProfile> listCompleteProfiles(int first, int limit);

	public String findMatchString(PersonDatingProfile profile);

	public PersonDatingProfile searchMatch(PersonDatingProfile profile) throws DatingServiceException;

	

	

	

}
