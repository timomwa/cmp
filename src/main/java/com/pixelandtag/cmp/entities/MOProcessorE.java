package com.pixelandtag.cmp.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
	
	

}
