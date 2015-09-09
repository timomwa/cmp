package com.pixelandtag.mo.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.ejb.api.sms.ProcessorResolverEJBI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.customer.configs.ConfigurationException;
import com.pixelandtag.smssenders.Receiver;

/**
 * Servlet implementation class MOReceiver
 */
public class MOReceiver extends HttpServlet {
	
	private Logger logger = Logger.getLogger(getClass());
	
	//private Map<String, Receiver> receiverCache = new HashMap<String, Receiver>();
	
	@EJB
	private ProcessorResolverEJBI processorEJB;
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -2356001039800380250L;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public MOReceiver() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		
		Enumeration<String> enums = request.getParameterNames();
		
		String paramName = "";
		String value  = "";
		
		Map<String, String> incomingparams = new HashMap<String,String>();
		
		String ip_addr = request.getRemoteAddr();
		
		
		Enumeration<String> headernames = request.getHeaderNames();
		String headerstr = "\n";
		 while (headernames.hasMoreElements()) { 
			 String headerName = (String) headernames.nextElement();  
		     String headerValue = request.getHeader(headerName);  
		     headerstr += "\n\t\tHEADER >> "+headerName+ " : "+headerValue;
		     //incomingparams.put(Receiver.HTTP_HEADER_PREFIX+headerName, headerValue);
		 }
		
		 
		 logger.info(headerstr+"\n\n");
		
		
		incomingparams.put(Receiver.IP_ADDRESS, ip_addr);
		
		String params = "\n\n\t:: REQ "+ip_addr;
		
		while(enums.hasMoreElements()){
			
			paramName = (String) enums.nextElement();
			
			value = request.getParameter(paramName);
			
			incomingparams.put(paramName, value);
						
			params += "\n\t:: "+   paramName +" : "+value;
			
		}
		
		logger.info(params+"\n\n");
		
		final String body = getBody(request);
		
		
		if(!body.isEmpty())
			incomingparams.put(Receiver.HTTP_RECEIVER_PAYLOAD, body);
		
		try {
			IncomingSMS incomingsms = processorEJB.processMo(incomingparams);
			logger.info("incomingsms = "+incomingsms);
			logger.info("success = "+(incomingsms.getId().compareTo(0L)>0));
		} catch (ConfigurationException e) {
			logger.error(e.getMessage(),e);
		}
		
		
		
	}
	
	
	
	public String getBody(HttpServletRequest request) throws IOException {

	    String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;
	    InputStream inputStream = null;
	    
	    try {
	        inputStream = request.getInputStream();
	        if (inputStream != null) {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	        } else {
	            stringBuilder.append("");
	        }
	    } catch (IOException ex) {
	        logger.error(ex.getMessage(),ex);
	    } finally {
	    	
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	            	 logger.error(ex.getMessage(),ex);
	            }
	        }
	        
	        try {
	        	inputStream.close();
            } catch (IOException ex) {
            }
	    }

	    body = stringBuilder.toString();
	    return body;
	}

}
