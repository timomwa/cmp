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
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;


@Entity
@Table(name = "billable_queue")
public class Billable implements Serializable {

	

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4690931003011822080L;
	
	/**
	 * HTTP status code
	 */
	@Column(name = "resp_status_code")
	@Index(name="bilblidx")
	private String resp_status_code;
	
	
	
	/**
	 * The price point keyword
	 */
	@Column(name = "price_point_keyword")
	private String pricePointKeyword;
	
	
	@Column(name = "success")
	@Index(name="bilblidx")
	private Boolean success;
	
	/**
	 * the message id
	 */
	
	@Column(name = "message_id")
	@Index(name="bilblidx")
	private Long message_id;
	
	
	/**
	 * Boolean to mark the 
	 * record as in outgoing queue
	 */
	@Column(name = "in_outgoing_queue")
	@Index(name="outq_idx")
	private Long in_outgoing_queue;
	
	@Column(name = "valid")
	private Boolean valid;
	
	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name = "opco_id_fk", nullable=false)
	@Index(name="blblopcidx")
	private OperatorCountry opco;
	
	
	
	@Transient
	private Boolean transferIn;
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(timeStamp==null)
			timeStamp = new Date();
		if(success==null)
			success = Boolean.FALSE;
		if(processed==null)
			processed = new Long(0);
		if(valid==null)
			valid = Boolean.TRUE;
		if(opco_tx_id==null && getCp_tx_id()!=null)
			opco_tx_id = getCp_tx_id();
	}
	
	/**
	 * MTs are more important than push subscriptions
	 */
	@Column(name = "priority")
	@Index(name="priority_idx")
	private Long priority;
	
	@Column(name = "timeStamp",nullable = false)
	@Index(name="timeStamp_idx")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timeStamp;
	
	@Column(name = "maxRetriesAllowed")
	private Long maxRetriesAllowed;
	
	@Column(name = "retry_count")
	@Index(name="bilblidx")
	private Long retry_count;
	
	@Column(name = "operation")
	private String operation;
	
	@Column(name = "msisdn")
	@Index(name="msisdnIdx")
	private String msisdn;
	
	
	@Column(name = "shortcode")
	private String shortcode;
	
	
	@Column(name = "keyword")
	@Index(name="bilblmsisdidx")
	private String keyword;
	
	@Column(name = "price")
	@Index(name="bilblidx")
	private BigDecimal price;
	
	@Column(name = "cp_id")
	private String cp_id;
	
	@Column(name = "event_type")
    @Enumerated(EnumType.STRING)
	private EventType event_type;//very important
	
	@Column(name = "service_id")
	@Index(name="msisdnIdx")
	private String service_id;
	
	@Column(name = "discount_applied")
	private String discount_applied;
	
	@Column(name = "cp_tx_id", unique=true)
	@Index(name="cp_idtxid_idx")
	private String cp_tx_id;
	
	@Column(name = "opco_tx_id")
	@Index(name="opcotxid_idx")
	private String opco_tx_id;
	
	@Column(name = "processed")
	@Index(name="processed_idx")
	private Long processed;
	
	
	@Column(name = "transactionId")
	@Index(name="optxididx")
	private String transactionId;
	
	
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Date getTimeStamp() {
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

	public String getCp_tx_id() {
		return cp_tx_id;
	}

	public void setCp_tx_id(String cp_tx_id) {
		this.cp_tx_id = cp_tx_id;
	}

	

	public Long isProcessed() {
		return processed;
	}

	public void setProcessed(Long processed) {
		this.processed = processed;
	}
	
	

	public Boolean isSuccess() {
		if(success==null)
			return new Boolean(false);
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public long getMessage_id() {
		return message_id;
	}

	public void setMessage_id(long message_id) {
		this.message_id = message_id;
	}
	

	public String getChargeXML(String base_charge_xml) throws PricePointException {
		try{
			if(getOpco_tx_id()==null && getCp_tx_id()!=null )
				setOpco_tx_id(getCp_tx_id());
			
			
			base_charge_xml = base_charge_xml.replaceAll("\\{OPERATION\\}", getOperation());
			base_charge_xml = base_charge_xml.replaceAll("\\{MSISDN\\}", getMsisdn());
			base_charge_xml = base_charge_xml.replaceAll("\\{SHORTCODE\\}", getShortcode());
			base_charge_xml = base_charge_xml.replaceAll("\\{KEYWORD\\}", getPricePointKeyword());
			base_charge_xml = base_charge_xml.replaceAll("\\{SERVICE_ID\\}", getService_id());
			base_charge_xml = base_charge_xml.replaceAll("\\{PRICE\\}", String.valueOf( getPrice().doubleValue()));
			base_charge_xml = base_charge_xml.replaceAll("\\{CP_ID\\}", getCp_id());
			base_charge_xml = base_charge_xml.replaceAll("\\{EVENT_TYPE\\}", getEvent_type().getName());
			base_charge_xml = base_charge_xml.replaceAll("\\{TX_ID\\}",getOpco_tx_id() );
			base_charge_xml = base_charge_xml.replaceAll("\\{CP_TX_ID\\}", getCp_tx_id());
			base_charge_xml = base_charge_xml.replaceAll("\\{KEYWORD\\}", getPricePointKeyword());
		}catch(Exception exp){
			exp.getMessage();
			throw new PricePointException(exp);
		}
		return base_charge_xml;
	}

	

	@Override
	public String toString() {
		return "Billable [id=" + id + ", resp_status_code=" + resp_status_code
				+ ", pricePointKeyword=" + pricePointKeyword + ", success="
				+ success + ", message_id=" + message_id
				+ ", in_outgoing_queue=" + in_outgoing_queue + ", valid="
				+ valid + ", opco=" + opco + ", transferIn=" + transferIn
				+ ", priority=" + priority + ", timeStamp=" + timeStamp
				+ ", maxRetriesAllowed=" + maxRetriesAllowed + ", retry_count="
				+ retry_count + ", operation=" + operation + ", msisdn="
				+ msisdn + ", shortcode=" + shortcode + ", keyword=" + keyword
				+ ", price=" + price + ", cp_id=" + cp_id + ", event_type="
				+ event_type + ", service_id=" + service_id
				+ ", discount_applied=" + discount_applied + ", cp_tx_id="
				+ cp_tx_id + ", opco_tx_id=" + opco_tx_id + ", processed="
				+ processed + ", transactionId=" + transactionId + "]";
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public Boolean getSuccess() {
		return success;
	}

	public Long getProcessed() {
		return processed;
	}

	public void setMessage_id(Long message_id) {
		this.message_id = message_id;
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public Boolean getTransferIn() {
		return transferIn==null? Boolean.FALSE : transferIn;
	}

	public void setTransferIn(Boolean transferIn) {
		this.transferIn = transferIn;
	}

	public OperatorCountry getOpco() {
		return opco;
	}

	public void setOpco(OperatorCountry opco) {
		this.opco = opco;
	}

	public String getOpco_tx_id() {
		return opco_tx_id;
	}

	public void setOpco_tx_id(String opco_tx_id) {
		this.opco_tx_id = opco_tx_id;
	}
	
	
	
}
