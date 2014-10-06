package com.inmobia.axiata.reports.bean;

/**
 * 
 * RevenueBean
 * 
 * @author <a href="mailto:enter email address">Paul</a>
 * @version enter version, 26 Jun 2013
 * @since  enter jdk version
 */
public class RevenueBean {
	
	private String service;
	
	private double today;
	
	private double average;

	
	/**
	 * @return the service
	 */
	public String getService() {
	
		return service;
	}

	
	/**
	 * @param service the service to set
	 */
	public void setService(String service) {
	
		this.service = service;
	}
	
	/**
	 * @return the today
	 */
	public double getToday() {
	
		return today;
	}

	
	/**
	 * @param today the today to set
	 */
	public void setToday(double today) {
	
		this.today = today;
	}

	
	/**
	 * @return the average
	 */
	public double getAverage() {
	
		return average;
	}

	
	/**
	 * @param average the average to set
	 */
	public void setAverage(double average) {
	
		this.average = average;
	}
}