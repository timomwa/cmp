package com.pixelandtag.cmp.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "keywords")
public class Keyword extends BaseEntity {
	

	@Column(name = "keyword")
	private String keyword;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "price")
	private Double price;
	
	
	@Column(name = "subscription_push_tail_text")
	private Double subscription_push_tail_text;


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


	public Double getSubscription_push_tail_text() {
		return subscription_push_tail_text;
	}


	public void setSubscription_push_tail_text(Double subscription_push_tail_text) {
		this.subscription_push_tail_text = subscription_push_tail_text;
	}
	
	
}
