package com.pixelandtag.cmp.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

import org.hibernate.annotations.Index;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.sms.producerthreads.EventType;

@MappedSuperclass
public class GenericMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3693648989661851547L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	

	@Column(name="shortcode")
	private String shortcode;
		
	@Column(name="msisdn")
	private String msisdn;
	
	@Index(name="timestamp")
	private Date timestamp;
	
	@Index(name="osmopco_tx_id")
	@Column(name="opco_tx_id")
	private String opco_tx_id;
		
	@Index(name="osmcmp_tx_id")
	@Column(name="cmp_tx_id")
	private String cmp_tx_id;	

	@Column(name="sms",length=1000)
	private String sms;
	
	@Column(name="billing_status")
	@Enumerated(EnumType.STRING)
	private BillingStatus billing_status;
			
	@Column(name="price")
	private BigDecimal price;
	
	@Column(name="serviceid")
	private Long serviceid;
	
	@Column(name="split")
	private Boolean split;
	
	@Column(name="event_type", nullable=false)
	private String event_type;
	
	@Column(name="price_point_keyword")
	private String price_point_keyword;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="processor_id", nullable=false)
	private MOProcessor moprocessor;
	
	@Column(name="isSubscription", nullable=false)
	private Boolean isSubscription;
	
	@PrePersist
	public void onCreate(){
		if(isSubscription==null)
			isSubscription = Boolean.FALSE;
		if(split==null)
			split = Boolean.FALSE;
		if(price==null)
			price = BigDecimal.ZERO;
		if(timestamp==null)
			timestamp = new Date() ;
		if(billing_status==null)
			billing_status = BillingStatus.NO_BILLING_REQUIRED;
		if(event_type==null)
			event_type = EventType.CONTENT_PURCHASE.getName();
		
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
	
	public String getCmp_tx_id() {
		return cmp_tx_id;
	}

	public void setCmp_tx_id(String cmp_tx_id) {
		this.cmp_tx_id = cmp_tx_id;
	}

	public String getOpco_tx_id() {
		return opco_tx_id;
	}

	public void setOpco_tx_id(String opco_tx_id) {
		this.opco_tx_id = opco_tx_id;
	}
	
	public BillingStatus getBilling_status() {
		return billing_status;
	}

	public void setBilling_status(BillingStatus billing_status) {
		this.billing_status = billing_status;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}


	public BigDecimal getPrice() {
		return price;
	}


	public void setPrice(BigDecimal price) {
		this.price = price;
	}


	public Long getServiceid() {
		return serviceid;
	}


	public void setServiceid(Long serviceid) {
		this.serviceid = serviceid;
	}
	
	public String getSms() {
		return sms;
	}

	public void setSms(String sms) {
		this.sms = sms;
	}


	public String getShortcode() {
		return shortcode;
	}


	public void setShortcode(String shortcode) {
		this.shortcode = shortcode;
	}
	
	public Boolean getSplit() {
		return split;
	}

	public void setSplit(Boolean split) {
		this.split = split;
	}


	public String getEvent_type() {
		return event_type;
	}


	public void setEvent_type(String event_type) {
		this.event_type = event_type;
	}


	public String getPrice_point_keyword() {
		return price_point_keyword;
	}


	public void setPrice_point_keyword(String price_point_keyword) {
		this.price_point_keyword = price_point_keyword;
	}
	
	public MOProcessor getMoprocessor() {
		return moprocessor;
	}


	public void setMoprocessor(MOProcessor moprocessor) {
		this.moprocessor = moprocessor;
	}
	
	public Boolean getIsSubscription() {
		return isSubscription;
	}

	public void setIsSubscription(Boolean isSubscription) {
		this.isSubscription = isSubscription;
	}
	
}
