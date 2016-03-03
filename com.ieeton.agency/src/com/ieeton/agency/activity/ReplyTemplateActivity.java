package com.ieeton.agency.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.ReplyTemplate;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.view.PullDownView;
import com.ieeton.agency.view.ReplyItemView;
import com.ieeton.agency.view.PullDownView.UpdateHandle;
import com.ieeton.agency.R;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ReplyTemplateActivity extends TemplateActivity implements 
				UpdateHandle, OnScrollListener{
	private class FechQuickReplyTask extends AsyncTask<Integer, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			int page = params[0];
			try {
				result = NetEngine.getInstance(ReplyTemplateActivity.this).
								getQuickReplyList(Utils.getPassport(ReplyTemplateActivity.this), 
										20, page);
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
			mIsTaskFree = true;
			dismissProgress();
			if (mLoadMode == Constants.LOAD_MORE){
				mPageNum--;
			}
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
			dismissProgress();
			mPullDownView.endUpdate(null);
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(ReplyTemplateActivity.this, R.string.get_remark_failed, Toast.LENGTH_SHORT);
				}
				if (mLoadMode == Constants.LOAD_MORE){
					mPageNum--;
				}
				return;
			}
			processResult(result);
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	private class AddReplyTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(ReplyTemplateActivity.this)
									.addQuickReply(Utils.getPassport(ReplyTemplateActivity.this), 
											mContent);
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
		protected void onPostExecute(String result) {
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(ReplyTemplateActivity.this, R.string.add_remark_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			JSONObject obj;
			try {
				obj = new JSONObject(result);
				JSONObject data = obj.optJSONObject("messages").optJSONObject("data");
				int id = data.optInt("id");
				ReplyTemplate reply = new ReplyTemplate(id, mContent);
				if (mList == null){
					mList = new ArrayList<ReplyTemplate>();
				}
				mList.add(0, reply);
				mAdapter.notifyDataSetChanged();
				showEmpty(false);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	public class DeleteReplyTask extends AsyncTask<Integer, Void, String>{
		private Throwable mThr;
		private int replyId;
		
		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			replyId = params[0];
			try {
				result = NetEngine.getInstance(ReplyTemplateActivity.this)
									.delQuickReply(Utils.getPassport(ReplyTemplateActivity.this), 
											replyId);
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
		protected void onPostExecute(String result) {
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(ReplyTemplateActivity.this, R.string.operation_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			Utils.showToast(ReplyTemplateActivity.this, R.string.operation_succes, Toast.LENGTH_SHORT);
			deleteItem(replyId);
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	private class ReplyListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if (mList != null && !mList.isEmpty()){
				return mList.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ReplyItemView view;
			if (convertView == null){
				view = new ReplyItemView(ReplyTemplateActivity.this);
			}else{
				view = (ReplyItemView) convertView;
			}
			if (mList != null && mList.size() > 0 && position < mList.size()){
				view.update((ReplyTemplate) mList.get(position), position);
			}
			Button btn = (Button) view.findViewById(R.id.id_unfollow);
			btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new DeleteReplyTask().execute(mList.get(position).getId());
				}
			});
			return view;
		}
		
	}
	private int REQUEST_REPLY = 1;
	
	private SwipeListView mListView;
	private TextView mTvEmpty;
	private PullDownView mPullDownView;
	
	private List<ReplyTemplate> mList;
	private ReplyListAdapter mAdapter;
	private CustomToast mProgressDialog;
	private FechQuickReplyTask mTask;
	private int mPageNum = 1;
	private String mContent;
	private int mLoadMode = Constants.LOAD_REFRESH;
	private boolean mIsTaskFree = true;

	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			Intent intent = new Intent(this, EditActivity.class);
			intent.putExtra(EditActivity.EXTRA_MODE, EditActivity.MODE_REPLY);
			startActivityForResult(intent, REQUEST_REPLY);
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.reply_template);
		setTitleBar(getString(R.string.back), getString(R.string.title_quick_reply), 
						getString(R.string.title_new));
		
		mTvEmpty = (TextView) findViewById(R.id.empty_text);
		mPullDownView = (PullDownView) findViewById(R.id.pulldown_view);
		mPullDownView.setUpdateHandle(this);
		
		mListView = (SwipeListView) findViewById(R.id.list);
		mAdapter = new ReplyListAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(this);
		mListView.setSwipeListViewListener(new BaseSwipeListViewListener(){

			@Override
			public void onClickFrontView(int position) {
				if (mList != null && position<mList.size()){
					Intent data = new Intent();
					data.putExtra(ChatActivity.EXTRA_QUICK_REPLY, 
							((ReplyTemplate) mList.get(position)).getContent());
					setResult(RESULT_OK, data);
					finish();
				}
				super.onClickFrontView(position);
			}

		});
		//mListView.setOnItemClickListener(this);
		
		if (mList == null || mList.isEmpty()){
			refreshData(Constants.LOAD_REFRESH);
		}
	}

	private void refreshData(int mode){
		mLoadMode = mode;
		if (!mIsTaskFree){
			return;
		}
		if (mode == Constants.LOAD_REFRESH){
			mPageNum = 1;
			mPullDownView.update();
		}else{
			mPageNum += 1;
			mPullDownView.updateWithoutOffset();
		}
		mTask = new FechQuickReplyTask();
		try {
			mTask.execute(mPageNum);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK){
			return;
		}
		if (requestCode == REQUEST_REPLY){
			if (data == null){
				return;
			}
			mContent = data.getStringExtra(EditActivity.INPUT_CONTENT);
			addReply();
		}
		super.onActivityResult(requestCode, resultCode, data);
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
	
	private void processResult(String result){
		if (TextUtils.isEmpty(result)){
			return;
		}
		JSONObject obj;
		try {
			obj = new JSONObject(result);
			JSONObject data = obj.optJSONObject("messages").optJSONObject("data");
			JSONArray array = data.optJSONArray("replys");
			if (array == null || array.length() == 0){
				if (mLoadMode == Constants.LOAD_MORE){
					mPageNum--;
				}
				return;
			}
			if (mList == null){
				mList = new ArrayList<ReplyTemplate>();
			}
			List<ReplyTemplate> list = new ArrayList<ReplyTemplate>();
			for(int i=0; i<array.length(); i++){
				ReplyTemplate item = new ReplyTemplate(array.getJSONObject(i));
				list.add(item);
			}
			if (mLoadMode == Constants.LOAD_REFRESH){
				mList.clear();
			}
			mList.addAll(list);
			mAdapter.notifyDataSetChanged();
			if (mList.size()>0){
				showEmpty(false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void showEmpty(boolean show){
		if (show){
			mTvEmpty.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		}else{
			mTvEmpty.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		}
	}
	
	private void addReply(){
		new AddReplyTask().execute();
	}

	private void deleteItem(int id){
		if (mList == null || mList.isEmpty()){
			return;
		}
		for (ReplyTemplate item : mList){
			if (item.getId() == id){
				mList.remove(item);
				mAdapter.notifyDataSetChanged();
				mListView.closeOpenedItems();
				break;
			}
		}
	}
	
	private boolean retrievable;
	private boolean isEnd;
	protected int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
			isEnd = true;
		} else {
			isEnd = false;
		}
		if (firstVisibleItem == 0) {
			mScrollState = SCROLL_STATE_IDLE;
		}


		if ((firstVisibleItem + visibleItemCount == totalItemCount - 5 || firstVisibleItem
						+ visibleItemCount >= totalItemCount - 1)) 
		{
			retrievable = true;
		} else {
			retrievable = false;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (isEnd) {
			mScrollState = SCROLL_STATE_IDLE;
		} else {
			mScrollState = scrollState;
		}
		if (scrollState == SCROLL_STATE_IDLE && retrievable) {
			retrievable = false;
			refreshData(Constants.LOAD_MORE);
		}
		
	}

	@Override
	public void onUpdate() {
		refreshData(Constants.LOAD_REFRESH);
	}
}
