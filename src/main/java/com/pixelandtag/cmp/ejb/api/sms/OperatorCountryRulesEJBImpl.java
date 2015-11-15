package com.pixelandtag.cmp.ejb.api.sms;

import java.util.Date;
import java.util.TimeZone;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.pixelandtag.cmp.dao.opco.OperatorCountryRulesDAOI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.OperatorCountryRules;

@Stateless
@Remote
public class OperatorCountryRulesEJBImpl implements OperatorCountryRulesEJBI {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	
	@Inject
	private OperatorCountryRulesDAOI opcoRulesDAO;
	
	@EJB
	private TimezoneConverterI timeZoneConverterEJB;
	
	
	public Date findEarliestSendtime(OpcoSenderReceiverProfile opcosenderprofile) throws OpcoRuleException {
		
		try {
			
			Date timeinopco_  = timeZoneConverterEJB.convertFromOneTimeZoneToAnother(new Date(), TimeZone.getDefault().getID(), opcosenderprofile.getOpco().getCountry().getTimeZone());
			
			OperatorCountryRules earliest_message_send_hour_ = opcoRulesDAO.findBy("rule_name", "earliest_message_send_hour");
			if(earliest_message_send_hour_==null)
				throw new OpcoRuleException("There is no rule with the name 'earliest_message_send_hour' in the database. Please make sure such a record exists");
			OperatorCountryRules latest_message_send_hour_ = opcoRulesDAO.findBy("rule_name", "latest_message_send_hour");
			if(latest_message_send_hour_==null)
				throw new OpcoRuleException("There is no rule with the name 'latest_message_send_hour' in the database. Please make sure such a record exists");
			
			if(earliest_message_send_hour_.getRule_value()==null || earliest_message_send_hour_.getRule_value().isEmpty())
				throw new OpcoRuleException("The rule value for 'earliest_message_send_hour' in the database is either a blank field or a null value. Please save the right value, e.g 8");
			if(latest_message_send_hour_.getRule_value()==null || latest_message_send_hour_.getRule_value().isEmpty())
				throw new OpcoRuleException("The rule value for 'latest_message_send_hour' in the database is either a blank field or a null value. Please save the right value. e.g 18");
			
			DateTime timeinopco = new DateTime(timeinopco_);
			
			int earliest_message_send_hour = -1;
			int latest_message_send_hour = -1;
			
			
			try{
				earliest_message_send_hour = Integer.parseInt(earliest_message_send_hour_.getRule_value());
			}catch(NumberFormatException nfe){
				logger.warn("Can't parse "+earliest_message_send_hour_.getRule_value()+" to integer ");
				throw new OpcoRuleException("The rule value for 'earliest_message_send_hour' in the database isn't of the right data type. It must be an integer.");
			}
			
			try{
				latest_message_send_hour = Integer.parseInt(latest_message_send_hour_.getRule_value());
			}catch(NumberFormatException nfe){
				logger.warn("Can't parse "+latest_message_send_hour_.getRule_value()+" to integer ");
				throw new OpcoRuleException("The rule value for 'latest_message_send_hour' in the database isn't of the right data type. It must be an integer.");
			}
			
			
			int current_opco_hour = timeinopco.getHourOfDay();
			StringBuffer sb = new StringBuffer();
			sb.append("\n").append("----------------------------------------------------");
			sb.append("\n").append("Hour in opco "+current_opco_hour);
			sb.append("\n").append("latest_message_send_hour "+latest_message_send_hour);
			sb.append("\n").append("current_opco_hour "+current_opco_hour);
			sb.append("\n").append(" (current_opco_hour>=earliest_message_send_hour && current_opco_hour<=latest_message_send_hour) : "+(current_opco_hour>=earliest_message_send_hour && current_opco_hour<=latest_message_send_hour));
			sb.append("\n").append("----------------------------------------------------");
			
			if(current_opco_hour>=earliest_message_send_hour && current_opco_hour<=latest_message_send_hour){
				Date timetosend =  timeZoneConverterEJB.convertFromOneTimeZoneToAnother(timeinopco.toDate(),  opcosenderprofile.getOpco().getCountry().getTimeZone(), TimeZone.getDefault().getID());
				
				sb.append("\n").append("Message will be sent at : "+timetosend);
				sb.append("\n").append("----------------------------------------------------");
				logger.info(sb.toString());
				return timetosend;
			}
			//If we can't send, set the message to be sent next day earliest possible.
			timeinopco = timeinopco.plusDays(1);
			timeinopco = timeinopco.hourOfDay().setCopy(earliest_message_send_hour);
			timeinopco = timeinopco.minuteOfDay().setCopy(0);
			timeinopco = timeinopco.secondOfMinute().setCopy(0);
			
			Date timetosend = timeZoneConverterEJB.convertFromOneTimeZoneToAnother(timeinopco.toDate(),  opcosenderprofile.getOpco().getCountry().getTimeZone(), TimeZone.getDefault().getID());
			sb.append("\n").append("Message will be sent at : "+timetosend);
			sb.append("\n").append("----------------------------------------------------");
			logger.info(sb.toString());
			
			return timetosend;
			
		}catch(OpcoRuleException e){
			logger.error(e.getMessage(),e);
			throw e;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new OpcoRuleException(e.getMessage(),e);
		}
		
	}

}
