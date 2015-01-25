package com.pixelandtag.dating.entities;

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
@Table(name = "dating_questionLog")
public class QuestionLog implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4398447518059686224L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
		
	@Column(name="question_id_fk")
	private Long question_id_fk;
	
	@Column(name="profile_id_fk")
	@Index(name="profIdFkIdx")
	private Long profile_id_fk;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timeStamp", nullable = false)
	@Index(name="sblogtmstp_idx")
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

	public Long getQuestion_id_fk() {
		return question_id_fk;
	}

	public void setQuestion_id_fk(Long question_id_fk) {
		this.question_id_fk = question_id_fk;
	}

	public Long getProfile_id_fk() {
		return profile_id_fk;
	}

	public void setProfile_id_fk(Long profile_id_fk) {
		this.profile_id_fk = profile_id_fk;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	
	

}
