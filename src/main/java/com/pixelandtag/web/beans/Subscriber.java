package com.pixelandtag.web.beans;

import java.io.Serializable;

/**
 * Models a subscriber record
 * @author Timothy Mwangi Gikonyo
 *
 */
public class Subscriber implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2342351L;
	private String msisdn,name,last_action,last_teaser_id,last_teased,subscribed,teaser;
	private int id;
	private int language_id_=1;
	private int totalPoints;
	private int questionsAnsweredToday;
	private boolean has_reached_questions_quota_for_today,continuation_confirmed;
	private String teaserKey;
	private int idle_hours;
	private int mins_since_teased,hours_since_teased;
	
	
	
	
	public int getHours_since_teased() {
		return hours_since_teased;
	}

	public void setHours_since_teased(int hours_since_teased) {
		this.hours_since_teased = hours_since_teased;
	}

	public int getMins_since_teased() {
		return mins_since_teased;
	}

	public void setMins_since_teased(int mins_since_teased) {
		this.mins_since_teased = mins_since_teased;
	}

	public int getIdle_hours() {
		return idle_hours;
	}

	public void setIdle_hours(int idle_hours) {
		this.idle_hours = idle_hours;
	}

	public String getTeaserKey() {
		return teaserKey;
	}

	public void setTeaserKey(String teaserKey) {
		this.teaserKey = teaserKey;
	}

	public boolean isContinuation_confirmed() {
		return continuation_confirmed;
	}

	public void setContinuation_confirmed(boolean continuation_confirmed) {
		this.continuation_confirmed = continuation_confirmed;
	}

	public boolean isHas_reached_questions_quota_today() {
		return has_reached_questions_quota_for_today;
	}

	public void setHas_reached_questions_quota_for_today(boolean has_reached_questions_quota) {
		this.has_reached_questions_quota_for_today = has_reached_questions_quota;
	}

	public String getTeaser() {
		return teaser;
	}

	public void setTeaser(String teaser) {
		this.teaser = teaser;
	}

	public int getQuestionsAnsweredToday() {
		return questionsAnsweredToday;
	}

	public void setQuestionsAnsweredToday(int questionsAnsweredToday) {
		this.questionsAnsweredToday = questionsAnsweredToday;
	}

	public int getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(int totalPoints) {
		this.totalPoints = totalPoints;
	}

	public String getLast_teased() {
		return last_teased;
	}

	public void setLast_teased(String last_teased) {
		this.last_teased = last_teased;
	}

	public int getLanguage_id_() {
		return language_id_;
	}

	public void setLanguage_id_(int language_id_) {
		this.language_id_ = language_id_;
	}

	private boolean active;
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	

	

	@Override
	public String toString() {
		return "Subscriber [msisdn=" + msisdn + ", name=" + name
				+ ", last_action=" + last_action + ", last_teaser_id="
				+ last_teaser_id + ", last_teased=" + last_teased
				+ ", subscribed=" + subscribed + ", teaser=" + teaser + ", id="
				+ id + ", language_id_=" + language_id_ + ", totalPoints="
				+ totalPoints + ", questionsAnsweredToday="
				+ questionsAnsweredToday
				+ ", has_reached_questions_quota_for_today="
				+ has_reached_questions_quota_for_today
				+ ", continuation_confirmed=" + continuation_confirmed
				+ ", teaserKey=" + teaserKey + ", idle_hours=" + idle_hours
				+ ", mins_since_teased=" + mins_since_teased + ", active="
				+ active + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLast_action() {
		return last_action;
	}

	public void setLast_action(String last_action) {
		this.last_action = last_action;
	}

	public String getLast_teaser_id() {
		return last_teaser_id;
	}

	public void setLast_teaser_id(String last_teaser_id) {
		this.last_teaser_id = last_teaser_id;
	}

	public String getSubscribed() {
		return subscribed;
	}

	public void setSubscribed(String subscribed) {
		this.subscribed = subscribed;
	}
	
	

}
