package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.subscription.Subscription;
import com.pixelandtag.subscription.dto.SubscriptionDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.web.beans.RequestObject;
import com.pixelandtag.web.triviaI.MechanicsI;

public class SubscribeProcessor extends GenericServiceProcessor {

	
	private final Logger logger = Logger.getLogger(SubscribeProcessor.class);
	private DBPoolDataSource ds;
	//private MenuController menu_controller = null;
	private Subscription subscription = null;
	
	public SubscribeProcessor(){
		init_datasource();
		//menu_controller = new MenuController();
		subscription = new Subscription();
	}
	
	private void init_datasource(){
		
		int vendor = DriverUtilities.MYSQL;
		String driver = DriverUtilities.getDriver(vendor);
		String host = "db";
		String dbName =  HTTPMTSenderApp.props.getProperty("DATABASE");
		String url = DriverUtilities.makeURL(host, dbName, vendor);
		
		ds = new DBPoolDataSource();
	    ds.setName("Subscribe_PROCESSOR_DS");
	    ds.setDescription("Processes the YES Keyword. Thread datasource Name: "+ds.getName());
	    ds.setDriverClassName(driver);
	    ds.setUrl(url);
	    ds.setUser("root");
	    ds.setPassword("");
	    ds.setMinPool(1);
	    ds.setMaxPool(2);
	    ds.setMaxSize(3);
	    ds.setIdleTimeout(3600);  // Specified in seconds.
	    ds.setValidationQuery("SELECT 'Test'");
		
	}
	
	@Override
	public MOSms process(MOSms mo){
		
		Connection conn = null;
		
		try {
			
			final RequestObject req = new RequestObject(mo);
			
			final String KEYWORD = req.getKeyword().trim();
			final String MSISDN = req.getMsisdn();
			
			conn = getCon();
			
			
			if(KEYWORD.equals("YES")){
				
				SubscriptionDTO sub = subscription.checkAnyPending(conn, MSISDN);
				
				if(sub!=null){
					subscription.updateSubscription(conn, sub.getId(), SubscriptionStatus.confirmed);
					mo.setMt_Sent("You've successfully subscribed");
				}else{
					mo.setMt_Sent("You don't have any pending subscriptions");
				}
				
			}
			
			logger.info(mo.toString());
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			
			try{
				conn.close();
			}catch(Exception e){}
		
		}
		
		return mo;
	}

	@Override
	public void finalizeMe() {
		
		try {
			
			ds.releaseConnectionPool();
			
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
		}
		
	}

	@Override
	public Connection getCon() {
		
		try {
			
			return ds.getConnection();
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
			return null;
		
		}finally{
		
		}
	}

}