package com.pixelandtag.mo.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import com.pixelandtag.subscription.dto.MediumType;

/**
 * Servlet implementation class OrangeMOReceiver
 * /dn
 *
 */
public class SafaricomMOReceiver extends HttpServlet {
	
	private Logger logger = Logger.getLogger(SafaricomMOReceiver.class);
	
	private static final long serialVersionUID = 1L;
	
	@EJB
	private ProcessorResolverEJBI processorEJB;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SafaricomMOReceiver() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
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
		     headerstr += "\n\t\tMO_SAFARICOM:HEADER >> "+headerName+ " : "+headerValue;
		     //incomingparams.put(Receiver.HTTP_HEADER_PREFIX+headerName, headerValue);
		 }
		
		 
		 logger.info("MO_SAFARICOM:"+headerstr+"\n\n");
		
		
		incomingparams.put(Receiver.IP_ADDRESS, "124.124.124");
		
		String params = "\n\n\tMO_SAFARICOM::: real ip_addr "+ip_addr+" fake ip address 124.124.124";
		
		while(enums.hasMoreElements()){
			
			paramName = (String) enums.nextElement();
			
			value = request.getParameter(paramName);
			
			incomingparams.put(paramName, value);
						
			params += "\n\tMO_SAFARICOM::: "+   paramName +" : "+value;
			
		}
		
		logger.info("MO_SAFARICOM:"+params+"\n\n");
		
		final String body = getBody(request);
		
		logger.info("MO_SAFARICOM:"+body+"\n\n");
		
		if(!body.isEmpty())
			incomingparams.put(Receiver.HTTP_RECEIVER_PAYLOAD, body);
		
		incomingparams.put(Receiver.HTTP_RECEIVER_TYPE, MediumType.sms.name()); 
		
		try {
			IncomingSMS incomingsms = processorEJB.processMo(incomingparams);
			logger.info("incomingsms = "+incomingsms);
			logger.info("success = "+(incomingsms.getId().compareTo(0L)>0));
		} catch (ConfigurationException e) {
			logger.error(e.getMessage(),e);
		}
		
		PrintWriter pw = response.getWriter();
		
		try{
			String responsexml = "";
			pw.write(responsexml);
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}finally{
			try{
				pw.close();
			}catch(Exception es){}
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
