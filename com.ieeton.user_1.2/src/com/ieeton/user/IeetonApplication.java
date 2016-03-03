/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ieeton.user;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.easemob.chat.ConnectionListener;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMMessage;
import com.easemob.chat.OnMessageNotifyListener;
import com.easemob.chat.OnNotificationClickListener;
import com.ieeton.user.activity.MainActivity;
import com.ieeton.user.db.DbOpenHelper;
import com.ieeton.user.db.UserDao;
import com.ieeton.user.domain.User;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.location.IeetonLocation;
import com.ieeton.user.models.City;
import com.ieeton.user.models.ServerHostData;
import com.ieeton.user.models.Settings;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.receiver.VoiceCallReceiver;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.NickNameCache;
import com.ieeton.user.utils.Utils;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SocializeClientListener;

public class IeetonApplication extends Application {

	public static Context applicationContext;
	private static IeetonApplication instance;
	// login user name
	public final String PREF_USERNAME = "username";
	private String userName = null;
	// login password
	private static final String PREF_PWD = "pwd";
	private String password = null;
	private Map<String, User> contactList;
	/**
	 * 当前用户nickname,为了苹果推送不是userid而是昵称
	 */
	public static String currentUserNick = "";
	
	public static IeetonLocation mIeetonLocation = null;
	public static ServerHostData mServerHostData = null;
	public static List<City> mCityList;


	@Override
	public void onCreate() {
		super.onCreate();
		Utils.saveIeetonFrom(this);
		
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        // 如果使用到百度地图或者类似启动remote service的第三方库，这个if判断不能少
        if (processAppName == null || processAppName.equals("")) {
            // workaround for baidu location sdk
            // 百度定位sdk，定位服务运行在一个单独的进程，每次定位服务启动的时候，都会调用application::onCreate
            // 创建新的进程。
            // 但环信的sdk只需要在主进程中初始化一次。 这个特殊处理是，如果从pid 找不到对应的processInfo
            // processName，
            // 则此application::onCreate 是被service 调用的，直接返回
            return;
        }

        //定位
       // BaiduLocationHelper.startRequestLocation(this, mIeetonLocationListener);
		SDKInitializer.initialize(this);
        
		applicationContext = this;
		instance = this;
		// 初始化环信SDK,一定要先调用init()
		EMChat.getInstance().init(applicationContext);
		EMChat.getInstance().setDebugMode(false);
//		Log.d("EMChat Demo", "initialize EMChat SDK");
		// debugmode设为true后，就能看到sdk打印的log了

		// 获取到EMChatOptions对象
		EMChatOptions options = EMChatManager.getInstance().getChatOptions();
		// 默认环信是不维护好友关系列表的，如果app依赖环信的好友关系，把这个属性设置为true
		options.setUseRoster(true);
		// 默认添加好友时，是不需要验证的，改成需要验证
		options.setAcceptInvitationAlways(false);
		// 设置收到消息是否有新消息通知，默认为true,永远提醒
		options.setNotifyBySoundAndVibrate(true);
		boolean[] onoff = Utils.getMessageNotifySetting(applicationContext);
		// 设置收到消息是否有声音提示，默认为true
		options.setNoticeBySound(onoff[0]);
		// 设置收到消息是否震动 默认为true
		options.setNoticedByVibrate(onoff[1]);
		// 设置语音消息播放是否设置为扬声器播放 默认为true
		Settings settings = Utils.getSettings(applicationContext);
		options.setUseSpeaker(settings.getViaLoundSpeaker());
		// 设置notification消息点击时，跳转的intent为自定义的intent
		options.setOnNotificationClickListener(new OnNotificationClickListener() {

			@Override
			public Intent onNotificationClick(EMMessage message) {
//				Intent intent = new Intent(applicationContext, ChatActivity.class);
//				ChatType chatType = message.getChatType();
//				if (chatType == ChatType.Chat) { // 单聊信息
//					intent.putExtra(ChatActivity.EXTRA_USERID, message.getFrom());
//					intent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
//				} else { // 群聊信息
//							// message.getTo()为群聊id
//					intent.putExtra("groupId", message.getTo());
//					intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
//				}
				Intent intent = new Intent(applicationContext, MainActivity.class);
				return intent;
			}
		});
		// 设置一个connectionlistener监听账户重复登陆
		EMChatManager.getInstance().addConnectionListener(new MyConnectionListener());
		// 取消注释，app在后台，有新消息来时，状态栏的消息提示换成自己写的
		options.setNotifyText(new OnMessageNotifyListener() {

			@Override
			public String onNewMessageNotify(EMMessage message) {
				// 可以根据message的类型提示不同文字(可参考微信或qq)，demo简单的覆盖了原来的提示
				String notify = "";
				String passport = message.getFrom();
				String nick = NickNameCache.getInstance().get(passport);
				if(nick != null && !"".equals(nick)){
					String formatStr = getResources().getString( R.string.new_incoming_messages );
					notify = nick + String.format( formatStr, 1);
				}else{
					notify =  getString(R.string.new_message);
				}
				return notify;
			}

			@Override
			public String onLatestMessageNotify(EMMessage message, int fromUsersNum, int messageNum) {	
				String formatStr = getResources().getString( R.string.new_messages );
				String notify = String.format( formatStr, fromUsersNum, messageNum);
				return notify;
			}

			@Override
			public String onSetNotificationTitle(EMMessage message) {
				//修改标题
				return getString(R.string.app_name);
			}

			@Override
			public int onSetSmallIcon(EMMessage arg0) {
				// TODO Auto-generated method stub
				return 0;
			}


		});
		
		//注册一个语言电话的广播接收者
		IntentFilter callFilter = new IntentFilter(EMChatManager.getInstance().getIncomingVoiceCallBroadcastAction());
		registerReceiver(new VoiceCallReceiver(), callFilter);
		
		new GetDomainUrlsTask().execute();
	}

	public static IeetonApplication getInstance() {
		return instance;
	}

	// List<String> list = new ArrayList<String>();
	// list.add("1406713081205");
	// options.setReceiveNotNoifyGroup(list);
	/**
	 * 获取内存中好友user list
	 *
	 * @return
	 */
	public Map<String, User> getContactList() {
		if (getUserName() != null && contactList == null) {
			UserDao dao = new UserDao(applicationContext);
			// 获取本地好友user list到内存,方便以后获取好友list
			contactList = dao.getContactList();
		}
		return contactList;
	}

	/**
	 * 设置好友user list到内存中
	 *
	 * @param contactList
	 */
	public void setContactList(Map<String, User> contactList) {
		this.contactList = contactList;
	}

	public void setStrangerList(Map<String, User> List) {

	}

	/**
	 * 获取当前登陆用户名
	 *
	 * @return
	 */
	public String getUserName() {
		if (userName == null) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
			userName = preferences.getString(PREF_USERNAME, null);
		}
		return userName;
	}

	/**
	 * 获取密码
	 *
	 * @return
	 */
	public String getPassword() {
		if (password == null) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
			password = preferences.getString(PREF_PWD, null);
		}
		return password;
	}

	/**
	 * 设置用户名
	 *
	 * @param user
	 */
	public void setUserName(String username) {
		if (username != null) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
			SharedPreferences.Editor editor = preferences.edit();
			if (editor.putString(PREF_USERNAME, username).commit()) {
				userName = username;
			}
		}
	}

	/**
	 * 设置密码 下面的实例代码 只是demo，实际的应用中需要加password 加密后存入 preference 环信sdk
	 * 内部的自动登录需要的密码，已经加密存储了
	 *
	 * @param pwd
	 */
	public void setPassword(String pwd) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
		SharedPreferences.Editor editor = preferences.edit();
		if (editor.putString(PREF_PWD, pwd).commit()) {
			password = pwd;
		}
	}

	/**
	 * 退出登录,清空数据
	 */
	public void logout() {
		// 先调用sdk logout，在清理app中自己的数据
		EMChatManager.getInstance().logout();
		DbOpenHelper.getInstance(applicationContext).closeDB();
		
		thirdLogOut();
		
		// reset password to null
		setPassword(null);
		setContactList(null);
		
//		Utils.setMyUid(applicationContext, null);
		Utils.setMyType(applicationContext, 5);
		Utils.saveSettings(applicationContext, null);
		Utils.setNeedBindMobile(applicationContext, false);

		Intent successIntent = new Intent(Constants.NEED_RELOGIN_ACTION);
		sendBroadcast(successIntent);
	}

	private void thirdLogOut(){
		String loginType = Utils.getLoginType(applicationContext);
		if (Constants.LOGIN_MOBILE.equals(loginType)){
			return;
		}
		SHARE_MEDIA platform = SHARE_MEDIA.QQ;
		if (Constants.LOGIN_QQ.equals(loginType)){
			platform = SHARE_MEDIA.QQ;
		}else if (Constants.LOGIN_WEIBO.equals(loginType)){
			platform = SHARE_MEDIA.SINA;
		}else if (Constants.LOGIN_WX.equals(loginType)){
			platform = SHARE_MEDIA.WEIXIN;
		}
		Utils.logd("platform:"+platform);
		UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.login");
		mController.deleteOauth(this, platform,
				 new SocializeClientListener() {

					@Override
					public void onComplete(int status, SocializeEntity entity) {
						if (status == 200) {
							Utils.logd("deleteOauth success");
						} else {
							Utils.logd("deleteOauth failed");
						}
					}

					@Override
					public void onStart() {
						Utils.logd("deleteOauth onStart");
					}});
	}

	private String getAppName(int pID) {
		String processName = null;
		ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		List l = am.getRunningAppProcesses();
		Iterator i = l.iterator();
		PackageManager pm = this.getPackageManager();
		while (i.hasNext()) {
			ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
			try {
				if (info.pid == pID) {
					CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
					// Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
					// info.processName +"  Label: "+c.toString());
					// processName = c.toString();
					processName = info.processName;
					return processName;
				}
			} catch (Exception e) {
				// Log.d("Process", "Error>> :"+ e.toString());
			}
		}
		return processName;
	}

	class MyConnectionListener implements ConnectionListener {
		@Override
		public void onReConnecting() {
		}

		@Override
		public void onReConnected() {
		}

		@Override
		public void onDisConnected(String errorString) {
			if (errorString != null && errorString.contains("conflict")) {
				try{
					Intent intent = new Intent(applicationContext, MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("conflict", true);
					startActivity(intent);
				}catch(SecurityException e){
					e.printStackTrace();
				}
			}

		}

		@Override
		public void onConnecting(String progress) {

		}

		@Override
		public void onConnected() {
		}
	}
	
	private class GetDomainUrlsTask extends AsyncTask<Void, Void, ServerHostData>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected ServerHostData doInBackground(Void... arg0) {
			String result = "";

			try {
				result = NetEngine.getInstance(getApplicationContext()).getDomainUrls();
			} catch (PediatricsIOException e) {
				e.printStackTrace();
				mThr = e;
				return null;
			} catch (PediatricsParseException e) {
				e.printStackTrace();
				mThr = e;
				return null;
			} catch (PediatricsApiException e) {
				e.printStackTrace();
				mThr = e;
				return null;
			}

			JSONObject object = null;
			try {
				object = new JSONObject(result);
				ServerHostData host = new ServerHostData(getApplicationContext(),object);
				
				return host;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(ServerHostData result) {
			if (result == null || result.equals("")){
				//IeetonApplication.mServerHostData = null;
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplicationContext());
				}else{
					Utils.showToast(getApplicationContext(), R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			IeetonApplication.mServerHostData = result;
		}
	}
}
