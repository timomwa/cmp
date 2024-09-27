package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.subscription.SubscriptionOld;
import com.pixelandtag.subscription.dto.SubscriptionDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.web.beans.RequestObject;

public class SubscribeProcessor extends GenericServiceProcessor {

	
	private final Logger logger = Logger.getLogger(SubscribeProcessor.class);
	private DBPoolDataSource ds;
	private SubscriptionOld subscription = null;
    
	public SubscribeProcessor() throws Exception{
		subscription = new SubscriptionOld();
	}
	
	@Override
	public OutgoingSMS process(IncomingSMS incomingsms){
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		Connection conn = null;
		
		try {
			
			final RequestObject req = new RequestObject(incomingsms);
			
			final String KEYWORD = req.getKeyword().trim();
			final String MSISDN = req.getMsisdn();
			
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
			if(context!=null)
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
	

}
