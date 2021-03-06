package com.pixelandtag.cmp.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.sms.producerthreads.EventType;

@Entity
@Table(name = "sms_service")
public class SMSService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 23934386067699630L;



	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="mo_processorFK", nullable=false)
	private MOProcessor moprocessor;
	
	@Column(name="cmd")
	private String cmd;
	
	@Column(name="push_unique")
	private Boolean push_unique;
	
	@Column(name="service_name")
	private String service_name;
	
	@Column(name="service_description")
	private String service_description;
	
	@Column(name="price")
	private BigDecimal price;
	
	@Column(name="price_point_keyword")
	private String price_point_keyword;
	
	@Column(name="enabled")
	private Boolean enabled;
	
	@Column(name="split_mt")
	private Boolean split_mt;
	
	@Column(name="subscriptionText")
	private String subscriptionText;
	
	@Column(name="unsubscriptionText")
	private String unsubscriptionText;
	
	@Column(name="tailText_subscribed")
	private String tailText_subscribed;
	
	@Column(name="tailText_notsubscribed")
	private String tailText_notsubscribed;
	
	@Column(name="event_type")
   private String event_type;
	
	
	@Column(name="subscription_length")
	private Long subscription_length;
	
	@Column(name="subscription_length_time_unit")
	@Enumerated(EnumType.STRING)
	private TimeUnit subscription_length_time_unit;
	
	
	@Column(name="forward_url")
	private String forwardURL;

	
	/**
	 * When we want a service to just
	 * do the billing, we state the delegateFor
	 * This works where sms services
	 * are just billing points as opposed to
	 * actual services one can subscribe to
	 * due to lack of time
	 * we use this method before we get 
	 * a proper subscription service in
	 * place.
	 */
	@Column(name="isjustAPricePoint")
	private Boolean isjustAPricePoint;
	
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public MOProcessor getMoprocessor() {
		return moprocessor;
	}

	public void setMoprocessor(MOProcessor moprocessor) {
		this.moprocessor = moprocessor;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public Boolean getPush_unique() {
		return push_unique;
	}

	public void setPush_unique(Boolean push_unique) {
		this.push_unique = push_unique;
	}

	public String getService_name() {
		return service_name;
	}

	public void setService_name(String service_name) {
		this.service_name = service_name;
	}

	public String getService_description() {
		return service_description;
	}

	public void setService_description(String service_description) {
		this.service_description = service_description;
	}

	public BigDecimal getPrice() {
		return price==null ? BigDecimal.ZERO : price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getPrice_point_keyword() {
		return price_point_keyword;
	}

	public void setPrice_point_keyword(String price_point_keyword) {
		this.price_point_keyword = price_point_keyword;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getSplit_mt() {
		return split_mt;
	}

	public void setSplit_mt(Boolean split_mt) {
		this.split_mt = split_mt;
	}

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

	public String getTailText_subscribed() {
		return tailText_subscribed;
	}

	public void setTailText_subscribed(String tailText_subscribed) {
		this.tailText_subscribed = tailText_subscribed;
	}

	public String getTailText_notsubscribed() {
		return tailText_notsubscribed;
	}

	public void setTailText_notsubscribed(String tailText_notsubscribed) {	
		this.tailText_notsubscribed = tailText_notsubscribed;
	}

	public String getEvent_type() {
		return event_type;
	}

	public void setEvent_type(String event_type) {
		this.event_type = event_type;
	}
	
	public Long getSubscription_length() {
		return subscription_length;
	}

	public void setSubscription_length(Long subscription_length) {
		this.subscription_length = subscription_length;
	}

	public TimeUnit getSubscription_length_time_unit() {
		return subscription_length_time_unit;
	}

	public void setSubscription_length_time_unit(
			TimeUnit subscription_length_time_unit) {
		this.subscription_length_time_unit = subscription_length_time_unit;
	}

	

	public Boolean getIsjustAPricePoint() {
		if(isjustAPricePoint==null)
			return Boolean.FALSE;
		return isjustAPricePoint;
	}

	public void setIsjustAPricePoint(Boolean isjustAPricePoint) {
		this.isjustAPricePoint = isjustAPricePoint;
	}
	
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(isjustAPricePoint==null)
			isjustAPricePoint = new Boolean(false);
	}

	public JSONObject toJson(){
		JSONObject jsonObj = new JSONObject();
		try{
			jsonObj.put("id", getId());
			jsonObj.put("mo_processorFK", getMoprocessor().getId());
			jsonObj.put("cmd", getCmd());
			jsonObj.put("keyword", getCmd());
			jsonObj.put("push_unique", getPush_unique());
			jsonObj.put("service_name", getService_name());
			jsonObj.put("service_description", getService_description());
			jsonObj.put("price", getPrice());
			jsonObj.put("price_point_keyword", getPrice_point_keyword());
			jsonObj.put("enabled", getEnabled());
			jsonObj.put("split_mt", getSplit_mt());
			jsonObj.put("subscriptionText", getSubscriptionText());
			jsonObj.put("unsubscriptionText", getUnsubscriptionText());
			jsonObj.put("tailText_subscribed", getTailText_subscribed());
			jsonObj.put("tailText_notsubscribed", getTailText_notsubscribed());
			jsonObj.put("subscription_length",getSubscription_length());
			jsonObj.put("event_type", getEvent_type());
			jsonObj.put("subscription_length_time_unit", getSubscription_length_time_unit());
		}catch(JSONException jse){
			jse.printStackTrace();
			return null;
		}
		
		return jsonObj;
	}
	
	
}
