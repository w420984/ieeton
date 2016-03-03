package com.ieeton.user.activity;

import com.ieeton.user.R;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class AgreementActivity extends TemplateActivity {
	private LinearLayout mBackBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.agreement);
		setTitleBar(null, null, null);
		mBackBtn = (LinearLayout)findViewById(R.id.ll_back);
		mBackBtn.setOnClickListener(new OnClickListener() {
			
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
	protected void handleTitleBarEvent(int eventId) {
		
	}

}
