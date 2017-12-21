package com.pixelandtag.cmp.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Entity
@Table(name = "smsmenu_levels")
public class SMSMenuLevels implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -4735063375711392570L;

	@Transient
	private List<SMSMenuLevels> children = new ArrayList<SMSMenuLevels>();
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="language_id")
	private Long language_id;
	
	@Column(name="parent_level_id")
	private Long parent_level_id;
	
	@Column(name="menu_id")
	private Long menu_id;
	
	@Column(name="serviceid")
	private Long serviceid;
	
	@Column(name="visible")
	private Boolean visible;
	
	@Column(name="ussdTag",length=30)
	@Index(name="ussdtgidx")
	private String ussdTag = "";

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getLanguage_id() {
		return language_id;
	}

	public void setLanguage_id(Long language_id) {
		this.language_id = language_id;
	}

	public Long getParent_level_id() {
		return parent_level_id;
	}

	public void setParent_level_id(Long parent_level_id) {
		this.parent_level_id = parent_level_id;
	}

	public Long getMenu_id() {
		return menu_id;
	}

	public void setMenu_id(Long menu_id) {
		this.menu_id = menu_id;
	}

	public Long getServiceid() {
		return serviceid;
	}

	public void setServiceid(Long serviceid) {
		this.serviceid = serviceid;
	}
	
	
	
	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}
	
	public List<SMSMenuLevels> getChildren() {
		return children;
	}

	public void setChildren(List<SMSMenuLevels> children) {
		this.children = children;
	}

	public String getUssdTag() {
		return ussdTag;
	}

	public void setUssdTag(String ussdTag) {
		this.ussdTag = ussdTag;
	}
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(ussdTag==null)
			ussdTag = "";
	}
	
	
	public JSONObject toJson() throws JSONException{
		JSONObject obj = new JSONObject();
		obj.put("id", getId());
		obj.put("name", getName());
		obj.put("language_id", getLanguage_id());
		obj.put("parent_level_id", getParent_level_id());
		obj.put("menu_id", getMenu_id());
		obj.put("serviceid", getServiceid());
		obj.put("visible", getVisible());
		obj.put("ussdTag", getUssdTag());
		JSONArray jsar = new JSONArray();
		for(SMSMenuLevels chld : children){
			JSONObject childrn = new JSONObject();
			childrn.put("id", chld.getId());
			childrn.put("name", chld.getName());
			childrn.put("language_id", chld.getLanguage_id());
			childrn.put("parent_level_id", chld.getParent_level_id());
			childrn.put("menu_id", chld.getMenu_id());
			childrn.put("serviceid", chld.getServiceid());
			childrn.put("visible", chld.getVisible());
			childrn.put("ussdTag", chld.getUssdTag());
			jsar.put(childrn);
		}
		obj.put("children", jsar);
		
		return obj;
	}
}
