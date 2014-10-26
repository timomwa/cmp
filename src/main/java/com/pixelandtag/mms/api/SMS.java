package com.pixelandtag.mms.api;

public class SMS {

	String id,text,available,dateOfInsertion;

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public String getAvailable() {
		return available;
	}

	public String getDateOfInsertion() {
		return dateOfInsertion;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setAvailable(String available) {
		this.available = available;
	}

	public void setDateOfInsertion(String dateOfInsertion) {
		this.dateOfInsertion = dateOfInsertion;
	}

	@Override
	public String toString() {
		return "SMS [id=" + id + ", text=" + text + ", available=" + available
				+ ", dateOfInsertion=" + dateOfInsertion + "]";
	}
	
	
	
}
