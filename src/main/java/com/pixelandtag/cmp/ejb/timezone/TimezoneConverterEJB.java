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
	
	public String dateToString(Date datestr) throws ParseException{
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:m:ss");
		return format.format(datestr);
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#convertToThisTimezone(java.lang.String, java.lang.String, java.util.TimeZone)
	 */
	@Override
	public Date convertToThisTimezone(Date date, String dateformat, TimeZone timezone) throws ParseException{
		DateFormat format = new SimpleDateFormat(dateformat);
		format.setTimeZone(timezone);
		return format.parse( format.format(date) );
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#convertToThisTimezone(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Date convertToThisTimezone(String dateformat,String time,  String timeZone) throws ParseException {
		return convertToThisTimezone(time,dateformat,TimeZone.getTimeZone(timeZone));
	}
	

	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI#convertFromOneTimeZoneToAnother(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Date convertFromOneTimeZoneToAnother(String fromDates, String fromTimezone, String toTimeZone) throws ParseException{
		Date fromDate = stringToDate(fromDates);
		return convertFromOneTimeZoneToAnother(fromDate, fromTimezone, toTimeZone);
		 
	}
	
	
	
	@Override
	public java.util.Date convertFromOneTimeZoneToAnother(java.util.Date date, TimeZone fromTZ , TimeZone toTZ){
	    long fromTZDst = 0;
	    if(fromTZ.inDaylightTime(date))
	    {
	        fromTZDst = fromTZ.getDSTSavings();
	    }
	    long fromTZOffset = fromTZ.getRawOffset() + fromTZDst;
	 
	    long toTZDst = 0;
	    if(toTZ.inDaylightTime(date))
	    {
	        toTZDst = toTZ.getDSTSavings();
	    }
	    long toTZOffset = toTZ.getRawOffset() + toTZDst;
	 
	    return new java.util.Date(date.getTime() + (toTZOffset - fromTZOffset));
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI#convertFromOneTimeZoneToAnother(java.util.Date, java.lang.String, java.lang.String)
	 */
	@Override
	public Date convertFromOneTimeZoneToAnother(Date fromDate, String fromTimezone, String toTimeZone) throws ParseException{
		 return convertFromOneTimeZoneToAnother(fromDate,TimeZone.getTimeZone(fromTimezone), TimeZone.getTimeZone(toTimeZone));
	}
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#convertToThisTimezone(java.lang.String, java.lang.String)
	 */
	@Override
	public Date convertToHostTimezone(String time, String sourcetimeZone) throws ParseException {
		return convertToThisTimezone(time,"yyyy-MM-dd HH:m:ss",TimeZone.getTimeZone(sourcetimeZone));
	}
	
	@Override
	public Date convertToHostTimezone(Date time, String sourcetimeZone) throws ParseException {
		return convertToThisTimezone(time,"yyyy-MM-dd HH:m:ss",TimeZone.getTimeZone(sourcetimeZone));
	}
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.BulkSMSUtilBeanI#convertToThisTimezone(java.lang.String)
	 */
	@Override
	public Date convertToThisTimezone(String time) throws ParseException {
		return convertToThisTimezone(time,"yyyy-MM-dd HH:m:ss",TimeZone.getDefault());
	}
	
	public boolean isDateInThePast(Date date){
		Date d = new Date();
		return date.before(d);
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
