package com.pixelandtag.subscription;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.MOProcessorFactory;
import com.pixelandtag.api.ServiceProcessorI;
import com.pixelandtag.api.Settings;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.serviceprocessors.dto.ServiceSubscription;
import com.pixelandtag.serviceprocessors.dto.SubscriptionDTO;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.web.triviaI.MechanicsI;

public class SubscriptionMain{
	
	private Logger logger  = Logger.getLogger(SubscriptionMain.class);
	private static DBPoolDataSource ds;
	private ArrayBlockingQueue<ServiceSubscription> to_be_pushed = null;
	private static Semaphore uniq;
	private static Properties log4Jprops = null;
	static{
		uniq = new Semaphore(1, true);
	}

	
	private final String sub = "SELECT ss.id as 'service_subscription_id', pro.id as 'mo_processor_id_fk', pro.shortcode,pro.ServiceName,sm.cmd,sm.CMP_Keyword,sm.CMP_SKeyword,sm.price as 'price', sm.push_unique,ss.serviceid as 'sms_serviceid',pro.threads,pro.ProcessorClass as 'ProcessorClass'"
				+" FROM `celcom`.`ServiceSubscription` ss "
				+"LEFT JOIN `celcom`.`sms_service` sm "
				+"ON sm.id = ss.serviceid "
				+"LEFT JOIN `celcom`.`mo_processors` pro "
				+"ON pro.id = sm.mo_processorFK WHERE pro.enabled=1 AND hour(`ss`.`schedule`)=hour(now()) AND `ss`.`lastUpdated`<now() AND `ss`.`ExpiryDate`>now()";
	
	private Map<Integer,ArrayBlockingQueue<SubscriptionDTO>> processor_map = new HashMap<Integer,ArrayBlockingQueue<SubscriptionDTO>>();
	
	
	public SubscriptionMain(){
		init();
	}
	
	private void init() {
		
		
		log4Jprops = getPropertyFile("log4j.properties");
		//BasicConfigurator.configure();
		PropertyConfigurator.configure(log4Jprops);
		
		logger.debug("\ninitializing...........");
		
		
		init_datasource();	
		init_processor_map();
		populateServicesToBePushed();
		
		
		
	}

	private void init_datasource() {
		
		int vendor = DriverUtilities.MYSQL;
	    String driver = DriverUtilities.getDriver(vendor);
	    String host = "db";
	    String dbName =  HTTPMTSenderApp.props.getProperty("DATABASE");
	    String url = DriverUtilities.makeURL(host, dbName, vendor);
	    String username = "root";
	    String password = "";
	    
	    
	    ds = new DBPoolDataSource();
	    ds.setValidatorClassName("snaq.db.Select1Validator");
	    ds.setName("pool-ds");
	    ds.setDescription("Pooling DataSource");
	    ds.setDriverClassName(driver);
	    ds.setUrl(url);
	    ds.setUser(username);
	    ds.setPassword(password);
	    ds.setMinPool(2);
	    ds.setMaxPool(3);
	    ds.setMaxSize(5);
	    ds.setIdleTimeout(3600);  // Specified in seconds.
	    
	    ds.setValidationQuery("SELECT 'Test'");
	    
	    logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>> initialize datasource well.");
		
	}

	private void init_processor_map() {
		
		Connection conn = getConn();
		
		Statement stmt = null;
		
		ResultSet rs = null;
		
		try {
			
			stmt = conn.createStatement();
			
			
			rs = stmt.executeQuery(sub);
			
			SubscriptionDTO subdto = null;
			
			ArrayBlockingQueue<SubscriptionDTO> subscr = null;
			
			while(rs.next()){
				
				final int threads = rs.getInt("threads");
				final int service_id = rs.getInt("sms_serviceid");
				final String service_processor_class_name = rs.getString("ProcessorClass");
				
				subscr = new ArrayBlockingQueue<SubscriptionDTO>(1000,true);
				
				for(int i = 0; i<threads; i++){
					
					
					logger.debug(" >>>>>>>>>>>>>>>>>>>>>>>>>>> service name >>>> "+rs.getString("ServiceName"));
				
					subdto = new SubscriptionDTO();
					subdto.setProcessor_id(rs.getInt("mo_processor_id_fk"));
					subdto.setShortcode(rs.getString("shortcode"));
					subdto.setServiceName(rs.getString("ServiceName"));
					subdto.setCmd(rs.getString("cmd"));
					subdto.setCMP_AKeyword(rs.getString("CMP_Keyword"));
					subdto.setCMP_SKeyword(rs.getString("CMP_SKeyword"));
					subdto.setPush_unique(rs.getInt("push_unique"));
					subdto.setServiceid(service_id);
					subdto.setThreads(threads);
					subdto.setProcessorClass(service_processor_class_name);
					subdto.setPrice(rs.getDouble("price"));
					subdto.setId(rs.getInt("service_subscription_id"));
					
					ServiceProcessorI processor = MOProcessorFactory.getProcessorClass(service_processor_class_name,ServiceProcessorI.class);
					
					processor.setName(i+"_"+service_processor_class_name);
					processor.setName(i+"_"+subdto.getServiceName());
					processor.setInternalQueue(50);
					
					Thread t = new Thread(processor);
					t.start();
					
					subdto.setProcessor(processor);
					
					subscr.add(subdto);
					
				}
				
				processor_map.put(service_id, subscr);
			
			}
			
		} catch (Exception e) {
			
			log(e);
		
		}finally{
			
			try {
				rs.close();
			} catch (Exception e) {}
			
			try {
				stmt.close();
			} catch (Exception e) {}
			
			try {
				conn.close();
			} catch (Exception e) {}
		}
		
	}
	
	
	
	public static String generateNextTxId() throws InterruptedException{
		
		try{
			
			uniq.acquire();
			
			String timestamp = String.valueOf(System.currentTimeMillis());
			
			return Settings.INMOBIA_SUB.substring(0, (19-timestamp.length())) + timestamp;//(String.valueOf(Long.MAX_VALUE).length()-timestamp.length())) + timestamp;
		
		}finally{
			
			uniq.release();
		
		}
		
	}		

	private void log(Exception e) {
		logger.error(e.getMessage(),e);
	}

	
	/**
	 * Gets a connection object from the datasource
	 * @return - java.sql.Connection  - conn
	 */
	private Connection getConn() {
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			log(e);
		}
		return conn;
	}
	

	public void populateServicesToBePushed(){
		
		to_be_pushed = new ArrayBlockingQueue<ServiceSubscription>(1000,true);
		
		Connection conn = getConn();
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM `celcom`.`ServiceSubscription` WHERE hour(`schedule`)<=hour(now()) AND `lastUpdated`<now() AND ExpiryDate>now()");//first get services to be pushed now.
			
			
			ServiceSubscription subdto;
			
			while(rs.next()){
				subdto = new ServiceSubscription();
				subdto.setId(rs.getInt("id"));
				subdto.setServiceid(rs.getInt("serviceid"));
				subdto.setSchedule(rs.getString("schedule"));
				subdto.setLastUpdated(rs.getString("lastUpdated"));
				subdto.setExpiryDate(rs.getString("ExpiryDate"));
				
				to_be_pushed.put(subdto);
			}
			
			
			
		} catch (Exception e) {
			log(e);
		}finally{
			
			try {
				rs.close();
			} catch (Exception e) {}
			try {
				stmt.close();
			} catch (Exception e) {}
			try {
				conn.close();
			} catch (Exception e) {}
		}
		
	}
	
	public void pushSubscriptions(){
		
		
		logger.debug(" \n\n\n\n======================= TO BE PUSHED arraylist?? "+to_be_pushed+"\nsize : "+to_be_pushed.size()+"\n==========================================\n\n\n");
		for(ServiceSubscription s : to_be_pushed){
			
			
			logger.debug(" \n\n\n\n======================= SERVICESUBSCRIPTION "+s.toString()+"\n==========================================\n\n\n");
			
			final int service_id = s.getServiceid();
			final int subscription_service_id = s.getId();
			final String service_name = "Subscription thread:  " + processor_map.get(service_id).peek().getServiceName();
			
			ArrayBlockingQueue<SubscriptionDTO> processors = processor_map.get(service_id);
			
			SubscriptionWorker sw = new SubscriptionWorker(service_name,service_id,subscription_service_id,processors);
			
			Thread t = new Thread(sw);
			t.start();
			
			
		}
		
		
		//finalizeMe();
		
	}
	
	
	@Deprecated
	public void finalizeMe(){
		
		
		//Finalize all threads.
		for(Map.Entry<Integer, ArrayBlockingQueue<SubscriptionDTO>> entry : processor_map.entrySet()){
			
			Iterator<SubscriptionDTO> it = entry.getValue().iterator();
			
			SubscriptionDTO dto;
			
			while(it.hasNext()){
				
				dto = it.next();
				
				try{
					MOSms mo = new MOSms();
					mo.setCMP_Txid("-1");
					dto.getProcessor().submit(mo);
				}catch(Exception e){
					log(e);
				}
				
			}
			
		}
		
		
		
		boolean somebusy = true;
		
		int x = 0;
		
		while(somebusy){
			
			
			logger.debug("size: " +processor_map.size());
			
			
			//
			somebusy = false;
			//Finalize all threads.
			for(Map.Entry<Integer, ArrayBlockingQueue<SubscriptionDTO>> entry : processor_map.entrySet()){
				
				Iterator<SubscriptionDTO> it2 = entry.getValue().iterator();
				
				SubscriptionDTO dto;
				
				while(it2.hasNext()){
					
					x++;
					if(x==1)
						logger.debug("waiting");
					else
						logger.debug("...");
					dto = it2.next();
					
					
					logger.debug(" DTO:::: "+dto);
					try{
						if(dto.getProcessor().isRunning()){
							somebusy = true;
						}else{
							dto.getProcessor().setRun(false);
							dto.getProcessor().finalizeMe();
							
						}
					}catch(Exception e){
						log(e);
					}
					
				}
			}
			
			
			
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				log(e);
			}
		}
		
		
		ds.releaseConnectionPool();
		
		logger.debug(">>>>>>>>>>>>>>>> Subscription program finished");
		//System.exit(0);
	
	}
	
	
	public static void main(String[] args){
		/*String sub = "SELECT pro.id as 'mo_processor_id_fk', pro.shortcode,pro.ServiceName,sm.cmd,sm.CMP_Keyword,sm.CMP_SKeyword,sm.price, sm.push_unique,ss.serviceid as 'sms_serviceid',pro.threads,pro.ProcessorClass as 'ProcessorClass'"
				+" FROM `celcom`.`ServiceSubscription` ss "
				+"LEFT JOIN `celcom`.`sms_service` sm "
				+"ON sm.id = ss.serviceid "
				+"LEFT JOIN `celcom`.`mo_processors` pro "
				+"ON pro.id = sm.mo_processorFK WHERE pro.enabled=1";*/
		
		
	//	logger.debug(sub);
		new SubscriptionMain().pushSubscriptions();
		//logger.debug(t);
		//logger.debug("||||||||||||| >>>>>>>>>>>>>>>> Subscription program finished");
		
	}
	
	
	/**
	 * This gets the property file
	 * @param filename String the file name for the given property file
	 * @return java.util.Properties instance of the created property file
	 */
	private Properties getPropertyFile(String filename) {

		Properties prop = new Properties();
		InputStream inputStream = null;
		;
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
