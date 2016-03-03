package com.ieeton.agency.models;

import java.io.Serializable;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.ieeton.agency.utils.Utils;

public class Patient implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3751018160884299214L;
	private String mID;
	private String mNick;	//姓名
	private int mStatus;
	private String mPortrait;	//头像
	private String mMobile;
	private int mRegionId;
	private String mRegionName;
	private String mGender;
	private int mFollowCount;
	private int mArticleCount;
	private String mDescription;
	
	//与当前登录用户的关系
	private int mIsFollowed;	//是否关注
	
	public void setIsFollowedStatus( int isfollowed){
		mIsFollowed = isfollowed;
	}
	
	public int getRegionId(){
		return mRegionId;
	}
	
	public int getIsFollowedStatus(){
		return mIsFollowed;
	}
	
	public String getMobile(){
		return mMobile;
	}
	
	public String getPortraitUrl(){
		return mPortrait;
	}
	
	public int getStatus(){
		return mStatus;
	}
	
	public String getNick(){
		return mNick;
	}
	
	public String getRegionName(){
		return mRegionName;
	}
	
	public String getID(){
		return mID;
	}
	
	public String getGender(){
		return mGender;
	}
	
	public int getFollowCount(){
		return mFollowCount;
	}

	public int getArticleCount(){
		return mArticleCount;
	}
	
	public String getDescription(){
		return mDescription;
	}
	
	public Patient(Context context, JSONObject json){
		mStatus = json.optInt("status");
		mNick = json.optString("nickName");
		if (TextUtils.isEmpty(mNick)){
			mNick = json.optString("name");
		}
		mPortrait = json.optString("avatar");
		mID = json.optString("id");
		mMobile = json.optString("mobile");
		mRegionId = json.optInt("regionId");
		mIsFollowed = json.optInt("isFollowed");	
		mRegionName = json.optString("regionName");
		mGender = json.optString("gender");
		mFollowCount = json.optInt("follow_count");
		mArticleCount = json.optInt("articleCount");
		if (mArticleCount == 0){
			mArticleCount = json.optInt("article_count");
		}
		mDescription = json.optString("description");
		Utils.saveNickCache(context, mID, mNick);
		Utils.checkUserPortrait(context, mID, mPortrait);
	}	
	
	public Patient(){
		mNick = "";
		mPortrait = "";
		mID = "";
		mMobile = "";
		mRegionId = -1;
		mIsFollowed = 0;	
		mRegionName = "";
		mGender = "";
		mFollowCount = 0;
		mArticleCount = 0;
		mDescription = "";
	}
	
	public void setId(String id){
		mID = id;
	}
	
	public void setNick(String nick){
		mNick = nick;
	}
	
	public void setPortraitUrl(String url){
		mPortrait = url;
	}
	
	public void setGender(String gender){
		mGender = gender;
	}
}
