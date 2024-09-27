package com.pixelandtag.cmp.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="timezoneConfig")
public class TimeZoneConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2035787955592682577L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="utc_offset", nullable=false)
	private String utcOffset;
	
	@Column(name="enabled", nullable=false)
	private Boolean enabled;
	
	@Column(name="timeZone", nullable=false, unique=true)
	private String timeZone;//ISO timezone
	
	@Column(name="effectiveDate", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date effectiveDate;
	
	
	public void update(){
		if(effectiveDate==null)
			effectiveDate = new Date();
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getUtcOffset() {
		return utcOffset;
	}


	public void setUtcOffset(String utcOffset) {
		this.utcOffset = utcOffset;
	}


	public String getTimeZone() {
		return timeZone;
	}


	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}


	public Date getEffectiveDate() {
		return effectiveDate;
	}


	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}


	public Boolean getEnabled() {
		return enabled;
	}


	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TimeZoneConfig [id=");
		builder.append(id);
		builder.append(", \nutcOffset=");
		builder.append(utcOffset);
		builder.append(", \nenabled=");
		builder.append(enabled);
		builder.append(", \ntimeZone=");
		builder.append(timeZone);
		builder.append(", \neffectiveDate=");
		builder.append(effectiveDate);
		builder.append("]");
		return builder.toString();
	}


	

}
