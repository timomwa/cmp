package com.pixelandtag.billing;

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

import com.pixelandtag.cmp.entities.customer.configs.ProfileType;

@Entity
@Table(name = "biller_profile")
public class BillerProfile implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6486682388879963407L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="name", unique=true, nullable=false)
	@Index(name="bprofidx")
	private String name;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "effectiveDate", nullable = false)
	@Index(name="bopccoefdidx")
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


	@Override
	public String toString() {
		return "BillerProfile [id=" + id + ", name=" + name
				+ ", effectiveDate=" + effectiveDate + ", profiletype="
				+ profiletype + ", active=" + active + "]";
	}

}
