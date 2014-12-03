package com.pixelandtag.sms.producerthreads;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import com.pixelandtag.entities.MTsms;


@Entity
@Table(name = "billable_queue")
public class Billable implements Serializable {

	

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4690931003089822080L;
	
	/**
	 * HTTP status code
	 */
	@Column(name = "resp_status_code")
	private int resp_status_code;
	
	/**
	 * the message id
	 */
	@Column(name = "resp_status_code")
	private long message_id;
	
	
	/**
	 * Boolean to mark the 
	 * record as in outgoing queue
	 */
	@Column(name = "in_outgoing_queue")
	@Index(name="outq_idx")
	private int in_outgoing_queue;
	
	@PrePersist
	@PreUpdate
	public void setDefaults(){
		
	}
	
	/**
	 * MTs are more important than push subscriptions
	 */
	@Column(name = "priority")
	@Index(name="priority_idx")
	private int priority;
	
	@Column(name = "ttl")
	private int ttl;
	
	@Column(name = "retry_count")
	private int retry_count;
	
	@Column(name = "operation")
	private String operation;
	
	@Column(name = "msisdn")
	private String msisdn;
	
	
	@Column(name = "shortcode")
	private String shortcode;
	
	
	@Column(name = "keyword")
	private String keyword;
	
	@Column(name = "price")
	private String price;
	
	@Column(name = "cp_id")
	private String cp_id;
	
	@Column(name = "event_type")
	private EventType event_type;//very important
	
	@Column(name = "service_id")
	private String service_id;
	
	@Column(name = "discount_applied")
	private String discount_applied;
	
	@Column(name = "cp_tx_id")
	private long cp_tx_id;
	
	@Column(name = "tx_id")
	private long tx_id;
	
	@Column(name = "inqueue")
	private boolean inqueue;
	
	@Column(name = "processed")
	private boolean processed;
	
	
	
	
	public int getResp_status_code() {
		return resp_status_code;
	}

	public void setResp_status_code(int resp_status_code) {
		this.resp_status_code = resp_status_code;
	}

	public int getIn_outgoing_queue() {
		return in_outgoing_queue;
	}

	public void setIn_outgoing_queue(int in_outgoing_queue) {
		this.in_outgoing_queue = in_outgoing_queue;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public int getRetry_count() {
		return retry_count;
	}

	public void setRetry_count(int retry_count) {
		this.retry_count = retry_count;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
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

	public long getTx_id() {
		return tx_id;
	}

	public void setTx_id(long tx_id) {
		this.tx_id = tx_id;
	}

	public boolean isInqueue() {
		return inqueue;
	}

	public void setInqueue(boolean inqueue) {
		this.inqueue = inqueue;
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}
	
	

	public long getMessage_id() {
		return message_id;
	}

	public void setMessage_id(long message_id) {
		this.message_id = message_id;
	}

	public String getChargeXML(String base_charge_xml) {
		return base_charge_xml
				.replaceAll("{OPERATION}", getOperation())
				.replaceAll("{MSISDN}", getMsisdn())
				.replaceAll("{SHORTCODE}", getShortcode())
				.replaceAll("{KEYWORD}", getKeyword())
				.replaceAll("{SERVICE_ID}", getService_id())
				.replaceAll("{PRICE}", getPrice())
				.replaceAll("{CP_ID}", getCp_id())
				.replaceAll("{EVENT_TYPE}", getEvent_type().toString())
				.replaceAll("{TX_ID}", String.valueOf(getTx_id()))
				.replaceAll("{CP_TX_ID}", String.valueOf(getCp_tx_id()));
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
