package com.ieeton.user.models;

import java.io.Serializable;

import org.json.JSONObject;

public class Lable implements Serializable{

	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	private String description;
	private String iconUrl;
	
	public Lable(JSONObject obj){
		if (obj == null){
			return;
		}
		id = obj.optInt("id");
		name = obj.optString("lableName");
		description = obj.optString("description");
		iconUrl = obj.optString("picurl");
	}
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getDescription(){
		return description;
	}
	
	public String getIconUrl(){
		return iconUrl;
	}
}
