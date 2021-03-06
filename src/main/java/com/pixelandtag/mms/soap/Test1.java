package com.pixelandtag.mms.soap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;

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

import com.pixelandtag.api.BillingStatus;




public class Test1 {

	private static final String TxId = "505";
	private static final String password = "inmobia123";
	private static final String MM7_VERSION = "5.3.0";
	private static final String VASPID = "inmobia";
	private static final String VASID = "inmobia";
	private static final String SHORTCODE = "23355";
	private static final String SERVICE_CODE = "5674";
	private static final String LINKED_ID = "Linked";
	private static final String MSISDN = "0193685271";
	private static final String EARLIEST_DELIVERY_TIME = "2006-11-09T13:01:04+03:00";
	private static final String EXPIRY_DATE = "2007-11-10T13:01:04+03:00";
	private static final String DELIVERY_REPORT = "false";
	private static final String READ_REPLY = "false";
	private static final String SUBJECT = "TriviaMMS";
	private static final String DISTRIBUTION_INDICATION = "false";
	private static final String contentPath = "C:\\Users\\Paul\\Desktop\\MMS Pis\\mi_soap.jpg";
	private static final String MMS_TEXT = "MMS First Text";
	private static int max_throttle_billing = 60000;
	private static boolean enable_biller_random_throttling=true;
	private static int min_throttle_billing = 1000;
	private static Random r = new Random();
	
	
	public static int getRandomWaitTime(){
    	return enable_biller_random_throttling
    			? 
    			(r.nextInt(max_throttle_billing-min_throttle_billing) + min_throttle_billing) : -1;
	}
	
	private static String getDayNumberSuffix(int day) {
	    if (day >= 11 && day <= 13) {
	        return "th";
	    }
	    switch (day % 10) {
	    case 1:
	        return "st";
	    case 2:
	        return "nd";
	    case 3:
	        return "rd";
	    default:
	        return "th";
	    }
	}
	
	public static String getTransactionId(String resp) {
		int start = resp.indexOf("<transactionId>")+"<transactionId>".length();
		int end  = resp.indexOf("</transactionId>");
		return resp.substring(start, end);
	}
	public static int getRandom(){
		return (r.nextInt(1000-0) + 0) ;
	}
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		BigDecimal count = BigDecimal.valueOf(5000000000d);
		BigDecimal size = BigDecimal.valueOf(100);
		BigDecimal percentage = count.divide(size,1, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100).setScale(0));
		System.out.println(count.toPlainString());
		//System.out.println(("0"+"254737701674".substring(3)).contains(Integer.valueOf("0737701674").toString()));
		//System.out.println("254737701674".contains(Integer.valueOf("0737701674").toString()));
		if(true)
			return;
		
		System.out.println(getTransactionId("<transactionId>twss_5158b3586aaf085</transactionId><asdfasdf> asdfasfs"));
		
		if(true)
			return;
		
		String text = "".split("[\\s]")[0].toUpperCase();
		text = text.replaceAll("[\\r]", "");
		text = text.replaceAll("[\\n]", "");
		text = text.replaceAll("[\\t]", "");
		text = text.replaceAll("[.]", "");
		text = text.replaceAll("[,]", "");
		text = text.replaceAll("[?]", "");
		text = text.replaceAll("[@]", "");
		text = text.replaceAll("[\"]", "");
		text = text.replaceAll("[\\]]", "");
		text = text.replaceAll("[\\[]", "");
		text = text.replaceAll("[\\{]", "");
		text = text.replaceAll("[\\}]", "");
		text = text.replaceAll("[\\(]", "");
		text = text.replaceAll("[\\)]", "");
		text = text.trim();
		System.out.println(text);
		
		
		if(true)
			return;
		System.out.println("".split("[\\s]")[0].toUpperCase());
		if(true)
			return;
		
		BigInteger bi = new BigInteger("200220150306095055363089");
		System.out.println(bi);
		System.out.println(new BigDecimal(bi));
		System.out.println(BigInteger.ONE);
		if(true)
			return;
		LinkedHashMap<Integer, String> ms = new LinkedHashMap<Integer,String>();
		ms.put(1, "moja");
		ms.put(2, "mbili");
		
		System.out.println(ms.get(3));
		
		if(true)
			return;
		
		String age = "29years";
		BigDecimal bd = new BigDecimal(age);
		System.out.println(bd.intValue());
		if(true)return;
		
		double value = 0.58;
		System.out.println(new BigDecimal(value));
		System.out.println(BigDecimal.valueOf(value));
		
			if(true)
				return;
		 	FileReader fr = null;  
	        BufferedReader br = null;  
	        String line = "";  
		
		try {
			
			/*MessageFactory messageFactory = MessageFactory.newInstance();  
            SOAPMessage message = messageFactory.createMessage();  */
			//SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance(); 
			//SOAPConnection connection = factory.createConnection();
			
			
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage message = messageFactory.createMessage();
			
			SOAPPart soapPart = message.getSOAPPart();
			soapPart.setContentId("<rootpart@here.com>");
			
			
			SOAPEnvelope envelope = soapPart.getEnvelope();
			
			envelope.getHeader().detachNode();
			SOAPHeader sh = envelope.addHeader();
			
			SOAPHeaderElement soapHeader = sh
			.addHeaderElement(envelope
					.createName("TransactionID", "mm7",
							"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-2"));
			
			
			MimeHeaders mimeH = message.getMimeHeaders();
			
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
			
		
		} catch (Exception e) {
			
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
