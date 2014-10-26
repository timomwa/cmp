package com.pixelandtag.dynamic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class HttpUpdate_ {
	
	private Logger logger = Logger.getLogger(HttpUpdate_.class);
	private Properties props = null;
	private Properties log4j = null;
	
	public HttpUpdate_(){
		log4j = getPropertyFile("dynamiclog4j.properties");
		props = getPropertyFile("dynamic.properties");
		BasicConfigurator.configure();
	}
	
	/**
	 * Validates the xmlString
	 * 
	 * @param xmlString
	 *            String the xml string
	 * @return
	 */
	private boolean xmlIsValid(String xmlString) {
	
		Document document = createDocFromXML(xmlString);
		
		return (document!=null);
		
	}
	
	
	
	/**
	 * Validates the xmlString
	 * 
	 * @param xmlString
	 *            String the xml string
	 * @return
	 */
	private Document createDocFromXML(String xmlString) {
	
		// pass true to validate xml.
		SAXBuilder saxBuilder = new SAXBuilder(false);
		
	
		Document document = null;
		
		try {
	
			document = saxBuilder.build(new StringReader(xmlString));
	
		} catch (JDOMException e) {
	
			logger.error(e.getMessage(),e);
	
		} catch (IOException e) {
	
			logger.error(e.getMessage(),e);
	
		}catch (Exception e) {
	
			logger.error(e.getMessage(),e);
	
		}
		
		return document;
	}
	
	private String callUrl(String url_) {
		
		
		URL url = null;
		InputStream is = null;
		
		String xmlString = "";
		
		try {
			
			url = new URL(url_);
			is = url.openStream();
			
			xmlString =  convertStreamToString(is);
			
			if (xmlIsValid(xmlString)) {

				return xmlString;
			}
			
		} catch (MalformedURLException e) {
			
			logger.error(e.getMessage(), e);
		
		} catch (IOException e) {
			
			logger.error(e.getMessage(), e);
			
		} finally{
			
			try {
				
				if(is != null)
					is.close();
			
			} catch (Exception e) {
				
				logger.error(e.getMessage(), e);
			
			}
			
		}
		
		return xmlString;

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
		public  String convertStreamToString(InputStream is)
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
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HttpUpdate_ update = new HttpUpdate_();
		
		String url = update.generateURL();
		String xml = update.callUrl(url);
		boolean success = update.parseXML(xml);
		System.out.println(xml);

	}
	
	
	



	private boolean parseXML(String xml) {
		
		Document doc = createDocFromXML(xml);
		
		Element rootElement = doc.getRootElement();
		
		List<Element> items = rootElement.getChildren();
		
		for(Element item: items){
			
		}
		
		
		return false;
	
	}

	private String generateURL() {
		// TODO Auto-generated method stub
		return "http://m.inmobia.com/dynamic/xmlrpc/Dirty?telco=194";
	}



	public Properties getPropertyFile(String filename) {

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
				logger.info(filename + " not found!");
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

}
