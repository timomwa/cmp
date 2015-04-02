package com.pixelandtag.sms.producerthreads;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "topupnumber")
public class TopUpNumber implements Serializable {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 738562063235680166L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	@Column(name = "number", unique=true)
	@Index(name="numberIdx")
	private String number;
	
	
	@Column(name = "serial", unique=true)
	@Index(name="serialIdx")
	private BigDecimal serial;
	
	@Column(name = "telco")
	private Integer telco;
	
	@Column(name = "value")
	private Integer value;
	
	
	@Column(name = "depleted")
	private Boolean depleted;
	

	public Integer getTelco() {
		return telco;
	}


	public void setTelco(Integer telco) {
		this.telco = telco;
	}


	public Boolean getDepleted() {
		return depleted;
	}


	public void setDepleted(Boolean depleted) {
		this.depleted = depleted;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getNumber() {
		return number;
	}


	public void setNumber(String number) {
		this.number = number;
	}


	public BigDecimal getSerial() {
		return serial;
	}


	public void setSerial(BigDecimal serial) {
		this.serial = serial;
	}


	public Integer getValue() {
		return value;
	}


	public void setValue(Integer value) {
		this.value = value;
	}
	
	
	
	
	

}
