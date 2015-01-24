package com.pixelandtag.cmp.ejb;

import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManager;

import com.pixelandtag.entities.MOSms;

public interface BaseEntityI {
	
	public <T> T find(Class<T> entityClass, Long id) throws Exception;
	public <T> Collection<T> find(Class<T> entityClass,	Map<String, Object> criteria, int start, int end)   throws Exception;
	public <T> T saveOrUpdate(T t) throws Exception ;
	public <T> T find(Class<T> entityClass, String param_name, Object value) throws Exception;
	public boolean toStatsLog(MOSms mo, String toStatsLog)  throws Exception ;
	public boolean  acknowledge(long message_log_id) throws Exception;
	public boolean sendMT(MOSms mo, String sql) throws Exception;
	public EntityManager getEM();

}
