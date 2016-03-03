package com.ieeton.agency.net;

import com.ieeton.agency.DemoApplication;
import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.location.IeetonLocation;
import com.ieeton.agency.models.Doctor;
import com.ieeton.agency.models.ServerHostData;
import com.ieeton.agency.utils.Utils;
import com.umeng.socialize.bean.SHARE_MEDIA;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

public class NetEngine {

	private static NetEngine mInstance;
	private static Context mContext;
	public static String SERVER_HOST = "http://data.ieeton.com/data/VersionCheck.json";
	
	
	public NetEngine(Context context){
		mContext = context;
	}
	
	public static NetEngine getInstance(Context context){
		mContext = context;
		if (mInstance == null){
			mInstance = new NetEngine(context);
		}
		return mInstance;
	}
	
	public static String getImageUrl(String url){
		String newUrl = url;
		if (TextUtils.isEmpty(url)){
			return newUrl;
		}
		if (url.startsWith("http:")){
			return url;
		}
		newUrl = getImageServerUrl() + url;
		return newUrl;
	}
	
	private String getPassportServerUrl(){
		if(DemoApplication.mServerHostData != null){
			return DemoApplication.mServerHostData.getPassportServerUrl();
		}
		ServerHostData host = new ServerHostData();
		return host.getPassportServerUrl();
	}
	public static String getImageServerUrl(){
		if(DemoApplication.mServerHostData != null){
			return DemoApplication.mServerHostData.getImageServerUrl();
		}
		ServerHostData host = new ServerHostData();
		return host.getImageServerUrl();
	}
	private static String getContentServerUrl(){
		if(DemoApplication.mServerHostData != null){
			return DemoApplication.mServerHostData.getContentServerUrl();
		}
		ServerHostData host = new ServerHostData();
		return host.getContentServerUrl();
	}
	public static String getUploadServerUrl(){
		if(DemoApplication.mServerHostData != null){
			return DemoApplication.mServerHostData.getUploadServerUrl();
		}
		ServerHostData host = new ServerHostData();
		return host.getUploadServerUrl();
	}
	
	public static String getFeedbackId(){
		if(DemoApplication.mServerHostData != null){
			return DemoApplication.mServerHostData.getFeedbackId();
		}
		ServerHostData host = new ServerHostData();
		return host.getFeedbackId();
	}
	
	public static String getIvrNumber(){
		if(DemoApplication.mServerHostData != null){
			return DemoApplication.mServerHostData.getIvrNumber();
		}
		ServerHostData host = new ServerHostData();
		return host.getIvrNumber();
	}
    
	/*
	 * 注册界面发验证码，重发验证码
	 */
	public String sendCode(String phoneNumber) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getPassportServerUrl());
		url.append("registerMobile.do");
        
		Bundle params = new Bundle();
		params.putString("loginType", "mobile");
		params.putString("action", "sendMobileCode");
		params.putString("mobile", phoneNumber);
		params.putString("sendTime", System.currentTimeMillis()+""); //防止本地缓存，保证每次请求都能发不同的验证码

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 绑定手机号
	 */
	public String bindMobile(String phoneNumber, String passport, 
			String code) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getPassportServerUrl());
		url.append("registerMobile.do");
        
		Bundle params = new Bundle();
		params.putString("loginType", "mobile");
		params.putString("loginName", phoneNumber);
		params.putString("sendCode", code);
		params.putString("passport", passport);
		params.putString("link", "true");
		params.putString("remember", "365");

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	
	/*
	 * 手机注册接口
	 */
	public String register(String phoneNumber, String password, 
			String code) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getPassportServerUrl());
		url.append("registerMobile.do");
        
		Bundle params = new Bundle();
		params.putString("loginType", "mobile");
		params.putString("loginName", phoneNumber);
		params.putString("sendCode", code);
		params.putString("nickName", phoneNumber);
		params.putString("password", password);
		params.putString("password1", password);
		params.putString("type", "doctor");
		params.putString("remember", "365");
		params.putString("company", Utils.getIeetonFrom(mContext));

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 忘记密码界面发送验证码
	 */
	public String forgetPassword(String phoneNumber) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getPassportServerUrl());
		url.append("forgotPassword.do");
        
		Bundle params = new Bundle();
		params.putString("loginType", "mobile");
		params.putString("loginName", phoneNumber);
		params.putString("sendTime", System.currentTimeMillis()+""); //防止本地缓存，保证每次请求都能发不同的验证码

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 忘记密码界面重置密码
	 */
	public String resetPassword(String phoneNumber, String password, 
			String code) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getPassportServerUrl());
		url.append("resetPassword.do");
        
		Bundle params = new Bundle();
		params.putString("loginType", "mobile");
		params.putString("loginName", phoneNumber);
		params.putString("resetToken", code);
		params.putString("password", password);
		params.putString("password1", password);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 修改密码
	 */
	public String updatePassword(String passport, String oldPassword, String newPassword, 
			String newPasswrodCheck) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getPassportServerUrl());
		url.append("updatePassword.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("loginType", "mobile");
		params.putString("id", Utils.getMyUid(mContext));
		params.putString("loginName", Utils.getMyLoginName(mContext));
		params.putString("oldPassword", oldPassword);
		params.putString("newPassword", newPassword);
		params.putString("newPassword1", newPasswrodCheck);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 手机号登录
	 */
	public String login(String phoneNumber, String password) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getPassportServerUrl());
		url.append("login.do");
        
		Bundle params = new Bundle();
		params.putString("username", phoneNumber);
		params.putString("password", password);
		params.putString("type", "doctor");
		params.putString("remember", "365");

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 第三方登录
	 */
	public String thirdPartLogin(String token, String openId, String appid, 
			SHARE_MEDIA platform) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getPassportServerUrl());
		Bundle params = new Bundle();
		if (platform == SHARE_MEDIA.QQ){
			url.append("loginQQ.do");
		}else if (platform == SHARE_MEDIA.WEIXIN){
			url.append("loginWX.do");
		}else if (platform == SHARE_MEDIA.SINA){
			url.append("loginWB.do");
		}
        
		params.putString("accessToken", token);
		params.putString("openId", openId);
		params.putString("clientId", appid);
		params.putString("clientType", "mobile_doctor");
		params.putString("remember", "365");
		params.putString("type", "doctor");
		params.putString("company", Utils.getIeetonFrom(mContext));

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	
	public String getDomainUrls() throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        
		Bundle params = new Bundle();
		params.putString("device", "androidPhone");

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public IeetonLocation getLocation() throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		IeetonLocation l = new IeetonLocation();
		return l;
	}
	
	//获取上传图片的url
	public String getImageUploadUrl(int image_width, int image_heigth, String image_name, long file_size) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getUploadServerUrl());
//        url.append("upload.do");
        url.append("?action=upload&type=image&appId=iyuesai&targets=1_"+image_width+"x&targets=1_x"+image_heigth+"&name="+image_name+"&size="+file_size+"&format=json");

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, null, mContext, true, null);
		return result;
	}
	
	//上传图片
	public String postImageToServer(Context context, String url, String image_path) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		String result = NetUtils.postImage(context, url, image_path);
		return result;
	}
	
	//查询图片上传进度
	public String getImageUploadProgress(String request_url) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(request_url);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, null, mContext, true, null);
		return result;
	}
	
	//查询图片上传结果
	public String getImageUploadResult(String token) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder request_url = new StringBuilder(getUploadServerUrl());
		request_url.append("?action=query&token="+token+"&_="+System.currentTimeMillis());

		String result = NetUtils.openUrl(request_url.toString(), NetUtils.METHOD_GET, null, mContext, true, null);
		return result;
	}	
	
	//获取附近的医生列表
	public String getNearByDoctors(String passport, double lon, double lat, int pagesize, int pagenum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetNearbyDoctorList.do");
		//only for test
//        lat = 31.2;
//        lon = 121.5;
		Bundle params = new Bundle();
		if(passport != null && !passport.equals("")){
			params.putString("passport", passport);
		}
		params.putDouble("L", lon);
		params.putDouble("B", lat);
		if(pagesize == 0){
			pagesize = 20;	//每页20条
		}
		if(pagenum == 0){
			pagenum = 1;	//第一页
		}
		params.putInt("pageSize", pagesize);
		params.putInt("pageNum", pagenum);


		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取关注的医生列表
	 */
	public String getFollowedDoctor(String passport, int pagesize, int pagenum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetFollowedDoctorList.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		if(pagesize == 0){
			pagesize = 20;	//每页20条
		}
		if(pagenum == 0){
			pagenum = 1;	//第一页
		}
		params.putInt("pageSize", pagesize);
		params.putInt("pageNum", pagenum);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 关注医生
	 */
	public String followDoctor(String passport, String doctor_id) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("FollowDoctor.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("doctorId", doctor_id);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 取消关注医生
	 */
	public String unfollowDoctor(String passport, String doctor_id) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("UnFollowDoctor.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("doctorId", doctor_id);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 为医生点赞
	 */
	public String likeDoctor(String passport, String doctor_id) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("LikeDoctor.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("doctorId", doctor_id);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 取消点赞
	 */
	public String unLikeDoctor(String passport, String doctor_id) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("UnLikeDoctor.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("doctorId", doctor_id);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取患者信息(包括自己的个人信息)
	 */
	public String GetPatientInfo(String passport, String patientId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetPatientInfoNew.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("patientId", patientId);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取可选区域城市列表
	 */
	public String GetAvailableCityList() throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetRegionList.do");
        
		Bundle params = new Bundle();
		params.putInt("parentId", 1);
		params.putString("available", "true");
		params.putInt("level", 2);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 搜索医生
	 */
	public String searchDoctor(String passport, int regionid, String keyword, int pagesize, int pagenum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("SearchDoctor.do");

		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putInt("regionId", regionid);
		params.putString("keyword", keyword);
		if(pagesize == 0){
			pagesize = 20;	//每页20条
		}
		if(pagenum == 0){
			pagenum = 1;	//第一页
		}
		params.putInt("pageSize", pagesize);
		params.putInt("pageNum", pagenum);

		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取医生信息
	 */
	public String getDoctorInfo(String passport, String doctor_id) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetDoctorInfoNew.do");

		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("doctorId", doctor_id);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取搜索热词
	 */
	public String getHotSearchKeywords() throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetHotSearch.do");
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, null, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取接诊患者列表
	 */
	public String getPatientList (String passport, int pagesize, int pagenum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetPatientList.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		if(pagesize == 0){
			pagesize = 20;	//每页20条
		}
		if(pagenum == 0){
			pagenum = 1;	//第一页
		}
		params.putInt("pageSize", pagesize);
		params.putInt("pageNum", pagenum);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取医生关注的患者列表
	 */
	public String getFollowedPatientList (String passport, int pagesize, int pagenum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetFollowedPatientList.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		if(pagesize == 0){
			pagesize = 20;	//每页20条
		}
		if(pagenum == 0){
			pagenum = 1;	//第一页
		}
		params.putInt("pageSize", pagesize);
		params.putInt("pageNum", pagenum);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 增加接诊患者
	 */
	public String addPatientCount(String passport, String patientId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("AddPatientCount.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("patientId", patientId);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 关注患者
	 */
	public String followPatient(String passport, String patientId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("FollowPatient.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("patientId", patientId);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 取消关注患者
	 */
	public String unFollowPatient(String passport, String patientId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("UnFollowPatient.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("patientId", patientId);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}

	/*
	 * 增加备注
	 */
	public String addRemark(String passport, String patientId, String content) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("AddRemark.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("patientId", patientId);
		params.putString("content", content);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 删除备注
	 */
	public String deleteRemark(String passport, String remarkId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("DeleteRemark.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("remarkId", remarkId);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取备注列表
	 */
	public String getRemark(String passport, String patientId, int pageSize, int pageNum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetRemark.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("patientId", patientId);
		if(pageSize == 0){
			pageSize = 20;	//每页20条
		}
		if(pageNum == 0){
			pageNum = 1;	//第一页
		}
		params.putInt("pageSize", pageSize);
		params.putInt("pageNum", pageNum);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 更新医生个人信息
	 */
	public String updateDoctorInfo(String passport, Doctor doctor) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		if (doctor == null){
			return null;
		}
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("UpdateDoctorInfo.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		if (!TextUtils.isEmpty(doctor.getPortraitUrl())){
			params.putString("avatar", doctor.getPortraitUrl());
		}
		if (!TextUtils.isEmpty(doctor.getCertification())){
			params.putString("qualificationPath", doctor.getCertification());
		}
		if (!TextUtils.isEmpty(doctor.getDoctorName())){
			params.putString("name", doctor.getDoctorName());
		}
		if (!TextUtils.isEmpty(doctor.getSkillDescription())){
			params.putString("skill", doctor.getSkillDescription());
		}
		if (doctor.getHospitalId() != -1){
			params.putInt("hospitalId", doctor.getHospitalId());
		}
		if (doctor.getDepartmentId() != -1){
			params.putInt("departmentId", doctor.getDepartmentId());
		}
		if (doctor.getTitleId() != -1){
			params.putInt("titleId", doctor.getTitleId());
		}
		if (doctor.getPrice() != -1){
			params.putInt("price", doctor.getPrice());
		}
		if (doctor.getMessageOnlineStatus() != -1){
			params.putInt("messageOn", doctor.getMessageOnlineStatus());
		}
		if (doctor.getMobileOnlineStatus() != -1){
			params.putInt("mobileOn", doctor.getMobileOnlineStatus());
		}

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 更新医生个人信息
	 */
	public String perfectDoctorInfo(String passport, Doctor doctor) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		if (doctor == null){
			return null;
		}
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("RegisterDoctor.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		if (!TextUtils.isEmpty(doctor.getPortraitUrl())){
			params.putString("avatar", doctor.getPortraitUrl());
		}
		if (!TextUtils.isEmpty(doctor.getCertification())){
			params.putString("qualificationPath", doctor.getCertification());
		}
		if (!TextUtils.isEmpty(doctor.getDoctorName())){
			params.putString("name", doctor.getDoctorName());
		}
		if (!TextUtils.isEmpty(doctor.getMobile())){
			params.putString("mobile", doctor.getMobile());
		}
		if (!TextUtils.isEmpty(doctor.getSkillDescription())){
			params.putString("skill", doctor.getSkillDescription());
		}
		if (doctor.getHospitalId() != -1){
			params.putInt("hospitalId", doctor.getHospitalId());
		}
		if (doctor.getDepartmentId() != -1){
			params.putInt("departmentId", doctor.getDepartmentId());
		}
		if (doctor.getTitleId() != -1){
			params.putInt("titleId", doctor.getTitleId());
		}
		if (doctor.getPrice() != -1){
			params.putInt("price", doctor.getPrice());
		}
		if (doctor.getMessageOnlineStatus() != -1){
			params.putInt("messageOn", doctor.getMessageOnlineStatus());
		}
		if (doctor.getMobileOnlineStatus() != -1){
			params.putInt("mobileOn", doctor.getMobileOnlineStatus());
		}

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 或者城市列表
	 */
	public String getRegionList(int parentId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetRegionList.do");
        
		Bundle params = new Bundle();
		params.putInt("parentId", parentId);
		params.putString("available", "true");
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取医院列表
	 */
	public String getHospitalList(int regionId, int pageSize, int pageNum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetHospitalList.do");
        
		Bundle params = new Bundle();
		params.putInt("regionId", regionId);
		params.putInt("pageSize", pageSize);
		params.putInt("pageNum", pageNum);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取科室列表
	 */
	public String getDepartmentList() throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetDepartmentList.do");
        
		Bundle params = new Bundle();
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}

	/*
	 * 获取职称列表
	 */
	public String getTitleList() throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetTitleList.do");
        
		Bundle params = new Bundle();
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取回复模板
	 */
	public String getQuickReplyList(String passport, int pageSize, int pageNum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetQuickReplyList.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		if(pageSize == 0){
			pageSize = 20;	//每页20条
		}
		if(pageNum == 0){
			pageNum = 1;	//第一页
		}
		params.putInt("pageSize", pageSize);
		params.putInt("pageNum", pageNum);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 新增一个快速回复模板
	 */
	public String addQuickReply(String passport, String content) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("AddQuickReply.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("content", content);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 删除一个快速回复模板
	 */
	public String delQuickReply(String passport, int id) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("DeleteQuickReply.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putInt("replyId", id);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 删除一个快速回复模板
	 */
	public String deleteQuickReply(String passport, int replyId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("AddQuickReply.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putInt("replyId", replyId);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取用户昵称和头像地址
	 */
	public String getNickPortrait(String type, String uid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetUserInfo.do");
        
		Bundle params = new Bundle();
		params.putString("type", type);
		params.putString("userId", uid);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	//创建分享
	public String createShare(String passport, String content) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("CreateShare.do");

		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("content", content);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getAccountDetails(String passport, int pageSize, int pageNum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetAccountDetail.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		if(pageSize == 0){
			pageSize = 20;	//每页20条
		}
		if(pageNum == 0){
			pageNum = 1;	//第一页
		}
		params.putInt("pageSize", pageSize);
		params.putInt("pageNum", pageNum);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String prepareWithdraw(String passport, String amount) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("PrepareWithdraw.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("amount", amount);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String followUser(String uid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("FollowUser.do");
        
		Bundle params = new Bundle();
		params.putString("passport", Utils.getPassport(mContext));
		params.putString("userId", uid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}

	public String unFollowUser(String uid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("UnFollowUser.do");
        
		Bundle params = new Bundle();
		params.putString("passport", Utils.getPassport(mContext));
		params.putString("userId", uid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取所有文章列表
	 */
	public String getAllArticalList(String passport, int pagesize, int pagenum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetAllArticle.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		if(pagesize == 0){
			pagesize = 20;	//每页20条
		}
		if(pagenum == 0){
			pagenum = 1;	//第一页
		}
		params.putInt("pageSize", pagesize);
		params.putInt("pageNum", pagenum);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取所关注的人的文章列表
	 */
	public String getFollowedArticalList(String passport, int pagesize, int pagenum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetFollowedArticle.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		if(pagesize == 0){
			pagesize = 20;	//每页20条
		}
		if(pagenum == 0){
			pagenum = 1;	//第一页
		}
		params.putInt("pageSize", pagesize);
		params.putInt("pageNum", pagenum);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取指定用户的文章列表
	 */
	public String getArticalListByUserID(String passport, String userid, int pagesize, int pagenum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetUserArticle.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		if(pagesize == 0){
			pagesize = 20;	//每页20条
		}
		if(pagenum == 0){
			pagenum = 1;	//第一页
		}
		params.putString("userId", userid);
		params.putInt("pageSize", pagesize);
		params.putInt("pageNum", pagenum);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String favoriteArtical(String passport, String acticalid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("FavoriteArticle.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("articleId", acticalid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String unFavoriteArtical(String passport, String acticalid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("UnFavoriteArticle.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("articleId", acticalid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String likeArtical(String passport, String acticalid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("LikeArticle.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("articleId", acticalid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String unLikeArtical(String passport, String acticalid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("UnLikeArticle.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("articleId", acticalid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String awardArtical(String passport, String orderid, String userid, double amount, String sign) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("PayReward.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("orderId", orderid);
		params.putString("userId", userid);
		params.putDouble("amount", amount);
		params.putString("sign", sign);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getFavoriteArticle(String passport, int pageSize, int pageNum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetFavoriteArticle.do");
        
		Bundle params = new Bundle();
		params.putString("passport", Utils.getPassport(mContext));
		
		params.putInt("pageSize", pageSize);
		params.putInt("pageNum", pageNum);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String searchArticle(String keyword, int pagesize, int pagenum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("SearchArticle.do");

		Bundle params = new Bundle();
		if (!TextUtils.isEmpty(Utils.getPassport(mContext))){
			params.putString("passport", Utils.getPassport(mContext));
		}
		params.putString("keyword", keyword);
		if(pagesize == 0){
			pagesize = 20;	//每页20条
		}
		if(pagenum == 0){
			pagenum = 1;	//第一页
		}
		params.putInt("pageSize", pagesize);
		params.putInt("pageNum", pagenum);

		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String prepareRewardUser(String userId, double totalPrice) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("PrepareRewardUser.do");
        
		Bundle params = new Bundle();
		params.putString("passport", Utils.getPassport(mContext));
		params.putString("userId", userId);
		
		params.putDouble("amount", totalPrice);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String payReward (String userId, String orderId, double amount, String sign) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("PayReward.do");
        
		Bundle params = new Bundle();
		params.putString("passport", Utils.getPassport(mContext));
		params.putString("userId", userId);
		
		params.putString("orderId", orderId);
		params.putDouble("amount", amount);
		params.putString("sign", sign);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String prepareRecharge(String passport, String busi_partner, String name_goods,
			String sign_type, String money_order, String risk_item) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("PrepareRecharge.do");

		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("amount", money_order);
		params.putString("oid_partner", "");
		params.putString("busi_partner", busi_partner);
		params.putString("no_order", "");
		params.putString("dt_order", "");
		params.putString("notify_url", "");
		params.putString("name_goods", name_goods);
		params.putString("sign_type", sign_type);
		params.putString("money_order", money_order);
		params.putString("risk_item", risk_item);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String viewArticle(String id) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("ViewArticle.do");
        
		Bundle params = new Bundle();
		params.putString("articleId", id);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getFollowedUser(String passport, int pagesize, int pagenum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetFollowedUserList.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		if(pagesize == 0){
			pagesize = 20;	//每页20条
		}
		if(pagenum == 0){
			pagenum = 1;	//第一页
		}
		params.putInt("pageSize", pagesize);
		params.putInt("pageNum", pagenum);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getUserArticle(String passport, String userid, int pagesize, int pagenum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetUserArticle.do");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		if(pagesize == 0){
			pagesize = 20;	//每页20条
		}
		if(pagenum == 0){
			pagenum = 1;	//第一页
		}
		params.putString("userId", userid);
		params.putInt("pageSize", pagesize);
		params.putInt("pageNum", pagenum);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
}
