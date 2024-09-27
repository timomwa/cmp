package com.pixelandtag.billing;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;

@Entity
@Table(name = "opco_biller_profile")
@NamedQueries({
	@NamedQuery(
			name = OpcoBillingProfile.NQ_FIND_BY_OPCO,
			query = "select obp from OpcoBillingProfile obp where obp.opco=:opco AND obp.active=:active  order by obp.pickorder desc"
	),
	@NamedQuery(
			name = OpcoBillingProfile.NQ_LIST_ACTIVE,
			query = "select obp from OpcoBillingProfile obp where obp.active=:active  order by obp.pickorder desc"
	)
})
public class OpcoBillingProfile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3580315637952575965L;
	@Transient
	public static final String NQ_FIND_BY_OPCO = "opcobillingprofile.byopco";
	@Transient
	public static final String NQ_LIST_ACTIVE = "opcobillingprofile.listactive";
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "opco_id_fk")
	@Index(name="blopcproflidx")
	private OperatorCountry opco;
	
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "profile_id_fk", nullable=false)
	@Index(name="blopcproflidx")
	private BillerProfile profile;
	
	
	@Column(name="pickorder", nullable=false)
	@Index(name="blopcproflidx")
	private Integer pickorder;//0 is picked before 1. We order by pickorder desc
	
	@Column(name = "effectiveDate", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date effectiveDate;
	
	@Column(name = "active", nullable = false)
	private Boolean active;
	
	@Column(name="workers", nullable=false)
	private Integer workers;
	
	
	@PrePersist
	public void onCreate(){
		if(workers==null)
			workers = 1;
		if(pickorder==null)
			pickorder = 0;
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


	public BillerProfile getProfile() {
		return profile;
	}


	public void setProfile(BillerProfile profile) {
		this.profile = profile;
	}


	public Integer getPickorder() {
		return pickorder;
	}


	public void setPickorder(Integer pickorder) {
		this.pickorder = pickorder;
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


	public Integer getWorkers() {
		return workers;
	}


	public void setWorkers(Integer workers) {
		this.workers = workers;
	}
}
