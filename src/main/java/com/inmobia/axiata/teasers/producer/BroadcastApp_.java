package com.inmobia.axiata.teasers.producer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.inmobia.axiata.web.beans.BroadcastType;
import com.inmobia.celcom.sms.producerthreads.MTProducer;


public class BroadcastApp_ {

	
	private Logger logger = Logger.getLogger(BroadcastApp_.class);
	private String conString;
	private Properties log4Jprops,appProperties;
	public static String SERVER_TZ = "-05:00";
	public static String CLIENT_TZ = "+08:00";
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		BroadcastApp_ tt = new BroadcastApp_();
		tt.initialize();
		
		tt.runNormalTeaser();
		

	}
	
	
	public void runNormalTeaser() throws Exception{
		
		/*Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() { 
		    	
		    	System.out.println("SHUTTING DOWN!");
		    	BroadcastProducer.stopApp();
		    	
		    }
		});*/
		
		
	
	
		
		
		logger.info("FINISHED!!!!!!");
		
		
	}
	
	
	/**
	 * Creates a java.util.Properties object from 
	 * the file specified by the param filename
	 * @param filename the name of the properties file
	 * @return java.util.Properties ojbect created and populated
	 *          with the property-values set on the file "filename"
	 */
	public Properties getPropertyFile(String filename) {

		Properties prop = new Properties();
		InputStream inputStream = null;
		
		String path;
		try {
			path = System.getProperty("user.dir")
					+ System.getProperty("file.separator") + filename;
			inputStream = new FileInputStream(path);
		} catch (Exception e) {
			URL urlpath = new String().getClass().getResource(filename);
			try {
				inputStream = new FileInputStream(urlpath.getPath());
			} catch (Exception exb) {
				logger.error(filename + " not found!");
			}
		}
		try {
			if (inputStream != null) {
				prop.load(inputStream);
				inputStream.close();

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return prop;
	}
	
	
	public void initialize(){
		
		try {
			
			Class.forName("com.mysql.jdbc.Driver");
		
		} catch (ClassNotFoundException e) {
			
			logger.error(e.getMessage(),e);
			
		}
		
		log4Jprops = getPropertyFile("log4jteasers.properties");
		
		appProperties = getPropertyFile("teaserapp.properties");
		
		SERVER_TZ=appProperties.getProperty("SERVER_TZ");
		
		CLIENT_TZ=appProperties.getProperty("CLIENT_TZ");
		
		//PropertyConfigurator.configure(log4Jprops);
		BasicConfigurator.configure();
		
		conString = appProperties.getProperty("constr");
		
	}
	
	
	/**
	 * Get a connection
	 * @return
	 */
	public Connection getConn(String connectionStr){
		
		Connection conn = null;
		
		try {
		
			conn = DriverManager.getConnection(connectionStr);
		
		} catch ( Exception e ) {
			
			logger.error(e,e);
		
		}
	
		return conn;
	}
	
	/**
	 * Get a connection.
	 * 
	 * uses connstring: 
	 * @return
	 */
	public Connection getConn(){
		
		Connection conn = null;
		
		try {
		
			conn = DriverManager.getConnection("jdbc:mysql://localhost/?user=root&password=");
		
		} catch ( Exception e ) {
			
			logger.error(e,e);
		
		}
	
		return conn;
	}
}
