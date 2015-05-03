package com.pixelandtag.serviceprocessors.sms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.inmobia.util.StopWatch;
import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.HelloWorldI;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPClient;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPClientWorker;
import com.pixelandtag.sms.mt.workerthreads.GenericHTTPParam;
import com.pixelandtag.sms.producerthreads.MTProducer;
import com.pixelandtag.util.FileUtils;
import com.pixelandtag.web.beans.RequestObject;

public class ContentProxyProcessor extends GenericServiceProcessor {

	final Logger logger = Logger.getLogger(ContentProxyProcessor.class);
	private HelloWorldI helloBean;
	private InitialContext context;
	private Properties mtsenderprop;
	private StopWatch watch;
	private CMPResourceBeanRemote cmpbean;
	private GenericHTTPClient httpclient;
	private ServiceProcessorDTO serviceprocessor;
	

	public ContentProxyProcessor() throws NamingException {
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		initEJB();
		httpclient = new GenericHTTPClient();
		watch = new StopWatch();
	}

	public void initEJB() throws NamingException {
		String JBOSS_CONTEXT = "org.jboss.naming.remote.client.InitialContextFactory";
		
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		props.put(Context.PROVIDER_URL, "remote://localhost:4447");
		props.put(Context.SECURITY_PRINCIPAL, "testuser");
		props.put(Context.SECURITY_CREDENTIALS, "testpassword123!");
		props.put("jboss.naming.client.ejb.context", true);
		context = new InitialContext(props);
		helloBean = (HelloWorldI) context
				.lookup("cmp/HelloWorldEJB!com.pixelandtag.cmp.ejb.HelloWorldI");
		cmpbean =  (CMPResourceBeanRemote) 
	       		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		logger.debug("Successfully initialized EJB CMPResourceBeanRemote !!");
	}

	@Override
	public MOSms process(MOSms mo) {
		try {
			// TODO Auto-generated method stub
			final RequestObject req = new RequestObject(mo);
			final String KEYWORD = req.getKeyword().trim();
			final String MESSAGE = req.getMsg().trim();
			final int serviceid = mo.getServiceid();
			final String MSISDN = req.getMsisdn();
			
			if(serviceprocessor==null)
				serviceprocessor = cmpbean.getServiceProcessor(mo.getProcessor_id());
			
			GenericHTTPParam param = new GenericHTTPParam();
			param.setUrl(serviceprocessor.getForwarding_url());
			param.setId(mo.getId());
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair("cptxid", mo.getCMP_Txid().toString()));
			qparams.add(new BasicNameValuePair("sourceaddress",mo.getSMS_SourceAddr()));	
			qparams.add(new BasicNameValuePair("msisdn",mo.getMsisdn()));
			
			logger.info("\n\n\t\t:::::::::::::::PROXY_MO: mo.getSMS_Message_String() ::: "+mo.getSMS_Message_String());
			qparams.add(new BasicNameValuePair("sms",mo.getSMS_Message_String()));
			//qparams.add(new BasicNameValuePair("text",mo.getSMS_Message_String()));
			
			
			param.setHttpParams(qparams);
			watch.start();
			final int RESP_CODE = httpclient.call(param);
			watch.stop();
			logger.info(getName()+" PROXY_LATENCY_ON forwarding url ("+param.getUrl()+")::::::::::  "+(Double.parseDouble(watch.elapsedTime(TimeUnit.MILLISECONDS)+"")) + " mili-seconds");
			watch.reset();
			final String message = httpclient.getRespose_msg();
			logger.info("\n\n\t\t::::::SMPP:::::::::PROXY_RESP_CODE: "+RESP_CODE);
			logger.info("\n\n\t\t::::::SMPP:::::::::PROXY_RESPONSE: "+message);
			
			if(RESP_CODE==HttpStatus.SC_OK){
				mo.setMt_Sent(message);
			}else if(RESP_CODE==HttpStatus.SC_CREATED || RESP_CODE==HttpStatus.SC_NO_CONTENT){
				//mo.setMt_Sent("Request received.");
			}else if(RESP_CODE==HttpStatus.SC_INTERNAL_SERVER_ERROR){
				mo.setMt_Sent("External application Error. Kindly try again");
			}else if(RESP_CODE ==HttpStatus.SC_NOT_FOUND){
				//mo.setMt_Sent("External application is down.");
			}
			

			if(serviceprocessor.getProtocol().equalsIgnoreCase("smpp")){
				cmpbean.sendMTSMPP(mo,serviceprocessor.getSmppid());
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}

		return mo;
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

		return helloBean;
	}

}
