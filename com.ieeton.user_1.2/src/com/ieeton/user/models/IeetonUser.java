package com.ieeton.user.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.ieeton.user.utils.Utils;

import android.content.Context;
import android.text.TextUtils;


public class IeetonUser implements Serializable{

	private static final long serialVersionUID = -3064155268099935806L;

	/*
	 * 用户uid
	 */
	private String uid;				//用户uid
	private String name;			//用户昵称
	private String hxName;			//环信用户名
	private String hxPassword;		//环信密码
	private int gender;				//性别 0女 1男
	private String city;			//城市名
	private int userType;			//用户类型 1医生 2机构 3金牌妈妈 4普通用户 5游客
	private String avatar;			//用户头像链接
	private String channel;			//注册渠道
	private String hospital;		//医院名
	private String department;		//科室
	private String title;			//职称
	private int price;				//电话咨询价格
	private int followCount;		//粉丝数
	private int likeCount;			//点赞数
	private int articleCount;		//文章数
	private String description;		//简介
	private int switchMobile;		//电话咨询开关
	private int switchMessage;		//图文咨询开关
	private int balance;			//账户余额
	private int integral;			//账户积分
	private String clinicaltime;	//门诊时间
	private String registration;	//挂号链接
	private String address;			//地址
	private String videoUrl;		//视频链接
	private int isfollow;			//是否关注
	private String mobile;			//绑定手机号
	private String ivrmobile;		//电话咨询号码
	private double latitude;		//纬度
	private double longitude;		//经度
	private List<String> picList;	//图片列表
	private int age;				//暂时用在第三方登录时返回，如果是1表示服务器还没发过欢迎语
	
	public IeetonUser(){
	}
	
	public IeetonUser(Context context, JSONObject obj){
		if (obj == null){
			return;
		}
		uid = obj.optString("uid");
		name = obj.optString("nickname");			
		hxName = obj.optString("hxpassportid");			
		hxPassword = obj.optString("hxpassportpw");		
		gender = obj.optInt("gender");				
		city = obj.optString("cityname");			
		userType = obj.optInt("usertype");			
		avatar = obj.optString("photourl");			
		channel = obj.optString("channel");			
		hospital = obj.optString("hospitalname");		
		department = obj.optString("departmentname");		
		title = obj.optString("titlename");			
		price = obj.optInt("price");				
		followCount = obj.optInt("followcount");		
		likeCount = obj.optInt("likecount");			
		articleCount = obj.optInt("articlecount");		
		description = obj.optString("description");		
		switchMobile = obj.optInt("switchmobile");		
		switchMessage = obj.optInt("switchmessage");		
		balance = obj.optInt("balance");
		integral = obj.optInt("integral");
		clinicaltime = obj.optString("clinicaltime");	
		registration = obj.optString("registrationurl");	
		address = obj.optString("address");
		isfollow = obj.optInt("isfollow");
		mobile = obj.optString("mobile");
		ivrmobile = obj.optString("ivrmobile");
		videoUrl = obj.optString("videoUrl");
		latitude = obj.optDouble("latitude", 0);
		longitude = obj.optDouble("longitude", 0);
		age = obj.optInt("age");
		
		picList = new ArrayList<String>();	
		for (int i=0; i<9; i++){
			String key = "image" + (i+1) + "url";
			String value = obj.optString(key);
			if (!TextUtils.isEmpty(value)){
				picList.add(value);
			}
		}
		
		if (!TextUtils.isEmpty(name) && context != null){
			Utils.saveNickCache(context, uid, name);
		}
		if (!TextUtils.isEmpty(avatar) && context != null){
			Utils.checkUserPortrait(context, uid, avatar);
		}
	}
	
	public String getUid(){
		return uid;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getHxName(){
		return hxName;
	}
	
	public String getHxPassword(){
		return hxPassword;
	}
	
	public int getGender(){
		return gender;
	}
	
	public void setGender(int gender){
		this.gender = gender;
	}
	
	public String getCity(){
		return city;
	}
	
	public int getUserType(){
		return userType;
	}
	
	public String getAvatar(){
		return avatar;
	}
	
	public void setAvatar(String avatar){
		this.avatar = avatar;
	}
	
	public String getChannel(){
		return channel;
	}
	
	public String getHospital(){
		return hospital;
	}
	
	public String getDepartment(){
		return department;
	}
	
	public String getTitle(){
		return title;
	}
	
	public int getPrice(){
		return price;
	}
	
	public int getFollowCount(){
		return followCount;
	}
	
	public int getLikeCount(){
		return likeCount;
	}
	
	public int getArticleCount(){
		return articleCount;
	}
	
	public String getDescription(){
		return description;
	}
	
	public int getSwitchMobile(){
		return switchMobile;
	}
	
	public int getSwichMessage(){
		return switchMessage;
	}
	
	public int getBalance(){
		return balance;
	}
	
	public int getIntegral(){
		return integral;
	}
	
	public String getClinicaltile(){
		return clinicaltime;
	}
	
	public String getRegistration(){
		return registration;
	}
	
	public String getAddress(){
		return address;
	}
	
	public int getIsfollow(){
		return isfollow;
	}
	
	public void setIsfollow(int status){
		isfollow = status;
	}
	
	public String getMobile(){
		return mobile;
	}
	
	public String getIvrMobile(){
		return ivrmobile;
	}
	
	public String getVideoUrl(){
		return videoUrl;
	}
	
	public double getLongitude(){
		return longitude;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public List<String> getPicList(){
		return picList;
	}
	
	public void addFollowCount(){
		followCount++;
	}
	
	public void subtractFollowCount(){
		followCount--;
		if (followCount < 0){
			followCount = 0;
		}
	}
	
	public int getAge(){
		return age;
	}
}
