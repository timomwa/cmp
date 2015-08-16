package com.pixelandtag.cmp.ejb.api.sms;

import java.io.Serializable;
import java.util.Map;

import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.cmp.entities.customer.configs.ProfileTemplate;

public class SenderConfiguration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3428198415123821862L;
	
	Map<String,ProfileConfigs> opcoconfigs;
	Map<String,ProfileTemplate> opcotemplates;
	public Map<String, ProfileConfigs> getOpcoconfigs() {
		return opcoconfigs;
	}
	public void setOpcoconfigs(Map<String, ProfileConfigs> opcoconfigs) {
		this.opcoconfigs = opcoconfigs;
	}
	public Map<String, ProfileTemplate> getOpcotemplates() {
		return opcotemplates;
	}
	public void setOpcotemplates(Map<String, ProfileTemplate> opcotemplates) {
		this.opcotemplates = opcotemplates;
	}
	
	

}
