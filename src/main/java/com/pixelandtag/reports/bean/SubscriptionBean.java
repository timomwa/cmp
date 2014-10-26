package com.pixelandtag.reports.bean;

/**
 * 
 * SubscriptionBean
 * 
 * @author <a href="mailto:enter email address">Paul</a>
 * @version enter version, 26 Jun 2013
 * @since  enter jdk version
 */
public class SubscriptionBean {
	
	private String date;
	
	private long subscribers;
	
	private long unsubscribers;
	
	private long totalSubscribers;

	
	/**
	 * @return the subscribers
	 */
	public long getSubscribers() {
	
		return subscribers;
	}

	
	/**
	 * @param subscribers the subscribers to set
	 */
	public void setSubscribers(long subscribers) {
	
		this.subscribers = subscribers;
	}

	
	/**
	 * @return the unsubscribers
	 */
	public long getUnsubscribers() {
	
		return unsubscribers;
	}

	
	/**
	 * @param unsubscribers the unsubscribers to set
	 */
	public void setUnsubscribers(long unsubscribers) {
	
		this.unsubscribers = unsubscribers;
	}


	
	/**
	 * @return the totalSubscribers
	 */
	public long getTotalSubscribers() {
	
		return totalSubscribers;
	}


	
	/**
	 * @param totalSubscribers the totalSubscribers to set
	 */
	public void setTotalSubscribers(long totalSubscribers) {
	
		this.totalSubscribers = totalSubscribers;
	}


	
	/**
	 * @return the date
	 */
	public String getDate() {
	
		return date;
	}


	
	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
	
		this.date = date;
	}
}