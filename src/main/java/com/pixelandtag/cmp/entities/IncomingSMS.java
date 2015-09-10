package com.pixelandtag.cmp.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.subscription.dto.MediumType;

@Entity
@Table(name = "incoming_sms")
@NamedQueries({
	@NamedQuery(
			name = IncomingSMS.NQ_LIST_UNPROCESSED,
			query = "select inc from IncomingSMS inc where inc.processed=:processed AND inc.mo_ack=:mo_ack order by inc.timestamp asc"
	),
	@NamedQuery(
			name = IncomingSMS.NQ_DELETE_BY_TX_ID,
			query = "delete from IncomingSMS inc where inc.cmp_tx_id=:cmp_tx_id OR inc.opco_tx_id=:opco_tx_id"
	)
})
public class IncomingSMS extends GenericMessage implements Serializable{
	
	@Transient
	public static final String NQ_LIST_UNPROCESSED = "incomingsms.listunprocessed";
	
	@Transient
	public static final String NQ_DELETE_BY_TX_ID = "incomingsms.deletebytxid";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -816506620150511093L; 
	
	
	@Column(name="mo_ack", nullable=false)
	private Boolean mo_ack;

	
	@Column(name="processed", nullable=false)
	private Boolean processed;
	
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "opco_id_fk", nullable=false)
	@Index(name="opcproflidx")
	private OperatorCountry opco;
	
	
	
	@PreUpdate
	@PrePersist
	public void onCreate(){
		if(processed==null)
			processed = Boolean.FALSE;
		if(mo_ack==null)
			mo_ack = Boolean.FALSE;
		if(getTimestamp()==null)
			setTimestamp(new Date());
		if(getIsSubscription()==null)
			setIsSubscription(Boolean.FALSE);
		if(getBilling_status()==null)
		   setBilling_status(BillingStatus.NO_BILLING_REQUIRED);
		if(getMediumType()==null)
			setMediumType(MediumType.sms);
		if(getEvent_type()==null)
			setEvent_type( EventType.CONTENT_PURCHASE.getName() );
	}


	public Boolean getProcessed() {
		return processed;
	}


	public void setProcessed(Boolean processed) {
		this.processed = processed;
	}


	public Boolean getMo_ack() {
		return mo_ack;
	}


	public void setMo_ack(Boolean mo_ack) {
		this.mo_ack = mo_ack;
	}


	public OperatorCountry getOpco() {
		return opco;
	}


	public void setOpco(OperatorCountry opco) {
		this.opco = opco;
	}


	public OutgoingSMS convertToOutgoing() {
		OutgoingSMS outgoing = new OutgoingSMS();
		outgoing.setBilling_status(getBilling_status());
		outgoing.setCmp_tx_id(getCmp_tx_id());
		outgoing.setEvent_type(getEvent_type());
		outgoing.setIn_outgoing_queue(Boolean.FALSE);
		outgoing.setMsisdn(getMsisdn());
		outgoing.setOpco_tx_id(getOpco_tx_id());
		outgoing.setPrice(getPrice());
		outgoing.setPrice_point_keyword(getPrice_point_keyword());
		outgoing.setPriority(3);
		outgoing.setRe_tries(0L);
		outgoing.setSent(Boolean.FALSE);
		outgoing.setServiceid(getServiceid());
		outgoing.setShortcode(getShortcode());
		outgoing.setSplit(getSplit());
		outgoing.setTimestamp(getTimestamp());
		outgoing.setTtl(3L);
		outgoing.setIsSubscription(getIsSubscription());
		outgoing.setMoprocessor(getMoprocessor());
		outgoing.setMediumType(getMediumType());
		return outgoing;
	}
	
	
}
