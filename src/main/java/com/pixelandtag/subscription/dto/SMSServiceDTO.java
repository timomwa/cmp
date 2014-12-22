package com.pixelandtag.subscription.dto;

/**
 * represents `celcom`.`sms_service`
 * @author tim
 *
 */
public class SMSServiceDTO {
	
	private int id;
	private int mo_processor_FK;
	private String cmd;
	private boolean push_unique;
	private String service_name;
	private String service_description;
	private double price;
	private String cmp_keyword;
	private String cmp_skeyword;
	private boolean enabled;
	private boolean split_mt;
	private String pricePointKeyword;
	
	public boolean isSplit_mt() {
		return split_mt;
	}
	public void setSplit_mt(boolean split_mt) {
		this.split_mt = split_mt;
	}
	public int getId() {
		return id;
	}
	public int getMo_processor_FK() {
		return mo_processor_FK;
	}
	public String getCmd() {
		return cmd;
	}
	public boolean isPush_unique() {
		return push_unique;
	}
	public String getService_name() {
		return service_name;
	}
	public String getService_description() {
		return service_description;
	}
	public double getPrice() {
		return price;
	}
	public String getCmp_keyword() {
		return cmp_keyword;
	}
	public String getCmp_skeyword() {
		return cmp_skeyword;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setMo_processor_FK(int mo_processor_FK) {
		this.mo_processor_FK = mo_processor_FK;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public void setPush_unique(boolean push_unique) {
		this.push_unique = push_unique;
	}
	public void setService_name(String service_name) {
		this.service_name = service_name;
	}
	public void setService_description(String service_description) {
		this.service_description = service_description;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public void setCmp_keyword(String cmp_keyword) {
		this.cmp_keyword = cmp_keyword;
	}
	public void setCmp_skeyword(String cmp_skeyword) {
		this.cmp_skeyword = cmp_skeyword;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getPricePointKeyword() {
		return pricePointKeyword;
	}
	public void setPricePointKeyword(String pricePointKeyword) {
		this.pricePointKeyword = pricePointKeyword;
	}
	@Override
	public String toString() {
		return "SMSServiceDTO [id=" + id + ", mo_processor_FK="
				+ mo_processor_FK + ", cmd=" + cmd + ", push_unique="
				+ push_unique + ", service_name=" + service_name
				+ ", service_description=" + service_description + ", price="
				+ price + ", cmp_keyword=" + cmp_keyword + ", cmp_skeyword="
				+ cmp_skeyword + ", enabled=" + enabled + ", split_mt="
				+ split_mt + ", pricePointKeyword=" + pricePointKeyword + "]";
	}
	
		

}
