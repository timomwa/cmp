package com.pixelandtag.cmp.ejb.api.sms;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;

public interface OpcoEJBI {

	public OperatorCountry findOpcoByCode(String opcocode);

	public OperatorCountry findOpcoById(Long opcoid);

}
