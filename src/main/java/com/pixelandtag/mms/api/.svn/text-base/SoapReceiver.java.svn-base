package com.inmobia.mms.api;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

import com.inmobia.celcom.api.GenericMessage;
import com.inmobia.mms.api.SaajUtils;

public class SoapReceiver extends HttpServlet {
  private MessageFactory messageFactory;

  public void init() throws ServletException {
    try {
      // Initialize it to the default.
      messageFactory = MessageFactory.newInstance();
    }
    catch( SOAPException ex ) {
      throw new ServletException( "Unable to create message factory"
                                  + ex.getMessage() );
    }
  }

  public void doPost( HttpServletRequest req, HttpServletResponse resp )
      throws ServletException, IOException {
	  
	InputStream is =  null;
	OutputStream os = null;
    
	try {
      // Get all the headers from the HTTP request.
      MimeHeaders headers = SaajUtils.getHeaders( req );

      Iterator<MimeHeader> al = headers.getAllHeaders();
      
      while(al.hasNext()){
    	  MimeHeader mh = al.next();
    	  System.out.println(">>>>>>>>>>>>>"+mh.getName()+ " : "+ mh.getValue());
      }
      
      // Get the body of the HTTP request.
     is = req.getInputStream();
      
      /*File f=new File("C:\\Users\\Paul\\Desktop\\soap_construct.xml");
      OutputStream out=new FileOutputStream(f);
      byte buf[]=new byte[1024];
      int len;
      while((len=is.read(buf))>0)
      out.write(buf,0,len);
      out.close();
      is.close();*/
      
     /* String received = StreamUtils.convertStreamToString(is);
      
      System.out.println("received:   "+received);
      
      System.out.println("\n\n\n\n=============END OF OUTPRINT================\n\n\n\n");*/
      
      

      // Now internalize the contents of a HTTP request and
      // create a SOAPMessage
      SOAPMessage msg = messageFactory.createMessage( headers, is );
      SOAPMessage reply = null;

      // There are no replies in case of an OnewayListener.
      reply = onMessage( msg );

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

        os.flush();
        
      }
      else {
        resp.setStatus( HttpServletResponse.SC_NO_CONTENT );
      }
    }
    catch( Exception ex ) {
    	ex.printStackTrace();
      throw new ServletException( "Saaj POST failed " + ex.getMessage() );
    }finally{
    	if(os!=null)
    		os.close();
    	if(is!=null)
    		is.close();
    }
  }

  // This is the application code for handling the message.. Once the
  // message is received the application can retrieve the soap part, the
  // attachment part if there are any, or any other information from the
  // message.

  public SOAPMessage onMessage( SOAPMessage message) {
    System.out.println( "On message called in receiving servlet" );
    try {
      System.out.println( "Here's the message: " );
      message.writeTo( System.out );
      System.out.println( SaajUtils.getAttachmentReport( message ) );

      SOAPMessage msg = messageFactory.createMessage();
      SOAPPart soapPart = msg.getSOAPPart();
      SOAPEnvelope env = soapPart.getEnvelope();
      SOAPHeader header = msg.getSOAPHeader();
      SOAPBody body = env.getBody();
      header.addTextNode("1232");
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


  public void doGet( HttpServletRequest req, HttpServletResponse resp )
      throws ServletException, IOException {
    System.out.println( "### got the servlet..." );
    resp.getWriter().println( "hello you!" );
  }
}
