package com.pixelandtag.cmp.entities.subscription;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
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
import javax.persistence.UniqueConstraint;

import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.OutgoingSMS;

@Entity
@Table(name = "double_confirmationqueue", uniqueConstraints = @UniqueConstraint(columnNames={"msisdn","opcosmsservice_id_fk"}))
@NamedQueries({
	@NamedQuery(
			name = DoubleConfirmationQueue.NQ_FIND_BY_MSISDN_AND_SERVICE,
			query = "from DoubleConfirmationQueue dcq WHERE dcq.msisdn=:msisdn AND dcq.opcosmsservice=:opcosmsservice order by timestamp desc"
	)
})
public class DoubleConfirmationQueue implements Serializable {
	
	public static final String NQ_FIND_BY_MSISDN_AND_SERVICE = "doubleconfirm.findbymsisdnandservice";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8578137564429958522L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@Column(name="msisdn")
	private String msisdn;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="opcosmsservice_id_fk", nullable=false)
	private OpcoSMSService opcosmsservice;
	
	@Column(name="timestamp")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	@PrePersist
	@PreUpdate
	public void update(){
		if(timestamp==null)
			timestamp = new Date();
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

	public OpcoSMSService getOpcosmsservice() {
		return opcosmsservice;
	}

	public void setOpcosmsservice(OpcoSMSService opcosmsservice) {
		this.opcosmsservice = opcosmsservice;
	}


	public Date getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "DoubleConfirmationQueue [id=" + id + ",\n msisdn=" + msisdn
				+ ",\n opcosmsservice=" + opcosmsservice + ",\n timestamp="
				+ timestamp + "]";
	}

}
