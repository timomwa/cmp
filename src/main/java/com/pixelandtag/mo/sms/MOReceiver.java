package com.pixelandtag.mo.sms;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.pixelandtag.api.CelcomHTTPAPI;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.api.sms.ProcessorResolverEJBI;
import com.pixelandtag.connections.ConnectionPool;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.util.StopWatch;

/**
 * 
 * @author Timothy Mwangi Gikonyo
 * @since 2nd February 2012
 * 
 * Servlet receives MO messages from Celcom and logs to the database..
 * /moreceiver
 *
 */
public class MOReceiver extends HttpServlet {
	
	private Logger logger = Logger.getLogger(MOReceiver.class);
	private StopWatch watch;
	private DataSource ds;
	private Context initContext;
	//private ConnectionPool connectionPool;
	private CelcomHTTPAPI celcomAPI;
	
	private byte[] OK_200 =  "200 OK".getBytes();
	private final String SERVER_TIMEZONE = "-05:00";
	private final String CLIENT_TIMEZONE = "+03:00";

	@EJB
	private CMPResourceBeanRemote cmpBean;
	
	@EJB
	private ProcessorResolverEJBI processorEJB;
	
	
	//private final int INITIAL_CONNECTIONS = 10;
	//private final int MAX_CONNECTIONS = 50;

	/**
	 * 
	 */
	private static final long serialVersionUID = 14512222156L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		
		ServletOutputStream sOutStream = null;
		
		
		Enumeration enums = req.getParameterNames();
		
		String paramName = "";
		String value  = "";
		
		while(enums.hasMoreElements()){
			
			paramName = (String) enums.nextElement();
			
			value = req.getParameter(paramName);
			
			String ip_addr = req.getRemoteAddr();
			
			logger.error("\t:::::: SMS REQ from "+ip_addr+"  : paramName: "+paramName+ " value: "+value);
			
			logger.error("NOT AN ERROR. CELCOM_MO_RECEIVER!: paramName: "+paramName+ " value: "+value);
			
		}
		
		
		watch.start();
		
		
		try{
			
			sOutStream = resp.getOutputStream();
			
			if(req.getParameter("test")!=null){
				sOutStream.write(("OK : Received : "+req.getParameter("test")).getBytes());
				sOutStream.close();
				return;
			}
			
			final MOSms moMessage = new MOSms(req,cmpBean.generateNextTxId());
			
			if(ds==null){
				init();
			}
			if(ds!=null){
				Connection conn = null;
				try{
					conn = ds.getConnection();//try retrieve a connection
					conn.setAutoCommit(true);//see if it's healthy. It would throw exception
				}catch(Exception e){
					logger.warn("Could not get a connection from datasource. We re-initialize DS");
					init();
				}finally{
					try{
						if(conn!=null)
							conn.close();
					}catch(Exception e){
					}
				}
			}
			
			//TODO - Do away with this receiver or modify it to use new
			
		//	processorEJB.logMO(moMessage);
			
			celcomAPI.setFr_tz(SERVER_TIMEZONE);
			celcomAPI.setTo_tz(CLIENT_TIMEZONE);
			
			logger.debug(moMessage.toString());
			
			watch.stop();
			
			logger.info(">< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to log the MO msg");
		      
			watch.reset();
			
			sOutStream.write(OK_200);
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			sOutStream.write("{\"status\": \"MO Request not understood\"}".getBytes());
		}finally{
			
			try{
			
				resp.sendError(HttpServletResponse.SC_OK);
				
				celcomAPI.closeConnectionIfNecessary();
				
				sOutStream.close();
				
			}catch(Exception e){
				sOutStream.write("{\"status\": \"MO Request not understood\"}".getBytes());
			}
			
		}
		
		
	}

	@Override
	public void destroy() {
		
		logger.info("CELCOM_MO_RECEIVER: in Destroy");
		
		try {
			
			if(initContext!=null)
				initContext.close();
		
		} catch (NamingException e) {
			logger.error(e.getMessage(),e);
		}
	
	}

	@Override
	public void init() throws ServletException {
		
		watch = new StopWatch();
		
		watch.start();
		
		
		try {
			
			initContext = new InitialContext();
			
			ds = (DataSource)initContext.lookup("java:/cmpDS");
			
			try {
//				//celcomAPI = new CelcomImpl("jdbc:mysql://db/pixeland_content360?user=pixeland_content&password=D13@pixel&Tag","tasdf");
				celcomAPI = new CelcomImpl(ds);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			
		} catch (NamingException e) {
			
			logger.error(e.getMessage(),e);
		
		}
		
		/*int vendor = DriverUtilities.MYSQL;
	    String driver = DriverUtilities.getDriver(vendor);
	    String host = "db";
	    String dbName = CelcomHTTPAPI.DATABASE;
	    String url = DriverUtilities.makeURL(host, dbName, vendor);
	    String username = "root";
	    String password = "";
	    
	    logger.info(url);
	    
	    try {
	    	
	      connectionPool =
	        new ConnectionPool(driver, url, username, password,INITIAL_CONNECTIONS,MAX_CONNECTIONS,true);
	      
	      watch.stop();
	      
	      logger.info(">< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to create the connection pool");
	      
	      watch.reset();
	      
	      
	      celcomAPI = new CelcomImpl(connectionPool);
	      
	    
	    } catch(SQLException sqle) {
	     
	    	logger.error("Error making pool: " + sqle);
	      
	    	connectionPool = null;
	    
	    }*/

	    
	}
	
	

	

}
