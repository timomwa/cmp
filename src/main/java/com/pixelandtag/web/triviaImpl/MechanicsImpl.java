package com.pixelandtag.web.triviaImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.pixelandtag.axiata.teasers.producer.BroadcastApp;
import com.pixelandtag.exceptions.MessageNotSetException;
import com.pixelandtag.exceptions.NoSettingException;
import com.pixelandtag.mms.api.TarrifCode;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.web.beans.Answer;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.beans.Question;
import com.pixelandtag.web.beans.Subscriber;
import com.pixelandtag.web.beans.TriviaLogRecord;
import com.pixelandtag.web.triviaI.MechanicsI;

/**
 * @author Timothy Mwangi Gikonyo
 *
 */
public class MechanicsImpl implements MechanicsI {
	
	public static final String INMOBIA = "IIIIIIIIIIIIIIIIIII";
	private String type;
	private Connection conn_obj;
	private Logger logger = Logger.getLogger(MechanicsImpl.class);
	private String smppid;
	private String sendfrom;
	private String price;
	private int default_language_id;
	private int correct_answer_points;
	private int wrong_answer_points;
	private int registration_points;
	private String first_teaser,high_teaser,other_teaser;
	private boolean isHappyHour = false;
	private DataSource ds;
	private boolean usingDS = false;
	
	
	
	private MechanicsImpl(){
		
	}
	
	
	public void setHappyHour(boolean isHappyHour) {
		this.isHappyHour = isHappyHour;
	}
	
	private boolean isThisHappyHour(){
		return this.isHappyHour;
	}


	public String getFirst_teaser() {
		return first_teaser;
	}


	public void setFirst_teaser(String first_teaser) {
		this.first_teaser = first_teaser;
	}


	public String getHigh_teaser() {
		return high_teaser;
	}


	public void setHigh_teaser(String high_teaser) {
		this.high_teaser = high_teaser;
	}


	public String getOther_teaser() {
		return other_teaser;
	}


	public void setOther_teaser(String other_teaser) {
		this.other_teaser = other_teaser;
	}


	/**
	 * 
	 * @param conn_
	 * @throws NoSettingException 
	 */
	public MechanicsImpl(Connection conn_) throws NoSettingException {
		
		usingDS  = false;
		
		if(conn_==null)
			throw new NoSettingException("NO CONNECTION OBJ PASSED"); 
		this.conn_obj = conn_;
		
		initialize();
		
	}
	
	
	public DataSource getDs() {
		return ds;
	}


	public void setDs(DataSource ds) {
		this.ds = ds;
	}


	
	
	public MechanicsImpl(DataSource ds) throws NoSettingException, SQLException  {
		
		this.ds = ds;
		
		usingDS = true;
		setUsingDS(true);
		
		if(ds==null)
			throw new NoSettingException("NO CONNECTION OBJ PASSED"); 
		initialize();
	}


	private void initialize() throws NoSettingException {

		try {
			//All these must be set in the settings table!
			smppid = getSetting("smppid");
			sendfrom = getSetting("sendfrom");
			price = getSetting("price");
			type = getSetting("type");
			default_language_id = Integer.valueOf(getSetting("default_language_id"));
			correct_answer_points = Integer.valueOf(getSetting("correct_answer_points"));
			wrong_answer_points = Integer.valueOf(getSetting("wrong_answer_points"));
			registration_points = Integer.valueOf(getSetting("registration_points"));
			
		} catch (NoSettingException up) {
			throw up;
		}
		
	}


	public int getRegistration_points() {
		return registration_points;
	}


	public void setRegistration_points(int registration_points) {
		this.registration_points = registration_points;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	private void log(Exception e){
		logger.error(e.getMessage(),e);
	}

	public int getCorrect_answer_points() {
		return correct_answer_points;
	}


	public void setCorrect_answer_points(int correct_answer_points) {
		this.correct_answer_points = correct_answer_points;
	}


	public int getWrong_answer_points() {
		return wrong_answer_points;
	}


	public void setWrong_answer_points(int wrong_answer_points) {
		this.wrong_answer_points = wrong_answer_points;
	}


	/**
	 * Depending on whether you are using a connection object or
	 * a connection passed to this class via the constructor,
	 * you get a connection obj returned from the field variable
	 * or from a datasource connection pool
	 * @return java.sql.Connection
	 */
	public Connection getConn() {
		
		if(isUsingDS()){
		
			try {
				
				return this.ds.getConnection();
			
			} catch (SQLException e) {
				
				log(e);
				
				return null;
			
			}
		
		}else{
			
			return conn_obj;
			
		}
		
	}

	public void setConn(Connection conn) {
		this.conn_obj = conn;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public String getSmppid() {
		return smppid;
	}

	public void setSmppid(String smppid) {
		this.smppid = smppid;
	}

	public String getSendfrom() {
		return sendfrom;
	}

	public void setSendfrom(String sendfrom) {
		this.sendfrom = sendfrom;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String _price) {
		this.price = _price;
	}

	public int getDefault_language_id() {
		return default_language_id;
	}

	public void setDefault_language_id(int default_language_id) {
		this.default_language_id = default_language_id;
	}

	
	public synchronized boolean isUsingDS() {
		return usingDS;
	}


	public synchronized void setUsingDS(boolean usingDS) {
		this.usingDS = usingDS;
	}


	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#registerSubscriber(java.lang.String)
	 */
	public boolean registerSubscriber(String msisdn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("INSERT INTO `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile`(msisdn) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				pstmt = getConn().prepareStatement("INSERT INTO `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile`(msisdn) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
			}
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(isUsingDS()){
					if(conn!=null)
					conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
		}
		
		return success;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#unregisterSubscriber(java.lang.String)
	 */
	public boolean toggleSubActive(String msisdn, boolean active) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` SET active = "+(active ? 1 : 0)+" WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` SET active = "+(active ? 1 : 0)+" WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
		}
		
		return success;
	}
	
	
	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#registerSubscriber(java.lang.String, java.lang.String)
	 */
	public boolean registerSubscriber(String msisdn, String name) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("INSERT INTO `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile`(msisdn,name) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				pstmt = getConn().prepareStatement("INSERT INTO `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile`(msisdn,name) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, msisdn);
			pstmt.setString(2, name);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
		}
		
		return success;
	
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#registerSubscriber(java.lang.String, java.lang.String, int)
	 */
	public boolean registerSubscriber(String msisdn, String name, int languageId) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
			
				pstmt = conn.prepareStatement("INSERT INTO `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile`(msisdn,name,language_id) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("INSERT INTO `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile`(msisdn,name,language_id) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, msisdn);
			pstmt.setString(2, name);
			pstmt.setInt(3, languageId);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return success;
	
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#changeSubscriberName(java.lang.String, java.lang.String)
	 */
	public boolean changeSubscriberName(String msisdn, String name) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` SET name = ? WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` SET name = ? WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, name);
			pstmt.setString(2, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return success;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getLanguageID(java.lang.String)
	 */
	public int getLanguageID(String language_) {
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int languageid = 1;
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT id FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`languages` WHERE name = ?", Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("SELECT id FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`languages` WHERE name = ?", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, language_);
			
			rs = pstmt.executeQuery();
			
			if(rs.next())
				languageid = rs.getInt(1);
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return languageid;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#setLanguage(java.lang.String, int)
	 */
	public boolean setLanguage(String msisdn, int languageid) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` SET language_id=? WHERE msisdn=?", Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` SET language_id=? WHERE msisdn=?", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setInt(1, languageid);
			
			pstmt.setString(2, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return success;
	}
	

	private String generateNextTxId(){
		
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String timestamp = String.valueOf(System.currentTimeMillis());
		
		return INMOBIA.substring(0, (19-timestamp.length())) + timestamp;//(String.valueOf(Long.MAX_VALUE).length()-timestamp.length())) + timestamp;
		
	}	
		
	
	
	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#insertIntoVasSMPPToSend(java.lang.String, java.lang.String)
	 */
	public boolean insertIntoHTTPToSend(String msisdn, String message, BigInteger txId) {
		
		
			/*if(mo.getMt_Sent().trim().startsWith(RM1)){
				mo.setCMP_SKeyword(TarrifCode.RM1.getCode());//1 ringgit
			}else if(mo.getMt_Sent().trim().startsWith(RM0)){
				mo.setCMP_SKeyword(TarrifCode.RM0.getCode());//1 ringgit
			}else if(!mo.getMt_Sent().trim().startsWith(RM1.trim()) || !mo.getMt_Sent().trim().startsWith(RM0.trim())){
				mo.setMt_Sent(RM0+mo.getMt_Sent());
			}*/
			PreparedStatement pstmt = null;
			//TODO introduce sort of semaphore here if this method will be accessed by multiple threads..
			boolean success = false;
			
			try {
				
				
					pstmt = getConn().prepareStatement("insert into `celcom`.`httptosend`" +
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
				pstmt.setBigDecimal(9, new BigDecimal(txId));
				
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
	
	

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#insertIntoVasSMPPToSend(java.lang.String, java.lang.String)
	 */
	public boolean insertIntoHTTPToSend(String msisdn, String message) {
		
		
		/*if(mo.getMt_Sent().trim().startsWith(RM1)){
			mo.setCMP_SKeyword(TarrifCode.RM1.getCode());//1 ringgit
		}else if(mo.getMt_Sent().trim().startsWith(RM0)){
			mo.setCMP_SKeyword(TarrifCode.RM0.getCode());//1 ringgit
		}else if(!mo.getMt_Sent().trim().startsWith(RM1.trim()) || !mo.getMt_Sent().trim().startsWith(RM0.trim())){
			mo.setMt_Sent(RM0+mo.getMt_Sent());
		}*/
		PreparedStatement pstmt = null;
		//TODO introduce sort of semaphore here if this method will be accessed by multiple threads..
		boolean success = false;
		
		try {
			
			
				pstmt = getConn().prepareStatement("insert into `celcom`.`httptosend`" +
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
			pstmt.setString(9, generateNextTxId());
			
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
	public Question getFirstQuestion(String msisdn){
		return getNextQuestion(msisdn);
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getNextQuestion(java.lang.String)
	 */
	public Question getNextQuestion(String msisdn) {
		
		Question question = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		Answer answer = null;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn  = getConn();
				
				pstmt = conn.prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions` WHERE id NOT IN (SELECT question_idFK from `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`trivia_log` WHERE msisdn = ? AND question_idFK>-1) ORDER BY RAND() LIMIT 1", Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions` WHERE id NOT IN (SELECT question_idFK from `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`trivia_log` WHERE msisdn = ? AND question_idFK>-1) ORDER BY RAND() LIMIT 1", Statement.RETURN_GENERATED_KEYS);
					
			}
			
			pstmt.setString(1, msisdn);
			
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
				//There are no questions to send to this subscriber. Alert someone!
				logger.warn("There are no more questions for "+msisdn);
			}
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return question;
	}

	
	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getNextQuestionid(java.lang.String)
	 */
	@Deprecated
	public int getNextQuestionid(String msisdn) {
		
		int nextQuestion_id = -1;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT id FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions` WHERE id NOT IN (SELECT questionIdFK FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions_sent` WHERE msisdn = ?) ORDER BY RAND()", Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				pstmt = getConn().prepareStatement("SELECT id FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions` WHERE id NOT IN (SELECT questionIdFK FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions_sent` WHERE msisdn = ?) ORDER BY RAND()", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
					
				nextQuestion_id = rs.getInt("id");
				
			}
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return nextQuestion_id;
		
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getMessage(com.pixelandtag.web.beans.MessageType, int)
	 */
	public String getMessage(MessageType key,int languageID) throws MessageNotSetException{
		
		if(languageID==-1)//IF its a new response who we don't know the language, then we use the set default language - its usually id 1.
			languageID = this.default_language_id;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		String message = "Error: MESSAGE with KEY \""+key.toString()+"\" not set in db. language id = "+languageID;
		
		Connection conn = null;
		
	
		
		try{
			
			if(isUsingDS()){	
			
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT message FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`message` WHERE `key` = ? AND language_id = ? ORDER BY RAND() LIMIT 1", Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("SELECT message FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`message` WHERE `key` = ? AND language_id = ? ORDER BY RAND() LIMIT 1", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, key.toString());
			
			pstmt.setInt(2, languageID);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				message = rs.getString(1);
				
			}else{
				
				throw new MessageNotSetException(message);
			
			}
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
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
	public String perSonalizeMessage(String message, Subscriber sub, int pointsAwarded) {
		
		if(message.indexOf("<NAME>")>-1){
			
			message = message.replace("<NAME>", sub.getName());
			
		}
			
		if(message.indexOf("<TOTAL_POINTS>")>-1){
			
			String totalPoints = sub.getTotalPoints()>0 ? String.valueOf(sub.getTotalPoints()) : getTotalPoints(sub.getMsisdn());
			message = message.replace("<TOTAL_POINTS>", totalPoints);
		}
		
		if(message.indexOf("<WEEKLY_POINTS>")>-1){
			
			String totalPoints = getTotalPoints(sub.getMsisdn());
			message = message.replace("<WEEKLY_POINTS>", String.valueOf(totalPoints));
		}
		
		if(message.indexOf("<POINTS>")>-1 && pointsAwarded>0){
			
			message = message.replace("<POINTS>", String.valueOf(pointsAwarded));
		
		}if(message.indexOf("<HAPPY_HOUR_MULTIPLIER>")>-1){
			
			
			try {
				
				message = message.replace("<HAPPY_HOUR_MULTIPLIER>", getSetting("happy_hour_multiplier"));
			
			} catch (NoSettingException e) {
				
				message = message.replace("<HAPPY_HOUR_MULTIPLIER>", "10");
				
				log(e);
			
			}
		
		}if(message.indexOf("<HAPPY_HOUR_END>")>-1){
			
			message = message.replace("<HAPPY_HOUR_END>", getHappyHourEnd());
			
		}
		
		message += message.endsWith(".") ? "" : ".";//If the editor did not add a full stop to the resp msgs
		
		return message;
	
	}

	

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getSetting(java.lang.String)
	 */
	public String getSetting(String key) throws NoSettingException {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		String setting = "ERROR: No setting with key \""+key+"\" is set in db.";
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT value FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`settings` WHERE `key` = ?", Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				pstmt = getConn().prepareStatement("SELECT value FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`settings` WHERE `key` = ?", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, key);
			
			rs = pstmt.executeQuery();
			
			if(rs.next())
				setting = rs.getString(1);
			else
				throw new NoSettingException(setting);
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return setting;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#calculateTotalPoints(java.lang.String)
	 */
	public int calculateTotalPoints(String msisdn) {
		
		int points = 0;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
			
				pstmt = conn.prepareStatement("SELECT sum(points) FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`trivia_log` WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				pstmt = getConn().prepareStatement("SELECT sum(points) FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`trivia_log` WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				points = rs.getInt(1);
			}
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return points;
	}
	
	
	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getTotalPoints(java.lang.String)
	 */
	public String getTotalPoints(String msisdn) {
		
		String points = "0";
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT sum(points) FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`trivia_log` WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("SELECT sum(points) FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`trivia_log` WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				points = rs.getString(1);
			}
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
		}
		
		return points;
	}
	

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getSubscriber(java.lang.String)
	 */
	public Subscriber getSubscriber(String msisdn) {
		
		Subscriber sub = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` WHERE msisdn = ?", Statement.RETURN_GENERATED_KEYS);
			
			}
			
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
				sub.setActive(rs.getBoolean("active"));
			}
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return sub;
	}
	
	

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getCorrectAnswer(int)
	 */
	public Answer getCorrectAnswer(int questionId) {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		Answer answer = null;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				conn = getConn();
				pstmt = conn.prepareStatement("SELECT answer FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions` WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
			}else{
				pstmt = getConn().prepareStatement("SELECT answer FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions` WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
			}
			
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
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return answer;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getLastUnansweredQuestionSentToSub(java.lang.String)
	 */
	public Question getLastUnansweredQuestionSentToSub(String msisdn) {
		
		Question question = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT *  FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions` WHERE id = (SELECT questionIdFK FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions_sent` qs WHERE qs.msisdn = ? and qs.answered = 0)", Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				pstmt = getConn().prepareStatement("SELECT *  FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions` WHERE id = (SELECT questionIdFK FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions_sent` qs WHERE qs.msisdn = ? and qs.answered = 0)", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			//logger.info("::::::::::::::::::::::::::::::::::::::::::::::::\n SELECT *  FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions` WHERE id = (SELECT questionIdFK FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions_sent` qs WHERE qs.msisdn = '"+msisdn+"' and qs.answered = 0) \n :::::::::::::::::::::::::::::::::::::::::::::\n");
			
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
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return question;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#updateQuestionSent(java.lang.String, int)
	 */
	public boolean logAsSentButNotAnswered(String msisdn, int questionId) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				conn = getConn();
				pstmt = conn.prepareStatement("INSERT INTO `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions_sent`(msisdn,questionIdFK) VALUES(?,?) ON DUPLICATE KEY UPDATE `questionIdFK` = ?, `answered`=0", Statement.RETURN_GENERATED_KEYS);
			}else{
				pstmt = getConn().prepareStatement("INSERT INTO `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions_sent`(msisdn,questionIdFK) VALUES(?,?) ON DUPLICATE KEY UPDATE `questionIdFK` = ?, `answered`=0", Statement.RETURN_GENERATED_KEYS);
			}
			
			pstmt.setString(1, msisdn);
			
			pstmt.setInt(2, questionId);
			
			pstmt.setInt(3, questionId);
			
			/*pstmt.setString(3, msisdn);
			
			pstmt.setInt(4, questionId);
			*/
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
			
		}
		
		return success;
	}
	
	
	

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getQuestion(int)
	 */
	public Question getQuestion(int questionId) {
		
		Question question = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		Answer answer = null;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				conn = getConn();
				pstmt = conn.prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions` WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
			}else{
				pstmt = getConn().prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions` WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
			}
			
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
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					
					if(conn!=null)
						conn.close();
					
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return question;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#logPlay(com.pixelandtag.web.beans.TriviaLogRecord)
	 */
	public boolean logPlay(TriviaLogRecord triviaLog) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				conn = getConn();
				pstmt = conn.prepareStatement("INSERT INTO `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`trivia_log`(msisdn,name,correct,question_idFK,answer,points) VALUES(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
				//"ON DUPLICATE KEY UPDATE `msisdn`=?, `name`=?, `correct`=?, `question_idFK`=?, `answer`=?, `points`=?", Statement.RETURN_GENERATED_KEYS);
			}else{
				pstmt = getConn().prepareStatement("INSERT INTO `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`trivia_log`(msisdn,name,correct,question_idFK,answer,points) VALUES(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
				//"ON DUPLICATE KEY UPDATE `msisdn`=?, `name`=?, `correct`=?, `question_idFK`=?, `answer`=?, `points`=?", Statement.RETURN_GENERATED_KEYS);
				
			}
			
			int n = 0;
			pstmt.setString(++n, triviaLog.getMsisdn());
			pstmt.setString(++n, triviaLog.getName());
			pstmt.setInt(++n, triviaLog.isCorrect());
			pstmt.setInt(++n, triviaLog.getQuestion_idFK());
			pstmt.setString(++n, triviaLog.getAnswer());
			pstmt.setInt(++n, triviaLog.getPoints());
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
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return success;
		
		
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#addPoint(java.lang.String, int)
	 */
	public boolean addPoint(String msisdn, int points) {
		
		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				conn = getConn();
				pstmt = conn.prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`points` WHERE msisdn=?",Statement.RETURN_GENERATED_KEYS);
			}else{
				pstmt = getConn().prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`points` WHERE msisdn=?",Statement.RETURN_GENERATED_KEYS);
			}
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			int i = -1;
			
			if(!rs.next()){
				
				pstmt2 = getConn().prepareStatement("INSERT INTO `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`points`(msisdn,points) VALUES(?,?)",Statement.RETURN_GENERATED_KEYS);
				pstmt2.setString(1, msisdn);
				pstmt2.setInt(2, points);
				i = pstmt2.executeUpdate();
				
				success = i>0;
				
			}else{
			
				pstmt = getConn().prepareStatement("UPDATE  `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`points` SET points=? WHERE msisdn=?", Statement.RETURN_GENERATED_KEYS);
				
				pstmt.setInt(1, points);
				pstmt.setString(2, msisdn);
				i = pstmt.executeUpdate();
				
				success = i>0;
			}
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt2!=null)
					pstmt2.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return success;
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#closeConnection()
	 */
	public void closeConnection() throws SQLException {
		
		if(conn_obj!=null)
			if(!conn_obj.isClosed())
				conn_obj.close();
		
	}

	//@Override
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#validateAnswer(java.lang.String, com.pixelandtag.web.beans.Subscriber)
	 */
	public boolean validateAnswer(String answer, Subscriber sub) {
		
		Question question  = getLastUnansweredQuestionSentToSub(sub.getMsisdn());
		
		logger.info("Question's answer: "+question.getAnswer().toString());
		
		if(question.getAnswer().toString().equals(answer))
			return true;
		else
			return false;
		
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getQuestionToSend(com.pixelandtag.web.beans.Subscriber)
	 */
	@Deprecated
	public Question getQuestionToSend(Subscriber sub){
		
		int nextQuestionId = getNextQuestionid(sub.getMsisdn());
		
		if(nextQuestionId==-1){
			//There are no questions to send to this subscriber. Alert someone!
			logger.warn("There are no more questions for "+sub.getMsisdn());
			return null;
		
		}else{
				
			Question q = getNextQuestion(sub.getMsisdn());
			
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
	public int getPointsToAward(boolean isCorrect, Subscriber sub) {
		
		int points_to_award = 0;
		
		if(isCorrect)
			points_to_award = getCorrect_answer_points();
		else
			points_to_award =  getWrong_answer_points();
		
		if(isThisHappyHour()){
			
			try {
				
				points_to_award = Integer.valueOf(getSetting("happy_hour_multiplier")) * points_to_award;
				
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
	public boolean updateShopBucket(String msisdn) {
		
		PreparedStatement pstmt = null;
		
		PreparedStatement pstmt2 = null;
		
		ResultSet rs = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`shop_credits` WHERE msisdn=?",Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`shop_credits` WHERE msisdn=?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			int i = -1;
			
			if(!rs.next()){
				
				if(isUsingDS()){
					
					pstmt2 = conn.prepareStatement("INSERT INTO `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`shop_credits`(msisdn,points) VALUES(?,?)",Statement.RETURN_GENERATED_KEYS);
				
				}else{
					
					pstmt2 = getConn().prepareStatement("INSERT INTO `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`shop_credits`(msisdn,points) VALUES(?,?)",Statement.RETURN_GENERATED_KEYS);
				
				}
				
				pstmt2.setString(1, msisdn);
				pstmt2.setInt(2, SHOP_BUCKET_POINTS);
				i = pstmt2.executeUpdate();
				
				success = i>0;
				
			}else{
			

				if(isUsingDS()){
					
					pstmt = conn.prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`shop_credits` SET points=? WHERE msisdn=?", Statement.RETURN_GENERATED_KEYS);
				
				}else{
					
					pstmt = getConn().prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`shop_credits` SET points=? WHERE msisdn=?", Statement.RETURN_GENERATED_KEYS);
				
				}
				pstmt.setInt(1, SHOP_BUCKET_POINTS);
				pstmt.setString(2, msisdn);
				i = pstmt.executeUpdate();
				
				success = i>0;
			}
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt2!=null)
					pstmt2.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return success;
	}


	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#setAsAnswered(java.lang.String, com.pixelandtag.web.beans.Question)
	 */
	public boolean setAsAnswered(String msisdn,Question lastUnansweredQuestionSentToSub) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				conn = getConn();
				pstmt = conn.prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions_sent` SET answered=1 WHERE msisdn=? AND questionIdFK=?", Statement.RETURN_GENERATED_KEYS);
			}else{
				pstmt = getConn().prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`questions_sent` SET answered=1 WHERE msisdn=? AND questionIdFK=?", Statement.RETURN_GENERATED_KEYS);
			}
			
			pstmt.setString(1, msisdn);
			pstmt.setInt(2, lastUnansweredQuestionSentToSub.getId());
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return success;
		
	}


	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#isInBlacklist(java.lang.String)
	 */
	public boolean isInBlacklist(String msisdn) {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean isInBlacklist = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`blacklist` WHERE msisdn=?",Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`blacklist` WHERE msisdn=?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				isInBlacklist = true;
			}
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
			
		}
		
		return isInBlacklist;
	}


	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#blackistSubscriber(java.lang.String)
	 */
	public boolean blackistSubscriber(String msisdn) {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`blacklist` WHERE msisdn=?",Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`blacklist` WHERE msisdn=?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			int i = -1;
			
			if(!rs.next()){
				
				pstmt = getConn().prepareStatement("INSERT INTO `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`blacklist`(msisdn) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, msisdn);
				i = pstmt.executeUpdate();
				
				success = i>0;
			}
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
			
		}
		
		return success;
	}


	
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#updateLastAction(java.lang.String)
	 */
	public boolean updateLastAction(String msisdn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` SET last_action=CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"') WHERE msisdn=? ", Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` SET last_action=CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"') WHERE msisdn=? ", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return success;
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#setTeased(java.lang.String, java.sql.Connection)
	 */
	public boolean setTeased(String msisdn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` SET last_teased=CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"') WHERE msisdn=? ", Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				pstmt = getConn().prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` SET last_teased=CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"') WHERE msisdn=? ", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return success;
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#setTeased(java.lang.String, java.sql.Connection)
	 */
	public boolean setTeasedAndAutoSubscribed(String msisdn) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` SET last_teaser_id=1, last_teased=CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"') WHERE msisdn=? ", Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				pstmt = getConn().prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` SET last_teaser_id=1, last_teased=CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"') WHERE msisdn=? ", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return success;
	}
	
	
	
	public Map<Integer, Map<MessageType, String>> getTeasers() throws MessageNotSetException {
		
		Map<MessageType, String> teaserMap = null;
		Map<Integer, Map<MessageType, String>> teasers = new HashMap<Integer, Map<MessageType, String>>();
		
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		
		try {
			
			if(isUsingDS()){
				
				conn = getConn();
				
				stmt = conn.createStatement();
				
			}else{
				
				stmt = getConn().createStatement();
			
			}
			
			//rs = stmt.executeQuery("SELECT id from `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`languages`");
			
			//while(rs.next()){
				//teasers.put(rs.getInt(1), null);
			//}
			teasers.put(1, null);//english
			teasers.put(2, null);//malay
			
			//rs.close();
			//stmt.close();
			
		
			for(int language_idz : teasers.keySet()){
					
				
				teaserMap = new HashMap<MessageType,String>();
				
				teaserMap.put(MessageType.SECOND_HOUR_TEASER, getMessage(MessageType.SECOND_HOUR_TEASER, language_idz));
				teaserMap.put(MessageType.FOURTH_HOUR_TEASER, getMessage(MessageType.FOURTH_HOUR_TEASER, language_idz));
				teaserMap.put(MessageType.SIXTH_HOUR_TEASER, getMessage(MessageType.SIXTH_HOUR_TEASER, language_idz));
				
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
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return teasers;
	}

	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#isHappyHour()
	 */
	public boolean isHappyHour() {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean isHappyHour = false;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`happy_hour` where CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"') between `start` and `stop`",Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				pstmt = getConn().prepareStatement("SELECT * FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`happy_hour` where CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"') between `start` and `stop`",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				isHappyHour = true;
			}
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return isHappyHour;
	}

	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#getHappyHourEnd()
	 */
	public String getHappyHourEnd() {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		String happy_hour_end = "";
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT DATE_FORMAT(`stop`,'%r') as 'happy_hour_end' FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`happy_hour` where CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"') between `start` and `stop`",Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("SELECT DATE_FORMAT(`stop`,'%r') as 'happy_hour_end' FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`happy_hour` where CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"') between `start` and `stop`",Statement.RETURN_GENERATED_KEYS);
				
			}
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				happy_hour_end = rs.getString(1);
				
			}
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return happy_hour_end;
	}


	/* (non-Javadoc)
	 * @see com.pixelandtag.web.triviaI.MechanicsI#happyHourNotified(java.lang.String, boolean)
	 */
	public boolean happyHourNotified(String msisdn, boolean ishappyHour) {
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		Connection conn = null;
		
		try{
			
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` SET "+(ishappyHour ? "happy_hour_start_notified=1" : "happy_hour_stop_notified=1")+" WHERE msisdn=? ", Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("UPDATE `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` SET "+(ishappyHour ? "happy_hour_start_notified=1" : "happy_hour_stop_notified=1")+" WHERE msisdn=? ", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, msisdn);
			
			int i = pstmt.executeUpdate();
			
			success = i>0;
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
					conn.close();
				}
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return success;
	}


	@Override
	public boolean isLegalTimeToSendBroadcasts() {
		
		PreparedStatement pstmt = null;
		
		boolean we_are_good_to_go = false;
		
		ResultSet rs  = null;
		
		Connection conn = null;
		
		try{
			
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT HOUR(CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"')) BETWEEN t1.e and t1.l  from (SELECT `erly`.`value` as e, `late`.`value` as l FROM "+HTTPMTSenderApp.props.getProperty("DATABASE")+".settings erly JOIN "+HTTPMTSenderApp.props.getProperty("DATABASE")+".settings late WHERE  late.`key` ='latest_teaser' AND erly.`key` ='earliest_teaser') t1",Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("SELECT HOUR(CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"')) BETWEEN t1.e and t1.l  from (SELECT `erly`.`value` as e, `late`.`value` as l FROM "+HTTPMTSenderApp.props.getProperty("DATABASE")+".settings erly JOIN "+HTTPMTSenderApp.props.getProperty("DATABASE")+".settings late WHERE  late.`key` ='latest_teaser' AND erly.`key` ='earliest_teaser') t1",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			rs = pstmt.executeQuery();
			
			
			if(rs.next()){
				
				we_are_good_to_go = rs.getInt(1)>0;
			
			}else{
				
				throw new NoSettingException("earliest_teaser or latest_teaser values may not have been set in "+HTTPMTSenderApp.props.getProperty("DATABASE")+".settings! Please set those first");
			
			}
			
			
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
					conn.close();
				}
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return we_are_good_to_go;
	}


	@Override
	public int getHourNow() {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		int hour_now = -1;
		
		Connection conn = null;
		
		try{
			
			if(isUsingDS()){
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT HOUR(CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"'))", Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				pstmt = getConn().prepareStatement("SELECT HOUR(CONVERT_TZ(CURRENT_TIMESTAMP,'"+BroadcastApp.SERVER_TZ+"','"+BroadcastApp.CLIENT_TZ+"'))", Statement.RETURN_GENERATED_KEYS);
			
			}
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				hour_now = rs.getInt(1);
			}
			
		}catch(Exception e){
			
			this.log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			

			try {
				
				if(isUsingDS()){
					if(conn!=null)
						conn.close();
				}
				
			} catch (SQLException e) {
				
				this.log(e);
			
			}
			
			
		}
		
		return hour_now;
	}


	

}
