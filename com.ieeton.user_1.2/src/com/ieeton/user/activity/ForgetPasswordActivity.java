package com.ieeton.user.activity;

import java.util.concurrent.RejectedExecutionException;

import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Utils;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ForgetPasswordActivity extends TemplateActivity implements OnClickListener {
	class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
		}
		@Override
		public void onFinish() {//计时完毕时触发
			mBtnGetCode.setText(R.string.re_get_code);
			mBtnGetCode.setClickable(true);
			//mBtnGetCode.setBackgroundResource(R.drawable.btn_access_code_selector);
		}
		@Override
		public void onTick(long millisUntilFinished){//计时过程显示 
			mBtnGetCode.setClickable(false);
			mBtnGetCode.setText(millisUntilFinished /1000+"S");
			//mBtnGetCode.setBackgroundResource(R.drawable.button_code_s);
		}
	}

	private class SendCodeTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(ForgetPasswordActivity.this)
						.sendCode(mEtAccount.getText().toString(), "resetPassword");
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
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(ForgetPasswordActivity.this, R.string.get_code_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			Utils.showToast(ForgetPasswordActivity.this, R.string.get_code_success, Toast.LENGTH_SHORT);
		}
		
	}
	
	private class ResetPasswordTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(ForgetPasswordActivity.this)
							.resetPassword(mEtAccount.getText().toString(), 
									mEtPassword.getText().toString(), 
									mEtCode.getText().toString());
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
					Utils.showToast(ForgetPasswordActivity.this, R.string.reset_password_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			Utils.showToast(ForgetPasswordActivity.this, R.string.reset_password_success, Toast.LENGTH_SHORT);
			startActivity(new Intent(ForgetPasswordActivity.this, LoginActivity.class));
			finish();
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}

	private EditText mEtAccount;
	private EditText mEtPassword;
	private EditText mEtCode;
	private TextView mBtnGetCode;
	private ImageView mBackBtn;

	private TimeCount mTimer;
	private ResetPasswordTask mTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_forget_password);
		setTitleBar(null, null, null);
		initView();
	}

	private void initView(){
		mEtAccount = (EditText)findViewById(R.id.username);
		mEtPassword = (EditText)findViewById(R.id.password);
		mEtCode = (EditText)findViewById(R.id.code);
		mBtnGetCode = (TextView)findViewById(R.id.btn_get_code);
		
		mBtnGetCode.setOnClickListener(this);
		
		mBackBtn = (ImageView)findViewById(R.id.iv_back);
		mBackBtn.setOnClickListener(this);

		mTimer = new TimeCount(60000, 1000);
	}
	
	@Override
	protected void onPause() {
		dismissProgress();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
			mTask = null;
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnGetCode){
			getCode();
		}else if (v == mBackBtn){
			finish();
		}
		super.onClick(v);
	}
	
	private void getCode(){
		String account = mEtAccount.getText().toString();
		if (!Utils.isMobileNumber(account)){
			Utils.showToast(this, R.string.input_account_error, Toast.LENGTH_SHORT);
			return;
		}
		mTimer.start();
		SendCodeTask task = new SendCodeTask();
		try {
			task.execute();
		} catch (RejectedExecutionException e) {
		}
	}
	
	public void resetPassword(View view){
		String account = mEtAccount.getText().toString();
		String password = mEtPassword.getText().toString();
		String code = mEtCode.getText().toString();
		
		if (!Utils.isMobileNumber(account)){
			Utils.showToast(this, R.string.input_account_error, Toast.LENGTH_SHORT);
			return;
		}
		if (!Utils.checkCode(code)){
			Utils.showToast(this, R.string.input_code_error, Toast.LENGTH_SHORT);
			return;
		}
		if (!Utils.checkPassword(password)){
			Utils.showToast(this, R.string.input_password_error, Toast.LENGTH_SHORT);
			return;
		}
		mTask = new ResetPasswordTask();
		try {
			mTask.execute();
		} catch (RejectedExecutionException e) {
		}
	}
	
	@Override
	protected void handleTitleBarEvent(int eventId) {
		
	}
}
