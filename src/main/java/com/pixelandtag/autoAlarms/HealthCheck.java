package com.pixelandtag.autoAlarms;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.autodraw.Alarm;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.web.triviaI.MechanicsI;
import com.pixelandtag.web.triviaImpl.MechanicsS;

public class HealthCheck implements Callable<Boolean> {
	
	private ScheduledExecutorService service;
	private int time;
	private TimeUnit timeUnit;
	private Logger logger  = Logger.getLogger(HealthCheck.class);
	private static DBPoolDataSource ds;
	
	private Alarm alarm = new Alarm();

	/**
	 * 
	 * @param service
	 * @param time
	 * @param timeUnit
	 */
	public HealthCheck(ScheduledExecutorService service, int time, TimeUnit timeUnit) {
		super();
		
		initialize();// we start with that so as db connection pool is ready
		
		this.time = time;
		
		Connection conn = null;
		
		try{
			
			conn = getConn();
			int health_check_interval_minutes = Integer.valueOf(MechanicsS.getSetting("health_check_interval_minutes", conn));
			this.time = health_check_interval_minutes;
			
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}finally{
			
			try{
				if(conn!=null)
					conn.close();
			}catch(Exception e){}
			
		}
		
		this.service = service;
		
		this.timeUnit = timeUnit;
		
		
	    
	}



	private void initialize() {
		
		int vendor = DriverUtilities.MYSQL;
	    String driver = DriverUtilities.getDriver(vendor);
	    String host = "db";
	    String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
	    String url = DriverUtilities.makeURL(host, dbName, vendor);
	    String username = "root";
	    String password = "";
	    
	    
	    ds = new DBPoolDataSource();
	    ds.setValidatorClassName("snaq.db.Select1Validator");
	    ds.setName("HealthCheck");
	    ds.setDescription("HealthCheck");
	    ds.setDriverClassName("com.mysql.jdbc.Driver");
	    ds.setUrl(url);
	    ds.setUser("root");
	    ds.setPassword("");
	    ds.setMinPool(1);
	    ds.setMaxPool(2);
	    ds.setMaxSize(3);
	    ds.setIdleTimeout(3600);  // Specified in seconds.
	    ds.setValidationQuery("SELECT now()");
		
	}

	
	public boolean isConnOK(Connection conn_) {
		
		if(conn_==null)
			return false;
		
		Statement stmt = null;
		ResultSet rs = null;
		boolean connOK = false;
		
		try{
			
			stmt = conn_.createStatement();
			rs = stmt.executeQuery("SELECT now()");
			
			if(rs.next()){
				logger.debug("conn ok!");
				connOK = true;
			}
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			try{
				if(rs!=null)
					rs.close();
			}catch (Exception e){}
			
			try{
				if(stmt!=null)
					stmt.close();
			}catch (Exception e){}
			
		}
		
		return connOK;
		
	}


	
	
	public void finalizeME(){
		
		try{
			
			ds.releaseConnectionPool();
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}
		
	}
	
	private Connection getConn() throws SQLException{
		
		return ds.getConnection();
		
	}
	
	

	public Boolean call() {
		
		Connection conn = null;
		
		try{
			
			conn = getConn();
			
			TimeUnit tu = TimeUnit.MINUTES;
			
			
			
			long lastBillable = 0;
			long lastMMSQued = 0;
			int mmsQueue = 0;
			int mms_queue_threshhold = 10;
			
			int mms_traffic_threshhold_mins = 5;
				
			int earliest_alarm = 5;
			int latest_alarm = 23;
			
			try{
				lastBillable = MechanicsS.getTimeSinceLastSuccessfulBillable(conn,tu);
				lastMMSQued = MechanicsS.getTimeLastMMSSentOut(conn,tu);
				earliest_alarm = Integer.valueOf(MechanicsS.getSetting("earliest_alarm", conn));
				latest_alarm = Integer.valueOf(MechanicsS.getSetting("latest_alarm", conn));
				mms_traffic_threshhold_mins = Integer.valueOf(MechanicsS.getSetting("mms_traffic_threshhold_mins", conn));
				mmsQueue = MechanicsS.getMMSQueue(conn);
				mms_queue_threshhold = Integer.valueOf(MechanicsS.getSetting("mms_queue_threshhold", conn));
				
				
			}catch(Exception e){
				logger.error(e.getMessage(),e);
			}
			
			
			long timeDiff = lastBillable-lastMMSQued;
			
			
			boolean weok = false;
			
			logger.debug(">>>>>>>>>> Hi,\n\nMINUTES SINCE LAST SUCCESSFUL BILLABLE SMS WAS SENT:"+lastBillable+"\n\nMINUTES SINCE WE SENT AN MMS: "+lastMMSQued+"\n\n  Regards");
			
			
			if(mmsQueue>=mms_queue_threshhold){
				logger.warn("Hi,\n\nWe've reachedthe MMS queue threshhold, we have an mms queue of "+mmsQueue+" mms(s) \n\nRegards.");
				alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: WARNING: MMS QUEUE REACHED THRESHHOLD", "Hi,\n\nWe've reachedthe MMS queue threshhold, we have an mms queue of "+mmsQueue+" mms(s) \n\nRegards.");
			}
			
			if(timeDiff > mms_traffic_threshhold_mins){
				
				int hourNow = getHourNow();
				
				
				
				if(hourNow>=earliest_alarm && hourNow<=latest_alarm)
				try{
					
					alarm.send(MechanicsS.getSetting("alarm_emails", conn), "Malaysia Trivia: WARNING: WE'VE NOT SENT MMS IN A WHILE", "Hi,\n\nMINUTES SINCE LAST SUCCESSFUL BILLABLE SMS WAS SENT:"+lastBillable+"\n\nMINUTES SINCE WE SENT AN MMS: "+lastMMSQued+"\n\nRegards.");
					
					
					
				}catch(Exception e3){
					
					log(e3);
					
				}finally{
					
					//we close it in the other finally block
					
				}
				
				weok= false;
			}else{
				weok = true;
			}
			
			logger.debug("ayeiya! lastBillable="+lastBillable+" lastMMSQued="+lastMMSQued);
			
			return weok;
			
		}catch(Exception e){
			
			log(e);
			return false;
			
		}finally{
			
			
			int health_check_interval_minutes = this.time;
			
			try{
				
				health_check_interval_minutes = Integer.valueOf(MechanicsS.getSetting("health_check_interval_minutes", conn));
				this.time = health_check_interval_minutes;
				
			}catch(Exception e){
				
				logger.error(e.getMessage(),e);
				
			}finally{
				
				try{
					if(conn!=null)
						conn.close();
				}catch(Exception e){}
				
			}
			
			service.schedule(this, health_check_interval_minutes, timeUnit);
			
		}
	}
	
	
	private static int getHourNow() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH");
		String strdate = sdf.format(new Date());
		return Integer.valueOf(strdate);
	}



	private void log(Exception e3) {
		logger.error(e3.getMessage(),e3);
	}



	public static void main(String[] args) {
		HealthCheck hc = new HealthCheck(Executors.newSingleThreadScheduledExecutor(), 15, TimeUnit.MINUTES);
		hc.call();
	}

}
