package com.pixelandtag.cmp.entities.audittools;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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

import com.pixelandtag.cmp.entities.customer.OperatorCountry;

@Entity
@Table(name = "latency_log")
public class LatencyLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 508683218436071621L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name = "opco_id_fk")
	@Index(name="llocpidx")
	private OperatorCountry opco;
	
	@Column(name="link", nullable=false)
	private String link;
	
	@Column(name = "latency")
	private Long latency;
	
	@Column(name = "timeStamp",nullable = false)
	@Index(name="tmeStmp_idx")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timeStamp;
	
	
	@PrePersist
	@PreUpdate
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


	public OperatorCountry getOpco() {
		return opco;
	}


	public void setOpco(OperatorCountry opco) {
		this.opco = opco;
	}


	public String getLink() {
		return link;
	}


	public void setLink(String link) {
		this.link = link;
	}


	public Long getLatency() {
		return latency;
	}


	public void setLatency(Long latency) {
		this.latency = latency;
	}


	public Date getTimeStamp() {
		return timeStamp;
	}


	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
}
