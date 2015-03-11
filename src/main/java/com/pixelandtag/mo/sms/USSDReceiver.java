package com.pixelandtag.mo.sms;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.connections.ConnectionPool;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.subscription.dto.MediumType;
import com.pixelandtag.util.StopWatch;
import com.pixelandtag.web.beans.RequestObject;

/**
 * 
 * @author Timothy Mwangi Gikonyo
 * @since 25th February 2015
 * 
 * Servlet receives MO messages from Celcom and logs to the database..
 * /ussd
 *
 */
public class USSDReceiver extends HttpServlet {
	
	private Logger logger = Logger.getLogger(USSDReceiver.class);
	private StopWatch watch;
	private DataSource ds;
	private Context initContext;
	
	private byte[] OK_200 =  "200 OK".getBytes();
	private final String SERVER_TIMEZONE = "-05:00";
	private final String CLIENT_TIMEZONE = "+03:00";

	
	@EJB
	private CMPResourceBeanRemote cmpBean;
	
	@EJB
	private DatingServiceI datingBean;


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
			
			logger.error("\t\t\tUSSD YAY!!!>>>>> REQ from "+ip_addr+"  : paramName: "+paramName+ " value: "+value);
			
		}
		
		
		watch.start();
		
		PrintWriter pw = resp.getWriter();
		String response =""; 
		
		try{
			
			final RequestObject ro = new RequestObject(req);
			
			ro.setMediumType(MediumType.ussd);
			
			
			
			long messageID = -1;
			try{
				
				final MOSms moMessage = new MOSms(req);
				moMessage.setMediumType(MediumType.ussd);
				moMessage.setMt_Sent(response);
				messageID = datingBean.logMO(moMessage).getId();
				ro.setMessageId(messageID);
				
				logger.info("\n\n\n\n\n\t\t\t GENERATED MESSAGE ID:::)()()()()()()()(() "+messageID);
			}catch(Exception e){
				logger.error(e.getMessage(),e);
			}
			
			
			Person p = datingBean.getPerson(ro.getMsisdn());
			
			if(p==null || (p!=null && !p.getActive())){
				response = datingBean.processDating(ro);
			}
			
			
			if(response.equals("")){//if profile isn't complete, we try complete it
				PersonDatingProfile prof  = datingBean.getProfile(p);
				if(!prof.getProfileComplete())
				 response = datingBean.processDating(ro);
			}
			
			if(response.equals("")){//we assume they want to renew subscription
					
				response = cmpBean.processUSSD(ro);
			}
			
			pw.write(response);
			
		}catch(Exception e){
			pw.write("There was a problem processing your request. Kindly do try again minutes.");
			logger.error(e.getMessage(),e);
			try{
				pw.close();
			}catch(Exception ex){
				logger.error(ex.getMessage(),ex);
			}
			
		}finally{
			
			try{
				pw.close();
			}catch(Exception e){
				logger.error(e.getMessage(),e);
				//sOutStream.write("{\"status\": \"MO Request not understood\"}".getBytes());
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
			//	celcomAPI = new CelcomImpl(ds);
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
