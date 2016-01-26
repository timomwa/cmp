package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;

public class PaulTestService extends GenericServiceProcessor {

	private InitialContext context;
	private CMPResourceBeanRemote cmpbean;

	public PaulTestService() throws NamingException{
		initEJB();
	}
	
	public void initEJB() throws NamingException {
		String JBOSS_CONTEXT = "org.jboss.naming.remote.client.InitialContextFactory";
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		props.put(Context.PROVIDER_URL,
				"remote://" + mtsenderprop.getProperty("ejbhost") + ":" + mtsenderprop.getProperty("ejbhostport"));
		props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
		props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
		props.put("jboss.naming.client.ejb.context", true);
		context = new InitialContext(props);
		cmpbean = (CMPResourceBeanRemote) context
				.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		logger.info("Successfully initialized EJB CMPResourceBeanRemote !!");
	}

	@Override
	public OutgoingSMS process(IncomingSMS incomingSMS) {
		OutgoingSMS outgoingsms = incomingSMS.convertToOutgoing();
		outgoingsms.setSms("Mandazi");
		return outgoingsms;
	}

	@Override
	public void finalizeMe() {
		try {

			context.close();

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

		}
	}

	@Override
	public Connection getCon() {
		return null;
	}

	@Override
	public BaseEntityI getEJB() {
		return this.cmpbean;
	}

}
