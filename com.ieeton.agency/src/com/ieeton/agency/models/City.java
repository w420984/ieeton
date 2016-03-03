package com.ieeton.agency.models;

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
	
	public City(JSONObject obj){
		try{
			id = obj.getInt("id");
			name = obj.getString("name");
		}catch(JSONException e){
			e.printStackTrace();
		}
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
}