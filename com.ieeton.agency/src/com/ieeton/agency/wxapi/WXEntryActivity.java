
package com.ieeton.agency.wxapi;

import android.content.Intent;
import android.os.Bundle;

import com.ieeton.agency.utils.Utils;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.umeng.socialize.weixin.view.WXCallbackActivity;

public class WXEntryActivity extends WXCallbackActivity {

	@Override
	protected IWXAPI getWXApi() {
		Utils.logd("getWXApi");
		return super.getWXApi();
	}

	@Override
	protected void handleIntent(Intent intent) {
		Utils.logd("handleIntent");
		super.handleIntent(intent);
	}

	@Override
	protected void initWXHandler() {
		Utils.logd("initWXHandler");
		super.initWXHandler();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Utils.logd("onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onReq(BaseReq req) {
		Utils.logd("onReq");
		super.onReq(req);
	}

	@Override
	public void onResp(BaseResp resp) {
		Utils.logd("onResp");
		super.onResp(resp);
	}

}
