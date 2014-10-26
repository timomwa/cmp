package com.pixelandtag.vouchersystem;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.inmobia.luckydip.api.LuckyDipFactory;
import com.inmobia.luckydip.api.LuckyDipI;
import com.inmobia.util.Utils;
import com.mysql.jdbc.Statement;


public class Producer implements Runnable {
	
	private boolean run = true;
	private Logger logger = Logger.getLogger(Producer.class);
	private ArrayBlockingQueue<Entry> queue = new ArrayBlockingQueue<Entry>(1000, true);
	private List<Consumer> consumers = new ArrayList<Consumer>();
	private Connection conn;
	private int workers = 1;
	public static String constr = "jdbc:mysql://db/lucky_dip?user=root&password=";
	private Properties log4jprops = null;
	private Properties props = null;
	public static String TODAYS_WINNING_ID  = "TODAYS_WINNING_ID";
	public static String DAILY_WINNER_QUOTA  = "DAILY_WINNER_QUOTA";
	
	public Producer(int workers,String constr){
		this.workers=workers;
		Producer.constr=constr;
		initialize();
	}
	
	private void initialize(){
		
		log4jprops = getPropertyFile("log4j_voucher_draw.properties");
		
		props = getPropertyFile("voucher_system.properties");
		
		if(props.getProperty("constr")!=null && props.getProperty("constr").length()>0){
			constr = props.getProperty("constr");
		}
		
		if(props.getProperty("workers")!=null && props.getProperty("workers").length()>0){
			try{
				workers = Integer.valueOf(props.getProperty("workers"));
			}catch(Exception e){
				workers=1;
			}
		}
		
		PropertyConfigurator.configure(log4jprops);
		//BasicConfigurator.configure();
		
		registerDriver();
		
		conn = getConn();
		
		try{
			
			for(int x = 0; x<workers;x++){
				Consumer c = new Consumer("Thread-"+x, constr, queue);
				c.start();
				consumers.add(c);
			}
			
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}
		
	
		
	}
	
	 /**
	 * Register the driver
	 */
	private void registerDriver(){
		
		try {
				
			Class.forName("com.mysql.jdbc.Driver");
				
		}catch(Exception e){
				
				logger.error(e.getMessage(),e);
			
		}
		
	}
	
	public void run(){
		
		while(run){
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			
			
			if(queue.isEmpty())//if queue is empty, we put some stuff there.
			try{
				
				conn = getConn();
				
				int today_winners = VoucherUtils.getWinnersToday(conn);
				int daily_winner_quota = Integer.valueOf(VoucherUtils.getSetting(DAILY_WINNER_QUOTA,conn));
				
				
				
				logger.debug("today_winners = "+today_winners);
				logger.debug("daily_winner_quota = "+daily_winner_quota);
				logger.debug("(today_winners<daily_winner_quota) = "+(today_winners<daily_winner_quota));
				
				if(today_winners<daily_winner_quota){//if we still need winners or have exceeded winner quota
					
					pstmt = conn.prepareStatement(String.format("SELECT id FROM `voucher_system`.`unprocessed_participant_batch` WHERE `status`='queued' AND `timeStamp` between timestamp(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY)) AND ((CURRENT_DATE + INTERVAL 1 DAY) - INTERVAL 1 SECOND)  ORDER BY RAND() LIMIT %s",String.valueOf(daily_winner_quota)), Statement.RETURN_GENERATED_KEYS);
					rs = pstmt.executeQuery();
					
					while(rs.next()){
						VoucherUtils.insertToDrawTable(rs.getInt("id"), conn);
					}/*else{
						throw new Exception("Could not find a winning ID, draw cannot go on! Check if we have any entries today in `voucher_system`.`unprocessed_participant_batch`");
					}*/
					try{
						rs.close();
					}catch(Exception e){}
					
					try{
						pstmt.close();
					}catch(Exception e){}
				}
				
				
				
				
				pstmt = conn.prepareStatement(String.format("SELECT * FROM `voucher_system`.`unprocessed_participant_batch` WHERE `status`='%s' AND `timeStamp` between timestamp(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY)) AND ((CURRENT_DATE + INTERVAL 1 DAY) - INTERVAL 1 SECOND)  ORDER BY timeStamp desc LIMIT 1000/*FIFO*/",Status.queued.toString()),PreparedStatement.RETURN_GENERATED_KEYS);
				
				rs = pstmt.executeQuery();
				
				Entry mt = null;
				
				int k = 0;
				while(rs.next()){
					
					mt = new Entry();
					
					mt.setId(rs.getInt("id"));
					mt.setMsisdn(rs.getString("msisdn"));
					mt.setCmp_txid_fk(rs.getString("cmp_txid_fk"));
					mt.setTimeStampAwarded(rs.getString("timeStamp"));
					
					queue.add(mt);
					
					changeStatus(mt.getId(), Status.processing);
					k++;
					
				}
				
				if(k==0)
					Thread.sleep(30000);//sleep for 30sec if there is nothing to be sent
				
				
			
			} catch (IllegalStateException e) {
			
				logger.debug("GUGAMUGA_ Queue is full, we try in another cycle. We wait for 30 seconds.");
				
				try{
				
					Thread.sleep(30000);
				
				}catch(Exception e1){
				}
				
			}catch(InterruptedException e){
				
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}finally{
				
				try{
					rs.close();
				}catch(Exception e){}
				try{
					pstmt.close();
				}catch(Exception e){}
				try{
					conn.close();
				}catch(Exception e){}
				
			}
		}
		
	}
	
	
	private Connection getConn() {
		
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
						conn = DriverManager.getConnection(Producer.constr,"root","");
						//logger.debug("got a connection");
					} catch ( Exception e ) {
						logger.warn(e,e);
						try { Thread.sleep(500); } catch ( Exception ee ) {}
					}
				}
				return conn;
			} catch ( Exception e ) {
				logger.error(e,e);
				try { Thread.sleep(1000); } catch ( Exception ee ) {}
			}
		}
	}
	
	
	
	
	private void changeStatus(int id, Status processed) {
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		try{
			conn = getConn();
			pstmt = conn.prepareStatement("UPDATE `voucher_system`.`unprocessed_participant_batch` SET `status`=? WHERE id=?",Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, processed.toString());
			pstmt.setInt(2, id);
			pstmt.executeUpdate();
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}finally{
			try{
				pstmt.close();
			}catch(Exception e){}
		}
		
	}
	
	
	
	
	/**
	 * This gets the property file
	 * @param filename String the file name for the given property file
	 * @return java.util.Properties instance of the created property file
	 */
	private Properties getPropertyFile(String filename) {

		Properties prop = new Properties();
		InputStream inputStream = null;
		;
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
