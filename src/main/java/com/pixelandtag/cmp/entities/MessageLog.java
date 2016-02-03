package com.pixelandtag.cmp.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "message_log")
@NamedQueries({
	@NamedQuery(
			name = MessageLog.NQ_BY_CMP_TXID,
			query = "from MessageLog ml WHERE ml.cmp_tx_id=:cmp_tx_id"
	),
	@NamedQuery(
			name = MessageLog.NQ_BY_OPCO_TXID,
			query = "from MessageLog ml WHERE ml.opco_tx_id=:opco_tx_id"
	)
})
public class MessageLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5783583112893932358L;


	public static final String NQ_BY_CMP_TXID = "messagelog.getby_cmp_txid";
	
	public static final String NQ_BY_OPCO_TXID = "messagelog.getby_opco_txid";


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@Column(name="mo_processor_id_fk")
	private Long mo_processor_id_fk;
	
	
	@Index(name="mlopco_tx_id")
	@Column(name="opco_tx_id")
	private String opco_tx_id;
		
	@Index(name="mlcmp_tx_id")
	@Column(name="cmp_tx_id", unique=true, nullable=false)
	private String cmp_tx_id;
	
	@Column(name="mo_sms",length=2000)
	private String mo_sms;
		
	@Column(name="mo_timestamp", nullable=false)
	@Index(name="motimestampidx")
	@Temporal(TemporalType.TIMESTAMP)
	private Date mo_timestamp;
	
	
	@Column(name="mt_sms",length=1000)
	private String mt_sms;
	

	@Column(name="mt_timestamp")
	@Index(name="mttimestampidx")
	@Temporal(TemporalType.TIMESTAMP)
	private Date mt_timestamp;
		
	@Column(name="shortcode")
	private String shortcode;
	
	
	@Column(name="msisdn")
	@Index(name="msisdnidx")
	private String msisdn;
	
	@Column(name="status")
	@Index(name="statusidx")
	private String status;
	
	@Column(name="retry_count")
	private Long retryCount;
	
	@Column(name="source")
	@Index(name="sourceidx")
	private String source;
	
	
	@PrePersist
	public void onCreate(){
		if(mo_timestamp==null)
			mo_timestamp = new Date();
		if(retryCount==null)
			retryCount = 0L;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Long getMo_processor_id_fk() {
		return mo_processor_id_fk;
	}


	public void setMo_processor_id_fk(Long mo_processor_id_fk) {
		this.mo_processor_id_fk = mo_processor_id_fk;
	}


	public String getOpco_tx_id() {
		return opco_tx_id;
	}


	public void setOpco_tx_id(String opco_tx_id) {
		this.opco_tx_id = opco_tx_id;
	}


	public String getCmp_tx_id() {
		return cmp_tx_id;
	}


	public void setCmp_tx_id(String cmp_tx_id) {
		this.cmp_tx_id = cmp_tx_id;
	}


	public String getMo_sms() {
		return mo_sms;
	}


	public void setMo_sms(String mo_sms) {
		this.mo_sms = mo_sms;
	}


	public Date getMo_timestamp() {
		return mo_timestamp;
	}


	public void setMo_timestamp(Date mo_timestamp) {
		this.mo_timestamp = mo_timestamp;
	}


	public String getMt_sms() {
		return mt_sms;
	}


	public void setMt_sms(String mt_sms) {
		this.mt_sms = mt_sms;
	}


	public Date getMt_timestamp() {
		return mt_timestamp;
	}


	public void setMt_timestamp(Date mt_timestamp) {
		this.mt_timestamp = mt_timestamp;
	}


	public String getShortcode() {
		return shortcode;
	}


	public void setShortcode(String shortcode) {
		this.shortcode = shortcode;
	}


	public String getMsisdn() {
		return msisdn;
	}


	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public Long getRetryCount() {
		return retryCount;
	}


	public void setRetryCount(Long retryCount) {
		this.retryCount = retryCount;
	}


	public String getSource() {
		return source;
	}


	public void setSource(String source) {
		this.source = source;
	}


	@Override
	public String toString() {
		return "MessageLog [id=" + id + ", mo_processor_id_fk="
				+ mo_processor_id_fk 
				+ ", opco_tx_id=" + opco_tx_id + ", cmp_tx_id=" + cmp_tx_id
				+ ", mo_sms=" + mo_sms + ", mo_timestamp=" + mo_timestamp
				+ ", mt_sms=" + mt_sms + ", mt_timestamp=" + mt_timestamp
				+ ", shortcode=" + shortcode + ", msisdn=" + msisdn
				+ ", status=" + status + ", retryCount=" + retryCount
				+ ", source=" + source + "]";
	}
	
	
}
