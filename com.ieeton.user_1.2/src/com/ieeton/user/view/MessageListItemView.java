package com.ieeton.user.view;

import java.util.Date;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.util.DateUtils;
import com.ieeton.user.Constant;
import com.ieeton.user.R;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.AsyncBitmapLoader.ImageCallBack;
import com.ieeton.user.utils.SmileUtils;
import com.ieeton.user.utils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
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
	private IeetonUser mUser;
	private EMConversation mConversation;
	
	/** 和谁的聊天记录 */
	private TextView mUserName;
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
		mConversation.getUserName();
		initView();
	}
	
	public void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.row_chat_history, this);
		mAvatar = (ImageView)findViewById(R.id.avatar);
		
		mUserName = (TextView) findViewById(R.id.name);
		unreadLabel = (TextView) findViewById(R.id.unread_msg_number);
		message = (TextView) findViewById(R.id.message);
		time = (TextView) findViewById(R.id.time);
		mAvatar = (ImageView) findViewById(R.id.avatar);
		msgState = findViewById(R.id.msg_state);
	}

	public void update(EMConversation conversation){
		final String username = conversation.getUserName();
		String nick = Utils.getNickCache(mContext, username);
		if(TextUtils.isEmpty(nick)){
			if (username.equals(NetEngine.getSecretaryID())){
				mUserName.setText(mContext.getString(R.string.secretary_default_name));
				Utils.saveNickCache(mContext, username, mContext.getString(R.string.secretary_default_name));
			}else{
				mUserName.setText(getStrng(mContext, R.string.default_doctor_nickname));
			}
		}else{
			mUserName.setText(nick);
		}
				
		AsyncBitmapLoader.getInstance().loadBitmap(mContext, username, null, mAvatar, new ImageCallBack(){
			@Override
			public void imageLoad(Bitmap bitmap, IeetonUser user) {
				if (user != null){
					mUser = user;
					if (mUser != null){
						mUserName.setText(mUser.getName());
						Utils.saveNickCache(mContext, username, mUser.getName());
					}
				}
			}
			
		});
				
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
			message.setText(SmileUtils.getSmiledText(mContext, getMessageDigest(lastMessage, mContext)),
					BufferType.SPANNABLE);
			//message.setText(getMessageDigest(lastMessage, (this.getContext())));
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
			message.getBody();
			//digest = getStrng(context, R.string.picture) + imageBody.getFileName();
			digest = getStrng(context, R.string.picture);
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
	
	public IeetonUser getUser(){
		return mUser;
	}
	
}
