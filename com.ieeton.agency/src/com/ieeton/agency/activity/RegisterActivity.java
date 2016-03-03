package com.ieeton.agency.activity;

import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.util.EMLog;
import com.ieeton.agency.DemoApplication;
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


/**
 * 注册页
 * 
 */
public class RegisterActivity extends TemplateActivity {
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
				result = NetEngine.getInstance(RegisterActivity.this)
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
					Utils.showToast(RegisterActivity.this, R.string.get_code_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			Utils.showToast(RegisterActivity.this, R.string.get_code_success, Toast.LENGTH_SHORT);
		}
		
	}
	
	private class RegisterTask extends AsyncTask<Void, Void, Boolean>{
		private Throwable mThr;

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = false;
			String netResult = "";
			try {
				netResult = NetEngine.getInstance(RegisterActivity.this)
							.register(mEtAccount.getText().toString(), 
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
			
			JSONObject obj;
			Session session = null;
			Account account = null;
			String huanxinPass = "";
			try {
				obj = new JSONObject(netResult);
				JSONObject data = obj.optJSONObject("messages")
						.optJSONObject("data");
				JSONObject sessionObj = data.optJSONObject("session");
				JSONObject userObj = data.optJSONObject("user");
				
				session = new Session(sessionObj);
				account = new Account(userObj);
				Utils.setPassport(RegisterActivity.this, session.getId());
				Utils.setMyUid(RegisterActivity.this, account.getUserId());
				Utils.setMyLoginName(RegisterActivity.this, mEtAccount.getText().toString());
			} catch (JSONException e) {
				e.printStackTrace();
				return result;
			}
			//通过获取个人信息得到环信用户名和密码
			try {
				netResult = NetEngine.getInstance(RegisterActivity.this)
								.getDoctorInfo(Utils.getPassport(RegisterActivity.this), 
												Utils.getMyUid(RegisterActivity.this));
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
			if (TextUtils.isEmpty(netResult)){
				return result;
			}
			
			try {
				obj = new JSONObject(netResult);
				JSONObject data = obj.optJSONObject("messages")
						.optJSONObject("data");
				huanxinPass = data.optJSONObject("doctor").optString("huanxinPass");
			} catch (JSONException e) {
				e.printStackTrace();
				return result;
			}
			result = true;
			//环信登录

			final String password = huanxinPass;
			final String username = account.getUserId();//mAccount;
			com.ieeton.agency.DemoApplication.currentUserNick = username;
			Utils.logd("username:"+username);
			Utils.logd("passowrd:"+password);
			if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
				EMChatManager.getInstance().login(username, password, new EMCallBack() {

					@Override
					public void onSuccess() {
						Utils.logd("huanxin login success");
						DemoApplication.getInstance().setUserName(username);
						DemoApplication.getInstance().setPassword(password);
						runOnUiThread(new Runnable() {
							public void run() {
							}
						});
						
						boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(DemoApplication.currentUserNick);
						if (!updatenick) {
							EMLog.e("LoginActivity", "update current user nick fail");
						}

					}

					@Override
					public void onProgress(int progress, String status) {

					}

					@Override
					public void onError(int code, final String message) {
						Utils.logd("huanxin login onError:"+message);
//						if (!progressShow) {
//							return;
//						}
//						runOnUiThread(new Runnable() {
//							public void run() {
//								pd.dismiss();
//								Toast.makeText(getApplicationContext(), "鐧诲綍澶辫触: " + message, 0).show();
//
//							}
//						});
					}
				});
			}
			return result;
		}

		@Override
		protected void onCancelled() {
			dismissProgress();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			dismissProgress();
			if (!result){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(RegisterActivity.this, R.string.register_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			Utils.showToast(RegisterActivity.this, R.string.register_success, Toast.LENGTH_SHORT);
			Utils.setNeedPerfectInfo(RegisterActivity.this, true);
			startActivity(new Intent(RegisterActivity.this, PerfectInfoActivity.class));
			finish();
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	private ImageView mBtnEye;
	private EditText mEtAccount;
	private EditText mEtPassword;
	private EditText mEtCode;
	private Button mBtnGetCode;

	private boolean isShowPassword = false;
	private TimeCount mTimer;
	private CustomToast mProgressDialog;
	private RegisterTask mTask;
	
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
		setView(R.layout.activity_register);
		setTitleBar(getString(R.string.back), getString(R.string.register), null);
		initView();
	}

	private void initView(){
		mBtnEye = (ImageView)findViewById(R.id.iv_eye);
		mEtAccount = (EditText)findViewById(R.id.username);
		mEtPassword = (EditText)findViewById(R.id.password);
		mEtCode = (EditText)findViewById(R.id.code);
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
	
	public void register(View view){
		String account = mEtAccount.getText().toString();
		String password = mEtPassword.getText().toString();
		String code = mEtCode.getText().toString();
		
		if (!Utils.isMobileNumber(account)){
			Utils.showToast(this, R.string.input_account_error, Toast.LENGTH_SHORT);
			return;
		}
		if (!Utils.checkPassword(password)){
			Utils.showToast(this, R.string.input_password_error, Toast.LENGTH_SHORT);
			return;
		}
		if (!Utils.checkCode(code)){
			Utils.showToast(this, R.string.input_code_error, Toast.LENGTH_SHORT);
			return;
		}
		
		mTask = new RegisterTask();
		try {
			mTask.execute();
		} catch (RejectedExecutionException e) {
		}
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
