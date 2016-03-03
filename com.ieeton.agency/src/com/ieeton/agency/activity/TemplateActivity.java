package com.ieeton.agency.activity;

import com.ieeton.agency.view.BaseLayout;
import com.ieeton.agency.R;
import com.umeng.analytics.MobclickAgent;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

public abstract class TemplateActivity extends BaseActivity implements OnClickListener{
	public final static int RIGHT_BUTTON = 0;
	public final static int LEFT_BUTTON = 1;
	protected BaseLayout ly;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
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
	
}
