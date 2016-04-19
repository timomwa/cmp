package com.pixelandtag.billing;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.NamedQueries;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "biller_profile_configs")
@NamedQueries({
	@NamedQuery(
			name = BillerProfileConfig.NQ_FIND_BY_PROFILE_AND_NAME,
			query = "select oc from BillerProfileConfig oc where oc.name=:name AND oc.profile=:profile  order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = BillerProfileConfig.NQ_FIND_BY_PROFILEID_AND_NAME,
			query = "select oc from BillerProfileConfig oc where oc.name=:name AND oc.profile.id=:profileid order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = BillerProfileConfig.NQ_FIND_BY_PROFILE,
			query = "select oc from BillerProfileConfig oc where oc.profile=:profile order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = BillerProfileConfig.NQ_FIND_BY_PROFILEID,
			query = "select oc from BillerProfileConfig oc where oc.profile.id=:profileid order by oc.effectiveDate desc"
	)
})
public class BillerProfileConfig {
	
	@Transient
	public static final String NQ_FIND_BY_PROFILE_AND_NAME = "bpprofileconfigs.byprofileandname";
	
	@Transient
	public static final String NQ_FIND_BY_PROFILEID_AND_NAME = "bpprofileconfigs.byprofileidandname";
	
	@Transient
	public static final String NQ_FIND_BY_PROFILE = "bpprofileconfigs.byprofile";
	
	@Transient
	public static final String NQ_FIND_BY_PROFILEID = "bpprofileconfigs.byprofileid";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="name")
	@Index(name="bproflconfidx")
	private String name;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "effectiveDate", nullable = false)
	@Index(name="bproflconfidx")
	private Date effectiveDate;
	
	
	@Column(name="value")
	private String value;
	
	@Column(name="data_type")
	private String data_type;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "profile_id_fk")
	@Index(name="bproflconfidx")
	private BillerProfile profile;
		
	
	@PrePersist
	public void onCreate(){
		if(effectiveDate==null)
			effectiveDate = new Date();
		if(data_type==null)
			data_type = "string";
	}

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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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

	public BillerProfile getProfile() {
		return profile;
	}

	public void setProfile(BillerProfile profile) {
		this.profile = profile;
	}

}
