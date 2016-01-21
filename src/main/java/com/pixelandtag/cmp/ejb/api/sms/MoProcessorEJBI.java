package com.pixelandtag.cmp.ejb.api.sms;

import java.util.List;

import com.pixelandtag.cmp.entities.IncomingSMS;

public interface MoProcessorEJBI {
	
	public List<IncomingSMS> getLatestMO(int size);
	
	public IncomingSMS saveOrUpdate(IncomingSMS incomingsms) throws Exception;

}
