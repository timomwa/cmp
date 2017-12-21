package com.pixelandtag.cmp.entities.customer.configs;

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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "sender_receiver_profile")
public class SenderReceiverProfile implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1706699828940876203L;


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="name", unique=true, nullable=false, length=50)
	@Index(name="profidx")
	private String name;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "effectiveDate", nullable = false)
	@Index(name="opccoefdidx")
	private Date effectiveDate;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private ProfileType profiletype;
	
	
	@Column(name = "active", nullable = false)
	private Boolean active;
	

	@PrePersist
	public void onCreate(){
		if(effectiveDate==null)
			effectiveDate = new Date();
		if(active==null)
			active = Boolean.TRUE;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Date getEffectiveDate() {
		return effectiveDate;
	}


	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}


	public Boolean getActive() {
		return active;
	}


	public void setActive(Boolean active) {
		this.active = active;
	}


	public ProfileType getProfiletype() {
		return profiletype;
	}


	public void setProfiletype(ProfileType profiletype) {
		this.profiletype = profiletype;
	}


	
	
}
