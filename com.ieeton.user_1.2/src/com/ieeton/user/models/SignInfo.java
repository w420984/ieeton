package com.ieeton.user.models;

import org.json.JSONObject;

public class SignInfo {
	private String name;			//当前用户昵称		
	private int serialDay;			//连续签到天数
	private int integral;			//当前总积分
	private int nextIntegral;		//下次签到可获得积分数
	private boolean isSigned;		//当天是否已签到
	
	public SignInfo(JSONObject obj){
		if (obj == null){
			return;
		}
		name = obj.optString("nickname");
		serialDay = obj.optInt("evensign");
		integral = obj.optInt("Integral");
		nextIntegral = obj.optInt("ReturnIntegral");
		isSigned = obj.optBoolean("isSign");
	}
	
	public String getName(){
		return name;
	}
	
	public int getSerialDay(){
		return serialDay;
	}
	
	public int getIntegral(){
		return integral;
	}
	
	public int getNextIntegral(){
		return nextIntegral;
	}
	
	public boolean isSigned(){
		return isSigned;
	}
}
