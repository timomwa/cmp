package com.pixelandtag.cmp.entities;

import java.io.Serializable;
import java.math.BigDecimal;

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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;

@Entity
@Table(name = "opco_sms_service", uniqueConstraints = @UniqueConstraint(columnNames={"sms_service_id_fk","opco_id_fk"}))
@NamedQueries({
	@NamedQuery(
			name = OpcoSMSService.NQ_FIND_BY_SERVICE_ID_AND_OPCO,
			query = "from OpcoSMSService osms WHERE osms.smsservice.id=:service_id AND osms.opco=:opco"
	)
})
public class OpcoSMSService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5529601341679397355L;
	
	@Transient
	public static final String NQ_FIND_BY_SERVICE_ID_AND_OPCO = "findbyserviceidandopco";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="sms_service_id_fk", nullable=false)
	private SMSService smsservice;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="opco_id_fk", nullable=false)
	private OperatorCountry opco;
	
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="mo_processor_id")
	private MOProcessor moprocessor;
	
	
	@Column(name="price", scale=2, precision=20)
	private BigDecimal price;
	
	
	@PreUpdate
	@PrePersist
	public void update(){
		if(price==null)
			price = BigDecimal.ZERO;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SMSService getSmsservice() {
		return smsservice;
	}

	public void setSmsservice(SMSService smsservice) {
		this.smsservice = smsservice;
	}

	public OperatorCountry getOpco() {
		return opco;
	}

	public void setOpco(OperatorCountry opco) {
		this.opco = opco;
	}

	public MOProcessor getMoprocessor() {
		return moprocessor;
	}

	public void setMoprocessor(MOProcessor moprocessor) {
		this.moprocessor = moprocessor;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
}
