package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.beans.RequestObject;

public class UnknownKeyword extends GenericServiceProcessor {

	final Logger logger = Logger.getLogger(UnknownKeyword.class);
	private Connection conn = null;
	private DBPoolDataSource dbpds,ds;
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
	public UnknownKeyword() {

		

	
		logger.info(">>>>>>>>>>>>> unknown keyowrd processor initialized and dbpoolds initialized!");

	}

	@Override
	public OutgoingSMS process(IncomingSMS incomingsms) {
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		Connection conn = null;
		try {
			conn = getCon();
			
			final RequestObject req = new RequestObject(incomingsms);
			final String MSISDN = req.getMsisdn();
		
			int language_id = UtilCelcom.getSubscriberLanguage(MSISDN, conn);
			
			String response = UtilCelcom.getMessage(MessageType.UNKNOWN_KEYWORD_ADVICE, conn, language_id) ;
			
			outgoingsms.setSms(response);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
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
						conn = dbpds.getConnection();

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
	@Override
	public CMPResourceBeanRemote getEJB() {
		return this.cmpbean;
	}

	
}
