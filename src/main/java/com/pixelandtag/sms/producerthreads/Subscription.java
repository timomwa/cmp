package com.pixelandtag.sms.producerthreads;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

import com.pixelandtag.subscription.dto.MediumType;
import com.pixelandtag.subscription.dto.SubscriptionStatus;

@Entity
@Table(name = "subscription")
public class Subscription implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="subscription_status")
	@Enumerated(EnumType.STRING)
	private SubscriptionStatus subscription_status;
	
	
	@Column(name="sms_service_id_fk")
	private Long sms_service_id_fk;
	
	@Column(name="msisdn")
	private String msisdn;
	
	@Column(name="subscription_timeStamp")
	@Index(name="tstmpIDX")
	@Temporal(TemporalType.TIMESTAMP)
	private Date subscription_timeStamp;
	
	@Column(name="smsmenu_levels_id_fk")
	private int smsmenu_levels_id_fk;
	
	@Column(name="request_medium")
	@Enumerated(EnumType.STRING)
	private MediumType request_medium;
	
	
	@PrePersist
	@PreUpdate
	public void setDefaults(){
		if(subscription_timeStamp==null)
			subscription_timeStamp = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SubscriptionStatus getSubscription_status() {
		return subscription_status;
	}

	public void setSubscription_status(SubscriptionStatus subscription_status) {
		this.subscription_status = subscription_status;
	}

	public Long getSms_service_id_fk() {
		return sms_service_id_fk;
	}

	public void setSms_service_id_fk(Long sms_service_id_fk) {
		this.sms_service_id_fk = sms_service_id_fk;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public Date getSubscription_timeStamp() {
		return subscription_timeStamp;
	}

	public void setSubscription_timeStamp(Date subscription_timeStamp) {
		this.subscription_timeStamp = subscription_timeStamp;
	}

	public int getSmsmenu_levels_id_fk() {
		return smsmenu_levels_id_fk;
	}

	public void setSmsmenu_levels_id_fk(int smsmenu_levels_id_fk) {
		this.smsmenu_levels_id_fk = smsmenu_levels_id_fk;
	}

	public MediumType getRequest_medium() {
		return request_medium;
	}

	public void setRequest_medium(MediumType request_medium) {
		this.request_medium = request_medium;
	}
	
	
	
}
