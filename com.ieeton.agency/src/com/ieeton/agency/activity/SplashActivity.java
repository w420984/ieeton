package com.ieeton.agency.activity;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.agency.Constant;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.FileUtils;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateConfig;
import com.umeng.update.UpdateStatus;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash);
        
        ImageView iv_logo = (ImageView) findViewById(R.id.iv_logo);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);  
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        lp.setMargins(0, d.getHeight()/4, 0, 0);  
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        iv_logo.setLayoutParams(lp);
        
        new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				goNextScreen();
		        UMUpdateCheck();
		        generatePortraitPath();
			}
		}, 3000);
    }

    private void generatePortraitPath(){
		String skipMiderScan = new StringBuilder(Constants.FOLDER_PORTRAIT).
				append(".nomedia").toString();
		File file = new File(skipMiderScan);
		FileUtils.makesureFileExist(file);
    }
    
    private void goNextScreen(){
//    	if (TextUtils.isEmpty(Utils.getPassport(this))){
//        	startActivity(new Intent(this, LoginActivity.class));
//    	}else if (Utils.needPerfectInfo(this)){
//    		startActivity(new Intent(this, PerfectInfoActivity.class));
//    	}else{
//    		startActivity(new Intent(this, MainActivity.class));  		
//    	}
    	startActivity(new Intent(this, TaskTopActivity.class)); 
    	finish();
    }
        
    private void UMUpdateCheck(){
    	//友盟版本更新
    	MobclickAgent.updateOnlineConfig(getApplicationContext());
    	UpdateConfig.setDebug(true);	//调试信息开关
    	UmengUpdateAgent.setUpdateOnlyWifi(false);	//设置是否仅wifi模式才更新
    	UmengUpdateAgent.setUpdateCheckConfig(true);		//集成监测开关
    	UmengUpdateAgent.update(getApplicationContext());
    	
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
				        	android.app.AlertDialog.Builder builder =  new android.app.AlertDialog.Builder(SplashActivity.this);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
