package com.ieeton.agency.activity;

import java.io.File;
import java.text.DecimalFormat;
import java.util.concurrent.RejectedExecutionException;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.ieeton.agency.DemoApplication;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.PreferenceUtils;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.SliderSwitchView;
import com.ieeton.agency.view.SliderSwitchView.OnChangedListener;
import com.ieeton.agency.R;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SocializeClientListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SettingMainActivity extends TemplateActivity implements OnChangedListener{
	private ViewGroup mVgLogout;
	private ViewGroup mVgMessage;
	private ViewGroup mVgClearCache;
	private SliderSwitchView mSwitchSound;
	EMChatOptions mChatOptions;
	private static final int DIALOG_ALERT_CLEAR_CACHE = 1000;
	private static final int DIALOG_PROGRESS_DOING = 1001;

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
		setView(R.layout.setting_main);
		setTitleBar(getString(R.string.back), getString(R.string.setting), null);
		mChatOptions = EMChatManager.getInstance().getChatOptions();

		mVgLogout = (ViewGroup) findViewById(R.id.rl_logout);
		mVgLogout.setOnClickListener(this);
		mVgMessage = (ViewGroup) findViewById(R.id.rl_messages);
		mVgMessage.setOnClickListener(this);
		mSwitchSound = (SliderSwitchView) findViewById(R.id.switch_sound);
		mSwitchSound.setOnChangedListener(this);
		mSwitchSound.setChecked(mChatOptions.getUseSpeaker());
		
		mVgClearCache = (ViewGroup) findViewById(R.id.rl_clear_cache);
		mVgClearCache.setOnClickListener(this);
		try{
			CleanCacheTask task = new CleanCacheTask();
			task.execute();
		}catch(RejectedExecutionException e){
			e.printStackTrace();
		}
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
	protected void onDestroy() {
		
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v == mVgLogout){
			DemoApplication.getInstance().logout();
			Intent intent = new Intent(this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			thirdLogOut();
			finish();
		}else if (v == mVgMessage){
			startActivity(new Intent(this, SettingMessageActivity.class));
		}else if (v == mVgClearCache){
			showDialog(DIALOG_ALERT_CLEAR_CACHE);
		}
		super.onClick(v);
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case DIALOG_ALERT_CLEAR_CACHE:
    			AlertDialog.Builder builder = new AlertDialog.Builder(SettingMainActivity.this);
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
                                            Utils.showToast(SettingMainActivity.this,
                                                    R.string.setting_clear_success,
                                                    Toast.LENGTH_SHORT);
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
    }	

	@Override
	public void OnChanged(SliderSwitchView sliderSwitch, boolean checkState) {
		if (sliderSwitch == mSwitchSound){
			mChatOptions.setUseSpeaker(checkState);
			EMChatManager.getInstance().setChatOptions(mChatOptions);
			PreferenceUtils.getInstance(this).setSettingMsgSpeaker(checkState);
		}
	}

	private void thirdLogOut(){
		if (Constants.LOGIN_MOBILE.equals(DemoApplication.mLoginType)){
			return;
		}
		SHARE_MEDIA platform = SHARE_MEDIA.QQ;
		if (Constants.LOGIN_QQ.equals(DemoApplication.mLoginType)){
			platform = SHARE_MEDIA.QQ;
		}else if (Constants.LOGIN_WEIBO.equals(DemoApplication.mLoginType)){
			platform = SHARE_MEDIA.SINA;
		}else if (Constants.LOGIN_WX.equals(DemoApplication.mLoginType)){
			platform = SHARE_MEDIA.WEIXIN;
		}
		Utils.logd("platform:"+platform);
		UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.login");
		mController.deleteOauth(this, platform,
				 new SocializeClientListener() {
					 @Override
					 public void onStart() {
							Utils.logd("deleteOauth onStart");
					 }
					 @Override
					 public void onComplete(int status, SocializeEntity entity) {
					 if (status == 200) {
							Utils.logd("deleteOauth success");
	//					 Toast.makeText(mContext, "删除成功.", 
	//							 	Toast.LENGTH_SHORT).show();
					 } else {
							Utils.logd("deleteOauth failed");
	//					 Toast.makeText(mContext, "删除失败", 
	//							 	Toast.LENGTH_SHORT).show();
					 }
				 }
		});
	}
}
