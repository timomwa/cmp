package com.pixelandtag.cmp.ejb.providers;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.Failure;


/**
 * 
 * @author Timothy Mwangi
 * For Exception disambiguation. These will
 * only come from the web services
 *
 */
public class APIException  extends  Failure {


	/**
	 * 
	 */
	private static final long serialVersionUID = -6817154505527809291L;

	/**
	 * 
	 * @param s
	 * @param errorCode
	 */
	public APIException(String s, int errorCode) {
		super(s, errorCode);
	}

	public APIException(String s) {
		super(s);
	}

	public APIException(String s, Response response) {
		super(s, response);
	}

	

}
