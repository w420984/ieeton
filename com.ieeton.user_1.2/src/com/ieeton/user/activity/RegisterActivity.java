package com.ieeton.user.activity;

import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.util.EMLog;
import com.ieeton.user.IeetonApplication;
import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.ErrorMessage;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * 注册页
 * 
 */
public class RegisterActivity extends TemplateActivity implements OnClickListener {
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
			mBtnGetCode.setText(millisUntilFinished /1000+" "+getString(R.string.second));
			//mBtnGetCode.setBackgroundResource(R.drawable.button_code_s);
		}
	}
	
	private class SendCodeTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(RegisterActivity.this)
							.sendCode(mEtAccount.getText().toString(), "register");
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
									mEtAccount.getText().toString(),
									mEtCode.getText().toString());
			} catch (PediatricsIOException e) {
				mThr = e;
				e.printStackTrace();
				return result;
			} catch (PediatricsParseException e) {
				mThr = e;
				e.printStackTrace();
				return result;
			} catch (PediatricsApiException e) {
				mThr = e;
				e.printStackTrace();
				return result;
			}
			
			JSONObject obj;
			IeetonUser user = null;
			try {
				obj = new JSONObject(netResult);
				user = new IeetonUser(RegisterActivity.this, obj);
				if(user.getUserType() == 1 || user.getUserType() == 2){
					ErrorMessage em = new ErrorMessage();
					em.errmsg = getString(R.string.login_type_error);
					em.errorcode = "login_type_error";
					mThr = new PediatricsApiException(em);
					return false;
				}
				
				Utils.setMyUid(RegisterActivity.this, user.getUid());
				Utils.setMyType(RegisterActivity.this, user.getUserType());
				//Utils.setLoginType(RegisterActivity.this, session.getType());
		        Utils.addUMengAlias(getApplicationContext(), Utils.getMyUid(getApplicationContext()));
			} catch (JSONException e) {
				e.printStackTrace();
				return result;
			}
			
			result = true;
			//环信登录

			final String password = user.getHxName();
			final String username = user.getHxPassword();//mAccount;
			com.ieeton.user.IeetonApplication.currentUserNick = username;
			Utils.logd("username:"+username);
			Utils.logd("passowrd:"+password);
			if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
				EMChatManager.getInstance().login(username, password, new EMCallBack() {

					@Override
					public void onSuccess() {
						Utils.logd("huanxin login success");
						IeetonApplication.getInstance().setUserName(username);
						IeetonApplication.getInstance().setPassword(password);
						runOnUiThread(new Runnable() {
							public void run() {
							}
						});
						
						boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(IeetonApplication.currentUserNick);
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
			startActivity(new Intent(RegisterActivity.this, MainActivity.class));
			Intent successIntent = new Intent(Constants.LOGIN_ACTION);
			sendBroadcast(successIntent);
			new WelcomeTask().execute();
			finish();
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	private class WelcomeTask extends AsyncTask<Void, Void, String>{

		@Override
		protected String doInBackground(Void... params) {
			try {
				NetEngine.getInstance(RegisterActivity.this).requestWelcome();
			} catch (PediatricsIOException e) {
				e.printStackTrace();
			} catch (PediatricsParseException e) {
				e.printStackTrace();
			} catch (PediatricsApiException e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	private EditText mEtAccount;
	private EditText mEtPassword;
	private EditText mEtCode;
	private TextView mBtnGetCode;
	private TextView mAgreement;
	private Button mRegist;
	private ImageView mBackBtn;

	private TimeCount mTimer;
	private RegisterTask mTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_register);
		setTitleBar(null, null, null);
		initView();
	}

	private void initView(){
		mEtAccount = (EditText)findViewById(R.id.username);
		mEtPassword = (EditText)findViewById(R.id.password);
		mEtCode = (EditText)findViewById(R.id.code);
		mBtnGetCode = (TextView)findViewById(R.id.btn_get_code);
		
		mBtnGetCode.setOnClickListener(this);
		
		mAgreement = (TextView)findViewById(R.id.tv_agreement);
		mAgreement.setOnClickListener(this);
		
		mRegist = (Button)findViewById(R.id.regist_btn);
		
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
	
	public void register(View view){
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
		imm.hideSoftInputFromWindow(mEtAccount.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(mEtPassword.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(mEtCode.getWindowToken(), 0);

		String account = mEtAccount.getText().toString();
		String nick = mEtAccount.getText().toString();
		String password = mEtPassword.getText().toString();
		String code = mEtCode.getText().toString();
		
		if (!Utils.isMobileNumber(account) || account.length() != 11){
			Utils.showToast(this, R.string.input_account_error, Toast.LENGTH_SHORT);
			return;
		}
		
		if(Utils.checkSpecialCharacters(nick)){
			Utils.showToast(this, R.string.nick_containt_special_character, Toast.LENGTH_SHORT);
			return;
		}
		if(Utils.calculateLength(nick) < Constants.MIN_NICKNAME_LENGTH || Utils.calculateLength(nick) > Constants.MAX_NICKNAME_LENGTH){
			Toast.makeText(RegisterActivity.this, R.string.nick_hint, Toast.LENGTH_SHORT).show();
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
		if (v == mBtnGetCode){
			getCode();
		}else if(v == mAgreement){
			startActivity(new Intent(this, AgreementActivity.class));
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
	
	@Override
	protected void handleTitleBarEvent(int eventId) {
		
	}
}
