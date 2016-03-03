package com.ieeton.agency.activity;

import com.ieeton.agency.utils.SocialShareUtils;
import com.ieeton.agency.R;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.sso.UMSsoHandler;

import android.content.Intent;
import android.os.Bundle;

public class MyQrcodeActivity extends TemplateActivity {

	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			share();
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.my_qrcode_activity);
		setTitleBar(getString(R.string.back), getString(R.string.my_qrcode), 
					getString(R.string.share));
	}

	private void share(){
		String url = "http://www.ieeton.com/";
		SocialShareUtils.shareToWX(this, getString(R.string.share_title), getString(R.string.share_qrcode_content), 
				null, R.drawable.myprofile_icon_docqrcode,url);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/**使用SSO授权必须添加如下代码 */
		UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
	    UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode) ;
	    if(ssoHandler != null){
	       ssoHandler.authorizeCallBack(requestCode, resultCode, data);
	    }
	}
}
