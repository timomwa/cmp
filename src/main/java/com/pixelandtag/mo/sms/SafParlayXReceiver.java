package com.pixelandtag.mo.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.pixelandtag.api.MessageStatus;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.LocationBeanI;
import com.pixelandtag.cmp.ejb.api.sms.ConfigsEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoEJBI;
import com.pixelandtag.cmp.ejb.api.sms.ProcessorResolverEJBI;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.ejb.subscription.DNDListEJBI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.MOProcessor;
import com.pixelandtag.cmp.entities.MessageLog;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.ConfigurationException;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPClient;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPParam;
import com.pixelandtag.sms.mt.workerthreads.GenericHttpResp;
import com.pixelandtag.smssenders.Receiver;
import com.pixelandtag.subscription.dto.MediumType;
import com.pixelandtag.util.StopWatch;
import com.pixelandtag.web.beans.RequestObject;

/**
 * Servlet implementation class SafParlayXReceiver
 */
@WebServlet(
		description = "safparlayx", 
		urlPatterns = { 
				"/safparlayx", 
				"/parlayxreceiver"
		})
public class SafParlayXReceiver extends HttpServlet {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -778247067095885243L;


	private Logger logger = Logger.getLogger(SafParlayXReceiver.class);

	
	@EJB
	private ProcessorResolverEJBI processorEJB;
	
	private StopWatch watch = new StopWatch();
	private GenericHTTPClient httpclient;


	private static final String SYNC_ORDER_RELATION_FLAG = "syncOrderRelation";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SafParlayXReceiver() {
        super();
        try {
			httpclient = new GenericHTTPClient("http");
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
		
		incomingparams.put(Receiver.IP_ADDRESS, "126.126.126");
		
		String params = "\n\n\tMO_SAFARICOM::: real ip_addr "+ip_addr+" fake ip address 126.126.126";
		
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
		
		String responsexml = "";
		
		
		if(!body.isEmpty() && body.contains(SYNC_ORDER_RELATION_FLAG)){
			
			String msisdn = getValue(body, "ID");
			
			responsexml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
							+"		xmlns:loc=\"http://www.csapi.org/schema/parlayx/data/sync/v1_0/local\">"
							+"<soapenv:Header/>"
							+"<soapenv:Body>"
							+"<loc:syncOrderRelationResponse>"
							+"<loc:result>0</loc:result>"
							+"<loc:resultDescription>OK</loc:resultDescription>"
							+"</loc:syncOrderRelationResponse>"
							+"</soapenv:Body>"
							+"</soapenv:Envelope>";
			
			logger.info("\n\n\t\t:::::: IS SUBSCRIPTION!! MSISDN =>>> "+msisdn);
			
			try{
			
				GenericHTTPParam param = new GenericHTTPParam();
				param.setUrl("http://139.162.223.21/mosms.php");
				List<NameValuePair> qparams = new ArrayList<NameValuePair>();
				qparams.add(new BasicNameValuePair("cptxid", System.currentTimeMillis()+""));
				qparams.add(new BasicNameValuePair("code","40420"));	
				qparams.add(new BasicNameValuePair("msisdn",msisdn));
				qparams.add(new BasicNameValuePair("text","alerts"));
				
				param.setHttpParams(qparams);
				watch.start();
				final GenericHttpResp resp = httpclient.call(param);
				final int RESP_CODE = resp.getResp_code();
				watch.stop();
				logger.info(" SafParlayxReceiver PROXY_LATENCY_ON forwarding url ("+param.getUrl()+")::::::::::  "+(Double.parseDouble(watch.elapsedTime(TimeUnit.MILLISECONDS)+"")) + " mili-seconds");
				watch.reset();
				String message = resp.getBody();
				if(message==null || message.trim().isEmpty())
					message = "Request received. To unsubscribe, send STOP to 40420";
				
				logger.info("\n\n\t\t::::::_:::::::::PROXY_RESP_CODE: "+RESP_CODE);
				logger.info("\n\n\t\t::::::_:::::::::PROXY_RESPONSE: "+message);
			
			}catch(Exception exp){
				logger.error(exp.getMessage(),exp);
			}
			
			
		}else{
		
			incomingparams.put(Receiver.HTTP_RECEIVER_TYPE, MediumType.sms.name()); 
			
			try {
				IncomingSMS incomingsms = processorEJB.processMo(incomingparams);
				logger.info("incomingsms = "+incomingsms);
				logger.info("success = "+(incomingsms.getId().compareTo(0L)>0));
			} catch (ConfigurationException e) {
				logger.error(e.getMessage(),e);
			}
			
			responsexml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:loc=\"http://www.csapi.org/schema/parlayx/sms/notification/v3_1/local\">"
					   +"<soapenv:Header/>"
					   +"<soapenv:Body>"
					   +"<loc:notifySmsReceptionResponse/>"
					   +"</soapenv:Body>"
					   +"</soapenv:Envelope>";
		}
		
		PrintWriter pw = response.getWriter();
		
		try{
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

	
	private String getValue(String xml,String tagname) {
		String startTag = "<"+tagname+">";
		String endTag = "</"+tagname+">";
		int start = xml.indexOf(startTag)+startTag.length();
		int end  = xml.indexOf(endTag);
		if(start<0 || end<0)
			return "";
		return xml.substring(start, end);
	}

}
