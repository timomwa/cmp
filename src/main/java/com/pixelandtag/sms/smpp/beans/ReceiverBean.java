package com.pixelandtag.sms.smpp.beans;

public class ReceiverBean {

	private long smppid;
	private String serverip;
	private int serverport;
	private String type;
	private String username;
	private String password;
	private String shortcode;
	private int version;
	private int altsernderid;
	
	
	public ReceiverBean(long smppid, String serverip, int serverport, String type, String username, String password,
			String shortcode, int version, int altsernderid) {
		super();
		this.smppid = smppid;
		this.serverip = serverip;
		this.serverport = serverport;
		this.type = type;
		this.username = username;
		this.password = password;
		this.shortcode = shortcode;
		this.version = version;
		this.altsernderid = altsernderid;
	}
	
	public long getSmppid() {
		return smppid;
	}
	public void setSmppid(long smppid) {
		this.smppid = smppid;
	}
	public String getServerip() {
		return serverip;
	}
	public void setServerip(String serverip) {
		this.serverip = serverip;
	}
	public int getServerport() {
		return serverport;
	}
	public void setServerport(int serverport) {
		this.serverport = serverport;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getShortcode() {
		return shortcode;
	}
	public void setShortcode(String shortcode) {
		this.shortcode = shortcode;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public int getAltsernderid() {
		return altsernderid;
	}
	public void setAltsernderid(int altsernderid) {
		this.altsernderid = altsernderid;
	}
}