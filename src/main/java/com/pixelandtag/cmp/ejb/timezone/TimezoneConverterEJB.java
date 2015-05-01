package com.pixelandtag.cmp.ejb.timezone;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

@Stateless
@Remote
public class TimezoneConverterEJB implements TimezoneConverterI {
	
	
	private Logger logger = Logger.getLogger(getClass());

	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#validateTimezone(java.lang.String)
	 */
	@Override
	public boolean validateTimezone(String timezone) {
		String regex = "\\w+\\/\\w+";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(timezone);
		return matcher.matches();
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#stringToDate(java.lang.String, java.lang.String)
	 */
	@Override
	public Date stringToDate(String dateStr, String dateformat) throws ParseException{
		DateFormat format = new SimpleDateFormat(dateformat);
		return format.parse(dateStr);
	}
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#stringToDate(java.lang.String)
	 */
	@Override
	public Date stringToDate(String dateStr) throws ParseException{
		return stringToDate(dateStr,"yyyy-MM-dd HH:m:ss");
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#convertToThisTimezone(java.lang.String, java.lang.String, java.util.TimeZone)
	 */
	@Override
	public Date convertToThisTimezone(String time, String dateformat, TimeZone timezone) throws ParseException{
		DateFormat format = new SimpleDateFormat(dateformat);
		format.setTimeZone(timezone);
		return format.parse(time);
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#convertToThisTimezone(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Date convertToThisTimezone(String dateformat,String time,  String timeZone) throws ParseException {
		return convertToThisTimezone(time,dateformat,TimeZone.getTimeZone(timeZone));
	}
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#convertToThisTimezone(java.lang.String, java.lang.String)
	 */
	@Override
	public Date convertToThisTimezone(String time, String timeZone) throws ParseException {
		return convertToThisTimezone(time,"yyyy-MM-dd HH:m:ss",TimeZone.getTimeZone(timeZone));
	}
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#convertToThisTimezone(java.lang.String)
	 */
	@Override
	public Date convertToThisTimezone(String time) throws ParseException {
		return convertToThisTimezone(time,"yyyy-MM-dd HH:m:ss",TimeZone.getDefault());
	}
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#isDateInThePast(java.util.Date, java.lang.String)
	 */
	@Override
	public boolean isDateInThePast(Date schedule, String timezone) throws ParseException {
		StringBuffer sb = new StringBuffer();
		DateFormat mdyFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		mdyFormat.setTimeZone(TimeZone.getTimeZone(timezone));
		Date now = new Date();
		String nowInOpcoTZ = mdyFormat.format(now);
		mdyFormat.setTimeZone(TimeZone.getDefault());
		Date now_to_opcotimezone = mdyFormat.parse(nowInOpcoTZ);
		
		boolean isinthepast = schedule.before(now_to_opcotimezone);
		
		sb.append("\n\n\t>>>>current timestamp to opco timezone: "+now_to_opcotimezone+" "+TimeZone.getTimeZone(timezone).getID());
		sb.append("\n\n\t>>>>current timestamp in this timezone: "+now+" "+TimeZone.getDefault().getID());
		sb.append("\n\n\t>>>>Schedule in opco timezone: "+schedule+" "+timezone);
		sb.append("\n\n\t>>>> this date is in the past ?: "+isinthepast);
		
		logger.info(sb.toString());
		
		return isinthepast;
	}
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI#getDateConvertToTimezone(java.util.Date, java.lang.String)
	 */
	@Override
	public Date convertDateToDestinationTimezone(Date dateTime, String timeZone) throws ParseException{
		
		DateFormat mdyFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		mdyFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
		String nowInOpcoTZ = mdyFormat.format(dateTime);
		Date now_to_opcotimezone = mdyFormat.parse(nowInOpcoTZ);
		
		return now_to_opcotimezone;
	}
	
}
