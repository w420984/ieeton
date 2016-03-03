package com.ieeton.agency.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.ieeton.agency.utils.Utils;

public class Doctor implements Serializable {
	public static String DOCTOR_PEOPLE = "people";
	public static String DOCTOR_INSTITUTION = "institution";
	/**
	 * 
	 */
	private static final long serialVersionUID = 3751018160884299214L;
	private String mID;
	private String mName;	//姓名
	private String mTitle;	//职称
	private String mHospital;	//所属医院
	private String mDepartment;	//科室
	private String mSkill;	//擅长
	private int mFansNum;	//粉丝数
	private int mPatientNum;	//患者数
	private int mPositiveNum;	//好评数
	private int mStatus;
	private String mPortrait;	//头像
	private String mCertification;	//证书图片
	private String mMobile;
	private int mMobileOnline;
	private int mMessageOnline;
	private int mPrice;
	private int mHospitalId;
	private int mDepartmentId;
	private int mTitleId;
	private int mPriceEditable;
	private int mBalance;	//账户余额
	private int mArticalNum;		//发布文章数
	
	//与当前登录用户的关系
	private int mIsLiked;	//是否点赞该医生
	private int mIsFollowed;	//是否关注该医生
	private String mDoctorType;
	private String mAddress;
	private List<String> mPics;
	
	public void setHostitalId(int id){
		mHospitalId = id;
	}
	
	public int getHospitalId(){
		return mHospitalId;
	}
	
	public void setDepartmentId(int id){
		mDepartmentId = id;
	}
	
	public int getDepartmentId(){
		return mDepartmentId;
	}
	
	public void setTitleId(int id){
		mTitleId = id;
	}
	
	public int getTitleId(){
		return mTitleId;
	}
	
	public void setIsFollowedStatus( int isfollowed){
		mIsFollowed = isfollowed;
	}
	
	public int getIsLikedStatus(){
		return mIsLiked;
	}
	
	public int getIsFollowedStatus(){
		return mIsFollowed;
	}
	
	public int getPrice(){
		return mPrice;
	}
	
	public int getBalance(){
		return mBalance;
	}
	
	public void setBanlance(int balance){
		mBalance = balance;
	}
	
	public void setPrice(int price){
		mPrice = price;
	}
	
	public int getMessageOnlineStatus(){
		return mMessageOnline;
	}
	
	public void setMessageOnlineStatus(int status){
		mMessageOnline = status;
	}
	
	public int getMobileOnlineStatus(){
		return mMobileOnline;
	}
	
	public void setMobileOnlineStatus(int status){
		mMobileOnline = status;
	}
	
	public String getPortraitUrl(){
		return mPortrait;
	}
	
	public void setPortraitUrl(String link){
		mPortrait = link;
	}
	
	public void setCertification(String certification){
		mCertification = certification;
	}
	
	public String getCertification(){
		return mCertification;
	}
	
	public int getStatus(){
		return mStatus;
	}
	
	public int getPositiveNum(){
		return mPositiveNum;
	}
	
	public int getPatientNum(){
		return mPatientNum;
	}
	
	public int getFansNum(){
		return mFansNum;
	}
	
	public String getSkillDescription(){
		return mSkill;
	}
	
	public String getMobile(){
		return mMobile;
	}
	
	public void setMobile(String moblile){
		mMobile = moblile;
	}
	
	public void setSkillDescription(String content){
		mSkill = content;
	}
	
	public String getDepartment(){
		return mDepartment;
	}
	
	public void setDepartment(String department){
		mDepartment = department;
	}
	
	public String getHospitalName(){
		return mHospital;
	}
	
	public void setHospital(String hospital){
		mHospital = hospital;
	}
	
	public String getTitleName(){
		return mTitle;
	}
	
	public String getDoctorType(){
		return mDoctorType;
	}
	
	public String getAddress(){
		return mAddress;
	}
	
	public List<String> getPics(){
		return mPics;
	}
	public void setTitleName(String title){
		mTitle = title;
	}
	
	public int getArticalNum(){
		return mArticalNum;
	}
	
	public String getDoctorName(){
		return mName;
	}
	
	public void setDoctorName(String name){
		mName = name;
	}
	
	public String getID(){
		return mID;
	}
	
	public int getPriceEditable(){
		return mPriceEditable;
	}
	
	public Doctor(){
		mStatus = -1;
		mName = "";
		mTitle = "";
		mPortrait = "";
		mCertification = "";
		mHospital = "";
		mDepartment = "";
		mID = "";
		mFansNum = 0;
		mPatientNum = 0;
		mMobileOnline = -1;
		mMessageOnline = -1;
		mPrice = -1;
		mSkill = "";
		mPositiveNum = 0;
		mIsLiked = 0;
		mIsFollowed = 0;
		mHospitalId = -1;
		mDepartmentId = -1;
		mTitleId = -1;
		mPriceEditable = 0;
		mMobile = "";
		mBalance = 0;
	}
	
	public Doctor(Context context, JSONObject json){
		mStatus = json.optInt("status");
		mName = json.optString("name");
		mTitle = json.optString("titleName");
		mPortrait = json.optString("avatar");
		mCertification = json.optString("qualification_path");
		mHospital = json.optString("hospitalName");
		mDepartment = json.optString("departmentName");
		mID = json.optString("id");
		mFansNum = json.optInt("follow_count");
		mPatientNum = json.optInt("patient_count");
		mMobileOnline = json.optInt("mobile_on");
		mMessageOnline = json.optInt("message_on");
		mPrice = json.optInt("price");
		mSkill = json.optString("skill");
		mPositiveNum = json.optInt("like_count");
		mIsLiked = json.optInt("isLiked");
		mIsFollowed = json.optInt("isFollowed");
		mPriceEditable = json.optInt("priceEditable");
		mMobile = json.optString("mobile");
		mHospitalId = -1;
		mDepartmentId = -1;
		mTitleId = -1;
		String balance = json.optString("balance");
		if (TextUtils.isEmpty(balance)){
			mBalance = 0;
		}else{
			mBalance = (int)Float.parseFloat(balance);
		}
		mDoctorType = json.optString("doctorType");
		mAddress = json.optString("address");
		mPics = new ArrayList<String>();
		for (int i=0; i<9; i++){
			String key = "imagePath" + (i+1);
			String value = json.optString(key);
			if (!TextUtils.isEmpty(value)){
				mPics.add(value);
			}
		}
		mArticalNum = json.optInt("articaleCount");

		//特殊处理，通过我的关注接口获取的信息时会用到这里
		if (TextUtils.isEmpty(mHospital)){
			String hdt = json.optString("hdt");
			if (TextUtils.isEmpty(hdt)){
				return;
			}
			String[] list = hdt.split("\\|");
			if (list == null || list.length == 0){
				return;
			}
			mHospital = list[0];
			if (list.length > 1){
				mDepartment = list[1];
			}
			if (list.length > 2){
				mTitle = list[2];
			}
		}
		if (context != null && !TextUtils.isEmpty(mName)
				&& !TextUtils.isEmpty(mPortrait)){
			Utils.saveNickCache(context, mID, mName);
			Utils.checkUserPortrait(context, mID, mPortrait);
		}
	}
}
