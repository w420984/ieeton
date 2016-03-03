package com.ieeton.user.models;

import java.io.Serializable;

import org.json.JSONObject;

public class SubscribeInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	private String address;
	private String date;
	private String name;
	private String mobile;
	private double latitude;
	private double longitude;
	private Product product;
	
	public SubscribeInfo(){
		
	}
	
	public SubscribeInfo(JSONObject obj){
		if (obj == null){
			return;
		}
		address = obj.optString("address");
		date = obj.optString("date");
		name = obj.optString("name");
		mobile = obj.optString("mobile");
		latitude = obj.optDouble("latitude");
		longitude = obj.optDouble("longitude");
		product = new Product(obj.optJSONObject("productInfo"));
	}
	
	public String getAddress(){
		return address;
	}
	
	public void setAddress(String address){
		this.address = address;
	}
	
	public String getDate(){
		return date;
	}
	
	public void setDate(String date){
		this.date = date;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getMobile(){
		return mobile;
	}
	
	public void setMobile(String mobile){
		this.mobile = mobile;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public void setLatitude(double latitude){
		this.latitude = latitude;
	}
	
	public double getLongitude(){
		return longitude;
	}
	
	public void setLongitude(double longitude){
		this.longitude = longitude;
	}
	
	public Product getProduct(){
		return product;
	}
}
