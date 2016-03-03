package com.ieeton.agency.utils;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import android.app.Activity;
import android.widget.Toast;

public class ShareToUtils {
	public static void shareToWX(final Activity activity, String title, String content, String picPath){
		// 添加微信平台
		UMWXHandler wxHandler = new UMWXHandler(activity,Utils.getWeixinAppkey(activity));
		wxHandler.addToSocialSDK();
		// 支持微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(activity,Utils.getWeixinAppkey(activity));
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();
		com.umeng.socialize.controller.UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
		
		//设置微信好友分享内容
		WeiXinShareContent weixinContent = new WeiXinShareContent();
		//设置分享文字
		weixinContent.setShareContent(content);
		//设置title
		weixinContent.setTitle(title);
		//设置分享内容跳转URL
		//weixinContent.setTargetUrl(Constants.WEB_BROWSER_CHENZAO_URL);
		//设置分享图片
		if (picPath != null && FileUtils.isFileExist(picPath)){
			weixinContent.setShareImage(new UMImage(activity, picPath));
		}
		mController.setShareMedia(weixinContent);
		
		//设置微信朋友圈分享内容
		CircleShareContent circleMedia = new CircleShareContent();
		circleMedia.setShareContent(content);
		//设置朋友圈title
		circleMedia.setTitle(title);
		if (picPath != null && FileUtils.isFileExist(picPath)){
			circleMedia.setShareImage(new UMImage(activity, picPath));
		}
		//circleMedia.setTargetUrl(Constants.WEB_BROWSER_CHENZAO_URL);
		mController.setShareMedia(circleMedia);
		
//		mController.setAppWebSite(Constants.WEB_BROWSER_CHENZAO_URL);
		mController.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE);
		//mController.getConfig().removePlatform( SHARE_MEDIA.RENREN, SHARE_MEDIA.DOUBAN, SHARE_MEDIA.SINA, SHARE_MEDIA.TENCENT);
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
}
