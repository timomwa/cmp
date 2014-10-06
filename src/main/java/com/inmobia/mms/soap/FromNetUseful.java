package com.inmobia.mms.soap;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.Date;

import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.codec.*;
import org.apache.commons.codec.binary.Base64;

//import sun.misc.BASE64Encoder;

public class FromNetUseful {

	public void mmsSender(String sender, String msisdn, String mmscurl,
			String vasid, String vaspId, String contentPath, String subject,
			String password) {

		try {
			
			System.out.println("here");
			Calendar today = Calendar.getInstance();
			String strdate = "";//formatN("" + today.get(Calendar.YEAR), 4)
					//+ formatN("" + (today.get(Calendar.MONTH) + 1), 2)
					//+ formatN("" + (today.get(Calendar.DATE) + 1), 2);
			//strdate = strdate.substring(0, 4) + "-" + strdate.substring(4, 6)
				//	+ "-" + strdate.substring(6, 8);
			// System.out.println(strdate);

			/*SOAPConnectionFactory soapConnFactory = SOAPConnectionFactory
					.newInstance();
			SOAPConnection connection = soapConnFactory.createConnection();*/

			// Next, create the actual message
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage message = messageFactory.createMessage();
			message.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "utf-8");
			message.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");

			// Create objects for the message parts
			SOAPPart soapPart = message.getSOAPPart();
			soapPart.addMimeHeader("Content-Type", "multipart/related");
			soapPart.addMimeHeader("Content-Transfer-Encoding", "binary");
			// soapPart.addMimeHeader("SOAPAction", "\"\"");
			String authorization = toBASE64(password, 0);
			soapPart.addMimeHeader("Content-ID", "<mm7-submit>");
			
			// soapPart.addMimeHeader("Host", "59.161.254.30:10023");

			// -------------------Envelope\-----------------------

			SOAPEnvelope envelope = soapPart.getEnvelope();
			// envelope.addNamespaceDeclaration("xsi",
			// "http://www.w3.org/2001/XMLSchema-instance");
			// envelope.addNamespaceDeclaration("xsd",
			// "http://www.w3.org/2001/XMLSchema");

			// ---------------------------HEAD PART--------------------------
			envelope.getHeader().detachNode();
			SOAPHeader sh = envelope.addHeader();

			Date dt = new Date();
			SOAPHeaderElement shElement = sh
					.addHeaderElement(envelope
							.createName("TransactionID", "mm7",
									"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-2"));
			// shElement.setActor("");
			shElement.setMustUnderstand(true);

			shElement.addTextNode("mivas"
					+ ((dt.getMinutes() + dt.getSeconds() * 60) / dt
							.getSeconds()));
			// ----------------------------------Body Part
			// ----------------------------
			// Populate the body
			// Create the main element and namespace
			SOAPBody body = envelope.getBody();
			SOAPBodyElement sbe = body
					.addBodyElement(envelope
							.createName("SubmitReq", "mm7",
									"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-2"));
			sbe.addChildElement("MM7Version").addTextNode("5.3.0");
			SOAPElement senderID = sbe.addChildElement("SenderIdentification");
			senderID.addChildElement("VASPID").addTextNode(vaspId);
			senderID.addChildElement("VASID" ).addTextNode(vaspId);
			SOAPElement senderadd = senderID.addChildElement("SenderAddress");
			senderadd.addChildElement("Number").addTextNode(sender);

			SOAPElement Recipients = sbe.addChildElement("Recipients");
			SOAPElement To = Recipients.addChildElement("To");
			To.addChildElement("Number").addTextNode(msisdn);

			sbe.addChildElement("MessageClass").addTextNode("Personal");
			/*sbe.addChildElement("ExpiryDate")
					.addTextNode(strdate + "T2H46M40S");*/
			sbe.addChildElement("DeliveryReport").addTextNode("yes");
			sbe.addChildElement("ReadReply").addTextNode("false");
			sbe.addChildElement("Priority").addTextNode("Normal");
			sbe.addChildElement("ServiceCode").addTextNode("MMSCMPMMS0000");
			sbe.addChildElement("Subject").addTextNode(subject);

			// All attachments are contained in a multipart attachment
			MimeMultipart aMultiPart = new MimeMultipart("related");
			// System.setProperty(" mail.mime.multipart.ignoreexistingboundaryparameter",
			// "true");

			// First text
			InternetHeaders someText1Headers = new InternetHeaders();
			someText1Headers.addHeader("Content-Type",
					"text/plain; charset=utf-8");
			someText1Headers.addHeader("Content-ID", "");
			someText1Headers.addHeader("Content-Transfer-Encoding", "7bit");
			someText1Headers.addHeader("Content-Disposition", "attachment");// filename=MMtext0.txt
			MimeBodyPart aTextPart1 = new MimeBodyPart(someText1Headers,
					"MMS First text".getBytes("UTF-8"));
			aMultiPart.addBodyPart(aTextPart1);
			

			// Second text
			/*
			 * InternetHeaders someText2Headers = new InternetHeaders();
			 * someText2Headers.addHeader("Content-Type",
			 * "text/plain; charset=utf-8");
			 * someText2Headers.addHeader("Content-ID", ""); MimeBodyPart
			 * aTextPart2 = new MimeBodyPart(someText2Headers,
			 * "MMS Second text".getBytes("UTF-8"));
			 * aMultiPart.addBodyPart(aTextPart2);
			 */

			InternetHeaders someImageHeaders = new InternetHeaders();
			someImageHeaders.addHeader("Content-Type", "image/jpeg");
			someImageHeaders.addHeader("Content-Transfer-Encoding", "base64");
			someImageHeaders.addHeader("Content-Disposition", "attachment");// filename=MMtext0.txt

			String anImageB64 = toBASE64(contentPath, 1);

			someImageHeaders.addHeader("Content-Length",
					"" + anImageB64.length());// filename=MMtext0.txt

			MimeBodyPart anImagePart = new MimeBodyPart(someImageHeaders,
					anImageB64.getBytes("UTF-8"));

			someImageHeaders
					.addHeader("Content-Id", anImagePart.getContentID());

			aMultiPart.addBodyPart(anImagePart);

			AttachmentPart anAttachment = message.createAttachmentPart(
					aMultiPart, aMultiPart.getContentType());
			message.addAttachmentPart(anAttachment);
			anAttachment.setContentId("");

			SOAPElement source = sbe.addChildElement("Content");
			// source.addAttribute(envelope.createName("allowAdaptations"),
			// "true");
			source.addAttribute(envelope.createName("href"), "cid:"
					+ anImagePart.getContentID());

			// SOAPBodyElement er =
			// body.addBodyElement(envelope.createName("EricssonSubmitReq",
			// "ericMm7", "REL-5-MM7-1-1-ericsson.xsd"));
			// er.addChildElement("SenderVisibility",
			// "ericMm7").addTextNode("true");

			
			
			message.saveChanges();
			
			message.getMimeHeaders().setHeader("Content-Type", 
					  message.getMimeHeaders().getHeader("Content-Type")[0]+
					    "; start=\"</celcom-200102/mm7-submit>\"");
			
			message.getMimeHeaders().setHeader("Authorization",
					"Basic " + authorization.trim());
			
			MimeHeaders mimeH = message.getMimeHeaders();
			//mimeH.addHeader("Content-Type", "multipart/related;");
			//mimeH.addHeader("Content-Type", "test");
			
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
			
			
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance(); 
	        
			SOAPConnection connection = soapConnectionFactory.createConnection(); 
			SOAPMessage resp = connection.call(message, mmscurl); 
			
			resp.writeTo(System.out);
			System.out.println();
			// Comment for request

			// Send the message
			//SOAPMessage reply = connection.call(message, mmscurl);

			// ----------------------------------------------------------------------------------------
			// get the resonse

			System.out.println("\nRESPONSE:\n");
			// Create the transformer
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			// Extract the content of the reply
			/*Source sourceContent = reply.getSOAPPart().getContent();
			// Set the output for the transformation
			StreamResult result = new StreamResult(System.out);
			transformer.transform(sourceContent, result);
			System.out.println();*/
			//connection.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------------
	public String toBASE64(String filePath, int flag) throws Exception {
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
			Base64 encoder = new Base64();
			return encoder.encodeBase64String(byteArray);

		}

		// BASE64Encoder encoder = new BASE64Encoder();
		// return encoder.encode(byteArray);

	}

	// --------------------------------------------------------

	public int PrintAndLog(String Buff) {
		try {
			Calendar today = Calendar.getInstance();
			String ALERTS = "MMS";
			String strlogfile = "" + formatN("" + today.get(Calendar.YEAR), 4)
					+ formatN("" + (today.get(Calendar.MONTH) + 1), 2)
					+ formatN("" + today.get(Calendar.DATE), 2);
			String strdate = formatN("" + today.get(Calendar.YEAR), 4)
					+ formatN("" + (today.get(Calendar.MONTH) + 1), 2)
					+ formatN("" + today.get(Calendar.DATE), 2);
			String strtime = formatN("" + today.get(Calendar.HOUR_OF_DAY), 2)
					+ formatN("" + today.get(Calendar.MINUTE), 2)
					+ formatN("" + today.get(Calendar.SECOND), 2);
			Buff = "[" + ALERTS + " " + strdate + " " + strtime + "]--> "
					+ Buff;
			System.out.println(Buff);
			FileOutputStream outfile = new FileOutputStream("./log/" + ALERTS
					+ "_" + strlogfile + ".log", true);
			PrintStream outprint = new PrintStream(outfile);
			outprint.println(Buff);
			outprint.close();
			outfile.close();
			return 1;
		} catch (Exception e) {
			System.out.println(e.toString());
			return 0;
		}
	}

	// --------------------------------------------------------
	// --------------------------------------------------------

	public String formatN(String str, int x){
		/*int len;
		//System.out.println(str);
		String ret_str="";
		len = str.length();
		if (len >= x)
			ret_str = str;
		else
		
		for(int i=0; i<x;x++){
			ret_str = ret_str + "0";
		
			ret_str = ret_str + str;
		}
		//System.out.println(ret_str);
*/		return str;
	}

	// --------------------------------------------------------

	public static void main(String[] args) {
		String endpointURL = "http://203.82.66.118:5777/mm7/mm7tomms.sh";
		
		String sender = "23355", msisdn = "0193685271", mmscurl = endpointURL, vasid = "inmobia", vaspId = "inmobia", contentPath = "C:\\Users\\Paul\\Desktop\\MMS Pis\\mi_soap.jpg", subject = "test", password = "inmobia:inmobia123";
		password = "inmobia:inmobia123";
		if(args!=null){
			if(args.length>0){
				contentPath =args[0];
				if(args[1]!=null)
					msisdn = args[1];
				if(args[2]!=null)
					endpointURL = args[2];
			}
				
		}
		
		new FromNetUseful().mmsSender(sender, msisdn, mmscurl, vasid, vaspId,
				contentPath, subject, password);
	}
}