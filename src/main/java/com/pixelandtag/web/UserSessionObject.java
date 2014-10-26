package com.pixelandtag.web;
/**
 * 
 * @author Timothy Mwangi
 * Session object for the purpose of keeping session
 * data
 *
 */
public class UserSessionObject {

	
	private String username,password,first_name,last_name,registered;
	private boolean active;
	private int role,id,store_id_fk;
	
	

	public int getStore_id_fk() {
		return store_id_fk;
	}

	public void setStore_id_fk(int store_id_fk) {
		this.store_id_fk = store_id_fk;
	}

	public synchronized boolean isActive() {
		return active;
	}

	public synchronized void setActive(boolean active) {
		this.active = active;
	}

	public synchronized int getId() {
		return id;
	}

	public synchronized void setId(int id) {
		this.id = id;
	}
	public  String getFirst_name() {
		return first_name;
	}

	public  void setFirst_name(String firstName) {
		first_name = firstName;
	}

	public  String getLast_name() {
		return last_name;
	}

	public  void setLast_name(String lastName) {
		last_name = lastName;
	}

	

	
	
	public synchronized String getRegistered() {
		return registered;
	}

	public synchronized void setRegistered(String registered) {
		this.registered = registered;
	}

	public  String getUsername() {
		return username;
	}

	public  void setUsername(String username) {
		this.username = username;
	}

	public int getRole() {
		return role;
	}

	public  void setRole(int role) {
		this.role = role;
	}

	public  String getPassword() {
		return password;
	}

	public  void setPassword(String password) {
		this.password = password;
	}
	
}
