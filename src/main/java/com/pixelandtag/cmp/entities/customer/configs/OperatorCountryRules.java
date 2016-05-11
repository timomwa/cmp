package com.pixelandtag.cmp.entities.customer.configs;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;

@Entity
@Table(name = "opco_rules")
@NamedQueries({
	@NamedQuery(
			name = OperatorCountryRules.NQ_FIND_BY_NAME_AND_OPCO,
			query = "from OperatorCountryRules ocrl WHERE ocrl.opco=:opco AND ocrl.rule_name=:rule_name"
	)}
)
public class OperatorCountryRules implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1001805634163403994L;


	public static final String NQ_FIND_BY_NAME_AND_OPCO = "opco_rules.nq_findbynameandopco";
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "opco_id_fk")
	@Index(name="opcrleidx")
	private OperatorCountry opco;
	
	@Column(name="rule_name")
	private String rule_name;
	
	@Column(name="rule_value")
	private String rule_value;
	
	@Column(name="data_type")
	private String data_type;
	
	
	@Column(name="effectiveDate")
	@Index(name="eftstampidx")
	@Temporal(TemporalType.TIMESTAMP)
	private Date effectiveDate;
	
	@Column(name="active")
	private Boolean active;
	
	
	@PrePersist
	@PreUpdate
	public void update(){
		
		if(effectiveDate==null)
			effectiveDate = new Date();
		
		if(active==null)
			active = Boolean.TRUE;
		
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public OperatorCountry getOpco() {
		return opco;
	}


	public void setOpco(OperatorCountry opco) {
		this.opco = opco;
	}


	public String getRule_name() {
		return rule_name;
	}


	public void setRule_name(String rule_name) {
		this.rule_name = rule_name;
	}


	public String getRule_value() {
		return rule_value;
	}


	public void setRule_value(String rule_value) {
		this.rule_value = rule_value;
	}


	public String getData_type() {
		return data_type;
	}


	public void setData_type(String data_type) {
		this.data_type = data_type;
	}


	public Date getEffectiveDate() {
		return effectiveDate;
	}


	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}


	public Boolean getActive() {
		return active;
	}


	public void setActive(Boolean active) {
		this.active = active;
	}


	@Override
	public String toString() {
		return "OperatorCountryRules [id=" + id + ", opco=" + opco
				+ ", rule_name=" + rule_name + ", rule_value=" + rule_value
				+ ", data_type=" + data_type + ", effectiveDate="
				+ effectiveDate + ", active=" + active + "]";
	}
	
}
