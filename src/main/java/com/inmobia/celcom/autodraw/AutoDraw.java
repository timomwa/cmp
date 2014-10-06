package com.inmobia.celcom.autodraw;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import javax.annotation.Generated;
import javax.mail.Part;



import org.apache.commons.mail.MultiPartEmail;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.inmobia.axiata.exceptions.MessageNotSetException;
import com.inmobia.axiata.exceptions.NoSettingException;
import com.inmobia.axiata.web.beans.MessageType;
import com.inmobia.axiata.web.beans.Question;
import com.inmobia.axiata.web.beans.Subscriber;
import com.inmobia.axiata.web.triviaI.MechanicsI;
import com.inmobia.axiata.web.triviaImpl.MechanicsImpl;
import com.inmobia.axiata.web.triviaImpl.MechanicsS;
import com.inmobia.celcom.sms.application.HTTPMTSenderApp;
import com.inmobia.util.DateConstraint;

public class AutoDraw {
	
	private static final String RM0 = "RM0 ";
	private static final String RM1 = "RM1 ";
	private static String DRAW_DURATION = null;
	private Logger logger = Logger.getLogger(AutoDraw.class);
	private Connection conn = null;
	private List<NameValuePair> qparams = null;
	private volatile UrlEncodedFormEntity entity;
	private volatile HttpEntity resEntity;
	private volatile HttpResponse response;
	private Alarm alarm = new Alarm();
	/**
	 * pre_draw_size = number of subscribers to be pre-drawn
	 */
	private int pre_draw_size = 100;
	private int HOUR_NOW = 0;
	private int select_after = 3;
	private MechanicsI mechanics;
	private Properties appProperties;
	private String conStr = "jdbc:mysql://db/axiata_trivia?user=root&password=";
	private volatile static ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
	private volatile static HttpClient httpclient;
	private final String DAILY_PRE_DRAW_QUERY = "SELECT" 
									+" sp.*,"
								    +" IFNULL(sum(tl.points),0) as 'total_points'" 
								    +" FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` sp" 
									+" LEFT JOIN" 
								    +" `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`trivia_log` tl on tl.msisdn = sp.msisdn"
								    +" WHERE " 
								    +" (tl.timeStamp between TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)) AND DATE_SUB(TIMESTAMP(CURRENT_DATE), INTERVAL 1 SECOND)  )" 
								    +" AND sp.msisdn not in (SELECT msisdn from `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`blacklist`) AND sp.msisdn in (select msisdn FROM celcom.SMSStatLog WHERE charged=1 AND CMP_SKeyword='IOD0100' AND statusCode='Success' AND charged=1 AND timeStamp between TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)) AND DATE_SUB(TIMESTAMP(CURRENT_DATE), INTERVAL 1 SECOND) )"
								    +" group by tl.msisdn HAVING total_points >0 ORDER BY total_points desc";
	
	private final String DAILY_FINAL_DRAW_QUERY = "SELECT sp.*, IFNULL(sum(tl.points),0) as 'total_points'  FROM `axiata_trivia`.`trivia_log` tl " +
			"LEFT JOIN `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` sp " +
			"ON sp.msisdn = tl.msisdn WHERE  tl.winning_question=1 " +
			"AND tl.correct=1 AND answer in ('A','B','MMS') " +
			"AND sp.msisdn not in (SELECT msisdn from `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`blacklist`)"+
			"AND `timeStamp` between TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)) AND DATE_SUB(TIMESTAMP(CURRENT_DATE), INTERVAL 1 SECOND) group by tl.msisdn HAVING total_points >0 ORDER BY RAND()";
	
	
	
	
	private final String WEEKLY_PRE_DRAW_QUERY = "SELECT" 
		+" sp.*,"
	    +" IFNULL(sum(tl.points),0) as 'total_points'" 
	    +" FROM `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` sp" 
		+" LEFT JOIN" 
	    +" `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`trivia_log` tl on tl.msisdn = sp.msisdn"
	    +" WHERE " 
	    +" (tl.timeStamp between TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 WEEK)) AND DATE_SUB(TIMESTAMP(CURRENT_DATE), INTERVAL 1 SECOND)  )" 
	    +" AND sp.msisdn not in (SELECT msisdn from `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`blacklist`)  AND sp.msisdn in (select msisdn FROM celcom.SMSStatLog WHERE charged=1 AND CMP_SKeyword='IOD0100' AND statusCode='Success' AND charged=1 AND timeStamp between TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 WEEK)) AND DATE_SUB(TIMESTAMP(CURRENT_DATE), INTERVAL 1 SECOND) )"
	    +" group by tl.msisdn HAVING total_points >0 ORDER BY total_points desc";

	private final String WEEKLY_FINAL_DRAW_QUERY = "SELECT sp.*, IFNULL(sum(tl.points),0) as 'total_points' FROM `axiata_trivia`.`trivia_log` tl " +
			"LEFT JOIN `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`subscriber_profile` sp " +
			"ON sp.msisdn = tl.msisdn WHERE answer>-1 " +
			"AND correct=1 AND answer in ('A','B','MMS') " +
			"AND sp.msisdn not in (SELECT msisdn from `"+HTTPMTSenderApp.props.getProperty("DATABASE")+"`.`blacklist`)"+
			"AND `timeStamp` between TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 WEEK)) AND DATE_SUB(TIMESTAMP(CURRENT_DATE), INTERVAL 1 SECOND) group by tl.msisdn HAVING total_points >0 ORDER BY RAND()";


	
	
	private int points = 0;
	
	private Vector<Subscriber> winners = new Vector<Subscriber>();
	private Integer hour_to_auto_draw = 0;
	private int final_daily_draw_size = 5;
	private Integer final_weekly_draw_size = 5;
	private Properties log4J;
	
	
	public void myfinalize(){
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public AutoDraw() throws NumberFormatException, NoSettingException{
		
		
		//System.out.println(WEEKLY_PRE_DRAW_QUERY);
		
		
		appProperties = getPropertyFile("autoDraw.properties");
		
		log4J = getPropertyFile("log4j.properties");
		
		PropertyConfigurator.configure(log4J);
		//BasicConfigurator.configure();
		
		try {
			
			Class.forName("com.mysql.jdbc.Driver");
		
		} catch (ClassNotFoundException e) {
			
			logger.error(e.getMessage(),e);
			
		}
		
		conn = getConnection(this.conStr);
		
		String dateNow = "";
		
		dateNow = getTodayDate();
		DateConstraint dc = new DateConstraint();
		dateNow = dc.addOrSubtractADays(dateNow.substring(0,10), -1);
		
		/*String admin_email = MechanicsS.getSetting("admin_email", conn);
		
		alarm.send(admin_email, "TEST1_"+getTodayDate(), "Hi,\n\nBelow are today's pooled winners who participated on "+dateNow+".\n"+listWinners()+"\n\nRegards.\n");
		alarm.send(admin_email, "TEST2_"+getTodayDate(), "Test\n\ntest");
*/
		
		
		mechanics = new MechanicsImpl(this.conn);
		
		
		try{
			pre_draw_size = Integer.valueOf(MechanicsS.getSetting("pre_draw_size", this.conn));
		}catch(Exception e){
			log(e);
		}
		
		try{
			final_daily_draw_size = Integer.valueOf(mechanics.getSetting("final_daily_draw_size"));
		}catch(Exception e){
			log(e);
		}
		
		try{
			final_weekly_draw_size  = Integer.valueOf(mechanics.getSetting("final_weekly_draw_size"));
		}catch(Exception e){
			log(e);
		}
		
		try{
			select_after = Integer.valueOf(mechanics.getSetting("select_after"));
		}catch(Exception e){
			log(e);
		}
		
		
		
		
		
		HOUR_NOW = mechanics.getHourNow();
		
		try{
			hour_to_auto_draw = Integer.valueOf(mechanics.getSetting("draw_hour"));
		}catch(Exception e){
			log(e);
		}
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
	    schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
	    cm = new ThreadSafeClientConnManager(schemeRegistry);
		cm.setDefaultMaxPerRoute(2);
		cm.setMaxTotal(3);//http connections that are equal to the worker threads.
		httpclient = new DefaultHttpClient(cm);
		qparams = new LinkedList<NameValuePair>();
		
	
	}
	
	
	public void log(Exception e){
		logger.error(e.getMessage(),e);
	}
	
	/**
	 * Request from Santhana that we first pre-select.
	 * @return
	 */
	private List<Subscriber> draw(int size, String query){
		
		
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Subscriber sub = null;
		List<Subscriber> drawn = null;
		
		int totalPoints = 100;
		
		try {
			
			pstmt = getConnection().prepareStatement("select sum(points) FROM trivia_log where timeStamp between timestamp(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY)) AND ((CURRENT_DATE + INTERVAL 1 DAY) - INTERVAL 1 SECOND)");
			rs = pstmt.executeQuery();
			if(rs.next())
				totalPoints = rs.getInt(1);
			
			logger.info("TOTAL_POINTS_IN_POOL:::::::: "+totalPoints);
			
			rs.close();
			pstmt.close();
			
			pstmt = getConnection().prepareStatement(query);
			
			rs = pstmt.executeQuery();
			
			points = 0;
			
			int totalPlayers = 0;
			
			winnerSearch:
			while(rs.next()){
				
				if(rs.isFirst()){
					rs.last();
					totalPlayers = rs.getRow();
					rs.first();
					
					logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>> come out!~ : maxSize: "+totalPlayers);
					drawn = new ArrayList<Subscriber>();
				}
				
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
				sub.setContinuation_confirmed(rs.getBoolean("continuation_confirmed"));
				sub.setHas_reached_questions_quota_for_today(rs.getBoolean("has_reached_questions_quota_for_today"));
				sub.setTotalPoints(rs.getInt("total_points"));
				
				//drawn.add(sub);
				
				if(winners.size() < totalPlayers && winners.size() < size){
					
					if(size>totalPlayers){
						
						logger.info("Adding straight away!!!!!!!");
						winners.add(sub);
						
					}else{
						
						//addAWinner(sub,totalPoints);
						winners.add(sub);
					
					}
					
				}else{
					
					logger.debug("We found : "+winners.size()+" lucky winners");
					break winnerSearch;
					
				}
				
				
				
				
				logger.debug("size: "+winners.size());
				logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+sub.toString());
				
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
		
		return drawn;
		
		
	}
	
	
	
	
	public Connection getConnection(String conStr){
		this.conStr = conStr;
		return getConnection();
	}
	
	/**
	 * Gets a connection object.
	 * If the field Connection object is not null and is not closed,
	 * then it is returned. Else a new one is made and returned.
	 * @return
	 */
	public Connection getConnection() {
		
		try {
			if(conn !=null)
				if(!conn.isClosed())
					return conn;
		} catch (SQLException e1) {
			logger.error(e1,e1);
		}
		//System.out.println(conStr);

		while( true ) {
			logger.info("in a loop!");
			try {
				logger.info("in a loop!");
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
	 * Legacy algo developed originally by Kenneth Vick.
	 * TODO since it's a muslim country where gambling, then we change this to select subscribers at random. points wont matter
	 * 
	 * @param sub
	 * @param totalPoints 
	 * @throws Exception
	 */
	private void addAWinner(Subscriber sub, int totalPoints) throws Exception{
		
		if(sub==null)
			return;
		//Find a random point that should win
		Random rand = new Random( System.currentTimeMillis() );
		
		logger.debug("::::::::::::::::::::::::::::::::::::: sub.getTotalPoints(): "+sub.getTotalPoints());
		int winner = rand.nextInt(totalPoints+1);
		logger.debug("::::::::::::::::::::::::::::::::::::: winner points: "+winner);
		logger.debug("::::::::::::::::::::::::::::::::::::: cumulative_points: "+points);
			//Does this person have the winning point?
			if (winner > points && winner <= (sub.getTotalPoints())) {
				//If he is already a winner try again.
				for (int i = 0; i < winners.size();i++) {
					if (winners.get(i).getMsisdn().equals(sub.getMsisdn())) 
						return;
				}
				
				//He is a new winner add him to the pool and stop searching
				winners.add(sub);
				
			}
			
			points += sub.getTotalPoints();
	}
	
	
	public static void main(String[] args){
		
		
		
		
		try {
			BasicConfigurator.configure();
			AutoDraw autoDraw = new AutoDraw();
			System.out.println("DAILY_FINAL_DRAW_QUERY: "+autoDraw.DAILY_FINAL_DRAW_QUERY);
			
			if(args.length>0){
				if(args[0].equalsIgnoreCase("DAILY"))
					DRAW_DURATION = "DAILY";
				if(args[0].equalsIgnoreCase("WEEKLY"))
					DRAW_DURATION = "WEEKLY";
				if(args[0].equalsIgnoreCase("FINAL"))
					DRAW_DURATION = "FINAL";
			}
			
			
			try{
				if(DRAW_DURATION==null){
					throw new NoSettingException("Draw duration not specified");
				}
			}catch (Exception e){
				
				DRAW_DURATION = "DAILY";
				e.printStackTrace();
			}
			
			autoDraw.run();
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (NoSettingException e) {
			e.printStackTrace();
		} catch (MessageNotSetException e) {
			e.printStackTrace();
		}
		
	}


	private void run() throws MessageNotSetException, NoSettingException {
		
		String drawQuery = "";
		
		
		//HOUR_NOW = 0;
		//hour_to_auto_draw = HOUR_NOW;
		if(HOUR_NOW==hour_to_auto_draw){//if it's the time to do an automatic draw.
			
			String subj = "DAILY_PRE_DRAW";
			if(DRAW_DURATION.equalsIgnoreCase("DAILY")){
				drawQuery = DAILY_PRE_DRAW_QUERY;
				subj = "DAILY_PRE_DRAW";
			}if(DRAW_DURATION.equalsIgnoreCase("WEEKLY")){
				drawQuery = WEEKLY_PRE_DRAW_QUERY;
				subj = "WEEKLY_PRE_DRAW";
			}
			
			String pre_select_query = "INSERT INTO "+HTTPMTSenderApp.props.getProperty("DATABASE")+".winners_drawn(session_id,msisdn) VALUES(?,?)";
			PreparedStatement pstmt = null;
			
			try {
				
				pstmt = conn.prepareStatement(pre_select_query);
			
				int winner_session_id = -1;
				
				draw(pre_draw_size,drawQuery);
				
				logger.debug("lucky winners: "+winners.size());
				
				String msg = "";
				
				Subscriber sub = null;
				Question winning_question_english = null;
				Question winning_question_malay = null;
				for(int i = 0; i < winners.size(); i++){
					
					if(i==0){
						
						
						winning_question_english = MechanicsS.getNextWinningQuestion(1, conn);
						winning_question_malay = MechanicsS.getNextWinningQuestion(2, conn);
						
						if(winning_question_english==null || winning_question_malay==null){
							
							try{
								alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia:WARNING:.", "Hi,\n\n could not do auto draw. if one of the below please add winner questions to the db.\nwinning_question_malay"+winning_question_malay+"\nwinning_question_malay="+winning_question_english+"\n\n  Regards");
								
							}catch(Exception e){
								log(e);
							}
							throw new NoSettingException("Winners question not set in db, winners_questions should have origin_id = 112");
							
						}
						
						winner_session_id = createWinnerSession(subj,true);
						
					}
					
					sub = winners.get(i);
					//sub.setMsisdn("0193685271");
					pstmt.setInt(1, winner_session_id);
					pstmt.setString(2, sub.getMsisdn());
					pstmt.executeUpdate();//add em to the winners_drawn_table
					
					msg = "";
					msg = MechanicsS.getMessage(MessageType.PRE_SELECTION_NOTIFICATION, sub.getLanguage_id_(), getConnection());
					msg = RM0+MechanicsS.perSonalizeMessage(msg, sub, 0, getConnection());
					mechanics.insertIntoHTTPToSend(sub.getMsisdn(), msg);//Pre-selection notification sent.
					
					int question_id = -1;
					//Now send the question to be answered within 3 hours.
					if(sub.getLanguage_id_()==1){
						
						msg = winning_question_english.getQuestion();
						question_id = winning_question_english.getId();
					}else{
						
						msg = winning_question_malay.getQuestion();
						question_id = winning_question_malay.getId();
					
					}
						
					msg = RM0+MechanicsS.perSonalizeMessage(msg, sub, 0, getConnection());
					mechanics.insertIntoHTTPToSend(sub.getMsisdn(), msg);//Pre-selection notification sent.
					
					//Log it as sent but not answered, so we can retrieve later
					MechanicsS.logAsSentButNotAnswered(sub.getMsisdn(), question_id, getConnection());
					
					
				}
				
				mechanics.insertIntoHTTPToSend(MechanicsS.getSetting("admin_msisdn", conn), "RM0 ["+subj+"]WE PRE-DREW: "+winners.size()+" subscribers");//Pre-selection notification sent.
				winners.clear();
				
				if(winning_question_english!=null)
					MechanicsS.setUnusable(winning_question_english.getId(), conn);
				if(winning_question_malay!=null)
					MechanicsS.setUnusable(winning_question_malay.getId(), conn);
			
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
		
		//HOUR_NOW = hour_to_auto_draw;
		//hour_to_auto_draw=hour_to_auto_draw-3;
		if(HOUR_NOW==hour_to_auto_draw+select_after){
			
			int drawSize = 5;
			String subj = "DAILY_WINNERS_DRAW";
			
			if(DRAW_DURATION.equalsIgnoreCase("DAILY")){
				drawQuery = DAILY_FINAL_DRAW_QUERY;
				drawSize = final_daily_draw_size;
				subj = "DAILY_WINNERS_DRAW";
			}
			if(DRAW_DURATION.equalsIgnoreCase("WEEKLY")){
				drawQuery = WEEKLY_PRE_DRAW_QUERY;
				drawSize = final_weekly_draw_size;
				subj = "WEEKLY_WINNERS_DRAW";
			}
			
			
			PreparedStatement pstmt = null;
		
			int winnerSession = -1;
			
			try{
				
				pstmt = conn.prepareStatement("INSERT INTO "+HTTPMTSenderApp.props.getProperty("DATABASE")+".winners_drawn(session_id,msisdn) VALUES(?,?)");
				winnerSession = createWinnerSession(subj, false);
				
				draw(drawSize, drawQuery);
				
				Subscriber sub = null;
				
				for(int i = 0; i < winners.size(); i++){
					
					sub = winners.get(i);
					pstmt.setInt(1, winnerSession);
					pstmt.setString(2, sub.getMsisdn());
					pstmt.executeUpdate();
					
				}
			
				sendEmailToCelcomAboutDrawnWinners(subj);
				
				mechanics.insertIntoHTTPToSend(MechanicsS.getSetting("admin_msisdn", conn), "RM0 ["+subj+"] WE DREW: "+winners.size()+" subscribers");//Pre-selection notification sent.
				
				
			}catch(SQLException e){
				
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
		
		myfinalize();
		
	}
	
	
	private String getTodayDate(){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
		String strdate=null;
       	
       	strdate=sdf.format(new Date());
       	
		return strdate;
       	
	}
	

	private void sendEmailToCelcomAboutDrawnWinners(String string) throws NoSettingException {
		
			String dateNow = "";
		
			dateNow = getTodayDate();
			DateConstraint dc = new DateConstraint();
			dateNow = dc.addOrSubtractADays(dateNow.substring(0,10), -1);
			
			alarm.send(MechanicsS.getSetting("draw_emails", conn), string+"_"+getTodayDate(), "Hi,\n\nBelow are today's pooled winners who participated on "+dateNow+".\n"+listWinners()+"\n\nRegards.\n");
		
	}


	private String listWinners() {
		
		String winnerList = "\n\n";
		Subscriber sub = null;
		logger.info("winners.size()::::::: "+winners.size());
		for(int i = 0; i < winners.size(); i++){
			
			sub = winners.get(i);
			winnerList += (i+1)+". "+sub.getMsisdn()+"\n";
			
		}
		
		return winnerList;
	}


	private int createWinnerSession(String session_name,boolean pre_selection) {
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		int session_id = -1;
		
		try {
			
			pstmt = getConnection().prepareStatement("INSERT INTO "+HTTPMTSenderApp.props.getProperty("DATABASE")+".winners_sessions(`from`,`to`,`user_id`,`name`,`pre_selection`) VALUES(TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)),DATE_SUB(TIMESTAMP(CURRENT_DATE), INTERVAL 1 SECOND),?,?,?)", Statement.RETURN_GENERATED_KEYS);
		
			pstmt.setInt(1, 1);
			pstmt.setString(2, getTodayDate()+"_"+session_name);
			pstmt.setInt(3, pre_selection ? 1 : 0);
			
			
			pstmt.executeUpdate();
			
			rs = pstmt.getGeneratedKeys();
			
			if(rs.next()){
				session_id = rs.getInt(1);
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
		}
		return session_id;
	}
	
	
	
	
	/**
	 * Gets the property File
	 * @param filename
	 * @return
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


	

}
