package com.ieeton.user.models;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class City implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1868585464523423422L;
	private int id;
	private String name;
	private double latitude;
	private double longitude;
	
	public City(JSONObject obj){
		id = obj.optInt("cityid");
		name = obj.optString("cityname");
		latitude = obj.optDouble("latitude", 0);
		longitude = obj.optDouble("longitude", 0);
	}
	
	public City(CommonItem item){
		id = item.getId();
		name = item.getName();
		latitude = item.getLatitude();
		longitude = item.getLongitude();
	}
	
	public City(int c_id, String c_name){
		id = c_id;
		name = c_name;
	}
	
	public int getCityID(){
		return id;
	}
	
	public String getCityName(){
		return name;
	}
	
	public void setCityName(String name){
		this.name = name;
	}
	
	public double getLongitude(){
		return longitude;
	}
	
	public void setLongitude(double longitude){
		this.longitude = longitude;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public void setLatitude(double latitude){
		this.latitude = latitude;
	}
}