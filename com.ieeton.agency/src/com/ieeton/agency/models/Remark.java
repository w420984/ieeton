package com.ieeton.agency.models;

import org.json.JSONObject;

import com.ieeton.agency.utils.Utils;

public class Remark {

	private String time;
	private String content;
	private String id;
	
	public Remark(String id, String time, String content){
		this.id = id;
		this.time = time;
		this.content = content;
	}
	
	public Remark(JSONObject obj){
		String serverTime = obj.optString("createDate");
		time = Utils.getTime(serverTime);
		content = obj.optString("content");
		id = obj.optString("id");
	}
		
	public String getId(){
		return id;
	}
	
	public String getTime(){
		return time;
	}
	
	public String getContent(){
		return content;
	}
}
