package com.pixelandtag.billing;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.*;

import java.security.cert.X509Certificate;

public class WebServiceClient {
	
	  public static void main(String[] args) {
	  	System.out.println("main(): Begin");
	  	System.setProperty("ilog.rules.res.allowSelfSignedCertificate", "true");
		System.setProperty("ilog.rules.teamserver.allowSelfSignedCertificate", "true");
		  try{
		  
		  	String chargingServiceURL="https://41.223.58.133:8443/ChargingServiceFlowWeb/sca/ChargingExport1";
		  	String requestXML = "" +
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
		 			      "<actualPrice>5</actualPrice>\r\n" + 
		 			      "<basePrice>5</basePrice>\r\n" +
		 			      "<discountApplied>0</discountApplied>\r\n" +
		 			     "<paymentMethod></paymentMethod>\r\n" +
		 			    "<revenuePercent></revenuePercent>\r\n" +
		 			   "<netShare>0</netShare>\r\n" +
		 			      "<cpId>CONTENT360_KE</cpId>\r\n" +
		 			     "<customerClass></customerClass>\r\n" +
		 			      "<eventType>SubscriptionOld Purchase</eventType>\r\n" +//very important
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
 			  System.out.println("main(): Input XML Request "+requestXML);
 			  String response = ""; 
 			  response = DoHttpsPost(chargingServiceURL, requestXML);
			  System.out.println("main(): Response Received is "+ response);
			  System.out.println("main(): End");	  
		  } catch (Exception e) {
		  	System.out.println("main(): Error" + e.getMessage());
			  e.printStackTrace();
		  }
	  }
	  public static String DoHttpsPost(String URL, String requestData)
			throws MalformedURLException, Exception {
	  		System.out.println("DoHTTPPost(): Start");
			String strResponse = "";
			HttpsURLConnection urlc=null;
			URL url = new URL(URL);
			try {
				
				HostnameVerifier hv = new HostnameVerifier(){
				public boolean verify(String urlHostName, SSLSession session) {
				System.out.println("Warning: URL Host: "+urlHostName+" vs. "+session.getPeerHost());
				return true;
				}
				};
				
				String usernamePassword = "CONTENT360_KE" + ":" + "4ecf#hjsan7"; // Username and password will be provided by TWSS Admin
				
				KeyStore ks = KeyStore.getInstance("JKS");
				ks.load(new FileInputStream("F:\\SDPKeyStore.jks"), "12345678".toCharArray());// Location of Key Store c:\\SDPKeystore.dat,12345678
				
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				//KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(ks, "12345678".toCharArray());
				
				TrustManager[] tm;
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				//TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(ks);
				tm = tmf.getTrustManagers();
				TrustManager[] trustAllCerts = {new X509TrustManager() {

					public X509Certificate[] getAcceptedIssuers() {

						return null;
					}

					public void checkClientTrusted(X509Certificate[] chain,
							String authType) {

					}

					public void checkServerTrusted(X509Certificate[] chain,
							String authType) {

					}
				}};
				SSLContext sslContext = SSLContext.getInstance("SSL");
				//sslContext.init(null, trustAllCerts, new SecureRandom());
				sslContext.init(kmf.getKeyManagers(), tm, null);
				
				
				
				
				
				SSLSocketFactory sslSocketFactory =sslContext.getSocketFactory();
				urlc = (HttpsURLConnection) url.openConnection();
				
				urlc.setHostnameVerifier(hv);
				urlc.setSSLSocketFactory(sslSocketFactory);	
			
				System.out.println("DoHttpPost(): Connection received");
				
				String encoding = null;
				sun.misc.BASE64Encoder encoder = (sun.misc.BASE64Encoder) Class.forName( "sun.misc.BASE64Encoder" ).newInstance(); 
				encoding = encoder.encode( usernamePassword.getBytes() ); 
				urlc.setRequestProperty( "Authorization", "Basic " + encoding );
				//urlc.setRequestProperty( "Authorization:Basic " , "QWdVc2VyOkFnVXNlcg");
 
			//	urlc.setRequestProperty("Username", "AgUser");
				//urlc.setRequestProperty("Password", "AgUser");
				urlc.setRequestMethod("POST");
				urlc.setUseCaches(false);
				urlc.setRequestProperty("SOAPAction","");
				urlc.setRequestProperty("Content-Type","text/xml; charset=utf-8");
				urlc.setDoOutput(true);
		        urlc.setDoInput(true);
		        OutputStream out = urlc.getOutputStream();
				out.write(requestData.getBytes());
				out.close();
				
				 try{		
			        InputStreamReader isr = new InputStreamReader(urlc.getInputStream());
			        BufferedReader in = new BufferedReader(isr);
			        String inputLine;
			        System.out.println("Response is -------------------------------------");
			        while ((inputLine = in.readLine()) != null){
			        	System.out.println(inputLine);
			        }	
			        in.close();
		        }
		        catch(Exception e){
		        	e.printStackTrace();
		        	System.out.println("Execption raised during parsing SOAP-response :::" + e.getMessage());
		        }
			}
			catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("Error "+ex);
			}
			return strResponse;
	  }
} 

