package com.ieeton.user.activity;

import com.ieeton.user.R;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.BaseLayout;
import com.ieeton.user.view.CustomToast;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

public abstract class TemplateActivity extends BaseActivity implements OnClickListener{
	public final static int RIGHT_BUTTON = 0;
	public final static int LEFT_BUTTON = 1;
	protected BaseLayout ly;
	private String fromPush;
	private CustomToast mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//友盟统计应用启动数据
		PushAgent.getInstance(this).onAppStart();
		Intent intent = getIntent();
		if (intent != null){
			fromPush = intent.getStringExtra("fromPush");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		MobclickAgent.onPageEnd(getCurActivityName()); 
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		MobclickAgent.onPageStart(getCurActivityName()); 
		MobclickAgent.onResume(this);
		super.onResume();
	}
	
	protected abstract void handleTitleBarEvent(int eventId);

	@Override
	public void onClick(View v) {
		if (v == this.ly.leftButton) {
			handleTitleBarEvent(LEFT_BUTTON);
		}
		else if (v == this.ly.rightButton || 
				v == this.ly.rightImage) {
			handleTitleBarEvent(RIGHT_BUTTON);
		}
	}

	protected void setTitleBar(String left, String middle,
			String right) {
		if (ly != null) {
			if (TextUtils.isEmpty(left) &&
					TextUtils.isEmpty(middle) &&
					TextUtils.isEmpty(right)){
				ly.titlebar.setVisibility(View.GONE);
			}else{				
				ly.setButtonTypeAndInfo(left, middle, right);
			}
		}
	}
	
	protected void setView(int resId) {
		setTheme(R.style.horizontal_slide);
		ly = new BaseLayout(this, resId);
		setContentView(ly);
		ly.leftButton.setOnClickListener(this);
		ly.rightButton.setOnClickListener(this);
		ly.rightImage.setOnClickListener(this);
	}
	
	private String getCurActivityName(){
		String shortClassName = "";
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        RunningTaskInfo info = manager.getRunningTasks(1).get(0);
        shortClassName = info.topActivity.getShortClassName();
        return shortClassName;
	}
	
	protected void backToMain(int mode){
		if ("true".equals(fromPush)){
			Intent intent = new Intent(this, TaskTopActivity.class);
			intent.putExtra(MainActivity.INPUT_INDEX, mode);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}
	
	protected void showProgress(){
		if (mProgressDialog == null){
			mProgressDialog = Utils.createProgressCustomToast(R.string.loading, this);
		}
		mProgressDialog.show();
	}
	
	protected void dismissProgress(){
		if (mProgressDialog != null){
			mProgressDialog.cancel();
		}
	}
}
