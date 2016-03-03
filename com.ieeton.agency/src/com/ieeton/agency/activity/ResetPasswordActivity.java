package com.ieeton.agency.activity;

import java.util.concurrent.RejectedExecutionException;

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.R;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

public class ResetPasswordActivity extends TemplateActivity {
	private class ResetPasswordTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(ResetPasswordActivity.this)
							.updatePassword(Utils.getPassport(ResetPasswordActivity.this), 
									mEtOldPassword.getText().toString(),
									mEtNewPassword.getText().toString(), 
									mEtNewPasswordCheck.getText().toString());
			} catch (PediatricsIOException e) {
				mThr = e;
				e.printStackTrace(); 
			} catch (PediatricsParseException e) {
				mThr = e;
				e.printStackTrace();
			} catch (PediatricsApiException e) {
				mThr = e;
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onCancelled() {
			dismissProgress();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(ResetPasswordActivity.this, R.string.reset_password_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			Utils.showToast(ResetPasswordActivity.this, R.string.reset_password_success, Toast.LENGTH_SHORT);
			//startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
			finish();
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}

	private EditText mEtOldPassword;
	private EditText mEtNewPassword;
	private EditText mEtNewPasswordCheck;

	private CustomToast mProgressDialog;
	private ResetPasswordTask mTask;
	
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
		setView(R.layout.reset_password);
		setTitleBar(getString(R.string.back), getString(R.string.reset_password), getString(R.string.save));
		
		mEtOldPassword = (EditText) findViewById(R.id.et_old_password);
		mEtNewPassword = (EditText) findViewById(R.id.et_new_password);
		mEtNewPasswordCheck = (EditText) findViewById(R.id.et_password_check);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private void save(){
		String oldPassword = mEtOldPassword.getText().toString();
		String newPassword = mEtNewPassword.getText().toString();
		String newPasswordCheck = mEtNewPasswordCheck.getText().toString();
		if (!Utils.checkPassword(oldPassword) || !Utils.checkPassword(newPassword)
			|| !Utils.checkPassword(newPasswordCheck)){
			Utils.showToast(this, R.string.input_password_error, Toast.LENGTH_SHORT);
			return;
		}
		if (!newPassword.equals(newPasswordCheck)){
			Utils.showToast(this, R.string.password_check_error, Toast.LENGTH_SHORT);
			return;
		}
		
		mTask = new ResetPasswordTask();
		try {
			mTask.execute();
		} catch (RejectedExecutionException e) {
			// TODO Auto-generated catch block
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
