package com.ieeton.agency.activity;

import com.ieeton.agency.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * 
 * 增加此界面是为了能在注销账号时的登录界面按返回能退出应用
 *
 */
public class TaskTopActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	if (TextUtils.isEmpty(Utils.getPassport(this))){
        	startActivityForResult(new Intent(this, LoginActivity.class), 1);
    	}else if (Utils.needBindMobile(this)){
    		startActivityForResult(new Intent(this, BindMobileActivity.class), 1);
    	}else if (Utils.needPerfectInfo(this)){
    		startActivityForResult(new Intent(this, PerfectInfoActivity.class), 1);
    	}else{
    		startActivityForResult(new Intent(this, MainActivity.class), 1);  		
    	}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if ((Intent.FLAG_ACTIVITY_CLEAR_TOP & intent.getFlags()) != 0) {   
            finish();   
		} 
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		finish();
		super.onActivityResult(requestCode, resultCode, data);
	}

}
