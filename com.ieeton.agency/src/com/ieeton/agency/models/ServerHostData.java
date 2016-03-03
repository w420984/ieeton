package com.ieeton.agency.models;

import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.agency.utils.Constants;

import android.content.Context;
import android.util.Log;

public class ServerHostData {
	private String mPassportServer;
	private String mImageServer;
	private String mUploadServer;
	private String mContentServer;
	private String mFeedbackId;
	private String mIvrNumber;
	
	public ServerHostData(){
		mPassportServer = Constants.SERVER_HOST_PASSPORT_SERVER;
		mImageServer = Constants.SERVER_HOST_IMAGE_SERVER;
		mUploadServer = Constants.SERVER_HOST_UPLOAD_SERVER;
		mContentServer = Constants.SERVER_HOST_CONTENT_SERVER;
		mFeedbackId = Constants.SERVER_FEEDBACK_HUANXIN_ID;
		mIvrNumber = Constants.SERVER_IVR_NUMBER;
	}
	
	public ServerHostData(JSONObject json) throws JSONException{
		mPassportServer = json.getString("passportServer");
		mImageServer = json.getString("imageServer");
		mUploadServer = json.getString("uploadServer");
		mContentServer = json.getString("contentServer");
		mFeedbackId = json.optString("huanxinSecretaryId");
		mIvrNumber = json.optString("ivrPhoneNumber");

	}
	
	public String getIvrNumber(){
		return mIvrNumber;
	}

	public String getPassportServerUrl(){
		return mPassportServer;
	}

	public String getImageServerUrl(){
		return mImageServer;
	}

	public String getUploadServerUrl(){
		return mUploadServer;
	}

	public String getContentServerUrl(){
		return mContentServer;
	}
	
	public String getFeedbackId(){
		return mFeedbackId;
	}
	
	public void setFeedbackId(String id){
		mFeedbackId = id;
	}
	
	public void setPassportServerUrl(String url){
		mPassportServer = url;
	}

	public void setImageServerUrl(String url){
		mImageServer = url;
	}

	public void setUploadServerUrl(String url){
		mUploadServer = url;
	}

	public void setContentServerUrl(String url){
		mPassportServer = url;
	}
}
