package com.pixelandtag.cmp.dao.core;

import com.pixelandtag.cmp.entities.TimeZoneConfig;
import com.pixelandtag.dao.generic.GenericDAO;

public interface TimeZoneConfigDAOI extends GenericDAO<TimeZoneConfig, Long>{

	public TimeZoneConfig getLatest();

}
