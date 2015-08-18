package com.pixelandtag.cmp.entities.customer.configs;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * When changes happen in the system, we
 * need to reload any cache'd configurations
 * @author Timothy Mwangi Gikonyo
 * Aug 18th 2015
 *
 */
@Entity
@Table(name = "cache_reset_cue")
public class CacheResetCue implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 495463161287926552L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="module")
	private String module;
	
	@Column(name="reset")
	private Boolean reset;
	
	@Column(name="resetafter")
	private Long resetafter;//Re-set the cache after how many hits/itterations ?

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public Boolean getReset() {
		return reset;
	}

	public void setReset(Boolean reset) {
		this.reset = reset;
	}

	public Long getResetafter() {
		return resetafter;
	}

	public void setResetafter(Long resetafter) {
		this.resetafter = resetafter;
	}
	
	
	
	

}
