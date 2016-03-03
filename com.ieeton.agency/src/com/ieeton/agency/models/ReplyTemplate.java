package com.ieeton.agency.models;

import org.json.JSONObject;

public class ReplyTemplate {
	private int id;
	private String content;
	
	public ReplyTemplate(int id, String content){
		this.id = id;
		this.content = content;
	}
	
	public ReplyTemplate(JSONObject obj){
		if (obj == null){
			return;
		}
		content = obj.optString("content");
		id = obj.optInt("id");
	}
	
	public int getId(){
		return id;
	}
	
	public String getContent(){
		return content;
	}
}
