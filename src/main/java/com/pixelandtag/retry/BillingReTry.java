package com.pixelandtag.retry;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.pixelandtag.api.ERROR;
import com.pixelandtag.axiata.teasers.producer.BroadcastApp;
import com.pixelandtag.entities.URLParams;
import com.pixelandtag.exceptions.NoSettingException;
import com.pixelandtag.teasers.workers.BroadcastWorker;
import com.pixelandtag.util.StopWatch;
import com.pixelandtag.web.beans.CelcomMessageLog;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.triviaI.MechanicsI;
import com.pixelandtag.web.triviaImpl.MechanicsImpl;
import com.pixelandtag.web.triviaImpl.MechanicsS;

public class BillingReTry {
	
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

	private Logger logger = Logger.getLogger(BillingReTry.class);
	
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
	private int started=0;
	private boolean isHappyHour = false;
	private String client_tz = "+08:00";
	private String server_tz = "+08:00";
	private StopWatch watch = new StopWatch();
	public static BroadcastApp instance = null;
	public static Semaphore semaphore = new Semaphore(1,true);
	private Properties props = null;
	private Properties log4J = null;
	private URLParams urlparams = null;
	private String constr = null;
	private int throttle = 10;
	private int workers = 5;
	private int initialDBConnections = 1;
	private int maxDBConnections = 1;
	private int queueSize = 100;
	private String fr_tz = "+08:00";
	private String to_tz = "+08:00";
	
	
	private void initialaize() throws NoSettingException{
		
		props = getPropertyFile("retryBilling.properties");
		log4J = getPropertyFile("log4jr.properties");
		
		urlparams = new URLParams(props);
		
		this.constr = props.getProperty("constr");
		
		if(props.getProperty("THROTTLE")!=null)
			this.throttle = Integer.valueOf(props.getProperty("THROTTLE"));
		
		if(props.getProperty("WORKER_THREADS")!=null)
			this.workers = Integer.valueOf(props.getProperty("WORKER_THREADS"));
		
		if(props.getProperty("initialDBConnections")!=null)
			this.initialDBConnections = Integer.valueOf(props.getProperty("initialDBConnections"));
		
		if(props.getProperty("maxDBConnections")!=null)
			this.maxDBConnections = Integer.valueOf(props.getProperty("maxDBConnections"));
		
		if(props.getProperty("queueSize")!=null)
			this.queueSize = Integer.valueOf(props.getProperty("queueSize"));
		
		
		try {
			
			Class.forName("com.mysql.jdbc.Driver");
		
		} catch (ClassNotFoundException e) {
			
			logger.error(e.getMessage(),e);
			
		}
		
		
		System.out.println("this.constr: "+this.constr);
		
		this.conn = getConnection(this.constr);
		
		mechanics = new MechanicsImpl(conn);
		
		HOUR_NOW = mechanics.getHourNow();
		
		
		PropertyConfigurator.configure(log4J);
		//BasicConfigurator.configure();
		
		
		logger.info("Hour now::::>>>>>>>>>>>> "+HOUR_NOW);
		
		// we check if we're legally supposed to be sending SMS to peeps.
		run = mechanics.isLegalTimeToSendBroadcasts();
		
		logger.info("mech.isLegalTimeToSendBroadcasts():::: Legal time for teasers? >> "+run);
		
		
		//it's our money isn't it?
		//if(run||HOUR_NOW<18){//if it's still 7pm and we're allowed to send teasers
			
		reTryBilling();
			
			
		//}
		
		
		
	}
	
	
	private void reTryBilling() {
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		
		try {
			
			String billing_re_try_hours = MechanicsS.getSetting("billing_re_try_hours", getConnection(this.constr));
			
			String sql = "SELECT * FROM `celcom`.`messagelog` WHERE MT_STATUS in ('PSANumberBarred','PSAInsufficientBalance') AND re_try=1 and (TIMESTAMPDIFF(HOUR,timeStamp,CONVERT_TZ(CURRENT_TIMESTAMP,'"+this.server_tz+"','"+this.client_tz+"'))<="+billing_re_try_hours+")";
			System.out.println(sql);
			logger.info(sql);
			pstmt = getConnection(this.constr).prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				CelcomMessageLog msglog = new CelcomMessageLog();
				msglog.setId(rs.getString("id"));
				msglog.setProcessing(rs.getInt("processing")==1);
				msglog.setcMP_Txid(rs.getString("CMP_Txid"));
				msglog.setmO_received(rs.getString("MO_Received"));
				msglog.setmT_Sent(rs.getString("MT_Sent"));
				msglog.setmT_Sendtime(rs.getString("MT_SendTime"));
				msglog.setsMS_SourceAddr(rs.getString("SMS_SourceAddr"));
				msglog.setsUB_Mobtel(rs.getString("SUB_Mobtel"));
				msglog.setsMS_DataCodingId(rs.getString("SMS_DataCodingId"));
				msglog.setcMP_Response(rs.getString("CMPResponse"));
				msglog.setApiType(rs.getInt("APIType"));
				msglog.setcMP_Keyword(rs.getString("CMP_Keyword"));
				msglog.setCMP_SKeyword(rs.getString("CMP_SKeyword"));
				msglog.setTimeStamp(rs.getString("timeStamp"));
				msglog.setMo_ack(rs.getString("mo_ack"));
				msglog.setMt_ack(rs.getString("mt_ack"));
				msglog.setmT_Status(ERROR.get(rs.getString("MT_STATUS")));
				msglog.setDelivery_report_arrive_time(rs.getString("delivery_report_arrive_time"));
				msglog.setPrice(rs.getDouble("price"));
				msglog.setServiceid(rs.getInt("serviceid"));
				msglog.setNumber_of_sms(rs.getInt("number_of_sms"));
				msglog.setMsg_was_split(rs.getInt("msg_was_split")==1);
				msglog.setRe_try_count(rs.getInt("re_try_count"));
				msglog.setRe_try(rs.getInt("re_try")==1);
				//if(rs.getString("newCMP_Txid").equals("-1"))
				msglog.setNewCmpTxId(rs.getString("CMP_Txid"));
				//else
				//msglog.setNewCmpTxId(rs.getString("newCMP_Txid"));
				
				
				reTrySending(msglog);
			}
			
			myFinalize();
			
		} catch (SQLException e) {
			log(e);
		} catch (NoSettingException e) {
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
		
	}


	private void reTrySending(CelcomMessageLog msglog) {
		
		try{
			
			logger.debug(msglog.toString());
			//System.out.println(msglog.toString());
			if(MechanicsS.insertIntoHttpToSend(msglog.getsUB_Mobtel(), msglog.getmT_Sent(), MechanicsS.generateNextTxId(), msglog.getServiceid(), msglog.getPrice(), msglog.getcMP_Txid(), conn)){
				MechanicsS.toggleRetry(msglog.getcMP_Txid(),conn,false,false);
				MechanicsS.incrementRetryCount(msglog.getcMP_Txid(),conn);
			}
			
		}catch(Exception e){
			
			log(e);
			
		}
		
	}


	private void log(Exception e) {
		logger.error(e.getMessage(),e);
	}


	private void myFinalize(){
		
		try {
			
			if(conn!=null)
				conn.close();
		} catch (SQLException e) {
			log(e);
		}
		
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
				if(!conn.isClosed()){
					return conn;
				}
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

	
	public static void main(String[] args){
		BillingReTry br = new BillingReTry();
		try {
			br.initialaize();
		} catch (NoSettingException e) {
			// TODO Auto-generated catch block
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
}
