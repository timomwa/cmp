package com.pixelandtag.cmp.ejb.api.billing;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.pixelandtag.cmp.dao.core.BillableDAOI;
import com.pixelandtag.sms.producerthreads.Billable;

@Stateless
@Remote
public class BillableQueueProcessorEJBImpl implements BillableQueueProcessorEJBI {
	
	@Inject
	private BillableDAOI billableDAO;
	
	public Billable saveOrUpdate(Billable billable)  throws Exception{
		return billableDAO.save(billable);
	}

}
