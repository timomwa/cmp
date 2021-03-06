package com.pixelandtag.cmp.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;



@Entity
@Table(name = "message", uniqueConstraints = @UniqueConstraint(columnNames={"language_id","msg_key", "opco_id"}))
@NamedQueries({
	@NamedQuery(
			name = Message.NQ_FIND_BY_LANG_AND_KEY,
			query = "from Message msg WHERE msg.language_id=:language_id AND msg.key=:key"
	),
	@NamedQuery(
			name = Message.NQ_FIND_BY_LANG_AND_KEY_AND_OPCOID,
			query = "from Message msg WHERE msg.language_id=:language_id AND msg.key=:key AND msg.opcoid=:opcoid"
	)
})
public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5195495880317013179L;

	
	@Transient
	public static final String NQ_FIND_BY_LANG_AND_KEY = "message_find_by_langid_and_key";
	
	@Transient
	public static final String NQ_FIND_BY_LANG_AND_KEY_AND_OPCOID = "message_find_by_langid_and_key_and_opcoid";
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="language_id")
	private Long language_id;
	
	@Column(name="msg_key", length=50)
	private String key;
	
	@Column(name="message", length=1000)
	private String message;
	
	@Column(name="opco_id")
	private Long opcoid;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

	public Long getLanguage_id() {
		return language_id;
	}

	public void setLanguage_id(Long language_id) {
		this.language_id = language_id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getOpcoid() {
		return opcoid;
	}

	public void setOpcoid(Long opcoid) {
		this.opcoid = opcoid;
	}
	
	
}
