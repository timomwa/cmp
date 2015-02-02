package com.pixelandtag.dating.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "dating_systemmatchlog")
public class SystemMatchLog  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 995864447633225925L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "person_a_id")
	@Index(name="psnAidx")
	private Long person_a_id;
	
	@Column(name = "person_b_id")
	@Index(name="psnAidx")
	private Long person_b_id;
	
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationDate", nullable = false)
	private Date creationDate;
	
	@Column(name = "person_a_notified")
	private Boolean person_a_notified;
	
	@Column(name = "person_b_notified")
	private Boolean person_b_notified;
	
	@PrePersist
	public void onCreate(){
		if(creationDate==null)
			creationDate = new Date();
		if(person_a_notified==null)
			person_a_notified = new Boolean(false);
		if(person_b_notified==null)
			person_b_notified = new Boolean(false);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPerson_a_id() {
		return person_a_id;
	}

	public void setPerson_a_id(Long person_a_id) {
		this.person_a_id = person_a_id;
	}

	public Long getPerson_b_id() {
		return person_b_id;
	}

	public void setPerson_b_id(Long person_b_id) {
		this.person_b_id = person_b_id;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Boolean getPerson_a_notified() {
		return person_a_notified;
	}

	public void setPerson_a_notified(Boolean person_a_notified) {
		this.person_a_notified = person_a_notified;
	}

	public Boolean getPerson_b_notified() {
		return person_b_notified;
	}

	public void setPerson_b_notified(Boolean person_b_notified) {
		this.person_b_notified = person_b_notified;
	}

	
	

}
