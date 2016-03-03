package com.ieeton.agency.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.google.gson.Gson;
import com.ieeton.agency.adapter.ShareChatHistoryAdapter;
import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.ChatUser;
import com.ieeton.agency.models.Patient;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.SocialShareUtils;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.R;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.sso.UMSsoHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

public class ShareChatHistory extends TemplateActivity implements OnClickListener{
	private Button mShareButton;
	private ChatUser mUser;
	private String mToChatUID;
	private EMConversation conversation;
	private ShareChatHistoryAdapter adapter;
	private ListView listView;
	private ProgressBar loadmorePB;
	private boolean isloading;
	private final int pagesize = 20;
	private boolean haveMoreData = true;
	private List<EMMessage> mEMMessageList;
	private boolean IsTaskFree = true;
	private String mContent;
	private CustomToast mProgressDialog;
	private GenShareUrlTask mTask;

	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			adapter.setCheckedItemsAllOn();
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_share_chat_history);
		setTitleBar(getString(R.string.back), getString(R.string.select_share_content), 
				getString(R.string.select_all));
		
		Intent intent = getIntent();
		if(intent == null){
			finish();
			return;
		}
		mUser = (ChatUser) intent.getExtras().getSerializable(ChatActivity.EXTRA_USERINFO);
		mToChatUID = intent.getStringExtra(ChatActivity.EXTRA_USERID);
						
		mShareButton = (Button)findViewById(R.id.share_button);
		mShareButton.setOnClickListener(this);
		
		listView = (ListView) findViewById(R.id.listview);
		loadmorePB = (ProgressBar) findViewById(R.id.pb_load_more);
		
		conversation = EMChatManager.getInstance().getConversation(mToChatUID);
		// 把此会话的未读数置为0
		conversation.resetUnsetMsgCount();
		adapter = new ShareChatHistoryAdapter(this, mToChatUID, ChatActivity.CHATTYPE_SINGLE, mUser.getId());
		// 显示消息
		listView.setAdapter(adapter);
		listView.setOnScrollListener(new ListScrollListener());
		int count = listView.getCount();
		if (count > 0) {
			listView.setSelection(count - 1);
		}

		listView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//todo
				return false;
			}
		});
	}

	/**
	 * listview滑动监听listener
	 * 
	 */
	private class ListScrollListener implements OnScrollListener {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE:
				if (view.getFirstVisiblePosition() == 0 && !isloading && haveMoreData) {
					loadmorePB.setVisibility(View.VISIBLE);
					// sdk初始化加载的聊天记录为20条，到顶时去db里获取更多
					List<EMMessage> messages;
					try {
						// 获取更多messges，调用此方法的时候从db获取的messages
						// sdk会自动存入到此conversation中
						messages = conversation.loadMoreMsgFromDB(adapter.getItem(0).getMsgId(), pagesize);
					} catch (Exception e1) {
						loadmorePB.setVisibility(View.GONE);
						return;
					}
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
					}
					if (messages.size() != 0) {
						// 刷新ui
						adapter.notifyDataSetChanged();
						listView.setSelection(messages.size() - 1);
						if (messages.size() != pagesize)
							haveMoreData = false;
					} else {
						haveMoreData = false;
					}
					loadmorePB.setVisibility(View.GONE);
					isloading = false;

				}
				break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

		}

	}

	@Override
	public void onClick(View v) {
		if(v == mShareButton){
			int[] checkitems = adapter.getCheckedItems();
			mEMMessageList = new ArrayList<EMMessage>();
			for(int i = 0; i < checkitems.length; i++){
				if(checkitems[i] == 1){
					EMMessage msg = conversation.getMessage(i);
					mEMMessageList.add(msg);
				}
			}
			if(mEMMessageList.size() <= 0){
				Toast.makeText(ShareChatHistory.this, getString(R.string.no_checked_items), Toast.LENGTH_SHORT).show();
				return;
			}
			
			Gson gson = new Gson();
			mContent = gson.toJson(mEMMessageList);
			Utils.logd("share,content1:"+mContent);
			
			if(IsTaskFree){
				try{
					mTask = new GenShareUrlTask();
					mTask.execute();
				}catch(RejectedExecutionException e){
					e.printStackTrace();
				}
			}			
			
		}
		super.onClick(v);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data); /**使用SSO授权必须添加如下代码 */
		/**使用SSO授权必须添加如下代码 */
		UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
	    UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode) ;
	    if(ssoHandler != null){
	       ssoHandler.authorizeCallBack(requestCode, resultCode, data);
	    }
	}
	
	class GenShareUrlTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			IsTaskFree = false;
			showProgress();
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String result = "";
			
			//获取我的信息
			try {
				result = NetEngine.getInstance(ShareChatHistory.this)
								.createShare(Utils.getPassport(ShareChatHistory.this), mContent);
			} catch (PediatricsIOException e) {
				e.printStackTrace();
				mThr = e;
			} catch (PediatricsParseException e) {
				e.printStackTrace();
				mThr = e;
			} catch (PediatricsApiException e) {
				e.printStackTrace();
				mThr = e;
			}

			try {
				JSONObject object = new JSONObject(result);
				if(!object.getBoolean("error")){
					result = object.getJSONObject("messages").getJSONObject("data").optString("shareId");
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}


			return result;
		}

		@Override
		protected void onCancelled() {
			IsTaskFree = true;
			super.onCancelled();
			dismissProgress();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			IsTaskFree = true;
			if (result == null || result.equals("")){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, ShareChatHistory.this);
				}else{
					Utils.showToast(ShareChatHistory.this, R.string.create_share_url_failed, Toast.LENGTH_SHORT);
				}

				return;
			}

			String shareId = result;
			
			String from_type = "";	//左侧用户的类型
			if(mToChatUID.equals(NetEngine.getFeedbackId())){
				from_type = "p";
			}else{
				from_type = "u";
			}
			String to_type = "d";	//右侧用户的类型
			
			StringBuilder url = new StringBuilder(Constants.SERVER_HOST_SHARE_SERVER);
			url.append("?s=" + shareId + "&l=" + from_type + "&r=" + to_type);
			Utils.logd("url:"+url.toString());
			SocialShareUtils.shareToWX(ShareChatHistory.this, getString(R.string.share_title), getString(R.string.share_content), 
						null, R.drawable.share_icon, url.toString());
		}
		
	}	
	
	private void showProgress(){
		if (mProgressDialog == null){
			mProgressDialog = Utils.createProgressCustomToast(R.string.create_share_url, ShareChatHistory.this);
		}
		mProgressDialog.show();
	}
	
	private void dismissProgress(){
		if (mProgressDialog != null){
			mProgressDialog.cancel();
		}
	}
}
