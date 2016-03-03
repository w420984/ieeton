package com.ieeton.user.models;

import java.io.Serializable;

import org.json.JSONObject;

public class ThirdPartner implements Serializable{

	private static final long serialVersionUID = 1L;

	private String url;
	private String title;
	private String picUrl;
	
	public ThirdPartner(JSONObject obj){
		if (obj == null){
			return;
		}
		
		url = obj.optString("menuurl");
		title = obj.optString("title");
		picUrl = obj.optString("urlimg");
	}
	
	public String getUrl(){
		return url;
	}
	
	public String getTitle(){
		return title;
	}
	
	public String getPicUrl(){
		return picUrl;
	}
}
