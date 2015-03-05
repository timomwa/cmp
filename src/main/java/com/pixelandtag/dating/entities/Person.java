package com.pixelandtag.dating.entities;

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

import org.hibernate.annotations.Index;

@Entity
@Table(name = "dating_person")
public class Person implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6468092870191452447L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Index(name="msisdnidx")
	@Column(name="msisdn")
	private String msisdn;
	
	
	@Index(name="active")
	@Column(name="active")
	private Boolean active;
	
	
	@Index(name="agreed_to_tnc")
	@Column(name="agreed_to_tnc")
	private Boolean agreed_to_tnc;
	
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(agreed_to_tnc==null)
			agreed_to_tnc = new Boolean(false);
	}

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

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getAgreed_to_tnc() {
		if(agreed_to_tnc==null)
			return new Boolean(false);
		return agreed_to_tnc;
	}

	public void setAgreed_to_tnc(Boolean agreed_to_tnc) {
		this.agreed_to_tnc = agreed_to_tnc;
	}
	
	
	
	
	
}
