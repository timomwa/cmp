package com.pixelandtag.cmp.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;

@Entity
@Table(name = "opco_sms_service")
public class OpcoSMSService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5529601341679397355L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="sms_service_id_fk", nullable=false)
	private SMSService smsservice;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="opco_id_fk", nullable=false)
	private OperatorCountry opco;

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
	
	
	
	

}
