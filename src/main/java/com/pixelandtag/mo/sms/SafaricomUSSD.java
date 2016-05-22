package com.pixelandtag.mo.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.inmobia.util.StopWatch;
import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.MessageStatus;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.LocationBeanI;
import com.pixelandtag.cmp.ejb.api.sms.ConfigsEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoEJBI;
import com.pixelandtag.cmp.ejb.api.sms.ProcessorResolverEJBI;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.ejb.api.ussd.USSDMenuEJBI;
import com.pixelandtag.cmp.ejb.subscription.DNDListEJBI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.MOProcessor;
import com.pixelandtag.cmp.entities.MessageLog;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPClient;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPParam;
import com.pixelandtag.sms.mt.workerthreads.GenericHttpResp;
import com.pixelandtag.subscription.dto.MediumType;

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
	
	
	@EJB
	private CMPResourceBeanRemote cmpBean;
	
	@EJB
	private DatingServiceI datingBean;
	
	@EJB
	private LocationBeanI locationBean;
	
	@EJB
	private ProcessorResolverEJBI processorEJB;
	
	@EJB
	private OpcoEJBI opcoEJB;

	@EJB
	private ConfigsEJBI configsEJB;
	
	@EJB
	private TimezoneConverterI timezoneEJB;
	
	@EJB
	private QueueProcessorEJBI queueprocEJB;
	
	@EJB
	private DNDListEJBI dndEJB;
	
	@EJB
	private USSDMenuEJBI ussdmenuEJB;
	
	private Logger logger = Logger.getLogger(SafaricomUSSD.class);
    /**
	 * 
	 */
	private static final long serialVersionUID = 7990814996737314912L;
	
	private byte[] OK_200 =  "200 OK".getBytes();
	
	private GenericHTTPClient httpclient;
	private StopWatch watch;
	

	@Override
	public void destroy() {
		super.destroy();
		 if(httpclient!=null)
	        	httpclient.finalizeMe();
	}

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			httpclient = new GenericHTTPClient("http");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ServletException(e);
		}
		watch = new StopWatch();
	}

	/**
     * @see HttpServlet#HttpServlet()
     */
    public SafaricomUSSD() {
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		final String body = getBody(request);
		
		logger.info("MO_SAFARICOM_USSD:"+body+"\n\n");
		
		String contextpath = request.getRequestURI();
		
		logger.info("MO_SAFARICOM_USSD_CONTEXT_PATH:"+contextpath+"\n\n");
		
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
			
			
			final String USSD_STRING = request.getParameter("USSD_STRING");
			final String SESSION_ID = request.getParameter("SESSION_ID");
			final String SERVICE_CODE = request.getParameter("SERVICE_CODE");
			final String MSISDN = request.getParameter("MSISDN");
			
			sb.append("\n\t\tUSSD_STRING = ").append(USSD_STRING).append("\n");
			sb.append("\t\tSESSION_ID = ").append(SESSION_ID).append("\n");
			sb.append("\t\tSERVICE_CODE = ").append(SERVICE_CODE).append("\n");
			sb.append("\t\tMSISDN = ").append(MSISDN).append("\n");
			
			GenericHTTPParam param = new GenericHTTPParam();
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair("USSD_STRING", USSD_STRING));
			qparams.add(new BasicNameValuePair("SESSION_ID",SESSION_ID));	
			qparams.add(new BasicNameValuePair("SERVICE_CODE",SERVICE_CODE));
			qparams.add(new BasicNameValuePair("MSISDN",MSISDN));
			param.setHttpParams(qparams);
			
			logger.info(sb.toString());
			sb.setLength(0);
			
			PrintWriter pw = response.getWriter();
			try{
				
				
				String cmp_tx_id = cmpBean.generateNextTxId();
				IncomingSMS incomingsms = new IncomingSMS();
				incomingsms.setBilling_status(BillingStatus.NO_BILLING_REQUIRED);
				incomingsms.setCmp_tx_id(cmp_tx_id);
				incomingsms.setIsSubscription(Boolean.FALSE);
				incomingsms.setMediumType(MediumType.ussd);
				incomingsms.setSms(  USSD_STRING );
				incomingsms.setShortcode(SERVICE_CODE);
				incomingsms.setProcessed(Boolean.TRUE);
				incomingsms.setMo_ack(Boolean.TRUE);
				incomingsms.setMsisdn(MSISDN);
				MOProcessor processor = processorEJB.getMOProcessor( SERVICE_CODE );
				incomingsms.setMoprocessor(processor);
				incomingsms.setOpco(opcoEJB.findOpcoByCode("KEN-639-02"));
				incomingsms.setPrice(BigDecimal.ZERO);
				
				logger.info(" >> processor = "+processor);
				
				dndEJB.removeFromDNDList(incomingsms.getMsisdn());
				datingBean.logMO(incomingsms).getId();
				
				
				MessageLog messagelog = new MessageLog();
				messagelog.setCmp_tx_id(incomingsms.getCmp_tx_id());
				messagelog.setMo_processor_id_fk(incomingsms.getMoprocessor().getId());
				messagelog.setMsisdn(incomingsms.getMsisdn());
				messagelog.setOpco_tx_id(incomingsms.getOpco_tx_id());
				messagelog.setShortcode(incomingsms.getShortcode());
				messagelog.setSource(incomingsms.getMediumType().name());
				messagelog.setStatus(MessageStatus.RECEIVED.name());
				messagelog.setMo_sms( USSD_STRING );
				messagelog = processorEJB.saveMessageLog(messagelog);
				param.setUrl( processor.getForwarding_url() );
				watch.start();
				final GenericHttpResp resp = httpclient.call(param);
				final int RESP_CODE = resp.getResp_code();
				watch.stop();
				logger.info(" PROXY_LATENCY_ON forwarding url ("+param.getUrl()+")::::::::::  "+(Double.parseDouble(watch.elapsedTime(TimeUnit.MILLISECONDS)+"")) + " mili-seconds");
				watch.reset();
				final String message = resp.getBody();
				
				logger.info(" RESP CODE "+RESP_CODE);
				logger.info(" message "+message);
				String respStr = message;
				if(RESP_CODE==HttpStatus.SC_OK){
					respStr = message;
				}else if(RESP_CODE==HttpStatus.SC_CREATED || RESP_CODE==HttpStatus.SC_NO_CONTENT){
					respStr = "END Request received. Thank you.";
				}else if(RESP_CODE==HttpStatus.SC_INTERNAL_SERVER_ERROR){
					respStr = "END Problem occurred. Kindly try again later.";
				}else if(RESP_CODE ==HttpStatus.SC_NOT_FOUND){
					respStr = "END Problem occurred. Service currently unavailable.";
				}
				
				pw.write(respStr);
				messagelog.setMt_sms(respStr);
				messagelog = processorEJB.saveMessageLog(messagelog);
				//httpclient.finalizeMe(); confirm whether we should do this
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
