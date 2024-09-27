package com.pixelandtag.bulksms;

import java.io.Serializable;
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
@Table(name = "bulksms_ipwhl")
public class IPAddressWhitelist implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3384143057658445942L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "ipaddress", length=50)
	@Index(name="ipadwidx")
	private String ipaddress;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "accnt_id_fk")
	@Index(name="wlaccidx")
	private BulkSMSAccount account;

	@Column(name = "timecreated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timecreated;
	
	@Column(name = "validity")
	private Integer validity;
	
	@Column(name = "timeunit")
	@Enumerated(EnumType.STRING)
	private TimeUnit timeunit;
	
	@Column(name = "active")
	private Boolean active;
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(timecreated==null)
			timecreated = new Date();
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

	public String getIpaddress() {
		return ipaddress;
	}

	
	
	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}



	public Date getTimecreated() {
		return timecreated;
	}



	public void setTimecreated(Date timecreated) {
		this.timecreated = timecreated;
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



	public Boolean getActive() {
		return active;
	}



	public void setActive(Boolean active) {
		this.active = active;
	}

	public BulkSMSAccount getAccount() {
		return account;
	}

	public void setAccount(BulkSMSAccount account) {
		this.account = account;
	}
	
	
	
}
