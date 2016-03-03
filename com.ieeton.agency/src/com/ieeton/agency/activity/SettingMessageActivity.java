package com.ieeton.agency.activity;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.ieeton.agency.utils.PreferenceUtils;
import com.ieeton.agency.view.SliderSwitchView;
import com.ieeton.agency.view.SliderSwitchView.OnChangedListener;
import com.ieeton.agency.R;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class SettingMessageActivity extends TemplateActivity implements OnChangedListener{
	private SliderSwitchView mSwitchMessage;
	private SliderSwitchView mSwitchSound;
	private SliderSwitchView mSwitchVibration;
	private EMChatOptions mChatOptions;
	private ViewGroup mVgSound;
	private ViewGroup mVgVibrate;

	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.setting_message);
		setTitleBar(getString(R.string.back), getString(R.string.new_messages), null);

		mChatOptions = EMChatManager.getInstance().getChatOptions();
		
		mSwitchMessage = (SliderSwitchView) findViewById(R.id.switch_new_message);
		mSwitchMessage.setOnChangedListener(this);
		mSwitchMessage.setChecked(mChatOptions.getNotificationEnable());
		
		mVgSound = (ViewGroup) findViewById(R.id.rl_switch_sound);
		mSwitchSound = (SliderSwitchView) findViewById(R.id.switch_sound);
		mSwitchSound.setOnChangedListener(this);
		mSwitchSound.setChecked(mChatOptions.getNoticedBySound());
		
		mVgVibrate = (ViewGroup) findViewById(R.id.rl_switch_vibrate);
		mSwitchVibration = (SliderSwitchView) findViewById(R.id.switch_vibration);
		mSwitchVibration.setOnChangedListener(this);
		mSwitchVibration.setChecked(mChatOptions.getNoticedByVibrate());
		
		if (mChatOptions.getNotificationEnable()){
			mVgSound.setVisibility(View.VISIBLE);
			mVgVibrate.setVisibility(View.VISIBLE);
		}else{
			mVgSound.setVisibility(View.GONE);
			mVgVibrate.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void OnChanged(SliderSwitchView sliderSwitch, boolean checkState) {
		if (sliderSwitch == mSwitchMessage){
			mChatOptions.setNotificationEnable(checkState);
			EMChatManager.getInstance().setChatOptions(mChatOptions);
			PreferenceUtils.getInstance(this).setSettingMsgNotification(checkState);
			if (checkState){
				mVgSound.setVisibility(View.VISIBLE);
				mVgVibrate.setVisibility(View.VISIBLE);
			}else{
				mVgSound.setVisibility(View.GONE);
				mVgVibrate.setVisibility(View.GONE);
			}
		}else if (sliderSwitch == mSwitchSound){
			mChatOptions.setNoticeBySound(checkState);
			EMChatManager.getInstance().setChatOptions(mChatOptions);
			PreferenceUtils.getInstance(this).setSettingMsgSound(checkState);
		}else if (sliderSwitch == mSwitchVibration){
			mChatOptions.setNoticedByVibrate(checkState);
			EMChatManager.getInstance().setChatOptions(mChatOptions);
			PreferenceUtils.getInstance(this).setSettingMsgVibrate(checkState);
		}
	}

}
