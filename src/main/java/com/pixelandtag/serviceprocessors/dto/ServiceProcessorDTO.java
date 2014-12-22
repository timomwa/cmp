package com.pixelandtag.serviceprocessors.dto;

import java.util.Arrays;

public class ServiceProcessorDTO {
	
	private int id;
	private String serviceName;
	private String shortcode;
	//The number of threads the service processor should have.
	private int threads;
	private String processorClass;
	private String CMP_AKeyword;
	private String CMP_SKeyword;
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
	public String getCMP_AKeyword() {
		return CMP_AKeyword;
	}
	public String getCMP_SKeyword() {
		return CMP_SKeyword;
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
	public void setCMP_AKeyword(String cMP_Keyword) {
		CMP_AKeyword = cMP_Keyword;
	}
	public void setCMP_SKeyword(String cMP_SKeyword) {
		CMP_SKeyword = cMP_SKeyword;
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
				+ ", processorClass=" + processorClass + ", CMP_AKeyword="
				+ CMP_AKeyword + ", CMP_SKeyword=" + CMP_SKeyword + ", active="
				+ active + ", class_status=" + class_status + ", keywords="
				+ Arrays.toString(keywords) + ", servKey=" + servKey
				+ ", price=" + price + ", toString()=" + super.toString() + "]";
	}


	
	
	
	
	
	

}
