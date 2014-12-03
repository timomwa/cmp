package com.pixelandtag.cmp.persistence;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.List;

import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.entities.Keyword;



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
					.lookup("ejb:/celcom/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
			DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
			symbols.setGroupingSeparator(',');
			df = new DecimalFormat("###,###.##",symbols);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(),e);
		}
		
	}
	
	public <T> T saveOrUpdate(T t) {
		t = resource_bean.getEM().merge(t);
        return t;
	} 

	public <T> T find(Class<T> entityClass, Object primaryKey) {
		return resource_bean.getEM().find(entityClass, primaryKey);
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> lists( Class<T> entityClass, int start, int end) {
		return resource_bean.getEM().createQuery("from "+entityClass.getSimpleName()).getResultList();
	}
	

}
