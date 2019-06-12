package com.pixelandtag.api;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.inmobia.luckydip.api.LuckyDipFactory;
import com.inmobia.luckydip.api.LuckyDipI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.ProcessorType;
//import com.pixelandtag.connections.ConnectionPool;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.entities.Notification;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;

public class CelcomImpl implements CelcomHTTPAPI, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1562L;
	private Logger logger = Logger.getLogger(CelcomImpl.class);
	private DataSource ds = null;
	private DBPoolDataSource dbpds = null;
	private Connection conn = null;
	private String conStr = null;
	private boolean connectionObjIsCached = true;
	private String fr_tz = "-05:00";
	private String to_tz = "+03:00";
	
	private Semaphore semaphore;
	private String BIGSPACER = ", ";
	private String RM0 = "RM0 ";
	private String MINUS_ONE = "-1";
	private final String RM1 = "RM1";
	private LuckyDipI lucky_dip = null;
	private String VOUCHER_TAG = "<VOUCHER_NUMBER>";
	private DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:m:ss");
	
	
	
	public void myfinalize(){
		
			try {
				
				if(conn!=null)
					conn.close();
				
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			try {
				
				dbpds.releaseConnectionPool();
				
			} catch (Exception e) {
				
				log(e);
			
			}
		
	}
	
	public CelcomImpl(String conStr_, String poolName) throws Exception{
		
		semaphore = new Semaphore(1,true);
		
		this.conStr = conStr_;
		
		connectionObjIsCached = false;
		
		int vendor = DriverUtilities.MYSQL;
	    String driver = DriverUtilities.getDriver(vendor);
	   
	    String dbName = "cmp";// HTTPMTSenderApp.props.getProperty("DATABASE");
	   
	    String host =  "db";//HTTPMTSenderApp.props.getProperty("db_host");
	    String url = DriverUtilities.makeURL(host, dbName, vendor);
	   // String username = "root";//"pixeland_content";//HTTPMTSenderApp.props.getProperty("db_username");
	    //String password = "Admin123#@!";//Admin123#@! HTTPMTSenderApp.props.getProperty("db_password");
	    
	    logger.info("********** db dbName : "+dbName);
	    logger.info("********** db host : "+host);
	    logger.info("********** db url : "+url);
	    logger.info("********** db conStr_ : "+conStr_);
	    
	    String msg = "";
	    
	   try {
			
			
		    
		    
			dbpds = new DBPoolDataSource();
			dbpds.setValidatorClassName("snaq.db.Select1Validator");
			dbpds.setName("celcom-impl");
			dbpds.setDescription("Impl Pooling DataSource");
			dbpds.setDriverClassName("com.mysql.jdbc.Driver");
			dbpds.setUrl(conStr_);
			//dbpds.setUser(username);
			//dbpds.setPassword(password);
			dbpds.setMinPool(1);
			dbpds.setMaxPool(2);
			dbpds.setMaxSize(3);
			dbpds.setIdleTimeout(3600);  // Specified in seconds.
		    
			dbpds.setValidationQuery("SELECT COUNT(*) FROM `"+database+"`.`sms_service`");
			
			logger.info("Initialized db pool ok! celcom mimpl line 131");
			
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
		
		}finally{}
	
	}

	

	private void initLuckyDip() {
		
		if(lucky_dip!=null)
			return;
		
		Connection conn = null;
		
		try{
			
			String classname = null;
			
			if(connectionObjIsCached){
			
				classname =  com.inmobia.util.Utils.getSetting("processor_class", getConn());
			
			}else{
				
				conn = getConn();
				
				classname =  com.inmobia.util.Utils.getSetting("processor_class", conn);
			
			}
			
			lucky_dip  =  LuckyDipFactory.getLuckyDipClass(classname);
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			closeConnectionIfNecessary(conn);
		}
		
	}

	public CelcomImpl(DataSource ds_) throws Exception{
		
		semaphore = new Semaphore(1,true);
		
		if(ds_==null){
			throw new Exception("DS Being passed is null");
		}
		connectionObjIsCached = false;
		semaphore = new Semaphore(1,true);
		
		this.ds = ds_;
		
	
	}
	
	
	

	

	/**
	 * @param e
	 */
	private void log(Exception e){
		logger.error(e.getMessage(),e);
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

	/* (non-Javadoc)
	 * @see com.pixelandtag.CelcomHTTPAPI#deleteMT(int)
	 */
	public boolean deleteMT(long id) {
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		try {
			
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("DELETE FROM `"+database+"`.`httptosend` WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("DELETE FROM `"+database+"`.`httptosend` WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
			
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
	
	
	
	public void updateSMSStatLog(Notification notif) {
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean success = false;
		
		
		try {
			
			String cmpTxid = notif.getCmp_tx_id();
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("SELECT CMP_Txid FROM `celcom`.`messagelog` WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)",Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, notif.getCmp_tx_id());
				pstmt.setString(2, notif.getCmp_tx_id());
				rs = pstmt.executeQuery();
				if(rs.next()){
					cmpTxid = rs.getString("CMP_Txid");
				}
				
				if(rs!=null)
					rs.close();
				if(pstmt!=null)
					pstmt.close();
				
				pstmt = getConn().prepareStatement("SELECT price,CMP_SKeyword FROM `"+database+"`.`SMSStatLog` WHERE transactionID = ?",Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT CMP_Txid FROM `celcom`.`messagelog` WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)",Statement.RETURN_GENERATED_KEYS);
				pstmt.setBigDecimal(1, new BigDecimal(notif.getCmp_tx_id()));
				pstmt.setBigDecimal(2, new BigDecimal(notif.getCmp_tx_id()));
				rs = pstmt.executeQuery();
				if(rs.next()){
					cmpTxid = rs.getString("CMP_Txid");
				}
				
				if(rs!=null)
					rs.close();
				if(pstmt!=null)
					pstmt.close();
				
				pstmt = conn.prepareStatement("SELECT price,CMP_SKeyword FROM `"+database+"`.`SMSStatLog` WHERE transactionID = ?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setBigDecimal(1, new BigDecimal(cmpTxid));
			
			rs = pstmt.executeQuery();
			
			double priceTbc = 0.0d;
			boolean wasInLog = false;
			
			if(rs.next()){
				
				priceTbc = 0.0d;//TarrifCode.get(rs.getString(2).trim()).getPrice();
				
				wasInLog  = true;
				
				logger.debug("Statlog rec found transactionID = "+cmpTxid);
			
			}else{
				
				logger.warn("Statlog rec with transactionID = "+cmpTxid+ " NOT found! Try search celcom.messagelog");
				
				
				try {
					
					if(connectionObjIsCached){
						pstmt = getConn().prepareStatement("SELECT SUB_Mobtel,CMP_Keyword,CMP_SKeyword,serviceid,price from celcom.messagelog WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)");
					}else{
						pstmt = conn.prepareStatement("SELECT SUB_Mobtel,CMP_Keyword,CMP_SKeyword,serviceid,price from celcom.messagelog WHERE (CMP_Txid = ?)  OR (newCMP_Txid = ?)");
					}
					
					pstmt.setBigDecimal(1, new BigDecimal(cmpTxid));
					pstmt.setBigDecimal(2, new BigDecimal(cmpTxid));
					
					rs = pstmt.executeQuery();
					
					String msisdn = "UNKNOWN",CMP_Keyword= "UNKNOWN",CMP_SKeyword= "UNKNOWN";
					
					int serviceid = -1;
					
					if(rs.next()){
						msisdn = rs.getString("SUB_Mobtel");
						CMP_Keyword = rs.getString("CMP_Keyword");
						CMP_SKeyword = rs.getString("CMP_SKeyword");
						serviceid = rs.getInt("serviceid");
						priceTbc = 0.0d;//TarrifCode.get(CMP_SKeyword.trim()).getPrice();
						
							
					}
					
					if(connectionObjIsCached){
						
						pstmt = getConn().prepareStatement("INSERT INTO `"+database+"`.`SMSStatLog`(SMSServiceID,msisdn,transactionID, CMP_Keyword, CMP_SKeyword,price,statusCode,charged) " +
								"VALUES(?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
						
					}else{
						
						pstmt = conn.prepareStatement("INSERT INTO `"+database+"`.`SMSStatLog`(SMSServiceID,msisdn,transactionID, CMP_Keyword, CMP_SKeyword,price,statusCode,charged) " +
									"VALUES(?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
						
					}
					
					pstmt.setInt(1, serviceid);
					pstmt.setString(2, msisdn);
					pstmt.setBigDecimal(3, new BigDecimal(cmpTxid));
					pstmt.setString(4, CMP_Keyword);
					pstmt.setString(5, CMP_SKeyword);
					pstmt.setDouble(6, priceTbc);
					pstmt.setString(7, notif.getErrorCode().toString());
					pstmt.setInt(8, (((priceTbc>0.0d) && notif.getErrorCode().equals(ERROR.Success)) ? 1: 0 )   );
					
					pstmt.executeUpdate();
					
					logger.debug("______CMP_SKeyword="+CMP_SKeyword+"________cmpTxid = "+cmpTxid+"________priceTbc = "+priceTbc+"_________________did not find in smsStatLog but no problem, we found in messagelog__________________________________________SUCCESSFULLY_INSERTED_INTO_SMSStatLog___________________________________________________________________________________");
					
				} catch (SQLException e) {
					
					log(e);
				
				} catch (Exception e) {
					
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
			
			
			
			//Only if the message exists in SMSStatLog
			if(wasInLog){
				
				if(connectionObjIsCached){
					
					pstmt = getConn().prepareStatement("UPDATE `"+database+"`.`SMSStatLog` SET  statusCode=?, charged=?  WHERE  transactionID = ?",Statement.RETURN_GENERATED_KEYS);
				
				}else{
					
					pstmt = conn.prepareStatement("UPDATE `"+database+"`.`SMSStatLog` SET statusCode=?,  charged=? WHERE  transactionID = ?",Statement.RETURN_GENERATED_KEYS);
				
				}
				
				pstmt.setString(1, notif.getErrorCode().toString());
				pstmt.setInt(2, (((priceTbc>0.0) && notif.getErrorCode().equals(ERROR.Success)) ? 1: 0 )   );
				pstmt.setBigDecimal(3, new BigDecimal(cmpTxid));
				
				success = pstmt.executeUpdate()>0;
				
				logger.debug("MT (transactionID = "+cmpTxid+ ") " + (success ? "successfully logged into SMSStatLog" : "failed to log into SMSStatLog"));
				
			}
			
			
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
		
	}
	
	
	
	

	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.CelcomHTTPAPI#logMT(com.pixelandtag.MT)
	 */
	public void logMT(MTsms mt) {
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		try {
			
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("INSERT INTO `"+database+"`.`messagelog`(CMP_Txid,MT_Sent,SMS_SourceAddr,SUB_Mobtel,SMS_DataCodingId,CMPResponse,APIType,CMP_Keyword,CMP_SKeyword,MT_STATUS,number_of_sms,msg_was_split,MT_SendTime,mo_ack,serviceid,price,newCMP_Txid,mo_processor_id_fk,price_point_keyword) " +
						"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'),1,?,?,?,?,?) ON DUPLICATE KEY UPDATE MT_Sent = ?, mo_ack=1, MT_SendTime=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'), MT_STATUS = ?, number_of_sms = ?, msg_was_split=?, serviceid=? , price=?, SMS_DataCodingId=?, CMPResponse=?, APIType=?, newCMP_Txid=?, CMP_SKeyword=?, mo_processor_id_fk=?, price_point_keyword=?",Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("INSERT INTO `"+database+"`.`messagelog`(CMP_Txid,MT_Sent,SMS_SourceAddr,SUB_Mobtel,SMS_DataCodingId,CMPResponse,APIType,CMP_Keyword,CMP_SKeyword,MT_STATUS,number_of_sms,msg_was_split,MT_SendTime,mo_ack,serviceid,price,newCMP_Txid,mo_processor_id_fk,price_point_keyword) " +
						"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'),1,?,?,?,?,?) ON DUPLICATE KEY UPDATE MT_Sent = ?, mo_ack=1, MT_SendTime=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'), MT_STATUS = ?, number_of_sms = ?, msg_was_split=?, serviceid=? , price=?, SMS_DataCodingId=?, CMPResponse=?, APIType=?, newCMP_Txid=?, CMP_SKeyword=?, mo_processor_id_fk=?, price_point_keyword=?",Statement.RETURN_GENERATED_KEYS);
			}
			
			String txid = mt.getIdStr();
			if(!mt.getCmp_tx_id().isEmpty()){
				
				if(!(mt.getCmp_tx_id().isEmpty())){
					txid = String.valueOf(mt.getCmp_tx_id());
					
					//if(mt.getNewCMP_Txid().equals(MINUS_ONE))
						pstmt.setString(1, txid);//Since we're starting the transaction, what is in the httptosend as id now becomes the value of CMP_Txid.
					//else	
					//	pstmt.setString(1, mt.getNewCMP_Txid());//Since we're starting the transaction, what is in the httptosend as id now becomes the value of CMP_Txid.
					
						
				}else{
					txid = mt.getIdStr();
					//if(mt.getNewCMP_Txid().equals(MINUS_ONE))
						pstmt.setString(1, txid);//Since we're starting the transaction, what is in the httptosend as id now becomes the value of CMP_Txid.
					//else
						//pstmt.setString(1, mt.getNewCMP_Txid());
				}
				
			}else{
				
				//if(mt.getNewCMP_Txid().equals(MINUS_ONE)){
					pstmt.setString(1, mt.getIdStr());
					txid = mt.getIdStr();
				//}else{
					//pstmt.setString(1, mt.getNewCMP_Txid());
					//txid = mt.getNewCMP_Txid();
				//}
			}
			
			boolean isRetry = !mt.getNewCMP_Txid().equals(MINUS_ONE);
			
			pstmt.setString(2, mt.getSms());
			pstmt.setString(3, mt.getFromAddr());//.getSMS_SourceAddr());//Shortcode sent with.
			pstmt.setString(4, mt.getMsisdn());//.getSUB_Mobtel());
			pstmt.setString(5, mt.getSMS_DataCodingId());//SMS_DataCodingId
			pstmt.setString(6, mt.getCMPResponse());//CMPResponse
			pstmt.setString(7, mt.getAPIType());//APIType,
			pstmt.setString(8, mt.getCMP_AKeyword());//CMP_Keyword
			pstmt.setString(9, mt.getCMP_SKeyword());//CMP_SKeyword
			pstmt.setString(10, mt.getMT_STATUS());//MT_STATUS
			pstmt.setInt(11, mt.getNumber_of_sms());//number_of_sms
			pstmt.setInt(12, (mt.isSplit_msg() ? 1 : 0));//number_of_sms
			pstmt.setInt(13, mt.getServiceid());//serviceid
			if(mt.getPrice()!=null)
				pstmt.setDouble(14, 1.0d);//price
			else
				pstmt.setDouble(14, mt.getPrice().doubleValue());//price
			pstmt.setString(15, mt.getNewCMP_Txid());//new CMPTxid
			pstmt.setInt(16, mt.getProcessor_id().intValue());//processor id
			pstmt.setString(17, mt.getPricePointKeyword());//processor id
			pstmt.setString(18, mt.getSms());//SMS
			
			if(isRetry)
				pstmt.setString(19, ERROR.PSAInsufficientBalance.toString());//MT_STATUS
			else
				pstmt.setString(19, mt.getMT_STATUS());//MT_STATUS
			
			pstmt.setInt(20, mt.getNumber_of_sms());//number_of_sms
			pstmt.setInt(21, (mt.isSplit_msg() ? 1 : 0));//number_of_sms
			pstmt.setInt(22, mt.getServiceid());//serviceid
			
			if(mt.getPrice()!=null)
				pstmt.setDouble(23, 1.0d);//price
			else
				pstmt.setDouble(23, mt.getPrice().doubleValue());//price
			
			pstmt.setString(24, mt.getSMS_DataCodingId());//SMS_DataCodingId
			pstmt.setString(25, mt.getCMPResponse());//CMPResponse
			pstmt.setString(26, mt.getAPIType());//APIType,
			pstmt.setString(27, mt.getNewCMP_Txid());//new CMPTxid
			
			if(mt.getSms().startsWith(RM1))
				pstmt.setString(28, "RMRMRM");//CMP_SKeyword
			else
				pstmt.setString(28, mt.getCMP_SKeyword());//CMP_SKeyword
			
			pstmt.setInt(29, mt.getProcessor_id().intValue());//CMP_SKeyword
			
			pstmt.setString(30, mt.getPricePointKeyword());//CMP_SKeyword
			
			
			pstmt.executeUpdate();
			
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
		
		
	}

	/* (non-Javadoc)
	 * @see com.pixelandtag.CelcomHTTPAPI#acknowledgeReceipt(com.pixelandtag.MO)
	 */
	public void acknowledgeReceipt(IncomingSMS mo) {
		
		
	}
	

	/* (non-Javadoc)
	 * @see com.pixelandtag.CelcomHTTPAPI#findMT(java.lang.String)
	 */
	public MTsms findMT(String cpm_txId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * Gets a connection from the pool
	 * @return java.sql.Connection
	 * @throws SQLException
	 * @throws InterruptedException 
	 */
	private Connection getConn() throws SQLException, InterruptedException{
		
		try{
			
			
				
			Connection conn_ = null;
			if(ds!=null){
				
				//logger.info("************TRYIG TO GET CONN from DS**********");
				
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				try{
					conn_ =  ds.getConnection();
					pstmt  = conn_.prepareStatement("select now()");
					rs = pstmt.executeQuery();
					
					if(rs.next())
						rs.getString(1);
					
				}catch(Exception e){
					
					logger.error(e.getMessage());
					//logger.info("************ FAILED TO GET connection from ds.. trying to get DS again.. **********");
					
					try {
						InitialContext initContext = new InitialContext();
						DataSource ds_ = (DataSource) initContext.lookup("java:/cmpDS");
						conn_ =  ds_.getConnection();
					} catch (NamingException e1) {
						logger.error(e.getMessage());
					}
				}finally{
					try{
						rs.close();
					}catch(Exception e){}
					try{
						pstmt.close();
					}catch(Exception e){}
					
				}
				
				return conn_;
			}else{
				return getConnection();
			}
			
		}finally{
			
			
		
		}
	
	}
	
	
	
	
	
	/**
	 * Gets the connection a new one.
	 * If it is not closed or null, return the existing connection object,
	 * else create one and return it
	 * @return java.sql.Connection object
	 * @throws InterruptedException 
	 */
	public Connection getConnection() {
			
		Connection conn_ = null;
		
		while( true ) {
			
			try {
				while ( conn_==null || conn_.isClosed() ) {
					try {
						conn_ = dbpds.getConnection();
						logger.debug("created connection! ");
						return conn_;
					} catch ( Exception e ) {
						logger.warn("Could not create connection. Reason: "+e.getMessage());
						try { Thread.sleep(500); } catch ( Exception ee ) {}
					}
				}
				return conn_;
			} catch ( Exception e ) {
				logger.warn("can't get a connection, re-trying");
				try { Thread.sleep(500); } catch ( Exception ee ) {}
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.pixelandtag.CelcomHTTPAPI#markInQueue(int)
	 */
	public boolean markInQueue(long http_to_send_id) throws Exception {
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		
		try {
			
			
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("UPDATE `"+database+"`.`httptosend` SET in_outgoing_queue = 1 WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("UPDATE `"+database+"`.`httptosend` SET in_outgoing_queue = 1 WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, String.valueOf(http_to_send_id));
			
			success = pstmt.executeUpdate()>0;
			
		} catch (SQLException e) {
			
			log(e);
			
			throw new Exception(e.getMessage());
			
		}catch(Exception e){
			
			//any other exception
			log(e);
			
			throw new Exception(e.getMessage());
		
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

	/* (non-Javadoc)
	 * @see com.pixelandtag.CelcomHTTPAPI#changeQueueStatus(int, boolean)
	 */
	public boolean changeQueueStatus(String http_to_send_id, boolean inQueue) {
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		
		try {
			
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("UPDATE `"+database+"`.`httptosend` SET in_outgoing_queue = "+(inQueue?"1":"0")+" WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
				
			}else{
				conn = getConn();
				pstmt = conn.prepareStatement("UPDATE `"+database+"`.`httptosend` SET in_outgoing_queue = "+(inQueue?"1":"0")+" WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, http_to_send_id);
			
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

	public boolean markSent(long http_to_send_id) {
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		
		try {
			
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("UPDATE `"+database+"`.`httptosend` SET `sent` = 1 WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
					
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("UPDATE `"+database+"`.`httptosend` SET `sent` = 1 WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, String.valueOf(http_to_send_id));
			
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

	public String toHex(String arg) throws UnsupportedEncodingException {
	    //return String.format("%x", new BigInteger(arg.getBytes("UTF8")));
		return Hex.encodeHexString(arg.getBytes("UTF8"));
	}

	
	
	
	public static String hexToString(String txtInHex){
		
        byte [] txtInByte = new byte [txtInHex.length() / 2];
        int j = 0;
        for (int i = 0; i < txtInHex.length(); i += 2)
        {
                txtInByte[j++] = Byte.parseByte(txtInHex.substring(i, i + 2), 16);
        }
        return new String(txtInByte);
    }
	
	public String toUnicodeString(String sms) {
		
		byte[] array = sms.getBytes();
		
		String op = "";
		
		int arl = array.length;
		
		for (int k = 0; k < arl; k++) {
	    		op += (   "\\" + "u00" + UnicodeFormatter.byteToHex(array[k]) );
	    }
		
		return op;
	}
	
	public static String tUc(String sms) {
		
		byte[] array = sms.getBytes();
		
		String op = "";
		
		int arl = array.length;
		
		for (int k = 0; k < arl; k++) {
	    		op += (   "\\" + "u00" + UnicodeFormatter.byteToHex(array[k]) );
	    }
		
		return op;
	}
	
	
	
	public static void main(String[] ar){
		try {
			double e = 0.3;
			
			System.out.println(e>0);
			
			System.out.println(hexToString("0067006900660074"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * TODO Confirm if its best to have this method synchronized
	 * @param conn
	 */
	public void closeConnectionIfNecessary(Connection conn){
		
		try {
			
			semaphore.acquire();
			
			if(this.ds!=null || !this.connectionObjIsCached){//If we're using a datasource, better close the connection
				
				try {
					
					if(conn!=null)
						conn.close();
				
				} catch (Exception e) {
					
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
	
	
	
	/**
	 * TODO Confirm if its best to have this method synchronized
	 * @param conn
	 */
	public void closeConnectionIfNecessary(){
		/*
		if(this.ds!=null){//If we're using a datasource, better close the connection
			
			try {
				
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
			//pool.release();
		}*/
		
	}

	public boolean containsUnicode(String sms) {
		
		boolean has_non_unicode = false;
		
		if(sms==null)
			return false;
		
		char[] chars = sms.toCharArray();
		int l = chars.length;
		
		for(int i=0; i<l; i++)
			if(chars[i] > 128)
				has_non_unicode =  true;
		
		return has_non_unicode;
		
	}

		
	
	private Date stringToTimeStamp(String timestamp) {
		if(timestamp!=null && !timestamp.isEmpty())
			try {
				return format.parse(timestamp);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		return null;
	}

	
	public  boolean postponeMT(long http_to_send_id) {
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		
		try {
			
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("UPDATE `"+database+"`.`httptosend` SET `sent` = 0, in_outgoing_queue=0  WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
					
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("UPDATE `"+database+"`.`httptosend` SET `sent` = 0, in_outgoing_queue=0 WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, String.valueOf(http_to_send_id));
			
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

			try{
				notify();
			}catch(Exception e){
				logger.warn(e);
			}
			
		}
		
		
		return success;
	}

	/* (non-Javadoc)
	 * @see com.pixelandtag.CelcomHTTPAPI#getServiceProcessors()
	 */
	public Queue<ServiceProcessorDTO> getServiceProcessors() {

		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		Queue<ServiceProcessorDTO>  services = null;
		
		
		try {
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("SELECT `mop`.id,`mop`.ServiceName,`mop`.ProcessorClass,`mop`.enabled,`mop`.class_status,`mop`.shortcode,`mop`.threads, `smss`.CMP_Keyword, `smss`.CMP_SKeyword, group_concat(`smss`.`cmd`) as `keywords`,  `smss`.`subscriptionText` as 'subscriptionText', `smss`.`unsubscriptionText` as 'unsubscriptionText', `smss`.`tailText_subscribed` as 'tailText_subscribed', `smss`.`tailText_notsubscribed` as 'tailText_notsubscribed' , `mop`.`processor_type` as 'processor_type' , `mop`.`forwarding_url` as 'forwarding_url', `mop`.`protocol` as 'protocol', coalesce(`mop`.`smppid`,-1,`mop`.`smppid`) as 'smppid'  FROM `"+database+"`.`mo_processors` `mop` LEFT JOIN `"+database+"`.`sms_service` `smss` ON `smss`.`mo_processorFK`=`mop`.`id` WHERE `mop`.`enabled`=1 AND `mop`.`processor_type` <> 'PHANTOM' group by `mop`.`id`", Statement.RETURN_GENERATED_KEYS);//"SELECT * FROM `"+DATABASE+"`.`mo_processors` WHERE enabled=1");
			
			}else{
				
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT `mop`.id,`mop`.ServiceName,`mop`.ProcessorClass,`mop`.enabled,`mop`.class_status,`mop`.shortcode,`mop`.threads, `smss`.CMP_Keyword, `smss`.CMP_SKeyword, group_concat(`smss`.`cmd`) as `keywords`,  `smss`.`subscriptionText` as 'subscriptionText', `smss`.`unsubscriptionText` as 'unsubscriptionText', `smss`.`tailText_subscribed` as 'tailText_subscribed', `smss`.`tailText_notsubscribed` as 'tailText_notsubscribed', `mop`.`processor_type` as 'processor_type' , `mop`.`forwarding_url` as 'forwarding_url', `mop`.`protocol` as 'protocol', coalesce(`mop`.`smppid`,-1,`mop`.`smppid`) as 'smppid'  FROM `"+database+"`.`mo_processors` `mop` LEFT JOIN `"+database+"`.`sms_service` `smss` ON `smss`.`mo_processorFK`=`mop`.`id` WHERE `mop`.`enabled`=1 AND `mop`.`processor_type` <> 'PHANTOM' group by `mop`.`id`", Statement.RETURN_GENERATED_KEYS);//"SELECT * FROM `"+DATABASE+"`.`mo_processors` WHERE enabled=1");
				
			}
			
			
			rs = pstmt.executeQuery();
			
			ServiceProcessorDTO service;
			
			while(rs.next()){
				
				service = new ServiceProcessorDTO();
				
				if(rs.isFirst()){
					services = new LinkedList<ServiceProcessorDTO>();
				}
				
				service.setSubscriptionText(rs.getString("subscriptionText"));
				service.setUnsubscriptionText(rs.getString("unsubscriptionText"));
				service.setTailTextSubscribed(rs.getString("tailText_subscribed"));
				service.setTailTextNotSubecribed(rs.getString("tailText_notsubscribed"));
				service.setId(rs.getInt("id"));
				service.setServiceName(rs.getString("ServiceName"));
				service.setProcessorClass(rs.getString("ProcessorClass"));
				service.setActive(rs.getBoolean("enabled"));
				service.setClass_status(rs.getString("class_status"));
				service.setShortcode(rs.getString("shortcode"));
				if(rs.getString("keywords")!=null)
					service.setKeywords(rs.getString("keywords").split(","));
				service.setForwarding_url(rs.getString("forwarding_url"));
				service.setProcessor_type(ProcessorType.fromString(rs.getString("processor_type")));
				service.setProtocol(rs.getString("protocol"));
				service.setSmppid(Long.valueOf(rs.getInt("smppid")));
				service.setServKey(service.getProcessorClassName()+"_"+service.getId()+"_"+service.getShortcode());
				
				service.setThreads(rs.getInt("threads"));
				services.add(service);
				
				
			}
			
			
			
		} catch (Exception e) {
			
			log(e);
			
		}finally{
			
			
			try {
				
				if(rs!=null)
					rs.close();
			
			} catch (Exception e) {
				
				log(e);
			
			}
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (Exception e) {
				
				log(e);
			
			}
			
			closeConnectionIfNecessary(conn);
			
		}
		
		return services;
	}

	
	public String replaceAllIllegalCharacters(String text){
		
		if(text==null)
			return null;
		
		text = text.replaceAll("[\\r]", "");
		text = text.replaceAll("[\\n]", "");
		text = text.replaceAll("[\\t]", "");
		text = text.replaceAll("[.]", "");
		text = text.replaceAll("[,]", "");
		text = text.replaceAll("[?]", "");
		text = text.replaceAll("[@]", "");
		text = text.replaceAll("[\"]", "");
		text = text.replaceAll("[\\]]", "");
		text = text.replaceAll("[\\[]", "");
		text = text.replaceAll("[\\{]", "");
		text = text.replaceAll("[\\}]", "");
		text = text.replaceAll("[\\(]", "");
		text = text.replaceAll("[\\)]", "");
		text = text.trim();
		
		return text;
		
	}


		
	/* (non-Javadoc)
	 * @see com.pixelandtag.CelcomHTTPAPI#beingProcessedd(long, boolean)
	 */
	public boolean  beingProcessedd(long http_to_send_id, boolean processing) {
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		boolean success = false;
		
		
		try {
			
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("UPDATE `"+database+"`.`messagelog` SET `processing` = 1, mo_ack = 1 WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
					
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("UPDATE `"+database+"`.`messagelog` SET `processing` = 1, mo_ack = 1 WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, String.valueOf(http_to_send_id));
			
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
	
	

	public void logResponse(String msisdn, String responseText){
			
			PreparedStatement ps = null;
			
			Connection conn = null;
			
			try {
				
				if(connectionObjIsCached){
					
					ps = getConn().prepareStatement("INSERT INTO `"+database+"`.`raw_out_log`(msisdn,response) VALUES(?,?)",Statement.RETURN_GENERATED_KEYS);
				
				}else{
					
					conn = getConn();
					
					ps = conn.prepareStatement("INSERT INTO `"+database+"`.`raw_out_log`(msisdn,response) VALUES(?,?)",Statement.RETURN_GENERATED_KEYS);
					
				}
				ps.setString(1, msisdn);
				
				ps.setString(2, responseText);
				
				ps.executeUpdate();
				
			} catch (SQLException e) {
				
				logger.error(e.getMessage(),e);
				
			} catch (InterruptedException e) {
				
				logger.error(e.getMessage(),e);
			
			}finally{
				
				try {
					
					if(ps != null)
						ps.close();
					
				} catch (SQLException e) {
					
					logger.error(e.getMessage(),e);
					
				}
				
				closeConnectionIfNecessary(conn);
			}
			
		}
	
	
	

	@Override
	public void flagMMSIfAny(Notification notification) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		Connection conn = null;
		
		try{
			
			String cmpTxid = notification.getCmp_tx_id();
			
			if(connectionObjIsCached){
				
				ps = getConn().prepareStatement("SELECT CMP_Txid FROM `celcom`.`messagelog` WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)",Statement.RETURN_GENERATED_KEYS);
				ps.setBigDecimal(1, new BigDecimal(notification.getCmp_tx_id()));
				ps.setBigDecimal(2, new BigDecimal(notification.getCmp_tx_id()));
				rs = ps.executeQuery();
				if(rs.next()){
					cmpTxid = rs.getString("CMP_Txid");
				}
				
				if(rs!=null)
					rs.close();
				if(ps!=null)
					ps.close();
				
				
				
				ps = getConn().prepareStatement("SELECT id,tx_id_waiting_to_succeed_before_sending, paidFor, billingStatus FROM `"+database+"`.`mms_to_send` WHERE tx_id_waiting_to_succeed_before_sending = ?",Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				conn = getConn();
				
				ps = conn.prepareStatement("SELECT CMP_Txid FROM `celcom`.`messagelog` WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)",Statement.RETURN_GENERATED_KEYS);
				ps.setBigDecimal(1, new BigDecimal(notification.getCmp_tx_id()));
				ps.setBigDecimal(2, new BigDecimal(notification.getCmp_tx_id()));
				rs = ps.executeQuery();
				if(rs.next()){
					cmpTxid = rs.getString("CMP_Txid");
				}
				
				if(rs!=null)
					rs.close();
				if(ps!=null)
					ps.close();
				
				
				ps = conn.prepareStatement("SELECT id,tx_id_waiting_to_succeed_before_sending, paidFor, billingStatus FROM  `"+database+"`.`mms_to_send` WHERE tx_id_waiting_to_succeed_before_sending = ?",Statement.RETURN_GENERATED_KEYS);
				
			}
			
			ps.setBigDecimal(1, new BigDecimal(cmpTxid));
			
			rs = ps.executeQuery();
			
			boolean thereIs = false;
			int mms_log_id = -1;
			
			if(rs.next()){
				
				mms_log_id = rs.getInt("id");
				
				thereIs = true;
				logger.info("_______>>>>>>>>>>>>>>>>>>>>>_____________ WE FOUND MMS THAT NEEDS FLAGGING :: txID = "+notification.getCmp_tx_id());
			}
			
			
			if(thereIs){//If there is an MMS waiting to be paid for before it's sent. We mark it as paidFor
				
				
				if(connectionObjIsCached){
					
					ps = getConn().prepareStatement("UPDATE `"+database+"`.`mms_to_send` set paidFor=?, billingStatus=?, dlrReceived=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"') WHERE tx_id_waiting_to_succeed_before_sending = ? AND id = ?",Statement.RETURN_GENERATED_KEYS);
				
				}else{
					
					ps = conn.prepareStatement("UPDATE `"+database+"`.`mms_to_send` set paidFor=?, billingStatus=?, dlrReceived=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"') WHERE tx_id_waiting_to_succeed_before_sending = ? AND id = ?",Statement.RETURN_GENERATED_KEYS);
					
				}
				
				ps.setInt(1, (notification.getErrorCode().equals(ERROR.Success) ? 1 : 0) );
				ps.setString(2, notification.getErrorCode().toString());
				ps.setBigDecimal(3, new BigDecimal(cmpTxid));
				ps.setInt(4, mms_log_id);
				
				ps.executeUpdate();
				
			}
			
			
		}catch(Exception e){
			
			log(e);
		
		}finally{
			
			
			try {
				
				if(rs != null)
					rs.close();
				
			} catch (SQLException e) {
				
				log(e);
				
			}
			
			
			try {
				
				if(ps != null)
					ps.close();
				
			} catch (SQLException e) {
				
				log(e);
				
			}
			
			closeConnectionIfNecessary(conn);
			
		}
	}

	
	

	
	public String generateNextTxId(){
		try {
			Thread.sleep(112);
		} catch (InterruptedException e) {
			logger.warn("\n\t\t::"+e.getMessage());
		}
		return String.valueOf(System.currentTimeMillis());
	}
	
	
	

}
