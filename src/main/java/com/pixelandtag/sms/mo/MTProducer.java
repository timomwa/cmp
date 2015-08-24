package com.pixelandtag.sms.mo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.gjt.mm.mysql.Driver;
import org.hibernate.Query;

import snaq.db.ConnectionPool;
import snaq.db.DBPoolDataSource;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.api.sms.ProcessorResolverEJBI;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.ProcessorType;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.CelcomHTTPAPI;
import com.pixelandtag.api.ERROR;
import com.pixelandtag.api.MOProcessorFactory;
import com.pixelandtag.api.ServiceProcessorI;
import com.pixelandtag.api.Settings;
import com.pixelandtag.api.UnicodeFormatter;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.entities.URLParams;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.sms.mo.MOProcessorThread;
import com.pixelandtag.sms.mt.ACTION;
import com.pixelandtag.sms.mt.CONTENTTYPE;
import com.inmobia.util.StopWatch;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPClientWorker;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPParam;
import com.pixelandtag.sms.producerthreads.NoServiceProcessorException;

/**
 * 
 * @author Timothy Mwangi Gikonyo
 * Date created: Tuesday 7th February 2012.
 * This will retrieve messages from the db and fairly distribute to 
 * http senders to send to the CMP/SMSC
 *
 */


public class MTProducer extends Thread {
	
	private static final boolean FAIR = true;
	private String constr;
	private int workers;
	private int throttle;
	private int initialConnections;
	private int maxConnections;
	private int queueSize;
	private int pollWait;
	private StopWatch watch;
	private boolean run = true;
	private URLParams urlparams; 
	public static CelcomHTTPAPI celcomAPI;
	private Statement stmt = null;
	private ResultSet rs = null;
	private int x = 0;
	public static volatile MTProducer instance;
	private static Logger logger = Logger.getLogger(MTProducer.class);
	private String limitStr;
	private MOProcessorThread moProcessor;
	/**
	 * This will hold a processor pool.
	 */
	public static volatile Map<Integer,ArrayList<ServiceProcessorI>> processor_pool = new ConcurrentHashMap<Integer,ArrayList<ServiceProcessorI>>();
	public static volatile List<ServiceProcessorDTO> serviceProcessors;
	private static Map<Long,ServiceProcessorDTO> serviceProcessoorCache = new HashMap<Long, ServiceProcessorDTO>();
	private int idleWorkers;
	private String fr_tz;
	private String to_tz;
	private Connection conn;
	public static final String DEFLT = "DEFAULT_DEFAULT";
	private static final String HTTP = "http";
	private static final String SMPP = "smpp";
	private static int sentMT = 0;
	private ProcessorResolverEJBI processorEJB;
	private QueueProcessorEJBI queueprocessorEJB;
	
	private static Semaphore semaphoreg;
	private static Semaphore semaphore;//a semaphore because we might need to recover from a deadlock later.. listen for when we have many recs in the db but no msg is being sent out..
	private static Semaphore uniq;
	static{
		uniq = new Semaphore(1, FAIR);
	}
	//private volatile static ConnectionPool pool;
	//private static Semaphore dbSemaphore;
	private static DBPoolDataSource ds;
	private CMPResourceBeanRemote cmpbean;
	private  Context context = null;
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
			 processorEJB =  (ProcessorResolverEJBI) 
			       		context.lookup("cmp/ProcessorResolverEJBImpl!com.pixelandtag.cmp.ejb.api.sms.ProcessorResolverEJBI");
			 queueprocessorEJB =  (QueueProcessorEJBI) 
			       		context.lookup("cmp/QueueProcessorEJBImpl!com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI");
			 logger.info(getClass().getSimpleName()+": Successfully initialized EJB CMPResourceBeanRemote !!");
	 }
	
	
	
	
	
	
	
	public String getFr_tz() {
		return fr_tz;
	}




	public String getTo_tz() {
		return to_tz;
	}




	public void setFr_tz(String fr_tz) {
		this.fr_tz = fr_tz;
	}




	public void setTo_tz(String to_tz) {
		this.to_tz = to_tz;
	}


	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

	public int getSentMT() {
		return sentMT;
	}
	
	public static void resetSentMT() {
		sentMT = 0;
	}

	public static void increaseMT() {
		sentMT++;
	}

	
	
	public void myfinalize(){
		
		try {
			if(context!=null)
				context.close();
		} catch (Exception e) {
			log(e);
		}
		
		try{
			
			conn.close();
		
		}catch(Exception e){
			
			log(e);
		
		}
		
		if(ds!=null)
			ds.releaseConnectionPool();
		
		
		
	}
	
	
	public static void stopApp(){
		System.out.println("Shutting down...");
		
		try{
			if(instance!=null){
				System.out.println("Shutting down...");
				instance.setRun(false);
				System.out.print("...");
				System.out.print("...");
				instance.waitForAllWorkersToFinish();
				System.out.print("...");
				instance.myfinalize();
				System.out.println("...");
			
			}else{
				
				System.out.println("App not yet initialized or started.");
			
			}
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}catch(Error e){
			logger.error(e.getMessage(),e);
		}
		
	}
	
	
	
	
	
	/**
	 * Actually stops all Workers.
	 */
	public synchronized void waitForAllWorkersToFinish(){
		
		boolean finished = false;
		
	}

	
	
	
	/**
	 * Gets connection object from a pool
	 * @return java.sql.Connection
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	private Connection getConnFromDbPool() throws InterruptedException, SQLException{
		
		return ds.getConnection();
	}
	
	/**
	 * Gets the connection.
	 * If it is not closed or null, return the existing connection object,
	 * else create one and return it
	 * @return java.sql.Connection object
	 * @throws InterruptedException 
	 * @throws SQLException 
	 */
	private Connection getConnection() throws InterruptedException, SQLException {
		
		//return getConnFromDbPool();
		
		
		try{
		
			if(conn!=null || !conn.isClosed()){
				conn.setAutoCommit(true);
				return conn;
			}
			
		}catch(Exception e){
			logger.warn("we'll create a new connection!");
			//conn = ds.getConnection("root", "");
		}
		
		
		while( true ) {
			
			try {
				while ( conn==null || conn.isClosed() ) {
					
					
					
					try {
						conn = getConnFromDbPool();//MTProducer.
						logger.debug(">>>> created connection! ");
						return conn;
					} catch ( Exception e ) {
						logger.warn("Could not create connection. Reason: "+e.getMessage());
						try { Thread.sleep(500); } catch ( Exception ee ) {}
					}
				}
				//return conn;
			} catch ( Exception e ) {
				logger.warn("can't get a connection, re-trying");
				try { Thread.sleep(500); } catch ( Exception ee ) {}
			}
		}
		
	}
	

	
	/**
	 * 
	 * @param connStr_ - java.lang.String
	 * @param workers_ int the number of workers who will deal with MT messages.
	 * @param throttle_ - int the time in milliseconds to sleep before checking for new messages in the db.
	 * @param initialConnections_ - int the initial connections to be created.
	 * @param maxConnections_ int the maximum number of connections that can exist in the connection pool
	 * @param urlparams - com.pixelandtag.entities.URLParams - a set of other runtime variables
	 * @throws Exception 
	 */
	public MTProducer(String connStr_, int workers_, int throttle_, int initialConnections_, int maxConnections_, int queueSize_, int pollWait_, URLParams urlparams_) throws Exception{
		
		semaphore = new Semaphore(1, FAIR);
		
		semaphoreg = new Semaphore(1, FAIR);
		
		this.throttle = throttle_;
		
		this.urlparams = urlparams_;
		
		this.constr = connStr_;
		
		this.workers = workers_;
		
		this.initialConnections = initialConnections_;
		
		this.maxConnections = maxConnections_;
		
		this.queueSize = queueSize_;
		
		this.pollWait = pollWait_;
		
		this.fr_tz = urlparams_.getSERVER_TZ(); //timezone
		
		this.to_tz = urlparams_.getCLIENT_TZ(); //timezone
		
		watch = new StopWatch();
		
		initEJB();
		initialize();
	}
	
	
	private void initialize() throws Exception {
		
		watch.start();
	    
	    
	    celcomAPI = new CelcomImpl(ds);
	    
	    try {
	  
	      if(celcomAPI==null){
	    	  logger.info("API is null");
	      }
	      
	      celcomAPI.setFr_tz(fr_tz);
	      
	      celcomAPI.setTo_tz(to_tz);
	      
	      watch.stop();
	      
	      logger.info(">< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to create the connection pool");
	      
	      watch.reset();
	      
	    
	    } catch(Exception sqle) {
	     
	    	logger.error("Error making pool: " + sqle.getMessage(),sqle);
	      
	    
	    }
	    
	    if(queueSize>0){
	    	
	    	limitStr = " LIMIT "+queueSize;
	   
	    }else{
	    	
	    	limitStr = "";
	    
	    }
	    
	 
	    instance = this;
	    
	    
	    while(instance==null){
	    	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
	    }
	   
		
	    initWorkers();
	    
	}

	
	
	public void run() {
		
		try{
			
			while(run){
				
				populateQueue();
				
				try {
					
					Thread.sleep(1000);
				
				} catch (InterruptedException e) {
					
					log(e);
				
				}finally{
					
					watch.start();
				
				}
				
			}
			
			
		}catch(OutOfMemoryError e){
			
			logger.error("NEEDS RESTART: MEM_USAGE: "+MTProducer.getMemoryUsage() +" >> "+e.getMessage(),e);
			
		}finally{
			
		
		}
		
		logger.info("producer shut down!");
		
		System.exit(0);
		
		
	}


	

	/**
	 * Populates the queue
	 * @throws InterruptedException 
	 */
	private void populateQueue()  {
		
		try {
			
			
			List<OutgoingSMS> outgoingsmses = queueprocessorEJB.getUnsent(Long.valueOf(queueSize));
			
			for(OutgoingSMS outgoingsms : outgoingsmses){
				
			
				x++;
				
				
			}
			
			
			
			if(getSentMT()>0){
				
				watch.stop();
				
				logger.info("END!>>>>>>>>>>>>>>>>>>>>< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()/1000d+"")) + " seconds to clear a queue of "+getSentMT()+" messages");
			   
				watch.reset();
				
				MTProducer.resetSentMT();
			}
			
		
		} catch (Exception e) {
			
			log(e);
		
		}finally{
			
			try {
				
				if(rs!=null)
					rs.close();
			
			} catch (SQLException e) {
				
				log(e);
			
			}
			
			
			try {
				
				if(stmt!=null)
					stmt.close();
			
			} catch (SQLException e) {
				
				log(e);
			
			}
						
		}
		
		
	}



	private ServiceProcessorDTO findProcessorDto(int processor_id){
		for(ServiceProcessorDTO servicep : serviceProcessors)
			if(servicep.getId()==processor_id)
				return servicep;
		return null;
	}
	
	


	/**
	 * Wait
	 */
	public synchronized void pauze() {
		
		try {
			
			wait();
		
		} catch (InterruptedException e) {
			
			logger.error(e.getMessage(),e);
		}
	
	}
	
	
	
	/**
	 * notify
	 */
	public synchronized void rezume() {
		
		notify();
	
	}



	/**
	 * Logs the exception
	 * @param e - java.lang.Exception
	 */
	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}
	
	
	
	
	
	public static String getMemoryUsage() {
		long mem = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
		if ( mem>=(1024*1024*1024) )
			return ((int)mem/1024/1024/1024)+" GB";
		if ( mem>=(1024*1024) )
			return ((int)mem/1024/1024)+" MB";
		if ( mem>=(1024) )
			return ((int)mem/1024)+" KB";
		return mem+"B";
			
	}
	
	
	
	
	public static String hexToString(String txtInHex)
    {
        byte [] txtInByte = new byte [txtInHex.length() / 2];
        int j = 0;
        for (int i = 0; i < txtInHex.length(); i += 2)
        {
                txtInByte[j++] = Byte.parseByte(txtInHex.substring(i, i + 2), 16);
        }
        return new String(txtInByte);
    }
	
	
	public static void main(String[] args) {
		System.out.println("["+hexToString("004f006e00200031".replaceAll("00",""))+"]");
	}
	
	

}
