package com.pixelandtag.cmp.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderProfile;

/**
 * An improvement of com.pixelandtag.cmp.entities.HttpToSend
 * httptosend
 * We need to have a general outgoing queue
 * previous queue was too operator specific.
 * 
 * This one is will hold just the core parameters
 * needed to send an sms and the stat
 * 
 * @author Timothy Mwangi Gikonyo
 * @date 18th August 2015
 * 
 *
 */
@Entity
@Table(name = "outgoing_sms")
@NamedQueries({
	@NamedQuery(
			name = OutgoingSMS.NQ_LIST_UNSENT_ORDER_BY_PRIORITY_DESC,
			query = "select que from OutgoingSMS que where que.billing_status in (:billstatus) AND que.in_outgoing_queue=0 AND que.sent=0  order by que.priority desc"
	)
})
public class OutgoingSMS implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4942680309002195734L;
	
	@Transient
	public static final String NQ_LIST_UNSENT_ORDER_BY_PRIORITY_DESC = "smsqueue.listunsent";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="opco_profile_id")
	private OpcoSenderProfile opcosenderprofile;
	
	@Column(name="in_outgoing_queue")
	private Boolean in_outgoing_queue;
	
	@Column(name="sms",length=1000)
	private String sms;

	@Column(name="msisdn")
	private String msisdn;
	
	@Column(name="shortcode")
	private String shortcode;
	
	@Column(name="price")
	private BigDecimal price;
	
	@Column(name="priority")
	private Integer priority;
	
	@Column(name="serviceid")
	private Long serviceid;

	@Column(name="re_tries")
	private Long re_tries;

	@Column(name="ttl")
	private Long ttl;
	
	@Index(name="timestamp")
	private Date timestamp;
	
	@Column(name="charged")
	private Boolean charged;

	@Column(name="sent")
	private Boolean sent;
	
	@Index(name="osmcmp_tx_id")
	@Column(name="cmp_tx_id")
	private String cmp_tx_id;
	
	@Index(name="osmopco_tx_id")
	@Column(name="opco_tx_id")
	private String opco_tx_id;
	
	@Column(name="split")
	private Boolean split;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="processor_id")
	private MOProcessor moprocessor;
	
	@Column(name="billing_status")
	@Enumerated(EnumType.STRING)
	private BillingStatus billing_status;
	
	@PrePersist
	public void onCreate(){
		if(split==null)
			split = Boolean.FALSE;
		if(in_outgoing_queue==null)
			in_outgoing_queue = Boolean.FALSE;
		if(price==null)
			price = BigDecimal.ZERO;
		if(sent==null)
			sent = Boolean.FALSE;
		if(priority==null)
			priority = 0;
		if(re_tries==null)
			re_tries = 0L;
		if(ttl==null)
			ttl = 0L;
		if(timestamp==null)
			timestamp = new Date();
		if(charged==null)
			charged = Boolean.FALSE;
		if(billing_status==null)
			billing_status = BillingStatus.NO_BILLING_REQUIRED;
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public OpcoSenderProfile getOpcosenderprofile() {
		return opcosenderprofile;
	}

	public void setOpcosenderprofile(OpcoSenderProfile opcosenderprofile) {
		this.opcosenderprofile = opcosenderprofile;
	}

	public Boolean getIn_outgoing_queue() {
		return in_outgoing_queue;
	}

	public void setIn_outgoing_queue(Boolean in_outgoing_queue) {
		this.in_outgoing_queue = in_outgoing_queue;
	}

	public String getSms() {
		return sms;
	}

	public void setSms(String sms) {
		this.sms = sms;
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

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Long getServiceid() {
		return serviceid;
	}

	public void setServiceid(Long serviceid) {
		this.serviceid = serviceid;
	}

	public Long getRe_tries() {
		return re_tries;
	}

	public void setRe_tries(Long re_tries) {
		this.re_tries = re_tries;
	}

	public Long getTtl() {
		return ttl;
	}

	public void setTtl(Long ttl) {
		this.ttl = ttl==null || ttl.intValue()<0 ? 1 : ttl;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Boolean getCharged() {
		return charged;
	}

	public void setCharged(Boolean charged) {
		this.charged = charged;
	}

	public Boolean getSent() {
		return sent;
	}

	public void setSent(Boolean sent) {
		this.sent = sent;
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

	public Boolean getSplit() {
		return split;
	}

	public void setSplit(Boolean split) {
		this.split = split;
	}

	public MOProcessor getMoprocessor() {
		return moprocessor;
	}

	public void setMoprocessor(MOProcessor moprocessor) {
		this.moprocessor = moprocessor;
	}

	public BillingStatus getBilling_status() {
		return billing_status;
	}

	public void setBilling_status(BillingStatus billing_status) {
		this.billing_status = billing_status;
	}
	
	
	

}
