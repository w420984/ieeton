package com.ieeton.user.activity;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.ieeton.user.R;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.SliderSwitchView;
import com.ieeton.user.view.SliderSwitchView.OnChangedListener;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class SettingMessageActivity extends TemplateActivity implements OnChangedListener{
	private SliderSwitchView mSwitchMessage;
	private SliderSwitchView mSwitchSound;
	private SliderSwitchView mSwitchVibration;
	private LinearLayout mBtnBack;
	private boolean[] mSettings;
	private EMChatOptions mChatOptions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.setting_message);
		setTitleBar(null, null, null);
		
		mChatOptions = EMChatManager.getInstance().getChatOptions();
		mChatOptions.setNotificationEnable(true);
		EMChatManager.getInstance().setChatOptions(mChatOptions);
		
		mSettings = Utils.getMessageNotifySetting(SettingMessageActivity.this);

		mSwitchMessage = (SliderSwitchView) findViewById(R.id.switch_new_message);
		mSwitchMessage.setOnChangedListener(this);
		mSwitchMessage.setSwitchType(SliderSwitchView.SWITCH_ON_OFF);
		
		mSwitchSound = (SliderSwitchView) findViewById(R.id.switch_sound);
		mSwitchSound.setOnChangedListener(this);
		mSwitchSound.setSwitchType(SliderSwitchView.SWITCH_ON_OFF);
		mSwitchSound.setChecked(mSettings[0]);
		
		mSwitchVibration = (SliderSwitchView) findViewById(R.id.switch_vibration);
		mSwitchVibration.setOnChangedListener(this);
		mSwitchVibration.setSwitchType(SliderSwitchView.SWITCH_ON_OFF);
		mSwitchVibration.setChecked(mSettings[1]);
		
		mBtnBack = (LinearLayout)findViewById(R.id.ll_back);
		mBtnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void OnChanged(SliderSwitchView sliderSwitch, boolean checkState) {
		if(mSwitchMessage == sliderSwitch){
			
		}else if(mSwitchSound == sliderSwitch){
			mSettings[0] = checkState;
			mChatOptions.setNoticeBySound(checkState);
		}else if(mSwitchVibration == sliderSwitch){
			mSettings[1] = checkState;
			mChatOptions.setNoticedByVibrate(checkState);
		}
		//保存本地设置
		Utils.setMessageNotifySetting(SettingMessageActivity.this, mSettings);
		//设置环信提醒
		EMChatManager.getInstance().setChatOptions(mChatOptions);
	}

	@Override
	protected void handleTitleBarEvent(int eventId) {
		
	}

}
