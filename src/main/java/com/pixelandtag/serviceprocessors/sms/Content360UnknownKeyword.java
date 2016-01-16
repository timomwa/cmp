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
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.beans.RequestObject;

public class Content360UnknownKeyword extends GenericServiceProcessor {

	final Logger logger = Logger.getLogger(Content360UnknownKeyword.class);
	//private Connection conn = null;
	//private DBPoolDataSource ds;
	
	//int vendor = DriverUtilities.MYSQL;
    //String driver = DriverUtilities.getDriver(vendor);
    //String host =  HTTPMTSenderApp.props.getProperty("db_host");
    //String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
    //String url = DriverUtilities.makeURL(host, dbName, vendor);
    //String username = HTTPMTSenderApp.props.getProperty("db_username");
    //String password = HTTPMTSenderApp.props.getProperty("db_password");
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
	public Content360UnknownKeyword() throws Exception{
		
		initEJB();
		/*ds = new DBPoolDataSource();
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
	*/
	
		logger.info(">>>>>>>>>>>>> Content360UnknownKeyword keyowrd processor initialized and dbpoolds initialized!");
	}
	
	@Override
	public OutgoingSMS process(IncomingSMS incomingsms) {
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		//Connection conn = null;
		try {
			//conn = getCon();
			
			final RequestObject req = new RequestObject(incomingsms);
			
			final String MSISDN = req.getMsisdn();
		
			int language_id = cmpbean.getSubscriberLanguage(MSISDN);
			
			String response = cmpbean.getMessage(MessageType.UNKNOWN_KEYWORD_ADVICE, language_id, incomingsms.getOpco().getId()) ;
			
			outgoingsms.setSms(response + " "+ getTailTextNotSubecribed().replaceAll("<KEYWORD>", req.getKeyword()).replaceAll("<PRICE>", incomingsms.getPrice().toEngineeringString()));
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}finally{
			
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
		
	}
	

	@Override
	public Connection getCon() {
		return null;
	}

	
	@Override
	public BaseEntityI getEJB() {
		return this.cmpbean;
	}
}
