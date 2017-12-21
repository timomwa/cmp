package com.pixelandtag.cmp.entities.customer.configs;

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
@Table(name = "opco_senderprofiles")
@NamedQueries({
	@NamedQuery(
			name = OpcoSenderReceiverProfile.NQ_FIND_BY_OPCO,
			query = "select osp from OpcoSenderReceiverProfile osp where osp.opco=:opco AND osp.active=:active  order by osp.pickorder desc"
	),
	@NamedQuery(
			name = OpcoSenderReceiverProfile.NQ_FIND_BY_OPCO_AND_TYPE,
			query = "select osp from OpcoSenderReceiverProfile osp where osp.opco=:opco AND osp.active=:active AND osp.profile.profiletype=:profiletype  order by osp.pickorder desc"
	),
	@NamedQuery(
			name = OpcoSenderReceiverProfile.NQ_LIST_ACTIVE,
			query = "select osp from OpcoSenderReceiverProfile osp where osp.active=:active  order by osp.pickorder desc"
	)
})
public class OpcoSenderReceiverProfile implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -5584961608967143955L;
	
	@Transient
	public static final String NQ_FIND_BY_OPCO = "opcosenderprofile.byopco";
	
	@Transient
	public static final String NQ_FIND_BY_OPCO_AND_TYPE = "opcosenderprofile.byopcoandtype";
	
	@Transient
	public static final String NQ_LIST_ACTIVE = "opcosenderprofile.listactive";
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "opco_id_fk")
	@Index(name="opcproflidx")
	private OperatorCountry opco;
	
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "profile_id_fk", nullable=false)
	@Index(name="opcproflidx")
	private SenderReceiverProfile profile;
	
	@Column(name="pickorder", nullable=false)
	@Index(name="opcproflidx")
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


	public SenderReceiverProfile getProfile() {
		return profile;
	}


	public void setProfile(SenderReceiverProfile profile) {
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
