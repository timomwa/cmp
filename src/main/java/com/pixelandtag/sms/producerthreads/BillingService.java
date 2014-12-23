package com.pixelandtag.sms.producerthreads;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import com.inmobia.util.StopWatch;
import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.CelcomHTTPAPI;
import com.pixelandtag.api.ServiceProcessorI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.persistence.CMPDao;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.sms.mt.ACTION;
import com.pixelandtag.sms.mt.CONTENTTYPE;
import com.pixelandtag.sms.mt.workerthreads.HttpBillingWorker;
import com.pixelandtag.sms.mt.workerthreads.MTHttpSender;
import com.pixelandtag.util.FileUtils;
import com.pixelandtag.web.triviaImpl.MechanicsS;

import snaq.db.DBPoolDataSource;

public class BillingService extends Thread{
	
	public static Logger logger = Logger.getLogger(BillingService.class);
	private static Semaphore semaphore = new Semaphore(1, true);;
	private static Semaphore save_Sem = new Semaphore(1, true);
	private static Semaphore uniq;
	private boolean run = true;
	private  Context context = null;
	public static CelcomHTTPAPI celcomAPI;
	private CMPResourceBeanRemote cmpbean;
	private int idleWorkers;
	private String server_tz;
	private String client_tz;
	//private static DBPoolDataSource ds;
	//private Connection conn;
	private StopWatch watch;
	private int x = 0;
	private static int sentMT = 0;
	private int queueSize;
	public volatile  BlockingQueue<HttpBillingWorker> httpSenderWorkers = new LinkedBlockingDeque<HttpBillingWorker>();
	private volatile static BlockingDeque<Billable> billableQ =  new LinkedBlockingDeque<Billable>(1);
	private static final ThreadLocal<Session> session = new ThreadLocal<Session>();

	public static List<Long> refugees = new ArrayList<Long>();
	private static String cannonicalPath = "";
	private static File ft = new File(".");
	static{
		try {
			cannonicalPath = ft.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static File f = new File(cannonicalPath+ System.getProperty("file.separator") +"hibernate.cfg.xml");
	private static  SessionFactory sessionFactory = new AnnotationConfiguration().configure(f).buildSessionFactory();
	private static BillingService instance;
	private Properties log4J;
	private Properties mtsenderprops;
	public static Session getSession() {
		Session session = (Session) BillingService.session.get();
		if (session == null) {
			session = sessionFactory.openSession();
			BillingService.session.set(session);
			getSession().setFlushMode(FlushMode.AUTO);
		}
		return session;
	}

	/**
	 * saves and commits
	 * @param t
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T find(Class<T> entityClass, Long id) {
		Query query = getSession().createQuery("from " + entityClass.getSimpleName() + " WHERE id =:id ").setParameter("id", id);
		try{
			if(query.list().size()>0)
				return (T) query.list().get(0);

			return null;
		}finally{
			close();
		}
	}
	
	
	/**
	 * saves and commits
	 * @param t
	 * @return
	 */
	public static  <T> T saveOrUpdate(T t) {
		logger.debug("in com.pixelandtag.sms.producerthreads.BillingService.saveOrUpdate(T)");
		try {
			getSession().beginTransaction();
			getSession().saveOrUpdate(t);
			getSession().getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			try{
				getSession().getTransaction().rollback();
			}catch(Exception e1){}
		}finally{
			logger.debug("exiting com.pixelandtag.sms.producerthreads.BillingService.saveOrUpdate(T)");
			
			try{
				close();
			}catch(Exception e1){}
		}
		
		return t;
	}
	
	protected void begin() {
		getSession().beginTransaction();
	}

	
	public void commitMe(){
		this.commit();
	}
	protected void commit() {
		getSession().getTransaction().commit();
	}

	protected void rollback() {

		try {
			getSession().getTransaction().rollback();
		} catch (HibernateException e) {
			logger.warn("Cannot rollback", e);
		}
		try {
			getSession().close();
		} catch (HibernateException e) {
			logger.warn("Cannot close", e);
		}
		BillingService.session.set(null);
	}

	public static void close() {
		getSession().close();
		BillingService.session.set(null);
	}
	
	
	
	static{
		uniq = new Semaphore(1, true);
	}
	private ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
	private HttpClient httpsclient;
	 
	private  TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
        @Override
        public boolean isTrusted(X509Certificate[] certificate, String authType) {
            return true;
        }
    };
	private int workers = 1;
    
    
    public BillingService() throws Exception{
    	watch = new StopWatch();
    	initEJB();
    	initWorkers();
    	instance = this;
    }
    
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
		 
		 System.out.println("Successfully initialized EJB CMPResourceBeanRemote !!");
    }
    
    
	private void initWorkers() throws Exception{
		log4J = FileUtils.getPropertyFile("log4j.billing.properties");
		mtsenderprops = FileUtils.getPropertyFile("mtsender.properties");
		
		server_tz = mtsenderprops.getProperty("SERVER_TZ");
		client_tz = mtsenderprops.getProperty("CLIENT_TZ");
		
		PropertyConfigurator.configure(log4J);
		SSLSocketFactory sf = new SSLSocketFactory(acceptingTrustStrategy, 
		SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				    
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", 8443, sf));
		cm = new ThreadSafeClientConnManager(schemeRegistry);
		cm.setDefaultMaxPerRoute(2);
		cm.setMaxTotal(2);//http connections that are equal to the worker threads.
						
		httpsclient = new DefaultHttpClient(cm);
					
		Thread t1;
		for(int i = 0; i<this.workers ; i++){
			HttpBillingWorker worker;
			worker = new HttpBillingWorker(server_tz, client_tz, "THREAD_WORKER_#_"+i,httpsclient, cmpbean);
			t1 = new Thread(worker);
			t1.start();
			httpSenderWorkers.add(worker);
		}
		//wake all thread's because we're done initializing.
		for(HttpBillingWorker worker: httpSenderWorkers)
			worker.rezume();
	}
	

	public static Billable getBillable() {
		
		try{
			
			logger.debug(">>Threads waiting to retrieve message before : " + semaphore.getQueueLength() );
			
			semaphore.acquire();//now lock out everybody else!
			
			logger.debug(">>Threads waiting to retrieve message after: " + semaphore.getQueueLength() );
			
			
			 final Billable billable = billableQ.takeFirst();//performance issues versus reliability? I choose reliability in this case :)
			 
			 try {
				 
				logger.info("billable.getId():  "+billable.getId());
			
			 } catch (Exception e) {
				
				 logger.error("\nRootException: " + e.getMessage()+ ": " +
				 		"\n Something happenned. We were not able to mark " +
				 		"\n the message as being in queue. " +
				 		"\n To prevent another thread re-taking the object, " +
				 		"\n we've returned null to the thread/method requesting for " +
				 		"\na n MT.",e);
				 
				 return null;
			}
			 
			 return billable;
		
		} catch (Exception e1) {
			logger.error(e1.getMessage(),e1);			
			return null;
		}finally{
			
			semaphore.release(); // then give way to the next thread trying to access this method..
			
		
		}
	}
	
	
	
	public void run() {
		
		try{
			
			while(run){
				
				
				try{
					
					populateQueue();
					
				}catch(Exception e){
					
					logger.error(e.getMessage(),e);
				}
				
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
			
			myfinalize();
			
		}
		
		logger.info("producer shut down!");
		
		System.exit(0);
		
		
	}
	
	
	
	private void populateQueue() {
		
		try {
			
			x = 0;
			
			begin();
			
			List<Billable> billables = getBillable(100);
			
			logger.debug("\n\n\n\n\n\n\n\t\t\tbillables:"+billables.size()+"\n\n\n\n\n\n\n");
			
			for(Billable billable : billables){
				
				logger.info("\n\n\t\tbillable.toString():"+billable.toString()+"\n");
				
			
				x++;
				
				
				if(queueSize==0){//if we can add as many objects to the queue, then we just keep adding
					
					
					//adds to the last of the queue. 
					//We don't have limitations of the size of the queue, 
					//so its least likely for there to be no space to add an element.
					try {
						
						//Inserts the specified element at the end of this 
						//deque if it is possible to do so immediately without violating 
						//capacity restrictions, returning true upon success and false if no 
						//space is currently available. When using a capacity-restricted deque, 
						//this method is generally preferable to the addLast method, which can fail 
						//to insert an element only by throwing an exception. 
						billableQ.offerLast(billable);
						
						billable.setIn_outgoing_queue(1L);
						getSession().saveOrUpdate(billable);
						//celcomAPI.markInQueue(mtsms.getId());//change at 11th March 2012 - I later realzed we still sending SMS twice!!!!!!
						
					}catch(Exception e){
						
						log(e);
					
					}
					
				}else{//putLast waits for queue to have empty space before adding new elements.	
					
					//We have a restriction so we have to wait until 
					//The queue has space in it to add an element.
					try {
						
						billableQ.putLast(billable);//if we've got a limit to the queue
						
						billable.setIn_outgoing_queue(1L);
						getSession().saveOrUpdate(billable);
						//celcomAPI.markInQueue(mtsms.getId());//change at 11th March 2012 - I fucking later realzed we still sending SMS twice!!!!!!
						
						//addedToqueue = true;
						
					} catch (InterruptedException e) {
			
						log(e);
					
					}catch(Exception e){
						
						log(e);
					
					}
				}
				
				increaseMT();
				
				
			}
			
			
			
			if(getSentMT()>0){
				
				watch.stop();
				
				logger.info("END!>>>>>>>>>>>>>>>>>>>>< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()/1000d+"")) + " seconds to log a queue of "+getSentMT()+" messages");
			   
				watch.reset();
				
				resetSentMT();
			}
			
			
			Thread.sleep(10);//Sleep 100 ms
		
		} catch (NullPointerException e) {
			
			log(e);
			try{
				rollback();
			}catch(Exception e3){}
		
		} catch (Exception e) {
			
			log(e);
			
			try{
			rollback();
			}catch(Exception e3){}
		
		}finally{
			
			try{
				commit();
			}catch(Exception e3){}
			
			try{
				close();
			}catch(Exception e3){}
						
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private List<Billable> getBillable(int limit) {
		try{
			Query qry =  getSession().createQuery("from Billable where in_outgoing_queue=0 AND (retry_count<=maxRetriesAllowed) AND resp_status_code is null AND price>0 AND  processed=0 order by priority asc");
			qry.setFirstResult(0);
			qry.setMaxResults(limit);
			return qry.list();
		}finally{
			//close();
		}
		
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


	/**
	 * Logs the exception
	 * @param e - java.lang.Exception
	 */
	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}

	public static void main(String[] args) {
		
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() { 
		    	
		    	System.out.println("SHUTTING DOWN!");
		    	BillingService.stopApp();
		    	
		    }
		});
		
		try {
			BillingService billingserv = new BillingService();
		//	billingserv.initWorkers();
			billingserv.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	protected static void stopApp() {
		System.out.println("Shutting down...");
		
		try{
			if(instance!=null){
				System.out.println("Shutting down...");
				instance.setRun(false);
				System.out.print("...");
				instance.waitForQueueToBecomeEmpty();
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

	private void waitForAllWorkersToFinish() {
			
		boolean finished = false;
		
		
		//First and foremost, let all threads die if they finish to process what they're processing currently.
		//We don't interrupt them still..
		for(HttpBillingWorker tw : httpSenderWorkers){
			if(tw.isRunning())
				tw.setRun(false);
		}
		
		//all unprocessed messages in queue are put back to the db.
		
		while(!finished){//Until all workers are idle or dead...
			
			idleWorkers = 0;
			
			for(HttpBillingWorker tw : httpSenderWorkers){
				
				if(tw.isRunning())
					tw.setRun(false);
				
				
				
				//might not be necessary because we already set run to false for each thread.
				//but in case we have an empty queue, then we add a poison pill that has id = -1 which forces the thread to run, then terminate
				//because we already set run to false.
				billableQ.addLast(new Billable());//poison pill...the threads will swallow it and surely die.. bwahahahaha!
				
				if(!tw.isBusy()){
					idleWorkers++;
				}
				
			}
			
			try {
				
				logger.info("workers: "+workers);
				logger.info("idleWorkers: "+idleWorkers);
				
				Thread.sleep(500);
			
			} catch (InterruptedException e) {
				
				log(e);
			
			}
			
			finished = workers==idleWorkers;
			
		}
		
		
		
		logger.info("We're shutting down, we put back any unprocessed message to the db queue so that they're picked next time we run..");
		//Now, if we have a big queue of unprocessed messages, we return them back to the db (or rather set the necessary flags
		for(Billable sms : billableQ){
			sms.setIn_outgoing_queue(0L);//and its now not in the queue
			sms.setProcessed(0L);//nope, we're not processing it
			logger.info("Returned to db: "+ sms.toString());
			sms = cmpbean.saveOrUpdate(sms);
		}
		//now all messages should be put back in quque
		
		
		
		billableQ.clear();//Nothing is useful in the queue now. Necessary? we will find out using test.. 
		logger.info("workers: "+workers);
		logger.info("idleWorkers: "+idleWorkers);
		
		
		
	}

	private void setRun(boolean b) {
		this.run = b;
		
	}

	private void waitForQueueToBecomeEmpty() {
		
		while(billableQ.size()>0){
			
			logger.info("MTProducer.mtMsgs.size() : "+billableQ.size());
			
			try {
				
				Thread.sleep(500);
				
			} catch (InterruptedException e) {
				
				log(e);
			
			}
			
		}
		
		logger.info("Queue is now empty, and all threads have been asked not to wait for elements in the queue!");
		
		notify();
		
	}

	public static void updateMessageInQueue(long cp_tx_id, BillingStatus billstatus) {
		getSession().beginTransaction();
		Query qry = getSession().createSQLQuery("UPDATE httptosend set priority=:priority, charged=:charged, billing_status=:billing_status WHERE CMP_TxID=:CMP_TxID ")
		.setParameter("priority", billstatus.equals(BillingStatus.SUCCESSFULLY_BILLED) ? 0 :  3)
		.setParameter("charged", billstatus.equals(BillingStatus.SUCCESSFULLY_BILLED) ? 1 :  0)
		.setParameter("billing_status", billstatus)
		.setParameter("CMP_TxID", cp_tx_id);
		int success = qry.executeUpdate();
		getSession().getTransaction().commit();
	}


/*
	*//**
	 * Gets connection object from a pool
	 * @return java.sql.Connection
	 * @throws InterruptedException
	 * @throws SQLException
	 *//*
	private Connection getConnFromDbPool() throws InterruptedException, SQLException{
		
		return ds.getConnection();
	}
	
	*//**
	 * Gets the connection.
	 * If it is not closed or null, return the existing connection object,
	 * else create one and return it
	 * @return java.sql.Connection object
	 * @throws InterruptedException 
	 * @throws SQLException 
	 *//*
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
		
	}*/
	
	
	
	public void myfinalize(){
		
		try{
			
			context.close();
		
		}catch(Exception e){
			
			log(e);
		
		}
		
		try{
			
			cm.shutdown();
		
		}catch(Exception e){
			
			log(e);
		
		}
		
		//if(ds!=null)
		//	ds.releaseConnectionPool();
		
		
		
	}
}
