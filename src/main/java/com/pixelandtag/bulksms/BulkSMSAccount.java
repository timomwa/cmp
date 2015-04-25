package com.pixelandtag.bulksms;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "bulksms_account")
public class BulkSMSAccount implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2537602333626051349L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "name", unique=true)
	private String accountName;
	
	@Column(name = "uname", unique=true)
	private String username;
	
	@Column(name = "pwd")
	private String password;
	
	@Column(name = "active")
	private Boolean active;
	
	@Column(name = "accountCode", unique=true)
	private String accountCode;
	
	@Column(name = "apiKey")
	private String apiKey;
	
	@Column(name = "dateactivated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date activationDate;
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(activationDate==null)
			activationDate = new Date();
		if(active==null)
			active = new Boolean(false);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getAccountCode() {
		return accountCode;
	}

	public void setAccountCode(String accountCode) {
		this.accountCode = accountCode;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	
	
	

}
