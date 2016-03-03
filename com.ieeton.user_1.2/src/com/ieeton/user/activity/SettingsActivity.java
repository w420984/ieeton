package com.ieeton.user.activity;

import java.io.File;
import java.text.DecimalFormat;
import java.util.concurrent.RejectedExecutionException;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.ieeton.user.IeetonApplication;
import com.ieeton.user.R;
import com.ieeton.user.models.Settings;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.SliderSwitchView;
import com.ieeton.user.view.SliderSwitchView.OnChangedListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends TemplateActivity implements OnChangedListener, OnClickListener{
	private SliderSwitchView mSoundViaSwitch;
	private SliderSwitchView mAutoClearCache;
	private RelativeLayout mExitLayout;
	private RelativeLayout mSettingLayout;
	private RelativeLayout mCleanCacheLayout;
	private Settings mCurSettings;
	private LinearLayout mBackBtn;
	private EMChatOptions mChatOptions;
	private static final int DIALOG_ALERT_CLEAR_CACHE = 1000;
	private static final int DIALOG_PROGRESS_DOING = 1001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_settings);
		setTitleBar(null, null, null);
		
		mChatOptions = EMChatManager.getInstance().getChatOptions();
		mChatOptions.setNotificationEnable(true);
		EMChatManager.getInstance().setChatOptions(mChatOptions);

		
		mCurSettings = Utils.getSettings(SettingsActivity.this);
		
		mSoundViaSwitch = (SliderSwitchView)findViewById(R.id.slider_sound_onoff);
		mSoundViaSwitch.setSwitchType(SliderSwitchView.SWITCH_ON_OFF);
		mSoundViaSwitch.setChecked(mCurSettings.getViaLoundSpeaker());
		mSoundViaSwitch.setOnChangedListener(this);

		mAutoClearCache = (SliderSwitchView)findViewById(R.id.slider_cache_onoff);
		mAutoClearCache.setSwitchType(SliderSwitchView.SWITCH_ON_OFF);
		mAutoClearCache.setChecked(mCurSettings.getAutoCleatCache());
		mAutoClearCache.setOnChangedListener(this);
		
		mCleanCacheLayout = (RelativeLayout)findViewById(R.id.rl_clean_cache);
		mCleanCacheLayout.setOnClickListener(this);
		
		mExitLayout = (RelativeLayout)findViewById(R.id.rl_exit);
		mExitLayout.setOnClickListener(this);		
		if (Utils.getMyType(this) == 5){
			mExitLayout.setVisibility(View.GONE);
		}
		
		mBackBtn = (LinearLayout)findViewById(R.id.ll_back);
		mBackBtn.setOnClickListener(this);
		
		mSettingLayout = (RelativeLayout)findViewById(R.id.rl_my_setting);
		mSettingLayout.setOnClickListener(this);
		
		try{
			CleanCacheTask task = new CleanCacheTask();
			task.execute();
		}catch(RejectedExecutionException e){
			e.printStackTrace();
		}
}

	@Override
	public void OnChanged(SliderSwitchView sliderSwitch, boolean checkState) {
		if(mSoundViaSwitch == sliderSwitch){
			mCurSettings.setViaLoundSpeaker(checkState);
			mChatOptions.setUseSpeaker(checkState);
			//设置环信提醒
			EMChatManager.getInstance().setChatOptions(mChatOptions);
		}else if(mAutoClearCache == sliderSwitch){
			mCurSettings.setAutoClearCache(checkState);
		}
		
		Utils.saveSettings(SettingsActivity.this, mCurSettings);
	}

	@Override
	public void onClick(View v) {
		if(v ==  mExitLayout){
			IeetonApplication.getInstance().logout();
			
//			Utils.setMyUid(SettingsActivity.this, null);
//			Utils.setPassport(SettingsActivity.this, null);
//			Utils.saveSettings(SettingsActivity.this, null);
//
//			Intent successIntent = new Intent(Constants.NEED_RELOGIN_ACTION);
//			sendBroadcast(successIntent);

			finish();
		}else if(v ==  mBackBtn){
			finish();
		}else if(v == mSettingLayout){
			startActivity(new Intent(SettingsActivity.this, SettingMessageActivity.class));
		}else if(v == mCleanCacheLayout){
			showDialog(DIALOG_ALERT_CLEAR_CACHE);
		}
		super.onClick(v);
	}

	@Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case DIALOG_ALERT_CLEAR_CACHE:
    			AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
    			builder.setTitle(R.string.clear_cache)
    					.setMessage(R.string.clean_cache_summary)
    					.setCancelable(false)
    					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
    						
    						@Override
    						public void onClick(DialogInterface dialog, int which) {
                                try {
                                    new AsyncTask<Void, Void, Void>() {

                                        protected Void doInBackground(Void... params) {
                                            // FileUtil.deleteFiles(getCacheDir());
                                            clearCache();
                                            return null;
                                        }

                                        protected void onPostExecute(Void result) {
                                            super.onPostExecute(result);
                                            try {
                                                dismissDialog(DIALOG_PROGRESS_DOING);
                                                ((TextView)findViewById(R.id.tv_clean_cache)).setText("0MB");
                                            } catch (IllegalArgumentException e) {
                                                // 任务执行时间比较长，
                                                // 对话框有可能被用户关闭，
                                                // 程序再次关闭Dialog，会出现异常
                                                // 捕获异常，避免crash
                                            }
                                            Toast.makeText(SettingsActivity.this,
                                                    R.string.setting_clear_success,
                                                    android.widget.Toast.LENGTH_SHORT);
                                        }

                                        protected void onPreExecute() {
                                            super.onPreExecute();
                                            showDialog(DIALOG_PROGRESS_DOING);
                                        }

                                    }.execute();
                                } catch (RejectedExecutionException e) {
                                    // RejectedExecutionException thread pool full
                                    Utils.loge(e);
                                }
                            }
    					})
    					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
    						
    						@Override
    						public void onClick(DialogInterface dialog, int which) {
    							dialog.dismiss();
    						}
    					});
    			dialog = builder.create();
    			dialog.show();
                break;
            case DIALOG_PROGRESS_DOING:
                dialog = new ProgressDialog(this);
                ((ProgressDialog) dialog)
                        .setMessage(getString(R.string.setting_clear_doing));
                break;
        }
        return dialog;
    }
    
    private void clearCache(){
    	//清除头像缓存
        Utils.clearDirectory(new File(Constants.FOLDER_PORTRAIT));
        
        //清除搜索记录
        String path = getCacheDir().getAbsolutePath() + "/searchkeywordlistcaches/" + Utils.getMyUid(SettingsActivity.this);
        Utils.clearDirectory(new File(path));
    }	
    
    public class CleanCacheTask extends AsyncTask<Void, Void, Long>{

		@Override
		protected Long doInBackground(Void... params) {
            long size = Utils.getCacheSize(new File(Constants.FOLDER_PORTRAIT));
            return size;
		}

		@Override
		protected void onPostExecute(Long result) {
            if(!isFinishing()){
                double size = (result + 0.0) / (1024 * 1024);  
                
                DecimalFormat df = new DecimalFormat("0.0");// 以Mb为单位保留两位小数  
                String filesize = df.format(size) + "MB";  
                ((TextView)findViewById(R.id.tv_clean_cache)).setText(filesize);
            }
		}
    	
    }

	@Override
	protected void handleTitleBarEvent(int eventId) {
		
	}
}
