package com.pixelandtag.cmp.entities.customer.configs;

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
@Table(name = "sender_profiles")
public class SenderProfile implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1706699828940876203L;


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="name", unique=true, nullable=false)
	@Index(name="profidx")
	private String name;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "effectiveDate", nullable = false)
	@Index(name="opcconfidx")
	private Date effectiveDate;
	
	
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
		return "SenderProfile [id=" + id + ", name=" + name
				+ ", effectiveDate=" + effectiveDate + ", active=" + active
				+ "]";
	}
	
	
	
}
