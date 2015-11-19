package com.pixelandtag.cmp.ejb;

import java.util.Date;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.dao.core.MatchesLogDAOI;
import com.pixelandtag.dating.entities.MatchesLog;
import com.pixelandtag.dating.entities.PersonDatingProfile;

@Stateless
@Remote
public class MatchesLogEJBImpl implements MatchesLogEJBI {

	Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Inject
	private MatchesLogDAOI matchDAO;
	
	public void log(PersonDatingProfile profile){
		try{
			MatchesLog log = new MatchesLog();
			log.setProfile(profile);
			log.setTimeStamp(new Date());
			log = matchDAO.save(log);
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
	}
}
