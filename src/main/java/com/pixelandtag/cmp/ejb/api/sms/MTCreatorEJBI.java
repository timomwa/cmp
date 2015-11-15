package com.pixelandtag.cmp.ejb.api.sms;

import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;

public interface MTCreatorEJBI {

	public OutgoingSMS sendMT(String message, Long serviceid, String msisdn,
			OperatorCountry opco, int priority) throws Exception;

}
