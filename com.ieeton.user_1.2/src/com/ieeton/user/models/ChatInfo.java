package com.ieeton.user.models;

public class ChatInfo {
	private String mDoctorID;
	private int mFromMsgNum;
	private int mForceClosed;
	
	public ChatInfo(String id, int num, int flag){
		mDoctorID = id;
		mFromMsgNum = num;
		mForceClosed = flag;
	}
	
	public ChatInfo(ChatInfo info){
		mDoctorID = info.mDoctorID;
		mFromMsgNum = info.mFromMsgNum;
		mForceClosed = info.mForceClosed;
	}
	
	public String getDoctorID(){
		return mDoctorID;
	}
	
	public int getFromMsgNum(){
		return mFromMsgNum;
	}
	
	public int getLikeBarStatus(){
		return mForceClosed;
	}
	
	public void setLikeBarStatus(int status){
		mForceClosed = status;
	}
	
	public void setFromMsgNum(int num){
		mFromMsgNum = num;
	}
}
