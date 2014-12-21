package com.pixelandtag.connections;

/**
 * Some simple utilities for building Oracle and Sybase JDBC connections. This
 * is <I>not</I> general-purpose code -- it is specific to my local setup.
 * <P>
 * Taken from Core Servlets and JavaServer Pages from Prentice Hall and Sun
 * Microsystems Press, http://www.coreservlets.com/. &copy; 2000 Marty Hall; may
 * be freely used or adapted.
 */

public class DriverUtilities {
	public static final int ORACLE = 1;
	public static final int SYBASE = 2;
	public static final int MYSQL = 3;
	public static final int UNKNOWN = -1;

	/**
	 * Build a URL in the format needed by the Oracle and Sybase drivers I am
	 * using.
	 */

	public static String makeURL(String host, String dbName, int vendor) {
		if (vendor == ORACLE) {
			return ("jdbc:oracle:thin:@" + host + ":1521:" + dbName);
		} else if (vendor == SYBASE) {
			return ("jdbc:sybase:Tds:" + host + ":1521" + "?SERVICENAME=" + dbName);
		} else if (vendor == MYSQL) {
			return ("jdbc:mysql://" + host + "/" + dbName);
		} else {
			return (null);
		}
	}

	public static String makeURL(String host, String dbName, int vendor,
			String username, String password) {
		if (vendor == ORACLE) {
			return ("jdbc:oracle:thin:@" + host + ":1521:" + dbName);
		} else if (vendor == SYBASE) {
			return ("jdbc:sybase:Tds:" + host + ":1521" + "?SERVICENAME=" + dbName);
		} else if (vendor == MYSQL) {
			return ("jdbc:mysql://" + host + "/" + dbName+ "?user="+username+"&password="+password);
		} else {
			return (null);
		}
	}

	/** Get the fully qualified name of a driver. */

	public static String getDriver(int vendor) {
		if (vendor == ORACLE) {
			return ("oracle.jdbc.driver.OracleDriver");
		} else if (vendor == SYBASE) {
			return ("com.sybase.jdbc.SybDriver");
		} else if (vendor == MYSQL) {
			return ("com.mysql.jdbc.Driver");
		} else {
			return (null);
		}
	}

	/** Map name to int value. */

	public static int getVendor(String vendorName) {
		if (vendorName.equalsIgnoreCase("oracle")) {
			return (ORACLE);
		} else if (vendorName.equalsIgnoreCase("sybase")) {
			return (SYBASE);
		} else if (vendorName.equalsIgnoreCase("mysql")) {
			return (MYSQL);
		} else {
			return (UNKNOWN);
		}
	}
}
