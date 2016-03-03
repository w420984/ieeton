package com.ieeton.agency.activity;

import java.util.concurrent.RejectedExecutionException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.R;

public class BindMobileActivity extends TemplateActivity {
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
				result = NetEngine.getInstance(BindMobileActivity.this)
							.sendCode(mEtAccount.getText().toString());
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
					Utils.showToast(BindMobileActivity.this, R.string.get_code_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			Utils.showToast(BindMobileActivity.this, R.string.get_code_success, Toast.LENGTH_SHORT);
		}
		
	}
	
	private class BindMobileTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(BindMobileActivity.this)
							.bindMobile(mEtAccount.getText().toString(), 
									Utils.getPassport(BindMobileActivity.this), 
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
					Utils.showToast(BindMobileActivity.this, R.string.bind_mobile_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			Utils.setNeedBindMobile(BindMobileActivity.this, false);
			Utils.showToast(BindMobileActivity.this, R.string.bind_mobile_success, Toast.LENGTH_SHORT);
			if (!isChangeMobile){
				startActivity(new Intent(BindMobileActivity.this, PerfectInfoActivity.class));
			}
			finish();
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	public static String EXTRA_ISCHANGE_MOBILE = "is_change_mobile";

	private EditText mEtAccount;
	private EditText mEtCode;
	private Button mBtnGetCode;

	private TimeCount mTimer;
	private CustomToast mProgressDialog;
	private BindMobileTask mTask;
	private boolean isChangeMobile = false;		//判断是否是修改手机号的标志

	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			
			break;
		case LEFT_BUTTON:
			Utils.exitApp(this);
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.bind_mobile);
		setTitleBar(getString(R.string.back), getString(R.string.bind_mobile), null);
		initView();
		Intent intent = getIntent();
		if (intent != null){
			isChangeMobile = intent.getBooleanExtra(EXTRA_ISCHANGE_MOBILE, false);
		}
	}

	private void initView(){
		mEtAccount = (EditText)findViewById(R.id.username);
		mEtCode = (EditText)findViewById(R.id.code);
		mBtnGetCode = (Button)findViewById(R.id.btn_get_code);
		
		mBtnGetCode.setOnClickListener(this);

		mTimer = new TimeCount(60000, 1000);
	}
	
	@Override
	protected void onPause() {
		dismissProgress();
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			Utils.exitApp(this);
		}
		return super.onKeyDown(keyCode, event);
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
	
	public void bindMobile(View view){
		String account = mEtAccount.getText().toString();
		String code = mEtCode.getText().toString();
		
		if (!Utils.isMobileNumber(account)){
			Utils.showToast(this, R.string.input_account_error, Toast.LENGTH_SHORT);
			return;
		}
		if (!Utils.checkCode(code)){
			Utils.showToast(this, R.string.input_code_error, Toast.LENGTH_SHORT);
			return;
		}
		mTask = new BindMobileTask();
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
