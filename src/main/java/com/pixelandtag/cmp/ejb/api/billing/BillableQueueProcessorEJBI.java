package com.pixelandtag.cmp.ejb.api.billing;

import com.pixelandtag.sms.producerthreads.Billable;

public interface BillableQueueProcessorEJBI {

	public Billable saveOrUpdate(Billable billable)  throws Exception;

}
