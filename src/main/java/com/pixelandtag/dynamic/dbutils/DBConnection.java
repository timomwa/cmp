package com.pixelandtag.dynamic.dbutils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

public class DBConnection {

	private static Connection conn = null;
	private static Logger logger = Logger.getLogger(DBConnection.class);
	public static String CONSTR = "jdbc:mysql://db:3306/celcom?user=root&password=";
	private static volatile int createdConns = 0;
	private static Context initContext = null;
	private static DataSource ds = null;
	private static boolean registeredDriver = false;

	private static void initialZe() {
		try {

			if (initContext == null || ds == null) {
				initContext = new InitialContext();
				ds = (DataSource) initContext.lookup("java:/CELCOM_DYNAMIC_DS_ONLY");
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	/**
	 * Gets the connection object... uses DYNAMIC_DS_ONLY
	 * 
	 * @return java.sql.Connection
	 */
	public static Connection getNewConnection() {

		Connection conn_ = null;

		try {

			int x = 0;

			int maxTries = 3;

			while (x < maxTries) {
				try {

					if (ds == null) {
						initialZe();
					}

					conn_ = ds.getConnection();

					if (isConnOK(conn_))
						return conn_;
					else
						throw new SQLException("Conn not ok. Tried " + x
								+ " times.");

				} catch (Exception e) {
					logger.warn(e.getMessage() + ": I Tried " + x
							+ " times so far.");
					initialZe();
				}
				x++;
			}

		} catch (Exception e) {

			boolean tryAnother = false;

			if (tryAnother)
				return createFromConnString(CONSTR);

		}

		return conn_;
	}

	public static Connection createFromConnString(String CONSTR_) {
		
		Connection conn_= null;
		
		try {

			logger.info("CREATING THE "
					+ createdConns
					+ "th db connection after failing to get from datasource. Now Creating a db connection from a conn str: "
					+ CONSTR_);

			if (!registeredDriver) {// make sure you register driver
									// only once.
				Class.forName("com.mysql.jdbc.Driver");
				registeredDriver = true;
			}

			conn_ = DriverManager.getConnection(CONSTR_);

			createdConns++;

			return conn_;

		} catch (Exception eq) {

			logger.error(eq.getMessage(), eq);

		}
		
		return conn_;
	}

	/**
	 * Gets the connection object
	 * 
	 * @return
	 */
	public static Connection getExistingConnectionOrCreateNewWithDS(String ds_name) {

		try {

			if (conn != null && !conn.isClosed()) {

				if (isConnOK(conn))
					return conn;
			}

			int x = 0;
			int maxTries = 3;

			while (x < maxTries) {
				try {
					Context initContext = new InitialContext();
					DataSource ds = (DataSource) initContext.lookup("java:/"
							+ ds_name);
					conn = ds.getConnection();

					if (isConnOK(conn))
						return conn;
					else
						throw new SQLException("Conn not ok.");

				} catch (Exception e) {
					logger.warn(e.getMessage(), e);
				}
				x++;
			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

		}

		return conn;
	}

	public static boolean isConnOK(Connection conn_) {

		if (conn_ == null)
			return false;

		Statement stmt = null;
		ResultSet rs = null;
		boolean connOK = false;

		try {

			stmt = conn_.createStatement();
			rs = stmt.executeQuery("SELECT now()");

			if (rs.next()) {
				logger.debug("conn ok!");
				connOK = true;
			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

		} finally {

			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}

			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}

		}

		return connOK;

	}

	public static void closeConn() {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}

	}

}
