package com.pixelandtag.datatransfer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.inmobia.ibx.client.IBXClient;
import com.inmobia.ibx.dto.Command;



public class HitSender {
	
	Connection conn = null;
	private Logger logger = Logger.getLogger(HitSender.class);
	private Properties log4Jprops;
	private Properties appProperties;
	private String conString,ibxServerURL;
	private String opcoID;
	private int throttleSize;
	
	
	public void initialize(){
		
		try {
			
			Class.forName("com.mysql.jdbc.Driver");
		
		} catch (ClassNotFoundException e) {
			
			logger.error(e.getMessage(),e);
			
		}
		
		log4Jprops = getPropertyFile("log4j.properties");
		
		appProperties = getPropertyFile("trivia.properties");
		
		opcoID = appProperties.getProperty("opcoID");
		
		PropertyConfigurator.configure(log4Jprops);
		//BasicConfigurator.configure();
		
		conString = appProperties.getProperty("constr");
		
		ibxServerURL = appProperties.getProperty("ibxServerURL");
		
		try{
			throttleSize = Integer.valueOf(appProperties.getProperty("throttleSize"));
		}catch(Exception e){
			throttleSize = 1000;
			logger.error(e.getMessage(),e);
		}
		
	}
	
	public static void main(String[] args){
		
		HitSender hs = new HitSender();
		
		hs.initialize();
		
		hs.sendHitsToMiami();
		
	}
	
	
	public void sendHitsToMiami(){
		
		Connection conn = getConnection(conString);
		
		IBXClient client = new IBXClient();
		client.setIbxServerURL(ibxServerURL);
		System.out.println("usingURL: "+ibxServerURL);
		//Data Request from server/ Download
		Command c = new Command();
		c.setDataSourceSql("SELECT id,"+opcoID+" as 'country_id',msisdn,correct,question_idFK as 'question_id',timeStamp,answer,name,points,0 as 'isUSSD',CONVERT_TZ(CURRENT_TIMESTAMP,'+08:00','+08:00') as 'miami_timestamp', price as 'amount' FROM `axiata_trivia`.`trivia_log` WHERE dirty=1 LIMIT "+throttleSize);
		c.setDb("axiata_trivia");
		c.setSourceDbTable("trivia_log");//set the db table
		c.setDestDbTable("trivia_master_log");
		c.setId("1");
		c.setType("insert");
		c.setDownload(false);
		c.setUpload(true);
		c.setUpdate(true);
		
		c.setInsetIgnore(false);// if you want the query to be constructed with insert ignore
		c.setBatchUpdate(false);//when you want to do a batch update.
		
		c.setPrimaryKeyFieldName("id");//This is the primay Key we need
		c.setUploadReportNeeded(true);
		
		c.setFlagKeyName("dirty");//the flag that says the records must be uploaded
		c.setSuccessFlagValue("0");//What the above field will be set to when records are uploaded successfully
		
		
		boolean success = client.handle(c, conn);
		System.out.println("THE QUERY was run, data uploaded to the server and  result: "+success);
		
		
		try {
			
			if(conn!=null)
				conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets a connection object.
	 * If the field Connection object is not null and is not closed,
	 * then it is returned. Else a new one is made and returned.
	 * @return
	 */
	public synchronized Connection getConnection(String conStr) {
		
		try {
			if(conn !=null)
				if(!conn.isClosed())
					return conn;
		} catch (SQLException e1) {
			logger.error(e1,e1);
		}
		

		while( true ) {
			try {
				while ( conn==null || conn.isClosed() ) {
					try {
						conn = DriverManager.getConnection(conStr);
					} catch ( Exception e ) {
						logger.warn(e,e);
						try { Thread.sleep(500); } catch ( Exception ee ) {}
					}
				}
				return conn;
			} catch ( Exception e ) {
				logger.error(e,e);
				try { Thread.sleep(500); } catch ( Exception ee ) {}
			}
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
