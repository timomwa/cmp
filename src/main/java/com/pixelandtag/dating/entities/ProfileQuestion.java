package com.pixelandtag.dating.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dating_profileQuestions")
public class ProfileQuestion  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8566143399004033296L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="question")
	private String question;
	
	@Column(name="version")
	private Long version;
	
	@Column(name="serial")
	private Long serial;
	
	
	@Column(name="language_id")
	private Long language_id;
	
	@Column(name="active")
	private Boolean active;
	
	
	@Enumerated(EnumType.STRING)
	@Column(name="attrib")
	private ProfileAttribute attrib;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Long getSerial() {
		return serial;
	}

	public void setSerial(Long serial) {
		this.serial = serial;
	}

	public Long getLanguage_id() {
		return language_id;
	}

	public void setLanguage_id(Long language_id) {
		this.language_id = language_id;
	}

	public ProfileAttribute getAttrib() {
		return attrib;
	}

	public void setAttrib(ProfileAttribute attrib) {
		this.attrib = attrib;
	}
	
	

}
