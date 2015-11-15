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
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.subscription.dto.MediumType;

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
			query = "select que from OutgoingSMS que where que.timestamp<=:timestamp AND que.billing_status in (:billstatus) AND que.in_outgoing_queue=0 AND que.sent=0  order by que.priority desc, que.timestamp asc"
	),
	@NamedQuery(
			name = OutgoingSMS.NQ_LIST_UNSENT_BY_PROFILEID_ORDER_BY_PRIORITY_DESC,
			query = "select que from OutgoingSMS que where que.opcosenderprofile.id=:opcosenderprofileid AND que.timestamp<=:timestamp AND que.billing_status in (:billstatus) AND que.in_outgoing_queue=0 AND que.sent=0  order by que.priority desc, que.timestamp asc"
	),
	@NamedQuery(
			name = OutgoingSMS.NQ_LIST_UNSENT_BY_PROFILE_ORDER_BY_PRIORITY_DESC,
			query = "select que from OutgoingSMS que where que.opcosenderprofile=:opcosenderprofile AND que.timestamp<=:timestamp AND que.billing_status in (:billstatus) AND que.in_outgoing_queue=0 AND que.sent=0  order by que.priority desc, que.timestamp asc"
	),
	@NamedQuery(
			name = OutgoingSMS.NQ_LIST_UPDATE_QUEUE_STATUS_BY_ID,
			query = "update OutgoingSMS set  in_outgoing_queue=:in_outgoing_queue where id=:id"
	)
})
public class OutgoingSMS extends GenericMessage implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4942680309002195734L;
	
	@Transient
	public static final String NQ_LIST_UNSENT_ORDER_BY_PRIORITY_DESC = "smsqueue.listunsent";
	
	@Transient
	public static final String NQ_LIST_UNSENT_BY_PROFILEID_ORDER_BY_PRIORITY_DESC = "smsqueue.listunsent.by.profile";
	
	@Transient
	public static final String NQ_LIST_UNSENT_BY_PROFILE_ORDER_BY_PRIORITY_DESC = "smsqueue.listunsent.by.profile.and.ts";

	@Transient
	public static final String NQ_LIST_UPDATE_QUEUE_STATUS_BY_ID = "smsqueue.updatequeuestatusbyid";


	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="opco_profile_id")
	private OpcoSenderReceiverProfile opcosenderprofile;
	
	@Column(name="in_outgoing_queue", nullable=false)
	private Boolean in_outgoing_queue;

	
	@Column(name="priority")
	private Integer priority;
	
	@Column(name="re_tries")
	private Long re_tries;

	@Column(name="ttl")
	private Long ttl;
		
	
	@Column(name="charged")
	private Boolean charged;

	@Column(name="sent")
	private Boolean sent;
	

	
	
	@PrePersist
	public void onCreate(){
		if(in_outgoing_queue==null)
			in_outgoing_queue = Boolean.FALSE;
		if(sent==null)
			sent = Boolean.FALSE;
		if(priority==null)
			priority = 3;
		if(re_tries==null)
			re_tries = 0L;
		if(ttl==null)
			ttl = 1L;
		if(charged==null)
			charged = Boolean.FALSE;
		if(getMediumType()==null)
			setMediumType(MediumType.sms);
		if(getEvent_type()==null)
			setEvent_type( EventType.CONTENT_PURCHASE.getName() );
		
	}
	
	public OpcoSenderReceiverProfile getOpcosenderprofile() {
		return opcosenderprofile;
	}

	public void setOpcosenderprofile(OpcoSenderReceiverProfile opcosenderprofile) {
		this.opcosenderprofile = opcosenderprofile;
	}

	public Boolean getIn_outgoing_queue() {
		return in_outgoing_queue;
	}

	public void setIn_outgoing_queue(Boolean in_outgoing_queue) {
		this.in_outgoing_queue = in_outgoing_queue;
	}


	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
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

	@Override
	public String toString() {
		return "OutgoingSMS [opcosenderprofile=" + opcosenderprofile
				+ ",\n in_outgoing_queue=" + in_outgoing_queue
				+ ",\n priority=" + priority + ",\n re_tries=" + re_tries
				+ ",\n ttl=" + ttl + ",\n charged=" + charged + ",\n sent="
				+ sent + ",\n getId()=" + getId() + ",\n getMsisdn()="
				+ getMsisdn() + ",\n getCmp_tx_id()=" + getCmp_tx_id()
				+ ",\n getOpco_tx_id()=" + getOpco_tx_id()
				+ ",\n getBilling_status()=" + getBilling_status()
				+ ",\n getTimestamp()=" + getTimestamp() + ",\n getPrice()="
				+ getPrice() + ",\n getServiceid()=" + getServiceid()
				+ ",\n getSms()=" + getSms() + ",\n getShortcode()="
				+ getShortcode() + ",\n getSplit()=" + getSplit()
				+ ",\n getEvent_type()=" + getEvent_type()
				+ ",\n getPrice_point_keyword()=" + getPrice_point_keyword()
				+ ",\n getMoprocessor()=" + getMoprocessor()
				+ ",\n getIsSubscription()=" + getIsSubscription()
				+ ",\n getMediumType()=" + getMediumType() + ",\n toString()="
				+ super.toString() + ",\n getClass()=" + getClass()
				+ ",\n hashCode()=" + hashCode() + "]";
	}

	


}
