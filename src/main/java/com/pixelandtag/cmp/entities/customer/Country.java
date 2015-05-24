package com.pixelandtag.cmp.entities.customer;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "country")
public class Country implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4085675538577916701L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="name", nullable=false, unique=true)
	private String name;
	
	@Column(name="isdcode", nullable=false, unique=true)
	private String isdcode;
	
	@Column(name="isocode")
	private String isocode;
	
	
	@Column(name="timeZone", nullable=false, unique=true)
	private String timeZone;

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

	public String getIsdcode() {
		return isdcode;
	}

	public void setIsdcode(String isdcode) {
		this.isdcode = isdcode;
	}

	public String getIsocode() {
		return isocode;
	}

	public void setIsocode(String isocode) {
		this.isocode = isocode;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	
	
	
	

}
