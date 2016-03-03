package com.ieeton.agency.activity;

import com.ieeton.agency.R;

import android.os.Bundle;

public class AgreementActivity extends TemplateActivity {

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
		setView(R.layout.agreement);
		setTitleBar(getString(R.string.back), getString(R.string.agreement_title), null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
