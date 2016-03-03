package com.ieeton.user.models;

import java.io.Serializable;

import org.json.JSONObject;

public class ProductCategory implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int categoryId;
	private String categoryName;
	private String categoryIconUrl;
	
	public ProductCategory(JSONObject obj){
		if (obj == null){
			return;
		}
		categoryId = obj.optInt("categoryid");
		categoryName = obj.optString("categoryname");
		categoryIconUrl = obj.optString("categoryurl");
				
	}
	
	public ProductCategory(){
		categoryId = 0;
		categoryName = "";
		categoryIconUrl = "";
	}
		
	public int getCategoryId(){
		return categoryId;
	}
	
	public void setCategoryId(int id){
		categoryId = id;
	}
	
	public String getCagegoryName(){
		return categoryName;
	}
	
	public void setCategoryName(String name){
		categoryName = name;
	}
	
	public String getCategoryIconUrl(){
		return categoryIconUrl;
	}
	
	public void setCategoryIconUrl(String url){
		categoryIconUrl = url;
	}
}
