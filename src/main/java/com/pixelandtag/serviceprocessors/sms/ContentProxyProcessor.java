package com.pixelandtag.serviceprocessors.sms;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.inmobia.util.StopWatch;
import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPClient;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPParam;
import com.pixelandtag.sms.mt.workerthreads.GenericHttpResp;
import com.pixelandtag.util.FileUtils;
import com.pixelandtag.web.beans.RequestObject;

public class ContentProxyProcessor extends GenericServiceProcessor {

	final Logger logger = Logger.getLogger(ContentProxyProcessor.class);
	private InitialContext context;
	private Properties mtsenderprop;
	private StopWatch watch;
	private CMPResourceBeanRemote cmpbean;
	private GenericHTTPClient httpclient;
	private OpcoSMSServiceEJBI opcosmsserviceejb;
	private OpcoSenderProfileEJBI opcosenderprofileEJB;
	private Map<Long, OpcoSenderReceiverProfile> opco_sender_profile_cache = new HashMap<Long, OpcoSenderReceiverProfile>();
	
	

	public ContentProxyProcessor() throws NamingException, KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		initEJB();
		httpclient = new GenericHTTPClient("http");
		watch = new StopWatch();
	}

	public void initEJB() throws NamingException {
		String JBOSS_CONTEXT = "org.jboss.naming.remote.client.InitialContextFactory";
		
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		props.put(Context.PROVIDER_URL, "remote://"+mtsenderprop.getProperty("ejbhost")+":"+mtsenderprop.getProperty("ejbhostport"));
		props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
		props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
		props.put("jboss.naming.client.ejb.context", true);
		context = new InitialContext(props);
		cmpbean =  (CMPResourceBeanRemote) 
	       		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		opcosmsserviceejb = (OpcoSMSServiceEJBI) context.lookup("cmp/OpcoSMSServiceEJBImpl!com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI");
		opcosenderprofileEJB = (OpcoSenderProfileEJBI) context.lookup("cmp/OpcoSenderProfileEJBImpl!com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI");
		logger.debug("Successfully initialized EJB CMPResourceBeanRemote !!");
	}

	@Override
	public OutgoingSMS process(IncomingSMS incomingsms) {
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		try {
			logger.info(" >>> incoming "+incomingsms.toString());
			logger.info("\n\n\t\t:::::::::::::::PROXY_MO: incomingsms.getMoprocessor().getForwarding_url() ::: "+incomingsms.getMoprocessor().getForwarding_url());
			
			OpcoSenderReceiverProfile opcotrxprofile = opco_sender_profile_cache.get(incomingsms.getOpco().getId());
			if(opcotrxprofile==null){
				opcotrxprofile = opcosenderprofileEJB.getActiveProfileForOpco(incomingsms.getOpco().getId());
				opco_sender_profile_cache.put(incomingsms.getOpco().getId(), opcotrxprofile);
			}
			
			
			outgoingsms.setOpcosenderprofile(opcotrxprofile);
			
			String isdcode = incomingsms.getOpco().getCountry().getIsdcode();
			String msisdn = incomingsms.getMsisdn();
			if(msisdn!=null){
				if(!msisdn.trim().startsWith(isdcode)){
					msisdn = isdcode+(msisdn.trim());
					incomingsms.setMsisdn(msisdn);
				}
			}
			
			
			GenericHTTPParam param = new GenericHTTPParam();
			param.setUrl(incomingsms.getMoprocessor().getForwarding_url());
			param.setId(incomingsms.getId());
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair("cptxid", incomingsms.getCmp_tx_id()));
			qparams.add(new BasicNameValuePair("code",incomingsms.getShortcode()));	
			qparams.add(new BasicNameValuePair("msisdn",incomingsms.getMsisdn()));
			qparams.add(new BasicNameValuePair("text",incomingsms.getSms()));
			//incomingsms.getMoprocessor()
			
			logger.info("\n\n\t\t:::::::::::::::PROXY_MO: incomingsms.getMoprocessor().getForwarding_url() ::: "+incomingsms.getMoprocessor().getForwarding_url()
					+"\n\t\t:::::::::::::::PROXY_MO: mo.getSMS_Message_String() ::: "+incomingsms.getSms()
					+"\n\t\t:::::::::::::::PROXY_MO: incomingsms.getCmp_tx_id() ::: "+incomingsms.getCmp_tx_id()
					+"\n\t\t:::::::::::::::PROXY_MO: incomingsms.getShortcode() ::: "+incomingsms.getShortcode()
					+"\n\t\t:::::::::::::::PROXY_MO: incomingsms.getMsisdn() ::: "+incomingsms.getMsisdn());
			
			//qparams.add(new BasicNameValuePair("text",mo.getSMS_Message_String()));
			
			
			param.setHttpParams(qparams);
			watch.start();
			final GenericHttpResp resp = httpclient.call(param);
			final int RESP_CODE = resp.getResp_code();
			watch.stop();
			logger.info(getName()+" PROXY_LATENCY_ON forwarding url ("+param.getUrl()+")::::::::::  "+(Double.parseDouble(watch.elapsedTime(TimeUnit.MILLISECONDS)+"")) + " mili-seconds");
			watch.reset();
			final String message = resp.getBody();
			logger.info("\n\n\t\t::::::_:::::::::PROXY_RESP_CODE: "+RESP_CODE);
			logger.info("\n\n\t\t::::::_:::::::::PROXY_RESPONSE: "+message);
			
			if(RESP_CODE==HttpStatus.SC_OK){
				outgoingsms.setSms(message);
			}else if(RESP_CODE==HttpStatus.SC_CREATED || RESP_CODE==HttpStatus.SC_NO_CONTENT){
				//mo.setMt_Sent("Request received.");
			}else if(RESP_CODE==HttpStatus.SC_INTERNAL_SERVER_ERROR){
				outgoingsms.setSms("External application Error. Kindly try again");
			}else if(RESP_CODE ==HttpStatus.SC_NOT_FOUND){
				//mo.setMt_Sent("External application is down.");
			}
			
			if(resp!=null && resp.getLatencyLog()!=null)
				cmpbean.saveOrUpdate(resp.getLatencyLog());
			
			if(incomingsms.getMoprocessor().getProtocol().equalsIgnoreCase("smpp")){
				cmpbean.sendMTSMPP(outgoingsms,incomingsms.getMoprocessor().getSmppid());
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}

		return outgoingsms;
	}

	@Override
	public void finalizeMe() {
		// TODO Auto-generated method stub
		try {
			context.close();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Connection getCon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseEntityI getEJB() {

		return cmpbean;
	}

}
