package com.pixelandtag.cmp.ejb.providers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.dao.core.TechSupportMemberDAOI;
import com.pixelandtag.cmp.ejb.api.sms.ConfigsEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.TechSupportMember;

@Stateless
@Remote
public class TechSupportEJBImpl implements TechSupportEJBI {
	
	public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
		
	@EJB
	private ConfigsEJBI configsEJB;
	
	@EJB
	private QueueProcessorEJBI queueprocEJB;
	
	@EJB
	private TimezoneConverterI timezoneConverterEJB;
	
	@Inject
	private TechSupportMemberDAOI techsupportDAO;
	
	public List<TechSupportMember> getTechsupportWorkingAtThisHour() throws Exception {
		
		List<TechSupportMember> finallist = new ArrayList<TechSupportMember>();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("alarmenabled", Boolean.TRUE);
		List<TechSupportMember> supportraw = techsupportDAO.findByNamedQuery(TechSupportMember.NQ_ALARM_ENABLED, params);
		
		for(TechSupportMember member : supportraw){
			
			Date memberstime = timezoneConverterEJB.convertFromOneTimeZoneToAnother(new Date(), TimeZone.getDefault().getID(), member.getOpco().getCountry().getTimeZone());
			Date workinghoursbegin = member.getWorkinghours_start();
			Date workinghoursend = member.getWorkinghours_end();
			Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
			calendar.setTime(workinghoursbegin);
			
			int hour_of_day_start = calendar.get(Calendar.HOUR_OF_DAY);
			
			calendar.setTime(workinghoursend);
			
			int hour_of_day_end = calendar.get(Calendar.HOUR_OF_DAY);
			
			calendar.setTime(memberstime);
			
			int current_hour = calendar.get(Calendar.HOUR_OF_DAY);
			
			
			if(current_hour>=hour_of_day_start & current_hour<hour_of_day_end){
				
				finallist.add(member);
				
				
			}
		}
		
		return finallist;
	}

}
