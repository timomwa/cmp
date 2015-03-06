package com.pixelandtag.web.triviaImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;












import org.apache.log4j.Logger;

import com.pixelandtag.api.Settings;
import com.pixelandtag.axiata.teasers.producer.BroadcastApp;
import com.pixelandtag.customAnnotations.ReVisit;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.exceptions.MessageNotSetException;
import com.pixelandtag.exceptions.NoSettingException;
import com.pixelandtag.mms.api.MMS;
import com.pixelandtag.mms.api.SMS;
import com.pixelandtag.mms.api.TarrifCode;
import com.pixelandtag.web.beans.Answer;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.beans.Question;
import com.pixelandtag.web.beans.Subscriber;
import com.pixelandtag.web.beans.TriviaLogRecord;

/**
 * @author Timothy Mwangi Gikonyo
 *
 */
public class MechanicsS{
	
	
	/**
	 * The database name
	 */
	public static final String DATABASE = "axiata_trivia";
	
	/**
	 * The default language id if not set
	 */
	public static final int DEFAULT_LANGUAGE_ID = -1;
	public static final int SHOP_BUCKET_POINTS = 10;
	public static final int TODAY = 101;
	public static final int IN_TOTAL = 102;
	private static final String questions_per_mms = "questions_per_mms";
	private static final String max_revenue_per_sub_daily = "max_revenue_per_sub_daily";
	private static final String questions_quota_per_day = "questions_quota_per_day";
	public static int QUESTIONS_PER_MMS = 1;
	public static int DAILY_QUESTION_QUOTA = 2;
	public static int MAX_MONEY_SPENT_PER_DAY = 3;
	
	public static final int REGISTRATION_POINTS = 0;
	public static final int TEASERS_CHANGE_AFTER = 5;//The teasers are changed after sending to TEASERS_CHANGE_AFTER subscribers

	private static final int TRIVIA_SERVICE_ID = 2;//hard coded service id for the trivia

	public static final String INMOBIA = "INMOBIAIIIIIIIIIIII";
	private static String type;
	private static Logger logger = Logger.getLogger(MechanicsS.class);

	public static String fr_tz = "+08:00";

	public static String to_tz = "+08:00";
	
	public static String ENGLISH_ANSWERING_INSTRUCTIONS = ". Send A/B to 23355";
	public static String MALAY_ANSWERING_INSTRUCTIONS = ". Htr A/B ke 23355";

	private static int i;
	
	private static Semaphore semaphore = new Semaphore(1,true);
	
	
	private MechanicsS(){
		
	}
	
	public static String getType() {
		return type;
	}


	public static void setType(String type) {
		MechanicsS.type = type;
	}


	private static void log(Exception e){
		logger.error(e.getMessage(),e);
	}

	
	public static long generateNextTxId(){
		
		
		try {
			
			try{
				semaphore.acquire();
			}catch(Exception e){log(e);}
			
			try{
				Thread.sleep(1);
				}catch(Exception e){}
				
			/*String timestamp = String.valueOf(System.currentTimeMillis());
			String txid =  INMOBIA.substring(0, (19-timestamp.length())) + timestamp;//(String.valueOf(Long.MAX_VALUE).length()-timestamp.length())) + timestamp;
			*/
			
			
			return System.currentTimeMillis();
			
		}finally{
			semaphore.release();
		}
		
		
		
		
	}	
	
	
	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#insertIntoVasSMPPToSend(java.lang.String, java.lang.String)
	 */
	public boolean insertIntoHTTPToSend(String msisdn, String message, Connection conn) {
		
		PreparedStatement pstmt = null;
		//TODO introduce sort of semaphore here if this method will be accessed by multiple threads..
		boolean success = false;
		
		try {
			
			
				pstmt = conn.prepareStatement("insert into `celcom`.`httptosend`" +
				"(SMS,MSISDN,SendFrom,fromAddr,CMP_AKeyword,CMP_SKeyword,Priority,split,CMP_TxID) " +
				"VALUES(?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
				////cmp_a_keyword,cmp_s_keyword
			
			
			
			pstmt.setString(1, message);
			pstmt.setString(2, msisdn);
			pstmt.setString(3, "23355");
			pstmt.setString(4, "23355");
			
			pstmt.setString(5, "TRIVIA");
			
			if(message.startsWith("RM0"))
				pstmt.setString(6, TarrifCode.RM0.getCode());
			else if(message.startsWith("RM1"))
				pstmt.setString(6, TarrifCode.RM1.getCode());
			else
				pstmt.setString(6, TarrifCode.RM0.getCode());
		
			
			pstmt.setInt(7, 1);//though useless and pointless
			
			//pstmt.setString(10, String.valueOf(mo.getCMP_Txid()));
			pstmt.setInt(8, 1);
			pstmt.setLong(9, generateNextTxId());
			
			pstmt.executeUpdate();
			
			success = true;
		
		} catch (SQLException e) {
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				logger.error(e.getMessage(),e);
			
			}
		
		}
		
		return success;
	
	}
	
	
	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		MechanicsS.logger = logger;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#registerSubscriber(java.lang.String)
	 */
	public static boolean registerSubscriber(String msisdn, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		
		try{
			
			pstmt = conn.prepareStatement("INSERT INTO `"+DATABASE+"`.`subscriber_profile`(msisdn,last_action) VALUES(?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'))", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
			
		}
		
		return success;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#unregisterSubscriber(java.lang.String)
	 */
	public static boolean toggleSubActive(String msisdn, boolean active, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`subscriber_profile` SET active = "+(active ? "1" : "0")+" WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
		}
		
		return success;
	}
	
	
	
	
	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#unregisterSubscriber(java.lang.String)
	 */
	public static boolean toggleSubSubscribed(String msisdn, boolean subscribed, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`subscriber_profile` SET `subscribed` = "+(subscribed ? "1" : "0")+" WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return success;
	}
	
	
	
	
	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#unregisterSubscriber(java.lang.String)
	 */
	public static boolean changeSubLanguage(String msisdn, int language_id, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`subscriber_profile` SET language_id = ? WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setInt(1, language_id);
			
			pstmt.setString(2, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return success;
	}
	
	
	
	
	public static boolean toggleReachedDailyQuestionQuota(String msisdn, boolean quota_reached, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`subscriber_profile` SET `has_reached_questions_quota_for_today` = "+(quota_reached ? "1" : "0")+" WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return success;
	}
	
	
	
	public static boolean toggleSubContinuationConfirmed(String msisdn, boolean confirmed, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`subscriber_profile` SET `continuation_confirmed` = "+(confirmed ? "1" : "0")+" WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return success;
	}
	
	
	
	public static boolean toggleWeAreWaitingForSubConfirmation(String msisdn, boolean confirmed, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`subscriber_profile` SET `we_are_waiting_confirmation` = "+(confirmed ? "1" : "0")+" WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return success;
	}
	
	
	
	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#registerSubscriber(java.lang.String, java.lang.String)
	 */
	public static boolean registerSubscriber(String msisdn, String name, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("INSERT INTO `"+DATABASE+"`.`subscriber_profile`(msisdn,name,last_action) VALUES(?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'))", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			pstmt.setString(2, name);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return success;
	
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#registerSubscriber(java.lang.String, java.lang.String, int)
	 */
	public static int registerSubscriber(String msisdn, String name, int languageId, Connection conn) {
		
		int subscriber_id = -1;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		
		try{
			
			pstmt = conn.prepareStatement("INSERT INTO `"+DATABASE+"`.`subscriber_profile`(msisdn,name,language_id,last_action) VALUES(?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'))", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			pstmt.setString(2, name);
			pstmt.setInt(3, languageId);
			
			pstmt.executeUpdate();
			
			rs = pstmt.getGeneratedKeys();
			
			if(rs.next())
				subscriber_id = rs.getInt(1);
			
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return subscriber_id;
	
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#changeSubscriberName(java.lang.String, java.lang.String)
	 */
	public static boolean changeSubscriberName(String msisdn, String name, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`subscriber_profile` SET name = ? WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, name);
			pstmt.setString(2, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return success;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getLanguageID(java.lang.String)
	 */
	public static int getLanguageID(String language_, Connection conn) {
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int languageid = 1;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT id FROM `"+DATABASE+"`.`languages` WHERE name = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, language_);
			
			rs = pstmt.executeQuery();
			
			if(rs.next())
				languageid = rs.getInt(1);
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return languageid;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#setLanguage(java.lang.String, int)
	 */
	public static boolean setLanguage(String msisdn, int languageid, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`subscriber_profile` SET language_id=? WHERE msisdn=?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setInt(1, languageid);
			
			pstmt.setString(2, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return success;
	}
	

	

	/**
	 * 
	 * @param msisdn - the msisdn/Sub_Mobtel
	 * @param message - sms to be sent
	 * @param txid - transaction id
	 * @param conn
	 * @return
	 */
	public static boolean insertIntoHttpToSend(String msisdn, String message,String txid, int serviceid, double price,Connection conn) {
		

		PreparedStatement pstmt = null;
	
		boolean success = false;
		
		try {
			
			
				pstmt = conn.prepareStatement("insert into `celcom`.`httptosend`" +
				"(SMS,MSISDN,SendFrom,fromAddr,CMP_AKeyword,CMP_SKeyword,Priority,split,CMP_TxID,serviceid,price) " +
				"VALUES(?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
				////cmp_a_keyword,cmp_s_keyword
			
			
			
			pstmt.setString(1, message);
			pstmt.setString(2, msisdn);
			pstmt.setString(3, "23355");
			pstmt.setString(4, "23355");
			
			pstmt.setString(5, "TRIVIA");
			
			if(message.trim().startsWith("RM0"))
				pstmt.setString(6, TarrifCode.RM0.getCode());
			else if(message.trim().startsWith("RM1"))
				pstmt.setString(6, TarrifCode.RM1.getCode());
			else
				pstmt.setString(6, TarrifCode.RM0.getCode());
		
			
			pstmt.setInt(7, 1);//though useless and pointless
			
			pstmt.setInt(8, 1);
			pstmt.setString(9, txid);
			pstmt.setInt(10, serviceid);
			pstmt.setDouble(11, price);
			
			pstmt.executeUpdate();
			
			success = true;
		
		} catch (SQLException e) {
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				logger.error(e.getMessage(),e);
			
			}
		
		}
		
		return success;
	}
	
	
	/**
	 * 
	 * @param tx_id
	 * @return
	 */
	public static MTsms getMTsmsFromMessageLog(BigInteger tx_id, Connection conn){
		
		MTsms mtsms = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `celcom`.`messagelog` WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)",Statement.RETURN_GENERATED_KEYS);
			pstmt.setBigDecimal(1, new BigDecimal(tx_id));
			pstmt.setBigDecimal(2, new BigDecimal(tx_id));
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				mtsms = new MTsms();
				mtsms.setId(rs.getInt("id"));
				mtsms.setCMP_Txid(new BigInteger(rs.getString("CMP_Txid")));
				//mtsms.setSms(rs.getString("MO_Received"));
				mtsms.setShortcode(rs.getString("SMS_SourceAddr"));
				mtsms.setSUB_R_Mobtel(rs.getString("SUB_Mobtel"));
				mtsms.setSMS_DataCodingId(rs.getString("SMS_DataCodingId"));
				mtsms.setCMP_AKeyword(rs.getString("CMP_Keyword"));
				mtsms.setCMP_SKeyword(rs.getString("CMP_SKeyword"));
				mtsms.setServiceid(rs.getInt("serviceid"));
				mtsms.setMT_STATUS(rs.getString("MT_STATUS"));
				mtsms.setPrice(BigDecimal.valueOf(rs.getDouble("price")));
			}
			
			
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}finally{
			
			try {
				rs.close();
			} catch (Exception e) {
			}
			try {
				pstmt.close();
			} catch (Exception e) {
			}
		}
		
		return mtsms;
	}
	
	
	/**
	 * 
	 * @param msisdn
	 * @param message
	 * @param txid
	 * @param serviceid
	 * @param price
	 * @param shortcode
	 * @param CMP_Keyword
	 * @param conn
	 * @return true if tx successful, false if not.
	 */
	public static boolean insertIntoHttpToSend(String msisdn, String message,long txid, int serviceid, double price, String shortcode,
			String CMP_Keyword, String CMP_SKeyword, Connection conn) {
		

		PreparedStatement pstmt = null;
	
		boolean success = false;
		
		try {
			
			
				pstmt = conn.prepareStatement("insert into `celcom`.`httptosend`" +
				"(SMS,MSISDN,SendFrom,fromAddr,CMP_AKeyword,CMP_SKeyword,Priority,split,CMP_TxID,serviceid,price) " +
				"VALUES(?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
				////cmp_a_keyword,cmp_s_keyword
			
			
			
			pstmt.setString(1, message);
			pstmt.setString(2, msisdn);
			pstmt.setString(3, shortcode);
			pstmt.setString(4, shortcode);
			
			pstmt.setString(5, CMP_Keyword);
			
			if(message.trim().startsWith("RM0"))
				pstmt.setString(6, TarrifCode.RM0.getCode());
			else if(message.trim().startsWith("RM1"))
				pstmt.setString(6, TarrifCode.RM1.getCode());
			else if(message.trim().startsWith("RM0.15"))
				pstmt.setString(6, TarrifCode.RM_015.getCode());
			else
				pstmt.setString(6, CMP_SKeyword);
			
			pstmt.setInt(7, 1);
			
			pstmt.setInt(8, 1);
			pstmt.setLong(9, txid);
			pstmt.setInt(10, serviceid);
			pstmt.setDouble(11, price);
			
			pstmt.executeUpdate();
			
			success = true;
		
		} catch (SQLException e) {
			
			logger.error(e.getMessage(),e);
			
		}catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				logger.error(e.getMessage(),e);
			
			}
		
		}
		
		return success;
	}
	
	
	
	
	
	public static boolean insertIntoHttpToSend(String msisdn, String message,long txid, int serviceid, double price, String shortcode,
			String CMP_Keyword, String CMP_SKeyword, Boolean split,Connection conn) {
		

		PreparedStatement pstmt = null;
	
		boolean success = false;
		
		try {
			
			
				pstmt = conn.prepareStatement("insert into `celcom`.`httptosend`" +
				"(SMS,MSISDN,SendFrom,fromAddr,CMP_AKeyword,CMP_SKeyword,Priority,split,CMP_TxID,serviceid,price) " +
				"VALUES(?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
				////cmp_a_keyword,cmp_s_keyword
			
			
			
			pstmt.setString(1, message);
			pstmt.setString(2, msisdn);
			pstmt.setString(3, shortcode);
			pstmt.setString(4, shortcode);
			
			pstmt.setString(5, CMP_Keyword);
			
			if(message.trim().startsWith("RM0"))
				pstmt.setString(6, TarrifCode.RM0.getCode());
			else if(message.trim().startsWith("RM1"))
				pstmt.setString(6, TarrifCode.RM1.getCode());
			else if(message.trim().startsWith("RM0.15"))
				pstmt.setString(6, TarrifCode.RM_015.getCode());
			else
				pstmt.setString(6, CMP_SKeyword);
			
			pstmt.setInt(7, 1);
			
			pstmt.setBoolean(8, split);
			pstmt.setLong(9, txid);
			pstmt.setInt(10, serviceid);
			pstmt.setDouble(11, price);
			
			pstmt.executeUpdate();
			
			success = true;
		
		} catch (SQLException e) {
			
			logger.error(e.getMessage(),e);
			
		}catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				logger.error(e.getMessage(),e);
			
			}
		
		}
		
		return success;
	}
	
	
	/**
	 * 
	 * @param msisdn
	 * @param message
	 * @param newCMPTxid
	 * @param serviceid
	 * @param price
	 * @param originalCmpTxid
	 * @param conn
	 * @return
	 */
	public static boolean insertIntoHttpToSend(String msisdn, String message,long newCMPTxid, int serviceid, double price, String cmpTxid, Connection conn) {
		

		PreparedStatement pstmt = null;
	
		boolean success = false;
		
		try {
			
			
				pstmt = conn.prepareStatement("insert into `celcom`.`httptosend`" +
				"(SMS,MSISDN,SendFrom,fromAddr,CMP_AKeyword,CMP_SKeyword,Priority,split,CMP_TxID,serviceid,price,newCMP_Txid) " +
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
				////cmp_a_keyword,cmp_s_keyword
			
			
			
			pstmt.setString(1, message);
			pstmt.setString(2, msisdn);
			pstmt.setString(3, "23355");
			pstmt.setString(4, "23355");
			
			pstmt.setString(5, "TRIVIA");
			
			if(message.trim().startsWith("RM0"))
				pstmt.setString(6, TarrifCode.RM0.getCode());
			else if(message.trim().startsWith("RM1"))
				pstmt.setString(6, TarrifCode.RM1.getCode());
			else
				pstmt.setString(6, TarrifCode.RM0.getCode());
		
			
			pstmt.setInt(7, 1);//though useless and pointless
			
			pstmt.setInt(8, 1);
			pstmt.setString(9, cmpTxid);
			pstmt.setInt(10, serviceid);
			pstmt.setDouble(11, price);
			pstmt.setLong(12, newCMPTxid);//for CMP_Txid
			
			
			pstmt.executeUpdate();
			
			success = true;
		
		} catch (SQLException e) {
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				logger.error(e.getMessage(),e);
			
			}
		
		}
		
		return success;
	}
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getFirstQuestion(java.lang.String)
	 */
	public static Question getFirstQuestion(Subscriber subscriber,Connection conn){
		return getNextQuestion(subscriber,conn);
	}
	
	
	

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getNextQuestion(java.lang.String)
	 */
	public static Question getNextQuestion(Subscriber sub,Connection conn) {
		
		Question question = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		Answer answer = null;
		
		try{
			
			//We try not to get a question from the category we had previously sent them
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`questions` qstn WHERE `qstn`.`usable`=1 AND `qstn`.`language_id` = ? AND `qstn`.`id` NOT IN (SELECT question_idFK from `"+DATABASE+"`.`trivia_log` trl WHERE trl.subscriber_profileFK=? AND trl.question_idFK>-1) AND `qstn`.`question_origin` NOT IN (SELECT qo.question_origin FROM (SELECT count(*) as 'cnt', question_origin FROM `axiata_trivia`.`trivia_log` atl WHERE atl.subscriber_profileFK=? AND atl.question_idFK>-1 group by atl.question_origin order by cnt desc LIMIT 1) qo) AND question_origin not in (112) ORDER BY RAND() LIMIT 1", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setInt(1, (sub.getLanguage_id_()==-1 ? 1 : sub.getLanguage_id_()));
			pstmt.setInt(2, sub.getId());
			pstmt.setInt(3, sub.getId());
			
			rs = pstmt.executeQuery();
			
			String ans = null;
			
			if(rs.next()){
				ans = rs.getString("answer");
			
				if(ans!=null)
					if(ans.trim().toUpperCase().equals("A"))
						answer = Answer.A;
					else if(ans.trim().toUpperCase().equals("B"))
						answer = Answer.B;
				
				question = new Question();
				
				question.setAnswer(answer);
				question.setDifficulty(rs.getInt("difficulty"));
				question.setId(rs.getInt("id"));
				question.setLanguage_id(rs.getInt("language_id"));
				question.setQuestion(rs.getString("question").trim());
				question.setQuestion_origin(rs.getInt("question_origin"));
				question.setTimeStampOfInsersion(rs.getString("timeStamp_Of_Insertion"));
			
			
			}else{
				
				logger.debug("::::::::::::::::::::::::Could not distribute question fairly, now getting any other random question");
				
				try{
					rs.close();
				}catch(Exception e){}
				try{
					pstmt.close();
				}catch(Exception e){}
				
				
				pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`questions` qstn WHERE `qstn`.`usable`=1 AND `qstn`.`language_id` = ? AND `qstn`.`id` NOT IN (SELECT question_idFK from `"+DATABASE+"`.`trivia_log` trl WHERE trl.subscriber_profileFK = ? AND trl.question_idFK>-1) AND question_origin not in (112) ORDER BY RAND() LIMIT 1", Statement.RETURN_GENERATED_KEYS);
				pstmt.setInt(1, (sub.getLanguage_id_()==-1 ? 1 : sub.getLanguage_id_()));
				pstmt.setInt(2, sub.getId());
				
				rs = pstmt.executeQuery();
				
				
				if(rs.next()){
					
						ans = rs.getString("answer");
						
						if(ans!=null)
							if(ans.trim().toUpperCase().equals("A"))
								answer = Answer.A;
							else if(ans.trim().toUpperCase().equals("B"))
								answer = Answer.B;
						
						question = new Question();
						
						question.setAnswer(answer);
						question.setDifficulty(rs.getInt("difficulty"));
						question.setId(rs.getInt("id"));
						question.setLanguage_id(rs.getInt("language_id"));
						question.setQuestion(rs.getString("question").trim());
						question.setQuestion_origin(rs.getInt("question_origin"));
						question.setTimeStampOfInsersion(rs.getString("timeStamp_Of_Insertion"));
						
				}else{
					
					//There are no questions to send to this subscriber. Alert someone!
					logger.warn("There are no more questions for "+sub.getMsisdn());
					
				}
				
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return question;
	}
	
	
	
	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getNextQuestion(java.lang.String)
	 */
	public static Question getNextWinningQuestion(int language_id, Connection conn) {
		
		
		
		Question question = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		Answer answer = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`questions` WHERE `usable`=1 AND `question_origin`=112  AND  `language_id` = ? ORDER BY RAND() LIMIT 1", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setInt(1, language_id);
			
			rs = pstmt.executeQuery();
			
			String ans = null;
			
			if(rs.next()){
				ans = rs.getString("answer");
			
				if(ans!=null)
					if(ans.trim().toUpperCase().equals("A"))
						answer = Answer.A;
					else if(ans.trim().toUpperCase().equals("B"))
						answer = Answer.B;
				
				question = new Question();
				
				question.setAnswer(answer);
				question.setDifficulty(rs.getInt("difficulty"));
				question.setId(rs.getInt("id"));
				question.setLanguage_id(rs.getInt("language_id"));
				question.setQuestion(rs.getString("question").trim());
				question.setQuestion_origin(rs.getInt("question_origin"));
				question.setTimeStampOfInsersion(rs.getString("timeStamp_Of_Insertion"));
			
			
			}else{
				//There are no more winner questions. Alert someone!
				logger.warn("Add more winner questions");
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return question;
	}


	
	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getNextQuestionid(java.lang.String)
	 */
	@Deprecated
	public static int getNextQuestionid(String msisdn, Connection conn) {
		
		int nextQuestion_id = -1;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT id FROM `"+DATABASE+"`.`questions` WHERE id NOT IN (SELECT questionIdFK FROM `"+DATABASE+"`.`questions_sent` WHERE msisdn = ?) ORDER BY RAND()", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
					
				nextQuestion_id = rs.getInt("id");
				
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return nextQuestion_id;
		
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getMessage(com.pixelandtag.web.beans.MessageType, int)
	 */
	public static String getMessage(MessageType key,int languageID, Connection conn) throws MessageNotSetException{
		
		if(languageID==-1)
			try {
				
				languageID = Integer.valueOf(MechanicsS.getSetting("default_language_id", conn));
			
			} catch (NumberFormatException e1) {
				
				languageID = 1;
				
				log(e1);
			
			} catch (NoSettingException e1) {
				
				languageID = 1;
				
				log(e1);
			
			}
		
		logger.debug("languageID : "+languageID);
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		String message = "Error: MESSAGE with KEY \""+key.toString()+"\" not set in db.";
		
	
		
		try{
			
			logger.debug("SELECT message FROM `"+DATABASE+"`.`message` WHERE `key` = '"+key.toString()+"' AND language_id = "+languageID+" ORDER BY RAND() LIMIT 1");
			pstmt = conn.prepareStatement("SELECT message FROM `"+DATABASE+"`.`message` WHERE `key` = ? AND language_id = ? ORDER BY RAND() LIMIT 1", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, key.toString());
			
			pstmt.setInt(2, languageID);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				message = rs.getString(1);
				
			}else{
				
				throw new MessageNotSetException(message);
			
			}
			logger.debug("msg: "+message);
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		if(message.startsWith("Error:")){
			message = "";
		}
		
		return message;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#perSonalizeMessage(java.lang.String, com.pixelandtag.web.beans.Subscriber, int)
	 */
	public static String perSonalizeMessage(String message, Subscriber sub, int pointsAwarded, Connection conn) {
		
		
		if(message.indexOf("<TOTAL_QUESTIONS_ANSWERED_TODAY>")>-1){
			
			message = message.replace("<TOTAL_QUESTIONS_ANSWERED_TODAY>", String.valueOf(getTotalAnsweredQuestions(TODAY, sub.getMsisdn(), conn)));
		
		}
		
		if(message.indexOf("<NAME>")>-1){
			
			message = message.replace("<NAME>", sub.getName().trim());
			
		}
			
		if(message.indexOf("<TOTAL_POINTS>")>-1){
			
			String totalPoints = sub.getTotalPoints()>0 ? String.valueOf(sub.getTotalPoints()) : getTotalPoints(sub.getMsisdn(),conn);
			if(totalPoints==null)
				totalPoints = "0";
				message = message.replace("<TOTAL_POINTS>", totalPoints);
		}
		
		if(message.indexOf("<WEEKLY_POINTS>")>-1){
			
			String totalPoints = getTotalPoints(sub.getMsisdn(),conn);
			if(totalPoints==null)
				totalPoints = "0";
				message = message.replace("<WEEKLY_POINTS>", String.valueOf(totalPoints));
		}
		
		if(message.indexOf("<POINTS>")>-1 && pointsAwarded>0){
			
			message = message.replace("<POINTS>", String.valueOf(pointsAwarded));
		
		}if(message.indexOf("<HAPPY_HOUR_MULTIPLIER>")>-1){
			
			
			try {
				
				message = message.replace("<HAPPY_HOUR_MULTIPLIER>", getSetting("happy_hour_multiplier",conn));
			
			} catch (NoSettingException e) {
				
				message = message.replace("<HAPPY_HOUR_MULTIPLIER>", "10");
				
				log(e);
			
			}
		
		}if(message.indexOf("<HAPPY_HOUR_END>")>-1){
			
			message = message.replace("<HAPPY_HOUR_END>", getHappyHourEnd(conn));
			
		}
		
		message = message.trim();
		
		
		int x = 0;
		intd:
			while(message.indexOf("  ")>-1){
				message = message.replaceAll("  "," ");
				if(x==5)
					break intd;
				x++;
			}
		
		
		message += (message.endsWith(".") || message.endsWith("!")) ? "" : ".";//If the editor did not add a full stop to the resp msgs
		
		return message;
	
	}

	

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getSetting(java.lang.String)
	 */
	public static String getSetting(String key, Connection conn) throws NoSettingException {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		String setting = "ERROR: No setting with key \""+key+"\" is set in db.";
	
		try{
			
			pstmt = conn.prepareStatement("SELECT value FROM `"+DATABASE+"`.`settings` WHERE `key` = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, key);
			
			rs = pstmt.executeQuery();
			
			if(rs.next())
				setting = rs.getString(1);
			else
				throw new NoSettingException(setting);
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			

		}
		
		return setting;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#calculateTotalPoints(java.lang.String)
	 */
	public static int calculateTotalPoints(String msisdn, Connection conn) {
		
		int points = 0;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		
		try{
			
			pstmt = conn.prepareStatement("SELECT sum(points) FROM `"+DATABASE+"`.`trivia_log` WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				points = rs.getInt(1);
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return points;
	}
	
	
	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getTotalPoints(java.lang.String)
	 */
	public static String getTotalPoints(String msisdn, Connection conn) {
		
		String points = "0";
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT sum(points) FROM `"+DATABASE+"`.`trivia_log` WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				points = rs.getString(1);
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}

			
		}
		
		return points;
	}
	

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getSubscriber(java.lang.String)
	 */
	public static Subscriber getSubscriber(String msisdn, Connection conn) {
		
		Subscriber sub = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`subscriber_profile` WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				sub = new Subscriber();
				sub.setId(rs.getInt("id"));
				sub.setMsisdn(rs.getString("msisdn"));
				sub.setLanguage_id_(rs.getInt("language_id"));
				sub.setName(rs.getString("name"));
				sub.setLast_action(rs.getString("last_action"));
				sub.setLast_teaser_id(rs.getString("last_teaser_id"));
				sub.setSubscribed(rs.getString("subscribed"));
				sub.setLast_teased(rs.getString("last_teased"));
				sub.setActive((rs.getInt("active")==1));
				sub.setContinuation_confirmed((rs.getInt("continuation_confirmed")==1));
				sub.setHas_reached_questions_quota_for_today((rs.getInt("has_reached_questions_quota_for_today")==1));
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return sub;
	}
	
	

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getCorrectAnswer(int)
	 */
	public static Answer getCorrectAnswer(int questionId, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		Answer answer = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT answer FROM `"+DATABASE+"`.`questions` WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setInt(1, questionId);
			
			rs = pstmt.executeQuery();
			
			String ans = null;
			
			if(rs.next())
				ans = rs.getString(1);
			
			if(ans!=null)
				if(ans.trim().toUpperCase().equals("A"))
					answer = Answer.A;
				else if(ans.trim().toUpperCase().equals("B"))
					answer = Answer.B;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
				
		}
		
		return answer;
	}

	
	public static Question getLastQuestionSentToSub(String msisdn, Connection conn, boolean answered) {
		
		Question question = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT *  FROM `"+DATABASE+"`.`questions` WHERE id = (SELECT questionIdFK FROM `"+DATABASE+"`.`questions_sent` qs WHERE qs.msisdn = ? and qs.answered = "+(answered ? "1" : "0")+")", Statement.RETURN_GENERATED_KEYS);
			
			
			//logger.debug("::::::::::::::::::::::::::::::::::::::::::::::::\n SELECT *  FROM `"+DATABASE+"`.`questions` WHERE id = (SELECT questionIdFK FROM `"+DATABASE+"`.`questions_sent` qs WHERE qs.msisdn = '"+msisdn+"' and qs.answered = "+(answered ? "1" : "0")+") \n :::::::::::::::::::::::::::::::::::::::::::::\n");
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				String ans = rs.getString("answer");
				
				Answer answer = null;
				
				if(ans!=null)
					if(ans.trim().toUpperCase().equals("A"))
						answer = Answer.A;
					else if(ans.trim().toUpperCase().equals("B"))
						answer = Answer.B;
				
				question = new Question();
				
				question.setAnswer(answer);
				question.setDifficulty(rs.getInt("difficulty"));
				question.setId(rs.getInt("id"));
				question.setLanguage_id(rs.getInt("language_id"));
				question.setQuestion(rs.getString("question").trim());
				question.setQuestion_origin(rs.getInt("question_origin"));
				question.setTimeStampOfInsersion(rs.getString("timeStamp_Of_Insertion"));
			
			
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return question;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#updateQuestionSent(java.lang.String, int)
	 */
	public static boolean logAsSentButNotAnswered(String msisdn, int questionId, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("INSERT INTO `"+DATABASE+"`.`questions_sent`(msisdn,questionIdFK) VALUES(?,?) ON DUPLICATE KEY UPDATE `questionIdFK` = ?, `answered`=0", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			pstmt.setInt(2, questionId);
			
			pstmt.setInt(3, questionId);
			
			/*pstmt.setString(3, msisdn);
			
			pstmt.setInt(4, questionId);
			*/
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return success;
	}
	
	
	

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getQuestion(int)
	 */
	public static Question getQuestion(int questionId, Connection conn) {
		
		Question question = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		Answer answer = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`questions` WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setInt(1, questionId);
			
			rs = pstmt.executeQuery();
			
			String ans = null;
			
			if(rs.next()){
				ans = rs.getString("answer");
			
				if(ans!=null)
					if(ans.trim().toUpperCase().equals("A"))
						answer = Answer.A;
					else if(ans.trim().toUpperCase().equals("B"))
						answer = Answer.B;
				
				question = new Question();
				
				question.setAnswer(answer);
				question.setDifficulty(rs.getInt("difficulty"));
				question.setId(rs.getInt("id"));
				question.setLanguage_id(rs.getInt("language_id"));
				question.setQuestion(rs.getString("question"));
				question.setQuestion_origin(rs.getInt("question_origin"));
				question.setTimeStampOfInsersion(rs.getString("timeStamp_Of_Insertion"));
			
			
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
						
		}
		
		return question;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#logPlay(com.pixelandtag.web.beans.TriviaLogRecord)
	 */
	public static boolean logPlay(TriviaLogRecord triviaLog, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("INSERT INTO `"+DATABASE+"`.`trivia_log`(msisdn,name,correct,question_idFK,answer,points,timeStamp, price, winning_question, question_origin, subscriber_profileFK) VALUES(?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'),?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			//"ON DUPLICATE KEY UPDATE `msisdn`=?, `name`=?, `correct`=?, `question_idFK`=?, `answer`=?, `points`=?", Statement.RETURN_GENERATED_KEYS);
			
			int n = 0;
			pstmt.setString(++n, triviaLog.getMsisdn());
			pstmt.setString(++n, triviaLog.getName());
			pstmt.setInt(++n, triviaLog.isCorrect());
			pstmt.setInt(++n, triviaLog.getQuestion_idFK());
			pstmt.setString(++n, triviaLog.getAnswer());
			pstmt.setInt(++n, triviaLog.getPoints());
			pstmt.setDouble(++n, Double.parseDouble(triviaLog.getPrice()));
			pstmt.setInt(++n, (triviaLog.isWinningQuestion() ? 1 : 0));
			pstmt.setInt(++n, triviaLog.getQuestion_origin());
			pstmt.setInt(++n, triviaLog.getSubscriber_profileFK());
			//On duplicate key update
			/*pstmt.setString(++n, triviaLog.getMsisdn());
			pstmt.setString(++n, triviaLog.getName());
			pstmt.setInt(++n, triviaLog.isCorrect());
			pstmt.setInt(++n, triviaLog.getQuestion_idFK());
			pstmt.setString(++n, triviaLog.getAnswer());
			pstmt.setInt(++n, triviaLog.getPoints());*/
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return success;
		
		
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#addPoint(java.lang.String, int)
	 */
	public static boolean addPoint(String msisdn, int points, Connection conn) {
		
		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		
		boolean success = false;
		
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`points` WHERE msisdn=?",Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			int i = -1;
			
			if(!rs.next()){
				
				pstmt2 = conn.prepareStatement("INSERT INTO `"+DATABASE+"`.`points`(msisdn,points) VALUES(?,?)",Statement.RETURN_GENERATED_KEYS);
				pstmt2.setString(1, msisdn);
				pstmt2.setInt(2, points);
				i = pstmt2.executeUpdate();
				
				success = i>0;
				
			}else{
			
				pstmt = conn.prepareStatement("UPDATE  `"+DATABASE+"`.`points` SET points=? WHERE msisdn=?", Statement.RETURN_GENERATED_KEYS);
				
				pstmt.setInt(1, points);
				pstmt.setString(2, msisdn);
				i = pstmt.executeUpdate();
				
				success = i>0;
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt2!=null)
					pstmt2.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return success;
	}


	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#validateAnswer(java.lang.String, com.pixelandtag.web.beans.Subscriber)
	 */
	public static boolean validateAnswer(String answer, Subscriber sub, Connection conn) {
		
		Question question  = getLastQuestionSentToSub(sub.getMsisdn(),conn, false);
		
		logger.debug("Question's answer: "+question.getAnswer().toString());
		
		if(question.getAnswer().toString().equals(answer))
			return true;
		else
			return false;
		
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getQuestionToSend(com.pixelandtag.web.beans.Subscriber)
	 */
	@Deprecated @ReVisit
	public static Question getQuestionToSend(Subscriber sub, Connection conn){
		
		int nextQuestionId = getNextQuestionid(sub.getMsisdn(),conn);
		
		if(nextQuestionId==-1){
			//There are no questions to send to this subscriber. Alert someone!
			logger.warn("There are no more questions for "+sub.getMsisdn());
			return null;
		
		}else{
				
			Question q = getNextQuestion(sub,conn);
			
			if(q==null){
				//There are no questions to send to this subscriber. Alert someone!
				logger.warn("There are no more questions for "+sub.getMsisdn());
				
			}
			
			return q;
			
				
		}
		
	}


	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getPointsToAward(boolean, com.pixelandtag.web.beans.Subscriber)
	 */
	public static int getPointsToAward(boolean isCorrect, Subscriber sub, Connection conn) {
		
		int points_to_award = 0;
		
		if(isCorrect){
			
			try {
				points_to_award = Integer.valueOf(getSetting("correct_answer_points",conn));
			} catch (NumberFormatException e1) {
				log(e1);
			} catch (NoSettingException e1) {
				log(e1);
			}
			
		}else{
			
			try {
				
				points_to_award =  Integer.valueOf(getSetting("wrong_answer_points",conn));
			
			} catch (NumberFormatException e1) {
				
				log(e1);
			
			} catch (NoSettingException e1) {
				
				log(e1);
			
			}
		
		}
		
		if(isHappyHour(conn)){
			
			try {
				
				points_to_award = Integer.valueOf(getSetting("happy_hour_multiplier",conn)) * points_to_award;
				
			} catch (NumberFormatException e) {
				
				log(e);
			
			} catch (NoSettingException e) {
				
				log(e);
			
			}
			
		}
		
		return points_to_award;
	}


	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#updateShopBucket(java.lang.String)
	 */
	public static boolean updateShopBucket(String msisdn, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		PreparedStatement pstmt2 = null;
		
		ResultSet rs = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`shop_credits` WHERE msisdn=?",Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			int i = -1;
			
			if(!rs.next()){
				
				pstmt2 = conn.prepareStatement("INSERT INTO `"+DATABASE+"`.`shop_credits`(msisdn,points,timeStampEarned) VALUES(?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'))",Statement.RETURN_GENERATED_KEYS);
				
				pstmt2.setString(1, msisdn);
				pstmt2.setInt(2, SHOP_BUCKET_POINTS);
				i = pstmt2.executeUpdate();
				
				success = i>0;
				
			}else{
			

				pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`shop_credits` SET points=? WHERE msisdn=? timeStampEarned=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"')", Statement.RETURN_GENERATED_KEYS);
				
				pstmt.setInt(1, SHOP_BUCKET_POINTS);
				pstmt.setString(2, msisdn);
				i = pstmt.executeUpdate();
				
				success = i>0;
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt2!=null)
					pstmt2.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return success;
	}


	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#setAsAnswered(java.lang.String, com.pixelandtag.web.beans.Question)
	 */
	public static boolean setAsAnswered(String msisdn,Question lastUnansweredQuestionSentToSub, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`questions_sent` SET answered=1 WHERE msisdn=? AND questionIdFK=?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			pstmt.setInt(2, lastUnansweredQuestionSentToSub.getId());
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
						
		}
		
		return success;
		
	}


	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#isInBlacklist(java.lang.String)
	 */
	public static boolean isInBlacklist(String msisdn, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean isInBlacklist = false;
		
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`blacklist` WHERE msisdn=?",Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				isInBlacklist = true;
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return isInBlacklist;
	}


	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#blackistSubscriber(java.lang.String)
	 */
	public static boolean blackistSubscriber(String msisdn, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`blacklist` WHERE msisdn=?",Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			int i = -1;
			
			if(!rs.next()){
				
				pstmt = conn.prepareStatement("INSERT INTO `"+DATABASE+"`.`blacklist`(msisdn) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, msisdn);
				i = pstmt.executeUpdate();
				
				success = i>0;
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return success;
	}


	
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#updateLastAction(java.lang.String)
	 */
	public static boolean updateLastAction(String msisdn, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`subscriber_profile` SET last_action=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"') WHERE msisdn=? ", Statement.RETURN_GENERATED_KEYS);
			
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}

		}
		
		return success;
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#setTeased(java.lang.String, java.sql.Connection)
	 */
	public static boolean setTeased(String msisdn, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`subscriber_profile` SET last_teased=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"') WHERE msisdn=? ", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			

			
		}
		
		return success;
	}
	
	
	
	public static Map<Integer, Map<MessageType, String>> getTeasers(Connection conn) throws MessageNotSetException {
		
		Map<MessageType, String> teaserMap = null;
		Map<Integer, Map<MessageType, String>> teasers = new HashMap<Integer, Map<MessageType, String>>();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery("SELECT id from `"+DATABASE+"`.`languages`");
			
			while(rs.next()){
				teasers.put(rs.getInt(1), null);
			}
			
			rs.close();
			stmt.close();
			
		
			for(int language_idz : teasers.keySet()){
					
				
				teaserMap = new HashMap<MessageType,String>();
				
				teaserMap.put(MessageType.FIRST_TEASER, getMessage(MessageType.FIRST_TEASER, language_idz,conn));
				teaserMap.put(MessageType.OTHER_TEASER, getMessage(MessageType.OTHER_TEASER, language_idz,conn));
				teaserMap.put(MessageType.HIGH_TEASER, getMessage(MessageType.HIGH_TEASER, language_idz,conn));
				
				teasers.put(language_idz, teaserMap);
						
			}
			
		} catch (SQLException e) {
			
			log(e);
			
		} finally{
			
			try {
				if(rs!=null)
				rs.close();
			} catch (SQLException e) {
				log(e);
			}
			
			try {
				if(stmt!=null)
					stmt.close();
			} catch (SQLException e) {
				log(e);
			}
			
		}
		
		return teasers;
	}

	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#isHappyHour()
	 */
	public static boolean isHappyHour(Connection conn) {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean isHappyHour = false;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`happy_hour` where CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"') between `start` and `stop`",Statement.RETURN_GENERATED_KEYS);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				isHappyHour = true;
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return isHappyHour;
	}

	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getHappyHourEnd()
	 */
	public static String getHappyHourEnd(Connection conn) {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		String happy_hour_end = "";
		
		try{
			
			pstmt = conn.prepareStatement("SELECT DATE_FORMAT(`stop`,'%r') as 'happy_hour_end' FROM `"+DATABASE+"`.`happy_hour` where CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"') between `start` and `stop`",Statement.RETURN_GENERATED_KEYS);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				happy_hour_end = rs.getString(1);
				
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return happy_hour_end;
	}


	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#happyHourNotified(java.lang.String, boolean)
	 */
	public static boolean happyHourNotified(String msisdn, boolean ishappyHour, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`subscriber_profile` SET "+(ishappyHour ? "happy_hour_start_notified=1" : "happy_hour_stop_notified=1")+" WHERE msisdn=? ", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return success;
	}

	public static int getRegistration_points(Connection conn) throws NumberFormatException, NoSettingException {
		return Integer.valueOf(getSetting("registration_points",conn));
	}
	
	public static void loginTraffic(String msisdn, String requestText, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		
		try{
			
			pstmt = conn.prepareStatement("INSERT INTO `"+DATABASE+"`.`raw_in_log`(msisdn,requestText) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			pstmt.setString(2, requestText);
			
			pstmt.executeUpdate();
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		
	}
	
	
	
	public static void logResponse(String msisdn, String responseText, Connection conn){
		
		PreparedStatement ps = null;
		
		try {
			
			ps = conn.prepareStatement("INSERT INTO `"+DATABASE+"`.`raw_out_log`(msisdn,response) VALUES(?,?)");
			
			ps.setString(1, msisdn);
			
			ps.setString(2, responseText);
			
			ps.executeUpdate();
			
		} catch (SQLException e) {
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				
				if(ps != null)
					ps.close();
				
			} catch (SQLException e) {
				
				logger.error(e.getMessage(),e);
				
			}
		}
		
	}
	
	public static boolean isTherePendingMMS(String msisdn, Connection conn){
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean there_is_pending_mms = false;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT id FROM `"+DATABASE+"`.`mms_queue` WHERE `msisdn` = ? AND sent=0", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				there_is_pending_mms = true;
				
			}
			
		}catch(SQLException e){
			
			log(e);
		
		}finally{
			
			
			try {
				if(rs!=null)
					rs.close();
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			try {
				if(pstmt!=null)
					pstmt.close();
			} catch (SQLException e) {
				log(e);
			}
			
		}
		
		return there_is_pending_mms;
	}
	
	
	
	public static boolean hasReachedMMSSet(String msisdn, Connection conn){
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean hasReachedMMSset = false;
		
		try{
			
			//WORKED: pstmt = conn.prepareStatement("SELECT (count(*)%`setting`.`value`) as 'needs_mms' FROM `"+DATABASE+"`.`trivia_log` tl JOIN `"+DATABASE+"`.`settings` `setting` WHERE `tl`.`question_idFK` >-1 AND `tl`.`timeStamp` between timestamp(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY)) AND ((CURRENT_DATE + INTERVAL 1 DAY) - INTERVAL 1 SECOND) AND `setting`.`key`='questions_per_mms' AND `tl`.`msisdn` = ?", Statement.RETURN_GENERATED_KEYS);
			//CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"')
			pstmt = conn.prepareStatement("SELECT (count(*)%`setting`.`value`) as 'needs_mms' FROM `"+DATABASE+"`.`trivia_log` tl JOIN `"+DATABASE+"`.`settings` `setting` WHERE `tl`.`question_idFK` >-1 AND `tl`.`timeStamp` between timestamp(DATE_SUB(CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'), INTERVAL 0 DAY)) AND ((CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"') + INTERVAL 1 DAY) - INTERVAL 1 SECOND) AND `setting`.`key`='questions_per_mms' AND `tl`.`msisdn` = ?", Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				hasReachedMMSset = !rs.getBoolean(1);
				
			}
			
		}catch(SQLException e){
			
			log(e);
		
		}finally{
			
			
			try {
				if(rs!=null)
					rs.close();
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			try {
				if(pstmt!=null)
					pstmt.close();
			} catch (SQLException e) {
				log(e);
			}
			
		}
		
		return hasReachedMMSset;
		
	}
	
	
	
	public static boolean hasMaxedOnSet(int setId, String msisdn, Connection conn){
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean hasMaxed = false;
		
		try{
			
			
			if(setId==QUESTIONS_PER_MMS ){
				
				pstmt = conn.prepareStatement("SELECT IF((count(*)%`setting`.`value`)=0,true,false) as 'needs_mms' FROM `"+DATABASE+"`.`trivia_log` tl JOIN `"+DATABASE+"`.`settings` `setting` WHERE `tl`.`question_idFK` >-1 AND   `tl`.`answer` in ('A','B') AND `setting`.`key`=? AND `tl`.`msisdn` = ?", Statement.RETURN_GENERATED_KEYS);
				
				pstmt.setString(1, questions_per_mms);
			
			}else if(setId==DAILY_QUESTION_QUOTA){
				
				//Commented out to exclude date dependancy
				pstmt = conn.prepareStatement("SELECT IF((count(*)>=`setting`.`value`),true,false) as 'has_maxed_on_questions' FROM `"+DATABASE+"`.`trivia_log` tl JOIN `"+DATABASE+"`.`settings` `setting` WHERE `tl`.`question_idFK` >-1 AND `tl`.`answer` in ('A','B') AND `tl`.`timeStamp` between timestamp(DATE_SUB(CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"'), INTERVAL 0 DAY)) AND ((CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"') + INTERVAL 1 DAY) - INTERVAL 1 SECOND) AND `setting`.`key`=? AND `tl`.`msisdn` = ?", Statement.RETURN_GENERATED_KEYS);
				//pstmt = conn.prepareStatement("SELECT IF((count(*)>=`setting`.`value`),true,false) as 'has_maxed_on_questions' FROM `"+DATABASE+"`.`trivia_log` tl JOIN `"+DATABASE+"`.`settings` `setting` WHERE `tl`.`question_idFK` >-1 AND `setting`.`key`=? AND `tl`.`answer` in ('A','B') AND `tl`.`msisdn` = ?", Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, questions_quota_per_day);
			
			}else if(setId==MAX_MONEY_SPENT_PER_DAY){
				
				pstmt = conn.prepareStatement("SELECT IF((count(*)>=`setting`.`value`),true,false) as 'has_maxed_on_revenue' FROM `celcom`.`SMSStatLog` tl JOIN `"+DATABASE+"`.`settings` `setting` WHERE `tl`.`CMP_SKeyword` ='IOD0100' AND `tl`.`statusCode`='Success' AND `tl`.`timeStamp` between timestamp(DATE_SUB(CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"'), INTERVAL 0 DAY)) AND ((CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"') + INTERVAL 1 DAY) - INTERVAL 1 SECOND) AND `setting`.`key`=? AND `tl`.`msisdn` = ?", Statement.RETURN_GENERATED_KEYS);
				
				pstmt.setString(1, max_revenue_per_sub_daily);
				
			}
			
			
			pstmt.setString(2, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				hasMaxed = rs.getBoolean(1);
				
			}
			
		}catch(SQLException e){
			
			log(e);
		
		}finally{
			
			
			try {
				if(rs!=null)
					rs.close();
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			try {
				if(pstmt!=null)
					pstmt.close();
			} catch (SQLException e) {
				log(e);
			}
			
		}
		
		return hasMaxed;
		
	}
	
	
	
	public static int getTotalAnsweredQuestions(int range, String msisdn, Connection conn) {

		int total_questions_answered = 0;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		try{
			
			if(range==MechanicsS.IN_TOTAL){
				pstmt = conn.prepareStatement("SELECT count(*) FROM `"+DATABASE+"`.`trivia_log` WHERE `msisdn` = ? AND `question_idFK` >-1", Statement.RETURN_GENERATED_KEYS);
			}if(range==MechanicsS.TODAY){
				pstmt = conn.prepareStatement("SELECT count(*) FROM `"+DATABASE+"`.`trivia_log` WHERE `question_idFK` >-1 AND `timeStamp` between timestamp(DATE_SUB(CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"'), INTERVAL 0 DAY)) AND ((CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"') + INTERVAL 1 DAY) - INTERVAL 1 SECOND) AND `msisdn` = ? ", Statement.RETURN_GENERATED_KEYS);
			}
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				total_questions_answered = rs.getInt(1);
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}

			
		}
		
		return total_questions_answered;
	}
	
	
	
	
	
	public static int getTotalBoughtMMS(int range, String msisdn, Connection conn) {

		int total_questions_answered = 0;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		try{
			
			if(range==MechanicsS.IN_TOTAL){
				pstmt = conn.prepareStatement("SELECT count(*) FROM `"+DATABASE+"`.`mms_log` WHERE `msisdn` = ? AND `mms_id_fk` >-1", Statement.RETURN_GENERATED_KEYS);
			}if(range==MechanicsS.TODAY){
				pstmt = conn.prepareStatement("SELECT count(*) FROM `"+DATABASE+"`.`mms_log` WHERE `mms_id_fk` >-1 AND `timeSent` between timestamp(DATE_SUB(CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"'), INTERVAL 0 DAY)) AND ((CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"') + INTERVAL 1 DAY) - INTERVAL 1 SECOND) AND `msisdn` = ? ", Statement.RETURN_GENERATED_KEYS);
			}
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				total_questions_answered = rs.getInt(1);
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}

			
		}
		
		return total_questions_answered;
	}
	
	
	
	
	public static String getAnswerBySubscriber(String msisdn, int question_idFK,
			Connection conn) {
		
		String sub_answer = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT `answer` FROM `"+DATABASE+"`.`trivia_log` WHERE `msisdn` = ? AND `question_idFK` = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			pstmt.setInt(2, question_idFK);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				sub_answer = rs.getString(1);
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}

			
		}
		
		return sub_answer;
	}

	

	public static boolean toggleActieAndSubscribed(String msisdn, boolean active_subscribed,
			Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`subscriber_profile` SET `subscribed` = "+(active_subscribed ? "1" : "0")+", `active` = "+(active_subscribed ? "1" : "0")+" WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return success;
		
	}

	public static MMS getNextMMS(String serviceCode, Subscriber sub, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		MMS mms = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`mms` WHERE available=1 AND  `id` NOT IN (SELECT mms_id_fk from `axiata_trivia`.`mms_log` WHERE msisdn = ?) ORDER BY RAND() LIMIT 1", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, sub.getMsisdn());
			
			rs = pstmt.executeQuery();
			
			String mediaPath;
			if(rs.next()){
				
				mms = new MMS();
				mms.setMsisdn(sub.getMsisdn());
				mms.setId(rs.getString("id"));
				mms.setTransactionID(rs.getString("id"));
				mediaPath = rs.getString("mediaURL");
				mms.setMediaPath(mediaPath);//must first pass media url so that subject and text are picked
				mms.setServicecode(serviceCode);
				//String mms_subject = mediaPath.split(MMS.FILE_SEPARATOR_REGEX)[mediaPath.split(MMS.FILE_SEPARATOR_REGEX).length-1].split(MMS.UNDERSCORE)[0];
				//mms.setSubject(mms_subject);
				
				/*String mms_text = mediaPath.split(MMS.FILE_SEPARATOR_REGEX)[mediaPath.split(MMS.FILE_SEPARATOR_REGEX).length-1].split(MMS.UNDERSCORE)[1];
				
				
				
				if(mms_text.toLowerCase().endsWith(MMS.JPG))
					mms_text = mms_text.split(MMS.JPG)[0];
				if(mms_text.toLowerCase().endsWith(MMS.PNG))
					mms_text = mms_text.split(MMS.PNG)[0];
				if(mms_text.toLowerCase().endsWith(MMS.GIF))
					mms_text = mms_text.split(MMS.GIF)[0];*/
				
			
				
				//mms.setMms_text(mms_text);
				
				
				if(rs.getString("subject")!=null){
					logger.debug("setting the subject>>>>>>>>>>>>>>>>>>>>>>mms>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+rs.getString("subject"));
					mms.setSubject(rs.getString("subject"));
				}
				
				if(rs.getString("text")!=null){
					logger.debug("setting the mmstext>>>>>>>>>>>>>>>>>>>>>>mms>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+rs.getString("text"));
					mms.setMms_text(rs.getString("text"));
				}
				
				
				mms.setServiceid(TRIVIA_SERVICE_ID);//hard coding for trivia service ID
			
			
			}else{
				//There are no questions to send to this subscriber. Alert someone!
				logger.warn("There are no more mms for "+sub.getMsisdn());
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return mms;
	}
	
	
	
	
	
	public static List<MMS> getNextMMSs(String serviceCode, Subscriber sub, Connection conn, int number) {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		List<MMS> mmss = null;
		MMS mms = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`mms` WHERE available=1 AND  `id` NOT IN (SELECT mms_id_fk from `axiata_trivia`.`mms_log` WHERE msisdn = ?) ORDER BY RAND() LIMIT "+number, Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, sub.getMsisdn());
			
			rs = pstmt.executeQuery();
			
			String mediaPath;
			while(rs.next()){
				
				if(rs.isFirst()){
					
					mmss = new ArrayList<MMS>();
				
				}
				
				mms = new MMS();
				mms.setMsisdn(sub.getMsisdn());
				mms.setId(rs.getString("id"));
				mms.setTransactionID(rs.getString("id"));
				mediaPath = rs.getString("mediaURL");
				mms.setMediaPath(mediaPath);//must first pass media url so that subject and text are picked
				mms.setServicecode(serviceCode);
				
				if(rs.getString("subject")!=null){
					logger.debug("setting the subject>>>>>>>>>>>>>>>>>>>>>>mms for ["+sub.getMsisdn()+"]>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+rs.getString("subject"));
					mms.setSubject(rs.getString("subject"));
				}
				
				if(rs.getString("text")!=null){
					logger.debug("setting the mmstext>>>>>>>>>>>>>>>>>>>>>>mms for ["+sub.getMsisdn()+"]>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+rs.getString("text"));
					mms.setMms_text(rs.getString("text"));
				}
				mms.setServiceid(TRIVIA_SERVICE_ID);//hard coding for trivia service ID
				
				mmss.add(mms);
			
			
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return mmss;
	}
	

	public static boolean logMMSAsSent(MMS mms, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("INSERT INTO `"+DATABASE+"`.`mms_log`(mms_id_fk,msisdn) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, mms.getId());
			
			pstmt.setString(2, mms.getMsisdn());
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return success;
	
	}
	
	
	
	
	
	
	
	public static boolean addToDbWhereNotExist(MMS mms, Connection conn) {
		
		if(mms==null)
			return false;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean success = false;
		
		
		try {
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`mms` WHERE mediaURL = ?",Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, mms.getMedia_path());
			
			rs = pstmt.executeQuery();
			
			
			
			if(rs.next()){
				
				logger.debug("MMS FOUND: MediaURL = "+mms.getMedia_path());
			
			}else{
				
				logger.warn("MMS with mediaURL = "+mms.getMedia_path()+ " WAS NOT found! Now will add it to the db");
					
				pstmt = conn.prepareStatement("INSERT INTO `"+DATABASE+"`.`mms`(`mediaURL`,`subject`,`text`,`dateOfInsertion`) VALUES(?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'))",Statement.RETURN_GENERATED_KEYS);
				
				pstmt.setString(1, mms.getMedia_path());
				pstmt.setString(2, mms.getSubject());
				pstmt.setString(3, mms.getMms_text());
				
				pstmt.executeUpdate();
				
				logger.debug("mms added.");
			}
			
		} catch (SQLException e) {
			
			log(e);
		
		} finally{
			
			
			try {
				
				if(rs!=null)
					rs.close();
			
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				log(e);
			
			}
			
		}
		
		return success;

		
	}

	public static TriviaLogRecord getPrevRecordWithAnswerCol(Subscriber sub, String answer, Connection conn) {
		
		TriviaLogRecord tr = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`trivia_log` WHERE msisdn = ? AND answer= ? AND question_idFK = -1 LIMIT 1", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, sub.getMsisdn());
			pstmt.setString(2, answer);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				tr = new TriviaLogRecord();
				tr.setId(rs.getString("id"));
				tr.setMsisdn(rs.getString("msisdn"));
				tr.setName(rs.getString("name"));
				tr.setCorrect(rs.getInt("correct"));
				tr.setQuestion_idFK(rs.getInt("question_idFK"));
				tr.setAnswer(rs.getString("answer"));
				tr.setPoints(rs.getInt("points"));
				tr.setTimeStamp(rs.getString("timeStamp"));
				tr.setDirty(rs.getString("dirty"));
			
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return tr;
	}

	public static SMS getNextSMS(Subscriber sub, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		SMS sms = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DATABASE+"`.`static_sms` WHERE available=1 AND  `id` NOT IN (SELECT sms_id_fk from `"+DATABASE+"`.`static_sms_log` WHERE msisdn = ?) ORDER BY RAND() LIMIT 1", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, sub.getMsisdn());
			
			rs = pstmt.executeQuery();
			
			
			if(rs.next()){
				
				sms = new SMS();
				sms.setId(rs.getString("id"));
				sms.setText(rs.getString("text"));
				sms.setAvailable(rs.getString("available"));
				sms.setDateOfInsertion(rs.getString("dateOfInsertion"));
			
			}else{
				//There are no questions to send to this subscriber. Alert someone!
				logger.warn("There are no more sms for "+sub.getMsisdn());
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return sms;
	}

	public static boolean logSMSAsSent(Subscriber sub, SMS sms, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("INSERT INTO `"+DATABASE+"`.`static_sms_log`(sms_id_fk,msisdn) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, sms.getId());
			
			pstmt.setString(2, sub.getMsisdn());
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			
		}
		
		return success;
		
	}
	
	
	
	
	public int getHourNow(Connection conn) {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		int hour_now = -1;
		
		
		try{
					
			pstmt = conn.prepareStatement("SELECT HOUR(CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"'))", Statement.RETURN_GENERATED_KEYS);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				hour_now = rs.getInt(1);
			}
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}
			
		}
		
		return hour_now;
	}

	public static String getAnseringInstructions(int language_id_) {
		if(language_id_==1){
			return ENGLISH_ANSWERING_INSTRUCTIONS ;
		}else if(language_id_==2){
			return MALAY_ANSWERING_INSTRUCTIONS;
		}else{
			return MALAY_ANSWERING_INSTRUCTIONS;
		}
	}

	public static boolean setUnusable(int id, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DATABASE+"`.`questions` SET usable=0 WHERE id=? ", Statement.RETURN_GENERATED_KEYS);
			
			
			pstmt.setInt(1, id);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			MechanicsS.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				MechanicsS.log(e);
			
			}

		}
		
		return success;
		
	}
	
	
		


	public static void toStatsLog(MOSms mo, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		try {
			
				
			pstmt = conn.prepareStatement("INSERT INTO `celcom`.`SMSStatLog`(SMSServiceID,msisdn,transactionID, CMP_Keyword, CMP_SKeyword, price) " +
						"VALUES(?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setInt(1, mo.getServiceid());
			pstmt.setString(2, mo.getMsisdn());
			pstmt.setBigDecimal(3, new BigDecimal(mo.getCMP_Txid()));
			pstmt.setString(4, mo.getCMP_AKeyword());
			pstmt.setString(5, mo.getCMP_SKeyword());
			pstmt.setDouble(6, mo.getPrice().doubleValue());
			
			pstmt.executeUpdate();
			
			
		} catch (SQLException e) {
			
			log(e);
		
		} catch (Exception e) {
			
			log(e);
			
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				log(e);
				
			}
		}
		
	}

	
	/**
	 * 
	 * @param cmp_Txid
	 * @param conn
	 * @param setRetryLater
	 * @param isReTry
	 */
	public static void toggleRetry(String cmp_Txid, Connection conn, boolean setRetryLater, boolean isReTry) {
		
		PreparedStatement pstmt = null;
		
		try {
		
			pstmt = conn.prepareStatement("UPDATE `celcom`.`messagelog` SET re_try="+(setRetryLater ? "1" : "0") +" WHERE "+(isReTry ? "newCMP_Txid" : "CMP_Txid")+" = ?");
		
			pstmt.setString(1, cmp_Txid);
			
			pstmt.executeUpdate();
			
			
		} catch (SQLException e) {
			log(e);
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				log(e);
				
			}
		}
		
	}
	
	
	/**
	 * 
	 * @param cmp_Txid
	 * @param conn
	 * @param setRetryLater
	 * @param isReTry
	 */
	public static void toggleRetry(BigInteger cmp_Txid, boolean reTry, Connection conn) {
		
		
		
		PreparedStatement pstmt = null;
		
		try {
		
			pstmt = conn.prepareStatement("UPDATE `celcom`.`messagelog` SET re_try="+(reTry ? "1" : "0") +" WHERE CMP_Txid  = ?");
		
			pstmt.setBigDecimal(1, new BigDecimal(cmp_Txid));
			
			int rec = pstmt.executeUpdate();
			
			logger.debug(">>>>>>>>::::::::::::::>>>>>>>>RE_TRY_TO_BILL: We set the cmptxid ["+cmp_Txid+"] to be re-tried later.. records affected = "+rec);
			
			
		} catch (SQLException e) {
			log(e);
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				log(e);
				
			}
		}
		
	}
	
	
	
	/**
	 * Request by MJL to re-try billing after every 2 hours until 7pm...
	 * here goes...
	 * @param cmp_Txid
	 * @param conn
	 * @param b 
	 */
	public static void incrementRetryCount(String cmp_Txid, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		try {
		
			pstmt = conn.prepareStatement("UPDATE `celcom`.`messagelog` SET re_try_count=(re_try_count+1) WHERE CMP_Txid = ?");
		
			pstmt.setString(1, cmp_Txid);
			
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			log(e);
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				log(e);
				
			}
		}
		
	}
	
	public static boolean hasUnpaidDuesToday(String msisdn, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean hasArreas = false;
		
		try {
			
			pstmt = conn.prepareStatement("select * from celcom.SMSStatLog WHERE statusCode in ('PSANumberBarred','PSAInsufficientBalance') and price>0 AND timeStamp between timestamp(DATE_SUB(CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"'), INTERVAL 0 DAY)) AND ((CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"') + INTERVAL 1 DAY) - INTERVAL 1 SECOND) AND msisdn=? order by timeStamp desc LIMIT 1",Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				int arreasId = rs.getInt("id");
				logger.debug(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::SUBSCRIBER HAS NOT PAID FOR MMS.... SMStatLog id ================================================= "+arreasId+"================================================================");
				//TODO Try and re-bill at this point...
				hasArreas = true;
			}
			
			
		} catch (SQLException e) {
			
			log(e);
			
		}finally{
		
			
				try {
					if(pstmt!=null)	
						pstmt.close();
				} catch (SQLException e) {
					log(e);
				}
				
				try {
					if(rs!=null)	
						rs.close();
				} catch (SQLException e) {
					log(e);
				}
				
		}
		
		return hasArreas;
		
	}
	

	public static boolean hasUnpaidDuesToday(Subscriber sub, Connection conn) {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean hasArreas = false;
		
		try {
			
			pstmt = conn.prepareStatement("select * from celcom.SMSStatLog WHERE statusCode in ('PSANumberBarred','PSAInsufficientBalance') and price>0 AND timeStamp between timestamp(DATE_SUB(CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"'), INTERVAL 0 DAY)) AND ((CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"') + INTERVAL 1 DAY) - INTERVAL 1 SECOND) AND msisdn=? order by timeStamp desc LIMIT 1",Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, sub.getMsisdn());
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				int arreasId = rs.getInt("id");
				logger.debug(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::SUBSCRIBER HAS NOT PAID FOR MMS.... SMStatLog id ================================================= "+arreasId+"================================================================");
				//TODO Try and re-bill at this point...
				hasArreas = true;
			}
			
			
		} catch (SQLException e) {
			
			log(e);
			
		}finally{
		
			
				try {
					if(pstmt!=null)	
						pstmt.close();
				} catch (SQLException e) {
					log(e);
				}
				
				try {
					if(rs!=null)	
						rs.close();
				} catch (SQLException e) {
					log(e);
				}
				
		}
		
		return hasArreas;
	}

	/** 
	 * checks if the sub has got free content to be sent for
	 * @param sub
	 * @param conn
	 * @return
	 */
	public static boolean hasFreeContent(Subscriber sub, Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean hasFree = false;
		
		try {
			pstmt = conn.prepareStatement("SELECt free_mms>0 as `eligible` FROM `axiata_trivia`.`compensation` WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, sub.getMsisdn());
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				hasFree= (rs.getInt(1) > 0);
				
			}
			
		} catch (SQLException e) {
			log(e);
		}catch (Exception e) {
			log(e);
		}finally{
			
				try {
					if(rs!=null)
						rs.close();
				} catch (SQLException e) {
					log(e);
				}
				try {
					if(pstmt!=null)
						pstmt.close();
				} catch (SQLException e) {
					log(e);
				}
		}
		
		return hasFree;
	}

	
	/**
	 * 
	 * @param sub
	 * @param deduct boolean - whether to deduct or increase the number of free mms. true if you want to reduce free mms available, false if you want to increase
	 * @param conn
	 */
	public static void updateFree(Subscriber sub, boolean deduct, Connection conn) {
		PreparedStatement pstmt = null;
		
		try {
			pstmt = conn.prepareStatement("UPDATE `axiata_trivia`.`compensation` set free_mms=free_mms"+(deduct ? "-":"+")+"1, dateUsed = CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"') WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, sub.getMsisdn());
			
			int n = pstmt.executeUpdate();
			
			logger.debug(" updated free ? "+(n<0));
			
		} catch (SQLException e) {
			log(e);
		}catch (Exception e) {
			log(e);
		}finally{
			
			try {
				if(pstmt!=null)
					pstmt.close();
			} catch (SQLException e) {
				log(e);
			}
		
		}
		
	}
	
	
	
	public static String stackTraceToString(Throwable e) {
		
		String retValue = null;
		StringWriter sw = null;
		PrintWriter pw = null;
		
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			retValue = sw.toString();
		} finally {
			try {
				if(pw != null)  
					pw.close();
				if(sw != null)  
					sw.close();
			} catch (IOException ignore) {
				
			}
		}
		
		return retValue;
	}

	public static long getTimeSinceLastSuccessfulBillable(Connection conn,TimeUnit tu) {
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		long lastProfit = -1;
		String timeUnit= "SECOND";
		
		if(tu==TimeUnit.SECONDS)
			timeUnit= "SECOND";
		else if(tu==TimeUnit.MINUTES)
			timeUnit="MINUTE";
		
		String checkInCelcom_messageLog = "select (TIMESTAMPDIFF("+timeUnit+",delivery_report_arrive_time,CURRENT_TIMESTAMP)) as 'diffr' FROM celcom.messagelog  WHERE `timeStamp` between  timestamp(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY)) AND ((CURRENT_DATE + INTERVAL 1 DAY) - INTERVAL 1 SECOND) AND CMP_SKeyword='IOD0100' AND MT_STATUS='Success' order by delivery_report_arrive_time desc LIMIT 1";
		String checkInCelcom_SMSStatLog = "select (TIMESTAMPDIFF("+timeUnit+",timeStamp,CURRENT_TIMESTAMP)) as 'diffr' FROM celcom.SMSStatLog WHERE `timeStamp` between  timestamp(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY)) AND ((CURRENT_DATE + INTERVAL 1 DAY) - INTERVAL 1 SECOND) AND CMP_SKeyword='IOD0100' AND statusCode='Success' order by timeStamp desc LIMIT 1";
		
		try{
			
			pstmt = conn.prepareStatement(checkInCelcom_SMSStatLog);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				lastProfit = rs.getLong(1);
			}
			
		}catch(Exception e){
			log(e);
		}finally{
			
			try{
				if(rs!=null){
					rs.close();
				}
			}catch(Exception e){}
			
			try{
				if(pstmt!=null){
					pstmt.close();
				}
			}catch(Exception e){}
			
			
		}
		
		return lastProfit;
		
	}

	public static long getTimeLastMMSSentOut(Connection conn, TimeUnit tu) {
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		long lastProfit = -1;
		String timeUnit= "SECOND";
		
		if(tu==TimeUnit.SECONDS)
			timeUnit= "SECOND";
		else if(tu==TimeUnit.MINUTES)
			timeUnit="MINUTE";
		
		String check = "select (TIMESTAMPDIFF("+timeUnit+",timeStampOfInsertion,CURRENT_TIMESTAMP)) as 'diffr' FROM celcom.mms_log  order by timeStampOfInsertion desc limit 1";// = "select (TIMESTAMPDIFF("+timeUnit+",timeStamp,CURRENT_TIMESTAMP)) as 'diffr' FROM celcom.SMSStatLog WHERE `timeStamp` between  timestamp(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY)) AND ((CURRENT_DATE + INTERVAL 1 DAY) - INTERVAL 1 SECOND) AND CMP_SKeyword='IOD0100' AND statusCode='Success' order by timeStamp desc LIMIT 1";
		
		try{
			
			pstmt = conn.prepareStatement(check);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				lastProfit = rs.getLong(1);
			}
			
		}catch(Exception e){
			log(e);
		}finally{
			
			try{
				if(rs!=null){
					rs.close();
				}
			}catch(Exception e){}
			
			try{
				if(pstmt!=null){
					pstmt.close();
				}
			}catch(Exception e){}
			
			
			
		}
		
		return lastProfit;
	}

	public static int getMMSQueue(Connection conn) {
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int lastProfit = 0;
		
		
		String sql = "select count(*) c from celcom.mms_to_send where sent = 0 AND inProcessingQueue=0  AND paidFor=1 and (TIMESTAMPDIFF(HOUR,timeStampOfInsertion,CURRENT_TIMESTAMP)<=24)";
		
		try{
			
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				lastProfit = rs.getInt(1);
			}
			
		}catch(Exception e){
			log(e);
		}finally{
			
			try{
				if(rs!=null){
					rs.close();
				}
			}catch(Exception e){}
			
			try{
				if(pstmt!=null){
					pstmt.close();
				}
			}catch(Exception e){}
			
		}
		
		return lastProfit;
		
		
	}



	
	
}
