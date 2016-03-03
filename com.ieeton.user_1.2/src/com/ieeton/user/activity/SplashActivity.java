package com.ieeton.user.activity;

import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.IeetonApplication;
import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.ServerHostData;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Utils;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateConfig;
import com.umeng.update.UpdateStatus;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class SplashActivity extends Activity {
	PushAgent mPushAgent;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash);
        
        //开启友盟push服务
        mPushAgent = PushAgent.getInstance(this);
        mPushAgent.enable();
                
        new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
		        UMUpdateCheck();
		        //获取domain urls
		        try{
		        	GetDomainUrlsTask task = new GetDomainUrlsTask();
		        	task.execute();
		        }catch(RejectedExecutionException e){
		        	e.printStackTrace();
		        }
		        
		        Utils.addUMengAlias(getApplicationContext(), Utils.getMyUid(getApplicationContext()));
			}
		}, 3000);        
    }

    private void goNextScreen(){
    	boolean show_guide = ((Utils.getUserGuideStatus(SplashActivity.this) == 0) ? true : false);
    	if(show_guide){
    		startActivity(new Intent(getApplicationContext(), UserGuideActivity.class));
    		finish();
    		return;
    	}
    	startActivity(new Intent(getApplicationContext(), TaskTopActivity.class));  		
    	finish();
    	
    }
        
    private void UMUpdateCheck(){
    	//友盟版本更新
    	MobclickAgent.updateOnlineConfig(getApplicationContext());
    	UpdateConfig.setDebug(false);	//调试信息开关
    	UmengUpdateAgent.setUpdateOnlyWifi(false);	//设置是否仅wifi模式才更新
    	UmengUpdateAgent.setUpdateCheckConfig(true);		//集成监测开关
    	UmengUpdateAgent.update(getApplicationContext());
    	UmengUpdateAgent.setDeltaUpdate(false);	//是否支持增量更新，默认全更新
    	
    	String updateConfig = MobclickAgent.getConfigParams(getApplicationContext(), "upgrade_mode");
    	Utils.logd("updateConfig="+updateConfig);
    	if (TextUtils.isEmpty(updateConfig)){
    		return;
    	}
    	try {
			JSONObject obj = new JSONObject(updateConfig);
			String versionCode = obj.getString("versionCode");
			String modeString = obj.getString("mode");
			if (TextUtils.isEmpty(versionCode) || TextUtils.isEmpty(modeString)){
				return;
			}
			String force_versionCode = obj.optString("force_update_versionCode");
			PackageInfo info = null;
			boolean flag = false;	//用来判断是否低于要求强制更新的最低版本
	        try {
	            info = getPackageManager().getPackageInfo(getPackageName(), 0);
				int curCode = info.versionCode;
				int forceCode = TextUtils.isEmpty(force_versionCode) ? 0 : Integer.valueOf(force_versionCode);
				Utils.logd("curCode="+curCode);
				Utils.logd("forceCode="+forceCode);
				if (curCode < forceCode){
					flag = true;
				}
	        } catch (NameNotFoundException e) {
	        	e.printStackTrace();
	        } catch (NumberFormatException e) {	        	
	        	e.printStackTrace();
	        }
			Utils.logd("modeString="+modeString);
			if ("F".equals(modeString) || flag){
				UmengUpdateAgent.setDialogListener(new UmengDialogButtonListener() {

				    @Override
				    public void onClick(int status) {
				        switch (status) {
				        case UpdateStatus.Update:
				            break;
				        default:
				        	if (SplashActivity.this.isFinishing()){
				        		Utils.showToast(getApplicationContext(), R.string.update_notice, Toast.LENGTH_SHORT);
								Utils.exitApp(SplashActivity.this);
								return;
				        	}
				        	AlertDialog.Builder builder =  new AlertDialog.Builder(SplashActivity.this);
				        	builder.setMessage(getString(R.string.update_notice));
				        	builder.setCancelable(false);		
				        	builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Utils.exitApp(SplashActivity.this);
								}
							});
				        	builder.show();
				        }
				    }
				});
			}else {
				
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
	private class GetDomainUrlsTask extends AsyncTask<Void, Void, ServerHostData>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected ServerHostData doInBackground(Void... arg0) {
			String result = "";

			try {
				result = NetEngine.getInstance(getApplicationContext()).getDomainUrls();
			} catch (PediatricsIOException e) {
				e.printStackTrace();
				mThr = e;
				return null;
			} catch (PediatricsParseException e) {
				e.printStackTrace();
				mThr = e;
				return null;
			} catch (PediatricsApiException e) {
				e.printStackTrace();
				mThr = e;
				return null;
			}

			JSONObject object = null;
			try {
				object = new JSONObject(result);
				ServerHostData host = new ServerHostData(getApplicationContext(),object);
				
				return host;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(ServerHostData result) {
			if (result == null || result.equals("")){
				//IeetonApplication.mServerHostData = null;
				if(mThr != null){
					Utils.handleErrorEvent(mThr, SplashActivity.this);
				}else{
					Utils.showToast(SplashActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			IeetonApplication.mServerHostData = result;
	        goNextScreen();
		}
	}

}
