package com.pixelandtag.dating.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.Index;


@Entity
@Table(name = "dating_profileloc")
public class ProfileLocation implements Serializable  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1481783954398239839L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(cascade=CascadeType.MERGE, fetch=FetchType.EAGER)
	@JoinColumn(name = "location_id_fk")
	@Index(name="plocidx")
	private Location location;
	
	@ManyToOne(cascade=CascadeType.MERGE, fetch=FetchType.EAGER)
	@JoinColumn(name = "profile_id_fk")
	@Index(name="plocpidx")
	private PersonDatingProfile profile;
	
	/**
	 * Date of recording
	 */
	@Column(name = "timeStamp", nullable = false)
	@Index(name="tsidx")
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

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public PersonDatingProfile getProfile() {
		return profile;
	}

	public void setProfile(PersonDatingProfile profile) {
		this.profile = profile;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

}
