package com.pixelandtag.billing;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.NodeList;

public class BillingClass {

	private static String xml  =  "" +
		      "<soapenv:Envelope\r\n" +
		      "xmlns:soapenv=" + 
		      "\"http://schemas.xmlsoap.org/soap/envelope/\"\r\n" + 
		      "xmlns:char=" + 
		      "\"http://ChargingProcess/com/ibm/sdp/services/charging/abstraction/Charging\">\r\n" +  
		      "<soapenv:Header />\r\n" + 
		      "<soapenv:Body>\r\n" + 
		      "<char:charge>\r\n" + 
		      "<inputMsg>\r\n" + 
		      "<operation>debit</operation>\r\n" + 
		      "<userId>254734252504</userId>\r\n" + 
		      "<contentId>32329_JOBS</contentId>\r\n" + 
		      "<itemName>32329_JOBS</itemName>\r\n" + 
		      "<contentDescription>32329_JOBS</contentDescription>\r\n" +
		     "<circleId></circleId>\r\n" +
		      "<lineOfBusiness></lineOfBusiness>\r\n" + 
		     "<customerSegment></customerSegment>\r\n" +
		      "<contentMediaType>32329_JOBS</contentMediaType>\r\n" + 
		     "<serviceId>JOBS</serviceId>\r\n" + 
		    "<parentId></parentId>\r\n" +
		      "<actualPrice>5.0</actualPrice>\r\n" + 
		      "<basePrice>5.0</basePrice>\r\n" +
		      "<discountApplied>0</discountApplied>\r\n" +
		     "<paymentMethod></paymentMethod>\r\n" +
		    "<revenuePercent></revenuePercent>\r\n" +
		   "<netShare>0</netShare>\r\n" +
		      "<cpId>CONTENT360_KE</cpId>\r\n" +
		     "<customerClass></customerClass>\r\n" +
		      "<eventType>Subscription Purchase</eventType>\r\n" +//very important
		     "<localTimeStamp></localTimeStamp>\r\n" +
		    "<transactionId>32329</transactionId>\r\n" +
		   "<subscriptionTypeCode>abcd</subscriptionTypeCode>\r\n" +
		  "<subscriptionName>0</subscriptionName>\r\n" +
		 "<parentType></parentType>\r\n" +
		      "<deliveryChannel>SMS</deliveryChannel>\r\n" +
		     "<subscriptionExternalId>0</subscriptionExternalId>\r\n" +
		     "<contentSize></contentSize>\r\n" +
		      "<currency>Kshs</currency>\r\n" + 
		      "<copyrightId>mauj</copyrightId>\r\n" + 
		     "<cpTransactionId>123456787785</cpTransactionId>\r\n" + 
		    "<copyrightDescription>copyright</copyrightDescription>\r\n" + 
		      "<sMSkeyword>JOBS</sMSkeyword>\r\n" + 
		      "<srcCode>32329</srcCode>\r\n" + 
		     "<contentUrl>www.content360.co.ke</contentUrl>\r\n" + 
		    "<subscriptiondays>2</subscriptiondays>\r\n" +
		      "</inputMsg>\r\n" + 	      
		      "</char:charge>\r\n" + 		      
		      "</soapenv:Body>\r\n" +  
		      "</soapenv:Envelope>\r\n";

	@SuppressWarnings("restriction")
	public static void main(String[] args) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, InstantiationException, IllegalAccessException, ClassNotFoundException, ClientProtocolException, IOException, SOAPException {
		
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
		HttpClient httpsclient;
		 
		TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
	        @Override
	        public boolean isTrusted(X509Certificate[] certificate, String authType) {
	            return true;
	        }
	    };
	    
	    SSLSocketFactory sf = new SSLSocketFactory(acceptingTrustStrategy, 
	    SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	    
		SchemeRegistry schemeRegistry = new SchemeRegistry();
	    schemeRegistry.register(new Scheme("https", 8443, sf));
	    cm = new ThreadSafeClientConnManager(schemeRegistry);
		cm.setDefaultMaxPerRoute(2);
		cm.setMaxTotal(2);//http connections that are equal to the worker threads.
			
		httpsclient = new DefaultHttpClient(cm);
		
		
		String chargingServiceURL="https://41.223.58.133:8443/ChargingServiceFlowWeb/sca/ChargingExport1";
	  	
		HttpPost httsppost = new HttpPost(chargingServiceURL);
		
		String usernamePassword = "CONTENT360_KE" + ":" + "4ecf#hjsan7"; // Username and password will be provided by TWSS Admin
		String encoding = null;
		sun.misc.BASE64Encoder encoder = (sun.misc.BASE64Encoder) Class.forName( "sun.misc.BASE64Encoder" ).newInstance(); 
		encoding = encoder.encode( usernamePassword.getBytes() ); 
		httsppost.setHeader("Authorization", "Basic " + encoding);
		httsppost.setHeader("SOAPAction","");
		httsppost.setHeader("Content-Type","text/xml; charset=utf-8");
		List<NameValuePair> qparams = new LinkedList<NameValuePair>();
		qparams.add(new BasicNameValuePair("login", ""));
		 StringEntity se = new StringEntity(xml);
		 httsppost.setEntity(se);
		
		 HttpResponse response = httpsclient.execute(httsppost);
		 
		 
		 final int RESP_CODE = response.getStatusLine().getStatusCode();
		 
		 String resp = convertStreamToString(response.getEntity().getContent());
		 
System.out.println(xml);
		 System.out.println("RESP CODE : "+RESP_CODE);
		 System.out.println("RESP XML : "+resp);
		 
		
	}
	
	private static SOAPMessage getSoapMessageFromString(String xml) throws SOAPException, IOException {
	    MessageFactory factory = MessageFactory.newInstance();
	    SOAPMessage message = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
	    return message;
	}
	
	
	/**
	 * Utility method for converting Stream To String
	 * To convert the InputStream to String we use the
	 * BufferedReader.readLine() method. We iterate until the BufferedReader
	 * return null which means there's no more data to read. Each line will
	 * appended to a StringBuilder and returned as String.
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static  String convertStreamToString(InputStream is)
			throws IOException {
		
		StringBuilder sb = null;
		BufferedReader reader = null;
		
		if (is != null) {
			sb = new StringBuilder();
			String line;

			try {
				reader = new BufferedReader(
						new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				is.close();
			}
			return sb.toString();
		} else {
			return "";
		}
	}

}
