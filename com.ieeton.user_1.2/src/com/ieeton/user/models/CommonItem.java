package com.ieeton.user.models;

import java.io.Serializable;

import org.json.JSONObject;

import com.ieeton.user.R;

import android.content.Context;


public class CommonItem implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1727184731316434033L;
	public static int TYPE_REGION = 1;
	public static int TYPE_HOSPITAL = 2;
	public static int TYPE_DEPARTMENT = 3;
	public static int TYPE_TITLE = 4;
	
	private int type;
	private int id;
	private String name;
	private double longitude;
	private double latitude;
	private boolean hasNextLevel;
	
	public CommonItem(Context context, JSONObject obj, int type){
		this.type = type;
		id = obj.optInt("id");
		name = obj.optString("name");
		latitude = obj.optDouble("latitude", 0);
		longitude = obj.optDouble("longitude", 0);
		
		if (type == TYPE_REGION){
			if (context.getString(R.string.region_beijing).equals(name)
					|| context.getString(R.string.region_tianjin).equals(name)
					|| context.getString(R.string.region_shanghai).equals(name)
					|| context.getString(R.string.region_chongqing).equals(name)
					|| context.getString(R.string.region_hongkong).equals(name)
					|| context.getString(R.string.region_macao).equals(name)
					){
				hasNextLevel = false;
			} else {
				hasNextLevel = true;
			}
		}
	}
	
	public CommonItem(City city, boolean hasNext){
		id = city.getCityID();
		name = city.getCityName();
		longitude = city.getLongitude();
		latitude = city.getLatitude();
		hasNextLevel = hasNext;
	}
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean hasNextLevel(){
		return hasNextLevel;
	}
	
	public int getType(){
		return type;
	}
	
	public double getLongitude(){
		return longitude;
	}
	
	public double getLatitude(){
		return latitude;
	}
}
