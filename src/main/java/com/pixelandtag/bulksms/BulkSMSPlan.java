package com.pixelandtag.bulksms;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

import com.pixelandtag.cmp.entities.TimeUnit;

@Entity
@Table(name = "bulksms_plan")
public class BulkSMSPlan implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7501083758988005779L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "no_of_sms")
	private BigInteger numberOfSMS;
	
	@Column(name = "date_purch")
	@Temporal(TemporalType.TIMESTAMP)
	private Date datePurchased;
	
	@Column(name = "validity")
	private Integer validity;
	
	@Column(name = "timeunit")
	@Enumerated(EnumType.STRING)
	private TimeUnit timeunit;
	
	@ManyToOne(cascade=CascadeType.MERGE, fetch=FetchType.EAGER)
	@JoinColumn(name = "account_id_fk")
	@Index(name="plnaccidx")
	private BulkSMSAccount account;
	
	@Column(name = "active")
	private Boolean active;
	
	@Column(name = "planid")
	@Index(name="plnaccidx")
	private String planid;
	
	
	@Column(name = "telcoid")
	@Index(name="plnaccidx")
	private String telcoid;
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(datePurchased==null)
			datePurchased = new Date();
		if(active==null)
			active = new Boolean(false);
		if(validity==null){
			validity = 365;
			timeunit = TimeUnit.DAY;
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigInteger getNumberOfSMS() {
		return numberOfSMS;
	}

	public void setNumberOfSMS(BigInteger numberOfSMS) {
		this.numberOfSMS = numberOfSMS;
	}

	public Date getDatePurchased() {
		return datePurchased;
	}

	public void setDatePurchased(Date datePurchased) {
		this.datePurchased = datePurchased;
	}
	
	public BulkSMSAccount getAccount() {
		return account;
	}

	public void setAccount(BulkSMSAccount account) {
		this.account = account;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Integer getValidity() {
		return validity;
	}

	public void setValidity(Integer validity) {
		this.validity = validity;
	}

	public TimeUnit getTimeunit() {
		return timeunit;
	}

	public void setTimeunit(TimeUnit timeunit) {
		this.timeunit = timeunit;
	}

	public String getPlanid() {
		return planid;
	}

	public void setPlanid(String planid) {
		this.planid = planid;
	}

	public String getTelcoid() {
		return telcoid;
	}

	public void setTelcoid(String telcoid) {
		this.telcoid = telcoid;
	}
	

}
