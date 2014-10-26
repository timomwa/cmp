package com.pixelandtag.mms.mo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.log4j.Logger;

import com.pixelandtag.api.GenericMessage;
import com.pixelandtag.mms.api.MM7Api;
import com.pixelandtag.mms.api.MM7DeliveryReport;
import com.pixelandtag.mms.api.SaajUtils;
import com.pixelandtag.mms.apiImpl.MMSApiImpl;
import com.pixelandtag.util.StopWatch;

public class MMSMO extends HttpServlet {

	Logger logger = Logger.getLogger(MMSMO.class);
	StopWatch watch;
	private byte[] OK_200 =  "200 OK".getBytes();
	private final String SERVER_TIMEZONE = "+08:00";
	private final String CLIENT_TIMEZONE = "+08:00";
	private MessageFactory messageFactory;
	private MM7Api mm7api;
	private DataSource ds;
	private Context initContext;
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1232044084L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ServletOutputStream sOutStream = null;
		
		
		watch.start();
		
		try{
			
			sOutStream = resp.getOutputStream();
			
			if(req.getParameter("test")!=null){
				sOutStream = resp.getOutputStream();
				sOutStream.write(("MMSM222: BrodaMeni: "+req.getParameter("test")).getBytes());
				sOutStream.close();
				return;
			}
			
			
		    
		    
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			resp.sendError(HttpServletResponse.SC_OK);
			
			sOutStream.close();
			
		}
		
	}

	@Override
	public void destroy() {
		
		logger.info("CELCOM_MMS_DN_RECEIVER: in Destroy");
		
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
			
			ds = (DataSource)initContext.lookup("java:/CELCOM_MMS_MO_RECEIVER_ONLY");

			messageFactory = MessageFactory.newInstance();
			
			mm7api = new MMSApiImpl(ds);
		  
		 }catch( SOAPException ex ) {
		    
			 throw new ServletException( "Unable to create message factory " + ex.getMessage() );

		 } catch (Exception e) {
			 
			 throw new ServletException( "The datasource is null! I can't live like this!!" + e.getMessage() );
		
		 }
		 
		 
		
	}
	
	
	
	
	public SOAPMessage onMessage( SOAPMessage message, MM7DeliveryReport report) {
	    logger.info( "On message called in receiving servlet" );
	    try {
	    logger.info( "Here's the message: " );
	      message.writeTo( System.out );
	      logger.info( SaajUtils.getAttachmentReport( message ) );

	      SOAPMessage msg = messageFactory.createMessage();
	      SOAPPart soapPart = msg.getSOAPPart();
	      SOAPEnvelope env = soapPart.getEnvelope();
	      SOAPHeader header = msg.getSOAPHeader();
	      SOAPBody body = env.getBody();
	      header.addTextNode(report.getCMP_Txid());
	      SOAPElement elem = body.addChildElement( env.createName( "SubmitRsp", "mm7", "http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-2" ) );//.addTextNode( "This is a response" );
	      SOAPElement mm7Version = elem.addChildElement( env.createName( "MM7Version", "mm7", "http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-2") );
	      mm7Version.addTextNode(GenericMessage.MM7_VERSION);
	      SOAPElement status = elem.addChildElement( env.createName( GenericMessage.STATUS_NODE_NAME ) );
	      status.addChildElement( env.createName( GenericMessage.STATUS_CODE_NODE_NAME )  ).addTextNode( GenericMessage.SUCCESS_STATUS_CODE );
	      status.addChildElement( env.createName( GenericMessage.STATUS_TEXT_NODE_NAME )  ).addTextNode( GenericMessage.SUCCESS_NODE_NAME );
	      
	      return msg;

	    }
	    catch( Exception e ) {
	      e.printStackTrace();
	      return null;
	    }
	  }
	
	

}
