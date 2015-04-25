package com.pixelandtag.api.bulksms;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class BaseRestImpl implements BaseRestI {

	private Logger logger = Logger.getLogger(getClass());
	/**
	 * Converts input stream to
	 * String
	 * @param incomingData
	 * @return java.lang.String
	 */
	@Override
	public String readString(InputStream incomingData) {
		InputStreamReader isr = null;
		StringBuffer sb = new StringBuffer();
		String data = "";
		try {
			isr = new InputStreamReader(incomingData);
			BufferedReader in = new BufferedReader(isr);
			String line = null;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			data = sb.toString();
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}finally{
			try{
				isr.close();
			}catch(Exception exp){
				logger.warn(exp.getMessage());
			}
		}
		
		return data;
	}

}
