package com.pixelandtag.serviceprocessors.sms;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;

public class PaulTestService extends GenericServiceProcessor {

	private InitialContext context;

	public PaulTestService() throws NamingException{
		initEJB();
	}
	
	public void initEJB() throws NamingException {
		
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
			if(context!=null)
				context.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}


}
