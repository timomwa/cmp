package com.pixelandtag.cmp.ejb.api.sms;

import java.io.Serializable;
import java.util.Map;

import com.pixelandtag.cmp.entities.customer.configs.OpcoConfigs;
import com.pixelandtag.cmp.entities.customer.configs.OpcoTemplates;

public class SenderConfiguration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3428198415123821862L;
	
	Map<String,OpcoConfigs> opcoconfigs;
	Map<String,OpcoTemplates> opcotemplates;
	public Map<String, OpcoConfigs> getOpcoconfigs() {
		return opcoconfigs;
	}
	public void setOpcoconfigs(Map<String, OpcoConfigs> opcoconfigs) {
		this.opcoconfigs = opcoconfigs;
	}
	public Map<String, OpcoTemplates> getOpcotemplates() {
		return opcotemplates;
	}
	public void setOpcotemplates(Map<String, OpcoTemplates> opcotemplates) {
		this.opcotemplates = opcotemplates;
	}
	
	

}
