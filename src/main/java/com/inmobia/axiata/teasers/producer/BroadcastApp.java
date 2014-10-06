package com.inmobia.axiata.teasers.producer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;


import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.inmobia.axiata.exceptions.MessageNotSetException;
import com.inmobia.axiata.teasers.workers.BroadcastWorker;
import com.inmobia.axiata.web.beans.MessageType;
import com.inmobia.axiata.web.beans.Question;
import com.inmobia.axiata.web.beans.Subscriber;
import com.inmobia.axiata.web.beans.TriviaLogRecord;
import com.inmobia.axiata.web.triviaI.MechanicsI;
import com.inmobia.axiata.web.triviaImpl.MechanicsImpl;
import com.inmobia.axiata.web.triviaImpl.MechanicsS;
import com.inmobia.celcom.entities.MOSms;
import com.inmobia.mms.api.MMS;
import com.inmobia.mms.api.ServiceCode;
import com.inmobia.util.StopWatch;


public class BroadcastApp {

	private static String conString;
	private Properties log4Jprops,appProperties;
	public static String SERVER_TZ = "+08:00";
	public static String CLIENT_TZ = "+08:00";
	
	private static final String RM0 = "RM0 ";
	private static final String RM1 = "RM1 ";
	private static final String ONE = "1";
	private static final String MINUS_ONE = "-1";
	private static final String ZERO = "0";
	private static int HOUR_NOW = 0;

	private Logger logger = Logger.getLogger(BroadcastApp.class);
	
	private Connection conn;
	
	public BlockingQueue<BroadcastWorker> teaserWorkers = new LinkedBlockingDeque<BroadcastWorker>();
	public Map<Integer,Map<MessageType,String>> teaserSet;
	
	public String high_teaser;
	public String other_teaser;
	public String first_teaser;
	private int processed = 0;
	private int totalRecords = 0;
	private MechanicsI mechanics;
	private boolean finished = false;
	private boolean run = false;
	public String conStr;
	private int started=0;
	private boolean isHappyHour = false;
	private String client_tz = "+08:00";
	private String server_tz = "+08:00";
	private StopWatch watch = new StopWatch();
	public static BroadcastApp instance = null;
	public static Semaphore semaphore = new Semaphore(1,true);
	
	
	
	
	/**
	 * This method tries - with all might :) not to allow more than one method to 
	 * access the MT message dequeue object.
	 * During tests, I experienced a situation where two threads got the same message
	 * from one queue..(Later I discovered it was because the messages were being returned back to the
	 * cue after the http call failed. This was a reliability feature which is cool.. so later
	 * I had to make sure that immediately after a message is put into the blocking queue, it is
	 * marked in the db as "being processed") Its like before the message was removed from the queue, another thread
	 * already took the given message...
	 * @return  com.inmobia.celcom.entities.MTsms
	 * @throws InterruptedException
	 * @throws NullPointerException
	 */
	public static Subscriber getSubscriber() throws InterruptedException, NullPointerException{
		
		try{
			
			instance.logger.info(">>Threads waiting to retrieve message before : " + semaphore.getQueueLength() );
			
			semaphore.acquire();//now lock out everybody else!
			
			
			instance.logger.info(">>Threads waiting to retrieve message after: " + semaphore.getQueueLength() );
			
			
			// final Subscriber myMt = msisdns.takeFirst();//performance issues versus reliability? I choose reliability in this case :)
			 
			// return myMt;
			return null;
		
		}finally{
			
			semaphore.release(); // then give way to the next thread trying to access this method..
			
		}
		
	}
	
	public synchronized boolean isHappyHour() {
		return isHappyHour;
	}

	public synchronized void setHappyHour(boolean isHappyHour) {
		this.isHappyHour = isHappyHour;
	}

	public synchronized int getStarted() {
		return started;
	}

	public synchronized void increaseStarted() {
		this.started++;
	}

	public boolean isFinished() {
		return finished;
	}


	/**
	 * 
	 * @param workers_
	 * @param conStr_
	 * @param bcastType_
	 * @throws Exception
	 */
	public BroadcastApp() throws Exception{
		
		
		//BasicConfigurator.configure();
		
		log4Jprops = getPropertyFile("log4jteasers.properties");
		
		appProperties = getPropertyFile("teaserapp.properties");
		
		SERVER_TZ=appProperties.getProperty("SERVER_TZ");
		
		CLIENT_TZ=appProperties.getProperty("CLIENT_TZ");
		
		PropertyConfigurator.configure(log4Jprops);
		
		conString = appProperties.getProperty("constr");
		
		try {
			
			Class.forName("com.mysql.jdbc.Driver");
		
		} catch (ClassNotFoundException e) {
			
			logger.error(e.getMessage(),e);
			
		}
		
		this.server_tz = SERVER_TZ;
		
		this.client_tz = CLIENT_TZ;
		
		this.conStr = conString;
		
		System.out.println(conString);
		
		this.conn = getConnection(this.conStr);
		
		mechanics = new MechanicsImpl(conn);
		
		HOUR_NOW = mechanics.getHourNow();
		
		
		logger.info("Hour now::::>>>>>>>>>>>> "+HOUR_NOW);
		
		// we check if we're legally supposed to be sending SMS to peeps.
		run = mechanics.isLegalTimeToSendBroadcasts();
		
		logger.info("mech.isLegalTimeToSendBroadcasts():::: Legal time for teasers? >> "+run);
		
		if(run){
			
			changeTeasers();
			
			
		}
		
		instance = this;
		
	}
	
	
	/**
	 * changes the teasers
	 * @throws MessageNotSetException
	 */
	public void changeTeasers() throws MessageNotSetException{
		teaserSet = mechanics.getTeasers();
	}
	
	
	public synchronized Map<Integer, Map<MessageType, String>> getTeaserSet() {
		return teaserSet;
	}


	public synchronized void  setTeaserSet(Map<Integer, Map<MessageType, String>> teaserSet) {
		this.teaserSet = teaserSet;
	}


	public synchronized void resetCounter(){
			processed=0;
			processed=0;
	}
	
	public synchronized void count(){
		
		++this.processed;
	}
	
	public synchronized int getCount(){
		return this.processed;
	}
	
	public void setTotalRecords(int recs){
		this.totalRecords = recs;
	}
	
	public synchronized int getTotalRecords(){
		return this.totalRecords;
	}
	
	
	
	
	
	/**
	 * Populates the msisdn queue
	 */
	private void populateMSISDNQueue(){
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			
			stmt = this.conn.createStatement();
			
			String sql = "SELECT"
				   +" sp.id as 'id',"
				   +" sp.msisdn as 'msisdn',"
				   +" sp.language_id as 'language_id',"
				   +" sp.`name` as 'name',"
				   +" sp.last_action as 'last_action',"
				   +" sp.last_teaser_id as 'last_teaser_id',"
				   +" sp.subscribed as 'subscribed',"
				   +" sp.last_teased as 'last_teased',"
				   +" sp.active as 'active',"
				   +" sp.continuation_confirmed as 'continuation_confirmed',"
				   +" sp.has_reached_questions_quota_for_today as 'has_reached_questions_quota_for_today',"
				   +" sum(tl.points) as 'points'," 
				   +" count(tl.msisdn) as 'questions_answered',"
				   +" IFNULL(TIMESTAMPDIFF(MINUTE,sp.last_teased,CONVERT_TZ(CURRENT_TIMESTAMP,'"+this.server_tz+"','"+this.client_tz+"')),0) as 'mins_since_teased',"
				   +" IFNULL(TIMESTAMPDIFF(HOUR,sp.last_teased,CONVERT_TZ(CURRENT_TIMESTAMP,'"+this.server_tz+"','"+this.client_tz+"')),0) as 'hours_since_teased',"
				   +" IF(TIMESTAMPDIFF(HOUR,last_action,CONVERT_TZ(CURRENT_TIMESTAMP,'"+this.server_tz+"','"+this.client_tz+"'))=2, 'SECOND_HOUR_TEASER', (IF(TIMESTAMPDIFF(HOUR,last_action,CONVERT_TZ(CURRENT_TIMESTAMP,'"+this.server_tz+"','"+this.client_tz+"'))=4, 'FOURTH_HOUR_TEASER', IF(TIMESTAMPDIFF(HOUR,last_action,CONVERT_TZ(CURRENT_TIMESTAMP,'"+this.server_tz+"','"+this.client_tz+"'))=6, 'SIXTH_HOUR_TEASER','NOT_TEASABLE')))) as 'teaser'," 
				   +" TIMESTAMPDIFF(HOUR,last_action,CONVERT_TZ(CURRENT_TIMESTAMP,'"+this.server_tz+"','"+this.client_tz+"')) as 'idle_hours'"

			 +" FROM" 

			 	+" `axiata_trivia`.`subscriber_profile` sp"  

			 +" LEFT JOIN"

			 	+" `axiata_trivia`.`trivia_log` tl on tl.msisdn=sp.msisdn" 

			 +" WHERE"
			 	//+" (tl.question_idFK>0 AND sp.subscribed=1 AND (TIMESTAMPDIFF(MINUTE,sp.last_teased,CONVERT_TZ(CURRENT_TIMESTAMP,'"+this.server_tz+"','"+this.client_tz+"'))>59) )"
			 +" (tl.question_idFK>0 AND (TIMESTAMPDIFF(MINUTE,sp.last_teased,CONVERT_TZ(CURRENT_TIMESTAMP,'"+this.server_tz+"','"+this.client_tz+"'))>59) )"
				+" OR (`sp`.`last_teased` IS NULL) /*HAVING idle_hours in (2,4,5,6,7)*/ group by tl.msisdn;";
		
			
			
			System.out.println(sql);
			rs = stmt.executeQuery(sql);
			
			//HOUR_NOW = 9;
								
			int i = 0;
			
			while(rs.next()){
				
				final Subscriber sub = new Subscriber();
				sub.setId(rs.getInt("id"));
				sub.setMsisdn(rs.getString("msisdn"));
				sub.setLanguage_id_(rs.getInt("language_id"));
				sub.setName(rs.getString("name"));
				sub.setLast_action(rs.getString("last_action"));
				sub.setLast_teaser_id(rs.getString("last_teaser_id"));
				sub.setSubscribed(rs.getString("subscribed"));
				sub.setLast_teased(rs.getString("last_teased"));
				sub.setActive(rs.getBoolean("active"));
				sub.setQuestionsAnsweredToday(rs.getInt("questions_answered"));
				sub.setTotalPoints(rs.getInt("points"));
				sub.setTeaserKey(rs.getString("teaser"));
				sub.setIdle_hours(rs.getInt("idle_hours"));
				sub.setContinuation_confirmed((rs.getInt("continuation_confirmed") == 1 ?  true: false));
				sub.setHas_reached_questions_quota_for_today((rs.getInt("has_reached_questions_quota_for_today")==1));
				sub.setMins_since_teased(rs.getInt("mins_since_teased"));
				sub.setHours_since_teased(rs.getInt("hours_since_teased"));
				
				
				if((i%MechanicsI.TEASERS_CHANGE_AFTER==0) || rs.isFirst())//every TEASERS_CHANGE_AFTERth subscriber, we change the teasers.
					try {
						changeTeasers();
					} catch (MessageNotSetException e) {
						log(e);
					}
					
					tease(sub);
					
					if(sub.getLanguage_id_()==-1 || sub.getSubscribed().equals(ONE))
						i++;
					
			}
			
			logger.info("fetched "+i+" subs to tease");
				
		} catch (SQLException e) {
			
			log(e);
		
		}finally{
			
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
		
	}
	
	
	
	
	private void tease(Subscriber sub) {
		
		if(sub.getMsisdn().equals("0133489823")){
			logger.info("VINNIE_0133489823 ");
			logger.info("VIN_sub.getLast_teased(): "+sub.getLast_teased());
			logger.info("is null ? sub.getLast_teased()==null ? : "+(sub.getLast_teased()==null));
			logger.info("is NULL lstring : "+sub.getLast_teased());
			logger.info(sub.toString());
		}
		
		if(sub==null)
			return;
		
		if(sub.isHas_reached_questions_quota_today() && sub.getIdle_hours()>=24){
			MechanicsS.toggleReachedDailyQuestionQuota(sub.getMsisdn(), false, conn);
			sub.setHas_reached_questions_quota_for_today(false);
		}
		
		if(sub!=null && sub.getId()!=-1 && !sub.isHas_reached_questions_quota_today()){
			
			if( ( sub.getLast_teased()==null && sub.getSubscribed().equals(ONE) ) || ( sub.getSubscribed().equals(ONE) && sub.getIdle_hours() > 1 && sub.getLanguage_id_()>-1 && sub.getHours_since_teased()>=1 && sub.getSubscribed().equals(ONE) && (HOUR_NOW==9 || HOUR_NOW==10)  )   ){//Morning teasers to ALL subscribed & have selected language at 9 and 10
		
				pushMorningTeaser(sub);
		
			}
			//Add HOUR_NOW==12 || HOUR_NOW==14 if you want to guarnatee subs get teasers with 2 hour interval
			if( (sub.getSubscribed().equals(ONE)  && sub.getLanguage_id_()>-1 && sub.getHours_since_teased()==2  || (sub.getIdle_hours()==2  || sub.getIdle_hours()==4 || sub.getIdle_hours()==6))){//Subscribers who've been idle for 2,4 & 6 hours also get teased..
				pushHourlyTeaser(sub);
			}
			
			
			if((sub.getLanguage_id_()==-1 && sub.getIdle_hours()>=4 && sub.getLast_teaser_id().equals(ZERO))){//who are idle and not active and have not been teased before
				try{
					autoRegister(sub);
				}catch(Exception e){
					log(e);
				}
			}
			
		
		}
		
	}
	
	
	
	
	private void autoRegister(Subscriber sub) throws MessageNotSetException {
		
		// auto - subscribe the guy..
		//We activate the sub again
		MechanicsS.toggleSubActive(sub.getMsisdn(), true, conn);
		MechanicsS.changeSubLanguage(sub.getMsisdn(), 2, getConnection(this.conStr));// 2 is the language id for the agreed default language 
		
		//charge sub
		String msg = MechanicsS.getMessage(MessageType.CONTINUITY_CONFIRMATION, 2, getConnection(this.conStr));
		
		ServiceCode price = ServiceCode.RM0;
		
		final TriviaLogRecord record = new TriviaLogRecord();
		record.setMsisdn(sub.getMsisdn());
		record.setName(sub.getName());
		record.setCorrect(-1);
		record.setQuestion_idFK(-1);
		record.setSubscriber_profileFK(Integer.valueOf(sub.getId()));
		record.setAnswer("RM1");//involuntary MMS sent
		record.setPoints(5);
		//if(price.equals(ServiceCode.RM1))
			record.setPrice("1.0");
		//else
			//record.setPrice("0.0");
		
		MechanicsS.logPlay(record,conn);//we capture the 5 points earned plus the revenue we get.
		
		MOSms mo = new MOSms();
		
		
			mo.setServiceid(2);
			mo.setSUB_Mobtel(sub.getMsisdn());
			mo.setCMP_Txid(MechanicsS.generateNextTxId());
			mo.setCMP_AKeyword("TRIVIA");
			mo.setCMP_SKeyword("IOD0100");
			mo.setPrice(1.0);
			
			MechanicsS.toStatsLog(mo, conn);
		
		
		
		//give them points?
		//get the first question and send too??
		//final Question firstQuestion = MechanicsS.getFirstQuestion(sub, conn);
		
		Question question = MechanicsS.getFirstQuestion(sub, getConnection(conStr));
		
		if(question!=null){
			msg += ((msg.endsWith(".") || msg.endsWith("?") || msg.endsWith("!")) ? " ":". ") + question.getQuestion() + MechanicsS.getAnseringInstructions(sub.getLanguage_id_());
			MechanicsS.logAsSentButNotAnswered(sub.getMsisdn(), question.getId(), getConnection(conStr));
		}
		
		
		msg = MechanicsS.perSonalizeMessage(msg, sub, 5, getConnection(this.conStr));
		
		
		
		msg = RM1+msg;
		
		
		
		logger.debug("MSISDN : ["+sub.getMsisdn()+"] sms Queued to be sent ? "+mechanics.insertIntoHTTPToSend(sub.getMsisdn(), msg, mo.getCMP_Txid()));
		
		//send content
		
		final List<MMS> mmss = MechanicsS.getNextMMSs(price.getCode(), sub, conn, 1);
		
		
		if(mmss!=null){
			
			if(mmss.size()>0){
				
				for(MMS mms :  mmss){
					if(mms!=null){
						
						mms.setPaidFor(false);
						mms.setLinked_id(mo.getCMP_Txid());
						mms.setWait_for_txId(mo.getCMP_Txid());
						
						if(queueMMSForSending(mms,  getConnection(this.conStr))){
							logger.info("mo.getCMP_Txid():::::::::::::: "+mo.getCMP_Txid()+" queued mms to be sent : "+mms.toString());
							MechanicsS.logMMSAsSent(mms, conn);
							
						}else{
							logger.warn("MMS NOT SENT! sent : "+mms.toString());
						}
					}else{
						logger.warn("SUBSCRIBER_DID_NOT_GET_MMS "+sub.getMsisdn());
					}
				}
				
			}
			
		}
		
		
		mechanics.setTeasedAndAutoSubscribed(sub.getMsisdn());
		
	}





	private void pushHourlyTeaser(Subscriber sub) {
		
		MessageType teaserType = null;
		
		if(sub.getIdle_hours()==2 || sub.getHours_since_teased()==2)
			teaserType = MessageType.SECOND_HOUR_TEASER;
		else if(sub.getIdle_hours()==4  || sub.getHours_since_teased()==4)
			teaserType = MessageType.FOURTH_HOUR_TEASER;
		else if(sub.getIdle_hours()==6  || sub.getHours_since_teased()==6)
			teaserType = MessageType.SIXTH_HOUR_TEASER;
		else 
			teaserType = MessageType.SECOND_HOUR_TEASER;// we should have another teaser.
		
		try {
			
			if(sub.getLanguage_id_()==-1)
				sub.setLanguage_id_(2);
			
			String msg =  mechanics.getTeasers().get(sub.getLanguage_id_()).get(teaserType);//mechanics.getMessage(teaserType, sub.getLanguage_id_());
			
			logger.debug("GOT_TEASER_FROM_CACHE >>"+msg);
			
			msg = RM0+MechanicsS.perSonalizeMessage(msg, sub, 0,  getConnection(this.conStr));
			
			
			
			MOSms mo = new MOSms();
			
			String txid = MechanicsS.generateNextTxId();
			mo.setServiceid(2);
			mo.setSUB_Mobtel(sub.getMsisdn());
			mo.setCMP_Txid(txid);
			mo.setCMP_AKeyword("TRIVIA");
			mo.setCMP_SKeyword("IOD0000");
			mo.setPrice(0);
			
			MechanicsS.toStatsLog(mo, conn);
			
			mechanics.insertIntoHTTPToSend(sub.getMsisdn(), msg,txid);
			mechanics.setTeased(sub.getMsisdn());
			
			
			Question question = MechanicsS.getNextQuestion(sub, getConnection(conStr));
			
			if(question!=null){
				msg =  question.getQuestion()+ MechanicsS.getAnseringInstructions(sub.getLanguage_id_());
				MechanicsS.logAsSentButNotAnswered(sub.getMsisdn(), question.getId(), getConnection(conStr));
				msg = RM0+MechanicsS.perSonalizeMessage(msg, sub, 0,  getConnection(this.conStr));
				
				mo = new MOSms();
				
				txid = MechanicsS.generateNextTxId();
				mo.setServiceid(2);
				mo.setSUB_Mobtel(sub.getMsisdn());
				mo.setCMP_Txid(txid);
				mo.setCMP_AKeyword("TRIVIA");
				mo.setCMP_SKeyword("IOD0000");
				mo.setPrice(0);
				
				MechanicsS.toStatsLog(mo, conn);
				mechanics.insertIntoHTTPToSend(sub.getMsisdn(), msg, txid);
			}
			
			
		
		
		} catch (MessageNotSetException e) {
			
			log(e);
		
		}catch (Exception e) {
			
			log(e);
			logger.info(sub.toString());
		
		}
		
	}


	private void pushMorningTeaser(Subscriber sub) {

		
		try {
			
			String msg  =  "";
			
			//TODO in main query, get this unanswered question so you reduce 60,000 db calls
			Question unansweredQuestion = MechanicsS.getLastQuestionSentToSub(sub.getMsisdn(), conn, false);
			
			
			if(unansweredQuestion!=null){
				
				if(sub.getLanguage_id_()==-1)
					sub.setLanguage_id_(2);
				
				msg  =  mechanics.getTeasers().get(sub.getLanguage_id_()).get(MessageType.SECOND_HOUR_TEASER);
				
				logger.info("GOT_TEASER_FROM_CACHE >>>"+msg);
				
				msg += ( (msg.endsWith(".") || msg.endsWith("!") || msg.endsWith(".") ) ? " " : ". " ) + unansweredQuestion.getQuestion();
				
				msg = RM0+MechanicsS.perSonalizeMessage(msg, sub, 0, getConnection(this.conStr));
				
				logger.info("MSISDN::::::::::::::"+sub.getMsisdn()+ " teaser >>>>>>> "+msg);
			
			}else{
				
				msg  = MechanicsS.getMessage(MessageType.CONTINUITY_MESSAGE_AFTER_AFTER_SET_OF_QUESTIONS, sub.getLanguage_id_(), getConnection(this.conStr));
				
				msg = RM0+MechanicsS.perSonalizeMessage(msg, sub, 0, getConnection(this.conStr));
				
				logger.info("MSISDN::::::::::::::"+sub.getMsisdn()+ " teaser >>>>>>> "+msg);
				
			}
			
			
			MOSms mo = new MOSms();
			
			String txid = MechanicsS.generateNextTxId();
			mo.setServiceid(2);
			mo.setSUB_Mobtel(sub.getMsisdn());
			mo.setCMP_Txid(txid);
			mo.setCMP_AKeyword("TRIVIA");
			mo.setCMP_SKeyword("IOD0000");
			mo.setPrice(0);
			
			MechanicsS.toStatsLog(mo, conn);
			
			mechanics.insertIntoHTTPToSend(sub.getMsisdn(), msg,MechanicsS.generateNextTxId());
			
			mechanics.setTeased(sub.getMsisdn());
		
		} catch (MessageNotSetException e) {
			
			log(e);
		
		}catch (Exception e) {
			
			log(e);
			
			logger.info(sub.toString());
		
		}
		
	}



	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}

	
	
public boolean queueMMSForSending(MMS mms, Connection conn) {
		
		if(mms==null)
			return false;
		
		//Connection conn = null;
		PreparedStatement pstmt = null;
		boolean success = false;

		try {
			
			
				
			pstmt =  conn.prepareStatement("INSERT INTO `celcom`.`mms_to_send`(msisdn, subject,mms_text, media_path, serviceid, shortcode, linked_id, earliest_delivery_time, expiry_date, delivery_report_requested, servicecode, timeStampOfInsertion, distributable, paidFor,tx_id_waiting_to_succeed_before_sending) " +
					"VALUES(?,?,?,?,?,?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+server_tz+"','"+client_tz+"'),?,?,?)",Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1,mms.getMsisdn());
			pstmt.setString(2,mms.getSubject());
			pstmt.setString(3,mms.getMms_text());
			pstmt.setString(4,mms.getMediaPath());
			pstmt.setInt(5,mms.getServiceid());
			pstmt.setString(6,mms.getShortcode());
			pstmt.setString(7,mms.getLinked_id());
			pstmt.setString(8,mms.getEariest_delivery_time());
			pstmt.setString(9,mms.getExpiry_date());
			pstmt.setInt(10,(mms.getDelivery_report_requested().equals("true")? 1 : 0));
			pstmt.setString(11,mms.getServicecode());
			pstmt.setInt(12,(mms.getDistribution_indicator().equals("true")? 1 : 0));
			pstmt.setInt(13,(mms.isPaidFor() ? 1 : 0));
			pstmt.setString(14,mms.getWait_for_txId());
			
			pstmt.executeUpdate();
			
			success = true;
			
		} catch (SQLException e) {
			
			log(e);
			
		} finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				log(e);
			
			}
			
		}
		
		return success;
	}


	public void run() {
		
		if(run)
			populateMSISDNQueue();
		
		
		myfinalize();
		
		logger.info("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::FINALIZED:::::::::::::::::::::::::::::::::::::::::::::::::::::");

		
	}
	
	
	public synchronized void pauze(){
		
		try {
			
			this.wait();
			
		} catch (InterruptedException e) {
			
			logger.info("we were interrupted "+e.getMessage());
			
		}
	}
	
	
	public synchronized void rezume(){
		
		this.notify();
		
	}
	
	
	public void myfinalize(){
		
		
		try {
			
			if(this.conn!=null)
				this.conn.close();
		
		} catch (SQLException e) {
			
			log(e);
		
		}
		
		try {
			
			if(mechanics!=null)
				mechanics.closeConnection();
		
		} catch (SQLException e) {
			
			log(e);
		
		}
		
		logger.info("finalize called: >< . >< . >< . >< . >< . >< . >< . >< . >< . >< . >< . ><");
		
		
	}
	
	
	/**
	 * Gets a connection object.
	 * If the field Connection object is not null and is not closed,
	 * then it is returned. Else a new one is made and returned.
	 * @return
	 */
	public synchronized Connection getConnection(String conStr) {
		
		try {
			if(conn !=null)
				if(!conn.isClosed())
					return conn;
		} catch (SQLException e1) {
			logger.error(e1,e1);
		}
		//System.out.println(conStr);

		while( true ) {
			try {
				while ( conn==null || conn.isClosed() ) {
					try {
						conn = DriverManager.getConnection(conStr);
					} catch ( Exception e ) {
						logger.warn(e,e);
						try { Thread.sleep(500); } catch ( Exception ee ) {}
					}
				}
				return conn;
			} catch ( Exception e ) {
				logger.error(e,e);
				try { Thread.sleep(500); } catch ( Exception ee ) {}
			}
		}
	}
	
	
	
	public static void main(String[] args){
		
		
		try {
			
			new BroadcastApp().run();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	
	
	
	
	/**
	 * Creates a java.util.Properties object from 
	 * the file specified by the param filename
	 * @param filename the name of the properties file
	 * @return java.util.Properties ojbect created and populated
	 *          with the property-values set on the file "filename"
	 */
	public Properties getPropertyFile(String filename) {

		Properties prop = new Properties();
		InputStream inputStream = null;
		
		String path;
		try {
			path = System.getProperty("user.dir")
					+ System.getProperty("file.separator") + filename;
			inputStream = new FileInputStream(path);
		} catch (Exception e) {
			URL urlpath = new String().getClass().getResource(filename);
			try {
				inputStream = new FileInputStream(urlpath.getPath());
			} catch (Exception exb) {
				logger.error(filename + " not found!");
			}
		}
		try {
			if (inputStream != null) {
				prop.load(inputStream);
				inputStream.close();

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return prop;
	}
	
	
	public void initialize(){
		
		
		
	}
	
	

	

}
