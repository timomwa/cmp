package com.pixelandtag.cmp.entities.subscription;

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
import javax.persistence.Table;

import com.pixelandtag.cmp.entities.OpcoSMSService;

@Entity
@Table(name = "sms_service_bundle")
public class SMSServiceBundle implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8002351882760688328L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="opcosmsservice_id_fk", nullable=false)
	private OpcoSMSService opcosmsservice;
	
	
	@Column(name="price", scale=2, precision=20)
	private BigDecimal price;
	
	@Column(name="name")
	private String name;
	
	
	@Column(name="enabled")
	private Boolean enabled;
	
	
	@Column(name="cardinal")
	private Boolean cardinal;


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public OpcoSMSService getOpcosmsservice() {
		return opcosmsservice;
	}


	public void setOpcosmsservice(OpcoSMSService opcosmsservice) {
		this.opcosmsservice = opcosmsservice;
	}


	public BigDecimal getPrice() {
		return price;
	}


	public void setPrice(BigDecimal price) {
		this.price = price;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Boolean getEnabled() {
		return enabled;
	}


	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}


	public Boolean getCardinal() {
		return cardinal;
	}


	public void setCardinal(Boolean cardinal) {
		this.cardinal = cardinal;
	}


	@Override
	public String toString() {
		return "SMSServiceBundle [id=" + id + ",\n opcosmsservice="
				+ opcosmsservice + ",\n price=" + price + ",\n name=" + name
				+ ",\n enabled=" + enabled + ",\n cardinal=" + cardinal + "]";
	}
	
}
