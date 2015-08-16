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
			name = OpcoSenderProfile.NQ_FIND_BY_OPCO,
			query = "select osp from OpcoSenderProfile osp where osp.opco=:opco AND osp.active=:active  order by osp.pickorder desc"
	)
})
public class OpcoSenderProfile implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -5584961608967143955L;
	
	@Transient
	public static final String NQ_FIND_BY_OPCO = "opcosenderprofile.byopco";
	
	
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
	private SenderProfile profile;
	
	@Column(name="pickorder", nullable=false)
	@Index(name="opcproflidx")
	private Integer pickorder;//0 is picked before 1. We order by pickorder desc
	
	
	@Column(name = "effectiveDate", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date effectiveDate;
	
	@Column(name = "active", nullable = false)
	private Boolean active;
	
	
	@PrePersist
	public void onCreate(){
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


	public SenderProfile getProfile() {
		return profile;
	}


	public void setProfile(SenderProfile profile) {
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

	
	
}
