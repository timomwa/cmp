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
@Table(name = "opco_configs")
@NamedQueries({
	@NamedQuery(
			name = OpcoConfigs.NQ_FIND_BY_OPCO_AND_NAME,
			query = "select oc from OpcoConfigs oc where oc.name=:name AND oc.opco=:opco  order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = OpcoConfigs.NQ_FIND_BY_OPCOID_AND_NAME,
			query = "select oc from OpcoConfigs oc where oc.name=:name AND oc.opco.id=:opcoid order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = OpcoConfigs.NQ_FIND_BY_OPCO,
			query = "select oc from OpcoConfigs oc where oc.opco=:opco order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = OpcoConfigs.NQ_FIND_BY_OPCOID,
			query = "select oc from OpcoConfigs oc where oc.opco.id=:opcoid order by oc.effectiveDate desc"
	)
})
public class OpcoConfigs implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7403355329478440828L;
	
	@Transient
	public static final String NQ_FIND_BY_OPCO_AND_NAME = "opcoconfigs.byopcoandname";
	
	@Transient
	public static final String NQ_FIND_BY_OPCOID_AND_NAME = "opcoconfigs.byopcoidandname";
	
	@Transient
	public static final String NQ_FIND_BY_OPCO = "opcoconfigs.byopco";
	
	@Transient
	public static final String NQ_FIND_BY_OPCOID = "opcoconfigs.byopcoid";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(cascade=CascadeType.MERGE, fetch=FetchType.EAGER)
	@JoinColumn(name = "opco_id_fk")
	@Index(name="opcconfidx")
	private OperatorCountry opco;
	
	@Column(name="name")
	@Index(name="opcconfidx")
	private String name;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "effectiveDate", nullable = false)
	@Index(name="opcconfidx")
	private Date effectiveDate;
	
	
	@Column(name="value")
	private String value;
	
	@Column(name="data_type")
	private String data_type;
	
	
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

	public OperatorCountry getOpco() {
		return opco;
	}

	public void setOpco(OperatorCountry opco) {
		this.opco = opco;
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
	
	
	
	

}
