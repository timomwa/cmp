package com.pixelandtag.serviceprocessors.dto;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.pixelandtag.cmp.entities.ProcessorType;

public class ServiceProcessorDTO implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -409154920628397349L;
	private int id;
	private String serviceName;
	private String shortcode;
	//The number of threads the service processor should have.
	private int threads;
	private String processorClass;
	private boolean active;
	private String class_status;
	private String[] keywords;
	private String servKey;
	private double price;
	
	protected String subscriptionText;
	protected String unsubscriptionText;
	protected String tailTextSubscribed;
	protected String tailTextNotSubecribed;
	private String pricePointKeyword;
	private ProcessorType processor_type;
	private String forwarding_url;
	private String protocol;
	private Long smppid;
	private Long opcoid;
	
	
	public String getSubscriptionText() {
		return subscriptionText;
	}


	public void setSubscriptionText(String subscriptionText) {
		this.subscriptionText = subscriptionText;
	}


	public String getUnsubscriptionText() {
		return unsubscriptionText;
	}


	public void setUnsubscriptionText(String unsubscriptionText) {
		this.unsubscriptionText = unsubscriptionText;
	}


	public String getTailTextSubscribed() {
		return tailTextSubscribed;
	}


	public void setTailTextSubscribed(String tailTextSubscribed) {
		this.tailTextSubscribed = tailTextSubscribed;
	}


	public String getTailTextNotSubecribed() {
		return tailTextNotSubecribed;
	}


	public void setTailTextNotSubecribed(String tailTextNotSubecribed) {
		this.tailTextNotSubecribed = tailTextNotSubecribed;
	}
	
	
	public String getProcessorClass() {
		return processorClass;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public String getShortcode() {
		return shortcode;
	}
	public void setShortcode(String shortcode) {
		this.shortcode = shortcode;
	}
	public int getThreads() {
		return threads;
	}
	public void setThreads(int threads) {
		this.threads = threads;
	}
	
	public String getServKey() {
		return servKey;
	}
	public void setServKey(String servKey) {
		this.servKey = servKey;
	}
	public int getId() {
		return id;
	}
	public String getServiceName() {
		return serviceName;
	}
	public String getProcessorClassName() {
		return processorClass;
	}
	public boolean isActive() {
		return active;
	}
	public String getClass_status() {
		return class_status;
	}
	public String[] getKeywords() {
		return keywords;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public void setProcessorClass(String processorClass_) {
		processorClass = processorClass_;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public void setClass_status(String class_status) {
		this.class_status = class_status;
	}
	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}
	
	public String getPricePointKeyword() {
		return pricePointKeyword;
	}


	public void setPricePointKeyword(String pricePointKeyword) {
		this.pricePointKeyword = pricePointKeyword;
	}
	
	


	@Override
	public String toString() {
		return "ServiceProcessorDTO [id=" + id + ", serviceName=" + serviceName
				+ ", shortcode=" + shortcode + ", threads=" + threads
				+ ", processorClass=" + processorClass + ", active=" + active
				+ ", class_status=" + class_status + ", keywords="
				+ Arrays.toString(keywords) + ", servKey=" + servKey
				+ ", price=" + price + ", subscriptionText=" + subscriptionText
				+ ", unsubscriptionText=" + unsubscriptionText
				+ ", tailTextSubscribed=" + tailTextSubscribed
				+ ", tailTextNotSubecribed=" + tailTextNotSubecribed
				+ ", pricePointKeyword=" + pricePointKeyword
				+ ", processor_type=" + processor_type + ", forwarding_url="
				+ forwarding_url + ", protocol=" + protocol + ", smppid="
				+ smppid + ", opcoid=" + opcoid + "]";
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


	public Long getOpcoid() {
		return opcoid;
	}


	public void setOpcoid(Long opcoid) {
		this.opcoid = opcoid;
	}
	
	
}
