package com.pixelandtag.cmp.entities.customer.configs;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;


@Entity
@Table(name = "techsupport_member")
@NamedQueries({
	@NamedQuery(
	name = TechSupportMember.NQ_ALARM_ENABLED,
	query = "from TechSupportMember tsm where  tsm.alarmenabled=:alarmenabled"
	)
})
public class TechSupportMember implements Serializable{
	
	@Transient
	public static final String NQ_ALARM_ENABLED = "techsupport.member.alarmenabled";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 477075380815249339L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="msisdn")
	private String msisdn;
	
	@Column(name="email")
	private String email;
	
	
	@Column(name="alarmenabled")
	private Boolean alarmenabled;
	
	@Column(name="workinghours_start")
	@Temporal(TemporalType.TIME)
	private Date workinghours_start;
	
	@Column(name="workinghours_end")
	@Temporal(TemporalType.TIME)
	private Date workinghours_end;
	
	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name = "opco_id_fk", nullable=false)
	@Index(name="blblopcidx")
	private OperatorCountry opco;
	
	
	
	public Long getId() {
		return id;
	}






	public void setId(Long id) {
		this.id = id;
	}






	public String getMsisdn() {
		return msisdn;
	}






	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}






	public String getEmail() {
		return email;
	}






	public void setEmail(String email) {
		this.email = email;
	}






	public Boolean getAlarmenabled() {
		return alarmenabled;
	}






	public void setAlarmenabled(Boolean alarmenabled) {
		this.alarmenabled = alarmenabled;
	}






	public Date getWorkinghours_start() {
		return workinghours_start;
	}






	public void setWorkinghours_start(Date workinghours_start) {
		this.workinghours_start = workinghours_start;
	}






	public Date getWorkinghours_end() {
		return workinghours_end;
	}






	public void setWorkinghours_end(Date workinghours_end) {
		this.workinghours_end = workinghours_end;
	}






	public OperatorCountry getOpco() {
		return opco;
	}






	public void setOpco(OperatorCountry opco) {
		this.opco = opco;
	}






	

}
