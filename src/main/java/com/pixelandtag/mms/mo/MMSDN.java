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
import com.pixelandtag.mms.api.SoapMMSDN;
import com.pixelandtag.mms.api.StreamUtils;
import com.pixelandtag.mms.apiImpl.MMSApiImpl;
import com.pixelandtag.util.StopWatch;

public class MMSDN extends HttpServlet {

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
		
		InputStream is =  null;
		OutputStream os = null;
		
		watch.start();
		
		try{
			
			sOutStream = resp.getOutputStream();
			
			if(req.getParameter("test")!=null){
				sOutStream = resp.getOutputStream();
				sOutStream.write(("MMSM222: BrodaMeni: "+req.getParameter("test")).getBytes());
				sOutStream.close();
				return;
			}
			
			
		    
			try {
		      // Get all the headers from the HTTP request.
		      MimeHeaders headers = SaajUtils.getHeaders( req );

		      Iterator<MimeHeader> al = headers.getAllHeaders();
		      
		      while(al.hasNext()){
		    	  MimeHeader mh = al.next();
		    	  logger.info("GUGAMUGA_6>>>>>>>>>>>>>"+mh.getName()+ " : "+ mh.getValue());
		      }
		      
		      // Get the body of the HTTP request.
		     is = req.getInputStream();
		     
		     String received = StreamUtils.convertStreamToString(is);
		     
		     SoapMMSDN dn = new SoapMMSDN(received);
		     
		     logger.info("GUGAMUGA_7 "+dn.toString());
		     
		      
		      /*File f=new File("/root/jboss/log/mms_celcom_dn_"+System.currentTimeMillis()+".log");
		      OutputStream out=new FileOutputStream(f);
		      byte buf[]=new byte[1024];
		      int len;
		      while((len=is.read(buf))>0)
		      out.write(buf,0,len);
		      out.close();
		      //is.close();
		      
		      String received = StreamUtils.convertStreamToString(is);
		      
		      System.out.println("received:   "+received);
		      
		      System.out.println("\n\n\n\n=============END OF OUTPRINT================\n\n\n\n");*/
		     
		      // Now internalize the contents of a HTTP request and
		      // create a SOAPMessage
		     
		     
		      mm7api.setFr_tz(SERVER_TIMEZONE);
		      
		      mm7api.setTo_tz(CLIENT_TIMEZONE);
		      
		      mm7api.acknowledge(dn);
		      
		      
		      SOAPMessage reply = null;

		      // There are no replies in case of an OnewayListener.
		      reply = onMessage( dn.getTransactionID());

		      if( reply != null ) {

		        // Need to saveChanges 'cos we're going to use the
		        // MimeHeaders to set HTTP response information. These
		        // MimeHeaders are generated as part of the save.

		        if( reply.saveRequired() ) {
		          reply.saveChanges();
		        }

		        resp.setStatus( HttpServletResponse.SC_OK );

		        SaajUtils.putHeaders( reply.getMimeHeaders(), resp );

		        // Write out the message on the response stream.
		        os = resp.getOutputStream();
		        
		        reply.writeTo( os );

		        
		        
		      }
		      else {
		    	  
		        resp.setStatus( HttpServletResponse.SC_NO_CONTENT );
		      
		      }
		    
			}catch( Exception ex ) {
		    	
		    	logger.error(ex.getMessage(),ex);
		     
		    	//throw new ServletException( "Saaj POST failed " + ex.getMessage() );
		    
		    }finally{
		    	
		    	if(os!=null)
		    		try{
		    			os.flush();
		    		}catch(Exception e){}
		    	
		    }
		    
		    
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			try{
				if(os!=null)
					os.close();
			}catch(Exception e){
				//DO NOT CARE!
			}
			
			try{
				if(is!=null)
					is.close();
			}catch(Exception e){
				//DO NOT CARE!
			}
			
			try{
				resp.sendError(HttpServletResponse.SC_OK);
			}catch(Exception e){
				//DO NOT CARE!
			}
			
			try{
				sOutStream.close();
			}catch(Exception e){
				//DO NOT CARE!
			}
			
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
			
			ds = (DataSource)initContext.lookup("java:/CELCOM_MMS_DN_RECEIVER_ONLY");

			messageFactory = MessageFactory.newInstance();
			
			mm7api = new MMSApiImpl(ds);
		  
		 }catch( SOAPException ex ) {
		    
			 throw new ServletException( "Unable to create message factory " + ex.getMessage() );

		 } catch (Exception e) {
			 
			 throw new ServletException( "The datasource is null! I can't live like this!!" + e.getMessage() );
		
		 }
		 
		 
		
	}
	
	
	
	
	public SOAPMessage onMessage( String txId) {
	    logger.info( "On message called in receiving servlet" );
	    try {
	   

	      SOAPMessage msg = messageFactory.createMessage();
	      SOAPPart soapPart = msg.getSOAPPart();
	      SOAPEnvelope env = soapPart.getEnvelope();
	      SOAPHeader header = msg.getSOAPHeader();
	      SOAPBody body = env.getBody();
	      header.addTextNode(txId);
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
