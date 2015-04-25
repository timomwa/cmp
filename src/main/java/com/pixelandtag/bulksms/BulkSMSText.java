package com.pixelandtag.bulksms;

import java.io.Serializable;
import java.math.BigInteger;
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

import com.pixelandtag.cmp.entities.TimeUnit;

@Entity
@Table(name = "bulksms_text")
public class BulkSMSText implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5631964578123181948L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name = "plan_id_fk")
	@Index(name="txtplnidx")
	private BulkSMSPlan plan;
	
	@Column(name = "timecreated")
	@Temporal(TemporalType.TIMESTAMP)
	@Index(name="txtpidx")
	private Date timecreated;
	
	@Column(name = "schedule_tz")
	private String scheduletimezone;
	
	
	@Column(name = "queuesize", precision=0,scale=19)
	private BigInteger queueSize;
	
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(timecreated==null)
			timecreated = new Date();
		if(queueSize==null)
			queueSize = BigInteger.ZERO;
	}
	
	/**
	 * shortcode
	 */
	@Column(name = "senderid")
	private String senderid;
	
	
	@Column(name = "content",length=720)
	private String content;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BulkSMSPlan getPlan() {
		return plan;
	}

	public void setPlan(BulkSMSPlan plan) {
		this.plan = plan;
	}

	public Date getTimecreated() {
		return timecreated;
	}

	public void setTimecreated(Date timecreated) {
		this.timecreated = timecreated;
	}

	public String getSenderid() {
		return senderid;
	}

	public void setSenderid(String senderid) {
		this.senderid = senderid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public BigInteger getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(BigInteger queueSize) {
		this.queueSize = queueSize;
	}

	
	
	
}
