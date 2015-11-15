package com.pixelandtag.cmp.ejb;

import java.util.Date;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.dao.core.ProfileCompletionReminderLogDAOI;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileCompletionReminderLog;

@Stateless
@Remote
public class ProfileCompletionReminderLogEJBImpl implements
		ProfileCompletionReminderLogEJBI {

	public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Inject
	private ProfileCompletionReminderLogDAOI profilecompletionreminderEJB;
	
	
	public ProfileCompletionReminderLog log(PersonDatingProfile profile){
		
		ProfileCompletionReminderLog log = new ProfileCompletionReminderLog();
		
		try{
			log.setProfile(profile);
			log.setTimeStamp(new Date());
			log = profilecompletionreminderEJB.save(log);
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
		
		return log;
	}
	
	
}
