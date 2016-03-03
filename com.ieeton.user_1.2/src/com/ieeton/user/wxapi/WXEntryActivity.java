
package com.ieeton.user.wxapi;

import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.umeng.socialize.weixin.view.WXCallbackActivity;

public class WXEntryActivity extends WXCallbackActivity {

	@Override
	protected IWXAPI getWXApi() {
		// TODO Auto-generated method stub
		return super.getWXApi();
	}

	@Override
	protected void handleIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.handleIntent(intent);
	}

	@Override
	protected void initWXHandler() {
		// TODO Auto-generated method stub
		super.initWXHandler();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onReq(BaseReq req) {
		// TODO Auto-generated method stub
		super.onReq(req);
	}

	@Override
	public void onResp(BaseResp resp) {
		// TODO Auto-generated method stub
		super.onResp(resp);
	}

}