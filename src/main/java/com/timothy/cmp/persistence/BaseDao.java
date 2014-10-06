package com.timothy.cmp.persistence;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

public class BaseDao {
	
	
	/*private BaseDAO(){
	}*/
	Logger logger = Logger.getLogger(BaseDao.class);
	
	BufferedReader brConsoleReader = null;
	Properties props;
	InitialContext ctx;
	 InputStream inputStream;
	{
		props = new Properties();
		
		try {
			props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			ctx = new InitialContext(props);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		brConsoleReader = new BufferedReader(new InputStreamReader(System.in));
	}

}
