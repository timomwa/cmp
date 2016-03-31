package com.pixelandtag.mo.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class SafaricomUSSD
 */
@WebServlet(
		description = "SafaricomUSSD", 
		urlPatterns = { 
				"/safaricomUSSD", 
				"/safUSSD"
		})
public class SafaricomUSSD extends HttpServlet {
	
	private Logger logger = Logger.getLogger(SafaricomUSSD.class);
    /**
	 * 
	 */
	private static final long serialVersionUID = 7990814996737314912L;
	
	private byte[] OK_200 =  "200 OK".getBytes();
	

	/**
     * @see HttpServlet#HttpServlet()
     */
    public SafaricomUSSD() {
        super();
        // TODO Auto-generated constructor stub
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		final String body = getBody(request);
		
		logger.info("MO_ORANGE_USSD:"+body+"\n\n");
		
		String contextpath = request.getRequestURI();
		
		logger.info("MO_ORANGE_USSD_CONTEXT_PATH:"+contextpath+"\n\n");
		
		Enumeration<String> headernames = request.getHeaderNames();
		String headerstr = "\n";
		 while (headernames.hasMoreElements()) { 
			 String headerName = (String) headernames.nextElement();  
		     String headerValue = request.getHeader(headerName);  
		     headerstr += "\n\t\tHEADER >> "+headerName+ " : "+headerValue;
		 }
		
		 
		 logger.info(headerstr+"\n\n");
		 Enumeration enums = request.getParameterNames();
		 String paramName = "";
			String value  = "";
			String msg =  request.getParameter("msg");
			
			final StringBuffer sb = new StringBuffer();
			sb.append("\n");
			while(enums.hasMoreElements()){
				
				paramName = (String) enums.nextElement();
				
				value = request.getParameter(paramName);
				
				String ip_addr = request.getRemoteAddr();
				
				sb.append("\t\t:::::: REQ from "+ip_addr+"  : paramName: "+paramName+ " value: "+value).append("\n");
				
			}
			
			logger.info(sb.toString());
			sb.setLength(0);
			PrintWriter pw = response.getWriter();
			try{
				
				pw.write("CON <message>1. Menu 1</message>"
						+ "");
				
			}catch(Exception e){
				logger.error(e.getMessage(),e);
				try{
					pw.close();
				}catch(Exception ex){
					logger.error(e.getMessage(),e);
				}
				
			}finally{
				
				try{
					pw.close();
				}catch(Exception e){
					logger.error(e.getMessage(),e);
				}
				
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
