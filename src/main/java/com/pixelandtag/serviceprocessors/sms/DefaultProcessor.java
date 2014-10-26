package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.autodraw.Alarm;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.exceptions.MessageNotSetException;
import com.pixelandtag.mms.api.MM7Api;
import com.pixelandtag.mms.api.MMS;
import com.pixelandtag.mms.api.ServiceCode;
import com.pixelandtag.mms.api.TarrifCode;
import com.pixelandtag.mms.apiImpl.MMSApiImpl;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.beans.Question;
import com.pixelandtag.web.beans.Subscriber;
import com.pixelandtag.web.beans.TriviaLogRecord;
import com.pixelandtag.web.triviaI.MechanicsI;
import com.pixelandtag.web.triviaImpl.MechanicsS;

public class DefaultProcessor extends GenericServiceProcessor {

	final Logger logger = Logger.getLogger(DefaultProcessor.class);
	private Connection conn = null;
	// boolean vervose_logging = false;
	private MM7Api mm7API;
	
	private static final String RM1 = "RM1 ";
	private static final String RM0 = "RM0 ";
	private DBPoolDataSource dbpds,ds;
	private Alarm alarm = new Alarm();
	private String ENG_INVALID_KEYWORD = "Oops! Invalid keyword. Reply with HELP to know how to play the trivia. To register, reply with \"ON <YOUR_NAME>\" to 23355. E.g \"ON Tom\".";
	private String MALAY_INVALID_KEYWORD = "Oops! Kata kunci tidak tepat. Hantar HELP untuk mengetahui bagaimana untuk menyertai trivia. Untuk mendaftar, hantar \"ON <NAMA_ANDA>\" ke 23355. Cth. \"ON Tom\".";

	public DefaultProcessor() {

		int vendor = DriverUtilities.MYSQL;
	String host = "db";
		String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
		String url = DriverUtilities.makeURL(host, dbName, vendor);
		

		dbpds = new DBPoolDataSource();
		dbpds.setValidatorClassName("snaq.db.Select1Validator");
		dbpds.setName("default-processor-ds");
		dbpds.setDescription("Default Processor Pooling DataSource");
		dbpds.setDriverClassName("com.mysql.jdbc.Driver");
		dbpds.setUrl(url);
		dbpds.setUser("root");
		dbpds.setPassword("");
		dbpds.setMinPool(1);
		dbpds.setMaxPool(2);
		dbpds.setMaxSize(3);
		dbpds.setIdleTimeout(3600); // Specified in seconds.

		dbpds.setValidationQuery("SELECT COUNT(*) FROM `celcom`.`sms_service`");
		
		
		
		
		ds = new DBPoolDataSource();
		ds.setValidatorClassName("snaq.db.Select1Validator");
		ds.setName("default-mm7-processor-ds");
		ds.setDescription("MM7 api Pooling DataSource");
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl(url);
		ds.setUser("root");
		ds.setPassword("");
		ds.setMinPool(1);
		ds.setMaxPool(2);
		ds.setMaxSize(3);
		ds.setIdleTimeout(3600); // Specified in seconds.

		ds.setValidationQuery("SELECT COUNT(*) FROM `celcom`.`sms_service`");
		
		
		 try {
				
				mm7API = new MMSApiImpl(ds);
			
			} catch (Exception e) {
				
				logger.error(e.getMessage(),e);
			
			}
		
		

		logger.info(">>>>>>>>>>>>> default processor initialized and dbpoolds initialized!");

	}

	public MOSms process(MOSms mo) {

		logger.debug(mo.toString());

		conn = getConnection();

		Subscriber sub = MechanicsS.getSubscriber(mo.getMsisdn(), conn);
		
		try{
			if(Boolean.valueOf(MechanicsS.getSetting("trivia_available", conn))==false){
				 int lang_id = sub!=null ?  sub.getLanguage_id_() : 2;
				
				 if(lang_id==-1)
					 lang_id = 2;
				
				String returnd = MechanicsS.getMessage(MessageType.TRIVIA_ENDED, lang_id, conn);
				
				if(sub==null){
					Subscriber subsc = new Subscriber();
					subsc.setMsisdn(mo.getMsisdn());
					subsc.setLanguage_id_(2);
					returnd = MechanicsS.perSonalizeMessage(returnd, subsc, 0, conn);
				}else{
					returnd = MechanicsS.perSonalizeMessage(returnd, sub, 0, conn);
				}
				
				
				mo.setMt_Sent(RM0+returnd);
				
				sendMT(mo);
				
				return mo;
				
			}
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}
		
		
		
		boolean hasGotSMSDues = false;
		
		if(sub==null){
			hasGotSMSDues = MechanicsS.hasUnpaidDuesToday(mo.getMsisdn(),conn);//see if they have PSAInsufficientBalance or PSANumberBarred
		}else{
			hasGotSMSDues = MechanicsS.hasUnpaidDuesToday(sub,conn);//see if they have PSAInsufficientBalance or PSANumberBarred
		}
		
		if(hasGotSMSDues){
			try {
				String reply = MechanicsS.getMessage(MessageType.INSUFFICIENT_BALANCE, sub.getLanguage_id_(), conn);
				mo.setMt_Sent(RM0+reply);
				sendMT(mo);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
				logger.warn("SUBSCRIBER_DID_NOT_GET_MMS "+sub.getMsisdn());
				try{
					alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: Warning:", "Hi,\n\n"+e.getMessage()+"\n\n below sub: \n\n"+ sub.toString()+"\n\n sent invalid keyword, but did not get a response. \n\nPlease add more mms into the system.\n\nRegards");
				}catch(Exception e1){
					logger.error(e1.getMessage(),e1);
				}
			}
			
		}else{

			if (sub == null) {
				mo.setMt_Sent(RM0+ENG_INVALID_KEYWORD);
				sendMT(mo);
	
				if (!mo.getMt_Sent().isEmpty()) {
	
					mo.setMt_Sent(RM0+MALAY_INVALID_KEYWORD);
					mo.setCMP_Txid(generateNextTxId());
					sendMT(mo);
	
				}
	
			} else {
	
				if (!sub.isHas_reached_questions_quota_today()) {
	
					Question lastSentQuestion = MechanicsS
							.getLastQuestionSentToSub(sub.getMsisdn(), conn, false);
	
					if (lastSentQuestion == null) {
						lastSentQuestion = MechanicsS.getNextQuestion(sub, conn);
						
						if(lastSentQuestion!=null)
							MechanicsS.logAsSentButNotAnswered(sub.getMsisdn(), lastSentQuestion.getId(), conn);
					}
	
					if (!sub.isActive()) {
						
						int language_id = sub.getLanguage_id_();
						
						if(sub.getLanguage_id_()==-1){
							language_id = 2;
						}
						String msg = MALAY_INVALID_KEYWORD;
						
						try {
							msg = MechanicsS.getMessage(MessageType.CONTINUITY_MESSAGE_AFTER_AFTER_SET_OF_QUESTIONS, language_id, conn);
							msg = MechanicsS.perSonalizeMessage(msg, sub, 0, conn);
							
						} catch (MessageNotSetException e) {
							logger.error(e.getMessage(),e);
						}
						
						mo.setMt_Sent(RM0+msg);
	
						sendMT(mo);
						
	
					} else {
	
						if (sub.getLanguage_id_() == 1) {
	
							String msg = ENG_INVALID_KEYWORD;
	
							try {
	
								msg = MechanicsS.getMessage(
										MessageType.WRONG_RESPONSE,
										sub.getLanguage_id_(), conn)
										+ ((lastSentQuestion != null) ? lastSentQuestion
												.getQuestion()
												+ MechanicsS
														.getAnseringInstructions(sub
																.getLanguage_id_())
												: "");
	
								msg = MechanicsS.perSonalizeMessage(msg, sub, 0,
										conn);
	
							} catch (MessageNotSetException e) {
	
								logger.error(e.getMessage(), e);
	
							}
	
							mo.setMt_Sent(RM0+msg);
	
							sendMT(mo);
	
						} else if (sub.getLanguage_id_() == 2) {
	
							String msg = MALAY_INVALID_KEYWORD;
	
							try {
	
								msg = MechanicsS.getMessage(
										MessageType.WRONG_RESPONSE,
										sub.getLanguage_id_(), conn)
										+ ((lastSentQuestion != null) ? lastSentQuestion
												.getQuestion()
												+ MechanicsS
														.getAnseringInstructions(sub
																.getLanguage_id_())
												: "");
	
								msg = MechanicsS.perSonalizeMessage(msg, sub, 0,
										conn);
	
							} catch (MessageNotSetException e) {
	
								logger.error(e.getMessage(), e);
	
							}
	
							mo.setMt_Sent(RM0+msg);
	
							sendMT(mo);
	
						}
					}
	
				} else {
					
					try{
					
						final String MSISDN = sub.getMsisdn();
						String reply = "";
		
						if(MechanicsS.getTotalBoughtMMS(MechanicsS.TODAY, MSISDN, conn)==0){//if its a new day.
							
							
							ServiceCode price = ServiceCode.RM0;
							final List<MMS> mmss = MechanicsS.getNextMMSs(price.getCode(), sub, conn,1);
							
							if(mmss!=null){
								for(MMS mms :  mmss){
									if(mms!=null){
										
										mms.setPaidFor(false);
										mms.setLinked_id(mo.getCMP_Txid());
										mms.setWait_for_txId(mo.getCMP_Txid());
										
										if(mm7API.queueMMSForSending(mms)){
											logger.debug("queued mms to be sent : "+mms.toString());
											MechanicsS.logMMSAsSent(mms, conn);
										}else{
											logger.warn("MMS NOT SENT! sent : "+mms.toString());
										}
									}else{
										logger.warn("SUBSCRIBER_DID_NOT_GET_MMS "+sub.getMsisdn());
										try{
											alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: Warning:", "Hi,\n\n below sub: \n\n"+ sub.toString()+"\n\n did not get an MMS. \n\nPlease add more mms into the system.\n\nRegards");
										}catch(Exception e){
											logger.error(e.getMessage(),e);
										}
									}
								
								}
							
								//We log as revenue traffic - just for stats
							    final TarrifCode tarrifCode = TarrifCode.RM1;
								final TriviaLogRecord record = new TriviaLogRecord();
								record.setMsisdn(MSISDN);
								record.setName(sub.getName());
								record.setCorrect(-1);
								record.setQuestion_idFK(-1);
								record.setAnswer("RM1");
								record.setPoints(0);
								record.setSubscriber_profileFK(Integer.valueOf(sub.getId()));
								if(tarrifCode.equals(TarrifCode.RM0))
									record.setPrice("0.0");
								else if(tarrifCode.equals(TarrifCode.RM1))
									record.setPrice("1.0");
								else if(tarrifCode.equals(TarrifCode.RM_015))
									record.setPrice("0.15");
								MechanicsS.logPlay(record,conn);
								
							}else{
								
								logger.warn("SUBSCRIBER_DID_NOT_GET_MMS "+sub.getMsisdn());
								try{
									alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: Warning:", "Hi,\n\n below sub: \n\n"+ sub.toString()+"\n\n did not get an MMS. Regards");
								}catch(Exception e){
									logger.error(e.getMessage(),e);
								}
								
							}
								
								
							
							logger.debug("subscriber: "+MSISDN+" wants to play today! Lets activate him!");
							MechanicsS.toggleReachedDailyQuestionQuota(MSISDN, false, conn);//reset daily questions quota
							//Send next question.
							
							
							reply = MechanicsS.getMessage(MessageType.DAILY_CONFIRMATION, sub.getLanguage_id_(), conn);
							
							Question lastQuestionSet = MechanicsS.getLastQuestionSentToSub(MSISDN, conn,false);
							
							if(lastQuestionSet==null){
								lastQuestionSet =  MechanicsS.getNextQuestion(sub, conn);
							}
							
							if(lastQuestionSet!=null){
								
								MechanicsS.logAsSentButNotAnswered(sub.getMsisdn(), lastQuestionSet.getId(),conn);
								
								reply += (reply.endsWith(".") ? " ":". ")+lastQuestionSet.getQuestion() + MechanicsS.getAnseringInstructions(sub.getLanguage_id_());
								
							}else{
								
								logger.warn("SUBSCRIBER_DID_NOT_GET_MMS "+sub.getMsisdn());
								
								try{
									alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: Warning:", "Hi,\n\n below sub: \n\n"+ sub.toString()+"\n\n did not get a question. \n\nPlease add more questions into the system.\n\nRegards");
								}catch(Exception e){
									logger.error(e.getMessage(),e);
								}
								
							}
							
							reply = RM1+MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
							
							mo.setMt_Sent(reply);
							
							sendMT(mo);
							
						}else{
							
							
								
								//Hi <NAME>. You have reached the maximum number of participation today.  Join us again tomorrow to stand more chance to grab Nissan X-Gear & more prizes
								reply =  MechanicsS.getMessage(MessageType.PLAY_TOMORROW_CAP_REACHED, sub.getLanguage_id_(), conn);
							
								reply = MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
								
								logger.debug(sub.toString()+" Subscriber ["+MSISDN+"] wants to play, but a day has not passed..");
								
								mo.setMt_Sent(reply);
								
								sendMT(mo);
								
							
						
						}
					
					}catch(Exception e){
						
						logger.error(e.getMessage(),e);
						
					}
	
				}
			
			

			}
		}

		acknowledge(mo.getId());

		return null;
	}

	public boolean acknowledge(long message_log_id) {

		PreparedStatement pst = null;

		boolean success = false;

		Connection conn = null;

		try {

			conn = getConnection();

			pst = conn.prepareStatement("UPDATE `" + CelcomImpl.database
					+ "`.`messagelog` SET mo_ack=1 WHERE id=?",
					Statement.RETURN_GENERATED_KEYS);

			pst.setString(1, String.valueOf(message_log_id));

			success = pst.executeUpdate() > 0;

		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

		} finally {

			try {

				if (pst != null)
					pst.close();

			} catch (SQLException e) {

				logger.error(e.getMessage(), e);

			}
		}

		return success;
	}

	@Override
	public void finalizeMe() {
		
		try{
			
			mm7API.myfinalize();
			
		}catch(Exception e){
			
			logger.error(e.getMessage(), e);
			
		}
		
		
		try{
			
			if(ds!=null)
				ds.releaseConnectionPool();
			
		}catch(Exception e){
			
			logger.error(e.getMessage(), e);
			
		}

		try {

			if (conn != null)
				conn.close();

		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

		}

	}

	
	
	/**
	 * Gets the connection. If it is not closed or null, return the existing
	 * connection object, else create one and return it
	 * 
	 * @return java.sql.Connection object
	 */
	private Connection getConnection(){

		try {

			if (conn != null && !conn.isClosed()) {
				conn.setAutoCommit(true);
				return conn;
			}

		} catch (SQLException e1) {
			logger.warn(e1.getMessage() + " will create a new conn");
		} catch (Exception e1) {
			logger.warn(e1.getMessage() + " will create a new conn");
		}

		while (true) {

			try {
				while (conn == null || conn.isClosed()) {
					try {
						conn = dbpds.getConnection();

						logger.info("created connection! ");
						if (conn != null)
							return conn;
					} catch (Exception e) {
						logger.warn("Could not create connection. Reason: "
								+ e.getMessage());
						try {
							Thread.sleep(500);
						} catch (Exception ee) {
						}
					}
				}

				if (conn != null)
					return conn;

			} catch (Exception e) {
				logger.warn("can't get a connection, re-trying");
				try {
					Thread.sleep(500);
				} catch (Exception ee) {
				}
			}
		}
	}

	@Override
	public Connection getCon() {
		return getConnection();
	}

}
