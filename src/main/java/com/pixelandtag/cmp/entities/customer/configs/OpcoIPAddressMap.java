package com.pixelandtag.cmp.entities.customer.configs;

import java.io.Serializable;

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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;

@Entity
@Table(name = "opco_ip_map")
@NamedQueries({
	@NamedQuery(
			name = OpcoIPAddressMap.NQ_FIND_BY_IP_ADDRESS,
			query = "select mp from OpcoIPAddressMap mp where mp.ipaddress=:ipaddress order by mp.id desc"
	)
})
public class OpcoIPAddressMap implements Serializable {
	
	@Transient
	public static final String NQ_FIND_BY_IP_ADDRESS = "opcoipaddress.findby_ip_address";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8745511729248391994L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "opco_id_fk", nullable=false)
	@Index(name="opcipadmapidx")
	private OperatorCountry opco;
	
	@Column(name="ipaddress")
	private String ipaddress;

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

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}
	
	
	

}
