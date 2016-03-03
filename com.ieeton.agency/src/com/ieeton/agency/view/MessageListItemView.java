package com.ieeton.agency.view;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.util.DateUtils;
import com.ieeton.agency.Constant;
import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.ChatUser;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.AsyncBitmapLoader;
import com.ieeton.agency.utils.SmileUtils;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.utils.AsyncBitmapLoader.ImageCallBack;
import com.ieeton.agency.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class MessageListItemView extends RelativeLayout{
	private Context mContext;
	private String mUid;
	private ChatUser mUser;
	private EMConversation mConversation;
	
	/** 和谁的聊天记录 */
	private TextView mName;
	/** 消息未读数 */
	private TextView unreadLabel;
	/** 最后一条消息的内容 */
	private TextView message;
	/** 最后一条消息的时间 */
	private TextView time;
	/** 用户头像 */
	private ImageView mAvatar;
	/** 最后一条消息的发送状态 */
	private View msgState;
	
	private String type;
	
	public MessageListItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}
	
	public MessageListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public MessageListItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}

	public MessageListItemView(Context context, EMConversation conversation){
		super(context);
		mContext = context;
		mConversation = conversation;
		mUid = mConversation.getUserName();
		initView();
	}
	
	public void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.row_chat_history, this);
		mAvatar = (ImageView)findViewById(R.id.avatar);
		
		mName = (TextView) findViewById(R.id.name);
		unreadLabel = (TextView) findViewById(R.id.unread_msg_number);
		message = (TextView) findViewById(R.id.message);
		time = (TextView) findViewById(R.id.time);
		mAvatar = (ImageView) findViewById(R.id.avatar);
		msgState = findViewById(R.id.msg_state);
	}

	public void update(EMConversation conversation){
		// 获取用户username或者群组groupid
		String username = conversation.getUserName();
		mUid = username;
		String name = Utils.getNickCache(mContext, username);
		if (TextUtils.isEmpty(name)){
			if (username.equals(NetEngine.getFeedbackId())){
				mName.setText(mContext.getString(R.string.xiaomishu));
				Utils.saveNickCache(mContext, username, mContext.getString(R.string.xiaomishu));
			}else{
				mName.setText(mContext.getString(R.string.default_nick));
			}
		}else{
			mName.setText(name);
		}		
		
		type = ChatUser.USER_HUANXIN;
		if (username.equals(NetEngine.getFeedbackId())){
			type = ChatUser.USER_PUBLIC;
		}
		if (type.equals("public")){
			mAvatar.setImageResource(R.drawable.userphoto_secretary);
		}else{
			Bitmap b = AsyncBitmapLoader.getInstance().loadBitmap(mContext, 
					username, null, type, new ImageCallBack() {
				@Override
				public void imageLoad(Bitmap bitmap, Object user) {
					if (bitmap !=null && !bitmap.isRecycled()){
						mAvatar.setImageBitmap(bitmap);
					}else {
						mAvatar.setImageResource(Utils.getDefaultPortraitId(type, user));
					}
					if (user != null){
						mUser = (ChatUser) user;
					}
					String name = Utils.getNickCache(mContext, mUid);
					if (!TextUtils.isEmpty(name)){
						mName.setText(name);
					}
				}
			});
			if (b !=null && !b.isRecycled()){
				mAvatar.setImageBitmap(b);
				if (TextUtils.isEmpty(name)){
					new FetchUserInfoTask().execute();
				}
			}else {
				mAvatar.setImageResource(Utils.getDefaultPortraitId(type, null));
			}
		}
		
		if (conversation.getUnreadMsgCount() > 0) {
			// 显示与此用户的消息未读数
			unreadLabel.setText(String.valueOf(conversation.getUnreadMsgCount()));
			unreadLabel.setVisibility(View.VISIBLE);
		} else {
			unreadLabel.setVisibility(View.INVISIBLE);
		}

		if (conversation.getMsgCount() != 0) {
			// 把最后一条消息的内容作为item的message内容
			EMMessage lastMessage = conversation.getLastMessage();
			message.setText(SmileUtils.getSmiledText(getContext(), getMessageDigest(lastMessage, (this.getContext()))),
					BufferType.SPANNABLE);

			time.setText(DateUtils.getTimestampString(new Date(lastMessage.getMsgTime())));
			if (lastMessage.direct == EMMessage.Direct.SEND && lastMessage.status == EMMessage.Status.FAIL) {
				msgState.setVisibility(View.VISIBLE);
			} else {
				msgState.setVisibility(View.GONE);
			}
		}
	}
	
	private String getStrng(Context context, int resId) {
		return context.getResources().getString(resId);
	}
	
	/**
	 * 根据消息内容和消息类型获取消息内容提示
	 * 
	 * @param message
	 * @param context
	 * @return
	 */
	private String getMessageDigest(EMMessage message, Context context) {
		String digest = "";
		switch (message.getType()) {
		case LOCATION: // 位置消息
			if (message.direct == EMMessage.Direct.RECEIVE) {
				// 从sdk中提到了ui中，使用更简单不犯错的获取string的方法
				// digest = EasyUtils.getAppResourceString(context,
				// "location_recv");
				digest = getStrng(context, R.string.location_recv);
				digest = String.format(digest, message.getFrom());
				return digest;
			} else {
				// digest = EasyUtils.getAppResourceString(context,
				// "location_prefix");
				digest = getStrng(context, R.string.location_prefix);
			}
			break;
		case IMAGE: // 图片消息
			ImageMessageBody imageBody = (ImageMessageBody) message.getBody();
			digest = getStrng(context, R.string.picture) ;//+ imageBody.getFileName();
			break;
		case VOICE:// 语音消息
			digest = getStrng(context, R.string.voice);
			break;
		case VIDEO: // 视频消息
			digest = getStrng(context, R.string.video);
			break;
		case TXT: // 文本消息
			if(!message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL,false)){
				TextMessageBody txtBody = (TextMessageBody) message.getBody();
				digest = txtBody.getMessage();
			}else{
				TextMessageBody txtBody = (TextMessageBody) message.getBody();
				digest = getStrng(context, R.string.voice_call) + txtBody.getMessage();
			}
			break;
		case FILE: // 普通文件消息
			digest = getStrng(context, R.string.file);
			break;
		default:
			System.err.println("error, unknow type");
			return "";
		}

		return digest;
	}
	
	public ChatUser getUser(){
		return mUser;
	}
	
	private class FetchUserInfoTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String result = "";
			//获取小秘书信息
			try {
				result = NetEngine.getInstance(mContext)
								.getNickPortrait(ChatUser.USER_HUANXIN, mUid);
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
			return result;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			if (TextUtils.isEmpty(result)){
//				if(mThr != null){
//					Utils.handleErrorEvent(mThr, mContext);
//				}else{
//					Utils.showToast(mContext, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
//				}

				return;
			}
			JSONObject object = null;
			try {
				object = new JSONObject(result);
				if(!object.getBoolean("error")){
					JSONObject json_data = object.getJSONObject("messages").getJSONObject("data").getJSONObject("user");
					mUser = new ChatUser(mContext, json_data);
					if (!TextUtils.isEmpty(mUser.getName())){
						mName.setText(mUser.getName());
						if (TextUtils.isEmpty(Utils.getNickCache(mContext, mUid))){
							Utils.saveNickCache(mContext, mUid, mUser.getName());
						}
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	}
}
