package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.ieeton.user.IeetonApplication;
import com.ieeton.user.R;
import com.ieeton.user.db.InviteMessgeDao;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.MessageListItemView;
import com.umeng.analytics.MobclickAgent;

/**
 * 显示所有会话记录，比较简单的实现，更好的可能是把陌生人存入本地，这样取到的聊天记录是可控的
 * 
 */
public class ChatAllHistoryFragment extends Fragment implements OnClickListener {

	private SwipeListView listView;

	private ChatAllHistoryAdapter adapter;
	public RelativeLayout errorItem;
	public TextView errorText;
	private boolean hidden;
	private Button mRegisterBtn;
	private Button mLoginBtn;
	private Activity mActivity;
	private RelativeLayout mNoLoginLayout;
	private ListView mListView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_conversation_history, container, false);
		initView(view);
		mActivity = getActivity();
		return view;
	}

	void initView(View view){
		mNoLoginLayout = (RelativeLayout)view.findViewById(R.id.rl_no_login);
		mListView = (ListView)view.findViewById(R.id.list);
		
		if(Utils.getMyType(getActivity()) == 5){
			mNoLoginLayout.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		}else{
			mNoLoginLayout.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);			
		}
		
		mRegisterBtn = (Button)view.findViewById(R.id.btn_register);
		mLoginBtn = (Button)view.findViewById(R.id.btn_login);
		mRegisterBtn.setOnClickListener(this);
		mLoginBtn.setOnClickListener(this);
	}
	
	public void update(){
		if(mNoLoginLayout != null && mListView != null){
			mNoLoginLayout.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		errorItem = (RelativeLayout) getView().findViewById(R.id.rl_error_item);
		errorText = (TextView) errorItem.findViewById(R.id.tv_connect_errormsg);
		errorItem.setVisibility(View.GONE);
		listView = (SwipeListView) getView().findViewById(R.id.list);
		listView.setDivider(getResources().getDrawable(R.drawable.list_divider));
		adapter = new ChatAllHistoryAdapter(getActivity(), 1, loadConversationsWithRecentChat());

		// 设置adapter
		listView.setAdapter(adapter);
		listView.setSwipeListViewListener(new BaseSwipeListViewListener(){

			@Override
			public void onClickFrontView(int position) {
				EMConversation conversation = adapter.getItem(position);
				MessageListItemView v = (MessageListItemView) listView.getChildAt(position);
				//医生数据为空时，还是能查看聊天记录（解决网络信号问题刷新后，点击消息列表无任何响应的问题）
//				if (v.getDoctor() == null){
//					return;
//				}
				String username = conversation.getUserName();
				if (username.equals(IeetonApplication.getInstance().getUserName()))
					Toast.makeText(getActivity(), "不能和自己聊天", Toast.LENGTH_SHORT).show();
				else {
					// 进入聊天页面
					Intent intent = new Intent(getActivity(), ChatActivity.class);
					intent.putExtra(Constants.EXTRA_UID, username);
					if (v != null){
						intent.putExtra(Constants.EXTRA_USER, v.getUser());
					}
					startActivity(intent);
				}
			}
			
		});
		// 注册上下文菜单
		registerForContextMenu(listView);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// if(((AdapterContextMenuInfo)menuInfo).position > 0){ m,
		getActivity().getMenuInflater().inflate(R.menu.delete_message, menu);
		// }
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.delete_message) {
			EMConversation tobeDeleteCons = adapter.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
			// 删除此会话
			EMChatManager.getInstance().deleteConversation(tobeDeleteCons.getUserName(),tobeDeleteCons.isGroup());
			InviteMessgeDao inviteMessgeDao = new InviteMessgeDao(getActivity());
			inviteMessgeDao.deleteMessage(tobeDeleteCons.getUserName());
			adapter.remove(tobeDeleteCons);
			adapter.notifyDataSetChanged();

			// 更新消息未读数
			((MainActivity) getActivity()).updateUnreadLabel();

			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * 刷新页面
	 */
	public void refresh() {
		adapter = new ChatAllHistoryAdapter(getActivity(), R.layout.row_chat_history, loadConversationsWithRecentChat());
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	/**
	 * 获取所有会话
	 * 
	 * @param context
	 * @return
	 */
	private List<EMConversation> loadConversationsWithRecentChat() {
		// 获取所有会话，包括陌生人
		Hashtable<String, EMConversation> conversations = EMChatManager.getInstance().getAllConversations();
		List<EMConversation> conversationList = new ArrayList<EMConversation>();
		//过滤掉messages seize为0的conversation
		for(EMConversation conversation : conversations.values()){
			if(conversation.getAllMessages().size() != 0)
				conversationList.add(conversation);
		}
		// 排序
		sortConversationByLastChatTime(conversationList);
		return conversationList;
	}

	/**
	 * 根据最后一条消息的时间排序
	 * 
	 * @param usernames
	 */
	private void sortConversationByLastChatTime(List<EMConversation> conversationList) {
		Collections.sort(conversationList, new Comparator<EMConversation>() {
			@Override
			public int compare(final EMConversation con1, final EMConversation con2) {

				EMMessage con2LastMessage = con2.getLastMessage();
				EMMessage con1LastMessage = con1.getLastMessage();
				if (con2LastMessage.getMsgTime() == con1LastMessage.getMsgTime()) {
					return 0;
				} else if (con2LastMessage.getMsgTime() > con1LastMessage.getMsgTime()) {
					return 1;
				} else {
					return -1;
				}
			}

		});
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if (!hidden) {
			refresh();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("ChatAllHistoryFragment"); 
		if (!hidden) {
			refresh();
		}
	}

	@Override
	public void onPause() {
		MobclickAgent.onPageEnd("ChatAllHistoryFragment"); 
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		if(v == mLoginBtn){
			startActivity(new Intent(getActivity(), LoginActivity.class));
//			getActivity().finish();
		}else if(v == mRegisterBtn){
			startActivity(new Intent(getActivity(), RegisterActivity.class));
//			getActivity().finish();
		}
	}

	
	public class ChatAllHistoryAdapter extends ArrayAdapter<EMConversation> {
		public ChatAllHistoryAdapter(Context context, int textViewResourceId, List<EMConversation> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// 获取与此用户/群组的会话
			EMConversation conversation = getItem(position);			
			MessageListItemView view;
			if (convertView == null) {
				view = new MessageListItemView(mActivity, conversation);
			}else{
				view = (MessageListItemView) convertView;
			}
			
			view.update(conversation);
			Button delete = (Button)view.findViewById(R.id.delete_btn);
			delete.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//关闭左滑偏移
					listView.closeOpenedItems();
					
					EMConversation tobeDeleteCons = adapter.getItem(position);
					// 删除此会话
					EMChatManager.getInstance().deleteConversation(tobeDeleteCons.getUserName(),tobeDeleteCons.isGroup());
					InviteMessgeDao inviteMessgeDao = new InviteMessgeDao(getActivity());
					inviteMessgeDao.deleteMessage(tobeDeleteCons.getUserName());
					adapter.remove(tobeDeleteCons);
					adapter.notifyDataSetChanged();

					// 更新消息未读数
					((MainActivity) getActivity()).updateUnreadLabel();
				}
			});

			return view;
		}

	}
	
}
