package com.pixelandtag.api;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;











//import com.pixelandtag.connections.ConnectionPool;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.beans.Subscriber;
import com.pixelandtag.web.triviaI.MechanicsI;
import com.pixelandtag.web.triviaImpl.MechanicsS;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.entities.Notification;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.mms.api.TarrifCode;
import com.inmobia.luckydip.api.LuckyDipFactory;
import com.inmobia.luckydip.api.LuckyDipI;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.sms.producerthreads.MTProducer;

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
	private String fr_tz;
	private String to_tz;
	
	private Semaphore semaphore;
	private String BIGSPACER = ", ";
	private String RM0 = "RM0 ";
	private String MINUS_ONE = "-1";
	private final String RM1 = "RM1";
	private LuckyDipI lucky_dip = null;
	private String VOUCHER_TAG = "<VOUCHER_NUMBER>";
	
	
	
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
	   
	    String dbName = "pixeland_content360";// HTTPMTSenderApp.props.getProperty("DATABASE");
	   
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
			
			long cmpTxid = notif.getCMP_Txid();
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("SELECT CMP_Txid FROM `celcom`.`messagelog` WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)",Statement.RETURN_GENERATED_KEYS);
				pstmt.setLong(1, notif.getCMP_Txid());
				pstmt.setLong(2, notif.getCMP_Txid());
				rs = pstmt.executeQuery();
				if(rs.next()){
					cmpTxid = rs.getLong("CMP_Txid");
				}
				
				if(rs!=null)
					rs.close();
				if(pstmt!=null)
					pstmt.close();
				
				pstmt = getConn().prepareStatement("SELECT price,CMP_SKeyword FROM `"+database+"`.`SMSStatLog` WHERE transactionID = ?",Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT CMP_Txid FROM `celcom`.`messagelog` WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)",Statement.RETURN_GENERATED_KEYS);
				pstmt.setLong(1, notif.getCMP_Txid());
				pstmt.setLong(2, notif.getCMP_Txid());
				rs = pstmt.executeQuery();
				if(rs.next()){
					cmpTxid = rs.getLong("CMP_Txid");
				}
				
				if(rs!=null)
					rs.close();
				if(pstmt!=null)
					pstmt.close();
				
				pstmt = conn.prepareStatement("SELECT price,CMP_SKeyword FROM `"+database+"`.`SMSStatLog` WHERE transactionID = ?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setLong(1, cmpTxid);
			
			rs = pstmt.executeQuery();
			
			double priceTbc = 0.0d;
			boolean wasInLog = false;
			
			if(rs.next()){
				
				priceTbc = TarrifCode.get(rs.getString(2).trim()).getPrice();
				
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
					
					pstmt.setLong(1, cmpTxid);
					pstmt.setLong(2, cmpTxid);
					
					rs = pstmt.executeQuery();
					
					String msisdn = "UNKNOWN",CMP_Keyword= "UNKNOWN",CMP_SKeyword= "UNKNOWN";
					
					int serviceid = -1;
					
					if(rs.next()){
						msisdn = rs.getString("SUB_Mobtel");
						CMP_Keyword = rs.getString("CMP_Keyword");
						CMP_SKeyword = rs.getString("CMP_SKeyword");
						serviceid = rs.getInt("serviceid");
						priceTbc = TarrifCode.get(CMP_SKeyword.trim()).getPrice();
						
							
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
					pstmt.setLong(3, cmpTxid);
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
				pstmt.setLong(3, cmpTxid);
				
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
	
	
	
	public void acknowledgeDN(Notification notif) {
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		boolean success = false;
		
		int serviceid = 2;
		double price = 1.0;
		
		try {
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("SELECT mt_ack,SUB_Mobtel,serviceid,price,CMP_Txid,newCMP_Txid FROM `"+database+"`.`messagelog` WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)",Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				conn = getConn();
				
				    pstmt = conn.prepareStatement("SELECT mt_ack,SUB_Mobtel,serviceid,price,CMP_Txid,newCMP_Txid FROM `"+database+"`.`messagelog` WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, String.valueOf(notif.getCMP_Txid()));
			pstmt.setString(2, String.valueOf(notif.getCMP_Txid()));
			
			rs = pstmt.executeQuery();
			
			int mt_ack = -1;
			
			String msisdn  = null;
			long cMP_Txid = notif.getCMP_Txid();
			long newCMP_Txid; 
			
			boolean isRetry = false;
			
			
			boolean isthere = false;
			
			
			if(rs.next()){
				
				isthere = true;
				
				mt_ack = rs.getInt(1);
				
				msisdn = rs.getString(2);
				
				serviceid = rs.getInt("serviceid");
				
				price = rs.getDouble("price");
				
				cMP_Txid = rs.getLong("CMP_Txid");
				
				newCMP_Txid = rs.getLong("newCMP_Txid");
				
				isRetry = newCMP_Txid==notif.getCMP_Txid();
				
				logger.debug("SMS FOUND: CMP_Txid = "+cMP_Txid+" re-try CMP_Txid = "+notif.getCMP_Txid());
			
			}else{
				
				logger.warn("SMS with CMP_Txid = "+cMP_Txid+ " NOT found!");
				
			}
			
			if(rs!=null)
				rs.close();
			
			if(pstmt!=null)
				pstmt.close();
			
			//Only if the message exists and was not previously marked as acknowldedged
			if((mt_ack>-1) && ((mt_ack==0) || isthere)){
				
				if(connectionObjIsCached){
					
					pstmt = getConn().prepareStatement("UPDATE `"+database+"`.`messagelog` SET mt_ack=1, delivery_report_arrive_time=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'), MT_STATUS=?  WHERE  CMP_Txid = ?",Statement.RETURN_GENERATED_KEYS);
				
				}else{
					
					pstmt = conn.prepareStatement("UPDATE `"+database+"`.`messagelog` SET mt_ack=1, delivery_report_arrive_time=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'), MT_STATUS=?  WHERE CMP_Txid = ?",Statement.RETURN_GENERATED_KEYS);
				
				}
				
				pstmt.setString(1, notif.getErrorCode().toString());
				pstmt.setLong(2, cMP_Txid);
				
				success = pstmt.executeUpdate()>0;
				
				
				
				if(notif.getErrorCode().equals(ERROR.PSAInsufficientBalance) || notif.getErrorCode().equals(ERROR.PSANumberBarred)){
					
					logger.debug(">>> cMP_Txid: "+cMP_Txid+" >> insufficientBalance "+notif.getErrorCode());
					
					MechanicsS.toggleRetry(cMP_Txid, true, conn);
					
				try {
					
					if(msisdn!=null){
						
						
							Subscriber sub = null;
							
							if(connectionObjIsCached){
								sub = MechanicsS.getSubscriber(msisdn, getConn());
							}else{
								sub = MechanicsS.getSubscriber(msisdn, conn);
							}
						
							String msg = "";
							
							if(sub!=null){
								
								if(connectionObjIsCached){
									msg = RM0  + sub.getName() + BIGSPACER + UtilCelcom.getMessage(MessageType.INSUFFICIENT_BALANCE, getConn(), sub.getLanguage_id_() );
								}else{
									msg = RM0 + sub.getName() + BIGSPACER+ UtilCelcom.getMessage(MessageType.INSUFFICIENT_BALANCE, conn, sub.getLanguage_id_());
								}
								
							}else{
								
								if(connectionObjIsCached){
									
									msg = UtilCelcom.getMessage(MessageType.INSUFFICIENT_BALANCE, 2, getConn());
								
								}else{
									
									msg = UtilCelcom.getMessage(MessageType.INSUFFICIENT_BALANCE, 2, conn);
								
								}
								
							}
							
							logger.info(msg+" >>>>> [isretry: "+isRetry+"]<><><><><><><><><><><>[msisdn="+msisdn+"]<><><>[cMP_Txid="+cMP_Txid+"]<><><>[txid="+cMP_Txid+"]<><><><>:::::::::::::SUB HAS INSUFFICIENT BALANCE. WE SEND THEM A MESSAGE TO TELL THEM TO TOP UP, THEN WE MARK THE MT for RE-TRY::::::::::::::<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><");
							
							
							if(!isRetry){
								
								if(connectionObjIsCached){
									MechanicsS.insertIntoHttpToSend(msisdn, msg, MechanicsS.generateNextTxId(), serviceid, price, "22222", "IOD", "IOD0000", getConn());
									//MechanicsS.insertIntoHttpToSend(msisdn, msg, MechanicsS.generateNextTxId(), serviceid,0, getConn());
								}else{
									MechanicsS.insertIntoHttpToSend(msisdn, msg, MechanicsS.generateNextTxId(), serviceid, price, "22222", "IOD", "IOD0000", conn);
									//MechanicsS.insertIntoHttpToSend(msisdn, msg, MechanicsS.generateNextTxId(),serviceid,0,conn);
								}
							}
							
							
							
					}
					
				} catch (Exception e) {
					
					log(e);
					
				}
			}
				
				
				logger.debug("MT (CMP_Txid = "+notif.getCMP_Txid()+ ") " + (success ? "successfully acknowledged" : "acknowledging failed!"));
				
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
		
	}
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.CelcomHTTPAPI#retrieveMO(java.lang.String)
	 */
	public MOSms retrieveMO(long cMP_Txid) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		MOSms mo =  null;
		
		try {
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("SELECT * FROM `"+database+"`.`messagelog` WHERE  CMP_Txid = ?",Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT * FROM `"+database+"`.`messagelog` WHERE  CMP_Txid = ?",Statement.RETURN_GENERATED_KEYS);
			
			}
			
			pstmt.setString(1, String.valueOf(cMP_Txid));
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				mo = new MOSms();
				mo.setId(rs.getInt("id"));
				mo.setCMP_Txid(cMP_Txid);
				mo.setSMS_Message_String(rs.getString("MO_received"));
				mo.setMt_Sent(rs.getString("MT_Sent"));
				mo.setSMS_SourceAddr(rs.getString("SMS_SourceAddr"));
				mo.setMsisdn(rs.getString("SUB_Mobtel"));
				mo.setSMS_DataCodingId(rs.getString("SMS_DataCodingId"));
				mo.setCMPResponse(rs.getString("CMPResponse"));
				mo.setAPIType(rs.getString("APIType"));
				mo.setCMP_AKeyword(rs.getString("CMP_Keyword"));
				mo.setCMP_SKeyword(rs.getString("CMP_SKeyword"));
				mo.setTimeStamp(rs.getString("timeStamp"));
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
		
		
		return mo;
	}

	/* (non-Javadoc)
	 * @see com.pixelandtag.CelcomHTTPAPI#logMO(com.pixelandtag.MO)
	 */
	public void logMO(MOSms mo) {
		
		logger.debug("LOGGING_MO_CELCOM_");
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		if(mo.getCMP_Txid()<1)
			mo.setCMP_Txid(generateNextTxId());
		
		
		logger.debug("BEFORE_LOGGING_SMS : mo.getSMS_DataCodingId()   ["+mo.getSMS_DataCodingId()+"]");
		logger.debug("BEFORE_LOGGING_SMS : GenericMessage.NON_ASCII_SMS_ENCODING_ID   ["+mo.getSMS_DataCodingId()+"]");
		logger.debug("BEFORE_LOGGING_SMS : mo.getSMS_DataCodingId()!=null  ["+(mo.getSMS_DataCodingId()!=null)+"]");
		logger.debug("BEFORE_LOGGING_SMS : mo.getSMS_DataCodingId().trim().equals(GenericMessage.NON_ASCII_SMS_ENCODING_ID)  ["+(mo.getSMS_DataCodingId().trim().equals(GenericMessage.NON_ASCII_SMS_ENCODING_ID))+"]");
		
		if((mo.getSMS_DataCodingId()!=null) && mo.getSMS_DataCodingId().trim().equals(GenericMessage.NON_ASCII_SMS_ENCODING_ID)){
			logger.debug("BEFORE_DECODING Data Encoding = Old sms = "+mo.getSMS_Message_String());
			mo.setSMS_Message_String(hexToString(mo.getSMS_Message_String().replaceAll("00","")));
			logger.debug("AFTER_DECODING new sms = "+mo.getSMS_Message_String());
		}
		
		try {
			
			if(connectionObjIsCached){
				
				mo = resolveKeywords(mo, getConn());//First resolve the keyword..
				
				pstmt = getConn().prepareStatement("INSERT INTO `"+database+"`.`messagelog`(CMP_Txid,MO_Received,SMS_SourceAddr,SUB_Mobtel,SMS_DataCodingId,CMPResponse,APIType,CMP_Keyword,CMP_SKeyword,price,serviceid,mo_processor_id_fk,msg_was_split,event_type,price_point_keyword) " +
						"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				conn = getConn();
				
				mo = resolveKeywords(mo, conn);//First resolve the keyword..
				
				pstmt = conn.prepareStatement("INSERT INTO `"+database+"`.`messagelog`(CMP_Txid,MO_Received,SMS_SourceAddr,SUB_Mobtel,SMS_DataCodingId,CMPResponse,APIType,CMP_Keyword,CMP_SKeyword,price,serviceid,mo_processor_id_fk,msg_was_split,event_type,price_point_keyword) " +
						"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			}
			
			
			
			pstmt.setLong(1, mo.getCMP_Txid());
			pstmt.setString(2, mo.getSMS_Message_String());
			pstmt.setString(3, mo.getSMS_SourceAddr());
			pstmt.setString(4, mo.getMsisdn());
			pstmt.setString(5, mo.getSMS_DataCodingId());
			pstmt.setString(6, mo.getCMPResponse());
			pstmt.setString(7, mo.getAPIType());
			pstmt.setString(8, mo.getCMP_AKeyword());
			pstmt.setString(9, mo.getCMP_SKeyword());
			
			pstmt.setDouble(10, mo.getPrice().doubleValue());
			pstmt.setInt(11, mo.getServiceid());
			pstmt.setInt(12, mo.getProcessor_id());
			pstmt.setBoolean(13, mo.isSplit_msg());
			
			pstmt.setString(14, mo.getEventType()!=null ? mo.getEventType().getName() : EventType.CONTENT_PURCHASE.getName());
			pstmt.setString(15, mo.getPricePointKeyword());
			
			logger.info(":::::::::::::::::::mo.getCMP_Keyword(): "+mo.getCMP_AKeyword());
			logger.info(":::::::::::::::::::mo.getCMP_SKeyword(): "+mo.getCMP_SKeyword());
			logger.info(":::::::::::::::::::mo.getPrice(): "+mo.getPrice());
			logger.info(":::::::::::::::::::mo.getProcessor_id(): "+mo.getProcessor_id());
			logger.info(":::::::::::::::::::mo.isSplit_msg(): "+mo.isSplit_msg());
			logger.info(":::::::::::::::::::mo.getSMS_DataCodingId(): "+mo.getSMS_DataCodingId());
			
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
	 * @see com.pixelandtag.CelcomHTTPAPI#logMT(com.pixelandtag.MT)
	 */
	public void logMT(MTsms mt) {
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		try {
			
			
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("INSERT INTO `"+database+"`.`messagelog`(CMP_Txid,MT_Sent,SMS_SourceAddr,SUB_Mobtel,SMS_DataCodingId,CMPResponse,APIType,CMP_Keyword,CMP_SKeyword,MT_STATUS,number_of_sms,msg_was_split,MT_SendTime,mo_ack,serviceid,price,newCMP_Txid,mo_processor_id_fk) " +
						"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'),1,?,?,?,?) ON DUPLICATE KEY UPDATE MT_Sent = ?, mo_ack=1, MT_SendTime=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'), MT_STATUS = ?, number_of_sms = ?, msg_was_split=?, serviceid=? , price=?, SMS_DataCodingId=?, CMPResponse=?, APIType=?, newCMP_Txid=?, CMP_SKeyword=?, mo_processor_id_fk=?",Statement.RETURN_GENERATED_KEYS);
				
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("INSERT INTO `"+database+"`.`messagelog`(CMP_Txid,MT_Sent,SMS_SourceAddr,SUB_Mobtel,SMS_DataCodingId,CMPResponse,APIType,CMP_Keyword,CMP_SKeyword,MT_STATUS,number_of_sms,msg_was_split,MT_SendTime,mo_ack,serviceid,price,newCMP_Txid,mo_processor_id_fk) " +
						"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'),1,?,?,?,?) ON DUPLICATE KEY UPDATE MT_Sent = ?, mo_ack=1, MT_SendTime=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'), MT_STATUS = ?, number_of_sms = ?, msg_was_split=?, serviceid=? , price=?, SMS_DataCodingId=?, CMPResponse=?, APIType=?, newCMP_Txid=?, CMP_SKeyword=?, mo_processor_id_fk=?",Statement.RETURN_GENERATED_KEYS);
			}
			
			String txid = mt.getIdStr();
			//logger.debug("sending message: "+mt.toString());
			if(mt.getCMP_Txid()>0){
				
				if(!(mt.getCMP_Txid()==-1)){
					txid = String.valueOf(mt.getCMP_Txid());
					
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
			if(mt.getCMP_SKeyword()!=null && mt.getCMP_SKeyword().equals(TarrifCode.RM1.getCode()))
				pstmt.setDouble(14, 1.0d);//price
			else
				pstmt.setDouble(14, mt.getPrice().doubleValue());//price
			pstmt.setString(15, mt.getNewCMP_Txid());//new CMPTxid
			pstmt.setInt(16, mt.getProcessor_id());//processor id
			pstmt.setString(17, mt.getSms());//SMS
			
			if(isRetry)
				pstmt.setString(18, ERROR.PSAInsufficientBalance.toString());//MT_STATUS
			else
				pstmt.setString(18, mt.getMT_STATUS());//MT_STATUS
			
			pstmt.setInt(19, mt.getNumber_of_sms());//number_of_sms
			pstmt.setInt(20, (mt.isSplit_msg() ? 1 : 0));//number_of_sms
			pstmt.setInt(21, mt.getServiceid());//serviceid
			
			if(mt.getCMP_SKeyword().equals(TarrifCode.RM1.getCode()))
				pstmt.setDouble(22, 1.0d);//price
			else
				pstmt.setDouble(22, mt.getPrice().doubleValue());//price
			
			pstmt.setString(23, mt.getSMS_DataCodingId());//SMS_DataCodingId
			pstmt.setString(24, mt.getCMPResponse());//CMPResponse
			pstmt.setString(25, mt.getAPIType());//APIType,
			pstmt.setString(26, mt.getNewCMP_Txid());//new CMPTxid
			
			if(mt.getSms().startsWith(RM1))
				pstmt.setString(27, TarrifCode.RM1.getCode());//CMP_SKeyword
			else
				pstmt.setString(27, mt.getCMP_SKeyword());//CMP_SKeyword
			
			pstmt.setInt(28, mt.getProcessor_id());//CMP_SKeyword
			
			
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
	public void acknowledgeReceipt(MOSms mo) {
		PreparedStatement pstmt = null;
		
		
	}

	/* (non-Javadoc)
	 * @see com.pixelandtag.CelcomHTTPAPI#findMO(java.lang.String)
	 */
	public MOSms findMO(String cpm_txId) {
		// TODO Auto-generated method stub
		return null;
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
				//logger.info("************ TRYING TO GET DS from CONN POOL **********");
				//System.out.println("************ TRYING TO GET DS from CONN POOL **********");
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

	public Queue<MOSms> getLatestMO(int limit, String CMP_Keyword, String CMP_SKeyword) {
		Queue<MOSms> moSMS = null;//new ArrayList<MOSms>();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		MOSms mo = null;
		
		
		try{
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("SELECT * FROM `"+database+"`.`messagelog` WHERE mo_ack = 0 AND CMP_Keyword = ? AND CMP_SKeyword = ? AND MT_STATUS='Success' ORDER BY timeStamp asc"+(limit>0 ? (" LIMIT "+limit) : ("")),Statement.RETURN_GENERATED_KEYS);
				
					
			}else{
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT * FROM `"+database+"`.`messagelog` WHERE mo_ack = 0 AND CMP_Keyword = ? AND CMP_SKeyword = ? AND MT_STATUS='Success' ORDER BY timeStamp asc"+(limit>0 ? (" LIMIT "+limit) : ("")),Statement.RETURN_GENERATED_KEYS);
				
			}
			
			pstmt.setString(1, CMP_Keyword);
			pstmt.setString(2, CMP_SKeyword);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				
				if(rs.isFirst()){
					moSMS = new LinkedList<MOSms>();
					//TODO use a collections object that maintains order!
				}
				
				mo = new MOSms();
				mo.setId(rs.getInt("id"));
				mo.setCMP_Txid(rs.getLong("CMP_Txid"));
				mo.setSMS_Message_String(rs.getString("MO_Received"));
				mo.setMt_Sent(rs.getString("MT_Sent"));
				mo.setSMS_SourceAddr(rs.getString("SMS_SourceAddr"));
				mo.setMsisdn(rs.getString("SUB_Mobtel"));
				mo.setSMS_DataCodingId(rs.getString("SMS_DataCodingId"));
				mo.setCMPResponse(rs.getString("CMP_Response"));
				mo.setAPIType(rs.getString("APIType"));
				mo.setCMP_AKeyword(rs.getString("CMP_Keyword"));
				mo.setCMP_SKeyword(rs.getString("CMP_SKeyword"));
				mo.setTimeStamp(rs.getString("timeStamp"));
				mo.setMo_ack(rs.getString("mo_ack"));
				mo.setMt_ack(rs.getString("mt_ack"));
				mo.setMT_STATUS(rs.getString("MT_STATUS"));
				
				moSMS.add(mo);
				
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
			
			closeConnectionIfNecessary(conn);
			
		}
		
		
		
		
		return moSMS;
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.CelcomHTTPAPI#getLatestMO(int)
	 */
	public synchronized Queue<MOSms> getLatestMO(int limit) {
		
		Queue<MOSms> moSMS = null;//new ArrayList<MOSms>();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		MOSms mo = null;
		
		
		try{
			if(connectionObjIsCached){
				
				pstmt = getConn().prepareStatement("SELECT * FROM `"+database+"`.`messagelog` WHERE (mo_ack = 0 AND processing=0 AND (CMP_Keyword IS NOT NULL) AND (CMP_SKeyword IS NOT NULL) AND (MT_Sent IS NULL) OR (MT_STATUS in ('PSANumberBarred','PSAInsufficientBalance','WaitingForDLR') AND re_try=1 AND MT_SendTime between timestamp(DATE_SUB(CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"'), INTERVAL 0 DAY)) AND ((CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"') + INTERVAL 1 DAY) - INTERVAL 1 SECOND)) ORDER BY timeStamp asc"+(limit>0 ? (" LIMIT "+limit) : ("")),Statement.RETURN_GENERATED_KEYS);
				
					
			}else{
				
				conn = getConn();
				
				     pstmt = conn.prepareStatement("SELECT * FROM `"+database+"`.`messagelog` WHERE (mo_ack = 0 AND processing=0 AND (CMP_Keyword IS NOT NULL) AND (CMP_SKeyword IS NOT NULL) AND (MT_Sent IS NULL) ) OR (MT_STATUS in ('PSANumberBarred','PSAInsufficientBalance','WaitingForDLR') AND re_try=1  AND MT_SendTime between timestamp(DATE_SUB(CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"'), INTERVAL 0 DAY)) AND ((CONVERT_TZ(CURRENT_DATE,'"+fr_tz+"','"+to_tz+"') + INTERVAL 1 DAY) - INTERVAL 1 SECOND)) ORDER BY timeStamp asc"+(limit>0 ? (" LIMIT "+limit) : ("")),Statement.RETURN_GENERATED_KEYS);
				
			}
			
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				
				if(rs.isFirst()){
					moSMS = new LinkedList<MOSms>();
					//TODO use a collections object that maintains order!
				}
				
				mo = new MOSms();
				mo.setId(rs.getInt("id"));
				mo.setCMP_Txid(rs.getLong("CMP_Txid"));
				mo.setSMS_Message_String(rs.getString("MO_Received"));
				mo.setMt_Sent(rs.getString("MT_Sent"));
				mo.setSMS_SourceAddr(rs.getString("SMS_SourceAddr"));
				mo.setMsisdn(rs.getString("SUB_Mobtel"));
				mo.setSMS_DataCodingId(rs.getString("SMS_DataCodingId"));
				mo.setCMPResponse(rs.getString("CMPResponse"));
				mo.setAPIType(rs.getString("APIType"));
				mo.setCMP_AKeyword(rs.getString("CMP_Keyword"));
				mo.setCMP_SKeyword(rs.getString("CMP_SKeyword"));
				mo.setTimeStamp(rs.getString("timeStamp"));
				mo.setMo_ack(rs.getString("mo_ack"));
				mo.setMt_ack(rs.getString("mt_ack"));
				mo.setMT_STATUS(rs.getString("MT_STATUS"));
				mo.setServiceid(rs.getInt("serviceid"));
				mo.setPrice(BigDecimal.valueOf(rs.getDouble("price")));
				mo.setProcessor_id(rs.getInt("mo_processor_id_fk"));//added by Tim since 2012-09-21 at 1.17pm
				mo.setSplit_msg(rs.getBoolean("msg_was_split"));
				mo.setEventType(EventType.get(rs.getString("event_type")));
				moSMS.add(mo);
				
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
						
			notify();//Wake up all threads waiting on this
			
		}
		
		
		
		
		return moSMS;
	}

	
	
	public void updateMessageLog(MOSms mo) {

		
		/*String sql = "insert into `celcom`.`httptosend`" +
				"(SMS,MSISDN,SendFrom,fromAddr,CMP_Keyword,CMP_SKeyword,cmp_a_keyword,cmp_s_keyword) " +
				"VALUES('Thank you.','60133811449','23355','23355','NEWS','RENEWAL','NEWS','RENEWAL')";*/
		PreparedStatement pstmt = null;
		
		
		try {
			
			//First check if that message exists..
			//pstmt = getConn().prepareStatement("");
			
			
			
			if(!(mo.getCMP_Txid()==-1))
				pstmt = getConn().prepareStatement("INSERT INTO `"+CelcomImpl.database+"`.`messagelog`" +
						"(MO_Received, MT_Sent,SMS_SourceAddr,SUB_Mobtel,CMP_Keyword,CMP_SKeyword,CMP_Txid,MT_SendTime,mo_ack) " +
						"VALUES(?,?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'),1) ON DUPLICATE KEY UPDATE MT_Sent = ?, mo_ack=1, MT_SendTime=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"')",Statement.RETURN_GENERATED_KEYS);
			else
				pstmt = getConn().prepareStatement("insert into `"+CelcomImpl.database+"`.`httptosend`" +
						"(MO_Received, MT_Sent,SMS_SourceAddr,SUB_Mobtel,CMP_AKeyword,CMP_SKeyword,CMP_Txid,MT_SendTime,mo_ack) " +
						"VALUES(?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"'),1) ON DUPLICATE KEY UPDATE MT_Sent = ?, mo_ack=1, MT_SendTime=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"')",Statement.RETURN_GENERATED_KEYS);
				
			
			
			pstmt.setString(1, mo.getSMS_Message_String());
			pstmt.setString(2, mo.getMt_Sent());
			pstmt.setString(3, mo.getSMS_SourceAddr());
			pstmt.setString(4, mo.getMsisdn());
			pstmt.setString(5, mo.getCMP_AKeyword());
			pstmt.setString(6, mo.getCMP_SKeyword());
			
			if(!(mo.getCMP_Txid()==-1)){
				pstmt.setLong(7, mo.getCMP_Txid());
				pstmt.setString(8, mo.getMt_Sent());
			}else{
				pstmt.setString(7, mo.getMt_Sent());
				
			}
			
			
			pstmt.executeUpdate();
		
		} catch (SQLException e) {
			
			logger.error(e.getMessage(),e);
			
		} catch (InterruptedException e) {
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			try {
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				logger.error(e.getMessage(),e);
			
			}
			
		}
		
	}
    //removed synchronized keyword
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
				
				pstmt = getConn().prepareStatement("SELECT `mop`.id,`mop`.ServiceName,`mop`.ProcessorClass,`mop`.enabled,`mop`.class_status,`mop`.shortcode,`mop`.threads, `smss`.CMP_Keyword, `smss`.CMP_SKeyword, group_concat(`smss`.`cmd`) as `keywords`,  `smss`.`subscriptionText` as 'subscriptionText', `smss`.`unsubscriptionText` as 'unsubscriptionText', `smss`.`tailText_subscribed` as 'tailText_subscribed', `smss`.`tailText_notsubscribed` as 'tailText_notsubscribed'  FROM `"+database+"`.`mo_processors` `mop` LEFT JOIN `"+database+"`.`sms_service` `smss` ON `smss`.`mo_processorFK`=`mop`.`id` WHERE `mop`.`enabled`=1 group by `mop`.`id`", Statement.RETURN_GENERATED_KEYS);//"SELECT * FROM `"+DATABASE+"`.`mo_processors` WHERE enabled=1");
			
			}else{
				
				
				conn = getConn();
				
				pstmt = conn.prepareStatement("SELECT `mop`.id,`mop`.ServiceName,`mop`.ProcessorClass,`mop`.enabled,`mop`.class_status,`mop`.shortcode,`mop`.threads, `smss`.CMP_Keyword, `smss`.CMP_SKeyword, group_concat(`smss`.`cmd`) as `keywords`,  `smss`.`subscriptionText` as 'subscriptionText', `smss`.`unsubscriptionText` as 'unsubscriptionText', `smss`.`tailText_subscribed` as 'tailText_subscribed', `smss`.`tailText_notsubscribed` as 'tailText_notsubscribed' FROM `"+database+"`.`mo_processors` `mop` LEFT JOIN `"+database+"`.`sms_service` `smss` ON `smss`.`mo_processorFK`=`mop`.`id` WHERE `mop`.`enabled`=1 group by `mop`.`id`", Statement.RETURN_GENERATED_KEYS);//"SELECT * FROM `"+DATABASE+"`.`mo_processors` WHERE enabled=1");
				
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
				service.setCMP_AKeyword(rs.getString("CMP_Keyword"));
				service.setCMP_SKeyword(rs.getString("CMP_SKeyword"));
				service.setActive(rs.getBoolean("enabled"));
				service.setClass_status(rs.getString("class_status"));
				service.setShortcode(rs.getString("shortcode"));
				if(rs.getString("keywords")!=null)
					service.setKeywords(rs.getString("keywords").split(","));
				service.setServKey(service.getProcessorClassName()+"_"+service.getCMP_AKeyword()+"_"+service.getCMP_SKeyword()+"_"+service.getShortcode());
				
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


	public MOSms resolveKeywords(MOSms mo, Connection conn) {
		
		logger.info(">>>>>>V.4>>>>>>>>>>>>>CELCOM_MO_SMS["+mo.getSMS_Message_String()+"]");
		
		if(mo.getSMS_Message_String()!=null){
			if(mo.getSMS_Message_String().isEmpty())
				return mo;
			
		}else{
			
			mo.setSMS_Message_String(mo.getSMS_Message_String().trim().toUpperCase());
			
			logger.info(">>>>>>>>>>>>>>>>>>>SMS["+mo.getSMS_Message_String()+"]");
			
		}
		
		ResultSet rs = null;
		
		PreparedStatement pstmt = null;
		
		
		try {
			
			
			pstmt = conn.prepareStatement("SELECT `mop`.id as 'mo_processor_id_fk', `sms`.CMP_Keyword, `sms`.CMP_SKeyword, `sms`.price as 'sms_price', `sms`.id as 'serviceid', `sms`.`split_mt` as 'split_mt', `sms`.`event_type` as 'event_type', `sms`.`price_point_keyword` as 'price_point_keyword' FROM `"+database+"`.`sms_service` `sms` LEFT JOIN `"+database+"`.`mo_processors` `mop` on `sms`.`mo_processorFK`=`mop`.`id` WHERE  `mop`.`shortcode`=? AND `sms`.`enabled`=1  AND  `mop`.`enabled`=1 AND `sms`.`CMD`= ?",Statement.RETURN_GENERATED_KEYS);
			
			mo.setSMS_Message_String(replaceAllIllegalCharacters(mo.getSMS_Message_String()));
			
			pstmt.setString(1, mo.getSMS_SourceAddr());
			pstmt.setString(2, mo.getSMS_Message_String().split("[\\s]")[0].toUpperCase());
			logger.info("Msg : "+mo.getSMS_Message_String());
			logger.info("Keyword : "+mo.getSMS_Message_String().split("[\\s]")[0].toUpperCase());
			
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				mo.setCMP_AKeyword(rs.getString("CMP_Keyword"));
				mo.setCMP_SKeyword(rs.getString("CMP_SKeyword"));
				mo.setServiceid(rs.getInt("serviceid"));
				mo.setPrice(BigDecimal.valueOf(rs.getDouble("sms_price")));
				mo.setProcessor_id(rs.getInt("mo_processor_id_fk"));
				mo.setSplit_msg(rs.getBoolean("split_mt"));
				mo.setPricePointKeyword(rs.getString("price_point_keyword"));
				mo.setEventType(com.pixelandtag.sms.producerthreads.EventType.get(rs.getString("event_type")));
				
			}else{
				
				try{
					rs.close();
				}catch(Exception e){}
				
				try{
					pstmt.close();
				}catch(Exception e){}
				
				pstmt = conn.prepareStatement("SELECT `mop`.id as 'mo_processor_id_fk', `sms`.CMP_Keyword, `sms`.CMP_SKeyword, `sms`.price as 'sms_price', sms.id as 'serviceid', `sms`.`split_mt` as 'split_mt', `sms`.`event_type` as 'event_type', `sms`.`price_point_keyword` as 'price_point_keyword' FROM `"+database+"`.`sms_service` sms LEFT JOIN `"+database+"`.`mo_processors` mop ON mop.id = sms.mo_processorFK WHERE sms.cmd='DEFAULT' AND sms.enabled=1 AND mop.shortcode=?",Statement.RETURN_GENERATED_KEYS);
				
				pstmt.setString(1,mo.getSMS_SourceAddr());
				
				rs = pstmt.executeQuery();
				
				if(rs.next()){
					
					mo.setCMP_AKeyword(rs.getString("CMP_Keyword"));
					mo.setCMP_SKeyword(rs.getString("CMP_SKeyword"));
					mo.setServiceid(rs.getInt("serviceid"));
					mo.setPrice(BigDecimal.valueOf(rs.getDouble("sms_price")));
					mo.setProcessor_id(rs.getInt("mo_processor_id_fk"));
					mo.setSplit_msg(rs.getBoolean("split_mt"));
					mo.setPricePointKeyword(rs.getString("price_point_keyword"));
					mo.setEventType(com.pixelandtag.sms.producerthreads.EventType.get(rs.getString("event_type")));
					
				}
				
				
			}
		
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
				
				if(pstmt!=null)
					pstmt.close();
			
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			//closeConnectionIfNecessary(conn);
			
		}
		
		
		return mo;
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
	
	
	public long generateNextTxId(){
		return System.currentTimeMillis();
		//String timestamp = String.valueOf(System.currentTimeMillis());
		
		//return Settings.INMOBIA.substring(0, (19-timestamp.length())) + timestamp;//(String.valueOf(Long.MAX_VALUE).length()-timestamp.length())) + timestamp;
		
	}

	@Override
	public void flagMMSIfAny(Notification notification) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		Connection conn = null;
		
		try{
			
			long cmpTxid = notification.getCMP_Txid();
			
			if(connectionObjIsCached){
				
				ps = getConn().prepareStatement("SELECT CMP_Txid FROM `celcom`.`messagelog` WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)",Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, notification.getCMP_Txid());
				ps.setLong(2, notification.getCMP_Txid());
				rs = ps.executeQuery();
				if(rs.next()){
					cmpTxid = rs.getLong("CMP_Txid");
				}
				
				if(rs!=null)
					rs.close();
				if(ps!=null)
					ps.close();
				
				
				
				ps = getConn().prepareStatement("SELECT id,tx_id_waiting_to_succeed_before_sending, paidFor, billingStatus FROM `"+database+"`.`mms_to_send` WHERE tx_id_waiting_to_succeed_before_sending = ?",Statement.RETURN_GENERATED_KEYS);
			
			}else{
				
				conn = getConn();
				
				ps = conn.prepareStatement("SELECT CMP_Txid FROM `celcom`.`messagelog` WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)",Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, notification.getCMP_Txid());
				ps.setLong(2, notification.getCMP_Txid());
				rs = ps.executeQuery();
				if(rs.next()){
					cmpTxid = rs.getLong("CMP_Txid");
				}
				
				if(rs!=null)
					rs.close();
				if(ps!=null)
					ps.close();
				
				
				ps = conn.prepareStatement("SELECT id,tx_id_waiting_to_succeed_before_sending, paidFor, billingStatus FROM  `"+database+"`.`mms_to_send` WHERE tx_id_waiting_to_succeed_before_sending = ?",Statement.RETURN_GENERATED_KEYS);
				
			}
			
			ps.setLong(1, cmpTxid);
			
			rs = ps.executeQuery();
			
			boolean thereIs = false;
			int mms_log_id = -1;
			
			if(rs.next()){
				
				mms_log_id = rs.getInt("id");
				
				thereIs = true;
				logger.info("_______>>>>>>>>>>>>>>>>>>>>>_____________ WE FOUND MMS THAT NEEDS FLAGGING :: txID = "+notification.getCMP_Txid());
			}
			
			
			if(thereIs){//If there is an MMS waiting to be paid for before it's sent. We mark it as paidFor
				
				
				if(connectionObjIsCached){
					
					ps = getConn().prepareStatement("UPDATE `"+database+"`.`mms_to_send` set paidFor=?, billingStatus=?, dlrReceived=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"') WHERE tx_id_waiting_to_succeed_before_sending = ? AND id = ?",Statement.RETURN_GENERATED_KEYS);
				
				}else{
					
					ps = conn.prepareStatement("UPDATE `"+database+"`.`mms_to_send` set paidFor=?, billingStatus=?, dlrReceived=CONVERT_TZ(CURRENT_TIMESTAMP,'"+fr_tz+"','"+to_tz+"') WHERE tx_id_waiting_to_succeed_before_sending = ? AND id = ?",Statement.RETURN_GENERATED_KEYS);
					
				}
				
				ps.setInt(1, (notification.getErrorCode().equals(ERROR.Success) ? 1 : 0) );
				ps.setString(2, notification.getErrorCode().toString());
				ps.setLong(3, cmpTxid);
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

	
	@Override
	public void processLuckyDip(Notification notification) {
		String msg = "";
		long tx_id = notification.getCMP_Txid();
		
		Connection conn = null;
		
		MTsms mt = null;
		int language_id = UtilCelcom.DEFAULT_LANGUAGE;
		
		String voucher_system_status = "";
		
		
		
		
		try{
			
			if(connectionObjIsCached){
				voucher_system_status  = UtilCelcom.getConfigValue("voucher_system_status", getConn()).trim();
			}else{
				conn = getConn();
				voucher_system_status  = UtilCelcom.getConfigValue("voucher_system_status", conn).trim();
			}
			
			if(!voucher_system_status.equalsIgnoreCase("on"))
				return;
			
			
			
			if(connectionObjIsCached){
				mt = MechanicsS.getMTsmsFromMessageLog(tx_id,getConn());
				language_id = UtilCelcom.getSubscriberLanguage(mt.getSUB_C_Mobtel(), getConn());
			}else{
				mt = MechanicsS.getMTsmsFromMessageLog(tx_id,conn);
				language_id = UtilCelcom.getSubscriberLanguage(mt.getSUB_C_Mobtel(), conn);
			}
			
			
			 
			
			
			if(mt!=null){//lucky dipping
				
				logger.debug("IN VOUCHER NOTIF BLOCK!!! >>> "+mt);
				String ticket_number_notf = null;
				
				if(mt.getPrice().compareTo(BigDecimal.ZERO)>0){
					if(connectionObjIsCached){
						
						ticket_number_notf = UtilCelcom.getMessage(MessageType.RANDOM_NUMBER_NOTIFICATION, getConn(), language_id);
						
						ticket_number_notf = ticket_number_notf.replaceAll(VOUCHER_TAG , String.valueOf(mt.getCMP_Txid()));
						
						
						ticket_number_notf = GenericServiceProcessor.RM.replaceAll(GenericServiceProcessor.PRICE_TG, "0")+ticket_number_notf;
						
						MechanicsS.insertIntoHttpToSend(mt.getSUB_C_Mobtel(), ticket_number_notf, MechanicsS.generateNextTxId(), mt.getServiceid(), 0d, mt.getShortcode(), UtilCelcom.getConfigValue("free_tarrif_code_cmp_AKeyword",  getConn()), UtilCelcom.getConfigValue("free_tarrif_code_cmp_SKeyword",  getConn()), false,getConn());
						
						UtilCelcom.queueIntoVoucherSystem(mt,getConn());
					}else{
						
						ticket_number_notf = UtilCelcom.getMessage(MessageType.RANDOM_NUMBER_NOTIFICATION, getConn(), language_id);
						
						ticket_number_notf = ticket_number_notf.replaceAll(VOUCHER_TAG, String.valueOf(mt.getCMP_Txid()));
						
						ticket_number_notf = GenericServiceProcessor.RM.replaceAll(GenericServiceProcessor.PRICE_TG, "0")+ticket_number_notf;
						
						MechanicsS.insertIntoHttpToSend(mt.getSUB_C_Mobtel(), ticket_number_notf, MechanicsS.generateNextTxId(), mt.getServiceid(), 0d, mt.getShortcode(), UtilCelcom.getConfigValue("free_tarrif_code_cmp_AKeyword",  conn), UtilCelcom.getConfigValue("free_tarrif_code_cmp_SKeyword", conn), false, conn);
						
						UtilCelcom.queueIntoVoucherSystem(mt,conn);
					}
				}
				
				/*if(mt.getPrice()>0){
				
					initLuckyDip();
					
					if(connectionObjIsCached){
						msg = "RM0 "+lucky_dip.getTailSMS(mt.getMsisdn(), mt.getServiceid(), getConn());
						MechanicsS.insertIntoHttpToSend(mt.getMsisdn(), msg, MechanicsS.generateNextTxId(), mt.getServiceid(), 0, mt.getSMS_SourceAddr(), mt.getCMP_AKeyword(), mt.getCMP_SKeyword(), getConn());
						
					}else{
						//connection object already created in code block higher up
						msg = "RM0 "+lucky_dip.getTailSMS(mt.getMsisdn(), mt.getServiceid(), conn);
						MechanicsS.insertIntoHttpToSend(mt.getMsisdn(), msg, MechanicsS.generateNextTxId(), mt.getServiceid(), 0, mt.getSMS_SourceAddr(), mt.getCMP_AKeyword(), mt.getCMP_SKeyword(),conn);
						
					}
					
				}*/
				
				
			}
			
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			closeConnectionIfNecessary(conn);
		}
		
		
		
		
		
	}

	
		
	
	
	

}
