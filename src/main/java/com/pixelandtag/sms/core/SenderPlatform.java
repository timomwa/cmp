package com.pixelandtag.sms.core;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.pixelandtag.entities.URLParams;
import com.pixelandtag.sms.mo.MOProcessorThread;
import com.pixelandtag.sms.producerthreads.BulkSMSProducer;

public class SenderPlatform {

	private Logger logger = Logger.getLogger(getClass());
	
	
	private String mturl = null;
	private String mt_username = null;
	private String mt_password = null;
	private String constr = null;
	private int throttle = 0;
	private int workers = 5;
	private int initialDBConnections = 5;
	private int maxDBConnections = 10;
	private int queueSize = 0;
	private int pollWait = 1000;
	public static Properties props;
	private Properties log4J;
	private URLParams urlparams;
	
	

	public void initialize(){
		
		props = getPropertyFile("mtsender.properties");
		log4J = getPropertyFile("log4j.properties");
		
		
		urlparams = new URLParams(props);
		
		this.constr = props.getProperty("constr");
		
		if(props.getProperty("THROTTLE")!=null)
			this.throttle = Integer.valueOf(props.getProperty("THROTTLE"));
		
		if(props.getProperty("WORKER_THREADS")!=null)
			this.workers = Integer.valueOf(props.getProperty("WORKER_THREADS"));
		
		if(props.getProperty("initialDBConnections")!=null)
			this.initialDBConnections = Integer.valueOf(props.getProperty("initialDBConnections"));
		
		if(props.getProperty("maxDBConnections")!=null)
			this.maxDBConnections = Integer.valueOf(props.getProperty("maxDBConnections"));
		
		if(props.getProperty("queueSize")!=null)
			this.queueSize = Integer.valueOf(props.getProperty("queueSize"));
		
		if(props.getProperty("pollWait")!=null)
			this.pollWait = Integer.valueOf(props.getProperty("pollWait"));
		
		if(log4J!=null)
			PropertyConfigurator.configure(log4J); 
		else
			PropertyConfigurator.configure(createProps());//BasicConfigurator.configure();
		
		StringBuffer sb = new StringBuffer();
		sb.append("\nmturl: ").append(mturl)
		.append("\nmt_username: ").append(mt_username)
		.append("\nmt_password: ").append(mt_password)
		.append("\nTHROTTLE: ").append(throttle)
		.append("\nWORKER_THREADS: ").append(workers)
		.append("\ninitialDBConnections: ").append(initialDBConnections)
		.append("\nmaxDBConnections: ").append(maxDBConnections)
		.append("\nqueueSize: ").append(queueSize)
		.append("\npollWait: ").append(pollWait)
		.append("\nurlparams: ").append(urlparams.toString());
		
		
		logger.info("::>>>????::CONFIG::::\n"+sb.toString());
	
	}
	
	public static void main(String[] args) throws Exception{
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() { 
		    	System.out.println("SHUTTING DOWN!");
		    	OutgoingQueueRouter.stopApp();
		    	BulkSMSProducer.stopApp();
		    }
		});
		
		SenderPlatform senderplatform = new SenderPlatform();
		senderplatform.initialize();
		
		OutgoingQueueRouter outgoingqueuRouter = new OutgoingQueueRouter();
		outgoingqueuRouter.start();
		
		MOProcessorThread moproc = new MOProcessorThread();
		moproc.start();
		
		BulkSMSProducer bp = new BulkSMSProducer();
		bp.start();
		
	}
	
	
	
	private static Properties createProps() {
		Properties props = new Properties();
		props.put("log4j.rootCategory", "INFO, dailyApp");
		props.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
		props.put("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
		props.put("log4j.appender.A1.layout.ConversionPattern", "%d %-4r [%t] %-5p %c{2} %x - %m%n");
		props.put("log4j.appender.dailyApp", "org.apache.log4j.ConsoleAppender");
		props.put("log4j.appender.dailyApp.layout", "org.apache.log4j.PatternLayout");
		props.put("log4j.appender.dailyApp.layout.ConversionPattern", "%d %-4r [%t] %-5p %c{2} %x - %m%n");
		return props;
	}

	private String getPath(String filename){
		return System.getProperty("user.dir")
		+ System.getProperty("file.separator") + filename;
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
