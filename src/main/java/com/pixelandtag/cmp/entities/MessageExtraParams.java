package com.pixelandtag.cmp.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "message_extra_params")
public class MessageExtraParams implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3334088215119251374L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="transactionid", nullable=false, unique=true)
	@Index(name="trxnididx")
	private String transactionid;
	
	@Column(name="paramKey", nullable=false)
	@Index(name="trxnididx")
	private String paramKey;
		
	@Column(name="paramValue", nullable=false)
	private String paramValue;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTransactionid() {
		return transactionid;
	}

	public void setTransactionid(String transactionid) {
		this.transactionid = transactionid;
	}

	public String getParamKey() {
		return paramKey;
	}

	public void setParamKey(String paramKey) {
		this.paramKey = paramKey;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	@Override
	public String toString() {
		return "MessageExtraParams [id=" + id + ", transactionid="
				+ transactionid + ", paramKey=" + paramKey + ", paramValue="
				+ paramValue + "]";
	}

}
