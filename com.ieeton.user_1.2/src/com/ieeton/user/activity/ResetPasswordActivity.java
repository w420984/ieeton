package com.ieeton.user.activity;
import java.util.concurrent.RejectedExecutionException;

import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.CustomToast;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


public class ResetPasswordActivity extends TemplateActivity implements OnClickListener {
	private class ResetPasswordTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(ResetPasswordActivity.this)
							.updatePassword(Utils.getMyUid(ResetPasswordActivity.this), 
									mEtOldPassword.getText().toString(), 
									mEtNewPassword.getText().toString());
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

	private ResetPasswordTask mTask;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_reset_password);
		setTitleBar(getString(R.string.back), getString(R.string.edit_password), 
				getString(R.string.save));
		
		mEtOldPassword = (EditText) findViewById(R.id.et_old_password);
		mEtNewPassword = (EditText) findViewById(R.id.et_new_password);
		mEtNewPasswordCheck = (EditText) findViewById(R.id.et_password_check);		
	}

	@Override
	protected void onDestroy() {
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
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
	}

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
}
