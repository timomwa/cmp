package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.web.beans.RequestObject;
import com.pixelandtag.web.triviaI.MechanicsI;

public class NewsProcessor extends GenericServiceProcessor{

	private final Logger logger = Logger.getLogger(NewsProcessor.class);
	private DBPoolDataSource ds;
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
		 
		 System.out.println("Successfully initialized EJB CMPResourceBeanRemote !!");
    }
	public NewsProcessor(){
		init_datasource();
	}
	
	private void init_datasource(){
		
		int vendor = DriverUtilities.MYSQL;
		String driver = DriverUtilities.getDriver(vendor);
		String host = "db";
		String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
		String url = DriverUtilities.makeURL(host, dbName, vendor);
		
		ds = new DBPoolDataSource();
	    ds.setName("NEWS_PROCESSOR_DS ");
	    ds.setDescription("News processor thread datasource: "+ds.getName());
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
	
	public MOSms process(MOSms mo) {
		
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		
		try {
			
			final RequestObject req = new RequestObject(mo);
			
			final String KEYWORD = req.getKeyword().trim();
			final int serviceid = 	mo.getServiceid();
			conn = getCon();
			final int content_id =  Integer.valueOf(UtilCelcom.getServiceMetaData(conn,serviceid,"dynamic_contentid"));
			final String sql = "SELECT c.`ID`, c.`Text` FROM `celcom`.`dynamiccontent_content` c WHERE c.`contentid`=? ORDER BY c.`timestamp` DESC LIMIT 0,1";
			
			logger.info(" KEYWORD ::::::::::::::::::::::::: ["+KEYWORD+"]");
			logger.info(" SERVICEID ::::::::::::::::::::::::: ["+serviceid+"]");
			
			
			if(KEYWORD.equalsIgnoreCase("berita")){
				
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, content_id);
				rs = pstmt.executeQuery();
				
				if(rs.next()){
					
					String news = rs.getString("Text").trim();
					mo.setMt_Sent(news);
					//int newsID = rs.getInt("ID");
				}
				
			}else{
				
				String unknown_keyword = UtilCelcom.getServiceMetaData(conn,-1,"unknown_keyword");
				
				if(unknown_keyword==null)
					unknown_keyword = "Unknown Keyword.";
					mo.setMt_Sent(unknown_keyword);
			}
			
			logger.info(mo.toString());
		
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
		
		return mo;
	}



	
	
	
	/**
	 * Gets connection from datasource
	 */
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
	public CMPResourceBeanRemote getEJB() {
		return this.cmpbean;
	}

}
