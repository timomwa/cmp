package com.inmobia.mms.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.Detail;
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
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jdom.output.DOMOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.inmobia.mms.soap.ChargedParty;
import com.inmobia.mms.soap.MessageClass;
import com.inmobia.mms.soap.Priority;

public class MM7_Submit_req {
	
	private Logger logger = Logger.getLogger(MM7_Submit_req.class);
	private static boolean RUN = true;
	SOAPConnectionFactory soapConnectionFactory = null;
	

	public MM7_Submit_req(String url) throws MalformedURLException{
		this.endpoint = new URL(url);
	}
	
	private MMS mms;
	private URL endpoint;
	public static boolean addNH;

	public MMS getMms() {
		return mms;
	}

	public void setMms(MMS mms) {
		this.mms = mms;
	}
	
	
	public SOAPMessage submit(MMS mms) throws SOAPException, IOException, MessagingException{
		
		this.mms = mms;
		SOAPConnection connection = null;
		SOAPMessage resp = null;
		
		try{
			
			SOAPMessage message = createSoapMessage();
		 
			 
			if(soapConnectionFactory==null)
				soapConnectionFactory = SOAPConnectionFactory.newInstance();
			
			if(RUN){
				
				connection = soapConnectionFactory.createConnection(); 
				
				resp = connection.call(message, this.endpoint);
	         
			}else{
				
				System.out.println("\nREQUEST:\n");
				message.writeTo(System.out);
				System.out.println();
	       
			}
			
		}catch(Exception t){
			
			logger.error(t.getMessage(),t);
			soapConnectionFactory = null;
			
		}finally{
			
			
			if(connection!=null)
				connection.close();
		}
		
		return resp;
	    
		
	}
	
	
	private SOAPMessage createSoapMessage() throws SOAPException, IOException, MessagingException, ParserConfigurationException {
		
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage message = factory.createMessage();
		
		
		
		
		SOAPPart soapPart = message.getSOAPPart(); 
		
		//Get the envelope
		SOAPEnvelope envelope = soapPart.getEnvelope(); 
		soapPart.setContentId("</celcom-200102/mm7-submit>");
		soapPart.setMimeHeader("Content-Type", "text/xml");
		
		MimeHeaders mimeHeader = message.getMimeHeaders();  
		mimeHeader.addHeader("Host", "204.51.86.79");
		mimeHeader.addHeader("Authorization", "Basic "+toBASE64(mms.getPassword_username(),0).trim());
		mimeHeader.addHeader("SOAPAction", "\"\"");
		mimeHeader.addHeader("Content-Type", "multipart/related;  type=\"text/xml\"; start=\"</celcom-200102/mm7-submit>\"");
		
			
		/*if(addNH){
			mimeHeader.addHeader("Connection", "close");
			mimeHeader.addHeader("MIME-Version", "1.0");
			mimeHeader.addHeader("Expect", "100-continue");
			
		}*/
		
		
		
	
		message.setProperty("SOAPAction","\"\"");
		
		message.setProperty(SOAPMessage.WRITE_XML_DECLARATION,"true");
		
		
		
		
		//You can now use the getHeader and getBody methods 
		//of envelope to retrieve its empty SOAPHeader and SOAPBody objects.
		
		envelope.getHeader().detachNode();
		SOAPHeader sh = envelope.addHeader();
		
		
		
	
		 
		//================CONSTRUCTING HEADER
		Name headerName = envelope.createName("TransactionID","mm7", "http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-2");
		 
		SOAPHeaderElement headerElement = sh.addHeaderElement(headerName); 
        headerElement.setTextContent(mms.getTxId());
        headerElement.setMustUnderstand(true);
        //===============HEADER DONE======================
     
        
        //===============CONSTRUCTING BODY==============
        SOAPBody body = envelope.getBody();
        
       
        
        Name bodyName = envelope.createName("SubmitReq", "mm7",
				"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-2");
        SOAPBodyElement sbe = body.addBodyElement(bodyName);
        
        sbe.addChildElement("MM7Version").addTextNode("5.3.0");
        
		SOAPElement senderID = sbe.addChildElement("SenderIdentification");
		senderID.addChildElement("VASPID").addTextNode(mms.getVASPID());
		senderID.addChildElement("VASID").addTextNode(mms.getVASID());
		
		SOAPElement senderadd = senderID.addChildElement("SenderAddress");
		senderadd.addChildElement("ShortCode").addTextNode(mms.getShortcode());

		SOAPElement Recipients = sbe.addChildElement("Recipients");
		SOAPElement to = Recipients.addChildElement("To");
		to.addChildElement("Number").addTextNode(mms.getMsisdn());
		sbe.addChildElement("ServiceCode").addTextNode(mms.getServicecode());
		
		sbe.addChildElement("MessageClass").addTextNode("Personal");
		sbe.addChildElement("DeliveryReport").addTextNode("true");
		sbe.addChildElement("ReadReply").addTextNode("false");
		sbe.addChildElement("Priority").addTextNode(Priority.Normal.toString());
		
		sbe.addChildElement("Subject").addTextNode(mms.getSubject());
		sbe.addChildElement("DistributionIndicator").addTextNode(mms.getDistribution_indicator());
		SOAPElement source = sbe.addChildElement("Content");
		source.addAttribute(envelope.createName("allowAdaptations"),"true");
		source.addAttribute(envelope.createName("href"), "cid:"
				+ "A0");
		
		// All attachments are contained in a multipart attachment
		MimeMultipart aMultiPart = new MimeMultipart("related");
		
		
		AttachmentPart anAttachment = message.createAttachmentPart(
				aMultiPart, aMultiPart.getContentType());
		message.addAttachmentPart(anAttachment);
		final String mhs = anAttachment.getMimeHeader("Content-Type")[0];
		final String contType = mhs.split(";")[0]+"; type=\"application/smil\"; start=\"<cid:my.smil>\"; "+mhs.split(";")[1].trim();
		
		anAttachment.setContentId("<A0>");
		
		
		
		final String SMILB = "<smil>"
			+"<head>"
			+"<layout>"
			+"<root-layout background-color=\"#000000\" />"
			+"<region id=\"Image\" width=\"100%\" height=\"100%\" left=\"0\" top=\"0\" />"
			+"<region id=\"Text\" width=\"100%\" height=\"100%\" left=\"0\" top=\"129\" />"
			+"</layout>"
			+"</head>"
			+"<body>"
			+"<par dur=\"35s\">"
			+"<text src=\"cid:desc.txt\" region=\"Text\" />"
			+"<img src=\"cid:mi_soap.jpg\" region=\"Image\" fit=\"fill\" />"
			+"</par>"
			+"</body>"
			+"</smil>";
		
		anAttachment.setMimeHeader("Content-Type", contType);
		InternetHeaders someText1Headers = new InternetHeaders();
		someText1Headers.addHeader("Content-ID", "<my.smil>");
		someText1Headers.addHeader("Content-Type", "application/smil");
		
		MimeBodyPart aTextPart1 = new MimeBodyPart(someText1Headers,
				SMILB.getBytes("UTF-8"));
		aMultiPart.addBodyPart(aTextPart1);
		
		
		message.saveChanges();
		
		
		
		//=============img
		InternetHeaders someImageHeaders = new InternetHeaders();
		someImageHeaders.addHeader("Content-Type", "image/jpeg");
		someImageHeaders.addHeader("Content-Transfer-Encoding", "base64");
		String anImageB64 = toBASE64(mms.getMediaPath(), 1);
		someImageHeaders.addHeader("Content-Length",
				"" + anImageB64.length());// filename=MMtext0.txt
		MimeBodyPart anImagePart = new MimeBodyPart(someImageHeaders,
				anImageB64.getBytes("UTF-8"));
		
		someImageHeaders
				.addHeader("Content-ID", "<mi_soap.jpg>");

		aMultiPart.addBodyPart(anImagePart);
		
		message.saveChanges();
		

		//============img
		 InternetHeaders someText2Headers = new InternetHeaders();
		 someText2Headers.addHeader("Content-Type","text/plain");
		 someText2Headers.addHeader("Content-ID", "<desc.txt>");
		 MimeBodyPart aTextPart2 = new MimeBodyPart(someText2Headers,mms.getMms_text().getBytes("UTF-8"));
		 aMultiPart.addBodyPart(aTextPart2);
		 message.saveChanges();
			
		 message.getMimeHeaders().removeHeader("Accept");
			message.getMimeHeaders().removeHeader("user-agent");
		
		message.saveChanges();
        
        MimeHeaders mimeH = message.getMimeHeaders();
		message.getMimeHeaders().setHeader("Content-Type", 
				  message.getMimeHeaders().getHeader("Content-Type")[0].trim()+
				    ";start=\"</celcom-200102/mm7-submit>\"");
		
		Iterator<MimeHeader> mh = mimeH.getAllHeaders();
		
		String newH = null;
		int i = 0;
		while(mh.hasNext()){
			MimeHeader mimH = mh.next();
			if(mimH.getName().equals("Content-Type"))
				newH= mimH.getValue()+" start=\"</celcom-200102/mm7-submit>\"";
			
			//System.out.println(i+". "+mimH.getName() + " : " +mimH.getValue());
			i++;
		}
		
	   return message;
        
        
       
	}
	
	
	public static String toBASE64(String filePath, int flag) throws IOException  {
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
			return Base64.encodeBase64String(byteArray);

		}
	}
	
	
	
	
	public static void main(String[] args) throws SOAPException, IOException, MessagingException {
		
		
		System.out.println("HELLOOO! SENDING MMS");
		
		BasicConfigurator.configure();
		
		String endpointURL = "http://203.82.66.118:5777/mm7/mm7tomms.sh";// "http://203.82.66.118:5777/mm7/mm7tomms.sh";//"http://localhost:8080/celcom/mm7/mm7.sh";//
		
		MM7_Submit_req request = new MM7_Submit_req(endpointURL);
		
		MMS mms = new MMS();
		
		
		mms.setMedia_path("C:\\Users\\tim\\Desktop\\works.png");
		
		
		addNH = false;
		
		
		//mms.setMsisdn("0133888037");//0196156805
		if(args!=null){
			if(args.length>0){
				mms.setMedia_path(args[0]);
				if(args.length>1)
				if(args[1]!=null)
					mms.setMsisdn(args[1]);
				if(args.length>2)
				if(args[2]!=null){
					addNH = Boolean.getBoolean(args[2]);
				}
			}
		}
		
		RUN=true;
		
		
		System.out.println("sending to: "+mms.getMsisdn());
		mms.setVASID("inmobia");
		mms.setVASPID("inmobia");
		mms.setTransactionID("INM"+System.currentTimeMillis());
		mms.setPassword_username("inmobia:inmobia121");
		mms.setSubject("Great SMS Services.");
		mms.setShortcode("22222");
		mms.setServicecode("MMSCMPMMS0000");
		mms.setMms_text("MMS Broadcast. Reply with code to subscribe to corresponding service");
		
		SOAPMessage response  = request.submit(mms);
		
		MM7DeliveryReport dlr = null;
		try {
			dlr = new MM7DeliveryReport(response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(dlr!=null)
			System.out.println("Status code::: "+dlr.getStatusCode());
		else
			System.out.println(" no status code since delivery report is null.");
		/*
		System.out.println("\nRESPONSE\n");
		response.writeTo(System.out);
		System.out.println();
		*/
		
		
	}
	
	

}
