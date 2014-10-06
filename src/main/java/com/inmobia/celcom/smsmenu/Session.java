package com.inmobia.celcom.smsmenu;

public class Session {

	private int id;
	private String msisdn;
	private int language_id;
	private int smsmenu_level_id_fk;
	private String timeStamp;
	private MenuItem menu_item;
	
	public int getId() {
		return id;
	}
	public int getLanguage_id() {
		return language_id;
	}
	public void setLanguage_id(int language_id) {
		this.language_id = language_id;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public int getSmsmenu_level_id_fk() {
		return smsmenu_level_id_fk;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public void setSmsmenu_level_id_fk(int smsmenu_level_id_fk) {
		this.smsmenu_level_id_fk = smsmenu_level_id_fk;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public MenuItem getMenu_item() {
		return menu_item;
	}
	public void setMenu_item(MenuItem menu_item) {
		this.menu_item = menu_item;
	}
	@Override
	public String toString() {
		return "Session [id=" + id + ", msisdn=" + msisdn
				+ ", smsmenu_level_id_fk=" + smsmenu_level_id_fk
				+ ", timeStamp=" + timeStamp + ", menu_item=" + menu_item + "]";
	}
	
	
	
}
