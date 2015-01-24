package com.pixelandtag.dating.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "dating_personDatingProfile")
public class PersonDatingProfile  implements Serializable  {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "username")
	@Index(name="usrnameIdx")
	private String username;
	
	@Column(name="person_id_fk", nullable=false)
	@Index(name="psnidfkidx")
	private Person person;
	
	@Column(name = "pref_gender")
	private Gender preferred_gender;
	
	@Column(name = "prefd_age")
	private BigDecimal preferred_age;
	
	@Column(name = "location")
	private String location;
	
	@Column(name = "prefd_location")
	private String preferred_location;
	
	@Column(name = "dob")
	@Index(name="psnidfkidx")
	private Date dob;

	
	@Column(name="language_id")
	private int language_id;
	
	
	@Column(name = "gender")
	@Enumerated(EnumType.STRING)
    private Gender gender;

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

	public String getPreferred_location() {
		return preferred_location;
	}

	public void setPreferred_location(String preferred_location) {
		this.preferred_location = preferred_location;
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
	
	
	
	
}
