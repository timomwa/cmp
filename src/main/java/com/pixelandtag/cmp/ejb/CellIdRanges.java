package com.pixelandtag.cmp.ejb;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "dating_cellidranges")
public class CellIdRanges implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2941170027288312246L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "min_cell_id", nullable = false)
	@Index(name="crmnclidx")
	private Long min_cell_id;
	
	@Column(name = "max_cell_id", nullable = false)
	@Index(name="crmxclidx")
	private Long max_cell_id;
	
	@Column(name = "location_id", nullable = false, unique=true)
	@Index(name="rlocididx")
	private Long location_id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMin_cell_id() {
		return min_cell_id;
	}

	public void setMin_cell_id(Long min_cell_id) {
		this.min_cell_id = min_cell_id;
	}

	public Long getMax_cell_id() {
		return max_cell_id;
	}

	public void setMax_cell_id(Long max_cell_id) {
		this.max_cell_id = max_cell_id;
	}

	public Long getLocation_id() {
		return location_id;
	}

	public void setLocation_id(Long location_id) {
		this.location_id = location_id;
	}
	
	

}
