package com.pixelandtag.cmp.entities;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;

@Entity
@Table(name = "mo_processors")
public class MOProcessorE implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5156836877189231807L;



	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@Column(name="ServiceName")
	private String serviceName;
	
	@Column(name="shortcode")
	private String shortcode;
	
	
	@Column(name="threads")
	private Long threads;
	
	@Column(name="ProcessorClass")
	private String processorClass;
	
	@Column(name="enable")
	private Long enable;
	
	@Column(name="class_status")
	@Enumerated(EnumType.STRING)
	private ClassStatus class_status;
	
	@Column(name="processor_type")
	@Enumerated(EnumType.STRING)
	ProcessorType processor_type;
	
	@Column(name="forwarding_url")
	private String forwarding_url;
	
	@Column(name="protocol")
	private String protocol;
	
	@Column(name="smppid")
	private Long smppid;
	
	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name = "opco_id_fk", nullable=false)
	@Index(name="mopropidx")
	private OperatorCountry opco;
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getShortcode() {
		return shortcode;
	}

	public void setShortcode(String shortcode) {
		this.shortcode = shortcode;
	}

	public Long getThreads() {
		return threads;
	}

	public void setThreads(Long threads) {
		this.threads = threads;
	}

	public String getProcessorClass() {
		return processorClass;
	}

	public void setProcessorClass(String processorClass) {
		this.processorClass = processorClass;
	}

	public Long getEnable() {
		return enable;
	}

	public void setEnable(Long enable) {
		this.enable = enable;
	}

	public ClassStatus getClass_status() {
		return class_status;
	}

	public void setClass_status(ClassStatus class_status) {
		this.class_status = class_status;
	}

	public ProcessorType getProcessor_type() {
		return processor_type;
	}

	public void setProcessor_type(ProcessorType processor_type) {
		this.processor_type = processor_type;
	}

	public String getForwarding_url() {
		return forwarding_url;
	}

	public void setForwarding_url(String forwarding_url) {
		this.forwarding_url = forwarding_url;
	}
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(processor_type==null)
			processor_type = ProcessorType.LOCAL;
		
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public Long getSmppid() {
		return smppid;
	}

	public void setSmppid(Long smppid) {
		this.smppid = smppid;
	}

	public OperatorCountry getOpco() {
		return opco;
	}

	public void setOpco(OperatorCountry opco) {
		this.opco = opco;
	}
	
	
	

}
