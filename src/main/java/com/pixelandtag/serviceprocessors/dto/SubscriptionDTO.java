package com.pixelandtag.serviceprocessors.dto;

import com.pixelandtag.api.ServiceProcessorI;

public class SubscriptionDTO extends ServiceProcessorDTO {

	private int serviceid;
	private ServiceProcessorI processor;
	private String cmd;
	private int push_unique;
	private int processor_id;

	
	public int getProcessor_id() {
		return processor_id;
	}

	public void setProcessor_id(int processor_id) {
		this.processor_id = processor_id;
	}

	public int getPush_unique() {
		return push_unique;
	}

	public void setPush_unique(int push_unique) {
		this.push_unique = push_unique;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public ServiceProcessorI getProcessor() {
		return processor;
	}

	public void setProcessor(ServiceProcessorI processor) {
		this.processor = processor;
	}

	public int getServiceid() {
		return serviceid;
	}

	public void setServiceid(int serviceid) {
		this.serviceid = serviceid;
	}

	@Override
	public String toString() {
		return "SubscriptionDTO [serviceid=" + serviceid + ", processor="
				+ processor + ", cmd=" + cmd + ", push_unique=" + push_unique
				+ ", processor_id=" + processor_id + ", toString()="
				+ super.toString() + "]";
	}

	
	
}
