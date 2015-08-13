package com.pixelandtag.cmp.entities.customer;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id; 
import javax.persistence.Table;

import org.hibernate.annotations.Index;


@Entity
@Table(name = "operator")
public class Operator implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7984328173253137879L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="name")
	private String name;
	
	
	@Column(name="code", nullable=false, unique=true)
	@Index(name="ortorcodidx")
	private String code;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}
