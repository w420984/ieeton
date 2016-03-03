package com.ieeton.agency.activity;

import java.util.concurrent.RejectedExecutionException;

import com.ieeton.agency.models.Doctor;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.UpdateProfileTask;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class SetPriceActivity extends TemplateActivity {
	private EditText mEtPrice;
	private ImageView mIvDelete;
	private CustomToast mProgressDialog;
	private UpdateProfileTask mTask;

	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			save();
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.set_price);
		setTitleBar(getString(R.string.back), getString(R.string.set_price), getString(R.string.save));

		mEtPrice = (EditText) findViewById(R.id.et_price);
		mIvDelete = (ImageView) findViewById(R.id.iv_delete);
		mIvDelete.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v == mIvDelete){
			mEtPrice.setText("");
		}
		super.onClick(v);
	}

	private void save(){
		String price = mEtPrice.getText().toString();
		if (TextUtils.isEmpty(price)){
			Utils.showToast(this, R.string.input_price_error, Toast.LENGTH_SHORT);
			return;
		}
		Doctor doctor = new Doctor();
		doctor.setPrice(Integer.valueOf(price));
		mTask = new UpdateProfileTask(this, doctor) {
			
			@Override
			protected void updateEnd(boolean success) {
				dismissProgress();
				if (success){
					Intent intent = new Intent(Constants.ACTION_UPDATE_INFO);
					sendBroadcast(intent);
					finish();
				}
			}
			
			@Override
			protected void updateCancel() {
				dismissProgress();
			}
			
			@Override
			protected void updateBegin() {
				showProgress();
			}
		};
		try {
			mTask.execute();
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private void showProgress(){
		if (mProgressDialog == null){
			mProgressDialog = Utils.createProgressCustomToast(R.string.loading, this);
		}
		mProgressDialog.show();
	}
	
	private void dismissProgress(){
		if (mProgressDialog != null){
			mProgressDialog.cancel();
		}
	}
}
