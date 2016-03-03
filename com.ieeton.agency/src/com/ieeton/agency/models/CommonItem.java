package com.ieeton.agency.models;

import java.io.Serializable;

import org.json.JSONObject;


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
	private boolean hasNextLevel;
	
	public CommonItem(JSONObject obj, int type){
		this.type = type;
		id = obj.optInt("id");
		name = obj.optString("name");
		
		if (type == TYPE_REGION){
			switch (id){
			case 2:		//北京
			case 5642:	//上海
			case 7215:	//澳门
			case 7006:	//重庆
			case 6089:	//天津
			case 7155:	//香港
				hasNextLevel = false;
				break;
			default:
				hasNextLevel = true;
			}
		}
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
}
