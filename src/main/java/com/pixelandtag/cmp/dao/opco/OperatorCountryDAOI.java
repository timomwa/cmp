package com.pixelandtag.cmp.dao.opco;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.dao.generic.GenericDAO;

public interface OperatorCountryDAOI extends GenericDAO<OperatorCountry, Long> {

	public OperatorCountry findbyOpcoCode(String opcocode);

}
