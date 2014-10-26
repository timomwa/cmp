package com.pixelandtag.dynamic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;

import com.pixelandtag.dynamic.dbutils.DBConnection;
import com.pixelandtag.dynamic.dto.NoContentTypeException;
import com.sun.net.ssl.HostnameVerifier;
import com.sun.net.ssl.HttpsURLConnection;

public class HTTPUpdate implements CallBackInterface, Runnable {

	static Logger log = Logger.getLogger(HTTPUpdate.class);
	private String xmlrpc;
	private String telco;
	private int id;
	public static ExecutorService service;
	private int count = 0;
	public boolean done = false;
	public long startDate = System.currentTimeMillis();
	private boolean isPushing = false;

	public void setPushing(boolean p) {

		this.isPushing = p;
	}

	public static void trustAllHttpsCertificates() throws Exception {

		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager tm = new miTM();
		trustAllCerts[0] = tm;
		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
				.getInstance("SSL");
		sc.init(null, trustAllCerts, null);
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
				.getSocketFactory());
	}
	
	public static class miTM
			implements
				javax.net.ssl.TrustManager,
				javax.net.ssl.X509TrustManager {

		public java.security.cert.X509Certificate[] getAcceptedIssuers() {

			return null;
		}
		public boolean isServerTrusted(
				java.security.cert.X509Certificate[] certs) {

			return true;
		}
		public boolean isClientTrusted(
				java.security.cert.X509Certificate[] certs) {

			return true;
		}
		public void checkServerTrusted(
				java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {

			return;
		}
		public void checkClientTrusted(
				java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {

			return;
		}
	}
	public static HostnameVerifier hv = new HostnameVerifier() {

		public boolean verify(String urlHostName, String session) {

			return true;
		}
	};
	public Document get(String url) {

		try {
			trustAllHttpsCertificates();
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
		} catch (Exception e) {
		}
		InputStream inStream = null;
		try {
			System.setProperty("sun.net.client.defaultConnectTimeout", "100000");
			System.setProperty("sun.net.client.defaultReadTimeout", "100000");
			URL u = new URL(url);
			inStream = (InputStream) u.getContent();
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder db = factory.newDocumentBuilder();
			return db.parse(inStream);
		} catch (IOException e) {
			log.error("Error while retrieving " + url, e);
		} catch (Exception e) {
			log.error(e.getMessage());
		}finally{
			
			try {
				inStream.close();
			} catch (Exception e) {
			}
		}
		return null;
	}
	public static Properties getPropertyFile(String filename) {

		Properties prop = new Properties();
		InputStream inputStream = null;;
		String path;
		try {
			path = System.getProperty("user.dir") + "/" + filename;
			inputStream = new FileInputStream(path);
		} catch (Exception e) {
			URL urlpath = new String().getClass().getResource(filename);
			try {
				inputStream = new FileInputStream(urlpath.getPath());
			} catch (Exception exb) {
				log.info(filename + " not found!");
			}
		}
		try {
			if (inputStream != null) {
				prop.load(inputStream);
				
			}
		} catch (Exception e) {
			System.out.println(e);
		}finally{
			if(inputStream!=null)
				try {
					inputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		return prop;
	}
	public HTTPUpdate() throws Exception {

		class BigBrother extends Thread {

			public void run() {

				while (!done) {
					long timeout = isPushing
							? (1000 * 60 * 60)
							: (1000 * 60 * 15);
					if ((System.currentTimeMillis() - startDate) > timeout) {
						log.debug("We're still running after 15 minutes.. that's cant be right! Lets commit suicide!!");
						InputStream is = null;
						Process p = null;
						Runtime runtime = null;
						try {
							byte[] bo = new byte[100];
							String[] cmd = {"/bin/bash", "-c", "echo $PPID"};
							p = Runtime.getRuntime().exec(cmd);
							is = p.getInputStream();
							is.read(bo);
							String spid = new String(bo);
							log.debug("My (string) PID:" + spid);
							int pid = Integer.parseInt(spid.trim());
							log.debug("My PID:" + pid);
							cmd[0] = "/bin/kill";
							cmd[1] = "-9";
							cmd[2] = "" + pid;
							runtime  = Runtime.getRuntime();
							runtime.exec(cmd);
							throw new Exception(
									"Dammit, why wont I die...? (my pid:" + pid
											+ ")");
						} catch (Exception e) {
							log.error(
									"Unable to commit suicide - "
											+ e.getMessage(), e);
						}finally{
							
							try{
								is.close();
							}catch(Exception e){}
							
							try{
								runtime.exit(0);//normal termination
							}catch(Exception e){}
							
							try{
								p.destroy();
							}catch(Exception e){}
						}
					} else {
						Thread.yield();
					}
				}
			}
		}
		new BigBrother().start();
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			PropertyConfigurator.configure(getPropertyFile("dynamiclog4j.properties"));
			Properties properties = getPropertyFile("dynamic.properties");
			conn = DBConnection.createFromConnString(properties.getProperty("connstr"));

			service = Executors.newFixedThreadPool(20);

			xmlrpc = properties.getProperty("xmlrpc").trim();
			if (!xmlrpc.endsWith("/"))
				xmlrpc += "/";
			telco = properties.getProperty("telco");
			if (null == telco || telco.trim().length() == 0)
				throw new Exception("No telco found in property file");

			int maxpush = 5;
			try {
				maxpush = Integer.parseInt(properties.getProperty("maxpush"));
			} catch (Exception e) {
			}

			int updates = 0;
			int retry = 0;
			do {
				retry = 0;
				count = 0;
				log.debug("fetching dirty content (" + xmlrpc + "Dirty?telco="
						+ telco + ")");
				Document doc = get(xmlrpc + "Dirty?telco=" + telco);
				if (null == doc) {
					retry++;
				} else {
					// Hand over the XML to the processor
					try {
						updates = Update.Process(conn, doc, maxpush, this);
						if (updates > 0)
							retry++;
					} catch (NoContentTypeException e) {
						log.warn(e, e);
						// Oops.. let see if we can update it! :)
						log.debug("Fetching contenttypes from master");
						doc = get(xmlrpc + "ContentTypes?telco=" + telco);
						Update.ProcessConentTypes(conn, doc);
						retry++;
					} catch (Exception e) {
						log.warn(e);
						try {
							Thread.sleep(5000);
						} catch (Exception ee) {
						}
					}
				}
			} while (retry > 0 && retry <= 5);

			// Wait for the service to finish
			service.shutdown();
			try {
				service.awaitTermination(1800, TimeUnit.SECONDS);
			} catch (Exception e) {
				log.error("Error while waiting for threads to finish", e);
			}
			log.debug("completed");
		} catch (Exception e) {
			log.error(e, e);
			throw e;
		}finally{
			
			/*
			 * This finally block was added by Timothy Mwangi 
			 * on 19th July 2012. The reason why was because there did not seem to be a place
			 * where were were closing the connection object.
			 */
			try{
				if(conn!=null){
					conn.close();
				}
			}catch(Exception e){}
		}
		done = true;
	}
	public static void main(String[] args) throws Exception {

		new HTTPUpdate();
	}

	/* The methods below are being used for pingback to the "master" server */
	public HTTPUpdate(String xmlrpc, String telco, int id) {

		this.xmlrpc = xmlrpc;
		this.telco = telco;
		this.id = id;
	}
	public void run() {

		try {
			String url = xmlrpc + "Update?telco=" + telco + "&ids=" + id;
			log.debug("pinging " + url);
			get(url);
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	public void ping(int id) {

		count++;
		service.submit(new HTTPUpdate(xmlrpc, telco, id));
	}
}
