package com.inmobia.axiata.web.beans;

import java.io.Serializable;

public class Question implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1124L;

	private int id,difficulty,question_origin,language_id;
	
	private String question,timeStampOfInsersion;
	
	private Answer answer;
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("id : ").append(id)
		.append("\ndifficulty : ").append(difficulty)
		.append("\nquestion_origin : ").append(question_origin)
		.append("\nlanguage_id : ").append(language_id)
		.append("\nquestion : ").append(question)
		.append("\ntimeStampOfInsersion : ").append(timeStampOfInsersion)
		.append("\nanswer : ").append(answer.toString());
		return sb.toString();
	}
	
	public Answer getAnswer() {
		return answer;
	}
	public void setAnswer(Answer answer) {
		this.answer = answer;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getDifficulty() {
		return difficulty;
	}
	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}
	public int getQuestion_origin() {
		return question_origin;
	}
	public void setQuestion_origin(int question_origin) {
		this.question_origin = question_origin;
	}
	public int getLanguage_id() {
		return language_id;
	}
	public void setLanguage_id(int language_id) {
		this.language_id = language_id;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public String getTimeStampOfInsersion() {
		return timeStampOfInsersion;
	}
	public void setTimeStampOfInsersion(String timeStampOfInsersion) {
		this.timeStampOfInsersion = timeStampOfInsersion;
	}
	

}
