package com.pixelandtag.serviceprocessors.sms;

import org.apache.log4j.Logger;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.beans.RequestObject;

public class Content360UnknownKeyword extends GenericServiceProcessor {

	final Logger logger = Logger.getLogger(Content360UnknownKeyword.class);
    
	public Content360UnknownKeyword() throws Exception{
	}
	
	@Override
	public OutgoingSMS process(IncomingSMS incomingsms) {
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		try {
			final RequestObject req = new RequestObject(incomingsms);
			final String MSISDN = req.getMsisdn();
			int language_id = baseEntityEJB.getSubscriberLanguage(MSISDN);
			String response = baseEntityEJB.getMessage(MessageType.UNKNOWN_KEYWORD_ADVICE, language_id, incomingsms.getOpco().getId()) ;
			outgoingsms.setSms(response + " "+ getTailTextNotSubecribed().replaceAll("<KEYWORD>", req.getKeyword()).replaceAll("<PRICE>", incomingsms.getPrice().toEngineeringString()));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}finally{
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
	}
	
}
