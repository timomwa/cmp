package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.web.beans.RequestObject;

public class MenuProcessor extends GenericServiceProcessor{

	private final Logger logger = Logger.getLogger(MenuProcessor.class);
	private DBPoolDataSource ds;
	
	
	public MenuProcessor() throws Exception{
		init_datasource();
		initEJB();
	}
	private InitialContext context;
	private CMPResourceBeanRemote cmpbean;
    
    public void initEJB() throws NamingException{
    	String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		 Properties props = new Properties();
		 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		 props.put(Context.PROVIDER_URL, "remote://"+mtsenderprop.getProperty("ejbhost")+":"+mtsenderprop.getProperty("ejbhostport"));
		 props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
		 props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
		 props.put("jboss.naming.client.ejb.context", true);
		 context = new InitialContext(props);
		 cmpbean =  (CMPResourceBeanRemote) 
       		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		 
		 logger.info("Successfully initialized EJB CMPResourceBeanRemote !!");
    }
    
    
	private void init_datasource(){
		
	}

	@Override
	public OutgoingSMS process(IncomingSMS incomingsms) {
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		Connection conn = null;
		
		
		try {
			
			final RequestObject req = new RequestObject(incomingsms);
			
			final String KEYWORD = req.getKeyword().trim();
			final Long serviceid = 	incomingsms.getServiceid();
			
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
				outgoingsms.setSms(more);
				
			
				
			}else{
				
				String unknown_keyword = UtilCelcom.getServiceMetaData(conn,-1,"unknown_keyword");
				
				if(unknown_keyword==null)
					unknown_keyword = "Unknown Keyword.";
				outgoingsms.setSms(unknown_keyword);
			
			}
			
			logger.info(outgoingsms.toString());
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			try{
				conn.close();
			}catch(Exception e){}
		
		}
		
		return outgoingsms;
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
