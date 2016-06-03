package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.web.beans.RequestObject;

public class UnknownKeyword extends GenericServiceProcessor {

	final Logger logger = Logger.getLogger(UnknownKeyword.class);
	private Connection conn = null;
	private DBPoolDataSource ds;
    
	public UnknownKeyword() throws Exception {
	}

	@Override
	public OutgoingSMS process(IncomingSMS incomingsms) {
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		Connection conn = null;
		
		try {
			final RequestObject req = new RequestObject(incomingsms);
			final String MSISDN = req.getMsisdn();
		
			int language_id = UtilCelcom.getSubscriberLanguage(MSISDN, conn);
			
			String response = "Unknown keyword.";// UtilCelcom.getMessage(MessageType.UNKNOWN_KEYWORD_ADVICE, conn, language_id) ;
			
			outgoingsms.setSms(response);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
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
		
		try{
			if(ds!=null)
				ds.releaseConnectionPool();
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}

		try {
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	
	
}
