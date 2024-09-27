package com.pixelandtag.cmp.dao.core;

import java.util.Date;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.entities.TimeZoneConfig;
import com.pixelandtag.dao.generic.GenericDaoImpl;

public class TimeZoneConfigDAOImpl extends GenericDaoImpl<TimeZoneConfig, Long> implements TimeZoneConfigDAOI {
	
	Logger logger = Logger.getLogger(getClass());
	
	public TimeZoneConfig getLatest(){
		try{
			Query qry = em.createQuery("from TimeZoneConfig WHERE enabled=:enabled AND effectiveDate <= :today_date order by effectiveDate desc");
			qry.setParameter("enabled", Boolean.TRUE);
			qry.setParameter("today_date", new Date());
			qry.setFirstResult(0);
			qry.setMaxResults(1);
			return (TimeZoneConfig) qry.getSingleResult();
		}catch(javax.persistence.NoResultException nre){
		
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		return null;
	}

}
