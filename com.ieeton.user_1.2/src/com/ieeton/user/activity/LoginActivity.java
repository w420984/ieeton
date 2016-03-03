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
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends TemplateActivity implements OnClickListener{
	private class LoginTask extends AsyncTask<Boolean, Void, Boolean>{
		private Throwable mThr;
		
		private boolean huanxin_login_success = false;
		private boolean login_success = false;
		private boolean isLoginByMobile;
		IeetonUser user = null;

		@Override
		protected Boolean doInBackground(Boolean... params) {
			boolean result = false;
			String netResult = "";
			isLoginByMobile = params[0];
			
			try {
				if (isLoginByMobile){
					netResult = NetEngine.getInstance(LoginActivity.this)
								.login(mAccount, mPassWord);
				}else{
					netResult = NetEngine.getInstance(LoginActivity.this)
								.thirdPartLogin(mAccessToken, mOpenId, mAppId, mPlatform);
				}
			} catch (PediatricsIOException e) {
				netResult = null;
				mThr = e;
				e.printStackTrace();
			} catch (PediatricsParseException e) {
				netResult = null;
				mThr = e;
				e.printStackTrace();
			} catch (PediatricsApiException e) {
				netResult = null;
				mThr = e;
				e.printStackTrace();
			}
			if (TextUtils.isEmpty(netResult)){
				return result;
			}
			JSONObject obj;
			try {
				obj = new JSONObject(netResult);
				user = new IeetonUser(LoginActivity.this, obj);
//				if(user.getUserType() == 1 || user.getUserType() == 2){
//					ErrorMessage em = new ErrorMessage();
//					em.errmsg = getString(R.string.login_type_error);
//					em.errorcode = "login_type_error";
//					mThr = new PediatricsApiException(em);
//					return false;
//				}
				
				Utils.setMyUid(LoginActivity.this, user.getUid());
				Utils.setMyType(LoginActivity.this, user.getUserType());
				//Utils.setLoginType(LoginActivity.this, session.getType());
		        Utils.addUMengAlias(getApplicationContext(), Utils.getMyUid(getApplicationContext()));
			} catch (JSONException e) {
				e.printStackTrace();
				return result;
			}
			
			result = true;
			//环信登录

			final String username = user.getHxName();
			final String password = user.getHxPassword();
			IeetonApplication.currentUserNick = username;
			Utils.logd("username:"+username);
			Utils.logd("passowrd:"+password);
			if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
				EMChatManager.getInstance().login(username, password, new EMCallBack() {

					@Override
					public void onSuccess() {
						dismissProgress();
						Utils.logd("huanxin login success");
						IeetonApplication.getInstance().setUserName(username);
						IeetonApplication.getInstance().setPassword(password);
						runOnUiThread(new Runnable() {
							public void run() {
							}
						});
						huanxin_login_success = true;
						boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(IeetonApplication.currentUserNick);
						if (!updatenick) {
							EMLog.e("LoginActivity", "update current user nick fail");
						}
						if (login_success){
							Intent intent = new Intent(LoginActivity.this, MainActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							if (!isLoginByMobile && user.getAge() == 0){
								new WelcomeTask().execute();
							}
							Intent successIntent = new Intent(Constants.LOGIN_ACTION);
							sendBroadcast(successIntent);
							finish();
						}
					}

					@Override
					public void onProgress(int progress, String status) {

					}

					@Override
					public void onError(int code, final String message) {
						dismissProgress();
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
			if (!result){
				dismissProgress();
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			login_success = true;
			if (huanxin_login_success){
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				if (!isLoginByMobile && user.getAge() == 0){
					new WelcomeTask().execute();
				}
				Intent successIntent = new Intent(Constants.LOGIN_ACTION);
				sendBroadcast(successIntent);
	
				finish();
			}
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
				NetEngine.getInstance(LoginActivity.this).requestWelcome();
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
	
	private ImageView mBtnWeibo;
	private ImageView mBtnWechat;
	private ImageView mBtnQQ;
	private TextView mTvForgetPassword;
	private TextView mTvRegister;
	private ImageView mBackBtn;
	
	private String mAccount;
	private String mPassWord;
	private LoginTask mTask;
	private boolean mIsLogout;
	
	//友盟三方登录
	private UMSocialService mController = null; 
	private UMQQSsoHandler qqSsoHandler = null;
	private UMWXHandler wxHandler = null;
	private String mAccessToken;
	private String mOpenId;
	private String mAppId;
	private SHARE_MEDIA mPlatform;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_login);
		setTitleBar(null, null, null);
		mIsLogout = getIntent().getBooleanExtra("isLogout", false);
		initView();
	}

	private void initView(){
		mBtnWeibo = (ImageView)findViewById(R.id.iv_weibo);
		mBtnWechat = (ImageView)findViewById(R.id.iv_wechat);
		mBtnQQ = (ImageView)findViewById(R.id.iv_qq);
		mTvForgetPassword = (TextView)findViewById(R.id.tv_forget_password);
		mTvRegister = (TextView) findViewById(R.id.register);
		
		mBtnWeibo.setOnClickListener(this);
		mBtnWechat.setOnClickListener(this);
		mBtnQQ.setOnClickListener(this);
		mTvForgetPassword.setOnClickListener(this);
		mTvRegister.setOnClickListener(this);
		
		mBackBtn = (ImageView)findViewById(R.id.iv_back);
		mBackBtn.setOnClickListener(this);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void login(View view){
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
		imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.username)).getWindowToken(), 0);
		imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.password)).getWindowToken(), 0);
		
		mAccount = ((EditText)findViewById(R.id.username)).getText().toString();
		mPassWord = ((EditText)findViewById(R.id.password)).getText().toString();
		
		if ("ieetontest".equals(mAccount) && "*#ieeton#*".equals(mPassWord)){
			String from = Utils.getIeetonFrom(this);
			Utils.showToast(this, from, Toast.LENGTH_SHORT);
			return;
		}
		
//		if(mAccount.length() != 11){
//			Utils.showToast(this, R.string.input_account_error, Toast.LENGTH_SHORT);
//			return;
//		}
//		if (!Utils.isMobileNumber(mAccount) || !Utils.checkPassword(mPassWord)){
//			Utils.showToast(this, R.string.input_error, Toast.LENGTH_SHORT);
//			return;
//		}
		mTask = new LoginTask();
		try {
			mTask.execute(true);
		} catch (RejectedExecutionException e) {
		}
	}
	
	public void register(){
		startActivityForResult(new Intent(this, RegisterActivity.class), 1);
	}
	
	public void forgetPassword(){
		startActivityForResult(new Intent(this, ForgetPasswordActivity.class), 2);
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnWeibo){
			ssoLogin(SHARE_MEDIA.SINA);
		}else if (v == mBtnWechat){
			ssoLogin(SHARE_MEDIA.WEIXIN);
		}else if (v == mBtnQQ){
			ssoLogin(SHARE_MEDIA.QQ);
		}else if (v == mTvForgetPassword){
			forgetPassword();
		}else if (v == mTvRegister){
			register();
		}else if (v == mBackBtn){
			if (mIsLogout){
				Utils.exitApp(this);
			}else{
				finish();
			}
		}
		super.onClick(v);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			if (mIsLogout){
				Utils.exitApp(this);
			}else{
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	private void ssoLogin(SHARE_MEDIA platform){
		if (mController == null){
			mController = UMServiceFactory.getUMSocialService("com.umeng.login");
		}
		if (qqSsoHandler == null){
			qqSsoHandler = new UMQQSsoHandler(this, Utils.getQQAppID(this),
	                Utils.getQQAppkey(this));
			qqSsoHandler.addToSocialSDK();
		}
		// 添加微信平台
		if (wxHandler == null){
			wxHandler = new UMWXHandler(this,Utils.getWeixinAppkey(this),Utils.getWeixinAppSecret(this));
			wxHandler.addToSocialSDK();
			wxHandler.setRefreshTokenAvailable(false);
		}
		
		mController.doOauthVerify(this, platform, new UMAuthListener() {
            @Override
            public void onError(SocializeException e, SHARE_MEDIA platform) {
            	Utils.logd("doOauthVerify onError");
            	e.printStackTrace();
            }
            @Override
            public void onComplete(Bundle value, SHARE_MEDIA platform) {
            	Utils.logd("doOauthVerify onComplete");
                if (value != null && !TextUtils.isEmpty(value.getString("uid"))) {
                	Utils.logd("value:"+value.toString());
                	Utils.showToast(LoginActivity.this, R.string.fech_token_success, 
                			Toast.LENGTH_SHORT);
                	mOpenId = value.getString("uid");
        			mPlatform = platform;
                	if (platform == SHARE_MEDIA.QQ){
            			mAccessToken = value.getString("access_token");
                		mAppId = Utils.getQQAppID(LoginActivity.this);
            		}else if (platform == SHARE_MEDIA.WEIXIN){
            			mAccessToken = value.getString("access_token");
            			mAppId = Utils.getWeixinAppkey(LoginActivity.this);
            		}else if (platform == SHARE_MEDIA.SINA){
            			mAccessToken = value.getString("access_key");
            			mAppId = Utils.getWeiboAppkey(LoginActivity.this);
            		}
            		mTask = new LoginTask();
            		try {
            			mTask.execute(false);
            		} catch (RejectedExecutionException e) {
            		}
                } else {
                	Utils.showToast(LoginActivity.this, R.string.fech_token_failed, 
                			Toast.LENGTH_SHORT);
                }
            }
            @Override
            public void onCancel(SHARE_MEDIA platform) {
            	Utils.logd("doOauthVerify onCancel");
            }
            @Override
            public void onStart(SHARE_MEDIA platform) {
            	Utils.logd("doOauthVerify onStart");
            }
		});
		
	}

	@Override
	protected void handleTitleBarEvent(int eventId) {
		
	}
}