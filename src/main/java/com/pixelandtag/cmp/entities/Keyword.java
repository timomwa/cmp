package com.pixelandtag.cmp.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.json.JSONException;
import org.json.JSONObject;

@Entity
@Table(name = "keywords")
public class Keyword {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "keyword")
	private String keyword;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "price")
	private Double price;
	
	
	@Column(name = "subscription_push_tail_text")
	private String subscription_push_tail_text;


	public String getKeyword() {
		return keyword;
	}


	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public Double getPrice() {
		return price;
	}


	public void setPrice(Double price) {
		this.price = price;
	}


	public String getSubscription_push_tail_text() {
		return subscription_push_tail_text;
	}


	public void setSubscription_push_tail_text(String subscription_push_tail_text) {
		this.subscription_push_tail_text = subscription_push_tail_text;
	}
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public JSONObject toJson() throws JSONException{
		JSONObject ob = null;
		ob = new JSONObject();
		ob.put("id", getId());
		ob.put("description", getDescription());
		ob.put("price", getPrice());
		ob.put("keyword", getKeyword());
		ob.put("subscription_push_tail_text", getSubscription_push_tail_text());
		return ob;
	}
	
	
}
