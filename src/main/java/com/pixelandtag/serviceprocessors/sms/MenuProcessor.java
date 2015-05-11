package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.web.beans.RequestObject;
import com.pixelandtag.web.triviaI.MechanicsI;

public class MenuProcessor extends GenericServiceProcessor{

	private final Logger logger = Logger.getLogger(MenuProcessor.class);
	private DBPoolDataSource ds;
	
	
	public MenuProcessor(){
		init_datasource();
	}
	private InitialContext context;
	private CMPResourceBeanRemote cmpbean;
    
    public void initEJB() throws NamingException{
    	String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		 Properties props = new Properties();
		 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		 props.put(Context.PROVIDER_URL, "remote://localhost:4447");
		 props.put(Context.SECURITY_PRINCIPAL, "testuser");
		 props.put(Context.SECURITY_CREDENTIALS, "testpassword123!");
		 props.put("jboss.naming.client.ejb.context", true);
		 context = new InitialContext(props);
		 cmpbean =  (CMPResourceBeanRemote) 
       		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		 
		 logger.info("Successfully initialized EJB CMPResourceBeanRemote !!");
    }
	private void init_datasource(){
		
		int vendor = DriverUtilities.MYSQL;
		String driver = DriverUtilities.getDriver(vendor);
		String host = "db";
		String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
		String url = DriverUtilities.makeURL(host, dbName, vendor);
		
		ds = new DBPoolDataSource();
	    ds.setName("MENU_STATICCONTENT_PROCESSOR_DS");
	    ds.setDescription("Static Content thread datasource: "+ds.getName());
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
	public MOSms process(MOSms mo) {
		
		Connection conn = null;
		
		
		try {
			
			final RequestObject req = new RequestObject(mo);
			
			final String KEYWORD = req.getKeyword().trim();
			final int serviceid = 	mo.getServiceid();
			
			conn = getCon();
			
			
			logger.info(" KEYWORD ::::::::::::::::::::::::: ["+KEYWORD+"]");
			logger.info(" SERVICEID ::::::::::::::::::::::::: ["+serviceid+"]");
			
			
			
			if(KEYWORD.equalsIgnoreCase("ENG")){
				String more =   "1. News\n"+
								"2. Prayer Times\n"+
								"3. Sports\n"+
								"4. What's up\n"+
								"5. Fun & Inspiration\n"+
								"6. Love & Family\n"+
								"7. Sports";
				mo.setMt_Sent(more);
				
			
				
			}else{
				
				String unknown_keyword = UtilCelcom.getServiceMetaData(conn,-1,"unknown_keyword");
				
				if(unknown_keyword==null)
					unknown_keyword = "Unknown Keyword.";
					mo.setMt_Sent(unknown_keyword);
			
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


		try{
			
			context.close();
		
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}
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
	

	
	@Override
	public BaseEntityI getEJB() {
		return this.cmpbean;
	}
}
