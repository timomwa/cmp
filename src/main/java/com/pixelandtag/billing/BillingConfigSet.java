package com.pixelandtag.billing;

import java.io.Serializable;
import java.util.Map;

import com.pixelandtag.billing.entities.BillerProfileTemplate;

public class BillingConfigSet implements Serializable {

	
	private static final long serialVersionUID = -3391125607025395267L;
	
	Map<String,BillerProfileConfig> opcoconfigs;
	Map<String,BillerProfileTemplate> opcotemplates;
	
	public Map<String, BillerProfileConfig> getOpcoconfigs() {
		return opcoconfigs;
	}
	public void setOpcoconfigs(Map<String, BillerProfileConfig> opcoconfigs) {
		this.opcoconfigs = opcoconfigs;
	}
	public Map<String, BillerProfileTemplate> getOpcotemplates() {
		return opcotemplates;
	}
	public void setOpcotemplates(Map<String, BillerProfileTemplate> opcotemplates) {
		this.opcotemplates = opcotemplates;
	}
	
	
	

}
