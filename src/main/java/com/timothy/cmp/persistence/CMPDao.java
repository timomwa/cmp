package com.timothy.cmp.persistence;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.apache.log4j.Logger;

import com.timothy.cmp.ejb.CMPResourceBeanRemote;



public class CMPDao  extends BaseDao {
	
	public static String FAIL_JSON = "{ \"response\" : \"{\"type\" : \"1\", \"message\" : \"Something went wrong :(\"}, \"}";
	public  CMPResourceBeanRemote resource_bean = null;
	private static CMPDao this_dao = null;
	private DecimalFormat df = null;
	private Logger logger = Logger.getLogger(CMPDao.class);
	public static CMPDao getInstance() {
		if(this_dao==null)
			this_dao = new CMPDao();
		return this_dao;
	}
	
	protected CMPDao(){
		init();
	}

	private void init() {
		try {
			resource_bean = (CMPResourceBeanRemote) ctx
					.lookup("ejb:/celcom/CMPResourceBean!com.timothy.cmp.ejb.CMPResourceBeanRemote");
			DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
			symbols.setGroupingSeparator(',');
			df = new DecimalFormat("###,###.##",symbols);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(),e);
		}
		
	}
	

}
