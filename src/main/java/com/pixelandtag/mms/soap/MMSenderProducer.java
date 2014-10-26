package com.pixelandtag.mms.soap;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import com.pixelandtag.mms.api.MM7_Submit_req;
import com.pixelandtag.mms.api.MMS;
import com.pixelandtag.util.StopWatch;

public class MMSenderProducer {

	/**
	 * @param args
	 * @throws MessagingException 
	 * @throws IOException 
	 * @throws SOAPException 
	 */
	public static void main(String[] args) throws SOAPException, IOException, MessagingException {
		
		String endpointURL = "http://203.82.66.118:5777/mm7/mm7tomms.sh";
		
		MM7_Submit_req request = new MM7_Submit_req(endpointURL){};
		
		MMS mms = new MMS();
		//add properties of the mms
		//mms.setMms_text("A test msisdn");
		//mms.setContent_path("http://m.inmobia.com/web");
		
		SOAPMessage response  = request.submit(mms);
		
		//get response, act accordingly
		
	}
	
	
	
	
	public File resize(File file) {
		   
		
		String endPoint;
		SOAPConnection connection;
		try {
			connection = SOAPConnectionFactory.newInstance().createConnection();
		
		   SOAPMessage message = MessageFactory.newInstance().createMessage();
		   SOAPPart part = message.getSOAPPart();
		   SOAPEnvelope envelope = part.getEnvelope();
		   SOAPBody body = envelope.getBody();
		   SOAPBodyElement operation = body.addBodyElement(
		       envelope.createName("resize", "ps", "http://example.com"));
		   DataHandler dh = new DataHandler(new FileDataSource(file));
		   AttachmentPart attachment = message.createAttachmentPart(dh);
		   SOAPElement source  = operation.addChildElement("source","");
		   SOAPElement percent = operation.addChildElement("percent","");
		   message.addAttachmentPart(attachment);
		   source.addAttribute(envelope.createName("href"), "cid:" + attachment.getContentId());
		   percent.addTextNode("20");
		   
		   
		   message.writeTo(System.out);
		   System.out.println();

		   /*SOAPMessage result = connection.call(message,endPoint);
		   part = result.getSOAPPart();
		   envelope = part.getEnvelope();
		   body = envelope.getBody();
		   if(!body.hasFault())  {
		      Iterator iterator = result.getAttachments();
		      if(iterator.hasNext()) {
		         dh = ((AttachmentPart)iterator.next()).getDataHandler();
		         String fname = dh.getName();
		         if (null != fname) return new File(fname);
		      }
		   }*/
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   return null;
		}

}
