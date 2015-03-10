package com.pixelandtag.sms.producerthreads;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "dating_disallowedwords")
public class DisallowedWords implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1501179340373697336L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	
	@Column(name = "word")
	@Index(name="dsalwedidx")
	private String word;


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getWord() {
		return word;
	}


	public void setWord(String word) {
		this.word = word;
	}
	
	
	

}
