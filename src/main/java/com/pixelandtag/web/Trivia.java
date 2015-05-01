package com.pixelandtag.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.ejb.CMPResourceBean;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.connections.ConnectionPool;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.util.StopWatch;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.beans.Question;
import com.pixelandtag.web.beans.RequestObject;
import com.pixelandtag.web.beans.Subscriber;
import com.pixelandtag.web.beans.TriviaLogRecord;
import com.pixelandtag.web.triviaI.MechanicsI;
import com.pixelandtag.web.triviaImpl.MechanicsImpl;
import com.pixelandtag.web.triviaImpl.MechanicsS;

/**
 * Servlet implementation class Trivia
 */
public class Trivia extends HttpServlet {
	
	private static final long serialVersionUID = 13423L;
	private DataSource ds;
	private Context initContext;
	private StopWatch watch;
	private MechanicsS mechanics = null;
	private ConnectionPool connectionPool;
	private final int INITIAL_CONNECTIONS = 10;
	private final int MAX_CONNECTIONS = 50;
	private Logger logger = Logger.getLogger(Trivia.class);
	

	@EJB
	private CMPResourceBeanRemote cmpresource;
	
	@Override
	public void init() throws ServletException {
		
		watch = new StopWatch();
		
		try {
			
			initContext = new InitialContext();
			
			ds = (DataSource)initContext.lookup("java:/cmpDS");
			
			
		
		} catch (NamingException e) {
			
			logger.error(e.getMessage(),e);
		
		}
		
		//mechanics = new MechanicsS();
		
		
		/*watch.start();
		int vendor = DriverUtilities.MYSQL;
	    String driver = DriverUtilities.getDriver(vendor);
	    String host = "db";
	    String dbName = MechanicsI.DATABASE;
	    String url = DriverUtilities.makeURL(host, dbName, vendor);
	    String username = "root";
	    String password = "";
	    
	    logger.info(url);
	    
	    try {
	    	
	      connectionPool =
	        new ConnectionPool(driver, url, username, password,INITIAL_CONNECTIONS,MAX_CONNECTIONS,true);
	      
	      watch.stop();
	      
	      logger.info(">< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to create the connection pool");
	      watch.reset();
	      
	    
	    } catch(SQLException sqle) {
	     
	    	logger.error("Error making pool: " + sqle);
	      
	    	connectionPool = null;
	    
	    }*/
	
	}
	

	@Override
	public void destroy() {
		
		logger.info("AXIATA: in Destroy");
		
		//connectionPool.closeAllConnections();
		try {
			
			initContext.close();
		
		} catch (NamingException e) {
			
			logger.error(e.getMessage(),e);
		
		}
		
	}




    /**
     * Default constructor. 
     */
    public Trivia() {
        //super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter pw = null;
		Connection conn = null;
		
		
		
		try {
			
			final RequestObject ro = new RequestObject(request,cmpresource.generateNextTxId());
			final String MSISDN = ro.getMsisdn();
			final String MSG = ro.getMsg();
			final String KEYWORD  = ro.getKeyword();
			final String SERVICEID = String.valueOf(ro.getServiceid());
			final String TELCOID = ro.getTelcoid();
			final String SERVICEACTIVE = ro.getServiceActive();
			final String LITMUS = ro.getLitmus();
			final int PRICE = ro.getPrice();
			final String TRIPWIRE = ro.getTripWire();
			
			
			
			conn = getConn();
			
			final boolean verbose_logging = Boolean.valueOf(MechanicsS.getSetting("verbose_logging", conn).trim());
			final boolean transaction_time_logging = Boolean.valueOf(MechanicsS.getSetting("transaction_time_logging", conn).trim());
			
			if(transaction_time_logging)
				watch.start();
			
			pw = response.getWriter();
			
			String reply = "";
			
			final Subscriber sub = MechanicsS.getSubscriber(MSISDN,conn);
			
			final boolean isNew = ((sub == null) ? true: false);
			
			MessageType msgKey = null;
			
			
			if(verbose_logging)
				MechanicsS.loginTraffic(MSISDN,"MSG: "+MSG+" KEYWORD: "+KEYWORD,conn);
			
			if(isNew){
				
				//This a new subscriber.
				final Subscriber newSub = new Subscriber();
				
				//If they send A or B, STOP or START, then ask them to register first.
				if(KEYWORD.equals("A") || KEYWORD.equals("B") || KEYWORD.equals("STOP") || KEYWORD.equals("START")){
					
					reply =  MechanicsS.getMessage(MessageType.REGISTER_FIRST, MechanicsI.DEFAULT_LANGUAGE_ID,conn);
					
					
					if(transaction_time_logging){
						watch.stop();
						logger.info(">< . >< . >< . >< . >< .KEYWORD: "+KEYWORD+" took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to execute");
						watch.reset();
					}
					
					pw.write(reply);
					
					if(verbose_logging)
						MechanicsS.logResponse(MSISDN,reply,conn);
					
					return;
					
				}else{
					
					int languageID = MechanicsS.getLanguageID(KEYWORD,conn);
					
					int b = MechanicsS.registerSubscriber(MSISDN, MSG,languageID,conn);
					
					boolean regSuccess = b > -1;
					
					newSub.setMsisdn(MSISDN);//Set the msisdn for our new subscriber
					newSub.setLanguage_id_(languageID);//Set the language ID
					newSub.setName(MSG);//Hopefully the name is from the second string - msg
					
					msgKey = regSuccess ? MessageType.WELCOME_MESSAGE : MessageType.REGISTRATION_FAILURE;
					
					Question firstQuestion = null;
					
					if(regSuccess){
						Subscriber sub1 = new Subscriber();
						sub1.setMsisdn(MSISDN);
						sub1.setLanguage_id_(languageID);
						firstQuestion = MechanicsS.getFirstQuestion(sub,conn);
					}
					
					reply = MechanicsS.getMessage(msgKey ,languageID,conn);
					
					logger.debug(msgKey.toString()+" : "+reply);
					
					//Now that we have some info about the sub, why don't we reply with  
					//a nice message
					reply = MechanicsS.perSonalizeMessage(reply,newSub,0,conn);
					
					logger.debug("after processing" +msgKey.toString()+" : "+reply);
					
					reply = reply+" "+(firstQuestion==null ? "" : firstQuestion.getQuestion());
					
					if(transaction_time_logging){
						watch.stop();
						logger.info(">< . >< . >< . >< . >< .KEYWORD: "+KEYWORD+" took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to execute");
						watch.reset();
					}
					
					pw.write(reply);//+":"+reply.length());
					
					if(verbose_logging)
						MechanicsS.logResponse(MSISDN,reply,conn);
					
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
						
						boolean logRegistration = MechanicsS.logPlay(record,conn);
						
						//Update the questions_sent table
						boolean questionMarkedAsSent = MechanicsS.logAsSentButNotAnswered(newSub.getMsisdn(), firstQuestion.getId(),conn);
						
						logger.debug("IS FIRST QUESTOION LOGGED ? "+questionMarkedAsSent);
						
					}
					
					
					return;
					
				}
				
				
			}else{
				
				//Check if the sub is blacklisted
				boolean isBlacklisted = MechanicsS.isInBlacklist(MSISDN,conn);
				if(isBlacklisted){
					reply = MechanicsS.getMessage(MessageType.BLACKLISTED, sub.getLanguage_id_(),conn);
					
					
					if(transaction_time_logging){
						watch.stop();
						logger.info(">< . >< . >< . >< . >< .KEYWORD: "+KEYWORD+" took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to execute");
						watch.reset();
					}
					
					
					pw.write(reply);
					
					if(verbose_logging)
						MechanicsS.logResponse(MSISDN,reply,conn);
					
					return;
				}
				
				
				
				//is not a new sub
				//logger.info("=======================\n"+sub.toString()+"\n===============================");
				
				logger.info("KEYWORD: "+KEYWORD);
				
				if(KEYWORD.equals("A") || KEYWORD.equals("B")){
					
					MechanicsS.updateLastAction(MSISDN,conn);
					
					
					final boolean subscriber_had_Opted_Out_And_Now_Is_Back = sub.isActive()==false;
					
					if(subscriber_had_Opted_Out_And_Now_Is_Back){
						
						MechanicsS.toggleSubActive(MSISDN, true,conn);
						String welcomeBack = MechanicsS.getMessage(MessageType.WELCOME_BACK, sub.getLanguage_id_(),conn);
						welcomeBack = MechanicsS.perSonalizeMessage(welcomeBack, sub, 0,conn);
						
						logger.info("welcomeBack:  "+welcomeBack);
						//TODO insert msg into vas.SMPPtoSend
					}
					
					
					reply = "";
					
					Question lastUnansweredQuestionSentToSub = MechanicsS.getLastQuestionSentToSub(sub.getMsisdn(),conn, false);
					
					if(lastUnansweredQuestionSentToSub!=null){
						
						//logger.info("Qs: "+lastUnansweredQuestionSentToSub.toString());
						
						boolean isCorrect = lastUnansweredQuestionSentToSub.getAnswer().toString().equals(KEYWORD);//MechanicsS.validateAnswer(KEYWORD,sub);
						
						
						MessageType key = null;
						boolean isHappyHour = MechanicsS.isHappyHour(conn);
						
						//MechanicsS.setHappyHour(isHappyHour);//Make sure that happy hour is set
						
						if(isHappyHour){
							
							key = (isCorrect ?  MessageType.HAPPY_HOUR_CORRECT_ANSWER : MessageType.HAPPY_HOUR_WRONG_ANSWER);
							
						}else{
							
							key = (isCorrect ?  MessageType.CORRECT_ANSWER : MessageType.WRONG_ANSWER);
						
						}
						
						int points = MechanicsS.getPointsToAward(isCorrect,sub,conn);
						
						logger.info("*.*.*.*.*.*.*.*.*.*.It's Christmas :) *.*.*.*.*.*.*.*.*.*.*.*"+MSISDN+" : points awarded for "+(isCorrect ? "Correct" : "Wrong")+ " answer. = "+points);
							
						reply = MechanicsS.getMessage(key, sub.getLanguage_id_(),conn);
						
						//log this transaction on trivia_log
						TriviaLogRecord record = new TriviaLogRecord();
						record.setMsisdn(MSISDN);
						record.setName(sub.getName());
						record.setCorrect((isCorrect ? 1 : 0));
						record.setQuestion_idFK(lastUnansweredQuestionSentToSub.getId());
						record.setAnswer(KEYWORD);
						record.setPoints(points);
							
						MechanicsS.logPlay(record,conn);
							
						//Update player's shop Bucket
						MechanicsS.updateShopBucket(sub.getMsisdn(),conn);
						
						
						//Make sure we mark the question as answered so we don't validate it against the next answer. necessary?
						MechanicsS.setAsAnswered(MSISDN,lastUnansweredQuestionSentToSub,conn);
						
							
						//Now get the next question to append in the message.
						Question questionToSend = MechanicsS.getNextQuestion(sub,conn);
						
						if(questionToSend!=null){	
							//Update the questions_sent table
							MechanicsS.logAsSentButNotAnswered(sub.getMsisdn(), questionToSend.getId(),conn);
							
						}
							
						//Now append the next question to the reply.
						if(questionToSend!=null){
							reply += (reply.endsWith(".") ? " ":". ")+questionToSend.getQuestion();
						}else{
							reply += (reply.endsWith(".") ? " ":". ")+MechanicsS.getMessage(MessageType.NO_MORE_QUESTIONS, sub.getLanguage_id_(),conn);
						}
						
						reply = MechanicsS.perSonalizeMessage(reply, sub, points,conn);
						
						
						if(transaction_time_logging){
							watch.stop();
							logger.info(">< . >< . >< . >< . >< .KEYWORD: "+KEYWORD+" took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to execute");
							watch.reset();
						}
						pw.write(reply);
						
						if(verbose_logging)
							MechanicsS.logResponse(MSISDN,reply,conn);
						
						
						
					}else{
						
						reply += MechanicsS.getMessage(MessageType.NO_MORE_QUESTIONS, sub.getLanguage_id_(),conn);
						
						reply = MechanicsS.perSonalizeMessage(reply, sub, 0,conn);
						
						
						if(transaction_time_logging){
							
							watch.stop();
							logger.info(">< . >< . >< . >< . >< .KEYWORD: "+KEYWORD+" took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to execute");
							watch.reset();
						
						}
						
						pw.write(reply);
						
						if(verbose_logging)
							MechanicsS.logResponse(MSISDN,reply,conn);
						
						logger.info(MSISDN+" has answered all questions!");
						
					}
					
					return;
					
				}else if(KEYWORD.equals("STOP") || KEYWORD.equals("START")){
					
					final boolean wantsOut = (KEYWORD.equals("STOP") ? true : false);
					
					final boolean activate = wantsOut ? false : true;
					
					final MessageType key = (wantsOut ? MessageType.STOP_MESSAGE : MessageType.WELCOME_BACK);
					
					Question questionToSend = null;
					
					if(!wantsOut && !sub.isActive()){//If keyword==START and the sub is active, then send them a question. It could be the last question we sent to them, or if they had answered that one, we send them another
						
						questionToSend = MechanicsS.getLastQuestionSentToSub(MSISDN,conn,false);
						
						boolean questionSent = false;
						
						if(questionToSend==null){	
							
							questionToSend = MechanicsS.getNextQuestion(sub,conn);
							
							//Update the questions_sent table, because its a new question we got for him
							if(questionToSend!=null)//Make sure we have got more questions to send
								questionSent = MechanicsS.logAsSentButNotAnswered(sub.getMsisdn(), questionToSend.getId(),conn);
						}
						
					}
					
					MechanicsS.toggleSubActive(MSISDN,activate,conn);
					
					reply = MechanicsS.getMessage(key, sub.getLanguage_id_(),conn);
					
					//Now append the next question to the reply if the user is wanting back in.
					if(wantsOut==false){
						
						if(questionToSend!=null){
							
							reply += (reply.endsWith(".") ? " ":". ")+questionToSend.getQuestion();
							
						
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
					
					pw.write(reply);
					
					if(verbose_logging)
						MechanicsS.logResponse(MSISDN,reply,conn);
					
					return;
					
				}else if(KEYWORD.equals("POINT") || KEYWORD.equals("POINTS")){
					
					reply = "";
					
					reply = MechanicsS.getMessage(MessageType.POINTS_REQUEST, sub.getLanguage_id_(),conn);
					
					reply = MechanicsS.perSonalizeMessage(reply, sub, 0,conn);
					
					if(transaction_time_logging){
						watch.stop();
						logger.info(">< . >< . >< . >< . >< .KEYWORD: "+KEYWORD+" took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to execute");
						watch.reset();
					}
					
					pw.write(reply);
					
					if(verbose_logging)
						MechanicsS.logResponse(MSISDN,reply,conn);
					
					return;
				
				}else{
					//If the subscriber has sent none of the playtime words, then we remind him of which keywords we have and what they do.
					
					reply = "";
					
					reply = MechanicsS.getMessage(MessageType.WRONG_RESPONSE, sub.getLanguage_id_(), conn);
					
					if(transaction_time_logging){
						
						watch.stop();
						logger.info(">< . >< . >< . >< . >< .KEYWORD: "+KEYWORD+" took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to execute");
						watch.reset();
					
					}
					
					pw.write(reply);
					
					if(verbose_logging)
						MechanicsS.logResponse(MSISDN,reply,conn);
					
					return;
					
				}
				
			}
				
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			if(pw!=null)
				pw.close();
			
				try {
					
					if(connectionPool!=null){
						
						if(conn!=null)
							connectionPool.free(conn);
					
					}else{
						
						if(conn!=null)
							conn.close();
					
					}
				
				} catch (Exception e) {
					
					logger.error(e.getMessage(),e);
				
				}
		
				if(connectionPool!=null)
					logger.info(">< . >< . >< . >< . >< . "+connectionPool.toString());
		
		}
		
	}
	
	
	
	/**
	 * 
	 * Get a connection using an already created datasource to improve performace
	 * see : http://www.ibm.com/developerworks/websphere/library/bestpractices/reusing_data_sources_for_jdbc_connections.html
	 * 
	 * @return java.sql.Connection
	 * @throws SQLException 
	 */
	public Connection getConn() throws SQLException{
		
		//return connectionPool.getConnection();
		return ds.getConnection();
	
	}


}
