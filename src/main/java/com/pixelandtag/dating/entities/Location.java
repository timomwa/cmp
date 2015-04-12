package com.pixelandtag.dating.entities;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
/**
 * When the operator sends us the location
 * details.
 * @author Timothy Mwangi
 * Date: 12th April 2015
 *
 */
@Entity
@Table(name = "dating_location")
public class Location implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1385489930038558955L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	/**
	 * External id 
	 * Operat's location id
	 */
	@Column(name = "location_id", nullable = false)
	@Index(name="locIdx")
	private Long location_id;
	
	@Column(name = "cellid", nullable = false)
	@Index(name="cellIdx")
	private Long cellid;
	
	@Column(name = "locationName")
	@Index(name="locNameIdx")
	private String locationName;
	
	/**
	 * Date of recording
	 */
	@Column(name = "timeStamp", nullable = false)
	private Date timeStamp;
	
	@PrePersist
	public void onCreate(){
		if(timeStamp==null)
			timeStamp = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getLocation_id() {
		return location_id;
	}

	public void setLocation_id(Long location_id) {
		this.location_id = location_id;
	}

	public Long getCellid() {
		return cellid;
	}

	public void setCellid(Long cellid) {
		this.cellid = cellid;
	}
	
	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	

}
