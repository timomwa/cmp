package com.inmobia.celcom.serviceprocessors.sms;


/**
 *Author 			
 */
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.WordUtils;

import snaq.db.DBPoolDataSource;

import com.inmobia.axiata.connections.DriverUtilities;
import com.inmobia.axiata.web.beans.Language;
import com.inmobia.axiata.web.beans.MessageType;
import com.inmobia.axiata.web.beans.Question;
import com.inmobia.axiata.web.beans.RequestObject;
import com.inmobia.axiata.web.beans.Subscriber;
import com.inmobia.axiata.web.beans.TriviaLogRecord;
import com.inmobia.axiata.web.triviaI.MechanicsI;
import com.inmobia.axiata.web.triviaImpl.MechanicsS;
import com.inmobia.celcom.api.GenericServiceProcessor;
import com.inmobia.celcom.autodraw.Alarm;
import com.inmobia.celcom.entities.MOSms;
import com.inmobia.celcom.sms.application.HTTPMTSenderApp;
import com.inmobia.mms.api.MM7Api;
import com.inmobia.mms.api.MMS;
import com.inmobia.mms.api.ServiceCode;
import com.inmobia.mms.api.TarrifCode;
import com.inmobia.mms.apiImpl.MMSApiImpl;
import com.inmobia.util.StopWatch;

public class AxiataTriviaProcessor extends GenericServiceProcessor {
	
	private static final String A = "A";
	private static final String B = "B";
	private static final String STOP = "STOP";
	private static final String START = "START";
	private static final String GO = "GO";
	private static final String SERVICE_KEYWORD3 = "ON";
	private static final String RM1 = "RM1 ";
	private static final String RM0 = "RM0 ";
	private Connection conn = null;
	private StopWatch watch;
	private final String SERVICE_KEYWORD1 = "AC";
	private final String SERVICE_KEYWORD2 = "AXIATA";
	private final String SERVICE_KEYWORD5 = "TRIVIA";
	private MM7Api mm7API;
	private DBPoolDataSource ds;
	private DBPoolDataSource ds2;
	private volatile MOSms mocopyWithin;
	
	private Alarm alarm = new Alarm();
	
	
	
	
	
	
	
	
	
	public AxiataTriviaProcessor(){
		
		//int queue_size_per_thread = 100;
		//s(100);
		super.max_queue_size = 100;
		
		watch = new StopWatch();
		int vendor = DriverUtilities.MYSQL;
		String driver = DriverUtilities.getDriver(vendor);
		String host = "db";
		String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
		String url = DriverUtilities.makeURL(host, dbName, vendor);
		
		    
		ds = new DBPoolDataSource();
	    ds.setName("TRIVIA_PROCESSOR_DS");
	    ds.setDescription("MM7 Sender thread : "+ds.getName());
	    ds.setDriverClassName(driver);
	    ds.setUrl(url);
	    ds.setUser("root");
	    ds.setPassword("");
	    ds.setMinPool(1);
	    ds.setMaxPool(2);
	    ds.setMaxSize(3);
	    ds.setIdleTimeout(3600);  // Specified in seconds.
	    ds.setValidationQuery("SELECT 'Test'");
	   // ds.registerShutdownHook();
		
	    try {
			
			mm7API = new MMSApiImpl(ds);
		
		} catch (Exception e) {
			
			log(e);
		
		}
		
		
		ds2 = new DBPoolDataSource();
	    ds2.setName("TRIVIA_PROCESSOR_DS2");
	    ds2.setDescription("AxiataTriviaProcessor Sender thread : "+ds.getName());
	    ds2.setDriverClassName(driver);
	    ds2.setUrl(url);
	    ds2.setUser("root");
	    ds2.setPassword("");
	    ds2.setMinPool(2);
	    ds2.setMaxPool(3);
	    ds2.setMaxSize(5);
	    ds2.setIdleTimeout(3600);  // Specified in seconds.
	    ds2.setValidationQuery("SELECT COUNT(*) FROM `celcom`.`sms_service`");
	    
	   
		
	}
	
	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}

	@Override
	public void finalizeMe(){
		
		try {
			
			if(conn!=null)
				conn.close();
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
		}
		
		
		try {
			
			ds.releaseConnectionPool();
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
		}
		
		try {
			
			ds2.releaseConnectionPool();
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
		}
		
	}
	
	public String toString(){
		return getClass().getName();
	}

	public MOSms process(MOSms mo) {
		
		
		//logger.info("::::::::::::::::______________________________::::::::::::::::::::::::::::::::::::::::SERVICEI_ID::::::::::::________________________________:::::::::::::"+mo.getServiceid());
		
		this.mocopyWithin = mo;//give
		
		logger.debug(mo.toString());
		
		mo.setMt_Sent(subProcess(mo));
		
		mo = this.mocopyWithin;//take back
		
		if(!mo.getMt_Sent().isEmpty()){
			
			if(mo.getMt_Sent().trim().startsWith(RM1)){
				mo.setCMP_SKeyword(TarrifCode.RM1.getCode());//1 ringgit
				mo.setPrice(1.0d);
			}else if(mo.getMt_Sent().trim().startsWith(RM0)){
				mo.setCMP_SKeyword(TarrifCode.RM0.getCode());//1 ringgit
				mo.setPrice(0.0d);
			}else if(!mo.getMt_Sent().trim().startsWith(RM1.trim()) && !mo.getMt_Sent().trim().startsWith(RM0.trim())){
				mo.setMt_Sent(RM0+mo.getMt_Sent());
				mo.setPrice(0.0d);
			}
			
			Connection conn = getCon();
			
			
			
			sendMT(mo);
			
			
			
			toStatsLog(mo, conn);
			
			try{
				if(conn!=null)
					conn.close();
			}catch(Exception e){
				log(e);
			}
		}
		
		acknowledge(mo.getId());
		
		return mo;
	}
	
	

	

	

	
	
	
	
	
	
	
	
	public String subProcess(MOSms mo){
		
		//Connection conn = null;
		
		String reply = "";
		
		try {
			
			
			final Connection conn = getCon();
			
			try{
			
					if(conn==null)
						throw new SQLException("Could not create a db connection! ");
					
					
					
					
					final RequestObject ro = new RequestObject(mo);
					final String MSISDN = ro.getMsisdn();
					final boolean verbose_logging = Boolean.valueOf(MechanicsS.getSetting("verbose_logging", conn).trim());
					
					if(verbose_logging){
						logger.debug("1a. KEYWORD: "+ro.getKeyword());
						logger.debug("1a. MESSAGE: "+ro.getMsg());
					}
					
					final String KEYWORD  = ro.getKeyword();
					final String MSG = ( (ro.getMsg().indexOf(" ") > -1 && KEYWORD.length()>-1) ?   ro.getMsg().substring(KEYWORD.length(), ro.getMsg().length()) : ro.getMsg()).trim() ; 
					
					final boolean transaction_time_logging = Boolean.valueOf(MechanicsS.getSetting("transaction_time_logging", conn).trim());
					
					
					if(verbose_logging){
						logger.debug("a. KEYWORD: "+ro.getKeyword());
						logger.debug("a. MESSAGE: "+ro.getMsg());
						
						logger.debug("f. KEYWORD: "+KEYWORD);
						logger.debug("f. MESSAGE: "+MSG);
					}
					
					
					if(verbose_logging)
						MechanicsS.loginTraffic(MSISDN,"MSG: "+MSG+" KEYWORD: "+KEYWORD,conn);
					
					
					if(MSISDN.equals(MechanicsS.getSetting("admin_msisdn", conn)) && KEYWORD.equals("TESTALARM")){
						try{
							alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: INFO: TEST_ALARM.", "Hi,\n\n below sub:\n\n Alarm tests. SMS invoked\n\n  Regards");
							return "Email sent!";
						}catch(Exception e){
							log(e);
							return "email not sent";
						}
						
					}
					
					if(transaction_time_logging)
						watch.start();
					
					final Subscriber sub = MechanicsS.getSubscriber(MSISDN,conn);
					
					
					if(Boolean.valueOf(MechanicsS.getSetting("trivia_available", conn))==false){
						 int lang_id = sub!=null ?  sub.getLanguage_id_() : 2;
						
						 if(lang_id==-1)
							 lang_id = 2;
						
						String returnd = MechanicsS.getMessage(MessageType.TRIVIA_ENDED, lang_id, conn);
						
						if(sub==null){
							Subscriber subsc = new Subscriber();
							subsc.setMsisdn(MSISDN);
							subsc.setLanguage_id_(2);
							returnd = MechanicsS.perSonalizeMessage(returnd, subsc, 0, conn);
						}else{
							returnd = MechanicsS.perSonalizeMessage(returnd, sub, 0, conn);
						}
						
						
						if(transaction_time_logging){
							watch.stop();
							watch.reset();
						}
						
						return RM0+returnd;
						
					}
					
					final boolean isNew = ((sub == null) ? true: false);
					
					MessageType msgKey = null;
					
					//Help and info lines
					if(KEYWORD.equals("INFO") || KEYWORD.equals("HELP")){
						
						MessageType type = MessageType.get("HELP");
						
						if(isNew){
						
							reply = MechanicsS.getMessage(type, MechanicsS.DEFAULT_LANGUAGE_ID, conn);
							
						}else{
							
							reply = MechanicsS.getMessage(type, sub.getLanguage_id_(), conn);
						
						}
						
						return reply;
						
					}
					
					if(isNew){
						
						//This a new subscriber.
						final Subscriber newSub = new Subscriber();
						
						//If they send A or B, STOP or START, then ask them to register first.
						// WINNER, WIN, WIN2 & BUY
						if(KEYWORD.equals(SERVICE_KEYWORD5) || KEYWORD.equals("BUY") || KEYWORD.equals("WIN2") || KEYWORD.equals("WIN") || KEYWORD.equals("WINNER") || KEYWORD.equals("POINT") || KEYWORD.equals("POINTS") || KEYWORD.equals("1") || KEYWORD.equals("2") || KEYWORD.equals("BM") || KEYWORD.equals("ENGLISH") || KEYWORD.equals("ENG") || KEYWORD.equals("MAL") || KEYWORD.equals("MALAY") || KEYWORD.equals("A") || KEYWORD.equals("B") || KEYWORD.equals("STOP") || KEYWORD.equals("START") || KEYWORD.equals(GO) || KEYWORD.equals("YES") || KEYWORD.equals("Y") || KEYWORD.equals("YEP") || KEYWORD.equals("YA")){
							
							reply =  MechanicsS.getMessage(MessageType.REGISTER_FIRST, MechanicsI.DEFAULT_LANGUAGE_ID,conn);
							
							if(transaction_time_logging){
								watch.stop();
								logger.info(">< . >< . >< . >< . >< .KEYWORD: "+KEYWORD+" took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to execute");
								watch.reset();
							}
							
							return reply;
							
							
						}else if(KEYWORD.equals(SERVICE_KEYWORD1) || KEYWORD.equals(SERVICE_KEYWORD2) || KEYWORD.equals(SERVICE_KEYWORD3) || KEYWORD.equals(SERVICE_KEYWORD5)){
							
							//Register subscriber
							if(MSG.trim().equalsIgnoreCase(SERVICE_KEYWORD1) || MSG.trim().equalsIgnoreCase(SERVICE_KEYWORD2) || MSG.trim().equalsIgnoreCase(SERVICE_KEYWORD3) || KEYWORD.equals(SERVICE_KEYWORD5)){
								
								newSub.setName("Customer");//set the sub's name so we don't hit the db again
							
							}else{
								
								if(MSG.length()>10)
									newSub.setName(WordUtils.capitalize(MSG.replaceAll("[^\\p{L}\\p{N}]", "").substring(0, 10).toLowerCase()));//set the sub's name so we don't hit the db again
								else
									newSub.setName(WordUtils.capitalize(MSG.replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase()));//set the sub's name so we don't hit the db again
							
							}
							
							
							int newSub_id = MechanicsS.registerSubscriber(MSISDN, newSub.getName(),MechanicsI.DEFAULT_LANGUAGE_ID,conn);
							boolean regSuccess = newSub_id>-1;
							
							if(regSuccess){
								
								
								
								
								newSub.setMsisdn(MSISDN);
								
								final TriviaLogRecord record = new TriviaLogRecord();
								
								record.setMsisdn(MSISDN);
								record.setName(newSub.getName());
								record.setCorrect(-1);//-1 means its a new registration
								record.setQuestion_idFK(-1);//Registration
								record.setAnswer("START");
								record.setPoints(MechanicsS.getRegistration_points(conn));
								record.setSubscriber_profileFK(newSub_id);
								MechanicsS.logPlay(record,conn);
								
								reply = MechanicsS.getMessage(MessageType.PRE_WELCOME_MESSAGE, MechanicsI.DEFAULT_LANGUAGE_ID,conn);
								
								reply = MechanicsS.perSonalizeMessage(reply, newSub, 0, conn);
							
								return reply;
								
							}else{
								
								logger.fatal("SUBSCRIPTION FAILED!! "+mo.toString());
								
								return "";
							
							}
							
							
						}else{
							
							int languageID = MechanicsS.getLanguageID(KEYWORD,conn);
							
							int new_sub_id = MechanicsS.registerSubscriber(MSISDN, MSG,languageID,conn);
							boolean regSuccess = new_sub_id>-1;
							
							newSub.setId(new_sub_id);
							newSub.setMsisdn(MSISDN);//Set the msisdn for our new subscriber
							newSub.setLanguage_id_(languageID);//Set the language ID
							newSub.setName(MSG);//Hopefully the name is from the second string onwards - msg
							
							msgKey = regSuccess ? MessageType.PRE_WELCOME_MESSAGE : MessageType.REGISTRATION_FAILURE;
							
							Question firstQuestion = null;
							
							
							
							if(regSuccess){
								Subscriber sub1 = new Subscriber();
								sub1.setMsisdn(MSISDN);
								sub1.setLanguage_id_(MechanicsI.DEFAULT_LANGUAGE_ID);
								firstQuestion = MechanicsS.getFirstQuestion(sub1,conn);
							}
							
							reply = MechanicsS.getMessage(msgKey ,languageID,conn);
							
							logger.debug(msgKey.toString()+" : "+reply);
							
							//Now that we have some info about the sub, why don't we reply with  
							//a nice message
							reply = MechanicsS.perSonalizeMessage(reply,newSub,0,conn);
							
							logger.debug("after processing" +msgKey.toString()+" : "+reply);
							
							/*MOSms regS = MOSms.clone(mo);
							regS.setCMP_Txid(generateNextTxId());
							regS.setMt_Sent(RM0+reply);
							sendMT(regS);
							reply = "";*/
							
							reply =  firstQuestion.getQuestion() + MechanicsS.getAnseringInstructions(sub.getLanguage_id_());
							//reply+" "+(firstQuestion==null ? "" :
							
							if(transaction_time_logging){
								watch.stop();
								logger.info(">< . >< . >< . >< . >< .KEYWORD: "+KEYWORD+" took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to execute");
								watch.reset();
							}
							
							//pw.write(reply);//+":"+reply.length());
							//if(verbose_logging)
								//MechanicsS.logResponse(MSISDN,reply,conn);
							
							//TODO Insert into vas.SMPPMsgLog ?
							//MechanicsS.insertIntoVasSMPPToSend(MSISDN, reply);
							
							
							if(regSuccess){
								final TriviaLogRecord record = new TriviaLogRecord();
								record.setMsisdn(MSISDN);
								record.setName(newSub.getName());
								record.setCorrect(-1);//-1 means its a new registration
								record.setQuestion_idFK(-1);//Registration
								record.setAnswer("START");
								record.setPoints(MechanicsS.getRegistration_points(conn));
								
								MechanicsS.logPlay(record,conn);
								
								//Update the questions_sent table
								boolean questionMarkedAsSent = MechanicsS.logAsSentButNotAnswered(newSub.getMsisdn(), firstQuestion.getId(),conn);
								
								logger.debug("IS FIRST QUESTOION LOGGED ? "+questionMarkedAsSent);
								
							}
							
							
							return RM0+reply;
							
						}
						
						
					}else if(sub.getLanguage_id_()==-1 && (KEYWORD.equals(GO) || KEYWORD.equals(START) )){
						
						
						if(verbose_logging)
							logger.info("WANTS TO START, but language not chosen!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!AGGGGGGGGGGRRRRR WHY YOU NO FOLLOW INSTRUCTIONS???!");
						
						MechanicsS.toggleSubSubscribed(MSISDN, true, conn);//set subscriber active or inactive depending whether START or STOP is sent
						
						reply = MechanicsS.getMessage(MessageType.CHOOSE_LANGUAGE_FIRST, sub.getLanguage_id_(), conn);
						
						reply = MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
						
						return reply;
						
					}else if(sub.getLanguage_id_()>-1 && (KEYWORD.equals("POINT") || KEYWORD.equals("POINTS"))){
						
						reply = "";
						
						reply = MechanicsS.getMessage(MessageType.POINTS_REQUEST, sub.getLanguage_id_(),conn);
						
						reply = MechanicsS.perSonalizeMessage(reply, sub, 0,conn);
						
						if(transaction_time_logging){
							watch.stop();
							logger.info(">< . >< . >< . >< . >< .KEYWORD: "+KEYWORD+" took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to execute");
							watch.reset();
						}
						
						//pw.write(reply);
						
						//if(verbose_logging)
							//MechanicsS.logResponse(MSISDN,reply,conn);
						
						return reply;
					
					}else if(KEYWORD.equals(STOP) || KEYWORD.equals(START) || KEYWORD.equals(GO)){
						
						
						final boolean wantsOut = (KEYWORD.equals(STOP) ? true : false);
						
						final boolean activate = wantsOut ? false : true;
						
						final MessageType key = (wantsOut ? MessageType.STOP_MESSAGE : MessageType.WELCOME_BACK);
						
						Question questionToSend = null;
						
						
						if(KEYWORD.equals(START) || KEYWORD.equals(GO)){//If keyword==START and the sub is active, then send them a question. It could be the last question we sent to them, or if they had answered that one, we send them another
							
							
							//TODO do implementation for com.inmobia.axiata.web.triviaImpl.MechanicsS.hasunpaidMMSToday(Subscriber, Connection)
							boolean hasGotSMSDues = MechanicsS.hasUnpaidDuesToday(sub,conn);//see if they have PSAInsufficientBalance or PSANumberBarred
							
							if(hasGotSMSDues){
								reply = MechanicsS.getMessage(MessageType.INSUFFICIENT_BALANCE, sub.getLanguage_id_(), conn);
								return reply;
							}
							
							
							if(MechanicsS.hasMaxedOnSet(MechanicsS.DAILY_QUESTION_QUOTA, MSISDN, conn)){
								reply = MechanicsS.getMessage(MessageType.PLAY_TOMORROW_CAP_REACHED, sub.getLanguage_id_(), conn);
								reply = MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
								return reply;
							}
							
							//I commented the below out because you want to exite the subscriber and send a different
							//question when they resume play. Instead of sending the old unanswered question, then we send a new one..
							//questionToSend = MechanicsS.getLastQuestionSentToSub(MSISDN,conn,false);//Get the last unanswerd question we sent them
							
							if(questionToSend==null){	
								
								questionToSend = MechanicsS.getNextQuestion(sub,conn);
								
								//Update the questions_sent table, because its a new question we got for him
								if(questionToSend!=null)//Make sure we have got more questions to send
									MechanicsS.logAsSentButNotAnswered(sub.getMsisdn(), questionToSend.getId(),conn);
							}
							
						}else{
							
							
							TriviaLogRecord pastR = MechanicsS.getPrevRecordWithAnswerCol(sub,STOP, conn);
							if(pastR==null){
								final TriviaLogRecord record = new TriviaLogRecord();
								record.setMsisdn(MSISDN);
								record.setName(sub.getName());
								record.setCorrect(-1);//-1 means its a new registration
								record.setQuestion_idFK(-1);//Registration or  stop
								record.setAnswer(STOP);
								record.setSubscriber_profileFK(Integer.valueOf(sub.getId()));
								
								MechanicsS.logPlay(record,conn);
							}
						}
						
						MechanicsS.toggleSubSubscribed(MSISDN, activate, conn);//set subscriber active or inactive depending whether START or STOP is sent
						
						reply = MechanicsS.getMessage(key, sub.getLanguage_id_(),conn);
						
						//Now append the next question to the reply if the user is wanting back in.
						if(wantsOut==false){
							
							if(questionToSend!=null){
								
								reply += (reply.endsWith(".") ? " ":". ")+questionToSend.getQuestion() + MechanicsS.getAnseringInstructions(sub.getLanguage_id_());
								
							
							}else{
								
								reply += (reply.endsWith(".") ? " ":". ")+MechanicsS.getMessage(MessageType.NO_MORE_QUESTIONS, sub.getLanguage_id_(),conn);
								
							}
							
						}
						
						reply = MechanicsS.perSonalizeMessage(reply, sub, 0,conn);
						
						if(transaction_time_logging){
							
							watch.stop();
							logger.info(">< . >< . >< . >< . >< .KEYWORD: "+KEYWORD+" took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to execute");
							watch.reset();
						
						}
						
						//pw.write(reply);
						
						//if(verbose_logging)
							//MechanicsS.logResponse(MSISDN,reply,conn);
						
						return reply;
						
					}else if(!sub.isActive() && !sub.isHas_reached_questions_quota_today() && sub.getLanguage_id_()>-1){//if sub is not yet activated. and if they've selected language
						
						if(verbose_logging)
							logger.debug(">>>>>>>>>>>>>>>>sub: "+sub.toString());
						
						if(KEYWORD.equals("WINTOO") || KEYWORD.equals("BUY") || KEYWORD.equals("WIN2") || KEYWORD.equals("WIN") || KEYWORD.equals("WINNER") || KEYWORD.equals("YES") || KEYWORD.equals("YA") || KEYWORD.equals("Y") || KEYWORD.equals("OK") || KEYWORD.equals("YEP")){

							
								boolean hasFreeContent = MechanicsS.hasFreeContent(sub, conn);
							
								//MechanicsS.toggleSubContinuationConfirmed(MSISDN, true, conn);//we take it as they've confirmed participation
								//SEND MMS
								//SENDING MMS
								
								ServiceCode price = ServiceCode.RM0;
								final List<MMS> mmss = MechanicsS.getNextMMSs(price.getCode(), sub, conn,1);
								
								
								if(mmss!=null){
									
									for(MMS mms :  mmss){
										if(mms!=null){
											mms.setPaidFor(false);
											mms.setLinked_id(mo.getCMP_Txid());
											mms.setWait_for_txId(mo.getCMP_Txid());
											
											if(mm7API.queueMMSForSending(mms)){
												if(verbose_logging)
													logger.info("queued mms to be sent : "+mms.toString());
												MechanicsS.logMMSAsSent(mms, conn);
												
												
											}else{
												logger.warn("MMS NOT SENT! sent : "+mms.toString());
											}
										}else{
											logger.warn("SUBSCRIBER_DID_NOT_GET_MMS "+sub.getMsisdn());
											try{
												alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: Warning:", "Hi,\n\n below sub: \n\n"+ sub.toString()+"\n\n did not get an MMS. Regards");
											}catch(Exception e){
												log(e);
											}
										}
									
									}
									
									
									final TriviaLogRecord record = new TriviaLogRecord();
									record.setMsisdn(MSISDN);
									record.setName(sub.getName());
									record.setCorrect(-1);
									record.setQuestion_idFK(-1);
									record.setAnswer("MMS");
									record.setPoints(5);
									record.setSubscriber_profileFK(Integer.valueOf(sub.getId()));
									//if(price.equals(ServiceCode.RM1))
									record.setPrice((hasFreeContent ? "0.0" : "1.0"));
									//else
									//	record.setPrice("0.0");
									MechanicsS.logPlay(record,conn);
								
								}else{
									
									logger.warn("SUBSCRIBER_DID_NOT_GET_MMS "+sub.getMsisdn());
									try{
										alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: Warning:", "Hi,\n\n below sub: \n\n"+ sub.toString()+"\n\n did not get an MMS. Regards");
									}catch(Exception e){
										log(e);
									}
								}
								
								//TODO do implementation for com.inmobia.axiata.web.triviaImpl.MechanicsS.hasunpaidMMSToday(Subscriber, Connection)
								boolean hasGotSMSDues = MechanicsS.hasUnpaidDuesToday(sub,conn);//see if they have PSAInsufficientBalance or PSANumberBarred
								
								if(!hasGotSMSDues){
									
									MechanicsS.toggleSubContinuationConfirmed(MSISDN, true, conn);//we take it as they've confirmed participation
									MechanicsS.toggleSubActive(MSISDN, true, conn);
								
									
										
										if(MechanicsS.hasMaxedOnSet(MechanicsS.QUESTIONS_PER_MMS,MSISDN, conn)){
												
											final String CONTINUITY_CONFIRMATION = MechanicsS.getMessage(MessageType.CONTINUITY_CONFIRMATION, sub.getLanguage_id_(), conn); 
												
											reply = CONTINUITY_CONFIRMATION;
												
											final Question nextQuestion  = MechanicsS.getNextQuestion(sub, conn);
												
											if(nextQuestion!=null){
												reply += ((reply.endsWith(".") || reply.endsWith("!") || reply.endsWith("?")) ? " ":". ") + nextQuestion.getQuestion() + MechanicsS.getAnseringInstructions(sub.getLanguage_id_());
											}else{
												try{
													alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: Warning: Sub did not receive a question.", "Hi,\n\n below sub: \n\n"+ sub.toString()+"\n\n did not get question. \n\nPlease add more questions into the system.\n\nRegards");
												}catch(Exception e){
													log(e);
												}
											}
												
											if(nextQuestion!=null)
												MechanicsS.logAsSentButNotAnswered(MSISDN, nextQuestion.getId(), conn);
												
											//MechanicsS.toggleSubActive(MSISDN, true, conn);
											
											if(!hasFreeContent){
												reply = RM1+MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
											}else{
												reply = RM0+MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
												MechanicsS.updateFree(sub, true, conn);
											}
											
											return reply;
											
										}else{
											
											//MechanicsS.toggleSubActive(MSISDN, true, conn);//already did above
											//get last unanswered question if any, if none, get new one and send
											Question question = MechanicsS.getLastQuestionSentToSub(MSISDN, conn, false);
											
											if(question==null){
												
												question = MechanicsS.getNextQuestion(sub, conn);
												
												if(question!=null)
													MechanicsS.logAsSentButNotAnswered(MSISDN, question.getId(), conn);
												
											}
											
											if(question!=null){
												
												reply = question.getQuestion() + MechanicsS.getAnseringInstructions(sub.getLanguage_id_());
												final String CONTINUITY_CONFIRMATION = MechanicsS.getMessage(MessageType.CONTINUITY_CONFIRMATION, sub.getLanguage_id_(), conn); 
												
												if(!hasFreeContent){
													reply = RM1+CONTINUITY_CONFIRMATION+" "+MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
												}else{
													reply = RM0+CONTINUITY_CONFIRMATION+" "+MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
													MechanicsS.updateFree(sub, true, conn);
												}
												
												return reply;
												
											}else{
												//report this case
												logger.warn("SUBSCRIBER_DID_NOT_GET_MMS "+sub.getMsisdn());
												try{
													alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: Warning:", "Hi,\n\n below sub: \n\n"+ sub.toString()+"\n\n did not get a question. \n\nPlease add more mms into the system.\n\nRegards");
												}catch(Exception e){
													log(e);
												}
											}
											
											
										}
								}else{
									
									reply = MechanicsS.getMessage(MessageType.INSUFFICIENT_BALANCE, sub.getLanguage_id_(), conn);
									
									return reply;
									
								}
								
							
						}else{
							
							reply = MechanicsS.getMessage(MessageType.CONFIRM_FIRST, sub.getLanguage_id_(), conn);
							
							reply = MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
							
							return reply;
							
						}
						
						
					}else if((sub.isHas_reached_questions_quota_today()) && sub.getLanguage_id_()>-1){
						
						
						//TODO do implementation for com.inmobia.axiata.web.triviaImpl.MechanicsS.hasunpaidMMSToday(Subscriber, Connection)
						boolean hasGotSMSDues = MechanicsS.hasUnpaidDuesToday(sub,conn);//see if they have PSAInsufficientBalance or PSANumberBarred
						
						
						//if(KEYWORD.equals(GO)){
							
							//logger.info("MechanicsS.getTotalAnsweredQuestions(MechanicsS.TODAY, MSISDN, conn): "+MechanicsS.getTotalAnsweredQuestions(MechanicsS.TODAY, MSISDN, conn));
							
						if(!hasGotSMSDues){
							if(MechanicsS.getTotalBoughtMMS(MechanicsS.TODAY, MSISDN, conn)==0){//if its a new day.
								
								if(sub.getSubscribed().equals("0"))
									MechanicsS.toggleSubSubscribed(MSISDN, true, conn);//set subscriber active or inactive depending whether START or STOP is sent
								
								ServiceCode price = ServiceCode.RM0;
								final List<MMS> mmss = MechanicsS.getNextMMSs(price.getCode(), sub, conn,1);
								
								if(mmss!=null){
									for(MMS mms :  mmss){
										if(mms!=null){
											
											mms.setPaidFor(false);
											mms.setLinked_id(mo.getCMP_Txid());
											mms.setWait_for_txId(mo.getCMP_Txid());
											
											if(mm7API.queueMMSForSending(mms)){
												
												if(verbose_logging)
													logger.info("queued mms to be sent : "+mms.toString());
												MechanicsS.logMMSAsSent(mms, conn);
											}else{
												
												logger.warn("MMS NOT SENT! sent : "+mms.toString());
											}
										}else{
											logger.warn("SUBSCRIBER_DID_NOT_GET_MMS "+sub.getMsisdn());
											try{
												alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: Warning:", "Hi,\n\n below sub: \n\n"+ sub.toString()+"\n\n did not get an MMS. \n\nPlease add more mms into the system.\n\nRegards");
											}catch(Exception e){
												log(e);
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
										log(e);
									}
									
								}
								
								if(verbose_logging)
									logger.info("subscriber: "+MSISDN+" wants to play today! Lets activate him!");
								
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
										log(e);
									}
									
								}
								
								
								if(!MechanicsS.hasFreeContent(sub, conn)){
									reply = RM1+MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
								}else{
									reply = RM0+MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
									MechanicsS.updateFree(sub, true, conn);
								}
								
								return reply;
								
							}else{
								
								//Hi <NAME>. You have reached the maximum number of participation today.  Join us again tomorrow to stand more chance to grab Nissan X-Gear & more prizes
								reply =  MechanicsS.getMessage(MessageType.PLAY_TOMORROW_CAP_REACHED, sub.getLanguage_id_(), conn);
							
								reply = MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
								
								if(verbose_logging)
									logger.info(sub.toString()+" Subscriber ["+MSISDN+"] wants to play, but a day has not passed..");
								
								return reply;
							
							}
						}else{
							
							reply = MechanicsS.getMessage(MessageType.INSUFFICIENT_BALANCE, sub.getLanguage_id_(), conn);
							
							return reply;
						}
							
						//}else{
							
							
						//	reply =  MechanicsS.getMessage(MessageType.PLAY_TOMORROW_CAP_REACHED, sub.getLanguage_id_(), conn);
							
						//	reply = MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
						//	
						//	return reply;
						//}
						//TODO test this
						
						
					}else if(/*!sub.isActive() && */((sub.getLanguage_id_()==-1) || (KEYWORD.equals("1") || KEYWORD.equals("BM") || KEYWORD.equals("ENGLISH")|| KEYWORD.equals("EN") || KEYWORD.equals("ENG") || KEYWORD.equals("MAL") || KEYWORD.equals("MALAY") || KEYWORD.equals("MALY") || KEYWORD.equals("2")))){
						
						final Language lang = Language.get(KEYWORD);
						
						if(lang!=null){
							
							final int total_questionsAnswered = MechanicsS.getTotalAnsweredQuestions(MechanicsS.IN_TOTAL, MSISDN, conn);
							
							final String language = lang.toString();
							
							final int lang_id = MechanicsS.getLanguageID(language, conn);
							
							MechanicsS.changeSubLanguage(MSISDN, lang_id, conn);
							
							sub.setLanguage_id_(lang_id);
							
							if(!sub.isActive()){
								
								reply = MechanicsS.getMessage(MessageType.POST_WELCOME_MESSAGE, lang_id, conn);// reply in the new language
								
								final String reply1 = RM1+MechanicsS.perSonalizeMessage(reply, sub, 5, conn);
								
								MOSms regS = MOSms.clone(mo);
								this.mocopyWithin.setCMP_Txid(generateNextTxId());
								regS.setMt_Sent(reply1);
								regS.setCMP_SKeyword(TarrifCode.RM1.getCode());
								regS.setPrice(1);
								toStatsLog(regS, conn);
								sendMT(regS);
								
							}else{
								
								reply = MechanicsS.getMessage(MessageType.LANGUAGE_CHANGED, lang_id, conn);// reply in the new language
								
								reply = reply.replace("<LANGUAGE>", language);
							
							}
							
							
							if(verbose_logging)
								logger.info("::::::::::::::::::::::::::sub.isActive():"+sub.isActive()+"::::::::: total_questionsAnswered  "+total_questionsAnswered);
							
							boolean we_just_activated_sub = false;
							
							if(total_questionsAnswered==0 && !sub.isActive()){// if they've not answered a single question, and they're not yet active
								
								//SEND MMS
								//SENDING MMS
								
								ServiceCode price = ServiceCode.RM0;
								final List<MMS> mmss = MechanicsS.getNextMMSs(price.getCode(), sub, conn,1);
								
								if(mmss!=null){
									for(MMS mms :  mmss){
										if(mms!=null){
											
											mms.setPaidFor(false);
											mms.setLinked_id(mo.getCMP_Txid());
											mms.setWait_for_txId(mo.getCMP_Txid());
											
											if(mm7API.queueMMSForSending(mms)){
												if(verbose_logging)
													logger.info("queued mms to be sent : "+mms.toString());
												MechanicsS.logMMSAsSent(mms, conn);
											}else{
												if(verbose_logging)
													logger.warn("MMS NOT SENT! sent : "+mms.toString());
											}
										}else{
											logger.warn("SUBSCRIBER_DID_NOT_GET_MMS "+sub.getMsisdn());
											try{
												alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: Warning:", "Hi,\n\n below sub: \n\n"+ sub.toString()+"\n\n did not get an MMS. \n\nPlease add more mms into the system.\n\nRegards");
											}catch(Exception e){
												log(e);
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
										log(e);
									}
								}
								
								
									
									
								
								
								//instead of calling toggleSubActive and toggleSubSubscribed, do it in a single db call. saves cpu a little bit
								we_just_activated_sub  = MechanicsS.toggleActieAndSubscribed(MSISDN, true, conn);
								
								
								//TODO MMS is sent at this point, also a question appended to the reply here
								final Question questionToSend = MechanicsS.getNextQuestion(sub,conn);
								
								if(verbose_logging)
									logger.info("::::::::::::::::::::::we_just_activated_sub : "+we_just_activated_sub+":::::::::::::::::: total_questionsAnswered  "+total_questionsAnswered);
								
								if(questionToSend!=null){	
									
									reply =  questionToSend.getQuestion() + MechanicsS.getAnseringInstructions(sub.getLanguage_id_());
									//((reply.endsWith("?") || reply.endsWith("!") || reply.endsWith(".")) ? " " : ". ") +
									//Update the questions_sent table
									MechanicsS.logAsSentButNotAnswered(sub.getMsisdn(), questionToSend.getId(),conn);
									
									if(verbose_logging)
										logger.info(":::::::::::::::::::::::::::: questionToSend.getQuestion()  "+ questionToSend.getQuestion() + MechanicsS.getAnseringInstructions(sub.getLanguage_id_()));
								
								}
								
							
							}
							
							reply = MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
							
							if(reply==null || reply.isEmpty())
								return "";
							else
								return RM0 + reply;
							
						}else{
							
							reply = MechanicsS.getMessage(MessageType.CHOOSE_LANGUAGE_FIRST, sub.getLanguage_id_(), conn);
							
							reply = MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
							
							return reply;
							
						}
						
					}else if((sub.getLanguage_id_()>-1) && KEYWORD.equals("NAME")){
						final String newNick = MSG.length()<=10 ? 
								WordUtils.capitalize(MSG.replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase()) 
								:
								WordUtils.capitalize(MSG.replaceAll("[^\\p{L}\\p{N}]", "").substring(0, 10).toLowerCase());
						MechanicsS.changeSubscriberName(MSISDN, newNick, conn);
						
						reply = MechanicsS.getMessage(MessageType.NAME_CHANGED, sub.getLanguage_id_(), conn);
						
						reply = reply.replaceAll("<OLD_NAME>", "\""+sub.getName()+"\"").replaceAll("<NEW_NAME>", "\""+newNick+"\"").trim();
						
						reply = MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
						
						return reply;
						
						
					}else{
						
						if(verbose_logging)
							logger.debug("KEYWORD: "+KEYWORD);
						
						if(KEYWORD.equals(A) || KEYWORD.equals(B)){
							
							
							
							MechanicsS.updateLastAction(MSISDN,conn);
							
							final boolean subscriber_had_Opted_Out_And_Now_Is_Back = sub.isActive()==false;
							
							if(subscriber_had_Opted_Out_And_Now_Is_Back){
								
								MechanicsS.toggleSubActive(MSISDN, true,conn);
								String welcomeBack = MechanicsS.getMessage(MessageType.WELCOME_BACK, sub.getLanguage_id_(),conn);
								welcomeBack = MechanicsS.perSonalizeMessage(welcomeBack, sub, 0,conn);
								
							}
							
							
							boolean hasGotSMSDues = MechanicsS.hasUnpaidDuesToday(sub,conn);//see if they have PSAInsufficientBalance or PSANumberBarred
							
							if(hasGotSMSDues){
								reply = MechanicsS.getMessage(MessageType.INSUFFICIENT_BALANCE, sub.getLanguage_id_(), conn);
								return reply;
							}
							
							reply = "";
							
							Question lastUnansweredQuestionSentToSub = MechanicsS.getLastQuestionSentToSub(sub.getMsisdn(),conn,false);
							
							if(lastUnansweredQuestionSentToSub!=null){
								
								final boolean isCorrect = lastUnansweredQuestionSentToSub.getAnswer().toString().equals(KEYWORD);
								
								final int points = MechanicsS.getPointsToAward(isCorrect,sub,conn);
								
								MessageType key = null;
								
								boolean isHappyHour = false;
								
								if(isHappyHour){
									
									key = (isCorrect ?  MessageType.HAPPY_HOUR_CORRECT_ANSWER : MessageType.HAPPY_HOUR_WRONG_ANSWER);
									
								}else{
									
									key = (isCorrect ?  MessageType.CORRECT_ANSWER : MessageType.WRONG_ANSWER);
								
								}
								
								
								if(verbose_logging)
									logger.debug("*.*.*.*.*.*.*.*.*.*.It's Christmas :) *.*.*.*.*.*.*.*.*.*.*.*"+MSISDN+" : points awarded for "+(isCorrect ? "Correct" : "Wrong")+ " answer. = "+points);
									
								reply = MechanicsS.getMessage(key, sub.getLanguage_id_(),conn);
								
								
								
								/**
								 * 
								 * Winning questions have origin_id = 112..
								 * 
								 */
								int isWinning = lastUnansweredQuestionSentToSub.getQuestion_origin()==112 ? 1 : 0;
								
								final TriviaLogRecord record = new TriviaLogRecord();
								record.setMsisdn(MSISDN);
								record.setName(sub.getName());
								record.setCorrect((isCorrect ? 1 : 0));
								record.setQuestion_idFK(lastUnansweredQuestionSentToSub.getId());
								record.setAnswer(KEYWORD);
								record.setPoints(points);
								record.setQuestion_origin(lastUnansweredQuestionSentToSub.getQuestion_origin());
								record.setSubscriber_profileFK(Integer.valueOf(sub.getId()));
								record.setWinningQuestion((isWinning == 1));
									
								MechanicsS.logPlay(record,conn);
								
								
								//Dear code maintainer. I am saving CPU usage. by duplicating this line of code. If its anywhere else,
								//There will be one more call to the db which is more CPU power used.
								
								//Make sure we mark the question as answered so we don't validate it against the next answer. necessary?
								MechanicsS.setAsAnswered(MSISDN,lastUnansweredQuestionSentToSub,conn);
								
									
								//Update player's shop Bucket
								//MechanicsS.updateShopBucket(sub.getMsisdn(),conn);
								
								if(MechanicsS.hasMaxedOnSet(MechanicsS.QUESTIONS_PER_MMS, MSISDN, conn)){//MechanicsS.hasMaxedOnSet(MSISDN, conn)){//(total_questions_answered%3)==0){//
									
									MechanicsS.toggleSubActive(MSISDN, false, conn);
									sub.setContinuation_confirmed(false);
								
								}
								
								
								if(!sub.isContinuation_confirmed()){//if the sub is not confirmed
									
									//final int total_questions_answered = MechanicsS.getTotalAnsweredQuestions(MechanicsS.IN_TOTAL,sub.getMsisdn(), conn);
									
									if(MechanicsS.hasMaxedOnSet(MechanicsS.QUESTIONS_PER_MMS, MSISDN, conn)){//MechanicsS.hasMaxedOnSet(MSISDN, conn)){//(total_questions_answered%3)==0){//
										
										MechanicsS.toggleSubActive(MSISDN, false, conn);
										//MechanicsS.toggleSubContinuationConfirmed(MSISDN, false, conn);
										
										final String reply1 = RM0+MechanicsS.perSonalizeMessage(reply, sub, points, conn);
										
										MOSms mosms = MOSms.clone(mo);
										mosms.setCMP_Txid(generateNextTxId());
										mosms.setMt_Sent(reply1);
										mosms.setPriority(0);//give it a higher priority
										mosms.setCMP_SKeyword(TarrifCode.RM0.getCode());
										sendMT(mosms);
										
										if(MechanicsS.hasMaxedOnSet(MechanicsS.MAX_MONEY_SPENT_PER_DAY,MSISDN, conn) && MechanicsS.hasMaxedOnSet(MechanicsS.DAILY_QUESTION_QUOTA,MSISDN, conn)){
											
											MechanicsS.toggleReachedDailyQuestionQuota(MSISDN, true, conn);
											
											//Hi <NAME>. You have reached the maximum number of participation today.  Join us again tomorrow to stand more chance to grab Nissan X-Gear & more prizes
											reply =  MechanicsS.getMessage(MessageType.PLAY_TOMORROW_CAP_REACHED, sub.getLanguage_id_(), conn);
										
											
										}else{
											
											MechanicsS.toggleSubActive(MSISDN, false, conn);
											reply = MechanicsS.getMessage(MessageType.CONTINUITY_MESSAGE_AFTER_AFTER_SET_OF_QUESTIONS, sub.getLanguage_id_(), conn);
										
										}
										//reply = MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
										
									}else{
										
										//Check if subs have answered a quota.
										
										if(MechanicsS.hasMaxedOnSet(MechanicsS.MAX_MONEY_SPENT_PER_DAY,MSISDN, conn) && MechanicsS.hasMaxedOnSet(MechanicsS.DAILY_QUESTION_QUOTA, MSISDN, conn)){//if sub has used max money usable and answered all questions.. if the sub has not answered all questions in quota, send him the next question.
											
											MechanicsS.toggleReachedDailyQuestionQuota(MSISDN, true, conn);
											//TODO don't forget a cron that re-sets daily quota
											//TODO suggest to the rest of the team that its better the quota is reset automatically every day
											//TODO This is to make it easier for people to play. It's tiring if I must activate my play every day considering that I am charged for every MO!
											
											//TODO - change this to Halijah's request to have this text as below
											//Hi <NAME>. You have reached the maximum number of participation today.  Join us again tomorrow to stand more chance to grab Nissan X-Gear & more prizes
											reply += ((reply.endsWith(".") || reply.endsWith("?") || reply.endsWith("!")) ? " ":". ") + MechanicsS.getMessage(MessageType.PLAY_TOMORROW_CAP_REACHED, sub.getLanguage_id_(), conn);
										
										
										}else{	
											
											//if(questions_answered_today<daily_quota){//if the sub has not answered all questions in quota, send him the next question.
											//Now get the next question to append in the message.
											Question questionToSend = MechanicsS.getNextQuestion(sub,conn);
											if(questionToSend!=null){	
												//Update the questions_sent table
												MechanicsS.logAsSentButNotAnswered(sub.getMsisdn(), questionToSend.getId(),conn);
											}
											
											//Now append the next question to the reply or a notification if there is no more question to ask sub. IF quota not reached
											if(questionToSend!=null){
												reply += ((reply.endsWith(".") || reply.endsWith("?") || reply.endsWith("!")) ? " ":". ") + questionToSend.getQuestion() + MechanicsS.getAnseringInstructions(sub.getLanguage_id_());
											}else{
												
												try{
													alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: Warning: No proper response", "Hi,\n\n below sub: \n\n"+ sub.toString()+"\n\n did not get a question SMS. \n\nPlease add more questions into the system.\n\nRegards");
												}catch(Exception e){
													log(e);
												}
												//reply += ((reply.endsWith(".") || reply.endsWith("?") || reply.endsWith("!")) ? " ":". ") + MechanicsS.getMessage(MessageType.NO_MORE_QUESTIONS, sub.getLanguage_id_(),conn);
											}
											
												
											
											
										}
										
										
										//reply = MechanicsS.perSonalizeMessage(reply, sub, points,conn);
										
									}
									
								}else{
									
									
									//Make sure we mark the question as answered so we don't validate it against the next answer. necessary?
									MechanicsS.setAsAnswered(MSISDN,lastUnansweredQuestionSentToSub,conn);
									
									
									
									//Check if sub has reached quota
									/*final int  questions_answered_today = MechanicsS.getTotalAnsweredQuestions(MechanicsS.TODAY,sub.getMsisdn(), conn);
									final int daily_quota = Integer.valueOf(MechanicsS.getSetting("questions_quota_per_day", conn).trim());
									*/
									
									if(!MechanicsS.hasMaxedOnSet(MechanicsS.MAX_MONEY_SPENT_PER_DAY,MSISDN, conn) && !MechanicsS.hasMaxedOnSet(MechanicsS.DAILY_QUESTION_QUOTA,MSISDN, conn)){//if the sub has not answered all questions in quota, send him the next question.
										
										
										if(MechanicsS.hasMaxedOnSet(MechanicsS.QUESTIONS_PER_MMS,MSISDN, conn))
											MechanicsS.toggleSubContinuationConfirmed(MSISDN, false, conn);
										
										
										//Now get the next question to append in the message.
										Question questionToSend = MechanicsS.getNextQuestion(sub,conn);
										if(questionToSend!=null){	
											//Update the questions_sent table
											MechanicsS.logAsSentButNotAnswered(sub.getMsisdn(), questionToSend.getId(),conn);
										}
										
										
										//Now append the next question to the reply or a notification if there is no more question to ask sub.
										if(questionToSend!=null){
											reply += (reply.endsWith(".") ? " ":". ")+questionToSend.getQuestion() + MechanicsS.getAnseringInstructions(sub.getLanguage_id_());
										}else{
											
											try{
												alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: Warning: No proper response", "Hi,\n\n below sub: \n\n"+ sub.toString()+"\n\n did not get a question SMS. \n\nPlease add more questions into the system.\n\nRegards");
											}catch(Exception e){
												log(e);
											}

										}
										
									}else{// if(questions_answered_today==daily_quota){
										
										if(MechanicsS.hasMaxedOnSet(MechanicsS.DAILY_QUESTION_QUOTA,MSISDN, conn)){
											
												//TODO set reached questions_quota_per day to true
												MechanicsS.toggleReachedDailyQuestionQuota(MSISDN, true, conn);
												//TODO don't forget a cron that re-sets daily quota
												//TODO suggest to the rest of the team that its better the quota is reset automatically every day
												//TODO This is to make it easier for people to play. It's tiring if I must activate my play every day considering that I am charged for every MO!
												//TODO - subscriber 
												reply += (reply.endsWith(".") ? " ":". ") + MechanicsS.getMessage(MessageType.DAILY_QUESTION_QUOTA_REACHED, sub.getLanguage_id_(), conn);
										
										}else{
											
											
											//TODO send question - added 16th April 2012 - at 1:46pm before taking a walk
											if(MechanicsS.hasMaxedOnSet(MechanicsS.QUESTIONS_PER_MMS,MSISDN, conn))
												MechanicsS.toggleSubContinuationConfirmed(MSISDN, false, conn);
											
											
											//Now get the next question to append in the message.
											Question questionToSend = MechanicsS.getNextQuestion(sub,conn);
											if(questionToSend!=null){	
												//Update the questions_sent table
												MechanicsS.logAsSentButNotAnswered(sub.getMsisdn(), questionToSend.getId(),conn);
											}
											
											
											//Now append the next question to the reply or a notification if there is no more question to ask sub.
											if(questionToSend!=null){
												reply += (reply.endsWith(".") ? " ":". ")+questionToSend.getQuestion() + MechanicsS.getAnseringInstructions(sub.getLanguage_id_());
											}else{
												
												try{
													alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: Warning: No proper response", "Hi,\n\n below sub: \n\n"+ sub.toString()+"\n\n did not get a question SMS. \n\nPlease add more questions into the system.\n\nRegards");
												}catch(Exception e){
													log(e);
												}

											}
											
											
											
											
										}
										
									}
									
								}
								
								
								reply = MechanicsS.perSonalizeMessage(reply, sub, points, conn);
								
								
								if(transaction_time_logging){
									watch.stop();
									logger.info(">< . >< . >< . >< . >< .KEYWORD: "+KEYWORD+" took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to execute");
									watch.reset();
								}
								
								
							}else{
								
								Question quest = MechanicsS.getNextQuestion(sub, conn);
								
								MechanicsS.logAsSentButNotAnswered(MSISDN, quest.getId(), conn);
								
								if(sub.isHas_reached_questions_quota_today() && !MechanicsS.hasMaxedOnSet(MechanicsS.DAILY_QUESTION_QUOTA, MSISDN, conn)){
									MechanicsS.toggleSubActive(MSISDN, true, conn);
									MechanicsS.toggleReachedDailyQuestionQuota(MSISDN, false, conn);
									
									reply = MechanicsS.getMessage(MessageType.WELCOME_BACK, sub.getLanguage_id_(), conn);
									reply = reply + (reply.endsWith(".") ? " ":". ") + quest.getQuestion();
									reply = RM0 + MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
									
								}else{
									reply = quest.getQuestion();
								}
								
							}
							
							return reply;
							
						}else{
							//If the subscriber has sent none of the playtime words, then we remind him of which keywords we have and what they do.
							boolean hasGotSMSDues = MechanicsS.hasUnpaidDuesToday(sub,conn);//see if they have PSAInsufficientBalance or PSANumberBarred
							
							if(hasGotSMSDues){
								reply = MechanicsS.getMessage(MessageType.INSUFFICIENT_BALANCE, sub.getLanguage_id_(), conn);
								return reply;
							}
							
							if(sub.isActive()){
								
								Question unanswered = MechanicsS.getLastQuestionSentToSub(MSISDN, conn, false);
								if(unanswered!=null){
									reply = RM0+unanswered.getQuestion() + MechanicsS.getAnseringInstructions(sub.getLanguage_id_());
									return reply;
								}
							}else{
								
								reply = RM0+MechanicsS.getMessage(MessageType.CONTINUITY_MESSAGE_AFTER_AFTER_SET_OF_QUESTIONS, sub.getLanguage_id_(), conn);
								reply = MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
								return reply;
							}
							
							
							if(!sub.isContinuation_confirmed() && MechanicsS.hasMaxedOnSet(MechanicsS.QUESTIONS_PER_MMS, MSISDN, conn)){
								reply = RM0+MechanicsS.getMessage(MessageType.CONTINUITY_MESSAGE_AFTER_AFTER_SET_OF_QUESTIONS, sub.getLanguage_id_(), conn);
								reply = MechanicsS.perSonalizeMessage(reply, sub, 0, conn);
								return reply;
							}
							
							
							reply = "";
							
							reply = MechanicsS.getMessage(MessageType.WRONG_RESPONSE, sub.getLanguage_id_(), conn);
							
							if(transaction_time_logging){
								
								watch.stop();
								logger.info(">< . >< . >< . >< . >< .KEYWORD: "+KEYWORD+" took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to execute");
								watch.reset();
							
							}
							
							//pw.write(reply);
							
							//if(verbose_logging)
								//MechanicsS.logResponse(MSISDN,reply,conn);
							
							return reply;
							
						}
						
					}
			
			}catch(Exception e){
				
				logger.error(e.getMessage(),e);
			
			}finally{
				
				try{
					
					if(conn!=null)
						conn.close();
				
				}catch(Exception e){
					
					logger.error(e.getMessage(),e);
				
				}
			
			}
				
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			
			
		}
		
		return reply;
	}
	
	
	/**
	 * Gets the connection.
	 * If it is not closed or null, return the existing connection object,
	 * else create one and return it
	 * @return java.sql.Connection object
	 */
	public synchronized Connection getCon() {
		
		Connection conn_ = null;
		
		while( true ) {
			
			try {
				while ( conn_==null || conn_.isClosed() ) {
					try {
						conn_ = ds.getConnection();//MTProducer.getConnFromDbPool();
						logger.info("created connection! ");
						return conn_;
					} catch ( Exception e ) {
						logger.warn("Could not create connection. Reason: "+e.getMessage());
						try { Thread.sleep(500); } catch ( Exception ee ) {}
					}
				}
				
				if(conn_!=null)
					return conn_;
			
			} catch ( Exception e ) {
				logger.warn("can't get a connection, re-trying");
				try { Thread.sleep(500); } catch ( Exception ee ) {}
			}
		}
		
	}
	
	public static void main(String[] args) {
		String MSG = "timo100";
		MSG = MSG.replaceAll("[^\\p{L}\\p{N}]","");
		System.out.println(MSG);
		StringEscapeUtils.escapeHtml("");
	}


}
