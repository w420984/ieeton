package com.ieeton.agency.activity;

import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.R;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AboutActivity extends TemplateActivity {

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
		setView(R.layout.about);
		setTitleBar(getString(R.string.back), getString(R.string.about_us), null);
		
		TextView version = (TextView) findViewById(R.id.tv_version);
		PackageInfo info;
		String nowVersion = "";
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
            nowVersion = info.versionName;
        } catch (NameNotFoundException e) {
        }
        version.setText(getString(R.string.about_version)+nowVersion);
        
        TextView serviceMobile = (TextView) findViewById(R.id.service);
        String service = String.format(getString(R.string.service_mobile), NetEngine.getIvrNumber());
        serviceMobile.setText(service);
        
        ImageView iv_logo = (ImageView) findViewById(R.id.iv_logo);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);  
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        lp.setMargins(0, d.getHeight()/8, 0, 0);  
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        iv_logo.setLayoutParams(lp);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
