package com.pixelandtag.mms.application;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.pixelandtag.mms.producerthreads.MMSProducer;


/**
 * 
 * @author Timothy Mwangi Gikonyo
 * Daemon app For sending MMS messages.
 *
 */
public class MMSSenderDaemon extends Thread{
	
	private static Logger logger = Logger.getLogger(MMSSenderDaemon.class);
	
	private long throttle = 0;
	private long workers = 5;
	private long fetchInterval = 100;
	private String ws_endpoint;
	private String server_timezone;
	private String client_timezone;
	private Properties properties;
	private Properties log4JProperties;
	
	
	public void initialize() throws Exception{
		
		
		properties = getPropertyFile("mmsender.properties");
		log4JProperties = getPropertyFile("mm7Log4J.properties");
		
		
		PropertyConfigurator.configure(log4JProperties);
		
		
		throttle = Long.parseLong(properties.getProperty("throttle"));
		workers = Long.parseLong(properties.getProperty("workers"));
		fetchInterval = Long.parseLong(properties.getProperty("fetchInterval"));
		ws_endpoint  = properties.getProperty("ws_endpoint");
		server_timezone  = properties.getProperty("server_timezone");
		client_timezone  = properties.getProperty("client_timezone");
		
		StringBuffer sb = new StringBuffer();
		sb.append("\nthrottle").append(" = ").append(throttle)
		.append("\nworkers").append(" = ").append(workers)
		.append("\nfetchInterval").append(" = ").append(fetchInterval)
		.append("\nws_endpoint").append(" = ").append(ws_endpoint)
		.append("\nserver_timezone").append(" = ").append(server_timezone)
		.append("\nclient_timezone").append(" = ").append(client_timezone);
		logger.info("\n\n\n=================APP STARTED ============================");
		logger.info(sb.toString());
		
		MMSProducer producer = new MMSProducer(workers, throttle, fetchInterval,ws_endpoint,server_timezone,client_timezone);
		
		producer.start();
		
		
	}
	
	
	
	
	public static void main(String[] args){
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() { 
		    	System.out.println("SHUTTING DOWN!");
		    	MMSProducer.stopApp();
		    	
		    }
		});
		
		
		MMSSenderDaemon mssender = new MMSSenderDaemon();
		
		
		try {
			
			mssender.initialize();
			
			mssender.start();
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
		
		}
		
		
		
		
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

}
