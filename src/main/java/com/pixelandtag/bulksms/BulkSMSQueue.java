package com.pixelandtag.bulksms;

import java.io.Serializable;
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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import com.pixelandtag.api.MTStatus;
import com.pixelandtag.cmp.entities.TimeUnit;

@NamedQueries({
	@NamedQuery(
	name = BulkSMSQueue.NAMED_QUERY,
	query = "SELECT bs from BulkSMSQueue bs WHERE bs.retrycount<bs.max_retries AND bs.status IN  (:statuses) AND "
			+ " year(bs.text.sheduledate)=year(:sheduledate) AND "
			+ " month(bs.text.sheduledate)=month(:sheduledate) AND "
			+ " day(bs.text.sheduledate)=day(:sheduledate) AND "
			+ " hour(bs.text.sheduledate)>=hour(:sheduledate) AND "
			+ " minute(bs.text.sheduledate)>=minute(:sheduledate) "
			+ " ORDER BY bs.timelogged asc, bs.priority asc"
	)
})
@Entity
@Table(name = "bulksms_queue")
public class BulkSMSQueue implements Serializable{

	private static final long serialVersionUID = -5878865129157812878L;
	@Transient
	public static final String NAMED_QUERY = "bulksmsQueue.getByStatus";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name = "txt_id_fk")
	private BulkSMSText text;
	
	@Column(name = "msisdn")
	@Index(name="logmsidnidx")
	private String msisdn;
	
	@Column(name = "bulktxId")
	@Index(name="logblkidx")
	private String bulktxId;
	
	@Column(name = "cptxId")
	@Index(name="logcpidx")
	private String cptxId;
	
	@Column(name = "priority")
	@Index(name="logstsidx")
	private Integer priority;
	
	
	@Column(name = "retrycount")
	private Integer retrycount;
	
	@Column(name = "max_retries")
	private Integer max_retries;
	
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	@Index(name="logstsidx")
	private MTStatus status;
	
	@Column(name = "timelogged")
	@Temporal(TemporalType.TIMESTAMP)
	@Index(name="logstsidx")
	private Date timelogged;
	
	@Column(name = "validity")
	private Integer validity;
	
	@Column(name = "timeunit")
	@Enumerated(EnumType.STRING)
	private TimeUnit timeunit;
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(timelogged==null)
			timelogged = new Date();
		if(status==null)
			status = MTStatus.RECEIVED;
		if(validity==null){
			validity = 365;
			timeunit = TimeUnit.DAY;
		}
		if(retrycount==null)
			retrycount = Integer.valueOf(0);
		if(max_retries==null)
			max_retries = Integer.valueOf(5);
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BulkSMSText getText() {
		return text;
	}

	public void setText(BulkSMSText text) {
		this.text = text;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getCptxId() {
		return cptxId;
	}

	public void setCptxId(String cptxId) {
		this.cptxId = cptxId;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public MTStatus getStatus() {
		return status;
	}

	public void setStatus(MTStatus status) {
		this.status = status;
	}

	public Date getTimelogged() {
		return timelogged;
	}

	public void setTimelogged(Date timelogged) {
		this.timelogged = timelogged;
	}

	public String getBulktxId() {
		return bulktxId;
	}

	public void setBulktxId(String bulktxId) {
		this.bulktxId = bulktxId;
	}

	public Integer getValidity() {
		return validity;
	}

	public void setValidity(Integer validity) {
		this.validity = validity;
	}

	public TimeUnit getTimeunit() {
		return timeunit;
	}

	public void setTimeunit(TimeUnit timeunit) {
		this.timeunit = timeunit;
	}

	public Integer getRetrycount() {
		return ((retrycount==null) ? 0 : retrycount);
	}

	public void setRetrycount(Integer retrycount) {
		this.retrycount = retrycount;
	}

	public Integer getMax_retries() {
		return max_retries;
	}

	public void setMax_retries(Integer max_retries) {
		this.max_retries = max_retries;
	}
	
}
