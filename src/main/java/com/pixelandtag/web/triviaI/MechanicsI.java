package com.pixelandtag.web.triviaI;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.pixelandtag.exceptions.MessageNotSetException;
import com.pixelandtag.exceptions.NoSettingException;
import com.pixelandtag.web.beans.Answer;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.beans.Question;
import com.pixelandtag.web.beans.Subscriber;
import com.pixelandtag.web.beans.TriviaLogRecord;

/**
 * 
 * @author Timothy Mwangi Gikonyo
 * @date created 12th Jan 2012
 * 
 * This deals with handling Http requests
 * during gameplay, logging to the database,
 * and giving a Http response.
 * 
 *
 */
public interface MechanicsI {
	
	/**
	 * The database name
	 */
	//public final String DATABASE = "axiata_trivia";
	
	/**
	 * The default language id if not set
	 */
	public final int DEFAULT_LANGUAGE_ID = -1;
	public final int SHOP_BUCKET_POINTS = 10;
	public final int REGISTRATION_POINTS = 0;

	public final int TEASERS_CHANGE_AFTER = 300;//The teasers are changed after sending to TEASERS_CHANGE_AFTER subscribers
	
	/**
	 * Registers a user to the trivia.
	 * @param msisdn - java.lang.String  -the msisdn of the sub
	 * @return true if the transaction was successful, else return false
	 */
	public boolean registerSubscriber(String msisdn);
	
	/**
	 * Registers a user to the trivia.
	 * @param msisdn - java.lang.String  -the msisdn of the sub
	 * @param name - java.lang.String  - the given name of the sub
	 * @param languageId - int - the sub's language id that he chose
	 * @return true if the transaction was successful, else returns false
	 */
	public boolean registerSubscriber(String msisdn, String name, int languageId);
	
	/**
	 * Registers a subscriber to the trivia.
	 * @param msisdn - java.lang.String  -the msisdn of the sub
	 * @param name - java.lang.String  - the given name of the sub
	 * @return boolean true if the transaction was successful, else returns false
	 */
	public boolean registerSubscriber(String msisdn, String name);
	
	/**
	 * Activates or de-activates a subscriber.
	 * Sets active value to true or false depending on whether sub is so
	 * @param msisdn - java.lang.String  -the msisdn of the sub
	 * @param active - boolean - a flag that decides what the sub will be between
	 * active and inactive
	 * @return boolean true if the transaction was successful, else returns false
	 */
	public boolean toggleSubActive(String msisdn, boolean active);
	
	/**
	 * Changes the subscriber's name
	 * @param msisdn - java.lang.String  -the msisdn of the sub
	 * @param name - java.lang.String  - the given name of the sub
	 * @return boolean - true if the transaction was successful, else returns false
	 */
	public boolean changeSubscriberName(String msisdn, String name);
	
	/**
	 * Gets the language id.
	 * When you pass a language String, it is matched against
	 * the language table and retrieves the id of the language
	 * @param language - java.lang.String  - the language, e.g ENGLISH,MALAY,INDONESIA
	 * @return int - the language id
	 */
	public int getLanguageID(String language);
	
	/**
	 * Sets the subscriber's language using languageid.
	 * If the sub decides to change the language.
	 * @param msisdn - java.lang.String - the msisdn
	 * @param languageid - int - the language id
	 * @return boolean - true if the transaction was successful, else returns false
	 */
	public boolean setLanguage(String msisdn, int languageid);
	
	/**
	 * 
	 * @param msisdn
	 * @param message
	 * @return boolean - true if the transaction was successful, else returns false
	 */
	public boolean insertIntoHTTPToSend(String msisdn, String message);
	
	
	/**
	 * 
	 * @param msisdn
	 * @param message
	 * @param txid - attached transaction id
	 * @return boolean - true if the transaction was successful, else returns false
	 */
	public boolean insertIntoHTTPToSend(String msisdn, String message, long txid);
	
	/**
	 * 
	 * @param questionId
	 * @return enum - com.pixelandtag.web.beans.Answer - answer of the question
	 */
	public Answer getCorrectAnswer(int questionId);
	
	/**
	 * Gets the question using the provided questionid from the database
	 * @param questionId
	 * @return com.pixelandtag.web.beans.Question - the question object
	 */
	public Question getQuestion(int questionId);
	
	/**
	 * When system sends a subscriber a question, the questionid
	 * is stored in the table questions_sent.
	 * The record has a flag - answered that marks the question as either
	 * answered or not. using this, we retrieve the last unanswered question
	 * sent to subscriber.
	 * 
	 * Gets the last unanswered question from the database.
	 * @param msisdn
	 * @return com.pixelandtag.web.beans.Question - the question object
	 */
	public Question getLastUnansweredQuestionSentToSub(String msisdn);

	/**
	 * Gets the next question to send to the subscriber
	 * @param sub
	 * @return com.pixelandtag.web.beans.Question - the question
	 */
	public Question getQuestionToSend(Subscriber sub);
	
	
	/**
	 * Gets the first question to the subscriber
	 * @param msisdn
	 * @return com.pixelandtag.web.beans.Question - the question
	 */
	public Question getFirstQuestion(String msisdn);
	
	
	/**
	 * Gets the next question to send to subscriber.
	 * @param msisdn
	 * @return com.pixelandtag.web.beans.Question - the question
	 */
	public Question getNextQuestion(String msisdn);
	
	/**
	 * 
	 * @param msisdn
	 * @param questionId
	 * @return boolean - true if the transaction was successful, else returns false
	 */
	public boolean logAsSentButNotAnswered(String msisdn, int questionId);
	
	/**
	 * Gets the id of the next question to be sent to the subscriber
	 * @param msisdn - java.lang.String - the msisdn of the subscriber
	 * @return int the questionid. -1 is returned if there is no next question to be sent
	 * or if we have run out of questions.
	 */
	public int getNextQuestionid(String msisdn);
	
	
	/**
	 * 
	 * @param key com.pixelandtag.web.beans.MessageType - can be either;<br/>
	 * 
	 * MessageType.WELCOME_MESSAGE		<br/>
	 * MessageType.CORRECT_ANSWER		<br/>
	 * MessageType.WRONG_ANSWER			<br/>
	 * MessageType.STOP_MESSAGE			<br/>
	 * MessageType.WELCOME_BACK_MESSAGE	<br/>
	 * MessageType.POINTS_REQUEST		<br/>	
	 * MessageType.RUSH_HOUR_START		<br/>
	 * MessageType.REGISTRATION_FAILURE	<br/>
	 * MessageType.REGISTER_FIRST		<br/>
	 * MessageType.NO_MORE_QUESTIONS	<br/>
	 * 
	 * @param languageID - int the languageid of the message desired
	 * @return java.lang.String - the message.
	 * @throws com.pixelandtag.exceptions.MessageNotSetException  - this is thown when the message is not set in the db.
	 */
	public String getMessage(MessageType key, int languageID) throws MessageNotSetException;
	
	
	/**
	 * Gets the setting in the settings table.
	 * 
	 * @param key - java.lang.String - 
	 * @return java.lang.String - the setting.
	 * @throws com.pixelandtag.exceptions.NoSettingException  - this is thrown when the setting is not set in the db
	 */
	public String getSetting(String key) throws NoSettingException;
	
	/**
	 * Calculates the subscriber's total points
	 * @param msisdn - the subscriber's msisdn
	 * @return int  - the total points of the subscriber
	 */
	public int calculateTotalPoints(String msisdn);
	
	/**
	 * Gets the subscriber's total points...
	 * This method does not calculate, but lifts the value from the db...
	 * TODO - saniti check of this method
	 * @param msisdn - java.lang.String - the msisdn.
	 * @return  java.lang.String - the points converted into a string.
	 */
	public String getTotalPoints(String msisdn);
	
	/**
	 * Gets the total points to award depending<br/> 
	 * <ul>
	 * <li>whether the answer provided earlier is correct or wrong</li>
	 * <li>Whether its point rush/gold rush/ etc</li>
	 * </ul>
	 * @param isCorrect - if the question provided was wrong or correct
	 * @param sub - com.pixelandtag.web.beans.Subscriber
	 * @return int - the points to award the subscriber
	 */
	public int getPointsToAward(boolean isCorrect, Subscriber sub);
	
	/**
	 * 
	 * @param msisdn
	 * @param points
	 * @return boolean - true if the transaction was successful, else returns false
	 */
	public boolean addPoint(String msisdn, int points);
	
	
	/**
	 * Gets the Subscriber's object from the msisdn provided
	 * @param msisdn - the sub's msisdn
	 * @return com.pixelandtag.web.beans.Subscriber - the subscriber if he exists, else null is returned
	 */
	public Subscriber getSubscriber(String msisdn);
	
	/**
	 * 
	 * @param triviaLog
	 * @return boolean - true if the transaction was successful, else returns false
	 */
	public boolean logPlay(TriviaLogRecord triviaLog);

	/**
	 * Basically closes the db connection. Also, you can do any
	 * cleanups in this method. It is the last method to be called
	 * after all transactions are done.
	 * 
	 * @throws SQLException
	 */
	public void closeConnection() throws SQLException;
	
	
	/**
	 * Personalizes the message to send to subscriber,
	 * replaces <NAME> with the sub's name, <POINTS> with the points
	 * that the subscriber has for the question answered,
	 * <TOTAL_POINTS> with the total points sub has
	 * @param message
	 * @param sub
	 * @param points
	 * @return
	 */
	public String perSonalizeMessage(String message, Subscriber sub, int points);

	/**
	 * Validates a provided answer against the stored answer in the db
	 * @param keyword
	 * @param sub
	 * @return boolean - true if the answer given is correct, false if the answer is wrong.
	 */
	public boolean validateAnswer(String answer, Subscriber sub);

	
	/**
	 * 
	 * @param msisdn
	 * @return boolean - true if the transaction was successful, else returns false
	 */
	public boolean updateShopBucket(String msisdn);
	
	
	/**
	 * Returns the points a sub is to get after subscribing
	 * @return int - the registration points
	 */
	public int getRegistration_points();

	/**
	 * 
	 * @param msisdn
	 * @param lastUnansweredQuestionSentToSub
	 * @return boolean - true if the transaction was successful, else returns false
	 */
	public boolean setAsAnswered(String msisdn, Question lastUnansweredQuestionSentToSub);
	
	/**
	 * checks if a subscriber is in the blacklist table.
	 * This could be subscribers who are playing for free, spammers etc.
	 * 
	 * @param msisdn - java.lang.Stringthe msisdn to blacklist 
	 * @return boolean - true if subscriber is in the blacklist table, false if they are not
	 */
	public boolean isInBlacklist(String msisdn);
	
	/**
	 * Blacklists a subscriber
	 * @param msisdn - java.lang.Stringthe msisdn to blacklist 
	 * @return boolean - true if the transaction was successful, else returns false
	 */
	public boolean blackistSubscriber(String msisdn);
	
	/**
	 * updates the last_action field
	 * @param msisdn - java.lang.Stringthe msisdn to blacklist 
	 * @return boolean - true if the transaction was successful, else returns false
	 */
	public boolean updateLastAction(String msisdn);
	
	
	/**
	 * Sets the timestamp for lastteased
	 * @param msisdn - the msisdn that was teased
	 * @return boolean - true if the transaction was successful, else returns false
	 */
	public boolean setTeased(String msisdn);
	
	
	/**
	 * Gets the teaser for sub
	 * @param sub - com.pixelandtag.web.beans.Subscriber
	 * @return java.lang.String - the teaser string for subscriber
	 */
	//public String getTeaser(Subscriber sub);
	
	
	/**
	 * Gets a map of teasers for all languages;
	 * @return
	 */
	public Map<Integer,Map<MessageType,String>> getTeasers() throws MessageNotSetException;
	
	
	/**
	 * Checks the db for happy hour.
	 * @return true if this very moment is a between start and stop 
	 * times of happy hour, else returns false.
	 */
	public boolean isHappyHour();
	
	
	/**
	 * Gets when the current happy hour ends.
	 * @return java.lang.String - happy hour end
	 */
	public String getHappyHourEnd();

	public void setHappyHour(boolean isHappyHour);

	/**
	 * Sets the flag for happy hour -start and stop.
	 * if it is happy hour, then happy_hour_start_notified is set to 1
	 * else happy_hour_stop_notified is set to 1
	 * @param msisdn - java.lang.String - the msisdn of the sub
	 * @param ishappyHour - boolean  true if its happy hour, false if not happy hour.
	 * @return boolean - true if the transaction was successful, else returns false
	 */
	public boolean happyHourNotified(String msisdn, boolean ishappyHour);
	
	
	
	/**
	 * We are not allowed to push content before 0900HRS and 1800 hours.
	 * This check must consider client time zone
	 * @return true if the current hour is between 9 and 18, else returns false if it's an unGodly hour :P
	 */
	public boolean isLegalTimeToSendBroadcasts();
	
	
	
	
	/**
	 * Basically runs this query..
	 * select HOUR(CONVERT_TZ(now(),'-05:00','+08:00'));
	 * Where '-05:00' is the time zone of the server where this application is running
	 * and '+08:00' is the timezone for the country where the product is being used. I heard that's how facebook did it :D and it's working
	 * @return the result for the query above.
	 */
	public int getHourNow();
	
	
	
	/**
	 * Cecom requested we auto subscribe..
	 * Then we must know if we teased and subscribed later, so this method comes in handy
	 * @param msisdn
	 * @return
	 */
	public boolean setTeasedAndAutoSubscribed(String msisdn);
	
	
	
	
	
	

}
