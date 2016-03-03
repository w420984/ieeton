package com.ieeton.user.activity;

import java.util.concurrent.RejectedExecutionException;

import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.CustomToast;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class BindingMobileActivity extends TemplateActivity implements OnClickListener{
	private LinearLayout mBtnBack;
	private Button mBindingBtn;
	private Button mAccessCodeBtn;
	private EditText mMobileEditText;
	private EditText mCodeEditText;
	private TimeCount mTimer;
	
	class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
		}
		@Override
		public void onFinish() {//计时完毕时触发
			mAccessCodeBtn.setText(R.string.re_get_code);
			mAccessCodeBtn.setClickable(true);
			//mAccessCodeBtn.setBackgroundResource(R.drawable.btn_access_code_selector);
		}
		@Override
		public void onTick(long millisUntilFinished){//计时过程显示
			mAccessCodeBtn.setClickable(false);
			mAccessCodeBtn.setText(millisUntilFinished /1000+" "+getString(R.string.second));
			//mAccessCodeBtn.setBackgroundResource(R.drawable.button_code_s);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_binding_mobile);
		setTitleBar(null, null, null);
				
		
		mBtnBack = (LinearLayout)findViewById(R.id.ll_back);
		mBtnBack.setOnClickListener(this);
		
		mMobileEditText = (EditText)findViewById(R.id.username);
		mCodeEditText = (EditText)findViewById(R.id.code);
		
		mAccessCodeBtn = (Button)findViewById(R.id.btn_get_code);
		mAccessCodeBtn.setOnClickListener(this);
		
		mBindingBtn = (Button)findViewById(R.id.binding_btn);
		mBindingBtn.setOnClickListener(this);
		
		mTimer = new TimeCount(60000, 1000);
	}

	@Override
	public void onClick(View v) {
		if(v == mBtnBack){
			finish();
		}else if(v == mAccessCodeBtn){
			getAccessCode();
		}else if(v == mBindingBtn){
			bindingMobile();
		}
		super.onClick(v);
	}
	
	public void bindingMobile(){
		String account = mMobileEditText.getText().toString();
		String code = mCodeEditText.getText().toString();
		
		if (!Utils.isMobileNumber(account) || account.length() != 11){
			Utils.showToast(this, R.string.input_account_error, Toast.LENGTH_SHORT);
			return;
		}
		
		if (!Utils.checkCode(code)){
			Utils.showToast(this, R.string.input_code_error, Toast.LENGTH_SHORT);
			return;
		}
		BindingMobileTask task = new BindingMobileTask();
		try {
			task.execute();
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
	public void getAccessCode(){
		String account = mMobileEditText.getText().toString();
		if (!Utils.isMobileNumber(account)){
			Utils.showToast(this, R.string.input_account_error, Toast.LENGTH_SHORT);
			return;
		}
		mTimer.start();
		
		GetAccessCodeTask task = new GetAccessCodeTask();
		try {
			task.execute();
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private class GetAccessCodeTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(BindingMobileActivity.this)
							.bindingMobileOperation(NetEngine.GET_ACCESS_CODE, mMobileEditText.getText().toString());
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
					Utils.showToast(BindingMobileActivity.this, R.string.get_code_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			Utils.showToast(BindingMobileActivity.this, R.string.get_code_success, Toast.LENGTH_SHORT);
		}
	}
	
	private class BindingMobileTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(BindingMobileActivity.this)
							.bindingMobileOperation(NetEngine.CHECK_MOBILE, mMobileEditText.getText().toString());
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
			if(mThr != null){
				return result;
			}
			
			result = "";
			try {
				result = NetEngine.getInstance(BindingMobileActivity.this)
							.bindingMobile("", mMobileEditText.getText().toString(), mCodeEditText.getText().toString());
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
					Utils.showToast(BindingMobileActivity.this, R.string.binding_mobile_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			Utils.showToast(BindingMobileActivity.this, R.string.binding_mobile_success, Toast.LENGTH_SHORT);
			Utils.setNeedBindMobile(BindingMobileActivity.this, false);
		}
	}
	
	@Override
	protected void handleTitleBarEvent(int eventId) {
		
	}
}
