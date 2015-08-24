package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.subscription.SubscriptionOld;
import com.pixelandtag.subscription.dto.SubscriptionDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.web.beans.RequestObject;

public class SubscribeProcessor extends GenericServiceProcessor {

	
	private final Logger logger = Logger.getLogger(SubscribeProcessor.class);
	private DBPoolDataSource ds;
	//private MenuController menu_controller = null;
	private SubscriptionOld subscription = null;
	private InitialContext context;
	private CMPResourceBeanRemote cmpbean;
    
    public void initEJB() throws NamingException{
    	String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		 Properties props = new Properties();
		 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		 props.put(Context.PROVIDER_URL, "remote://localhost:4447");
		 props.put(Context.SECURITY_PRINCIPAL, "testuser");
		 props.put(Context.SECURITY_CREDENTIALS, "testpassword123!");
		 props.put("jboss.naming.client.ejb.context", true);
		 context = new InitialContext(props);
		 cmpbean =  (CMPResourceBeanRemote) 
       		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		 
		 logger.info("Successfully initialized EJB CMPResourceBeanRemote !!");
    }
	public SubscribeProcessor(){
		init_datasource();
		//menu_controller = new MenuController();
		subscription = new SubscriptionOld();
	}
	
	private void init_datasource(){
		
		
		
	}
	
	@Override
	public OutgoingSMS process(IncomingSMS incomingsms){
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		Connection conn = null;
		
		try {
			
			final RequestObject req = new RequestObject(incomingsms);
			
			final String KEYWORD = req.getKeyword().trim();
			final String MSISDN = req.getMsisdn();
			
			conn = getCon();
			
			
			if(KEYWORD.equals("YES")){
				
				SubscriptionDTO sub = subscription.checkAnyPending(conn, MSISDN);
				
				if(sub!=null){
					subscription.updateSubscription(conn, sub.getId(), SubscriptionStatus.confirmed);
					outgoingsms.setSms("You've successfully subscribed");
				}else{
					outgoingsms.setSms("You don't have any pending subscriptions");
				}
				
			}
			
			logger.info(incomingsms.toString());
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			
			try{
				conn.close();
			}catch(Exception e){}
		
		}
		
		return outgoingsms;
	}

	@Override
	public void finalizeMe() {
		
		

		try{
			
			context.close();
		
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}
		
		try {
			
			ds.releaseConnectionPool();
			
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
		}
		
	}

	@Override
	public Connection getCon() {
		
		try {
			
			return ds.getConnection();
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
			return null;
		
		}finally{
		
		}
	}
	
	
	@Override
	public BaseEntityI getEJB() {
		return this.cmpbean;
	}

}
