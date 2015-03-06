package com.pixelandtag.mms.apiImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;

import org.apache.log4j.Logger;

import snaq.db.ConnectionPool;

import com.pixelandtag.entities.MOSms;
import com.pixelandtag.mms.api.MM7Api;
import com.pixelandtag.mms.api.MM7DeliveryReport;
import com.pixelandtag.mms.api.MMS;
import com.pixelandtag.mms.api.SaajUtils;
import com.pixelandtag.mms.api.SoapMMSDN;
import com.pixelandtag.sms.producerthreads.MTProducer;

public class MMSApiImpl implements MM7Api {
	
	
	
	
	private Logger logger = Logger.getLogger(MMSApiImpl.class);
	private DataSource ds = null;
	private Connection conn = null;
	private String conStr = null;
	private boolean connectionObjIsCached = true;
	
	private String fr_tz = "+08:00";
	private String to_tz = "+08:00";
	
	
	private volatile ConnectionPool pool;
	private Semaphore semaphore;
	
	
	public MMSApiImpl(DataSource ds_) throws Exception{
		
		semaphore = new Semaphore(1,true);
		
		if(ds_==null){
			throw new Exception("DS Being passed is null");
		}
		
		connectionObjIsCached = false;
		
		semaphore = new Semaphore(1,true);
		
		this.ds = ds_;
	
	}
	
	
	public String getFr_tz() {
		return fr_tz;
	}

	public String getTo_tz() {
		return to_tz;
	}

	public void setFr_tz(String fr_tz) {
		this.fr_tz = fr_tz;
	}

	public void setTo_tz(String to_tz) {
		this.to_tz = to_tz;
	}

	@Override
	public MMS createMMS(String msisdn, String media_path, String mmstext, String subject) {
		MMS mms = new MMS();
		mms.setMsisdn(msisdn);
		mms.setMedia_path(media_path);
		mms.setMms_text(mmstext);
		mms.setSubject(subject);
		return mms;
	}

	@Override
	public MMS retrieveMMS(String txID) {
		
		//Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		MMS mms =  null;
		
		try {
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("SELECT * FROM `"+database+"`.`mms_log` WHERE  txID = ?",Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT * FROM `"+database+"`.`mms_log` WHERE  txID = ?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, String.valueOf(txID));
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				mms = new MMS();
				mms.setId(rs.getString("id"));
				mms.setCMP_Txid(new BigInteger(rs.getString("txID")));
				mms.setMsisdn(rs.getString("msisdn"));
				mms.setSubject(rs.getString("subject"));
				mms.setMms_text(rs.getString("mms_text"));
				mms.setMedia_path(rs.getString("media_path"));
				mms.setServiceid(rs.getInt("serviceid"));
				mms.setShortcode(rs.getString("shortcode"));
				mms.setLinked_id(rs.getString("linked_id"));
				mms.setEariest_delivery_time(rs.getString("earliest_delivery_time"));
				mms.setExpiry_date(rs.getString("expiry_date"));
				mms.setDelivery_report_requested(rs.getString("delivery_report_requested"));
				mms.setServicecode(rs.getString("servicecode"));
				mms.setTimeStampOfInsertion(rs.getString("timeStampOfInsertion"));
				mms.setDistribution_indicator(String.valueOf(rs.getBoolean("distributable")));
				mms.setDlrArriveTime(rs.getString("DLRArriveTimeStamp"));
				mms.setDlrReportStatus(rs.getString("DLRReportStatus"));
			}
			
		} catch (SQLException e) {
			
			log(e);
		
		} catch (InterruptedException e) {
		
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
			
			closeConnectionIfNecessary(conn);			
		
		}
		
		
		return mms;
	}

	@Override
	public boolean toggleInProcessingQueue(String txID, boolean inqueue) {
		
		//Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		
		try {
			
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("UPDATE `"+database+"`.`mms_to_send` SET `inProcessingQueue` = ?, `timeStampOfInsertion` = CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"') WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
					
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("UPDATE `"+database+"`.`mms_to_send` SET `inProcessingQueue` = ?, `timeStampOfInsertion` = CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"') WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setInt(1, (inqueue ? 1 : 0));
			
			pstmt.setString(2, String.valueOf(txID));
			
			success = pstmt.executeUpdate()>0;
			
		} catch (SQLException e) {
			
			log(e);
		
		} catch (InterruptedException e) {
			
			log(e);
			
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			closeConnectionIfNecessary(conn);
			
		}
		
		
		return success;
	}

	@Override
	public boolean toggleSent(String txID, boolean sent) {
		
		//Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		
		try {
			
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("UPDATE `"+database+"`.`mms_to_send` SET `sent` = ? WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
					
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("UPDATE `"+database+"`.`mms_to_send` SET `sent` = ? WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setInt(1, (sent ? 1 : 0));
			
			pstmt.setString(2, String.valueOf(txID));
			
			success = pstmt.executeUpdate()>0;
			
		} catch (SQLException e) {
			
			log(e);
		
		} catch (InterruptedException e) {
			
			log(e);
			
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			closeConnectionIfNecessary(conn);
			
		}
		
		return success;
	}

	@Override
	public boolean queueMMSForSending(MMS mms) {
		
		if(mms==null)
			return false;
		
		//Connection conn = null;
		PreparedStatement pstmt = null;
		boolean success = false;

		try {
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("INSERT INTO `"+database+"`.`mms_to_send`(msisdn, subject, mms_text, media_path,serviceid, shortcode, linked_id, earliest_delivery_time, expiry_date, delivery_report_requested, servicecode, timeStampOfInsertion, distributable, paidFor,tx_id_waiting_to_succeed_before_sending) " +
						"VALUES(?,?,?,?,?,?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'),?,?,?)",Statement.RETURN_GENERATED_KEYS);
					
			}else{
				
				conn = getConn();
				
				pstmt =      conn.prepareStatement("INSERT INTO `"+database+"`.`mms_to_send`(msisdn, subject,mms_text, media_path, serviceid, shortcode, linked_id, earliest_delivery_time, expiry_date, delivery_report_requested, servicecode, timeStampOfInsertion, distributable, paidFor,tx_id_waiting_to_succeed_before_sending) " +
						"VALUES(?,?,?,?,?,?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'),?,?,?)",Statement.RETURN_GENERATED_KEYS);
			}
			
			
			
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
			
		} catch (InterruptedException e) {
			
			log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			closeConnectionIfNecessary(conn);
			
		}
		
		return success;
	}
	
	
	
	
	
	
	
	
	
	
	public boolean acknowledge(SoapMMSDN soap) {
		
		//Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean success = false;
		
		
		try {
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("SELECT mt_ack FROM `"+database+"`.`mms_log` WHERE txID = ?",Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT mt_ack FROM `"+database+"`.`mms_log` WHERE txID = ?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, String.valueOf(soap.getTransactionID()));
			
			rs = pstmt.executeQuery();
			
			boolean recExists = false;
			
			if(rs.next()){
				
				recExists = true;
				logger.debug("SMS FOUND: CMP_Txid = "+soap.getTransactionID());
			
			}else{
				
				logger.warn("SMS with CMP_Txid = "+soap.getTransactionID()+ " NOT found! Insertint into surrogateMMSLog");
				
				
				if(connectionObjIsCached){
					
					pstmt = getConn().prepareStatement("INSERT INTO `"+database+"`.`surrogatemmslog`(`txID`,`msisdn`,`Status`,`timeStamp`) VALUES(?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'))",Statement.RETURN_GENERATED_KEYS);
				
				}else{
					
					pstmt = conn.prepareStatement("INSERT INTO `"+database+"`.`surrogatemmslog`(`txID`,`msisdn`,`Status`,`timeStamp`) VALUES(?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'))",Statement.RETURN_GENERATED_KEYS);
				
					
				}
				
				pstmt.setString(1, soap.getTransactionID());
				pstmt.setString(2, soap.getRecipient());
				pstmt.setString(3, soap.getStatusText());
				
				pstmt.executeUpdate();
				
				logger.info("MMS DLR SUCCESSFULLY INSERTED INTO surrogatemmslog");
				
				
			}
			
			pstmt.close();
			
			//Only if the message exists
			if((recExists)){
				
				
				if(connectionObjIsCached){
					
					pstmt = getConn().prepareStatement("UPDATE `"+database+"`.`mms_log` SET mt_ack=1, DLRArriveTimeStamp=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'), DLRReportStatus=?  WHERE  txID = ?",Statement.RETURN_GENERATED_KEYS);
				
				}else{
					
					pstmt = conn.prepareStatement("UPDATE `"+database+"`.`mms_log` SET mt_ack=1, DLRArriveTimeStamp=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'), DLRReportStatus=?  WHERE  txID = ?",Statement.RETURN_GENERATED_KEYS);
				
				}
				
				pstmt.setString(1, soap.getStatusText());
				
				pstmt.setString(2, soap.getTransactionID());
				
				success = pstmt.executeUpdate()>0;
				
				logger.debug("MT (CMP_Txid = "+soap.getTransactionID()+ ") " + (success ? "successfully acknowledged" : "acknowledging failed!"));
				
			}
			
			
		} catch (SQLException e) {
			
			log(e);
		
		} catch (InterruptedException e) {
			
			log(e);
			
		}finally{
			
			
			try{
				
				if(rs!=null)
					rs.close();
				
			}catch(SQLException e){
				
				log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			closeConnectionIfNecessary(conn);	
		
		}
		
		return success;
		
	}
	

	@Override
	public boolean acknowledge(MM7DeliveryReport report) {
		
		//Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean success = false;
		
		
		try {
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("SELECT mt_ack FROM `"+database+"`.`mms_log` WHERE txID = ?",Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT mt_ack FROM `"+database+"`.`mms_log` WHERE txID = ?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, String.valueOf(report.getCMP_Txid()));
			
			rs = pstmt.executeQuery();
			
			boolean recExists = false;
			
			if(rs.next()){
				
				recExists = true;
				logger.debug("SMS FOUND: CMP_Txid = "+report.getCMP_Txid());
			
			}else{
				
				logger.warn("SMS with CMP_Txid = "+report.getCMP_Txid()+ " NOT found!");
				
			}
			
			pstmt.close();
			
			//Only if the message exists
			if((recExists)){
				
				
				if(connectionObjIsCached){
					
					pstmt = getConn().prepareStatement("UPDATE `"+database+"`.`mms_log` SET mt_ack=1, DLRArriveTimeStamp=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'), DLRReportStatus=?  WHERE  txID = ?",Statement.RETURN_GENERATED_KEYS);
				
				}else{
					
					pstmt = conn.prepareStatement("UPDATE `"+database+"`.`mms_log` SET mt_ack=1, DLRArriveTimeStamp=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'), DLRReportStatus=?  WHERE  txID = ?",Statement.RETURN_GENERATED_KEYS);
				
				}
				
				pstmt.setString(1, report.getStatusText()+"_"+report.getStatusCode());
				
				pstmt.setBigDecimal(2, new BigDecimal(report.getCMP_Txid()));
				
				success = pstmt.executeUpdate()>0;
				
				logger.debug("MT (CMP_Txid = "+report.getCMP_Txid()+ ") " + (success ? "successfully acknowledged" : "acknowledging failed!"));
				
			}
			
			
		} catch (SQLException e) {
			
			log(e);
		
		} catch (InterruptedException e) {
			
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
			
			closeConnectionIfNecessary(conn);	
		
		}
		
		return success;
		
	}
	
	
	private void log(Exception e){
		logger.error(e.getMessage(),e);
	}

	public void myfinalize(){
		
		try {
			
			if(conn!=null)
				conn.close();
			
		} catch (SQLException e) {
			
			log(e);
		
		}
		
		

		try {
			
			if(pool!=null)
				pool.release();
			
		} catch (Exception e) {
			
			log(e);
		
		}
	
	
	}
	
	
	
	/**
	 * Gets a connection from the pool
	 * @return java.sql.Connection
	 * @throws SQLException
	 * @throws InterruptedException 
	 */
	private Connection getConn() throws SQLException, InterruptedException{
		
		try{
				
			if(ds==null){
				
			}
			if(ds!=null){
				return ds.getConnection();
			}else{
				return getConnection();
			}
			
		}finally{
			
			
		
		}
	
	}
	
	
	/**
	 * Gets the connection more reliably.
	 * If it is not closed or null, return the existing connection object,
	 * else create one and return it
	 * @return java.sql.Connection object
	 * @throws InterruptedException 
	 */
	private Connection getConnection() throws InterruptedException {
			
		
		try {
			if(conn!=null && !conn.isClosed()){
				conn.setAutoCommit(true);
			}
		} catch (SQLException e1) {
			logger.warn("will create a connection");
		}
		
		while( true ) {
			
			try {
				while ( conn==null || conn.isClosed() ) {
					try {
						conn = ds.getConnection();//getConn();
						logger.debug("created connection! ");
						return conn;
					} catch ( Exception e ) {
						logger.warn("Could not create connection. Reason: "+e.getMessage());
						try { Thread.sleep(500); } catch ( Exception ee ) {}
					}
				}
				if(conn!=null)
					return conn;
			} catch ( Exception e ) {
				logger.warn("can't get a connection, re-trying");
				try { Thread.sleep(500); } catch ( Exception ee ) {}
			}
		}
		
	}
	
	
	/**
	 * TODO Confirm if its best to have this method synchronized
	 * @param conn
	 */
	public void closeConnectionIfNecessary(Connection conn){
		
		try {
			
			semaphore.acquire();
			
			if(this.ds!=null && !this.connectionObjIsCached){//If we're using a datasource, better close the connection
				
				
				try {
					logger.debug("We're closing connection.");
					if(conn!=null)
						conn.close();
				
				} catch (SQLException e) {
					
					log(e);
				
				}
				
			}else if(this.conStr!=null){
				//If we're using a connection 
				//string, then it means that 
				//we are maintaining a connection object
				//there is no need to close the connection object
			}/*else if(connectionPool!=null){//If its the custom connection pool, free the connection
				
				connectionPool.free(conn);
			
			}*/
			
		
		
		} catch (InterruptedException e) {
			
			log(e);
		
		} finally{
			
			semaphore.release();
			
		}
		
		
	}


	@Override
	public boolean deleteMMSMTToSend(String id) {
		
		//Connection conn = null;
		logger.info("DELETE FROM `"+database+"`.`mms_to_send` WHERE id = "+id);
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try {
			
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("DELETE FROM `"+database+"`.`mms_to_send` WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("DELETE FROM `"+database+"`.`mms_to_send` WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, String.valueOf(id));
			
			success = pstmt.executeUpdate()>0;
			
		} catch (SQLException e) {
			
			log(e);
		
		} catch (InterruptedException e) {
			
			log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			closeConnectionIfNecessary(conn);
			
		}
		
		return success;
	}


	@Override
	public BlockingDeque<MMS> getLatestMTMMS(int limit) {
		
		BlockingDeque<MMS> mtmmsq = null;//new ArrayList<MOSms>();
		
		//Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		MMS mtmms = null;
		
		
		try{
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("SELECT * FROM `"+database+"`.`mms_to_send` WHERE sent = 0 AND inProcessingQueue=0  ORDER BY `timeStampOfInsertion` asc"+(limit>0 ? (" LIMIT "+limit) : ("")),Statement.RETURN_GENERATED_KEYS);
				
					
			}else{
				
				conn = getConn();
				
				     pstmt = conn.prepareStatement("SELECT * FROM `"+database+"`.`mms_to_send` WHERE sent = 0 AND inProcessingQueue=0  ORDER BY `timeStampOfInsertion` asc"+(limit>0 ? (" LIMIT "+limit) : ("")),Statement.RETURN_GENERATED_KEYS);
				
				
			}
			
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				
				if(rs.isFirst()){
					mtmmsq = new LinkedBlockingDeque<MMS>();
				}
				
				mtmms = new MMS();
				mtmms.setId(rs.getString("id"));
				mtmms.setMsisdn(rs.getString("msisdn"));
				mtmms.setSubject(rs.getString("subject"));
				mtmms.setMms_text(rs.getString("mms_text"));
				mtmms.setMedia_path(rs.getString("media_path"));
				mtmms.setServiceid(rs.getInt("serviceid"));
				mtmms.setShortcode(rs.getString("shortcode"));
				mtmms.setServicecode(rs.getString("servicecode"));
				mtmms.setLinked_id(rs.getString("linked_id"));
				mtmms.setEariest_delivery_time(rs.getString("earliest_delivery_time"));
				mtmms.setExpiry_date(rs.getString("expiry_date"));
				mtmms.setDistribution_indicator(String.valueOf(rs.getBoolean("distributable")));
				mtmms.setTimeStampOfInsertion(rs.getString("timeStampOfInsertion"));
				mtmms.setInprocessingQueue(rs.getBoolean("inProcessingQueue"));
				mtmms.setSent(rs.getBoolean("sent"));
				
				mtmmsq.putLast(mtmms);
				
			}
		
		
		}catch(SQLException e){
			
			log(e);
		
		} catch (InterruptedException e) {
			
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
			
			
			if(!connectionObjIsCached){
				
				try {
					
					if(conn!=null)
						conn.close();
				} catch (SQLException e) {
					log(e);
				}
			
			}
			
			closeConnectionIfNecessary(conn);
			
		}
		
		return mtmmsq;
		
	}


	@Override
	public boolean logMTMMS(MMS mms) {
		//Connection conn = null;
		PreparedStatement pstmt = null;
		boolean success = false;

		try {
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("INSERT INTO `"+database+"`.`mms_log`(txID,msisdn,subject,mms_text,media_path,serviceid,shortcode,linked_id,earliest_delivery_time,expiry_date," +
						"delivery_report_requested,servicecode,timeStampOfInsertion,distributable,sent,DLRReportStatus,DLRArriveTimeStamp) " +
						"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'),?,?,?,?) ON DUPLICATE KEY UPDATE DLRReportStatus = ?, DLRArriveTimeStamp=?",Statement.RETURN_GENERATED_KEYS);
					
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("INSERT INTO `"+database+"`.`mms_log`(txID,msisdn,subject,mms_text,media_path,serviceid,shortcode,linked_id,earliest_delivery_time,expiry_date," +
						"delivery_report_requested,servicecode,timeStampOfInsertion,distributable,sent,DLRReportStatus,DLRArriveTimeStamp) " +
						"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'),?,?,?,?) ON DUPLICATE KEY UPDATE DLRReportStatus = ?, DLRArriveTimeStamp=?",Statement.RETURN_GENERATED_KEYS);
			}
			
			
			pstmt.setString(1,String.valueOf(mms.getCMP_Txid()));
			pstmt.setString(2,mms.getMsisdn());
			pstmt.setString(3,mms.getSubject());
			pstmt.setString(4,mms.getMms_text());
			pstmt.setString(5,mms.getMediaPath());
			pstmt.setInt(6,mms.getServiceid());
			pstmt.setString(7,mms.getShortcode());
			pstmt.setString(8,mms.getLinked_id());
			pstmt.setString(9,mms.getEariest_delivery_time());
			pstmt.setString(10,mms.getExpiry_date());
			pstmt.setInt(11,mms.getDelivery_report_requested().equals("true")? 1 : 0);
			pstmt.setString(12,mms.getServicecode());
			pstmt.setInt(13,mms.getDistribution_indicator().equals("true")? 1 : 0);
			pstmt.setInt(14,(mms.isSent() ? 1 : 0));
			pstmt.setString(15,mms.getBillingStatus().toString());
			pstmt.setString(16,mms.getDlrArriveTime());
			pstmt.setString(17,mms.getBillingStatus().toString());
			pstmt.setString(18,mms.getDlrArriveTime());
			
			
			success = pstmt.executeUpdate()>1;
			
			
		} catch (SQLException e) {
			
			log(e);
			
		} catch (InterruptedException e) {
			
			log(e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			closeConnectionIfNecessary(conn);
			
		}
		
		return success;
		
	}


	
	
	
	

}
