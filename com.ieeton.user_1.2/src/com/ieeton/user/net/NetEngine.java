package com.ieeton.user.net;

import java.util.List;

import com.ieeton.user.IeetonApplication;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.ProductCategory;
import com.ieeton.user.models.ServerHostData;
import com.ieeton.user.models.ThirdPartner;
import com.ieeton.user.utils.Md5Algorithm;
import com.ieeton.user.utils.Utils;
import com.umeng.socialize.bean.SHARE_MEDIA;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

public class NetEngine {

	private static NetEngine mInstance;
	private static Context mContext;
	private static String SERVER_HOST = "http://m.dadahealth.com:81/DaDaInterface_m_1.3.0/VersionCheck_m_130.ashx";
	public static String CHECK_MOBILE = "checkMobile";
	public static String GET_ACCESS_CODE = "getAccessCode";
	
	
	private NetEngine(Context context){
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
	
	public static String getSecretaryID(){
		if(IeetonApplication.mServerHostData != null){
			return IeetonApplication.mServerHostData.getSecretaryID();
		}
		ServerHostData host = new ServerHostData(mContext);
		return host.getSecretaryID();
	}
	
	public static String getIvrNumber(){
		if(IeetonApplication.mServerHostData != null){
			return IeetonApplication.mServerHostData.getIvrNumber();
		}
		ServerHostData host = new ServerHostData(mContext);
		return host.getIvrNumber();
	}

	private static String getImageServerUrl(){
		if(IeetonApplication.mServerHostData != null){
			return IeetonApplication.mServerHostData.getImageServerUrl();
		}
		ServerHostData host = new ServerHostData(mContext);
		return host.getImageServerUrl();
	}
	private String getContentServerUrl(){
		if(IeetonApplication.mServerHostData != null){
			return IeetonApplication.mServerHostData.getContentServerUrl();
		}
		ServerHostData host = new ServerHostData(mContext);
		return host.getContentServerUrl();
	}
	
	public static List<ProductCategory> getProductCategoryList(){
		if(IeetonApplication.mServerHostData != null){
			return IeetonApplication.mServerHostData.getProductCategoryList();
		}
		ServerHostData host = new ServerHostData(mContext);
		return host.getProductCategoryList();
	}
	
	public static List<ThirdPartner> getThirdPartnerList(){
		if(IeetonApplication.mServerHostData != null){
			return IeetonApplication.mServerHostData.getThirdPartnerList();
		}
		ServerHostData host = new ServerHostData(mContext);
		return host.getThirdPartnerList();
	}
    
	/*
	 * 注册界面发验证码，重发验证码
	 */
	public String sendCode(String phoneNumber, String action) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("SendCode_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("action", action);
		params.putString("mobile", phoneNumber);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 手机注册接口
	 */
	public String register(String phoneNumber, String password, String nickName,
			String code) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("RegisterMobile_m_130.ashx");
        
		Bundle params = new Bundle();
		if (!TextUtils.isEmpty(Utils.getMyUid(mContext))){
			params.putString("uid", Utils.getMyUid(mContext));
		}
		params.putString("loginName", phoneNumber);
		params.putString("sendCode", code);
		params.putString("nickName", nickName);
		String pw = Md5Algorithm.sign(password);
		params.putString("password", pw);
		params.putString("userType", "4");

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
		
	/*
	 * 忘记密码界面重置密码
	 */
	public String resetPassword(String phoneNumber, String password, 
			String code) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("CreatPassword_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("mobile", phoneNumber);
		params.putString("sendCode", code);
		String pw = Md5Algorithm.sign(password);
		params.putString("password", pw);
		if (!TextUtils.isEmpty(Utils.getMyUid(mContext))){
			params.putString("uid", Utils.getMyUid(mContext));
		}

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 修改密码
	 */
	public String updatePassword(String uid, String oldPassword, String newPassword) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("UpdatePassword_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", uid);
		String pw = Md5Algorithm.sign(oldPassword);
		params.putString("oldPassword", pw);
		pw = Md5Algorithm.sign(newPassword);
		params.putString("newPassword", pw);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 手机号登录
	 */
	public String login(String phoneNumber, String password) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("login_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("loginname", phoneNumber);
		String pw = Md5Algorithm.sign(password);
		params.putString("password", pw);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 友盟第三方登录（微博、微信、QQ）
	 */
	public String thirdPartLogin(String token, String openId, String appid, 
			SHARE_MEDIA platform) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		Bundle params = new Bundle();
		url.append("ThirdPartLogin_m_130.ashx");
		if (platform == SHARE_MEDIA.QQ){
			params.putString("platform", "qq");
		}else if (platform == SHARE_MEDIA.WEIXIN){
			params.putString("platform", "weixin");
		}else if (platform == SHARE_MEDIA.SINA){
			params.putString("platform", "weibo");
		}        
		params.putString("uid", Utils.getMyUid(mContext));
		params.putString("userType", "4");
		params.putString("accessToken", token);
		params.putString("appId", appid);
		params.putString("openId", openId);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getDomainUrls() throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        
		Bundle params = new Bundle();
		int needUid = TextUtils.isEmpty(Utils.getMyUid(mContext)) ? 1 : 0;
		params.putInt("getuid", needUid);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, params, mContext, true, null);
		return result;
	}
			
	//上传图片
	public String postImageToServer(Context context, int type, String image_path) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("UploadFile.ashx");
		url.append("?sType="+type);
		
		String result = NetUtils.postImage(context, url.toString(), image_path);
		return result;
	}
					
	/*
	 * 更新个人信息
	 */
	public String updateInfo(String avatar, String name, int gender) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("UpdateuserInfo_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		if (!TextUtils.isEmpty(name)){
			params.putString("nickname", name);
		}
		if (!TextUtils.isEmpty(avatar)){
			params.putString("photourl", avatar);
		}
		params.putInt("gender", gender);
//		params.putInt("regionId", account.getRegionId());

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取可选区域城市列表
	 */
	public String GetAvailableCityList() throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetCity_m_130.ashx");
        
		Bundle params = new Bundle();
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
				
	//获取用户信息
	public String getUserInfo(String userid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetUserInfo_m_130.ashx");

		Bundle params = new Bundle();
		params.putString("userid", userid);
		params.putString("uid", Utils.getMyUid(mContext));
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
		
	//绑定手机--绑定
	public String bindingMobile(String passport, String mobile, String accesscode) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("registerMobile_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("loginType", "mobile");
		params.putBoolean("link", true);
		params.putString("loginName", mobile);
		params.putString("sendCode", accesscode);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	//绑定手机--
	public String bindingMobileOperation(String action, String mobile) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("registerMobile_m_130.ashx");
        
		Bundle params = new Bundle();
		if(CHECK_MOBILE.equals(action)){
			params.putString("action", "validateMobile");
		}else if(GET_ACCESS_CODE.equals(action)){
			params.putString("action", "sendMobileCode");
		}
		params.putString("loginType", "mobile");
		params.putString("mobile", mobile);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	
	public String prepareRecharge(String body, int fee, String ip, int type, String orderId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("PrepareRecharge_m_130.ashx");

		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putString("body", body);
		params.putInt("total_fee", fee);
		params.putString("spbill_create_ip", ip);
		params.putInt("status", type);
		params.putString("internalId", orderId);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	/*
	 * 获取账号明细
	 */
	public String getAccountDetails(int pageNum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetAccountDetail_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		if(pageNum == 0){
			pageNum = 1;	//第一页
		}
		params.putInt("pageNum", pageNum);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
		
	/*
	 * 获取关注用户列表
	 */
	public String getFollowedUser(int userType, int pagesize, int pagenum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetFollowedUserList_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putInt("usertype", userType);
		if(pagesize == 0){
			pagesize = 10;	//每页20条
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
	 * 获取所有文章列表
	 */
	public String getAllArticalList(int pagenum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetAllArticle_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		if(pagenum == 0){
			pagenum = 1;	//第一页
		}		
		params.putInt("pageNum", pagenum);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
			
	public String followUser(String uid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("FllowUser_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uidA", Utils.getMyUid(mContext));
		params.putString("uidB", uid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}

	public String unFollowUser(String uid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("UnFollowUser_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uidA", Utils.getMyUid(mContext));
		params.putString("uidB", uid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String favoriteArtical(String acticalid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("FavoriteArticle_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putString("articleId", acticalid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String unFavoriteArtical(String acticalid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("UnFavoriteArticle_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putString("articleId", acticalid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String likeArtical(String passport, String acticalid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("LikeArticle_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("articleId", acticalid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String unLikeArtical(String passport, String acticalid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("UnLikeArticle_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("passport", passport);
		params.putString("articleId", acticalid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
			
	public String getProductDetail(String productId, String ownerId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetProductDetail_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("productId", productId);
		params.putString("owneruid", ownerId);
		params.putString("uid", Utils.getMyUid(mContext));
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
		
	}
	
	public String generateOrder(String productId, String ownerId, int count, int price, int amount, int integral, String date, String mobile) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("PrepareOrderProduct_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));		
		params.putString("productId", productId);
		params.putString("owneruid", ownerId);
		params.putInt("quantity", count);
		params.putInt("price", price);
		
		if (integral > 0){
			params.putInt("integral", integral);			
		}else{
			params.putInt("amount", amount);			
		}
		
		params.putString("datime", date);
		params.putString("phone", mobile);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
		
	public String payProduct(String orderId, String productId, String ownerId, 
			int count, int amount, int integral) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("PayProduct_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putString("internalId", orderId);
		params.putString("productId", productId);		
		params.putString("ownerId", ownerId);
		params.putInt("quantity", count);
		String sign="";
		if (integral > 0){
			params.putInt("integral", integral);
			String str = "integral="+integral+"&quantity="+count+"&internalId="+orderId+"&productId="
					+productId+"&uid="+Utils.getMyUid(mContext)+"&key=ieeton.pay";
			Utils.logd("str:"+str);
			sign = Utils.getMessageDigest(str.getBytes()).toUpperCase();
		}else{
			params.putInt("amount", amount);
			String str = "amount="+amount+"&quantity="+count+"&internalId="+orderId+"&productId="
					+productId+"&uid="+Utils.getMyUid(mContext)+"&key=ieeton.pay";
			Utils.logd("str:"+str);
			sign = Utils.getMessageDigest(str.getBytes()).toUpperCase();
		}
		params.putString("sign", sign);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getOrders(int pageSize, int pageNum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetMyOrder_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));		
		params.putInt("pageSize", pageSize);
		params.putInt("pageNum", pageNum);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getFavoriteArticle(int pageNum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetFavoriteArticle_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));		
		params.putInt("pageNum", pageNum);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getArticle(String id) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetArticleDetail_m_130.ashx");
        
		Bundle params = new Bundle();
		//params.putString("passport", Utils.getPassport(mContext));
		params.putString("articleId", id);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getRecommondProduct(int id, int categoryid, double longitude, double latitude,
				int cityid, int page) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetRecommendProduct_m_130.ashx");
        
		Bundle params = new Bundle();
		if (id>0){
			params.putInt("id", id);
			params.putInt("type", 2);
		}else if (categoryid > 0){
			params.putInt("categoryid", categoryid);
			params.putInt("type", 1);
		}else{
			params.putInt("type", 3);
		}
		params.putString("uid", Utils.getMyUid(mContext));
		params.putDouble("longitude", longitude);
		params.putDouble("latitude", latitude);
//		params.putInt("age", age);
		params.putInt("cityid", cityid);
		params.putInt("pageNum", page);
		params.putInt("pageSize", 20);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getUserProduct(String uid, int page) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
	StringBuilder url = new StringBuilder(getContentServerUrl());
	url.append("GetUserProduct_m_130.ashx");
    
	Bundle params = new Bundle();
	params.putString("uid", uid);
	params.putInt("pageNum", page);
	
	String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
	return result;
}
	
	public String searchProduct(int lableid, int categoryid, double longitude, double latitude,
			String keyword, int cityid, int page) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("SearchProduct_m_130.ashx");
        
		Bundle params = new Bundle();
		if (lableid > 0){
			params.putInt("id", lableid);
		}
		if (categoryid > 0){
			params.putInt("categoryId", categoryid);
		}
		params.putDouble("longitude", longitude);
		params.putDouble("latitude", latitude);
		params.putString("search", keyword);
		params.putInt("cityid", cityid);
		params.putInt("pageNum", page);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getHotKeyword() throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("hotsearch_m_130.ashx");
        
		Bundle params = new Bundle();
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getProductComment(String productId, String ownerId, int pageNum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetProductComment_m_130.ashx");
        
		Bundle params = new Bundle();
		//params.putString("uid", Utils.getMyUid(mContext));
		params.putString("productId", productId);
		params.putString("owneruid", ownerId);
		params.putInt("pageNum", pageNum);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getInstitutionDoctor(String uid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetInstitutionDoctor_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", uid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String favorateProduct(String productId, String ownerId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("FavorateProduct_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putString("productId", productId);
		params.putString("ownerUid", ownerId);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}

	public String unFavorateProduct(String productId, String ownerId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("UnFavorateProduct_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putString("productId", productId);
		params.putString("ownerUid", ownerId);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}

	public String cancelOrder(String orderId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("CancelOrder_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putString("orderId", orderId);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}

	public String deleteOrder(String orderId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("DeleteOrder_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putString("orderId", orderId);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}

	public String cancelSubscribe(String orderId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("CancelSubscribe_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putString("orderId", orderId);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getCount() throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetCount_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getFavorateProduct(int pageNum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetFavorateProduct_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putInt("pageNum", pageNum);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getIntegralProduct(int cityid, double longitude, double latitude, 
			int pageNum) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetIntegralProduct_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		if (longitude > 0){
			params.putDouble("longitude", longitude);
			params.putDouble("latitude", latitude);
		}
		if (cityid > 0){
			params.putInt("cityid", cityid);
		}
		params.putInt("pageNum", pageNum);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String addComment(String internalid, String ownerid, String productid, String content, 
			int level, List<String> pics) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("AddProductComment_m_130.ashx");
        
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putString("productId", productid);
		params.putString("ownerUid", ownerid);
		params.putString("content", content);
		params.putString("internalId", internalid);
		params.putInt("level", level);
		for(int i=0; pics!=null&&i<pics.size(); i++){
			params.putString("image"+(i+1), pics.get(i));
		}
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getSubscribeInfo(String orderId, String productid, String ownerid) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetSubscribeInfo_m_130.ashx");
		
		Bundle params = new Bundle();
		if (!TextUtils.isEmpty(Utils.getMyUid(mContext))){
			params.putString("uid", Utils.getMyUid(mContext));
		}
		if (!TextUtils.isEmpty(orderId)){
			params.putString("orderId", orderId);
		}
		if (!TextUtils.isEmpty(ownerid)){
			params.putString("owneruid", ownerid);
		}
		if (!TextUtils.isEmpty(productid)){
			params.putString("productid", productid);
		}
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
 	}
	
	public String getSignInfo() throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("IsSign_m_130.ashx");
		
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String sign(int integral) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("PostSign_m_130.ashx");
		
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putInt("integral", integral);		
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String updateSubcribe(String internalId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("updateappointment_m_130.ashx");
		
		Bundle params = new Bundle();
		params.putString("internalId", internalId);		
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String updateAgeLable(int minAge, int maxAge, String lable) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("updateAgeLable_m_130.ashx");
		
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		if (minAge > 0){
			params.putInt("minage", minAge);
		}
		if (maxAge > 0){
			params.putInt("maxage", maxAge);
		}
		if (!TextUtils.isEmpty(lable)){
			params.putString("label", lable);
		}
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getAgeLable() throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("GetLableAndAge_m_130.ashx");
		
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String addIntegral(String type, int integral) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("addUserIntegral_m_130.ashx");
		
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		if (TextUtils.isEmpty(type)){
			params.putString("type", type);
		}
		params.putInt("integral", integral);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String requestWelcome() throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("InsertYuyue_m_130.ashx");
		
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putString("type", "6");

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String updateSubscribInfo(String orderId, String dateTime, String mobile) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("upsubscribes_m_130.ashx");
		
		Bundle params = new Bundle();
		params.putString("internalId", orderId);
		if (!TextUtils.isEmpty(dateTime)){
			params.putString("datime", dateTime);
		}
		if (!TextUtils.isEmpty(mobile)){
			params.putString("phone", mobile);
		}

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String uploadUserAction(String productId, String ownerId) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
		StringBuilder url = new StringBuilder(getContentServerUrl());
		url.append("chainStatistics_m_130.ashx");
		
		Bundle params = new Bundle();
		params.putString("uid", Utils.getMyUid(mContext));
		params.putString("productid", productId);
		params.putString("owneruid", ownerId);

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
}
