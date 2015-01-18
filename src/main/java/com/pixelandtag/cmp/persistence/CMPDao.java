package com.pixelandtag.cmp.persistence;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.entities.Keyword;
import com.pixelandtag.sms.producerthreads.Billable;



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
					.lookup("ejb:/cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
			DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
			symbols.setGroupingSeparator(',');
			df = new DecimalFormat("###,###.##",symbols);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(),e);
		}
		
	}
	
	public <T> T saveOrUpdate(T t) throws Exception {
		t = resource_bean.saveOrUpdate(t);
        return t;
	} 

	public <T> T find(Class<T> entityClass, Object primaryKey) {
		return resource_bean.getEM().find(entityClass, primaryKey);
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> lists( Class<T> entityClass, int start, int end) {
		return resource_bean.getEM().createQuery("from "+entityClass.getSimpleName()).getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	public <T> Collection<T> find(Class<T> entityClass,Map<String, Object> criteria, int start, int end) {
		Query query = resource_bean.getEM().createQuery("from " + entityClass.getSimpleName()
				+ buildWhere(criteria));
		for (String key : criteria.keySet())
			query.setParameter(key + "_", criteria.get(key));
		return query.getResultList();
	}
	
	/**
	 * 
	 * @param limit
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Billable> getBillable(int limit){
		Query qry = resource_bean.getEM().createQuery("from Billable where in_outgoing_queue=0 AND (retry_count<=maxRetriesAllowed) AND resp_status_code <> 200 AND price>0 AND  processed=0 order by priority asc");
		qry.setFirstResult(0);
		qry.setMaxResults(limit);
		return qry.getResultList();
	}
	
	
	

	private String buildWhere(Map<String, Object> criteria) {
		StringBuffer sb = new StringBuffer();
		if (criteria.size() > 0)
			sb.append(" WHERE ");
		int counter = 0;
		for (String key : criteria.keySet()) {
			counter++;
			sb.append(key).append("=:").append(key).append("_")
					.append(criteria.size() == counter ? "" : " AND ");
		}
		return sb.toString();
	}
	

}
