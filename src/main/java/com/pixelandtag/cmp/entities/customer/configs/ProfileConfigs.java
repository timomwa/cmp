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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;


@Entity
@Table(name = "profile_configs")
@NamedQueries({
	@NamedQuery(
			name = ProfileConfigs.NQ_FIND_BY_PROFILE_AND_NAME,
			query = "select oc from ProfileConfigs oc where oc.name=:name AND oc.profile=:profile  order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = ProfileConfigs.NQ_FIND_BY_PROFILEID_AND_NAME,
			query = "select oc from ProfileConfigs oc where oc.name=:name AND oc.profile.id=:profileid order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = ProfileConfigs.NQ_FIND_BY_PROFILE,
			query = "select oc from ProfileConfigs oc where oc.profile=:profile order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = ProfileConfigs.NQ_FIND_BY_PROFILEID,
			query = "select oc from ProfileConfigs oc where oc.profile.id=:profileid order by oc.effectiveDate desc"
	)
})
public class ProfileConfigs implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7403355329478440828L;
	
	@Transient
	public static final String NQ_FIND_BY_PROFILE_AND_NAME = "profileconfigs.byprofileandname";
	
	@Transient
	public static final String NQ_FIND_BY_PROFILEID_AND_NAME = "profileconfigs.byprofileidandname";
	
	@Transient
	public static final String NQ_FIND_BY_PROFILE = "profileconfigs.byprofile";
	
	@Transient
	public static final String NQ_FIND_BY_PROFILEID = "profileconfigs.byprofileid";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="name")
	@Index(name="proflconfidx")
	private String name;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "effectiveDate", nullable = false)
	@Index(name="proflconfidx")
	private Date effectiveDate;
	
	
	@Column(name="value")
	private String value;
	
	@Column(name="data_type")
	private String data_type;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "profile_id_fk")
	@Index(name="proflconfidx")
	private SenderProfile profile;
	
	
	
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

	public SenderProfile getProfile() {
		return profile;
	}

	public void setProfile(SenderProfile profile) {
		this.profile = profile;
	}
	
	
	
	

}
