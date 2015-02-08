package com.pixelandtag.dating.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "subscription_history")
public class SubscriptionHistory implements Serializable {

	private static final long serialVersionUID = 7934579709972012118L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timeStamp", nullable = false)
	@Index(name = "subhistidx")
	private Date timeStamp;

	@Column(name = "service_id", nullable = false)
	@Index(name = "subhistidx")
	private Long service_id;

	@Column(name = "msisdn", nullable = false)
	@Index(name = "subhistidx")
	private String msisdn;

	@Column(name = "event")
	private Long event;

	@PrePersist
	public void onCreate() {
		if (timeStamp == null)
			timeStamp = new Date();
		if (event == null)
			event = 0L;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Long getService_id() {
		return service_id;
	}

	public void setService_id(Long service_id) {
		this.service_id = service_id;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public Long getEvent() {
		return event;
	}

	public void setEvent(Long event) {
		this.event = event;
	}
	
	
}
