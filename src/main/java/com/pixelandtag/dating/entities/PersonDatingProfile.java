package com.pixelandtag.dating.entities;

import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "dating_profile")
public class PersonDatingProfile  implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5509201526978993702L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "username", unique=true, nullable=false)
	@Index(name="usrnameIdx")
	private String username;
	
	@JoinColumn(name="person_id_fk", nullable=false )
	@Index(name="psnidfkidx")
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Person person;
	
	@Column(name = "pref_gender")
	@Enumerated(EnumType.STRING)
    private Gender preferred_gender;
	
	@Column(name = "prefd_age")
	private BigDecimal preferred_age;
	
	@Column(name = "location")
	private String location;
	
	
	@Column(name = "dob")
	@Index(name="psnidfkidx")
	private Date dob;

	
	@Column(name="language_id")
	private int language_id;
	
	
	@Column(name = "gender")
	@Enumerated(EnumType.STRING)
    private Gender gender;
	
	
	@Column(name = "profileComplete")
	private Boolean profileComplete;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationDate", nullable = false)
	private Date creationDate;
	
	@PrePersist
	public void onCreate(){
		if(profileComplete==null)
			profileComplete = new Boolean(false);
		if(creationDate==null)
			creationDate = new Date();
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Boolean getProfileComplete() {
		return profileComplete;
	}

	public void setProfileComplete(Boolean profileComplete) {
		this.profileComplete = profileComplete;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Gender getPreferred_gender() {
		return preferred_gender;
	}

	public void setPreferred_gender(Gender preferred_gender) {
		this.preferred_gender = preferred_gender;
	}

	public BigDecimal getPreferred_age() {
		return preferred_age;
	}

	public void setPreferred_age(BigDecimal preferred_age) {
		this.preferred_age = preferred_age;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public int getLanguage_id() {
		return language_id;
	}

	public void setLanguage_id(int language_id) {
		this.language_id = language_id;
	}

	@Override
	public String toString() {
		return "PersonDatingProfile [id=" + id + ", username=" + username
				+ ", person=" + person + ", preferred_gender="
				+ preferred_gender + ", preferred_age=" + preferred_age
				+ ", location=" + location + ", dob=" + dob + ", language_id="
				+ language_id + ", gender=" + gender + ", profileComplete="
				+ profileComplete + ", creationDate=" + creationDate + "]";
	}
	
	
	
}
