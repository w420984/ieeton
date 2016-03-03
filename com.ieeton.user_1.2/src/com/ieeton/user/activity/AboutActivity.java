package com.ieeton.user.activity;

import com.ieeton.user.R;
import com.ieeton.user.net.NetEngine;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AboutActivity extends TemplateActivity implements OnClickListener {
	private LinearLayout mBackBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.about);
		setTitleBar(null, null, null);
		
		TextView version = (TextView) findViewById(R.id.tv_version);
		PackageInfo info;
		String nowVersion = "";
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
            nowVersion = info.versionName;
        } catch (NameNotFoundException e) {
        }
        version.setText(getString(R.string.about_version)+nowVersion);
        
        TextView contact_us = (TextView)findViewById(R.id.tv_contact_us);
        String number = NetEngine.getInstance(AboutActivity.this).getIvrNumber();
//        number = number.substring(0, 3) + "-" + number.substring(3, 6) + "-" + number.substring(6);
        contact_us.setText(getString(R.string.contact_number) + number);
        //不支持点击拨号，节约线路成本
//        contact_us.setOnClickListener(this);
        
        TextView email_us = (TextView)findViewById(R.id.tv_email);
        email_us.setOnClickListener(this);
        
        mBackBtn = (LinearLayout)findViewById(R.id.ll_back);
		mBackBtn.setOnClickListener(this);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.ll_back){
			finish();
		}else if(v.getId() == R.id.tv_contact_us){
			String number = NetEngine.getInstance(AboutActivity.this).getIvrNumber();
			number.replace("-", "");
			if(number != null && !"".equals(number)){
				Intent intent=new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+number));
				startActivity(intent);
			}
		}else if(v.getId() == R.id.tv_email){
			String mail_addr = getString(R.string.contact_email);
			int index = mail_addr.indexOf("：");
			if(index >= 0){
				mail_addr = mail_addr.substring(index + 1);
			}
			

		}
		super.onClick(v);
	}

	@Override
	protected void handleTitleBarEvent(int eventId) {
		
	}
}
