package com.pixelandtag.cmp.entities.customer.configs;

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

import com.pixelandtag.cmp.entities.customer.OperatorCountry;

@Entity
@Table(name = "opco_templates")
@NamedQueries({
	@NamedQuery(
			name = OpcoTemplates.NQ_FIND_BY_OPCO_AND_NAME,
			query = "select oc from OpcoTemplates oc where oc.type=:type AND oc.name=:name AND oc.opco=:opco  order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = OpcoTemplates.NQ_FIND_BY_OPCOID_AND_NAME,
			query = "select oc from OpcoTemplates oc where oc.type=:type AND oc.name=:name AND oc.opco.id=:opcoid order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = OpcoTemplates.NQ_FIND_BY_OPCO,
			query = "select oc from OpcoTemplates oc where oc.type=:type AND oc.opco=:opco order by oc.effectiveDate desc"
	),
	@NamedQuery(
			name = OpcoTemplates.NQ_FIND_BY_OPCOID,
			query = "select oc from OpcoTemplates oc where oc.type=:type AND oc.opco.id=:opcoid order by oc.effectiveDate desc"
	)
})
public class OpcoTemplates implements Serializable{
	
	@Transient
	public static final String NQ_FIND_BY_OPCO_AND_NAME = "opcotemplates.byopcoandname";
	
	@Transient
	public static final String NQ_FIND_BY_OPCOID_AND_NAME = "opcotemplates.byopcoidandname";
	
	@Transient
	public static final String NQ_FIND_BY_OPCO = "opcotemplates.byopco";
	
	@Transient
	public static final String NQ_FIND_BY_OPCOID = "opcotemplates.byopcoid";

	/**
	 * 
	 */
	private static final long serialVersionUID = -398170859016675363L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(cascade=CascadeType.MERGE, fetch=FetchType.EAGER)
	@JoinColumn(name = "opco_id_fk")
	@Index(name="opctpltfidx")
	private OperatorCountry opco;
	
	@Column(name="type")
	@Enumerated(EnumType.STRING)
	private TemplateType type;
	
	@Column(name="name")
	@Index(name="opctpltfidx")
	private String name;
	
	@Column(name="value", length=10000)
	private String value;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "effectiveDate", nullable = false)
	@Index(name="opctpltfidx")
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

	public OperatorCountry getOpco() {
		return opco;
	}

	public void setOpco(OperatorCountry opco) {
		this.opco = opco;
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
	
	
}
