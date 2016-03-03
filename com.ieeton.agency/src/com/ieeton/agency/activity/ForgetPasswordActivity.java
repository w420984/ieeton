package com.ieeton.agency.activity;

import java.util.concurrent.RejectedExecutionException;

import org.json.JSONObject;

import com.ieeton.agency.activity.RegisterActivity.TimeCount;
import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.Account;
import com.ieeton.agency.models.Session;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.R;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ForgetPasswordActivity extends TemplateActivity {
	class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
		}
		@Override
		public void onFinish() {//计时完毕时触发
			mBtnGetCode.setText(R.string.re_get_code);
			mBtnGetCode.setClickable(true);
		}
		@Override
		public void onTick(long millisUntilFinished){//计时过程显示
			mBtnGetCode.setClickable(false);
			mBtnGetCode.setText(millisUntilFinished /1000+"S");
		}
	}

	private class SendCodeTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(ForgetPasswordActivity.this)
							.forgetPassword(mEtAccount.getText().toString());
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
	private ImageView mBtnEye;
	private Button mBtnGetCode;

	private boolean isShowPassword = false;
	private TimeCount mTimer;
	private CustomToast mProgressDialog;
	private ResetPasswordTask mTask;

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
		setView(R.layout.activity_forget_password);
		setTitleBar(getString(R.string.back), getString(R.string.forget_password), null);
		initView();
	}

	private void initView(){
		mEtAccount = (EditText)findViewById(R.id.username);
		mEtPassword = (EditText)findViewById(R.id.password);
		mEtCode = (EditText)findViewById(R.id.code);
		mBtnEye = (ImageView)findViewById(R.id.iv_eye);
		mBtnGetCode = (Button)findViewById(R.id.btn_get_code);
		
		mBtnEye.setOnClickListener(this);
		mBtnGetCode.setOnClickListener(this);

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
		if (v == mBtnEye){
			setShowPassword();
		}else if (v == mBtnGetCode){
			getCode();
		}
		super.onClick(v);
	}

	private void setShowPassword(){
		isShowPassword = !isShowPassword;
		if (isShowPassword){
			mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
			mBtnEye.setImageResource(R.drawable.icon_passwordbutton_s);
		}else{
			mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance()); 
			mBtnEye.setImageResource(R.drawable.icon_passwordbutton_n);
		}
		mEtPassword.postInvalidate();
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
