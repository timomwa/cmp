package com.pixelandtag.cmp.entities.audit;

import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

import com.pixelandtag.cmp.entities.User;

@Entity
@Table(name = "audit_trail")
public class AuditTrail implements Serializable{
	
	public AuditTrail(){
		
	}
	
	private AuditTrail(UserActionBuilder builder){
		this.user = builder.user;
		this.username = builder.username;
		this.module = builder.module;
		this.process = builder.process;
		this.objectAffected = builder.objectAffected;
		this.timeStamp = builder.timeStamp;
		this.timeZone = builder.timeZone;
		this.data = builder.data;
		this.remotehost = builder.remotehost;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8933473444448618653L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(cascade=CascadeType.MERGE, fetch=FetchType.EAGER)
	@JoinColumn(name = "user_id_fk")
	@Index(name="uactidx")
	private User user;
	
	@Column(name = "username", length=25)
	@Index(name="uactidx")
	private String username;
	
	@Column(name = "module")
	private String module;
	
	@Column(name = "process")
	private String process;
	
	@Column(name = "objectAffected")
	private String objectAffected;
	
	@Column(name = "timeStamp")
	@Index(name="uactidx")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timeStamp;
	
	@Column(name = "timeZone")
	private String timeZone;
	
	
	@Column(name = "data", length=4096)
	private String data;
	
	@Column(name = "remotehost")
	private String remotehost;
	
	
	@PrePersist
	@PreUpdate
	public void setDefaults(){
		if(timeStamp==null)
			timeStamp = new Date();
		if(timeZone==null || timeZone.isEmpty())
			timeZone = TimeZone.getDefault().getID();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public String getObjectAffected() {
		return objectAffected;
	}

	public void setObjectAffected(String objectAffected) {
		this.objectAffected = objectAffected;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
		
	}
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}


	public String getRemotehost() {
		return remotehost;
	}

	public void setRemotehost(String remotehost) {
		this.remotehost = remotehost;
	}


	public static class UserActionBuilder{
		private final User user;
		private String username;
		private String remotehost;
		private String module;
		private String process;
		private String objectAffected;
		private Date timeStamp;
		private String timeZone;
		private String data;
		
		
		public UserActionBuilder(User user_){
			this.user = user_;
			this.username  = user_.getUsername();
		}
		
		public UserActionBuilder remoteHost(String remoteHost_){
			this.remotehost = remoteHost_;
			return this;
		}
		
		public UserActionBuilder module(String module_){
			this.module = module_;
			return this;
		}
		
		public UserActionBuilder process(String process_){
			this.process = process_;
			return this;
		}
		
		public UserActionBuilder objectAffected(String objectAffected_){
			this.objectAffected = objectAffected_;
			return this;
		}
		
		public UserActionBuilder timeStamp(Date timeStamp_){
			this.timeStamp = timeStamp_;
			return this;
		}
		
		public UserActionBuilder timeZone(String timeZone_){
			this.timeZone = timeZone_;
			return this;
		}
		
		public UserActionBuilder data(String data_){
			this.data = data_;
			return this;
		}
		
		public AuditTrail build(){
			return new AuditTrail(this);
		}
	}
	
	

}
