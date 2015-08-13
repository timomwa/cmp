package com.pixelandtag.dating.entities;

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

import org.hibernate.annotations.Index;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;

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
	
	
	@Column(name="loggedin")
	private Boolean loggedin;
	
	@ManyToOne(cascade=CascadeType.MERGE, fetch=FetchType.EAGER)
	@JoinColumn(name = "opco_id_fk")
	@Index(name="popcoidx")
	private OperatorCountry opco;
	
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(agreed_to_tnc==null)
			agreed_to_tnc = Boolean.FALSE;
		if(loggedin==null)
			loggedin = Boolean.TRUE;
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

	public Boolean getLoggedin() {
		return loggedin;
	}

	public void setLoggedin(Boolean loggedin) {
		this.loggedin = loggedin;
	}

	public OperatorCountry getOpco() {
		return opco;
	}

	public void setOpco(OperatorCountry opco) {
		this.opco = opco;
	}
	
	
	
	
	
}
