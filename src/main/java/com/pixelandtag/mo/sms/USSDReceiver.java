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
import javax.persistence.Transient;
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
import com.pixelandtag.cmp.ejb.LocationBeanI;
import com.pixelandtag.cmp.entities.IncomingSMS;
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
	
	//private static final transient Logger logger = Logger.getLogger(USSDReceiver.class);
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
	
	@EJB
	private LocationBeanI locationBean;


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
			
			System.out.println("\t:::::: REQ from "+ip_addr+"  : paramName: "+paramName+ " value: "+value);
			
		}
		
		
		watch.start();
		
		PrintWriter pw = resp.getWriter();
		String response =""; 
		
		try{
			String tx_id = cmpBean.generateNextTxId();
			final RequestObject ro = new RequestObject(req,tx_id,false);
			
			ro.setMediumType(MediumType.ussd);
			
			long messageID = -1;
			
			final MOSms moMessage = new MOSms(req,tx_id);
			
			try{
				moMessage.setMediumType(MediumType.ussd);
				moMessage.setMt_Sent(response);
				
				IncomingSMS incomingsms = new IncomingSMS();
				incomingsms.setBilling_status(moMessage.getBillingStatus());
				incomingsms.setCmp_tx_id(moMessage.getCmp_tx_id());
				incomingsms.setEvent_type(moMessage.getEventType()!=null ? moMessage.getEventType().toString() : "" );
				incomingsms.setIsSubscription(Boolean.FALSE);
				messageID = datingBean.logMO(incomingsms).getId();
				ro.setMessageId(messageID);
				
			}catch(Exception e){
				e.printStackTrace();
			}
			
			
			Person p = datingBean.getPerson(ro.getMsisdn(),ro.getOpco());
			
			locationBean.updateSubscriberLocation(ro);
			
			if((response==null || response.isEmpty()) && (p==null || (p!=null && !p.getActive()))){
				response = datingBean.processDating(ro);
			}
			
			
			if(response==null || response.equals("")){//if profile isn't complete, we try complete it
				PersonDatingProfile prof  = datingBean.getProfile(p);
				if(!prof.getProfileComplete())
				 response = datingBean.processDating(ro);
			}
			
			if(response==null || response.equals("")){//we assume they want to renew subscription
					
				response = cmpBean.processUSSD(ro);
			}

			try{
				
				moMessage.setMt_Sent(response);
				//datingBean.updateMO(response,messageID); TODO - re-do the updateMO message
				ro.setMessageId(messageID);
				
			}catch(Exception e){
				e.printStackTrace();
			}
			
			
			pw.write(response);
			
		}catch(Exception e){
			//pw.write("");
			e.printStackTrace();
			try{
				pw.close();
			}catch(Exception ex){
				e.printStackTrace();
			}
			
		}finally{
			
			try{
				pw.close();
			}catch(Exception e){
				e.printStackTrace();
				//sOutStream.write("{\"status\": \"MO Request not understood\"}".getBytes());
			}
			
		}
		
		
	}

	@Override
	public void destroy() {
		
		System.out.println("CELCOM_MO_RECEIVER: in Destroy");
		
		try {
			
			if(initContext!=null)
				initContext.close();
		
		} catch (NamingException e) {
			e.printStackTrace();
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
				e.printStackTrace();
			}
			
		} catch (NamingException e) {
			
			e.printStackTrace();
		
		}
		
		/*int vendor = DriverUtilities.MYSQL;
	    String driver = DriverUtilities.getDriver(vendor);
	    String host = "db";
	    String dbName = CelcomHTTPAPI.DATABASE;
	    String url = DriverUtilities.makeURL(host, dbName, vendor);
	    String username = "root";
	    String password = "";
	    
	    System.out.println(url);
	    
	    try {
	    	
	      connectionPool =
	        new ConnectionPool(driver, url, username, password,INITIAL_CONNECTIONS,MAX_CONNECTIONS,true);
	      
	      watch.stop();
	      
	      System.out.println(">< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to create the connection pool");
	      
	      watch.reset();
	      
	      
	      celcomAPI = new CelcomImpl(connectionPool);
	      
	    
	    } catch(SQLException sqle) {
	     
	    	logger.error("Error making pool: " + sqle);
	      
	    	connectionPool = null;
	    
	    }*/

	    
	}
	
	

	

}
