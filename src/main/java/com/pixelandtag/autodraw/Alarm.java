package com.pixelandtag.autodraw;

import java.io.File;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
/*import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;*/
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.pixelandtag.util.DateConstraint;

public class Alarm {

	private Logger logger = Logger.getLogger(Alarm.class);

	private String getTodayDate() {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		String strdate = null;

		strdate = sdf.format(new Date());

		return strdate;

	}
	
	public static void main(String arg[]){
		
		
		//Alarm alarm = new Alarm();
		//alarm.send("timothy@inmobia.com,paul.kevin@inmobia.com", "Trivia system testing", "Hi,\n\n Testing \n\nRegards.");
		
		/*
		 
		 
		 Fibonachi?
		 
		String n_ = "5";
		BigInteger n = new BigInteger(n_);
		BigInteger y = new BigInteger("1");
		BigInteger x = new BigInteger("1");
		
		while(x.compareTo(n) == -1 ||  x.compareTo(n) == 0){
			System.out.print(y + " x " + x);
			y = y.multiply(x);
			System.out.println(" = " + y);
			x = x.add(new BigInteger("1"));
		}
		System.out.println("\n"+n+"! = "+y);*/
		
		
		
	}
	

	public void send(String emails,
			String subject, String body) {
		String dateNow = "";

		dateNow = getTodayDate();
		DateConstraint dc = new DateConstraint();
		dateNow = dc.addOrSubtractADays(dateNow.substring(0, 10), -1);
		List<NameValuePair> qparams = null;
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
				.getSocketFactory()));
		cm = new ThreadSafeClientConnManager(schemeRegistry);
		cm.setDefaultMaxPerRoute(1);
		cm.setMaxTotal(2);// http connections that are equal to the worker
							// threads.
		HttpClient httpclient = new DefaultHttpClient(cm);
		qparams = new LinkedList<NameValuePair>();
		HttpEntity resEntity;
		HttpResponse response;

		boolean DEBUG = true;
		try {
			if (emails != null) {
				if (DEBUG) {
					logger.debug("Sending emails to " + emails);

					HttpPost httppost = new HttpPost(
							"http://m.inmobia.com/sendmail/");
					qparams = new LinkedList<NameValuePair>();
					qparams.add(new BasicNameValuePair("to", emails));
					qparams.add(new BasicNameValuePair("subject", subject + "_"
							+ getTodayDate()));
					qparams.add(new BasicNameValuePair("body", body));

					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
							qparams, "UTF-8");

					httppost.setEntity(entity);

					response = httpclient.execute(httppost);

					resEntity = response.getEntity();

					final int RESP_CODE = response.getStatusLine()
							.getStatusCode();

					logger.info(RESP_CODE);

				} else {
					/*
					 * fMailServerConfig.setProperty("mail.smtp.host",
					 * "172.30.66.110");
					 * 
					 * fMailServerConfig.setProperty("mail.smtp.port", "6666");
					 */
					MultiPartEmail email = new MultiPartEmail();
					// System.out.println(System.p
					// .property("user.directory")+filename);
					// email.setHostName("172.30.66.110");
					// email.setSmtpPort(6666);[5:47:05 PM] Julio Cess: try
					// :25172.30.66.112
					email.setHostName("172.30.66.112");
					email.setSmtpPort(6666);
					email.setFrom("noreplay@inmobia.com",
							"Inmobia Email Service");
					/*
					 * EmailAttachment attachment = new EmailAttachment();
					 * attachment.setPath(filename);
					 * attachment.setName("trivia"+"-"+strdate+".xls");
					 * attachment.setDisposition(EmailAttachment.ATTACHMENT);
					 * email.attach(attachment);
					 */
					email.setSubject(subject + dateNow);
					email.setMsg("Hi,\n\nPlease find the report attached");
					for (int i = 0; i < emails.split(",").length; i++) {
						// System.out.println(appProperties.getProperty("email").split(",")[i]);
						email.addBcc(emails.split(",")[i]);
					}
					email.send();
				}
			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

		}

	}

	public void send(String emails, String subject, String body,
			String filename) {
		
		String dateNow = "";

		dateNow = getTodayDate();
		DateConstraint dc = new DateConstraint();
		dateNow = dc.addOrSubtractADays(dateNow.substring(0, 10), -1);
		List<NameValuePair> qparams = null;
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
				.getSocketFactory()));
		cm = new ThreadSafeClientConnManager(schemeRegistry);
		cm.setDefaultMaxPerRoute(1);
		cm.setMaxTotal(2);// http connections that are equal to the worker
							// threads.
		HttpClient httpclient = new DefaultHttpClient();
	    httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

		qparams = new LinkedList<NameValuePair>();
		HttpEntity resEntity;
		HttpResponse response;
		 

		boolean DEBUG = true;
		try {
			if (emails != null) {
				if (DEBUG) {
					
					File file = new File(filename);
					logger.debug("Sending emails to " + emails);

					subject =  subject + "_" + getTodayDate();
					
					HttpPost httppost = new HttpPost(
							"http://m.inmobia.com/sendmail/index.jsp?to="+URLEncoder.encode(emails,"UTF8")+"&subject="+URLEncoder.encode(subject,"UTF8")+"&body="+URLEncoder.encode(body,"UTF8"));
					qparams = new LinkedList<NameValuePair>();
					qparams.add(new BasicNameValuePair("to", emails));
					qparams.add(new BasicNameValuePair("subject", subject + "_"
							+ getTodayDate()));
					qparams.add(new BasicNameValuePair("body", body));
					

					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
							qparams, "UTF-8");

					
					MultipartEntity mpEntity = new MultipartEntity();
				    ContentBody cbFile = new FileBody(file, "application/xls");
				    mpEntity.addPart("userfile", cbFile);
				    
				    mpEntity.addPart("string_field",
				    		new StringBody("field value"));
				    
				   
				    
				    
				    httppost.setEntity(mpEntity);

					response = httpclient.execute(httppost);

					resEntity = response.getEntity();

					final int RESP_CODE = response.getStatusLine()
							.getStatusCode();

					logger.info(RESP_CODE);

				} else {
					/*
					 * fMailServerConfig.setProperty("mail.smtp.host",
					 * "172.30.66.110");
					 * 
					 * fMailServerConfig.setProperty("mail.smtp.port", "6666");
					 */
					MultiPartEmail email = new MultiPartEmail();
					// System.out.println(System.p
					// .property("user.directory")+filename);
					// email.setHostName("172.30.66.110");
					// email.setSmtpPort(6666);[5:47:05 PM] Julio Cess: try
					// :25172.30.66.112
					email.setHostName("172.30.66.112");
					email.setSmtpPort(6666);
					email.setFrom("noreplay@inmobia.com",
							"Inmobia Email Service");
					/*
					 * EmailAttachment attachment = new EmailAttachment();
					 * attachment.setPath(filename);
					 * attachment.setName("trivia"+"-"+strdate+".xls");
					 * attachment.setDisposition(EmailAttachment.ATTACHMENT);
					 * email.attach(attachment);
					 */
					email.setSubject(subject + dateNow);
					email.setMsg("Hi,\n\nPlease find the report attached");
					for (int i = 0; i < emails.split(",").length; i++) {
						// System.out.println(appProperties.getProperty("email").split(",")[i]);
						email.addBcc(emails.split(",")[i]);
					}
					email.send();
				}
			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

		}
		
	}

}
