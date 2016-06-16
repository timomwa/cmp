package com.pixelandtag.sms.producerthreads;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

import com.pixelandtag.cmp.entities.BillingType;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;

@Entity
@Table(name = "success_billing")
public class SuccessfullyBillingRequests  implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5539052893235380900L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	/**
	 * HTTP status code
	 */
	@Column(name = "resp_status_code")
	private String resp_status_code;
	
	@Column(name = "price_point_keyword")
	private String pricePointKeyword;
	
	@Column(name = "price")
	private BigDecimal price;
	
	@Column(name = "transactionId",unique=true)
	private String transactionId;
	
	
	@Column(name = "cp_tx_id", unique=true)
	private String cp_tx_id;
	
	@Column(name = "operation")
	private String operation;
	
	@Column(name = "msisdn")
	private String msisdn;
	
	
	@Column(name = "shortcode")
	private String shortcode;
	
	
	@Column(name = "keyword")
	private String keyword;
	
	@ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumn(name = "opco_id_fk", nullable=false)
	@Index(name="sblocpidx")
	private OperatorCountry opco;
	
	/**
	 * HTTP status code
	 */
	@Column(name = "success")
	private Boolean success;
	
	@Column(name = "timeStamp",nullable = false)
	@Index(name="billi_idx")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timeStamp;
	
	@Column(name = "transferin")
	private Boolean transferin;
	
	
	@Column(name = "billingType",nullable = true)
	@Enumerated(EnumType.STRING)
	private BillingType billingType;
	
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(timeStamp==null)
			timeStamp = new Date();
		if(success==null)
			success = Boolean.FALSE;
		if(transferin==null)
			transferin = Boolean.FALSE;
		if(billingType==null)
			billingType = BillingType.MT_BILLING;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getResp_status_code() {
		return resp_status_code;
	}


	public void setResp_status_code(String resp_status_code) {
		this.resp_status_code = resp_status_code;
	}


	public String getPricePointKeyword() {
		return pricePointKeyword;
	}


	public void setPricePointKeyword(String pricePointKeyword) {
		this.pricePointKeyword = pricePointKeyword;
	}


	public BigDecimal getPrice() {
		return price;
	}


	public void setPrice(BigDecimal price) {
		this.price = price;
	}


	public String getTransactionId() {
		return transactionId;
	}


	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}


	public String getCp_tx_id() {
		return cp_tx_id;
	}


	public void setCp_tx_id(String cp_tx_id) {
		this.cp_tx_id = cp_tx_id;
	}


	public String getOperation() {
		return operation;
	}


	public void setOperation(String operation) {
		this.operation = operation;
	}


	public String getMsisdn() {
		return msisdn;
	}


	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}


	public String getShortcode() {
		return shortcode;
	}


	public void setShortcode(String shortcode) {
		this.shortcode = shortcode;
	}


	public String getKeyword() {
		return keyword;
	}


	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}


	public Boolean getSuccess() {
		return success;
	}


	public void setSuccess(Boolean success) {
		this.success = success;
	}


	public Date getTimeStamp() {
		return timeStamp;
	}


	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}


	public Boolean getTransferin() {
		return transferin;
	}


	public void setTransferin(Boolean transferin) {
		this.transferin = transferin;
	}


	public OperatorCountry getOpco() {
		return opco;
	}


	public void setOpco(OperatorCountry opco) {
		this.opco = opco;
	}


	public BillingType getBillingType() {
		return billingType;
	}


	public void setBillingType(BillingType billingType) {
		this.billingType = billingType;
	}
	
	
	

}
