package com.inmobia.celcom.mo.sms;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

import com.inmobia.celcom.api.CelcomHTTPAPI;
import com.inmobia.celcom.api.CelcomImpl;
import com.inmobia.celcom.entities.Notification;
import com.inmobia.util.StopWatch;
/**
 * Delivery notification receiver.
 * 
 * URL: /dn
 * @author Timothy Gikonyo
 *
 */
public class DNReceiver extends HttpServlet {
	
	
	private Logger logger = Logger.getLogger(DNReceiver.class);
	private StopWatch watch;
	//private ConnectionPool connectionPool;
	private CelcomHTTPAPI celcomAPI;
	private DataSource ds;
	private Context initContext;
	
	private final byte[] OK_200 =  "200 OK".getBytes();
	private final String SERVER_TIMEZONE = "+08:00";
	private final String CLIENT_TIMEZONE = "+08:00";

	//private final int INITIAL_CONNECTIONS = 10;
	//private final int MAX_CONNECTIONS = 50;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1467625454634L;
	
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		
		ServletOutputStream sOutStream = null;
		
		
		
		try{
			
			sOutStream = resp.getOutputStream();
			if(req.getParameter("test")!=null){
				sOutStream.write(("OK : Received : "+req.getParameter("test")).getBytes());
				sOutStream.close();
				return;
				
			}
			
			watch.start();
			
			final Notification notification = new Notification(req);
			
			if(ds==null){
				init();
			}
			if(ds!=null){
				Connection conn = null;
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				try{
					conn = ds.getConnection();//try retrieve a connection
					pstmt = conn.prepareStatement("SELECT 'test'");//see if it's healthy. It would throw exception if it isn't
					rs = pstmt.executeQuery();
					if(rs.next())
						rs.getString(1);
					
				}catch(Exception e){
					logger.warn("Could not get a connection from datasource. We re-initialize DS");
					init();
				}finally{
					try{
						rs.close();
					}catch(Exception e){
					}
					try{
						pstmt.close();
					}catch(Exception e){
					}
					try{
						if(conn!=null)
							conn.close();
					}catch(Exception e){
					}
				}
			}
			
			celcomAPI.setFr_tz(SERVER_TIMEZONE);
			
			celcomAPI.setTo_tz(CLIENT_TIMEZONE);
			
			celcomAPI.acknowledgeDN(notification);
			
			watch.stop();
			logger.info(">< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to log the Notification msg");
		   
			
			watch.reset();
			watch.start();
			celcomAPI.flagMMSIfAny(notification);
			watch.stop();
			logger.info(">< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to flag any existing MMS depending on a DLR");
			
			
			watch.reset();
			watch.start();
			celcomAPI.updateSMSStatLog(notification);
			watch.stop();
			logger.info(">< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to update the SMSStatLog table");
		   
			
			
			
			watch.reset();
			watch.start();
			celcomAPI.processLuckyDip(notification);
			watch.stop();
			logger.info(">< . >< . >< . >< . >< . it took "+(Double.parseDouble(watch.elapsedMillis()+"")/1000d) + " seconds to process lucky dip process on DLR");
			
			
			logger.debug(notification.toString());
			
			   
			watch.reset();
		
			sOutStream.write(OK_200);
		
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{

			//resp.sendError(HttpServletResponse.SC_OK);
			
			if(sOutStream!=null)
				sOutStream.close();
			
		}
		
		
	}

	@Override
	public void destroy() {
		
		logger.info("CELCOM_DN_RECEIVER: in Destroy");
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
		
		try {
			
			initContext = new InitialContext();
			
			ds = (DataSource)initContext.lookup("java:/CELCOM_DN_RECEIVER_ONLY");
			
			try {
				celcomAPI = new CelcomImpl(ds);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			
		} catch (NamingException e) {
			
			logger.error(e.getMessage(),e);
		
		}
		

	}
	

	
	

}
