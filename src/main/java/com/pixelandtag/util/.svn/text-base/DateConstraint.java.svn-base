package com.inmobia.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;





/**
 * This class deals with comparing
 * date ranges, 
 * 
 * 1. Used do determine whether a given
 *    date falls within two dates.
 * 
 * 2. Used to determine whether a given
 *    date falls before or after a given 
 *    date
 *    
 * @author Timothy Mwangi Gikonyo 
 * Date Created ... 2010 June
 *
 */
public class DateConstraint {

	protected final Logger logger = Logger.getLogger(DateConstraint.class);
	
	/**
	 * Beginning of your date range  (Has getters and setters)
	 */
	private GregorianCalendar from = null;
	
	
	/**
	 * End of your date range (Has getters and setters)
	 */
	private GregorianCalendar to = null;
	
	
	private GregorianCalendar limitDate = null;
	
	private GregorianCalendar exactDate = null;
	
	


	/**
	 * If you only set the 'from' value, then
	 * DO NOT FORGET!!!! to set isRange to false
	 * Since you do not want your date to be
	 * sought to be in a given range...
	 */
	private boolean isRange;

	private boolean validDate = true;

	public boolean isValidDate() {
		return validDate;
	}


	public void setValidDate(boolean validDate) {
		this.validDate = validDate;
	}


	/**
	 * Set the range right
	 * 
	 * @param date
	 * @return
	 */
	public boolean fallsWithinRange(GregorianCalendar date) {
		
				
				if ((this.from.compareTo(date) < 0) && (this.to.compareTo(date) > -1)) {
				
					return true;
				
				} else {
					
					return false;
				
				}
			
		
	}

	
	/**
	 * Will return true if date supplied in the parameter
	 * falls before the set 'to' or limit date.
	 * 
	 * e.g if you the set to is 27th July 2010,
	 * and the date you pass is 26th July 2010,
	 * 
	 * then this method will return true because this 
	 * date falls BEFORE the 'to' or limit date.
	 * dateIsBeforeLimitDate
	 * @param date
	 * @return
	 */
	public boolean dateIsBeforeLimitDate(GregorianCalendar date){
		
		if(this.limitDate.compareTo(date)==1){
		
			return true;
		
		}else{
			
			return false;
		
		}
		
		
	
	}
	
	
	/**
	 * Will only return true if the given date
	 * 
	 * is the same value as the exactDate value;
	 * 
	 * @param date the date to be compared
	 * 
	 * @return true if they are the same, false if not
	 */
	public boolean isTheSameAs(GregorianCalendar date){
		
		this.exactDate.set(Calendar.MINUTE, 0);
		this.exactDate.set(Calendar.SECOND, 0);
		this.exactDate.set(Calendar.HOUR, 0);
		this.exactDate.set(Calendar.AM, Calendar.AM);
		
		int dateExact = exactDate.get(Calendar.DATE);
		int monthExact = exactDate.get(Calendar.MONTH);
		int yearExact = exactDate.get(Calendar.YEAR);
		
		
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.HOUR, 0);
		date.set(Calendar.AM, Calendar.AM);

		
		int dateDate = date.get(Calendar.DATE);
		int monthDate = date.get(Calendar.MONTH);
		int yearDate = date.get(Calendar.YEAR);
		
		
		
		if(dateExact==dateDate && monthExact==monthDate &&  yearExact==yearDate){
		
			return true;
		}
		
		
		
		
		
		if(this.isValidDate()==false){
			
			return true;
		}	
	
		if(this.exactDate.getTime().toString().equalsIgnoreCase(date.getTime().toString())){
	
			
			return true;
		
		}else{
		
			return false;
		
		}
		
	}
	
	
	
	/**
	 * Checks if a given date qualifies
	 * as per the set limits or ranges
	 * @param date
	 * @return
	 */
	public boolean qualifies(GregorianCalendar date){
		
		if (this.isRange) {
			
	
			
			return fallsWithinRange(date); 
		
		}else{
			
			return isTheSameAs(date);
		
		}
		
		
	}
	
	
	/**
	 * Checks if a given date qualifies
	 * as per the set limits or ranges
	 * @param date
	 * @return
	 */
	public boolean qualifies(String dateString){
		
		
		GregorianCalendar date = extractDate(dateString);
		
		if (this.isRange) {
			
	
			
			return fallsWithinRange(date); 
		
		}else{
			
			return isTheSameAs(date);
		
		}
		
		
	}
	
	
	/**
	 * Checks if a given date qualifies
	 * as per the set limits or ranges
	 * @param date
	 * @return
	 */
	public boolean qualifies(Date pdate){
		
		GregorianCalendar date = extractDate(pdate);
		
		if (this.isRange) {
			
		
			
			return fallsWithinRange(date); 
		
		}else{
			
			return isTheSameAs(date);
		
		}
		
		
	}
	
	
	
	
	/**
	 * Extracts the date and returns it in form of
	 * a GregorianCalendar
	 * 
	 * The date string MUST be in the following format
	 * YYYY-MM-DD HH:MM:SS
	 * 
	 * @param dateString a datestring
	 * @return GregorianCalendar
	 */
	public GregorianCalendar extractDate(String dateString){
		
		String[] splited = dateString.split("[ ]");
		
		String dateStr = splited[0];
		
		String[] components = dateStr.split("[-]");
		
		int year = Integer.valueOf(components[0]);
		
		int month = Integer.valueOf(components[1]);
		
		int dayOfMonth = Integer.valueOf(components[2]);
		
		
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		
		gregorianCalendar.set(Calendar.DATE, dayOfMonth);

		gregorianCalendar.set(Calendar.MONTH, month-1);

		gregorianCalendar.set(Calendar.YEAR, year);
		
		return gregorianCalendar;
		
	}
	
	
	/**
	 * Extracts the date and returns it in form of
	 * a GregorianCalendar
	 * 
	 * The date string MUST be in the following format
	 * YYYY-MM-DD HH:MM:SS
	 * 
	 * @param dateString a datestring
	 * @return GregorianCalendar
	 */
	public GregorianCalendar extractDate(Date date){
		
	
		
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		
		gregorianCalendar.setTimeInMillis(date.getTime());
		
		
		return gregorianCalendar;
		
	}
	
	
	
	/**
	 * Operates with sql strings.
	 * 
	 * @param date an sql string date, probably from geting an sql datestring
	 * @param dayOffset what to add or subtract from the date. Has to be a day
	 * @return String adjusted sql date 
	 */
	public String addOrSubtractADays(String date, int dayOffset){
		
		GregorianCalendar gc = extractDate(date);
		
		
		gc.add(Calendar.DATE, dayOffset);
		
		//Date d = new Date(gc.getTimeInMillis());
		
		int dateExact = gc.get(Calendar.DATE);
		int monthExact = gc.get(Calendar.MONTH) + 1;
		int yearExact = gc.get(Calendar.YEAR);
		
		return yearExact +"-"+ (monthExact>9 ? "" : "0") +monthExact +"-"+ (dateExact>9 ? "" : "0") +dateExact;
		 
	}
	
	
	
	
	/**
	 * 
	 * @return
	 */
	public GregorianCalendar getExactDate() {
		return exactDate;
	}


	public void setExactDate(GregorianCalendar exactDate) {
		this.exactDate = exactDate;
	}
	
	/**
	 * The beginning of your range
	 * @return from
	 */
	public GregorianCalendar getFrom() {
		return from;
	}

	
	/**
	 * The end of your range
	 * @param from
	 */
	public void setFrom(GregorianCalendar from) {
		this.from = from;
	}

	/**
	 * Specify whether your two dates
	 * make up a range.
	 * 
	 * Set true if you intend to set
	 * two dates as a range.
	 * 
	 * @return isRange boolean
	 */
	public boolean isRange() {
		return isRange;
	}

	
	/**
	 * Set the range
	 * @param isRange
	 */
	public void setRange(boolean isRange) {
		this.isRange = isRange;
	}

	/**
	 * 
	 * @return to
	 */
	public GregorianCalendar getTo() {
		return to;
	}

	
	/**
	 * set the limit
	 * 
	 * @param to the limit of your range
	 * 
	 */
	public void setTo(GregorianCalendar to) {
		this.to = to;
	}
	
	
	public GregorianCalendar getLimitDate() {
		return limitDate;
	}


	public void setLimitDate(GregorianCalendar limitDate) {
		this.limitDate = limitDate;
	}


	
	public void validDate(boolean b) {
		this.validDate = b;
		
	}


	/**
	 * Takes in an sql date and compares this to the comparison 
	 * set date
	 * 
	 * @param gameTime
	 * @return
	 */
	public boolean isTheSameAs(String gameTime) {
		return isTheSameAs(extractDate(gameTime));
	}


	
	/**
	 * Sets the exact date constraint
	 * 
	 * @param ut the exact date, has to be sql string
	 */
	public void setExactDate(String ut) {
		this.setExactDate(extractDate(ut));
	}

}
