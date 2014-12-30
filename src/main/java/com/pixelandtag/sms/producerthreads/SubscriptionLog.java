package com.pixelandtag.sms.producerthreads;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "subscriptionlog")
public class SubscriptionLog implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	//@Temporal(TemporalType.TIMESTAMP)
	//@Column(name = "updated", nullable = false)
//	private Date updated;
	 
	@Column(name = "msisdn")
	@Index(name="msisdn_idx")
	private String msisdn;
	
	//`pixeland_content360`.`servicesubscription`
	@Column(name = "service_subscription_id")
	@Index(name="servsub_idx")
	private int service_subscription_id;
	
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timeStamp", nullable = false)
	@Index(name="sblogtmstp_idx")
	private Date timeStamp;
	
	//`pixeland_content360`.`subscription`
	@Column(name = "subscription_id")
	@Index(name="subLog_idx")
	private Long subscription_id;
	
	@PrePersist
	public void onCreate(){
		if(timeStamp==null)
			timeStamp = new Date();
		//if(updated==null)
		//	updated = new Date();
	}

	// @PreUpdate
	// public void onUpdate() {
	  //  updated = new Date();
	// }
	 
	 
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public int getService_subscription_id() {
		return service_subscription_id;
	}

	public void setService_subscription_id(int service_subscription_id) {
		this.service_subscription_id = service_subscription_id;
	}

	public Date getTimeStamp() {
		if(timeStamp==null)
			timeStamp = new Date();
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Long getSubscription_id() {
		return subscription_id;
	}

	public void setSubscription_id(Long subscription_id) {
		this.subscription_id = subscription_id;
	}
	

}
