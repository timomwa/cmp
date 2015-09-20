package com.pixelandtag.billing.entities;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

import com.pixelandtag.billing.BillerProfile;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;
@Entity
@Table(name = "biller_profile_templates")
@NamedQueries({
	@NamedQuery(
			name = BillerProfileTemplate.NQ_FIND_BY_PROFILE_AND_NAME,
			query = "select oc from BillerProfileTemplate oc where oc.type=:type AND oc.name=:name AND oc.profile=:profile  order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = BillerProfileTemplate.NQ_FIND_BY_PROFILEID_AND_NAME,
			query = "select oc from BillerProfileTemplate oc where oc.type=:type AND oc.name=:name AND oc.profile.id=:profileid order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = BillerProfileTemplate.NQ_FIND_BY_PROFILE,
			query = "select oc from BillerProfileTemplate oc where oc.type=:type AND oc.profile=:profile order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = BillerProfileTemplate.NQ_FIND_BY_PROFILEID,
			query = "select oc from BillerProfileTemplate oc where oc.type=:type AND oc.profile.id=:profileid order by oc.effectiveDate desc"
	)
})
public class BillerProfileTemplate implements Serializable{
	
	@Transient
	public static final String NQ_FIND_BY_PROFILE_AND_NAME = "bpprofiletemplates.byprofileandname";
	
	@Transient
	public static final String NQ_FIND_BY_PROFILEID_AND_NAME = "bpprofiletemplates.byprofileidandname";
	
	@Transient
	public static final String NQ_FIND_BY_PROFILE = "bpprofiletemplates.byprofile";
	
	@Transient
	public static final String NQ_FIND_BY_PROFILEID = "bpprofiletemplates.byprofileid";

	/**
	 * 
	 */
	private static final long serialVersionUID = 4999816906925532354L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "profile_id_fk")
	@Index(name="bpopctpltfidx")
	private BillerProfile profile;
	
	@Column(name="type")
	@Enumerated(EnumType.STRING)
	private TemplateType type;
	
	@Column(name="name")
	@Index(name="bpopctpltfidx")
	private String name;
	
	@Column(name="value", length=10000)
	private String value;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "effectiveDate", nullable = false)
	@Index(name="bpopctpltfidx")
	private Date effectiveDate;
	
	@PrePersist
	public void onCreate(){
		if(effectiveDate==null)
			effectiveDate = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TemplateType getType() {
		return type;
	}

	public void setType(TemplateType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public BillerProfile getProfile() {
		return profile;
	}

	public void setProfile(BillerProfile profile) {
		this.profile = profile;
	}

}
