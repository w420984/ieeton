package com.ieeton.agency.models;

import org.json.JSONObject;

public class Session {
	private String mId;
	private String mExpires;
	private String mDevice;
	private String mType;

	public Session(){
		mId = "";
		mExpires = "";
		mDevice = "";
		mType = "";
	}
	
	public Session(JSONObject object){
		if (object == null){
			return;
		}
		mId = "";
		mExpires = "";
		mDevice = "";
		mType = "";
		
		mId = object.optString("id");
		mExpires = object.optString("expires");
		mDevice = object.optString("device");
		mType = object.optString("type");
	}
	
	public void setId(String id){
		mId = id;
	}
	
	public void setExpires(String expires){
		mExpires = expires;
	}
	
	public void setDevice(String device){
		mDevice = device;
	}
	
	public void setType(String type){
		mType = type;
	}
	
	public String getId(){
		return mId;
	}
	
	public String getExpires(){
		return mExpires;
	}
	
	public String getDevice(){
		return mDevice;
	}
	
	public String getType(){
		return mType;
	}
}
