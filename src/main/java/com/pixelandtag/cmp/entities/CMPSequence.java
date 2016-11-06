package com.pixelandtag.cmp.entities;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;
/**
 * System-wide sequence
 * @author Timothy
 *
 */
@Entity
@Table(name = "cmp_sequence")
public class CMPSequence implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5654767073043243121L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "name",nullable=false,unique=true,length=50)
	@Index(name="cmpsqnmidx")
	private String name;
	
	@Column(name = "prefix")
	private String prefix;
	
	@Column(name = "nextval")
	private BigInteger nextval;
	
	@Column(name = "suffix")
	private String suffix;
	
	@Column(name = "timeStamp",nullable = false)
	@Index(name="cmpsts0idx")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timeStamp;

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

	public String getPrefix() {
		return ( (prefix==null) ? "" : prefix);
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public BigInteger getNextval() {
		return nextval;
	}

	public void setNextval(BigInteger nextval) {
		this.nextval = nextval;
	}

	public String getSuffix() {
		return ( (suffix==null)?"":suffix);
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public String getSeqNumber(){
		return getPrefix()+getSuffix()+getNextval();
	}
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(timeStamp==null)
			timeStamp = new Date();
	}
	

}
