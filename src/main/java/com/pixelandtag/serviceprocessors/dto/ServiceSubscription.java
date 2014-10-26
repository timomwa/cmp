package com.pixelandtag.serviceprocessors.dto;

public class ServiceSubscription {
	
	private int id,serviceid;
	private String schedule,lastUpdated,ExpiryDate;
	public int getId() {
		return id;
	}
	public int getServiceid() {
		return serviceid;
	}
	public String getSchedule() {
		return schedule;
	}
	public String getLastUpdated() {
		return lastUpdated;
	}
	public String getExpiryDate() {
		return ExpiryDate;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setServiceid(int serviceid) {
		this.serviceid = serviceid;
	}
	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}
	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public void setExpiryDate(String expiryDate) {
		ExpiryDate = expiryDate;
	}
	

}
