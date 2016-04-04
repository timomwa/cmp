package com.pixelandtag.mo.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

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
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.sms.producerthreads.USSDSession;
import com.pixelandtag.subscription.dto.MediumType;
import com.pixelandtag.util.StopWatch;
import com.pixelandtag.web.beans.RequestObject;

/**
 * Servlet implementation class OrangeUSSD
 */
public class OrangeUSSD extends HttpServlet {
	private  Logger logger = Logger.getLogger(OrangeUSSD.class);
	private StopWatch watch;
	private DataSource ds;
	private Context initContext;
	
	private byte[] OK_200 =  "200 OK".getBytes();
	private final String SERVER_TIMEZONE = "-05:00";
	private final String CLIENT_TIMEZONE = "+04:00";

	
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


	/**
	 * 
	 */
	private static final long serialVersionUID = 14512222156L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		final String body = getBody(req);
		
		logger.info("MO_ORANGE_USSD:"+body+"\n\n");
		
		String contextpath = req.getRequestURI();
		
		logger.info("MO_ORANGE_USSD_CONTEXT_PATH:"+contextpath+"\n\n");
		
		Enumeration<String> headernames = req.getHeaderNames();
		String headerstr = "\n";
		 while (headernames.hasMoreElements()) { 
			 String headerName = (String) headernames.nextElement();  
		     String headerValue = req.getHeader(headerName);  
		     headerstr += "\n\t\tHEADER >> "+headerName+ " : "+headerValue;
		 }
		
		 
		 logger.info(headerstr+"\n\n");
		 
		ServletOutputStream sOutStream = null;
		
		
		Enumeration enums = req.getParameterNames();
		
		String paramName = "";
		String value  = "";
		String msg =  req.getParameter("msg");
		
		final StringBuffer sb = new StringBuffer();
		sb.append("\n");
		while(enums.hasMoreElements()){
			
			paramName = (String) enums.nextElement();
			
			value = req.getParameter(paramName);
			
			String ip_addr = req.getRemoteAddr();
			
			sb.append("\t\t:::::: REQ from "+ip_addr+"  : paramName: "+paramName+ " value: "+value).append("\n");
			
		}
		
		logger.info(sb.toString());
		sb.setLength(0);
		
		watch.start();
		
		String menuid = req.getParameter("menuid");
		
		//String respxml = ussdmenuEJB.getMenu(1, -1, 1);
		
		
		PrintWriter pw = resp.getWriter();
		String response =  ussdmenuEJB.getMenu(1, -1, 1);
		String x = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
					+" <!DOCTYPE pages SYSTEM \"cellflash-1.3.dtd\">"
					+" <pages descr=\"News\">"
					+" <page>"
					+" Headlines<br/>"
					+" <a href=\""+contextpath+"?item=1\">Interest rates cut</a><br/>"
					+" <a href=\""+contextpath+"?item=2\">Concorde resumes service</a><br/>"
					+" </page>"
					+" <page tag=\""+contextpath+"?item=3\">"
					+" WASHINGTON-In a much anticipated move, the Federal Reserve"
					+" announced new rate cuts amid growing economic concerns.<br/>"
					+" <a href=\""+contextpath+"?item=4\">Next article</a>"
					+" </page>"
					+" <page tag=\""+contextpath+"?item=5\">"
					+" PARIS-Air France resumed its Concorde service Monday."
					+" The plane had been grounded following a tragic accident."
					+" </page>"
					+" </pages>"; 
		
		try{
			
			logger.info(">>> "+response);
			pw.write(response);
			
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			response = "Problem occurred. Please try again later";
			try{
				pw.close();
			}catch(Exception ex){
				logger.error(e.getMessage(),e);
				response = "Problem occurred. Please try again later";
			}
			
		}finally{
			
			try{
				pw.close();
			}catch(Exception e){
				logger.error(e.getMessage(),e);
				response = "Problem occurred. Please try again later";
			}
			
		}
		
		
	}

	private String handleGeneralQuery(HttpServletRequest req) throws Exception {
		String response = "";
		String ip_addr = req.getRemoteAddr();
		String tx_id = cmpBean.generateNextTxId();
		final RequestObject ro = new RequestObject(req,tx_id,false);
		
		ro.setMediumType(MediumType.ussd);
		
		long messageID = -1;
		
		final MOSms moMessage = new MOSms(req,tx_id);
		
		OutgoingSMS outgoingsms = null;
		IncomingSMS incomingsms = new IncomingSMS();
		OperatorCountry opco = configsEJB.getOperatorByIpAddress(ip_addr);
		
		try{
			moMessage.setMediumType(MediumType.ussd);
			moMessage.setMt_Sent(response);
			
			
			incomingsms.setBilling_status(moMessage.getBillingStatus());
			incomingsms.setCmp_tx_id(moMessage.getCmp_tx_id());
			incomingsms.setOpco_tx_id(moMessage.getOpco_tx_id());
			incomingsms.setEvent_type(moMessage.getEventType()!=null ? moMessage.getEventType().toString() : "" );
			incomingsms.setIsSubscription(Boolean.FALSE);
			incomingsms.setMediumType(MediumType.ussd);
			incomingsms.setSms(moMessage.getSMS_Message_String());
			incomingsms.setShortcode(moMessage.getSMS_SourceAddr());
			incomingsms.setProcessed(Boolean.TRUE);
			incomingsms.setMo_ack(Boolean.TRUE);
			incomingsms.setMsisdn(moMessage.getMsisdn());
			MOProcessor processor = processorEJB.getMOProcessor(moMessage.getSMS_SourceAddr() );
			incomingsms.setOpco(opco);
			logger.info(" >> processor = "+processor);
			incomingsms.setMoprocessor(processor);
			
			dndEJB.removeFromDNDList(incomingsms.getMsisdn());
			
			messageID = datingBean.logMO(incomingsms).getId();
			ro.setMessageId(messageID);
			ro.setOpco(opco);
			
			MessageLog messagelog = new MessageLog();
			messagelog.setCmp_tx_id(incomingsms.getCmp_tx_id());
			messagelog.setMo_processor_id_fk(incomingsms.getMoprocessor().getId());
			messagelog.setMsisdn(incomingsms.getMsisdn());
			messagelog.setMt_sms(incomingsms.getSms());
			messagelog.setOpco_tx_id(incomingsms.getOpco_tx_id());
			messagelog.setShortcode(incomingsms.getShortcode());
			messagelog.setSource(incomingsms.getMediumType().name());
			messagelog.setStatus(MessageStatus.RECEIVED.name());
			messagelog.setMo_sms(moMessage.getSMS_Message_String());
			messagelog = processorEJB.saveMessageLog(messagelog);
			
			outgoingsms = incomingsms.convertToOutgoing();
			outgoingsms = queueprocEJB.saveOrUpdate(outgoingsms);
			
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			response = "Problem occurred. Please try again later";
		}
		
		
		Person p = datingBean.getPerson(ro.getMsisdn(),ro.getOpco());
		
		locationBean.updateSubscriberLocation(ro);
		
		if((response==null || response.isEmpty()) && (p==null || (p!=null && !p.getActive()))){
			response = datingBean.processDating(ro);
		}
		
		
		if(response==null || response.equals("")){//if profile isn't complete, we try complete it
			PersonDatingProfile prof  = datingBean.getProfile(p);
			if(!prof.getProfileComplete())
			 response = datingBean.processDating(ro);
		}
		
		if(response==null || response.equals("")){//we assume they want to renew subscription
				
			response = cmpBean.processUSSD(ro);
		}

		try{
			
			moMessage.setMt_Sent(response);
			ro.setMessageId(messageID);
			
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			response = "Problem occurred. Please try again later";
		}
		
		outgoingsms.setSms(response);
		queueprocEJB.deleteCorrespondingIncomingSMS(outgoingsms);
		queueprocEJB.updateMessageLog(outgoingsms, MessageStatus.SENT_SUCCESSFULLY);
		
		return response;
	}

	@Override
	public void destroy() {
		try {
			
			if(initContext!=null)
				initContext.close();
		
		} catch (NamingException e) {
			logger.error(e.getMessage(),e);
		}
	
	}

	@Override
	public void init() throws ServletException {
		
		watch = new StopWatch();
		
		watch.start();
		
		
		try {
			
			initContext = new InitialContext();
			
			ds = (DataSource)initContext.lookup("java:/cmpDS");
			
			try {

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} catch (NamingException e) {
			
			e.printStackTrace();
		
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
