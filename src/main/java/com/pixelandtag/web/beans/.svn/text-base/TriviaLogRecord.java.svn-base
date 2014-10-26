package com.inmobia.axiata.web.beans;

import java.io.Serializable;

public class TriviaLogRecord implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 770288864048384L;
	
	private String id,msisdn,name,answer,timeStamp,dirty, price="0.0";
	
	private int question_idFK,points = -1;
	
	private int subscriber_profileFK = 0;
	
	private int question_origin = -1;
	
	private boolean winning = false;
	
	
	private int correct = -1;
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("\nid").append(id)
		.append("\nmsisdn").append(msisdn)
		.append("\nname").append(name)
		.append("\nanswer").append(answer)
		.append("\ntimeStamp").append(timeStamp)
		.append("\ndirty").append(dirty)
		.append("\nquestion_idFK").append(question_idFK)
		.append("\npoints").append(points)
		.append("\ncorrect").append(correct)
		.append("\nprice").append(price);
		
		return sb.toString();
	}
	
	
	public int getSubscriber_profileFK() {
		return subscriber_profileFK;
	}


	public void setSubscriber_profileFK(int subscriber_profileFK) {
		this.subscriber_profileFK = subscriber_profileFK;
	}


	public int getQuestion_origin() {
		return question_origin;
	}


	public void setQuestion_origin(int question_origin) {
		this.question_origin = question_origin;
	}


	public boolean isWinningQuestion() {
		return winning;
	}


	public void setWinningQuestion(boolean winning) {
		this.winning = winning;
	}


	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int isCorrect() {
		return correct;
	}
	public void setCorrect(int isCorrect) {
		this.correct = isCorrect;
	}
	public int getQuestion_idFK() {
		return question_idFK;
	}
	public void setQuestion_idFK(int question_idFK) {
		this.question_idFK = question_idFK;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getDirty() {
		return dirty;
	}
	public void setDirty(String dirty) {
		this.dirty = dirty;
	}
	

}
