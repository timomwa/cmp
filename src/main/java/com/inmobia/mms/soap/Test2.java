package com.inmobia.mms.soap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

public class Test2 {

	private static final String XML = "------=_Part_1_13676443.1242791008859"
			+ "\nContent-Type: text/xml; charset=\"utf-8\"\nContent-ID: </celcom-200102/mm7-submit>"

			+ "<?xml version=\"1.0\" ?>"
			+ "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
			+ "<SOAP-ENV:Header>"
			+ "<TransactionID xmlns=\"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-3\""
			+ "SOAP-ENV:mustUnderstand=\"1\">M4e8a68a652ee0757112"
			+ "</TransactionID>"
			+ "</SOAP-ENV:Header>"

			+ "<SOAP-ENV:Body>"
			+ "<SubmitReq xmlns=\"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-3\"><MM7Version>5.3.0</MM7Version><SenderIdentification><VASPID>longtail</VASPID><VASID>jinny</VASID><SenderAddress><ShortCode>23131</ShortCode></SenderAddress></SenderIdentification><Recipients><To><Number>60195356246</Number></To></Recipients><ServiceCode>MMSCMPMMS0000</ServiceCode><DeliveryReport>true</DeliveryReport><Priority>Normal</Priority><Subject>RM0.00: FREE: </Subject><Content allowAdaptations=\"false\" href=\"cid:23131_mms_start\"/></SubmitReq></SOAP-ENV:Body></SOAP-ENV:Envelope>"
			+ "------=_Part_1_13676443.1242791008859"
			+ "\nContent-Type: multipart/related; start=\"<cid:partsmil>\"; type=\"application/smil\"; boundary=\"----=_Part_0_16713087.1242791008437\""
			+ "\nContent-ID: <23131_mms_start>"

			+ "\n\n------=_Part_0_16713087.1242791008437"
			+ "\nContent-Type: application/smil"
			+ "\nContent-ID: <partsmil>"

			+ "\n<smil><head><layout><root-layout background-color=\"#cbcbcb\"/><region id=\"Image\" width=\"100%\" height=\"100%\" left=\"0\" top=\"0\"/><region id=\"Text\" width=\"100%\" height=\"100%\" left=\"0\" top=\"129\"/></layout></head><body><par dur=\"35s\"><img src=\"cid:MMSGreetingIMG\" region=\"Image\" fit=\"fill\"/><text src=\"cid:MMSGreetingTXT\" region=\"Text\"/></par></body></smil>"

			+ "\n\n------=_Part_0_16713087.1242791008437"
			+ "\nContent-Type: image/jpeg"
			+ "\nContent-Transfer-Encoding: base64"
			+ "\nContent-ID: <MMSGreetingIMG>\n\n"

			+ "/9j/4AAQSkZJRgABAgEASABIAAD/4Rx8RXhpZgAATU0AKgAAAAgABwESAAMAAAABAAEAAAEaAAUAAAABAAAAYgEbAAUAAAABAAAAagEoAAMAAAABAAIAAAExAAIAAAAcAAAAcgEyAAIAAAAUAAAAjodpAAQAAAABAAAApAAAANAACvyAAAAnEAAK/IAAACcQQWRvYmUgUGhvdG9zaG9wIENTMyBXaW5kb3dzADIwMTA6MDU6MzEgMTQ6MjQ6MjMAAAAAA6ABAAMAAAAB//8AAKACAAQAAAABAAAA3KADAAQAAAABAAAA3AAAAAAAAAAGAQMAAwAAAAEABgAAARoABQAAAAEAAAEeARsABQ+Vk8nNd10RSHC8OC8/TEnxhPU5cOW/GmYmtOLxpeF9F7O+9mBrx2bhGv/v+H1d/M1dbcVVfmwjMV3zsVdirsVY35tj56FdnvG0bj6HA/jikP2J/59teYJNV/wCcf7rR5ZC58seZb61gUmvGK4SK6AHtzlfPQvZnLxaSv5siP0/pfjn/AIN+hrmPid+/d6BneOodirsVdirsVdirzTzHoeoWnmuHzpptpJqkcmkjSNb0yKnr+lFM1xbz24YgMUaSQOlQWDArUrxblPajsTJ2hjjLF9UL27wf0ufptQPD8KW29j3kUb+QSy581aKissupfo6Yfat7xZLOUHwKTrG1fozz2PZuqwyqeOQPuLkeGZef2vNfMF6msB4tJS61ydh8EdhbzXVf9lGjKPpOd72BinCQJiQPcxOMx50PeQHnVj/AM43eZvOmrQX/mqc+U9ARw0lsjRzanOp/ZULzigqP2mLMP5Bnsmm9sJ6LTnFp4+s/wAUunuj1+PyceWTHH+kfs/a+0PLflvRfKWi2Hl/y9YR6ZpOmx+na2sdTSpqzMxJZmYklmYkkmp3zjsuWeWZnMkykbJPMlxJzMzZT3IMXYq0RXFXjH5mflPD5xnXXtFuItM80QRLDI0wP1a+hSpSO44AsrJU8JFBIGxVhQDO0PaGTSSPDvE8x+ORdZ2p2Vi7Qx8E9pD6ZDmPf3x8viHzbqGi675adovMeiXej8DT61IhltG90uouUVP9Yg+IGbfL2lhzQNGj3F8x1/svr9NOxDjj3x3+Y5j4j4ozT7i3uVAhniuYzQ0jdZF/AkZ5d7TGwXN7NhLHIcQIPmCGWpe2em2ct1qN1Bp+m2yl7m5uXWKBE7l2chQKeOfMntfhlmkYQBlI7ADc35Vv8n1XsEGUhQs/Mvd/yjubi88g6Ncy29xa2kj3R0SK7R4pv0aLmUWLNHIAyhoOBUMAQvGoB2z6i9jcetxdjaWGuvxxjiJ3zvpfnVX5ud2oIjUz4a6XXfQ4vtt6VnSuA7FXYq7FXYq7FXYq7FX/0vv0KUFPoxVjfmv/ABN+h5/8Kej+mPUj9L1+PHhyHOnP4a08e1ab0zTdv/yj+Ul/J3B49iuP6ascX2W5vZ/5bxh+Yvgo8u/oyKH1fSj9fj63Eerwrx5U+LjXelelc28brfm4Zq9uSpkkOxV2KuxVo074q1t7/jire2Ku2xVvFXYq7FXYq0ad8VabjQ8vs03r0p74Fed6z/yqX1n/AE//AIU+s8vj+u/UvU5e/P4q5Rl8GvXw/Gmfqvf7Unsv+VHfpGz+qf4S/SPqr+ja/VOfrV+H0Of7df5N8wdP/J3i/uvD8Ty4eL9bkfv6NXXl+x66Kds2ziN4q7FXYq7FXYq7FXYq7FX/2Q=="

			+ "\n------=_Part_0_16713087.1242791008437"
			+ "\nContent-Type: text/plain; charset=utf-8"
			+ "\nContent-ID: <MMSGreetingTXT>"

			+ "\n\nPISCES: LOVE: without going very far, you will feel a deep need to ensure you of the reliability of your entourage. It will not be the moment to seek to make ra"

			+ "\n\n------=_Part_0_16713087.1242791008437--\n\n";

	public static void main(String[] msgs) throws SOAPException, FileNotFoundException, IOException {

		/*SOAPMessage message = MessageFactory.newInstance().createMessage();
		message.getSOAPBody().addChildElement("Test")
				.addTextNode("This is a Test");
		AttachmentPart ap = message.createAttachmentPart(new DataHandler(new FileDataSource(new File("C:\\Users\\Paul\\Desktop\\MMS Pis\\mi_soap.jpg"))));
		ap.setMimeHeader("Content-Transfer-Encoding", "Base64");
		message.addAttachmentPart(ap);
		message.writeTo(new FileOutputStream(new File("C:\\Users\\Paul\\Desktop\\MMS Pis\\soap.xml")));
		MimeHeaders headers = message.getMimeHeaders();
		SOAPMessage newMessage = MessageFactory.newInstance().createMessage(
				headers, new FileInputStream("C:\\Users\\Paul\\Desktop\\MMS Pis\\soap.xml"));
		newMessage.writeTo(System.out);*/
		
		/*SOAPFactory sf = SOAPFactory.newInstance();
		
		MimeHeaders headers =  new MimeHeaders();
		MessageFactory factory = MessageFactory.newInstance();
		
		
		
		InputStream is = new FileInputStream(new File("C:\\Users\\Paul\\Desktop\\MMS Pis\\holyGrail.xml"));
		SOAPMessage msg = factory.createMessage( headers, is );
		msg.writeTo(System.out);
		
		
		
		
        
		*/
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage message = factory.createMessage();
		
		SOAPPart soapPart =     message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body =         envelope.getBody();

        //Populate the Message
       StreamSource preppedMsgSrc = new StreamSource( 
                new FileInputStream("C:\\Users\\Paul\\Desktop\\MMS Pis\\holyGrail.xml"));
       soapPart.setContent(preppedMsgSrc);
       soapPart.setMimeHeader("Content-ID", "</celcom-200102/mm7-submit>");
       
       MimeHeaders mimeHeader = message.getMimeHeaders();  
		mimeHeader.addHeader("Host", "204.51.86.89");
		mimeHeader.addHeader("Authorization", "Basic aW5tb2JpYTppbm1vYmlhMTIz");//"Basic "+toBASE64("inmobia123",0));
		mimeHeader.addHeader("SOAPAction", "\"\"");
		mimeHeader.addHeader("Content-Type", "multipart/related;  type=\"text/xml\"; start=\"</celcom-200102/mm7-submit>\"");

        //Save the message
        message.saveChanges();
        
        
        AttachmentPart ap1 = message.createAttachmentPart(new DataHandler(new FileDataSource(new File("C:\\Users\\Paul\\Desktop\\MMS Pis\\smil.xml"))));
		ap1.setMimeHeader("Content-Type", "application/smil");
		ap1.setContentId("<A0>");
		message.addAttachmentPart(ap1);
		
		
        AttachmentPart ap = message.createAttachmentPart(new DataHandler(new FileDataSource(new File("C:\\Users\\Paul\\Desktop\\MMS Pis\\mi_soap.jpg"))));
		ap.setMimeHeader("Content-Transfer-Encoding", "Base64");
		ap.setContentId("<my.smil>");
		message.addAttachmentPart(ap);
		
		 
		soapPart.addMimeHeader("Start", "<here>");
		  //message.saveChanges();
        
        message.writeTo(System.out);
        
        
        boolean loc  = false;
        
       loc = true;
       
        String endpointURL;
        
        if(!loc)
        	endpointURL = "http://203.82.66.118:5777/mm7/mm7tomms.sh";
        else
        	endpointURL = "http://localhost:8080/celcom/mm7/mm7.sh";
        
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance(); 
        SOAPConnection connection = soapConnectionFactory.createConnection(); 
		SOAPMessage resp = connection.call(message,endpointURL); 
		
		
		System.out.println("\n\n\nRESPONSE");
		resp.writeTo(System.out);
		
		connection.close();
        
        
        
        
        
		

	}

	private static String callURL(String urlStr, String data) {

		String response = "";
		InputStream is = null;
		URLConnection conn = null;
		OutputStream os = null;
		BufferedReader rd = null;
		OutputStreamWriter wr = null;

		try {

			URL url = new URL(urlStr);
			conn = url.openConnection();
			conn.setRequestProperty("host", "204.51.86.89");
			conn.setRequestProperty("content-length", (data.length() * 8) + "");
			conn.setRequestProperty("content-type",
					"multipart/related; type=\"text/xml\"; start=\"</celcom-200102/mm7-submit>\"");
			conn.setRequestProperty("connection", "close");

			conn.setRequestProperty("authorization",
					"Basic aW5tb2JpYTppbm1vYmlhMTIz");

			/*
			 * conn.setReadTimeout(120000); conn.setConnectTimeout(120000);
			 * conn.setDoOutput(true); os = conn.getOutputStream(); wr = new
			 * OutputStreamWriter(os); wr.write(data); wr.flush();
			 */

			rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String line;

			conn.getInputStream();

			while ((line = rd.readLine()) != null) {
				response += line;
			}

			System.out.println("response: " + response);

		} catch (Exception e) {

			System.out.println(e.getMessage());

		} finally {

			try {

				rd.close();

			} catch (IOException e) {

				System.out.println(e.getMessage());

			}

			try {

				wr.close();

			} catch (IOException e) {

				System.out.println(e.getMessage());

			}

			try {

				os.close();

			} catch (IOException e) {

				System.out.println(e.getMessage());

			}

		}

		return response;
	}

}
