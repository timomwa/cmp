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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.api.MOProcessorFactory;
import com.pixelandtag.api.ServiceProcessorI;
import com.pixelandtag.api.Settings;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.serviceprocessors.dto.ServiceSubscription;
import com.pixelandtag.serviceprocessors.dto.SubscriptionDTO;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.web.triviaI.MechanicsI;

public class SubscriptionMain implements Runnable{
	
	private Logger logger  = Logger.getLogger(SubscriptionMain.class);
	//private static DBPoolDataSource ds;
	private ArrayBlockingQueue<ServiceSubscription> to_be_pushed = null;
	private static Semaphore uniq;
	private static Properties log4Jprops = null;
	private static Properties subscription_props = null;
	private String server_tz;
	private String client_tz;
	//private String host;
	//private String dbName;
	//private String username;
	//private String password;
	private  Context context = null;
	private CMPResourceBeanRemote cmpbean;
	public void initEJB() throws Exception{
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
			 try {
				 cmpbean.setServerTz(server_tz);
				 cmpbean.setClientTz(client_tz);
			} catch (Exception e) {
				throw new Exception("Problem while setting time zones! server_tz="+server_tz+", client_tz="+client_tz+". Error: "+e.getMessage(),e); 
			}
	}
	static{
		uniq = new Semaphore(1, true);
	}
	
	public static String DB = "pixeland_content360";

	
	private String sub;
	
	private Map<Integer,ArrayBlockingQueue<SubscriptionDTO>> processor_map = new HashMap<Integer,ArrayBlockingQueue<SubscriptionDTO>>();
	private String constr_;
	private boolean run = false;
	
	
	public SubscriptionMain() throws Exception{
		init();
	}
	
	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

	private void init() throws Exception {
		
		log4Jprops = getPropertyFile("log4jsub.properties");
		subscription_props = getPropertyFile("mtsender.properties");
		
		
		//host = subscription_props.getProperty("db_host");
		//dbName= subscription_props.getProperty("DATABASE");
		//username= subscription_props.getProperty("db_username");
		//password= subscription_props.getProperty("db_password");
		constr_= subscription_props.getProperty("constr");
		
		server_tz = subscription_props.getProperty("SERVER_TZ");
		client_tz = subscription_props.getProperty("CLIENT_TZ");
		
		sub = "SELECT "
				+ "ss.id as 'service_subscription_id', "//0
				+ "pro.id as 'mo_processor_id_fk', "//1
				+ "pro.shortcode,"//2
				+ "pro.ServiceName,"//3
				+ "sm.cmd,"//4
				+ "sm.CMP_Keyword,"//5
				+ "sm.CMP_SKeyword,"//6
				+ "sm.price as 'price', "//7
				+ "sm.push_unique,"//8
				+ "ss.serviceid as 'sms_serviceid',"//9
				+ "pro.threads,"//10
				+ "pro.ProcessorClass as 'ProcessorClass',"//11
				+ "sm.price_point_keyword"//12
				+" FROM `"+CelcomImpl.database+"`.`ServiceSubscription` ss "
				+"LEFT JOIN `"+CelcomImpl.database+"`.`sms_service` sm "
				+"ON sm.id = ss.serviceid "
				+"LEFT JOIN `"+CelcomImpl.database+"`.`mo_processors` pro "
				+"ON pro.id = sm.mo_processorFK WHERE pro.enabled=1 AND hour(`ss`.`schedule`)=hour(convert_tz(now(),'"+server_tz+"','"+client_tz+"')) AND `ss`.`lastUpdated`<convert_tz(now(),'"+server_tz+"','"+client_tz+"') AND `ss`.`ExpiryDate`>convert_tz(now(),'"+server_tz+"','"+client_tz+"')";

		//System.out.println(sub);
		//BasicConfigurator.configure();
		PropertyConfigurator.configure(log4Jprops);
		
		logger.debug("\ninitializing...........");
		

		initEJB();
		init_datasource();	
		
		
		
		
	}

	private void init_datasource() {
		
		/*int vendor = DriverUtilities.MYSQL;
	    String driver = DriverUtilities.getDriver(vendor);
	   
	   
	    this.constr_ = DriverUtilities.makeURL(host, dbName, vendor,username,password);
	    
	    
	    ds = new DBPoolDataSource();
	    ds.setValidatorClassName("snaq.db.Select1Validator");
	    ds.setName("pool-ds");
	    ds.setDescription("Pooling DataSource");
	    ds.setDriverClassName(driver);
	    ds.setUrl(constr_);
	    ds.setMinPool(2);
	    ds.setMaxPool(3);
	    ds.setMaxSize(5);
	    ds.setIdleTimeout(3600);  // Specified in seconds.
	    
	    ds.setValidationQuery("SELECT 'Test'");
	    
	    logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>> initialize datasource well.");*/
		
	}

	private void init_processor_map() {
		
		try {
			
			 try {
				 cmpbean.setServerTz(server_tz);
				 cmpbean.setClientTz(client_tz);
			} catch (Exception e) {
				throw new Exception("Problem while setting time zones! server_tz="+server_tz+", client_tz="+client_tz+". Error: "+e.getMessage(),e); 
			}
			ArrayBlockingQueue<SubscriptionDTO> subscr = null;
			
			cmpbean.deleteOldLogs();
			
			List<SubscriptionDTO> subscrList =  cmpbean.getSubscriptionServices();
			
			for(SubscriptionDTO subdto: subscrList){
				
				
				subscr = processor_map.get(subdto.getServiceid());
				int threads = subdto.getThreads();
				int threadBalance = threads;//we need to 
				
				//check if that processor's thread has already been initialized and started
				if(subscr!=null){
					int sizeLiveprocessors = processor_map.get(subdto.getServiceid()).size();
					int requiredProcessorthreads = subdto.getThreads();
					threadBalance = requiredProcessorthreads - sizeLiveprocessors;
				}
				
				
				if(subscr==null)
					subscr = new ArrayBlockingQueue<SubscriptionDTO>(1000,true);
				
				
				
				for(int i = 0; i<threadBalance; i++){//add the balance of threads
					
					String service_processor_class_name = subdto.getProcessorClass();
					ServiceProcessorI processor = MOProcessorFactory.getProcessorClass(service_processor_class_name,ServiceProcessorI.class);
					
					processor.setName(i+"_"+service_processor_class_name);
					processor.setName(i+"_"+subdto.getServiceName());
					processor.setInternalQueue(50);
					logger.info("started : "+service_processor_class_name);
					Thread t = new Thread(processor);
					t.start();
					
					subdto.setProcessor(processor);
					
					subscr.add(subdto);
					
				}
				
				processor_map.put(subdto.getServiceid(), subscr);
			
			}
			
		} catch (Exception e) {
			
			log(e);
		
		}finally{
			
		}
		
	}
	
	
	
	public static long generateNextTxId() throws InterruptedException{
		
		try{
			
			uniq.acquire();
			try{
				Thread.sleep(1);
			}catch(Exception e){}
			/*String timestamp = String.valueOf(System.currentTimeMillis());
			
			return Settings.INMOBIA_SUB.substring(0, (19-timestamp.length())) + timestamp;//(String.valueOf(Long.MAX_VALUE).length()-timestamp.length())) + timestamp;
*/		
			return System.currentTimeMillis();
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
	/*private Connection getConn() {
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			log(e);
		}
		return conn;
	}*/
	

	public void populateServicesToBePushed(){
		
		//if(to_be_pushed==null)
			to_be_pushed = new ArrayBlockingQueue<ServiceSubscription>(1000,true);
		
		try {
			
			List<ServiceSubscription> servSub = cmpbean.getServiceSubscription();
			
			logger.info("servSub.size(): "+servSub.size());
			
			for(ServiceSubscription  subdto : servSub)
				to_be_pushed.put(subdto);
			
		} catch (Exception e) {
			log(e);
		}finally{
			
		}
		
	}
	
	public void pushSubscriptions() throws Exception{
		
		StringBuffer sb = new StringBuffer();
		
	//	logger.debug(" \n\n\n\n======================= TO BE PUSHED arraylist?? "+to_be_pushed+"\nsize : "+to_be_pushed.size()+"\n==========================================\n\n\n");
		for(ServiceSubscription s : to_be_pushed){
			
			
		//	logger.debug(" \n\n\n\n======================= SERVICESUBSCRIPTION "+s.toString()+"\n==========================================\n\n\n");
		//	logger.debug(" \n\n\n\n======================= processor_map.size() "+processor_map.size()+"\n==========================================\n\n\n");
			final int service_id = s.getServiceid();
			final int subscription_service_id = s.getId();
			int x = cmpbean.countSubscribers(service_id);
			int y = cmpbean.countPushesToday(service_id); 
			//int hour_now = cmpbean.getHourNow();
			
			boolean pushnow = cmpbean.shouldPushNow(service_id);
			try{
				Thread.sleep(1000);
			}catch(Exception e){
				logger.error(e.getMessage(),e);
			}
			sb.append("\n\t\tSERVICE ID            : ").append(service_id);
			sb.append("\n\t\tTOTAL SUBSRIBERS      : ").append(x);
			sb.append("\n\t\t# SUBSCRIPTION PUSHES : ").append(y);
			sb.append("\n\t\tPUSH NOW : ").append(pushnow);
			logger.debug(sb.toString());
			
			sb.setLength(0);
			if((x>y) && pushnow)//if subscribers are more than the number of pushed count and it's the hour to push
				if(processor_map.size()>0){
					
					final String service_name = "Subscription thread:  " + processor_map.get(service_id).peek().getServiceName();
					
					ArrayBlockingQueue<SubscriptionDTO> processors = processor_map.get(service_id);
					
					SubscriptionWorker sw = new SubscriptionWorker(cmpbean, server_tz,client_tz,constr_,service_name,service_id,subscription_service_id,processors);
					Thread t = new Thread(sw);
					t.start();
					
					try{
						Thread.sleep(500);//sleep 1/2 second. Save CPU
					}catch(Exception e){
						logger.error(e.getMessage(),e);
					}
				}
				
		}
		
		
		//finalizeMe();
		
	}
	
	
	public void finalizeMe(){
		
		setRun(false);
		
		
		try {
			if(context!=null)
				context.close();
		} catch (NamingException e1) {
			e1.printStackTrace();
		}catch (RejectedExecutionException e1) {
			//https://issues.jboss.org/browse/EJBCLIENT-98
		}catch (Exception e1) {
			logger.error(e1.getMessage(),e1);
		}
			
		
		//Finalize all threads.
		for(Map.Entry<Integer, ArrayBlockingQueue<SubscriptionDTO>> entry : processor_map.entrySet()){
			
			Iterator<SubscriptionDTO> it = entry.getValue().iterator();
			
			SubscriptionDTO dto;
			
			while(it.hasNext()){
				
				dto = it.next();
				
				try{
					MOSms mo = new MOSms();
					mo.setCMP_Txid(-1l); 
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
		
		
		//ds.releaseConnectionPool();
		
		logger.debug(">>>>>>>>>>>>>>>> Subscription program finished");
		//System.exit(0);
	
	}
	
	
	public static void main(String[] args) throws Exception{
		SubscriptionMain subApp = new SubscriptionMain();
		Thread t = new Thread(subApp);
		t.start();
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

	@Override
	public void run() {

		

		try{
			
			setRun(true);
			while(run){
				init_processor_map();
				populateServicesToBePushed();
				pushSubscriptions();
				try{
					Thread.sleep(1000);
				}catch(Exception e){
				}
			}
			
		}catch(Exception e){

			setRun(false);
			logger.error(e.getMessage(),e);
		}finally{
			finalizeMe();
		}
		
	}
	
	
}
