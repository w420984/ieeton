package com.ieeton.user.models;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Article implements Serializable {
	private static final long serialVersionUID = 13432346546456L;

	private String mID;
	
	private String mSummary;	//文章简介
	private String mTitle;		//文章标题
	private String mSummaryPicUrl;	//文章预览图片链接
	
	private int mLikedNums;	//点赞次数
	private int mReadNums;	//阅读次数
	private String mPublishTime;	//发布时间
	
	private int mIsLiked;		//是否点赞过
	private int mIsFavorited;	//是否收藏过
	private int mIsFollowed;	//是否关注该文章断作者
	private String mExternalUrl;	//如果是其他来源文字，该链接不为空，查看文章详情时，直接跳转到对应到浏览器网页
		
	public String getID(){
		return mID;
	}
	
	public String getTitle(){
		return mTitle;
	}
	
	public String getSummary(){
		return mSummary;
	}
	
	public String getSummaryPicUrl(){
		return mSummaryPicUrl;
	}
	
	public String getPublishTime(){
		return mPublishTime;
	}
	
	public int getLikedNums(){
		return mLikedNums;
	}
	
	public void setLikedNums(int num){
		mLikedNums = num;
	}
	
	public int getReadNums(){
		return mReadNums;
	}
	
	public void addReadNums(){
		mReadNums += 1;
	}
	
	public int getFollowState(){
		return mIsFollowed;
	}
	
	public void setFollowState(int state){
		mIsFollowed = state;
	}
	
	public String getExternalUrl(){
		return mExternalUrl;
	}
	
	public int getIsLiked(){
		return mIsLiked;
	}
	
	public void setIsLiked(int status){
		mIsLiked = status;
	}
	
	public int getIsFavorited(){
		return mIsFavorited;
	}
	
	public void setIsFavorited(int status){
		mIsFavorited = status;
	}
		
	public Article(JSONObject obj){
		if(obj == null){
			return;
		}
		//Log.v("sereinli","obj:"+obj.toString());
		mID = obj.optString("articleid");
		mTitle = obj.optString("title");
		mSummary = obj.optString("summary");
		mSummaryPicUrl = obj.optString("imageurl");
		
		mPublishTime = obj.optString("publishdate");
		mLikedNums = obj.optInt("likecount");
		mReadNums = obj.optInt("viewcount");
		
		mIsLiked = obj.optInt("isLiked");
		mIsFavorited = obj.optInt("isfollow");
		
		mExternalUrl = obj.optString("externalurl");
		
	}	
}
