package com.pixelandtag.sms.producerthreads;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import com.pixelandtag.smsmenu.MenuItem;

@Entity
@Table(name = "ussd_session")
public class USSDSession  implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -645334545617235306L;
	@Transient
	private MenuItem menu_item;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	@Column(name = "msisdn")
	@Index(name="smsidnidx")
	private String msisdn;
	
	@Column(name = "smsmenu_levels_id_fk")
	private Long smsmenu_levels_id_fk;
	
	@Column(name = "language_id")
	private Long language_id;
	
	@Column(name = "timeStamp")
	@Index(name="ststpidx")
	private Date timeStamp;
	
	@Column(name = "menuid")
	private Long menuid;
	
	@Column(name = "sessionId")
	@Index(name="ssessidx")
	private BigInteger sessionId;
	
	
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(timeStamp==null)
			timeStamp = new Date();
	}



	public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}



	public String getMsisdn() {
		return msisdn;
	}



	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}



	public Long getSmsmenu_levels_id_fk() {
		return smsmenu_levels_id_fk;
	}



	public void setSmsmenu_levels_id_fk(Long smsmenu_levels_id_fk) {
		this.smsmenu_levels_id_fk = smsmenu_levels_id_fk;
	}



	public Long getLanguage_id() {
		return language_id;
	}



	public void setLanguage_id(Long language_id) {
		this.language_id = language_id;
	}



	public Date getTimeStamp() {
		return timeStamp;
	}



	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}



	public Long getMenuid() {
		return menuid;
	}



	public void setMenuid(Long menuid) {
		this.menuid = menuid;
	}



	public BigInteger getSessionId() {
		return sessionId;
	}



	public void setSessionId(BigInteger sessionId) {
		this.sessionId = sessionId;
	}



	public MenuItem getMenu_item() {
		return menu_item;
	}



	public void setMenu_item(MenuItem menu_item) {
		this.menu_item = menu_item;
	}
	
	
	

}
