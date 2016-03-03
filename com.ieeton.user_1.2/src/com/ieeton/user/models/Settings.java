package com.ieeton.user.models;

public class Settings {
	private int mMsgNotify;
	private boolean mViaLoundSpeaker;
	private boolean mAutoClearCache;

	public Settings(){
		mMsgNotify = 0;
		mViaLoundSpeaker = true;
		mAutoClearCache = true;
	}
	
	public Settings(int notify, boolean via, boolean cache){
		mMsgNotify = notify;
		mViaLoundSpeaker = via;
		mAutoClearCache = cache;
	}
	
	public void setNewMessageNotify(int type){
		mMsgNotify = type;
	}
	
	public void setViaLoundSpeaker(boolean onoff){
		mViaLoundSpeaker = onoff;
	}
	
	public void setAutoClearCache(boolean onoff){
		mAutoClearCache = onoff;
	}
	
	public int getNewMessageNotify(){
		return mMsgNotify;
	}
	
	public boolean getViaLoundSpeaker(){
		return mViaLoundSpeaker;
	}
	
	public boolean getAutoCleatCache(){
		return mAutoClearCache;
	}
}
