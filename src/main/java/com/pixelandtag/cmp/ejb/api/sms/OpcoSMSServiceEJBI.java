package com.pixelandtag.cmp.ejb.api.sms;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;

public interface OpcoSMSServiceEJBI {

	public String getShortcodeByServiceIdAndOpcoId(Long serviceid, OperatorCountry opco) throws ServiceNotLinkedToOpcoException;

}
