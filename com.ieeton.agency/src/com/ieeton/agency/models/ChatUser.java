package com.ieeton.agency.models;

import java.io.Serializable;

import org.json.JSONObject;

import com.ieeton.agency.utils.Utils;

import android.content.Context;
import android.text.TextUtils;

/*
 * 此类主要用于消息列表界面，在不知道对方是医生还是患者的情况下，通过getUserInfo接口
 * 获取对方信息时，用此类保存
 */
public class ChatUser implements Serializable {
	public static String USER_DOCTOR = "doctor";
	public static String USER_PATIENT = "patient";
	public static String USER_PUBLIC = "public";
	public static String USER_HUANXIN = "huanxin";

	private static final long serialVersionUID = -3322058093332605339L;
	
	private String id;			//用户uid
	private String name;		//用户昵称
	private String avatar;		//用户头像链接
	private String type;		//用户类型，doctor或者patient
	
	public ChatUser(Context context, JSONObject json){
		id = json.optString("id");
		name = json.optString("nickname");
		if (TextUtils.isEmpty(name)){
			name = json.optString("nickName");
		}
		avatar = json.optString("avatar");
		type = json.optString("type");
		if (context != null){
			Utils.saveNickCache(context, id, name);
			Utils.checkUserPortrait(context, id, avatar);
		}
	}
	
	public ChatUser(Doctor doctor){
		id = doctor.getID();
		name = doctor.getDoctorName();
		avatar = doctor.getPortraitUrl();
		type = USER_DOCTOR;
	}
	
	public ChatUser(Patient patient){
		id = patient.getID();
		name = patient.getNick();
		avatar = patient.getPortraitUrl();
		type = USER_PATIENT;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getAvatar(){
		return avatar;
	}
	
	public String getType(){
		return type;
	}
}
