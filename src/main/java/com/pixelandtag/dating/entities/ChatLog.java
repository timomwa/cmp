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
@Table(name = "dating_chatlog")
public class ChatLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8055068082942786404L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "source_person_id", nullable = false)
	@Index(name="sourcePidx")
	private Long source_person_id;
	
	@Column(name = "dest_person_id", nullable = false)
	@Index(name="destpIdx")
	private Long dest_person_id;
	
	@Column(name = "message")
	private String message;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timeStamp", nullable = false)
	@Index(name="chltsmpidx")
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

	public Long getSource_person_id() {
		return source_person_id;
	}

	public void setSource_person_id(Long source_person_id) {
		this.source_person_id = source_person_id;
	}

	public Long getDest_person_id() {
		return dest_person_id;
	}

	public void setDest_person_id(Long dest_person_id) {
		this.dest_person_id = dest_person_id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	
	
}
