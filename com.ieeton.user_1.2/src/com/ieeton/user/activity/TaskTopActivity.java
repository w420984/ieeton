package com.ieeton.user.activity;


import com.ieeton.user.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * 增加此界面是为了能在注销账号时的登录界面按返回能退出应用
 *
 */
public class TaskTopActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(MainActivity.INPUT_INDEX, 
				getIntent().getIntExtra(MainActivity.INPUT_INDEX, MainActivity.INPUT_HOME));
    	startActivity(intent);  		
    	//startActivity(new Intent(this, AddCommentActivity.class));  		
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
