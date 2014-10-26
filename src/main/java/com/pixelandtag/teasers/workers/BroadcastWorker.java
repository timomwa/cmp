package com.pixelandtag.teasers.workers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.pixelandtag.axiata.teasers.producer.BroadcastApp;
import com.pixelandtag.exceptions.MessageNotSetException;
import com.pixelandtag.exceptions.NoSettingException;
import com.pixelandtag.mms.api.MM7Api;
import com.pixelandtag.mms.api.MMS;
import com.pixelandtag.mms.api.ServiceCode;
import com.pixelandtag.web.beans.BroadcastType;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.beans.Question;
import com.pixelandtag.web.beans.Subscriber;
import com.pixelandtag.web.beans.TriviaLogRecord;
import com.pixelandtag.web.triviaI.MechanicsI;
import com.pixelandtag.web.triviaImpl.MechanicsImpl;
import com.pixelandtag.web.triviaImpl.MechanicsS;

public class BroadcastWorker implements Runnable {

	private static final String RM0 = "RM0 ";
	private static final String RM1 = "RM1 ";
	private static final String ONE = "1";
	private static final String MINUS_ONE = "-1";
	private static final String ZERO = "0";
	private Logger logger = Logger.getLogger(BroadcastWorker.class);
	private boolean busy = false;
	private boolean run = true;
	private String tName;
	private Connection conn;
	private volatile MechanicsI mechanics;
	private BroadcastApp producer;
	private boolean pauzed;
	
	private String client_timezone;
	private String server_timezone;
	
	
	public BroadcastWorker(String server_timezone_, String client_timezone_, BroadcastApp prod, String name) throws NoSettingException{
		
		this.server_timezone = server_timezone_;
		
		this.client_timezone =  client_timezone_;
		
		this.producer = prod;
		
		this.tName = name;
		
		this.conn = getConnection(producer.conStr);
		
		mechanics = new MechanicsImpl(conn);
		
		setMechanics(mechanics);
		
		setRunning(true);
		
		
		
		
	}

	
	
	public boolean isBusy() {
		return busy;
	}



	private void setBusy(boolean busy) {
		this.busy = busy;
	}



	public synchronized boolean isPauzed() {
		return pauzed;
	}

	public synchronized void setPauzed(boolean pauzed) {
		this.pauzed = pauzed;
	}

	

	public synchronized MechanicsI getMechanics() {
		return mechanics;
	}

	public synchronized void setMechanics(MechanicsI mechanics) {
		this.mechanics = mechanics;
	}

	public String gettName() {
		return tName;
	}

	public void settName(String tName) {
		this.tName = tName;
	}
	
	public boolean isRunning() {
		return run;
	}

	public void setRunning(boolean run_) {
		this.run = run_;
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
	


	public void run() {
		
		
		pauze();//pause and wait for producer to finish initializing
		
		final int HOUR_NOW = mechanics.getHourNow();
		
		while(run){
			
			logger.info(gettName()+" >> : I ran!");
			
			try{
				
				setBusy(false);
				
				
				logger.info(gettName()+">>> waiting to pick subscriber");
				final Subscriber sub = BroadcastApp.getSubscriber();
				
				
				if(sub.getId() == -1){
					logger.debug(gettName()+" is shutting down...");
				}else{
					logger.info(gettName()+">>> got subscriber..");
				}
			
				setBusy(true);
				
				if(sub!=null && sub.getId() != -1){
				
					if((sub.getIdle_hours() > 2 && sub.getLanguage_id_()>-1 && sub.getSubscribed().equals(ONE)) && (HOUR_NOW==9 || HOUR_NOW==10)){//Morning teasers to ALL subscribed & have selected language at 9 and 10
				
						pushMorningTeaser(sub);
				
					}
					
					if(sub.getLanguage_id_()>-1 && (sub.getIdle_hours()==2 || sub.getIdle_hours()==4 || sub.getIdle_hours()==6)){//Subscribers who've been idle for 2,4 & 6 hours also get teased..
						pushHourlyTeaser(sub);
					}
					
					
					if(sub.getLanguage_id_()==-1 && sub.getIdle_hours()==2 && sub.getLast_teaser_id().equals(ZERO)){//who are idle and not active
						try{
							autoRegister(sub);
						}catch(Exception e){
							log(e);
						}
					}
					
				
				}
					
					
					
			}catch(Exception e){
				logger.error(e.getMessage(),e);
			}
			
			
			
			
		}
		
		setBusy(false);
		
		setRunning(false);
		
		if(!isBusy() && !isRunning())
			logger.info(">>>>>>>>>>>>>>>>>"+gettName() + " Shut down safely!");
		
		myfinalize();
		

	}
	
	
	private void autoRegister(Subscriber sub) throws MessageNotSetException {
		
		// auto - subscribe the guy..
		//We activate the sub again
		MechanicsS.toggleSubActive(sub.getMsisdn(), true, conn);
		MechanicsS.changeSubLanguage(sub.getMsisdn(), 2, getConn());// 2 is the language id for the agreed default language 
		
		//charge sub
		String msg = MechanicsS.getMessage(MessageType.CONTINUITY_CONFIRMATION, 2, getConn());
		
		ServiceCode price = ServiceCode.RM1;
		
		final TriviaLogRecord record = new TriviaLogRecord();
		record.setMsisdn(sub.getMsisdn());
		record.setName(sub.getName());
		record.setCorrect(-1);
		record.setQuestion_idFK(-1);
		record.setAnswer("MMS1");//involuntary MMS sent
		record.setPoints(5);
		if(price.equals(ServiceCode.RM1))
			record.setPrice("1.0");
		else
			record.setPrice("0.0");
		
		MechanicsS.logPlay(record,conn);//we capture the 5 points earned plus the revenue we get.
		
		//give them points?
		//get the first question and send too??
		//final Question firstQuestion = MechanicsS.getFirstQuestion(sub, conn);
		
		
		msg = MechanicsS.perSonalizeMessage(msg, sub, 5, getConn());
		
		msg = RM1+msg;
		
		logger.debug("MSISDN : ["+sub.getMsisdn()+"] sms Queued to be sent ? "+mechanics.insertIntoHTTPToSend(sub.getMsisdn(), msg));
		
		//send content
		
		final List<MMS> mmss = MechanicsS.getNextMMSs(price.getCode(), sub, conn, 1);
		
		
		if(mmss!=null){
			
			if(mmss.size()>1){
				
				for(MMS mms :  mmss){
					if(mms!=null)
						if(queueMMSForSending(mms, getConn())){
							logger.info("queued mms to be sent : "+mms.toString());
							MechanicsS.logMMSAsSent(mms, conn);
							
						}else{
							logger.warn("MMS NOT SENT! sent : "+mms.toString());
						}
					else
						logger.warn("SUBSCRIBER_DID_NOT_GET_MMS "+sub.getMsisdn());
				
				}
				
			}
			
		}
		
		
		mechanics.setTeasedAndAutoSubscribed(sub.getMsisdn());
		
	}



	private void pushHourlyTeaser(Subscriber sub) {
		
		MessageType teaserType = null;
		
		if(sub.getIdle_hours()==2)
			teaserType = MessageType.SECOND_HOUR_TEASER;
		else if(sub.getIdle_hours()==4)
			teaserType = MessageType.FOURTH_HOUR_TEASER;
		else if(sub.getIdle_hours()==6)
			teaserType = MessageType.SIXTH_HOUR_TEASER;
		
		try {
			
			String msg = mechanics.getMessage(teaserType, sub.getLanguage_id_());
			
			msg = RM0+MechanicsS.perSonalizeMessage(msg, sub, 0, getConn());
			
			mechanics.insertIntoHTTPToSend(sub.getMsisdn(), msg);
			
			mechanics.setTeased(sub.getMsisdn());
		
		} catch (MessageNotSetException e) {
			
			log(e);
		
		}
		
	}



	private void pushMorningTeaser(Subscriber sub) {
		
		try {
			
			String msg  =  mechanics.getTeasers().get(sub.getLanguage_id_()).get(MessageType.SECOND_HOUR_TEASER);
			
			msg = RM0+MechanicsS.perSonalizeMessage(msg, sub, 0, getConn());
			
			logger.info("MSISDN::::::::::::::"+sub.getMsisdn()+ " teaser >>>>>>> "+msg);
			
			mechanics.insertIntoHTTPToSend(sub.getMsisdn(), msg);
			
			mechanics.setTeased(sub.getMsisdn());
		
		} catch (MessageNotSetException e) {
			
			log(e);
		
		}
		
	}



	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}


	public String getTeaser(int questions_answered_today, int language_id){
		
		String teaser = "";
		
		if(questions_answered_today==0){
			
			teaser = producer.getTeaserSet().get(language_id).get(MessageType.FIRST_TEASER);
		
		}else if(questions_answered_today>=5){
			
			teaser = producer.getTeaserSet().get(language_id).get(MessageType.HIGH_TEASER);
			
		}else{
			
			teaser = producer.getTeaserSet().get(language_id).get(MessageType.OTHER_TEASER);
		
		}
		
		
	
		return teaser;
		
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
	
	/**
	 * Get a connection.
	 * 
	 * uses connstring: 
	 * @return
	 */
	public Connection getConn(){
		
		//Connection conn = null;
		try {
			if(conn!=null && !conn.isClosed()){
				conn.setAutoCommit(true);
			}
		} catch (SQLException e1) {
			logger.error(e1.getMessage(),e1);
		}
		
		
		try {
		
			conn = DriverManager.getConnection("jdbc:mysql://db/celcom?user=root&password=");
		
		} catch ( Exception e ) {
			
			e.printStackTrace();
			logger.error(e,e);
		
		}
	
		return conn;
	}

	
	
	public boolean queueMMSForSending(MMS mms, Connection conn) {
		
		if(mms==null)
			return false;
		
		//Connection conn = null;
		PreparedStatement pstmt = null;
		boolean success = false;

		try {
			
			
				
			pstmt =  conn.prepareStatement("INSERT INTO `celcom`.`mms_to_send`(msisdn, subject,mms_text, media_path, serviceid, shortcode, linked_id, earliest_delivery_time, expiry_date, delivery_report_requested, servicecode, timeStampOfInsertion, distributable) " +
						"VALUES(?,?,?,?,?,?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+server_timezone+"','"+client_timezone+"'),?)",Statement.RETURN_GENERATED_KEYS);
			
			
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
	
	
	public void myfinalize(){
		
		try {
			mechanics.closeConnection();
		} catch (SQLException e1) {
			log(e1);
		}
		try {
			conn.close();
		} catch (SQLException e) {
			log(e);
		}
	}
}
