package com.inmobia.celcom.serviceprocessors.sms;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.inmobia.axiata.connections.DriverUtilities;
import com.inmobia.axiata.web.beans.MessageType;
import com.inmobia.axiata.web.beans.RequestObject;
import com.inmobia.celcom.api.GenericServiceProcessor;
import com.inmobia.celcom.entities.MOSms;
import com.inmobia.celcom.sms.application.HTTPMTSenderApp;
import com.inmobia.celcom.util.UtilCelcom;

public class Content360UnknownKeyword extends GenericServiceProcessor {

	final Logger logger = Logger.getLogger(Content360UnknownKeyword.class);
	private Connection conn = null;
	private DBPoolDataSource ds;
	
	int vendor = DriverUtilities.MYSQL;
    String driver = DriverUtilities.getDriver(vendor);
    String host =  HTTPMTSenderApp.props.getProperty("db_host");
    String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
    String url = DriverUtilities.makeURL(host, dbName, vendor);
    String username = HTTPMTSenderApp.props.getProperty("db_username");
    String password = HTTPMTSenderApp.props.getProperty("db_password");
    
	
	public Content360UnknownKeyword(){
		ds = new DBPoolDataSource();
		ds.setValidatorClassName("snaq.db.Select1Validator");
		ds.setName("Content360UnknownKeyword-processor-ds");
		ds.setDescription("Content360UnknownKeyword Pooling DataSource");
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl(url);
		ds.setUser(username);
		ds.setPassword(password);
		ds.setMinPool(1);
		ds.setMaxPool(2);
		ds.setMaxSize(3);
		ds.setIdleTimeout(3600); // Specified in seconds.
	
		ds.setValidationQuery("SELECT 'test'");
	
	
		logger.info(">>>>>>>>>>>>> Content360UnknownKeyword keyowrd processor initialized and dbpoolds initialized!");
	}
	
	@Override
	public MOSms process(MOSms mo) {
		
		Connection conn = null;
		try {
			conn = getCon();
			
			final RequestObject req = new RequestObject(mo);
			final String MSISDN = req.getMsisdn();
		
			int language_id = UtilCelcom.getSubscriberLanguage(MSISDN, conn);
			
			String response = UtilCelcom.getMessage(MessageType.UNKNOWN_KEYWORD_ADVICE, conn, language_id) ;
			
			mo.setMt_Sent(response);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}finally{
			
			try{
				conn.close();
			}catch(Exception e){}
		}
		return mo;
	}

	@Override
	public void finalizeMe() {
		try{
			
			if(ds!=null)
				ds.releaseConnectionPool();
			
		}catch(Exception e){
			
			logger.error(e.getMessage(), e);
			
		}

		try {

			if (conn != null)
				conn.close();

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

		}
		
	}

	@Override
	public Connection getCon() {
		try {

			if (conn != null && !conn.isClosed()) {
				conn.setAutoCommit(true);
				return conn;
			}

		} catch (SQLException e1) {
			logger.warn(e1.getMessage() + " will create a new conn");
		} catch (Exception e1) {
			logger.warn(e1.getMessage() + " will create a new conn");
		}

		while (true) {

			try {
				while (conn == null || conn.isClosed()) {
					try {
						conn = ds.getConnection();

						logger.info("created connection! ");
						if (conn != null)
							return conn;
					} catch (Exception e) {
						logger.warn("Could not create connection. Reason: "
								+ e.getMessage());
						try {
							Thread.sleep(500);
						} catch (Exception ee) {
						}
					}
				}

				if (conn != null)
					return conn;

			} catch (Exception e) {
				logger.warn("can't get a connection, re-trying");
				try {
					Thread.sleep(500);
				} catch (Exception ee) {
				}
			}
		}
	}

}
