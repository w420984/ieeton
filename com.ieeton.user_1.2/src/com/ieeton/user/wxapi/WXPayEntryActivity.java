package com.ieeton.user.wxapi;


import com.ieeton.user.R;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
	
    private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Utils.logd("WXPayEntryActivity onCreate");
        setContentView(R.layout.pay_result);
        
    	api = WXAPIFactory.createWXAPI(this, Utils.getWeixinAppkey(this));

        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		Utils.logd("WXPayEntryActivity onNewIntent");
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		Utils.logd("WXPayEntryActivity onReq");
	}

	@Override
	public void onResp(BaseResp resp) {
		Utils.logd("onPayFinish errCode = " + resp.errCode);

		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			if (resp.errCode == BaseResp.ErrCode.ERR_OK){
				Intent intent = new Intent(Constants.WEIXIN_PAY_SUCCESS_ACTION);
				sendBroadcast(intent);
				finish();
			}else{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				String message = "";
				if (resp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL){
					message = getString(R.string.user_cancel);
				}else{
					message = getString(R.string.pay_result_callback_msg, resp.errStr +";code=" + String.valueOf(resp.errCode));
				}
				builder.setTitle("提示");
				builder.setMessage(message);
				builder.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						finish();
					}
				});
				builder.show();
			}
		}
	}
}