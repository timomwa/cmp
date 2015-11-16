package com.pixelandtag.cmp.ejb.api.sms;

import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;

public interface OpcoSMSServiceEJBI {

	public String getShortcodeByServiceIdAndOpcoId(Long serviceid, OperatorCountry opco) throws ServiceNotLinkedToOpcoException;
	public OpcoSMSService getOpcoSMSService(Long serviceid, OperatorCountry opco) throws ServiceNotLinkedToOpcoException;
	public OpcoSMSService getOpcoSMSService(String keyword, OperatorCountry opco) throws ServiceNotLinkedToOpcoException;

}
