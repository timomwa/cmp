package com.pixelandtag.mms.soap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.codec.binary.Base64;

import com.pixelandtag.mms.api.ServiceCode;



public class Test3 {

	private static final String TxId = "505";
	private static final String MM7_VERSION = "5.3.0";
	private static final String VASPID = "inmobia";
	private static final String VASID = "inmobia";
	private static final String SHORTCODE = "23355";
	private static final String password = "inmobia123";
	private static final String SERVICE_CODE = ServiceCode.RM0.getCode();
	private static final String LINKED_ID = "Linked";
	private static  String MSISDN = "0193685271";
	private static final String EARLIEST_DELIVERY_TIME = "2006-11-09T13:01:04+03:00";
	private static final String EXPIRY_DATE = "2007-11-10T13:01:04+03:00";
	private static final String DELIVERY_REPORT = "false";
	private static final String READ_REPLY = "false";
	private static final String SUBJECT = "TriviaMMS";
	private static final String DISTRIBUTION_INDICATION = "false";
	private static String contentPath = "C:/Users/Paul/Desktop/MMS Pis/mi_soap.jpg";//"/home/inmobia/jobs/celcom/mms/imgs/mi_soap.jpg";
	private static final String MMS_TEXT = "MMS First Text";
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		if(args!=null){
			if(args.length>0){
				contentPath = args[0];
				if(args[1]!=null)
					MSISDN = args[1];
			}
				
		}
		java.net.URL endpoint = new URL(
		  "http://203.82.66.118:5777/mms/mm7tomms.sh?CP_UserId=inmobia&CP_Password=inmobia123");
		
		try {
			
			//SOAP Message created
			MessageFactory factory = MessageFactory.newInstance();
			SOAPMessage message = factory.createMessage();
			
			//MimeHeaders mimeHeader = message.getMimeHeaders();  
			//change header's attribute  
			//mimeHeader.setHeader("SOAPAction", "http://my.organization.org/webservices/WSSERVICE");  
			  
			//if you want to add new header's attribute use:   
			//mimeHeader.addHeader(action, value)
			
			/*MimeHeaders mimeHeader = message.getMimeHeaders();  
			  
			//change header's attribute  
			mimeHeader.setHeader("SOAPAction", "http://my.organization.org/webservices/WSSERVICE");  
			  
			//if you want to add new header's attribute use:   
			mimeHeader.addHeader("Content-Type", "multipart/related");*/  
			
			
			//part.setContentId("t");
			//part.setMimeHeader("g", "h");
			//MimeHeaders mh = message.getMimeHeaders();
			//message.addAttachmentPart(part);
			
			//.setHeader("", value)
			//mh.setHeader("t", "t");
			
		//	InternetHeaders someImageHeaderso = new InternetHeaders();
			//someImageHeaderso.setHeader("start","<mm7_msg>");
			//message.setContentDescription("desc");
			message.setProperty(SOAPMessage.CHARACTER_SET_ENCODING,"utf-8");
			message.setProperty("start","<mm7_msg>");
			message.setProperty("Content-Length","nnn");
			//message.setProperty("SOAPAction","");
			
			message.setProperty(SOAPMessage.WRITE_XML_DECLARATION,"true");
			
			
			MimeHeaders headers = message.getMimeHeaders();  
	        headers.addHeader("SOAPAction", "\"\"");  
	        
			//Now accessing the elements of the soap message
			SOAPPart soapPart = message.getSOAPPart(); 
			
			String authorization = "inmobia123";//toBASE64("inmobia123", 0);
			soapPart.addMimeHeader("Authorization",
					"Basic " + authorization.trim());
			
			soapPart.addMimeHeader("Host", "204.51.86.89");
			
			soapPart.addMimeHeader("Content-Type", "multipart/related");
			
			
			soapPart.addMimeHeader("type", "type=text/xml; start=\"<mm7_msg>\"");
			
			soapPart.addMimeHeader("\n\n\n\nContent-Length ", "xxx");
			
			soapPart.addMimeHeader("SoapAction:",  "\"\"");
			
			
			//soapPart.addMimeHeader("SOAPAction", "\"\"");
			/*soapPart.addMimeHeader("Content-Transfer-Encoding", "binary");
			soapPart.addMimeHeader("type", "application/smil");*/
			
			//Get the envelope
			SOAPEnvelope envelope = soapPart.getEnvelope(); 
			soapPart.setContentId("<mm7_msg>");
			
			
			//You can now use the getHeader and getBody methods 
			//of envelope to retrieve its empty SOAPHeader and SOAPBody objects.
			SOAPHeader header = envelope.getHeader();
			SOAPBody body = envelope.getBody();
			
			
		
			
			//Other ways of getting the body and header
			//header = message.getSOAPHeader();
			//body = message.getSOAPBody();
			
			
			//Deleting a node.
			//header.detachNode(); 
			SOAPFactory soapFactory = SOAPFactory.newInstance();
			
			
			
			Name headerName2 = soapFactory.createName("b1");
			  SOAPHeader header2 = envelope.getHeader();
			  header2.addAttribute(headerName2,"1s");
			 
			//================CONSTRUCTING HEADER
			Name headerName = soapFactory.createName("TransactionID",
	        		  "mm7", "http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-2");
			 
			SOAPHeaderElement headerElement = header.addHeaderElement(headerName); 
	        headerElement.setTextContent("TID."+TxId);
	        headerElement.setMustUnderstand(true);
	        //===============HEADER DONE======================
	        
	        
	        
	        
	        //===============CONSTRUCTING BODY==============
	        Name bodyName = soapFactory.createName("SubmitReq");
	        SOAPBodyElement bodyElement = body.addBodyElement(bodyName);
	        Name attributeName = envelope.createName("xmlsn");//change name to proper to xmls, though it does not print out
	        bodyElement.addAttribute(attributeName, "http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-2");
	        
	        
	        
	        
	        
	        
	        bodyName = soapFactory.createName("MM7Version");
	        SOAPElement MM7Version =  bodyElement.addChildElement(bodyName);
	        MM7Version.setTextContent(MM7_VERSION);
	        
	        bodyName = soapFactory.createName("SenderIdentification");
	        SOAPElement SenderIdentification =  bodyElement.addChildElement(bodyName);
	        
	        bodyName = soapFactory.createName("SenderAddress");
	        SOAPElement senderAddress =  SenderIdentification.addChildElement(bodyName);
	        
	        bodyName = soapFactory.createName("ShortCode");
	        SOAPElement shortCode =  senderAddress.addChildElement(bodyName);
	        shortCode.setTextContent(SHORTCODE);
	        
	        
	        bodyName = soapFactory.createName("Recipients");
	        SOAPElement recipients =  bodyElement.addChildElement(bodyName);
	       
	        
	        bodyName = soapFactory.createName("To");
	        SOAPElement to =  recipients.addChildElement(bodyName);
	        
	        
	        bodyName = soapFactory.createName("Number");
	        SOAPElement number =  to.addChildElement(bodyName);
	        number.setTextContent(MSISDN);
	        
	        
	        bodyName = soapFactory.createName("VASPID");
	        SOAPElement vaspID =  SenderIdentification.addChildElement(bodyName);
	        vaspID.setTextContent(VASPID);
	        
	        bodyName = soapFactory.createName("VASID");
	        SOAPElement vasID =  SenderIdentification.addChildElement(bodyName);
	        vasID.setTextContent(VASID);
	        
	        
	        bodyName = soapFactory.createName("ServiceCode");
	        SOAPElement serviceCode =  bodyElement.addChildElement(bodyName);
	        serviceCode.setTextContent(SERVICE_CODE);
	        
	        
	        bodyName = soapFactory.createName("LinkedID");
	        SOAPElement linkedID =  bodyElement.addChildElement(bodyName);
	        linkedID.setTextContent(LINKED_ID);
	        
	        
	        bodyName = soapFactory.createName("MessageClass");
	        SOAPElement messageClass =  bodyElement.addChildElement(bodyName);
	        messageClass.setTextContent(MessageClass.Personal.toString());
	        
	        
	        bodyName = soapFactory.createName("EarliestDeliveryTime");
	        SOAPElement earliestDeliveryTime =  bodyElement.addChildElement(bodyName);
	        earliestDeliveryTime.setTextContent(EARLIEST_DELIVERY_TIME);
	        
	        
	        bodyName = soapFactory.createName("ExpiryDate");
	        SOAPElement expiryDate =  bodyElement.addChildElement(bodyName);
	        expiryDate.setTextContent(EXPIRY_DATE);
	        
	        
	        bodyName = soapFactory.createName("DeliveryReport");
	        SOAPElement deliveryReport =  bodyElement.addChildElement(bodyName);
	        deliveryReport.setTextContent(DELIVERY_REPORT);
	        
	        bodyName = soapFactory.createName("ReadReply");
	        SOAPElement readReply =  bodyElement.addChildElement(bodyName);
	        readReply.setTextContent(READ_REPLY);
	        
	        
	        bodyName = soapFactory.createName("Priority");
	        SOAPElement priority =  bodyElement.addChildElement(bodyName);
	        priority.setTextContent(Priority.Normal.toString());
	        
	        
	        bodyName = soapFactory.createName("Subject");
	        SOAPElement subject =  bodyElement.addChildElement(bodyName);
	        subject.setTextContent(SUBJECT);
	        
	        
	        bodyName = soapFactory.createName("ChargedParty");
	        SOAPElement chargedParty =  bodyElement.addChildElement(bodyName);
	        chargedParty.setTextContent(ChargedParty.Recipient.toString());
	        
	        
	        bodyName = soapFactory.createName("DistributionIndicator");
	        SOAPElement distributionIndicator =  bodyElement.addChildElement(bodyName);
	        distributionIndicator.setTextContent(DISTRIBUTION_INDICATION);
	        
	        
	        bodyName = soapFactory.createName("Content");
	        SOAPElement content =  bodyElement.addChildElement(bodyName);
	        
	        attributeName = envelope.createName("href");//change name to proper to xmls, though it does not print out
	        SOAPElement attr = content.addAttribute(attributeName, "cid:A0");
	        
	        attributeName = envelope.createName("allowAdaptations");//change name to proper to xmls, though it does not print out
	        attr = content.addAttribute(attributeName, "false");
	        //=====================BODY DONE================
	        
	        
	        
	        //=================create attachment===========
	        MimeMultipart aMultiPart = new MimeMultipart("related");
	        
	        // First text
	        InternetHeaders someText1Headers = new InternetHeaders();
			someText1Headers.addHeader("Content-Type", "text/plain; charset=utf-8");
			someText1Headers.addHeader("Content-ID", "");
			someText1Headers.addHeader("Content-Transfer-Encoding", "7bit");
			someText1Headers.addHeader("Content-Disposition", "attachment");// filename=MMtext0.txt
			MimeBodyPart aTextPart1 = new MimeBodyPart(someText1Headers,MMS_TEXT.getBytes("UTF-8"));
			aMultiPart.addBodyPart(aTextPart1);
	        
	        
			InternetHeaders someImageHeaders = new InternetHeaders();
			someImageHeaders.addHeader("Content-Type", "image/jpeg");
			someImageHeaders.addHeader("Content-Transfer-Encoding", "base64");
			someImageHeaders.addHeader("Content-Disposition", "attachment");// filename=MMtext0.txt

			
			
			String anImageB64 = toBASE64(contentPath, 1);
			
			
			someImageHeaders.addHeader("Content-Length",
					"" + anImageB64.length());// filename=MMtext0.txt

			MimeBodyPart anImagePart = new MimeBodyPart(someImageHeaders,
					anImageB64.getBytes("UTF-8"));
			
			//anImagePart.setContentID("A0");

			someImageHeaders
					.addHeader("Content-Id", "A0");
			

			aMultiPart.addBodyPart(anImagePart);

			AttachmentPart anAttachment = message.createAttachmentPart(
					aMultiPart, aMultiPart.getContentType());
			message.addAttachmentPart(anAttachment);
			//anAttachment.setContentId("A10");

			
			
			message.saveChanges();
			/*SOAPElement source = bodyElement.addChildElement("Content");
			// source.addAttribute(envelope.createName("allowAdaptations"),
			// "true");
			source.addAttribute(envelope.createName("href"), "cid:"
					+ anImagePart.getContentID());*/
	       // AttachmentPart attachment = message.createAttachmentPart(); 
	        
	        
	        //Simple attach
	       /* String stringContent = "Update address for Sunny Skies " + 
	        "Inc., to 10 Upbeat Street, Pleasant Grove, CA 95439";

	      attachment.setContent(stringContent, "application/smil");
	      attachment.setContentId("update_address");

	      message.addAttachmentPart(attachment); */
	        
	        
	        /*URL url = new URL("http://www.petermak.nl/webalbum_peter_mak_aannemersbedrijf011002.jpg");
	        DataHandler dataHandler = new DataHandler(url);
	        AttachmentPart attachment1 = 
	          message.createAttachmentPart(dataHandler);
	       // attachment.setContent(stringContent, "application/smil");
	        attachment1.setContentId("attached_image");

	        message.addAttachmentPart(attachment1); 
	        
	        
	        URL url2 = new URL("file:///C:\\Users\\Paul\\Desktop\\MMS Pis\\deeply_800x600.jpg");
            dataHandler = new DataHandler(url2);
            AttachmentPart attachment2 = message.createAttachmentPart(
                        dataHandler);
            attachment2.setContentId("attached_image2");

            message.addAttachmentPart(attachment2);*/
	        
	        
	       
	        
	        
	        
	        
			MimeHeaders mimeH = message.getMimeHeaders();
			mimeH.addHeader("Host", "204.51.86.89");
			mimeH.addHeader("Authorization", "Basic "+password);
			mimeH.addHeader("SOAPAction", "");
			//mimeH.addHeader("Content-Length","");
			//mimeH.removeHeader("Accept");
			
			Iterator<MimeHeader> mh = mimeH.getAllHeaders();
			
			
			int i = 0;
			while(mh.hasNext()){
				MimeHeader mimH = mh.next();
				System.out.println(i+". "+mimH.getName() + " : " +mimH.getValue());
				i++;
			}
	        
	        
	        System.out.println("\nREQUEST:\n");
			 message.writeTo(System.out);
		     System.out.println();
	       
		     
		    SOAPConnectionFactory soapConnectionFactory = 
	        	  SOAPConnectionFactory.newInstance(); 
		    
	        SOAPConnection connection = 
	        	  soapConnectionFactory.createConnection(); 
	        
	        
	        SOAPMessage response = connection.call(message, endpoint); 
	         
	         
			 System.out.println("\nRESPONSE:\n");
			 response.writeTo(System.out);
		     System.out.println();
		     
		     connection.close();
		     
			
		
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static String getXMLDate(){
		java.util.Date date= new java.util.Date();
		return (new Timestamp(date.getTime()).toString());
	}
	
	
	
	public static String toBASE64(String filePath, int flag) throws Exception {
		if (flag == 0) {
			byte byteArray[] = filePath.getBytes();
			Base64 encoder = new Base64();
			return encoder.encodeToString(byteArray);

		} else {
			File file2 = new File(filePath);
			FileInputStream fin2 = new FileInputStream(file2);
			byte byteArray[] = new byte[fin2.available()];
			int i = -1, k = 0;
			while ((i = fin2.read()) != -1) {
				byteArray[k++] = (byte) i;
			}
			//Base64 encoder = new Base64();
			return Base64.encodeBase64String(byteArray);//encoder.encodeBase64String(byteArray);

		}
	}
}
