package com.pixelandtag.cmp.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.json.JSONException;
import org.json.JSONObject;


@Entity
@Table(name = "sms_service_metadata")
public class SMSServiceMetaData implements Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="sms_service_id_fk")
	private Long sms_service_id_fk;
	
	@Column(name="meta_field")
	private String meta_field;
	
	@Column(name="meta_value")
	private String meta_value;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSms_service_id_fk() {
		return sms_service_id_fk;
	}

	public void setSms_service_id_fk(Long sms_service_id_fk) {
		this.sms_service_id_fk = sms_service_id_fk;
	}

	public String getMeta_field() {
		return meta_field;
	}

	public void setMeta_field(String meta_field) {
		this.meta_field = meta_field;
	}

	public String getMeta_value() {
		return meta_value;
	}

	public void setMeta_value(String meta_value) {
		this.meta_value = meta_value;
	}
	
	public JSONObject toJson() throws JSONException{
		JSONObject jsob = new JSONObject();
		jsob.put("id", getId());
		jsob.put("sms_service_id_fk", getSms_service_id_fk());
		jsob.put("meta_field", getMeta_field());
		jsob.put("meta_value", getMeta_value());
		return jsob;
		
	}
}
