package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.entities.MOSms;

public class DatingServiceProcessor extends GenericServiceProcessor {

	final Logger logger = Logger.getLogger(Content360UnknownKeyword.class);
	
	
	@Override
	public MOSms process(MOSms mo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void finalizeMe() {
		// TODO Auto-generated method stub

	}

	@Override
	public Connection getCon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CMPResourceBeanRemote getEJB() {
		// TODO Auto-generated method stub
		return null;
	}

}
