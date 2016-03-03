package com.ieeton.agency.utils;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class SocialShareUtils {
	static final UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
	
	private static SocialShareUtils mInstance;
	private static Context mContext;
	public static String SERVER_HOST = "http://data.ieeton.com/data/VersionCheck.json";
	
	
	public SocialShareUtils(Context context){
		mContext = context;
	}
	
	public static SocialShareUtils getInstance(Context context){
		mContext = context;
		if (mInstance == null){
			mInstance = new SocialShareUtils(context);
		}
		return mInstance;
	}
	
	public static void shareToWX(final Activity activity, String title, String content, 
				String picPath, int picId, String url){
		// 添加微信平台
		UMWXHandler wxHandler = new UMWXHandler(activity, Utils.getWeixinAppkey(activity), Utils.getWeixinAppSecret(activity));
		wxHandler.addToSocialSDK();
		Log.v("sereinli", "weixin,appkey:"+Utils.getWeixinAppkey(activity)+" secret:"+Utils.getWeixinAppSecret(activity));

		// 支持微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(activity,Utils.getWeixinAppkey(activity), Utils.getWeixinAppSecret(activity));
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();
		
		//QQ
		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(activity, Utils.getQQAppID(activity),Utils.getQQAppkey(activity));
		qqSsoHandler.addToSocialSDK();  
		
		//Sina微博
		SinaSsoHandler sinaSsoHandler = new SinaSsoHandler(activity);
		sinaSsoHandler.addToSocialSDK();
		

		//设置微信好友分享内容
		WeiXinShareContent weixinContent = new WeiXinShareContent();
		//设置分享文字
		if (!TextUtils.isEmpty(content)){
			weixinContent.setShareContent(content);
		}
		//设置title
		weixinContent.setTitle(title);
		//设置分享内容跳转URL
		if (!TextUtils.isEmpty(url)){
			weixinContent.setTargetUrl(url);
		}
		//设置分享图片
		if (picPath != null && FileUtils.isFileExist(picPath)){
			weixinContent.setShareImage(new UMImage(activity, picPath));
		}else if (picId > 0){
			weixinContent.setShareImage(new UMImage(activity, picId));
		}
		mController.setShareMedia(weixinContent);
		
		
		//设置微信朋友圈分享内容
		CircleShareContent circleMedia = new CircleShareContent();
		if (!TextUtils.isEmpty(content)){
			circleMedia.setShareContent(content);
		}
		//设置朋友圈title
		circleMedia.setTitle(title+"。"+content);
		if (picPath != null && FileUtils.isFileExist(picPath)){
			circleMedia.setShareImage(new UMImage(activity, picPath));
		}else if (picId > 0){
			circleMedia.setShareImage(new UMImage(activity, picId));
		}
		if (!TextUtils.isEmpty(url)){
			circleMedia.setTargetUrl(url);
		}
		mController.setShareMedia(circleMedia);
		
		//新浪微博
		SinaShareContent sinaWeiboMedia = new SinaShareContent();
		if (!TextUtils.isEmpty(content)){
			sinaWeiboMedia.setShareContent(title+ "。" + content 
					+ "。" + url);
		}
//		sinaWeiboMedia.setTitle(title);
		if (picPath != null && FileUtils.isFileExist(picPath)){
			sinaWeiboMedia.setShareImage(new UMImage(activity, picPath));
		}else if (picId > 0){
			sinaWeiboMedia.setShareImage(new UMImage(activity, picId));
		}
//		if (!TextUtils.isEmpty(url)){
//			sinaWeiboMedia.setTargetUrl(url);
//		}
		mController.setShareMedia(sinaWeiboMedia);
		
		//QQ分享
		QQShareContent qqMedia = new QQShareContent();
		if (!TextUtils.isEmpty(content)){
			qqMedia.setShareContent(content);
		}
		qqMedia.setTitle(title);
		if (picPath != null && FileUtils.isFileExist(picPath)){
			qqMedia.setShareImage(new UMImage(activity, picPath));
		}else if (picId > 0){
			qqMedia.setShareImage(new UMImage(activity, picId));
		}
		if (!TextUtils.isEmpty(url)){
			qqMedia.setTargetUrl(url);
		}
		mController.setShareMedia(qqMedia);
		
		mController.setAppWebSite("");
		mController.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.SINA, SHARE_MEDIA.QQ);
		mController.openShare(activity, new SnsPostListener() {
			
			@Override
			public void onStart() {
				Toast.makeText(activity, "开始分享.", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onComplete(SHARE_MEDIA arg0, int eCode, SocializeEntity entity) {
				if (eCode == 200) {
                    Toast.makeText(activity, "分享成功.", Toast.LENGTH_SHORT).show();
                } else {
                     String eMsg = "";
                     if (eCode == -101){
                         eMsg = "没有授权";
                     }
                     Toast.makeText(activity, "分享失败[" + eCode + "] " + 
                                        eMsg,Toast.LENGTH_SHORT).show();
                }
				activity.finish();
			}
		});	
	}
		
	public void callback(int requestCode, int resultCode, Intent data){
		UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode) ;
		if(ssoHandler != null){
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}
}
