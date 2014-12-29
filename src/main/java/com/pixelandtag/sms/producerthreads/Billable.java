package com.pixelandtag.sms.producerthreads;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;

import com.pixelandtag.entities.MTsms;


@Entity
@Table(name = "billable_queue")
public class Billable implements Serializable {

	

	@Id
	//@GenericGenerator(name="gen",strategy="increment")
	//@GeneratedValue(generator="gen")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4690931003011822080L;
	
	/**
	 * HTTP status code
	 */
	@Column(name = "resp_status_code")
	private String resp_status_code;
	
	
	
	/**
	 * The price point keyword
	 */
	@Column(name = "price_point_keyword")
	private String pricePointKeyword;
	
	
	/**
	 * HTTP status code
	 */
	@Column(name = "success")
	private Boolean success;
	
	/**
	 * the message id
	 */
	@Column(name = "message_id")
	private Long message_id;
	
	
	/**
	 * Boolean to mark the 
	 * record as in outgoing queue
	 */
	@Column(name = "in_outgoing_queue")
	@Index(name="outq_idx")
	private Long in_outgoing_queue;
	
	@PrePersist
	@PreUpdate
	public void setDefaults(){
		if(timeStamp==null)
			timeStamp = new Date();
		if(success==null)
			success = new Boolean(false);
		if(processed==null)
			processed = new Long(0);
	}
	
	/**
	 * MTs are more important than push subscriptions
	 */
	@Column(name = "priority")
	@Index(name="priority_idx")
	private Long priority;
	
	@Column(name = "timeStamp", insertable = false, updatable = false)
	@Index(name="timeStamp_idx")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timeStamp;
	
	@Column(name = "maxRetriesAllowed")
	private Long maxRetriesAllowed;
	
	@Column(name = "retry_count")
	private Long retry_count;
	
	@Column(name = "operation")
	private String operation;
	
	@Column(name = "msisdn")
	private String msisdn;
	
	
	@Column(name = "shortcode")
	private String shortcode;
	
	
	@Column(name = "keyword")
	private String keyword;
	
	@Column(name = "price")
	private BigDecimal price;
	
	@Column(name = "cp_id")
	private String cp_id;
	
	@Column(name = "event_type")
    @Enumerated(EnumType.STRING)
	private EventType event_type;//very important
	
	@Column(name = "service_id")
	private String service_id;
	
	@Column(name = "discount_applied")
	private String discount_applied;
	
	@Column(name = "cp_tx_id", unique=true)
	@Index(name="cp_idtxid_idx")
	private Long cp_tx_id;
	
	@Column(name = "tx_id")
	private Long tx_id;
	
	@Column(name = "processed")
	@Index(name="processed_idx")
	private Long processed;
	
	
	
	
	public String getPricePointKeyword() throws PricePointException {
		if(pricePointKeyword==null || pricePointKeyword.isEmpty())
			throw new PricePointException("No price point set! Refer to CP Integration form");
		return pricePointKeyword;
	}

	public void setPricePointKeyword(String pricePointKeyword) {
		this.pricePointKeyword = pricePointKeyword;
	}

	public String getResp_status_code() {
		return resp_status_code;
	}

	public void setResp_status_code(String resp_status_code) {
		this.resp_status_code = resp_status_code;
	}

	public Long getIn_outgoing_queue() {
		return in_outgoing_queue;
	}

	public void setIn_outgoing_queue(Long in_outgoing_queue) {
		this.in_outgoing_queue = in_outgoing_queue;
	}

	public Long getPriority() {
		return priority;
	}

	public void setPriority(Long priority) {
		this.priority = priority;
	}

	

	public Long getMaxRetriesAllowed() {
		return maxRetriesAllowed;
	}

	public void setMaxRetriesAllowed(Long maxRetriesAllowed) {
		this.maxRetriesAllowed = maxRetriesAllowed;
	}

	public Long getRetry_count() {
		return retry_count;
	}

	public void setRetry_count(Long retry_count) {
		this.retry_count = retry_count;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	
	

	public Date getTimeStamp() {
		if(timeStamp==null)
			timeStamp = new Date();
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
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

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getCp_id() {
		return cp_id;
	}

	public void setCp_id(String cp_id) {
		this.cp_id = cp_id;
	}

	public EventType getEvent_type() {
		return event_type;
	}

	public void setEvent_type(EventType event_type) {
		this.event_type = event_type;
	}

	public String getService_id() {
		return service_id;
	}

	public void setService_id(String service_id) {
		this.service_id = service_id;
	}

	public String getDiscount_applied() {
		return discount_applied;
	}

	public void setDiscount_applied(String discount_applied) {
		this.discount_applied = discount_applied;
	}

	public long getCp_tx_id() {
		return cp_tx_id;
	}

	public void setCp_tx_id(long cp_tx_id) {
		this.cp_tx_id = cp_tx_id;
	}

	public Long getTx_id() {
		return tx_id;
	}

	public void setTx_id(long tx_id) {
		this.tx_id = tx_id;
	}

	public Long isProcessed() {
		return processed;
	}

	public void setProcessed(Long processed) {
		this.processed = processed;
	}
	
	

	public boolean isSuccess() {
		if(success==null)
			return new Boolean(false);
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public long getMessage_id() {
		return message_id;
	}

	public void setMessage_id(long message_id) {
		this.message_id = message_id;
	}

	public String getChargeXML(String base_charge_xml) throws PricePointException {
		return base_charge_xml
				.replaceAll("\\{OPERATION\\}", getOperation())
				.replaceAll("\\{MSISDN\\}", getMsisdn())
				.replaceAll("\\{SHORTCODE\\}", getShortcode())
				.replaceAll("\\{KEYWORD\\}", getPricePointKeyword())
				.replaceAll("\\{SERVICE_ID\\}", getService_id())
				.replaceAll("\\{PRICE\\}", String.valueOf( getPrice().doubleValue()))
				.replaceAll("\\{CP_ID\\}", getCp_id())
				.replaceAll("\\{EVENT_TYPE\\}", getEvent_type().getName())
				.replaceAll("\\{TX_ID\\}", String.valueOf(getTx_id()))
				.replaceAll("\\{CP_TX_ID\\}", String.valueOf(getCp_tx_id()));
	}

	public String toString() {
		return "Billable [operation=" + operation + ", msisdn=" + msisdn
				+ ", shortcode=" + shortcode + ", keyword=" + keyword
				+ ", price=" + price + ", cp_id=" + cp_id + ", event_type="
				+ event_type + ", service_id=" + service_id
				+ ", discount_applied=" + discount_applied + ", cp_tx_id="
				+ cp_tx_id + ", tx_id=" + tx_id + "]";
	}
	
	
	

}
