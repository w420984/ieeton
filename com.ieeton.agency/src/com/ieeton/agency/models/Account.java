package com.ieeton.agency.models;

import org.json.JSONObject;

public class Account {
	private String mUserId;
	private String mName;
	private String mMobile;
	
	public Account(){
		mUserId = "";
		mName = "";
		mMobile = "";
	}
	
	public Account(JSONObject object){
		if (object == null){
			return;
		}
		mUserId = "";
		mName = "";
		mMobile = "";

		mUserId = object.optString("userId");
		mName = object.optString("trueName");
		mMobile = object.optString("mobile");
	}
	
	public void setUserId(String id){
		mUserId = id;
	}
	
	public void setName(String name){
		mName = name;
	}
	
	public void setMobile(String mobile){
		mMobile = mobile;
	}
	
	public String getUserId(){
		return mUserId;
	}
	
	public String getName(){
		return mName;
	}
	
	public String getMobile(){
		return mMobile;
	}
}
