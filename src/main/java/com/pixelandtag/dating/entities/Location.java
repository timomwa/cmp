package com.pixelandtag.dating.entities;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "dating_chatlog")
public class Location implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@Column(name = "source_person_id", nullable = false)
	@Index(name="sourcePidx")
	private BigInteger source_person_id;
	/**
	 * 
	 */
	private static final long serialVersionUID = -1385489930038558955L;


}
