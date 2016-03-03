package com.ieeton.user.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

public class Comment implements Serializable{
	private static final long serialVersionUID = 1112435678789230L;

	private String productName;
	private int commentLevel;
	private String commentContent;
	private String commentDate;
	private String ownerId;
	private List<String> pics;
	private IeetonUser commentUser;
	
	public Comment(JSONObject obj){
		if (obj == null){
			return;
		}
		
		productName = obj.optString("productname");
		commentContent = obj.optString("comment");
		commentLevel = obj.optInt("commentLevel");
		commentDate = obj.optString("commentdate");
		ownerId = obj.optString("owneruid");
		commentUser = new IeetonUser(null, obj.optJSONObject("userinfo"));
		
		pics = new ArrayList<String>();
		for(int i=0; i<3; i++){
			String url = obj.optString("Commenturl"+(i+1));
			if (!TextUtils.isEmpty(url)){
				pics.add(url);
			}
		}
	}
	
	public String getProductName(){
		return productName;
	}
		
	public String getCommentContent(){
		return commentContent;
	}
	
	public int getCommentLevel(){
		return commentLevel;
	}
	
	public String getCommentDate(){
		return commentDate;
	}
	
	public IeetonUser getCommentUser(){
		return commentUser;
	}
	
	public String getOwnerId(){
		return ownerId;
	}
	
	public List<String> getPics(){
		return pics;
	}
	
}
