package com.pixelandtag.cmp.ejb.api.sms;

import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.audittools.LatencyLog;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;

public interface OpcoSMSServiceEJBI {

	public String getShortcodeByServiceIdAndOpcoId(Long serviceid, OperatorCountry opco) throws ServiceNotLinkedToOpcoException;
	public OpcoSMSService getOpcoSMSService(Long serviceid, OperatorCountry opco) throws ServiceNotLinkedToOpcoException;
	public OpcoSMSService getOpcoSMSService(String keyword, OperatorCountry opco) throws ServiceNotLinkedToOpcoException;
	public <T> T saveOrUpdate(T t) throws Exception;
	public OpcoSMSService getOpcoSMSService(String string, String shortcode, OperatorCountry opco)  throws ServiceNotLinkedToOpcoException;

}
