package com.pixelandtag.dating.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "dating_chat_bundle")
public class ChatBundle implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6075140082187891634L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@Index(name="msisdccidx")
	@Column(name="msisdn", nullable=false, unique=true)
	private String msisdn;
	
	@Column(name="sms")
	private Long sms;
	
	
	@Column(name="expiryDate")
	@Index(name="exrycbdateidx")
	@Temporal(TemporalType.TIMESTAMP)
	private Date expiryDate;

	@PreUpdate
	@PrePersist
	public void onCreate(){
		if(sms==null)
			sms = 0L;
	}

	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getMsisdn() {
		return msisdn;
	}


	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}


	public Long getSms() {
		return sms!=null ? sms : 0L;
	}


	public void setSms(Long sms) {
		this.sms = sms;
	}


	public Date getExpiryDate() {
		return expiryDate;
	}


	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChatBundle [id=");
		builder.append(id);
		builder.append(", \nmsisdn=");
		builder.append(msisdn);
		builder.append(", \nsms=");
		builder.append(sms);
		builder.append(", \nexpiryDate=");
		builder.append(expiryDate);
		builder.append("]");
		return builder.toString();
	}
	
	

}
