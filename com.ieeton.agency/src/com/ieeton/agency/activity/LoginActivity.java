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
import com.ieeton.agency.models.ErrorMessage;
import com.ieeton.agency.models.Session;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.R;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends TemplateActivity{
	private class LoginTask extends AsyncTask<Boolean, Void, Boolean>{
		private Throwable mThr;
		private boolean needAddInfo = false;
		private boolean needBindMobile = false;
		
		private boolean huanxin_login_success = false;
		private boolean login_success = false;

		@Override
		protected Boolean doInBackground(Boolean... params) {
			boolean result = false;
			String netResult = "";
			try {
				if (params[0]){
					netResult = NetEngine.getInstance(LoginActivity.this)
								.login(mAccount, mPassWord);
				}else{
					netResult = NetEngine.getInstance(LoginActivity.this)
								.thirdPartLogin(mAccessToken, mOpenId, mAppId, mPlatform);
				}
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
				
				String userType = userObj.optString("type");
				if (!"doctor".equals(userType)){
					ErrorMessage em = new ErrorMessage();
					em.errmsg = getString(R.string.login_type_error);
					em.errorcode = "login_type_error";
					mThr = new PediatricsApiException(em);
					return false;
				}
				session = new Session(sessionObj);
				account = new Account(userObj);
				Utils.setPassport(LoginActivity.this, session.getId());
				Utils.setMyUid(LoginActivity.this, account.getUserId());
				Utils.setMyLoginName(LoginActivity.this, mAccount);
				DemoApplication.mLoginType = session.getType();
			} catch (JSONException e) {
				e.printStackTrace();
				return result;
			}
			
			//通过获取个人信息得到环信用户名和密码
			try {
				netResult = NetEngine.getInstance(LoginActivity.this)
								.getDoctorInfo(Utils.getPassport(LoginActivity.this), 
												Utils.getMyUid(LoginActivity.this));
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
			//如果获取到的医生昵称为空，则需要完善资料
			if (TextUtils.isEmpty(account.getName())){
				needAddInfo = true;
				Utils.setNeedPerfectInfo(LoginActivity.this, true);
			}else{
				Utils.setNeedPerfectInfo(LoginActivity.this, false);
			}
			//如果手机号为空，则需要绑定手机
			if (TextUtils.isEmpty(account.getMobile())){
				needBindMobile = true;
				Utils.setNeedBindMobile(LoginActivity.this, true);
			}else{
				Utils.setNeedBindMobile(LoginActivity.this, false);
			}
			result = true;
			//环信登录

			final String username = account.getUserId();
			final String password = huanxinPass;
			com.ieeton.agency.DemoApplication.currentUserNick = username;
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
						huanxin_login_success = true;
						boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(DemoApplication.currentUserNick);
						if (!updatenick) {
							EMLog.e("LoginActivity", "update current user nick fail");
						}
						if (login_success){
							dismissProgress();
							if (needBindMobile){
								startActivity(new Intent(LoginActivity.this, BindMobileActivity.class));
							}else if (needAddInfo){
								startActivity(new Intent(LoginActivity.this, PerfectInfoActivity.class));
							}else{
								Intent intent = new Intent(LoginActivity.this, MainActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
							}
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
				dismissProgress();
				if (needAddInfo){
					startActivity(new Intent(LoginActivity.this, PerfectInfoActivity.class));
				}else{
					Intent intent = new Intent(LoginActivity.this, MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
				finish();
			}
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	private ImageView mBtnWeibo;
	private ImageView mBtnWechat;
	private ImageView mBtnQQ;
	private TextView mTvForgetPassword;
	
	private String mAccount;
	private String mPassWord;
	private CustomToast mProgressDialog;
	private LoginTask mTask;

	private UMSocialService mController = null; 
	private UMQQSsoHandler qqSsoHandler = null;
	private UMWXHandler wxHandler = null;
	
	private String mAccessToken;
	private String mOpenId;
	private String mAppId;
	private SHARE_MEDIA mPlatform;
	
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			Utils.exitApp(this);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_login);
		setTitleBar(getString(R.string.back), getString(R.string.login), null);
		initView();
	}

	private void initView(){
		mBtnWeibo = (ImageView)findViewById(R.id.iv_weibo);
		mBtnWechat = (ImageView)findViewById(R.id.iv_wechat);
		mBtnQQ = (ImageView)findViewById(R.id.iv_qq);
		mTvForgetPassword = (TextView)findViewById(R.id.tv_forget_password);
		
		mBtnWeibo.setOnClickListener(this);
		mBtnWechat.setOnClickListener(this);
		mBtnQQ.setOnClickListener(this);
		mTvForgetPassword.setOnClickListener(this);
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
		mAccount = ((EditText)findViewById(R.id.username)).getText().toString();
		mPassWord = ((EditText)findViewById(R.id.password)).getText().toString();

		if ("ieetontest".equals(mAccount) && "*#ieeton#*".equals(mPassWord)){
			String from = Utils.getIeetonFrom(this);
			Utils.showToast(this, from, Toast.LENGTH_SHORT);
			return;
		}
		
		if (!Utils.isMobileNumber(mAccount) || !Utils.checkPassword(mPassWord)){
			Utils.showToast(this, R.string.input_error, Toast.LENGTH_SHORT);
			return;
		}
		mTask = new LoginTask();
		try {
			mTask.execute(true);
		} catch (RejectedExecutionException e) {
		}
	}
	
	public void register(View view){
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
		}
		super.onClick(v);
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
                	
//授权接口已经可以获取到token和uid，可以不需要再获取用户信息了
//                    mController.getPlatformInfo(LoginActivity.this, platform, new UMDataListener(){
//
//            			@Override
//            			public void onComplete(int status, Map<String, Object> info) {
//                        	Utils.logd("getPlatformInfo onComplete");
//            				dismissProgress();
//            				if(status == 200 && info != null){
//            	                StringBuilder sb = new StringBuilder();
//            	                Set<String> keys = info.keySet();
//            	                for(String key : keys){
//            	                   sb.append(key+"="+info.get(key).toString()+"\r\n");
//            	                }
//            	                Utils.logd("TestData:"+sb.toString());
//            	            }else{
//            	            	Utils.logd("第三方登录发生错误："+status);
//            	           }
//            			}
//
//            			@Override
//            			public void onStart() {
//                        	Utils.logd("getPlatformInfo onStart");
//            				showProgress();
//            			}});
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
}