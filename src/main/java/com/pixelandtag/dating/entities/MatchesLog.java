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
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "daily_match_log")
public class MatchesLog implements Serializable  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4922691891985063335L;


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER)
	@JoinColumn(name = "profile_id_fk")
	@Index(name="dmlcpidx")
	private PersonDatingProfile profile;
	
	
	@Column(name="timeStamp")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timeStamp;
	
	
	@PreUpdate
	@PrePersist
	public void init(){
		if(timeStamp==null)
			timeStamp = new Date();
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
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
