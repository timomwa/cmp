package com.pixelandtag.serviceprocessors.sms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.HelloWorldI;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.util.FileUtils;
import com.pixelandtag.web.beans.RequestObject;

public class HelloWorldService extends GenericServiceProcessor {

	final Logger logger = Logger.getLogger(HelloWorldService.class);
	private HelloWorldI helloBean;
	private InitialContext context;
	private Properties mtsenderprop;

	public HelloWorldService() throws NamingException {
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		initEJB();
	}

	public void initEJB() throws NamingException {
		String JBOSS_CONTEXT = "org.jboss.naming.remote.client.InitialContextFactory";
		;
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		props.put(Context.PROVIDER_URL, "remote://localhost:4447");
		props.put(Context.SECURITY_PRINCIPAL, "testuser");
		props.put(Context.SECURITY_CREDENTIALS, "testpassword123!");
		props.put("jboss.naming.client.ejb.context", true);
		context = new InitialContext(props);
		helloBean = (HelloWorldI) context
				.lookup("cmp/HelloWorldEJB!com.pixelandtag.cmp.ejb.HelloWorldI");

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

			String responce = helloBean.reply(MSISDN, MESSAGE);
			mo.setPrice(BigDecimal.ZERO);
			mo.setMt_Sent(responce);
			

		} catch (Exception e) {
			e.printStackTrace();
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
