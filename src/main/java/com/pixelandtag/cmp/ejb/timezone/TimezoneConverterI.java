package com.pixelandtag.cmp.ejb.timezone;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

public interface TimezoneConverterI {

	/**
	 * Validates the timezone.
	 * Makes sure timezone is always word slash word e.g "America/New_York"
	 * @param timezone
	 * @return
	 */
	public boolean validateTimezone(String timezone);
	/**
	 * Converts string to given timezone
	 * @param dateStr
	 * @param dateformat
	 * @return java.util.Date
	 * @throws ParseException
	 */
	public Date stringToDate(String dateStr, String dateformat) throws ParseException;
	
	/**
	 * assumes date format to be 
	 * "yyyy-MM-dd HH:m:ss"
	 * 
	 * @param dateStr - java.lang.String
	 * @return date - java.util.Date 
	 * @throws ParseException
	 */
	public Date stringToDate(String dateStr) throws ParseException;
	/**
	 * Converts string to given timezone
	 * @param time
	 * @param dateformat
	 * @param timezone
	 * @return
	 * @throws ParseException
	 */
	public Date convertToThisTimezone(String time, String dateformat, TimeZone timezone) throws ParseException;
	/**
	 * <p></p>
	 * @param dateformat, e.g "yyyy-MM-dd HH:m:ss"
	 * @param schedule
	 * @param scheduleutcoffset e.g  "America/New_York", "Africa/Nairobi"
	 * @return
	 * @throws ParseException 
	 */
	public Date convertToThisTimezone(String dateformat,String time,  String timeZone) throws ParseException;
	
	/**
	 * 
	 * @param time
	 * @param timeZone
	 * @return
	 * @throws ParseException
	 */
	public Date convertToHostTimezone(String time, String timeZone) throws ParseException;
	
	
	
	/**
	 * 
	 * @param time - THE DATE IN CURRENT SERVER OR JAVA TIMEZONE
	 * @param timeZone - THIS IS THE TIMEZONE THAT YOU WANT TO CONVERT TO!!!! e.g Africa/Nairobi
	 * @return
	 * @throws ParseException
	 */
	public Date convertDateToDestinationTimezone(Date time, String timeZone) throws ParseException;
	
	/**
	 * 
	 * @param time
	 * @return
	 * @throws ParseException
	 */
	public Date convertToThisTimezone(String time) throws ParseException;
	

	/**
	 * 
	 * @param date
	 * @param dateformat
	 * @param timezone
	 * @return
	 * @throws ParseException
	 */
	public Date convertToThisTimezone(Date date, String dateformat, TimeZone timezone) throws ParseException;
	
	/**
	 * 
	 * @param time
	 * @param sourcetimeZone
	 * @return
	 * @throws ParseException
	 */
	public Date convertToHostTimezone(Date time, String sourcetimeZone) throws ParseException;
	
	/**
	 * 
	 * @param datestr
	 * @return
	 * @throws ParseException
	 */
	public String dateToString(Date date) throws ParseException;
	
	/**
	 * 
	 * @param fromDate
	 * @param fromTimezone
	 * @param toTimeZone
	 * @return
	 * @throws ParseException
	 */
	public Date convertFromOneTimeZoneToAnother(Date fromDate, String fromTimezone, String toTimeZone) throws ParseException;
	
	/**
	 * 
	 * @param fromDates
	 * @param fromTimezone
	 * @param toTimeZone
	 * @return
	 * @throws ParseException
	 */
	public Date convertFromOneTimeZoneToAnother(String fromDates, String fromTimezone, String toTimeZone) throws ParseException;
	
	/**
	 * 
	 * @param date
	 * @param fromTZ
	 * @param toTZ
	 * @return
	 */
	public java.util.Date convertFromOneTimeZoneToAnother(java.util.Date date, TimeZone fromTZ , TimeZone toTZ);
	
	/**
	 * 
	 * @param sheduledate_server_time
	 * @return
	 */
	public boolean isDateInThePast(Date sheduledate_server_time);
}
