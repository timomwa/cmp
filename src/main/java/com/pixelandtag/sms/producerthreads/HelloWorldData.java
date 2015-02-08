package com.pixelandtag.sms.producerthreads;

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


@Entity
@Table(name = "hello_world_anthony")
public class HelloWorldData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 134560164169461752L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name="msisdn")
	private String msisdn;
	
	@Column(name="mo")
	private String mo;
	
	@Column(name = "timeStamp",nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date timeStamp;
	
	@PrePersist
	public void onCreate(){
		if(timeStamp==null)
			timeStamp = new Date();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getMo() {
		return mo;
	}

	public void setMo(String mo) {
		this.mo = mo;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	
	
	
}
