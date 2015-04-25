package com.pixelandtag.api.bulksms;

import java.io.InputStream;

public interface BaseRestI {

	/**
	 * Converts input stream to
	 * String
	 * @param incomingData
	 * @return java.lang.String
	 */
	public String readString(InputStream incomingData);
}
